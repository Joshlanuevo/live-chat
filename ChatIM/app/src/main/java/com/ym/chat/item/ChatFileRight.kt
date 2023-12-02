package com.ym.chat.item

import android.graphics.BitmapFactory
import android.graphics.drawable.ClipDrawable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import coil.request.CachePolicy
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.FileMsgBean
import com.ym.chat.bean.ImageBean
import com.ym.chat.databinding.ItemFileChatRightBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.PictureActivity
import com.ym.chat.utils.*
import okhttp3.internal.filterList

/**
 * 文件消息item-右边
 */
class ChatFileRight(
    private val onChatItemListener: OnChatItemListener,
    private val onClickListener: ((bean: ChatMessageBean, position: Int) -> Unit)? = null,
) : QuickVBItemBinderPro<ChatMessageBean, ItemFileChatRightBinding>() {

    init {
        addChildClickViewIds(R.id.layoutFile)
        addChildLongClickViewIds(R.id.layoutFile)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemFileChatRightBinding {
        return ItemFileChatRightBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(holder: BinderVBHolder<ItemFileChatRightBinding>, data: ChatMessageBean) {

        holder.viewBinding.layoutHeader.flHeader.gone()
//        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
//            //需要显示对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.visible()
//        } else {
//            //不需要现实对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.gone()
//        }

        holder.viewBinding.progress.bindMsg(data)
        holder.viewBinding.tvTime.text = getTimeStr(data.createTime)

        try {
            if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
                MMKVUtils.getUser()?.id?.let {
                    try {
                        ChatDao.getGroupDb().getMemberInGroup(it, data.groupId)?.let { member ->
                            holder.viewBinding.layoutHeader.ivHeader.loadImg(member)
                            Utils.showDaShenImageView(
                                holder.viewBinding.layoutHeader.ivHeaderMark,
                                member
                            )
                        }
                    } catch (e: Exception) {
                        "---------获取成员数据异常-${e.message.toString()}".logE()
                    }
                }
            } else {
                val userBean = MMKVUtils.getUser()
//            显示自己头像
                holder.viewBinding.layoutHeader.ivHeader.loadImg(userBean)
                Utils.showDaShenImageView(
                    holder.viewBinding.layoutHeader.ivHeaderMark,
                    userBean?.displayHead == "Y",
                    userBean?.levelHeadUrl
                )
            }
            holder.viewBinding.loadView.gone()
            holder.viewBinding.ivFail.gone()
            try {
                val fileMsg = GsonUtils.fromJson(data.content, FileMsgBean::class.java)
                holder.viewBinding.tvName.text = fileMsg.name
                holder.viewBinding.tvSize.text = fileMsg.size

                val (isExit, id) = DownloadUtil.queryExist(context, fileMsg.url)
                if (isExit) {
                    (holder.viewBinding.ivIcon2.drawable as ClipDrawable).level = 0
                    holder.viewBinding.ivDownload.text = ""
                } else {
                    (holder.viewBinding.ivIcon2.drawable as ClipDrawable).level =
                        (10000 * (1 - data.downloadProcess)).toInt()
                    holder.viewBinding.ivDownload.text = context.getString(R.string.xiazai)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //显示发送状态
            when (data.sendState) {
                0 -> {
                    //发送中
                    holder.viewBinding.loadView.startAnimotion()
                    holder.viewBinding.loadView.visible()
                    holder.viewBinding.ivFail.gone()
                    holder.viewBinding.progress.visible()
                    holder.viewBinding.ivRead.gone()
                }
                2 -> {
                    //发送失败
                    holder.viewBinding.loadView.gone()
                    holder.viewBinding.progress.gone()
                    holder.viewBinding.ivFail.visible()
                    holder.viewBinding.ivRead.gone()
                    //消息重发
                    holder.viewBinding.ivFail.click {
                        onChatItemListener.reSendMsg(data)
                    }
                }
                else -> {
                    //发送成功
                    holder.viewBinding.loadView.gone()
                    holder.viewBinding.progress.gone()
                    holder.viewBinding.ivFail.gone()
//                    holder.viewBinding.ivRead.visible()
//                    if (data.msgReadState == 1) {
//                        holder.viewBinding.ivRead.setImageResource(R.drawable.ic_chat_read)
//                    } else {
//                        holder.viewBinding.ivRead.setImageResource(R.drawable.ic_chat_unread)
//                    }
                }
            }

            //显示回复
            if (!TextUtils.isEmpty(data.parentMessageId)) {
                //处理回复消息
                //处理回复消息
                holder.viewBinding.let { view ->
                    val list = adapter.data.filterList {
                        this is ChatMessageBean && this.id == data.parentMessageId
                    }
//                val parentMsg = ChatDao.getChatMsgDb().getMsgById(data.parentMessageId)
                    if (list != null && list.size > 0) {
                        val parentMsg = list[0] as ChatMessageBean
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onChildClick(
        holder: BinderVBHolder<ItemFileChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        if (view.id == R.id.layoutFile) {
            onClickListener?.invoke(data, position)
        }
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemFileChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.layoutFile) {
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
}
