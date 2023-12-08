package com.ym.chat.item

import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.ContactCardMsgBean
import com.ym.chat.databinding.ItemContactCardChatRightBinding
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.PopUtils
import com.ym.chat.utils.Utils
import okhttp3.internal.filterList
import org.json.JSONObject

/**
 * 名片消息item-右边
 */
class ChatContactCardRight(
    private val onChatItemListener: OnChatItemListener,
    private val onClickListener: ((bean: ContactCardMsgBean, position: Int) -> Unit)? = null,
) : QuickVBItemBinderPro<ChatMessageBean, ItemContactCardChatRightBinding>() {

    init {
        addChildLongClickViewIds(R.id.layoutFile)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemContactCardChatRightBinding {
        return ItemContactCardChatRightBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(
        holder: BinderVBHolder<ItemContactCardChatRightBinding>,
        data: ChatMessageBean
    ) {

        holder.viewBinding.layoutHeader.ivHeader.gone()
//        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
//            //需要显示对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.visible()
//        } else {
//            //不需要现实对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.gone()
//        }

        holder.viewBinding.tvTime.text = getTimeStr(data.createTime)

        try {
            val userBean = MMKVUtils.getUser()
            //显示自己头像
            holder.viewBinding.layoutHeader.ivHeader.apply {
                setRoundRadius(72F)
                setChatId(userBean?.id?:"")
                setChatName(userBean?.name?:"")
            }.showUrl(userBean?.headUrl)

            holder.viewBinding.loadView.gone()
            holder.viewBinding.ivFail.gone()
            try {
                val contactBean = if (data.operationType == "Forward") {
                    val c = JSONObject(data.content).optString("content")
                    GsonUtils.fromJson(c, ContactCardMsgBean::class.java)
                } else {
                    GsonUtils.fromJson(data.content, ContactCardMsgBean::class.java)
                }
                holder.viewBinding.tvName.text = contactBean.shareMemberName
                val defaultDrawable = Utils.getFirstNameDrawable(context,contactBean.shareMemberName)
                if(!TextUtils.isEmpty(contactBean.shareMemberHeadUrl)){
                    holder.viewBinding.ivIcon.load(contactBean.shareMemberHeadUrl){
                        placeholder(R.drawable.ic_mine_header)
                        error(defaultDrawable)
                    }
                }else{
                    holder.viewBinding.ivIcon.load(defaultDrawable)
                }
                holder.viewBinding.layoutFile.click {
                    onClickListener?.invoke(contactBean, holder.adapterPosition)
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
            ChatUtils.showTopMsg(holder.viewBinding.ivTop, data.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onChildClick(
        holder: BinderVBHolder<ItemContactCardChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemContactCardChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.layoutFile) {
            if (data.sendState == 0) return true //文件发送中不弹popup
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
