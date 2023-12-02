package com.ym.chat.item

import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.SizeUtils
import com.ym.base.ext.dp2Px
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.config.Config
import com.ym.chat.databinding.ItemChatAudioRightBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.ui.ChatActivity
import com.ym.chat.utils.*
import okhttp3.internal.filterList
import org.json.JSONObject

/**
 * @Description
 * @Author：
 * @Date：2021-08-04
 * @Time：16:33
 */
class ChatAudioRight(
    private val onChatItemListener: OnChatItemListener,
    private val onPlayClickListener: ((bean: ChatMessageBean, position: Int) -> Unit)? = null,
) : QuickVBItemBinderPro<ChatMessageBean, ItemChatAudioRightBinding>() {
    init {
        addChildLongClickViewIds(R.id.llContentRight)
        addChildClickViewIds(R.id.llContentRight, R.id.clAudio)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemChatAudioRightBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemChatAudioRightBinding>, data: ChatMessageBean) {

        holder.viewBinding.layoutHeader.flHeader.gone()
//        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
//            //需要显示对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.visible()
//        } else {
//            //不需要现实对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.gone()
//        }

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
            //显示自己头像
            holder.viewBinding.layoutHeader.ivHeader.loadImg(userBean)
            Utils.showDaShenImageView(
                holder.viewBinding.layoutHeader.ivHeaderMark,
                userBean?.displayHead == "Y",
                userBean?.levelHeadUrl
            )
        }

        val defaultWidth = 100.dp2Px()
        holder.viewBinding.let { vb ->
            try {
                val jsonObject = JSONObject(data.content)
                val time = jsonObject.optLong("time")
//                val url = jsonObject.optLong("url")
                vb.durationTextViewRight.text = "${time}''"

                var duration = time
                if (duration > Config.audioDuration) {
                    duration = Config.audioDuration.toLong()
                } else if (duration < 1) {
                    duration = 1
                }
                val increment: Long =
                    (DeviceInfoUtils.getDeviceWidth(context) / 2 - defaultWidth) / Config.audioDuration * duration
                val params: ViewGroup.LayoutParams = holder.viewBinding.llContentRight.layoutParams
                params.width = (defaultWidth + increment).toInt()
                holder.viewBinding.llContentRight.layoutParams = params
            } catch (e: Exception) {
                e.printStackTrace()
                val params: ViewGroup.LayoutParams = holder.viewBinding.llContentRight.layoutParams
                params.width = defaultWidth
                holder.viewBinding.llContentRight.layoutParams = params
            }

            if (data.isPlaying) {
                val animation = vb.audioImageViewRight.background as? AnimationDrawable
                if (animation?.isRunning == false) {
                    animation.start()
                }
            } else {
                vb.audioImageViewRight.background = null
                vb.audioImageViewRight.setBackgroundResource(R.drawable.audio_animation_right_list)
            }
            vb.ivSelect.setImageResource(if (data.isSel) R.drawable.ic_group_chat_member_select else R.drawable.ic_check_normal)
            vb.tvTime.text = getTimeStr(data.createTime)
        }

        //显示发送状态
        when (data.sendState) {
            0 -> {
                //发送中
                holder.viewBinding.loadView.visible()
                holder.viewBinding.loadView.startAnimotion()
                holder.viewBinding.ivFail.gone()
                holder.viewBinding.ivRead.gone()
            }
            2 -> {
                //发送失败
                holder.viewBinding.loadView.gone()
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
                holder.viewBinding.ivFail.gone()
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
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemChatAudioRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
//        if (view.id == R.id.llContent) {
//            //长按选中
//            showPopupWindow(view, data, position)
//        } else
        if (view.id == R.id.llContentRight) {
            val type = PopUtils.calculatePopWindowPos(view)
            if(data.sendState == 0) return true //文件发送中不弹popup
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
        holder: BinderVBHolder<ItemChatAudioRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        when (view.id) {
            R.id.llContentRight -> {
                if (data.isEditMode) onChatItemListener.onItemClick(data, position)
                else onPlayClickListener?.invoke(data, position)
            }
            R.id.clAudio -> {
                onChatItemListener.onItemClick(data, position)
            }
        }
    }
}