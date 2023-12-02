package com.ym.chat.item

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.google.android.flexbox.JustifyContent
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.databinding.ItemTxtChatRightBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.ContactActivity
import com.ym.chat.ui.FriendInfoActivity
import com.ym.chat.utils.*
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener
import okhttp3.internal.filterList
import java.io.Serializable


/**
 * 文字聊天窗口-右边
 */
class ChatTextRight(
    private val onChatItemListener: OnChatItemListener,
) : QuickVBItemBinderPro<ChatMessageBean, ItemTxtChatRightBinding>() {

    init {
        addChildLongClickViewIds(R.id.llTxtContent, R.id.tvContentRight)
        addChildClickViewIds(R.id.llTxtContent, R.id.tvContentRight)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemTxtChatRightBinding {
        return ItemTxtChatRightBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(holder: BinderVBHolder<ItemTxtChatRightBinding>, data: ChatMessageBean) {

        val userBean = MMKVUtils.getUser()

        holder.viewBinding.layoutHeader.flHeader.gone()

//        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
//            //需要显示对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.visible()
//        } else {
//            //不需要现实对方昵称和头像
//            holder.viewBinding.layoutHeader.flHeader.gone()
//            val layoutParams = holder.viewBinding.layoutMsg.layoutParams
////            if (layoutParams is RelativeLayout.LayoutParams) {
////                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
////            }
////            holder.viewBinding.layoutMsg.layoutParams = layoutParams
//        }

        //编辑
//        if ("Modify" == data.operationType) {
//            holder.viewBinding.tvEdit.visible()
//        } else {
//            holder.viewBinding.tvEdit.gone()
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
            //显示自己头像
            holder.viewBinding.layoutHeader.ivHeader.loadImg(userBean)
            Utils.showDaShenImageView(
                holder.viewBinding.layoutHeader.ivHeaderMark,
                userBean?.displayHead == "Y",
                userBean?.levelHeadUrl
            )
        }

        holder.viewBinding.tvContentRight.movementMethod =
            LinkMovementMethod.getInstance()//不设置点击会失效
        holder.viewBinding.tvContentRight.text = AtUserHelper.parseAtUserLinkJx(data.content,
            ContextCompat.getColor(context, R.color.color_at), object : AtUserLinkOnClickListener {
                override fun ulrLinkClick(str: String?) {
                    str?.let {
                        val linkUrl = if (str.contains("http") || str.contains("https")) {
                            str
                        } else {
                            "http://$str"
                        }
                        try {
                            val uri: Uri = Uri.parse(linkUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            context.getString(R.string.cuowudelianjiedizhi).toast()
                        }
                    }
                }

                override fun atUserClick(str: String?) {
                    /**获取@人 的群成员信息*/
                    val groupMember =
                        str?.let { ChatDao.getGroupDb().getMemberInGroup(it, data.groupId) }

                    /**获取自己 在群的成员信息*/
                    val groupMemberUser =
                        str?.let {
                            MMKVUtils.getUser()?.id?.let { it1 ->
                                ChatDao.getGroupDb().getMemberInGroup(
                                    it1, data.groupId
                                )
                            }
                        }
                    if (groupMemberUser != null && groupMemberUser.role.lowercase() != "Normal".lowercase()) {
                        /**只有自己是管理员获取群主才能查看资料*/
                        val chatInfo =
                            groupMember?.let { ChatMsgUtils.groupMemberCopyFriendListBean(it) }
                        if (chatInfo != null)
                            context.startActivity(
                                Intent(
                                    context, FriendInfoActivity::class.java
                                ).putExtra(ChatActivity.CHAT_INFO, chatInfo as Serializable)
                                    .putExtra(ContactActivity.IN_TYPE, 1)
                            )
                    }
                }

                override fun phoneClick(str: String?) {
                }
            })
        holder.viewBinding.tvTime.text = getTimeStr(data.createTime)

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
            }
            2 -> {
                //发送失败
                holder.viewBinding.loadView.gone()
                holder.viewBinding.ivFail.visible()
                holder.viewBinding.ivRead.gone()
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
//            holder.viewBinding.View.layoutParams.width = 0
        }

        //跳转闪烁
        if (data.isHighlight) {
            ChatUtils.highlightBackground(holder.viewBinding.root) {
                data.isHighlight = false
            }
        }

        //置顶消息显示
        if (!TextUtils.isEmpty(data.editId)) {
            ChatUtils.showTopMsg(holder.viewBinding.ivTop, data.editId)
        } else {
            ChatUtils.showTopMsg(holder.viewBinding.ivTop, data.id)
        }
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemTxtChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.llTxtContent || view.id == R.id.tvContentRight) {
            val type = PopUtils.calculatePopWindowPos(view)
            val gravityType = when (type[0]) {
                1 -> Gravity.BOTTOM
                2 -> Gravity.TOP
                else -> Gravity.TOP
            }
            val offsetY = when (type[0]) {
                1 -> SizeUtils.dp2px(10.0f)
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
        holder: BinderVBHolder<ItemTxtChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        //item点击事件
        val itemBean = adapter.data[position] as ChatMessageBean
        if (itemBean.isEditMode) {
            val sel = itemBean.isSel
            itemBean.isSel = !sel
            adapter.notifyItemChanged(position)
        }
    }
}