package com.ym.chat.item

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.ym.base.ext.logE
import com.ym.base.ext.toFile
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.VideoMsgBean
import com.ym.chat.databinding.ItemChatVideoRightBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.ui.ChatActivity
import com.ym.chat.utils.*
import okhttp3.internal.filterList
import org.json.JSONObject

/**
 * @Description
 * @Author：CASE
 * @Date：2021-08-04
 * @Time：16:33
 */
class ChatVideoRight(
    private val onChatItemListener: OnChatItemListener,
    private val onPlayClickListener: ((bean: ChatMessageBean) -> Unit)? = null,
) : QuickVBItemBinderPro<ChatMessageBean, ItemChatVideoRightBinding>() {

    init {
        addChildLongClickViewIds(R.id.flContentRight)
        addChildClickViewIds(R.id.flContentRight, R.id.clVideo)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemChatVideoRightBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemChatVideoRightBinding>, data: ChatMessageBean) {

        holder.viewBinding.layoutHeader.ivHeader.gone()
//        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
//            //需要显示对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.visible()
//        } else {
//            //不需要现实对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.gone()
//        }

        holder.viewBinding.progress.bindMsg(data)
        holder.viewBinding.tvTime.text = getTimeStr(data.createTime)

        val userBean = MMKVUtils.getUser()
        //显示自己头像
        holder.viewBinding.layoutHeader.ivHeader.apply {
            setRoundRadius(72F)
            setChatId(userBean?.id?:"")
            setChatName(userBean?.name?:"")
        }.showUrl(userBean?.headUrl)

        holder.viewBinding.let { vb ->
            vb.loadView.gone()
            vb.ivFail.gone()

            val videoInfo = if (data.operationType == "Forward") {
                val c = JSONObject(data.content).optString("content")
//                val original = JSONObject(data.content).optString("original")
//                holder.viewBinding.tvFromUserName.visible()
//                holder.viewBinding.tvFromUserName.text = "消息转发来自：${original}"
                GsonUtils.fromJson(c, VideoMsgBean::class.java)
            } else {
                holder.viewBinding.tvFromUserName.gone()
                GsonUtils.fromJson(data.content, VideoMsgBean::class.java)
            }
            data.videoInfo = videoInfo
            videoInfo?.apply {
                try {
                    val imageSize: IntArray = WeChatImageUtils.getImageSizeByOrgSizeToWeChat(
                        this.width,
                        this.height,
                        context
                    )
                    val width =
                        imageSize[0]//if (imageSize[0] in 1..400) imageSize[0] else SizeUtils.dp2px(200f)
                    val height =
                        imageSize[1]//if (imageSize[1] in 1..400)  else SizeUtils.dp2px(200f)
                    vb.ivCoverRight.layoutParams.width = width
                    vb.ivCoverRight.layoutParams.height = height
                    holder.viewBinding.consReply.layoutParams.width = width

                } catch (e: Exception) {
                    vb.ivCoverRight.layoutParams.width = 200
                    vb.ivCoverRight.layoutParams.height = 200
                    holder.viewBinding.consReply.layoutParams.width = 200
                }
            }


            if(videoInfo?.coverUrl?.startsWith("http")==true){
                vb.ivCoverRight.load(videoInfo?.coverUrl)
            }else{
                vb.ivCoverRight.load(data.localPath.toFile())
            }

            vb.ivSelect.setImageResource(if (data.isSel) R.drawable.ic_group_chat_member_select else R.drawable.ic_check_normal)
        }

        //消息重发
        holder.viewBinding.ivFail.click {
            onChatItemListener.reSendMsg(data)
        }

        //显示发送状态
        when (data.sendState) {
            0 -> {
                //发送中
                holder.viewBinding.loadView.visible()
                holder.viewBinding.loadView.startAnimotion()
                holder.viewBinding.ivFail.gone()
                holder.viewBinding.ivRead.gone()
                holder.viewBinding.progress.visible()
            }
            2 -> {
                //发送失败
                holder.viewBinding.loadView.gone()
                holder.viewBinding.ivFail.visible()
                holder.viewBinding.ivRead.gone()
                holder.viewBinding.progress.gone()
                //消息重发
                holder.viewBinding.ivFail.click {
                    onChatItemListener.reSendMsg(data)
                }
            }
            else -> {
                //发送成功
                holder.viewBinding.loadView.gone()
                holder.viewBinding.ivFail.gone()
                holder.viewBinding.progress.gone()
//                holder.viewBinding.ivRead.visible()
//                if (data.msgReadState == 1) {
//                    holder.viewBinding.ivRead.setImageResource(R.drawable.ic_chat_read)
//                } else {
//                    holder.viewBinding.ivRead.setImageResource(R.drawable.ic_chat_unread)
//                }
            }
        }

        //显示回复
        if (!TextUtils.isEmpty(data.parentMessageId)) {
            //处理回复消息
            holder.viewBinding.let { view ->
                var parentMsg = data.replayParentMsg
                if (parentMsg == null) {
                    val list = adapter.data.filterList {
                        this is ChatMessageBean && this.id == data.parentMessageId
                    }
                    if (list != null && list.size > 0) {
                        parentMsg = list[0] as ChatMessageBean
                    }
                }
                if (parentMsg != null) {
                    holder.viewBinding.consReply.visible()
                    holder.viewBinding.consReply.click {
                        onChatItemListener.clickReplyMsg(parentMsg)
                    }
                    ChatUtils.showRelyMsg(
                        context,
                        parentMsg,
                        view.ivReplyPreview,
                        view.tvReplyName,
                        view.tvReplyContent
                    )
                } else {
                    holder.viewBinding.consReply.gone()
                }
            }
        } else {
            holder.viewBinding.consReply.gone()
        }

        //跳转闪烁
        if (data.isHighlight) {
            ChatUtils.highlightBackground(holder.viewBinding.root) {
                data.isHighlight = false
            }
        }

        //置顶消息显示
        ChatUtils.showTopMsg(holder.viewBinding.ivTop,data.id)
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemChatVideoRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.flContent || view.id == R.id.flContentRight) {
            if(data.sendState == 0) return true //文件发送中不弹popup
            val type = PopUtils.calculatePopWindowPos(view)
            val gravityType = when (type[0]) {
                1 -> Gravity.BOTTOM
                2 -> Gravity.TOP
                else -> Gravity.TOP
            }
            val offsetY = when (type[0]) {
                1 -> SizeUtils.dp2px(0.0f)
                2 -> type[1]
                else -> SizeUtils.dp2px(-0.0f)
            }
            //长按选中
            ChatUtils.showPopupWindow(
                context,
                view,
                data,
                position,
                onChatItemListener,
                gravityType = gravityType,
                offsetY = offsetY
            )
        }
        return true
    }

    override fun onChildClick(
        holder: BinderVBHolder<ItemChatVideoRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        when (view.id) {
            R.id.flContent, R.id.flContentRight -> {
                onPlayClickListener?.invoke(data)
            }
        }
    }
}