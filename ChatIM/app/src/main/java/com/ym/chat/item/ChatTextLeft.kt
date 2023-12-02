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
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.databinding.ItemTxtChatLeftBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.popup.ChatHeaderPopupWindow
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.ContactActivity
import com.ym.chat.ui.FriendInfoActivity
import com.ym.chat.utils.*
import com.ym.chat.utils.StringExt.showInGroupName
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener
import okhttp3.internal.filterList
import java.io.Serializable

/**
 * 文字聊天窗口-左边
 */
class ChatTextLeft(
    private val onChatItemListener: OnChatItemListener
) : QuickVBItemBinderPro<ChatMessageBean, ItemTxtChatLeftBinding>() {

    init {
        addChildLongClickViewIds(R.id.llContentLeft, R.id.tvContentLeft, R.id.layout_header)
        addChildClickViewIds(R.id.tvContentLeft, R.id.tvContentRight, R.id.layout_header)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemTxtChatLeftBinding {
        return ItemTxtChatLeftBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(holder: BinderVBHolder<ItemTxtChatLeftBinding>, data: ChatMessageBean) {

        holder.viewBinding.tvContentLeft.movementMethod =
            LinkMovementMethod.getInstance()//不设置点击会失效
        holder.viewBinding.tvContentLeft.text = AtUserHelper.parseAtUserLinkJx(data.content,
            ContextCompat.getColor(context, R.color.color_at),
            object : AtUserLinkOnClickListener {
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

        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
            //需要显示对方昵称和头像
            showHeader(holder, data)
        } else {
            //不需要现实对方昵称和头像
            holder.viewBinding.tvFromUserName.gone()
            holder.viewBinding.layoutHeader.flHeader.gone()
        }

        //编辑
//        if ("Modify" == data.operationType) {
//            holder.viewBinding.tvEdit.visible()
//        } else {
//            holder.viewBinding.tvEdit.gone()
//        }

        //消息已读回执
        if (data.msgReadState == 0) {
            onChatItemListener.readCallBack(data)
        }

        //显示多选
        holder.viewBinding.ivSelectR.gone()

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

    /**
     * 显示头像
     */
    private fun showHeader(holder: BinderVBHolder<ItemTxtChatLeftBinding>, data: ChatMessageBean) {
        holder.viewBinding.tvFromUserName.visible()
        holder.viewBinding.layoutHeader.flHeader.visible()
        try {
            var member = ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)
            if (member != null) {
                holder.viewBinding.tvFromUserName.text = member.nickname.showInGroupName(member.role)
                holder.viewBinding.layoutHeader.ivHeader.loadImg(member)
                Utils.showDaShenImageView(holder.viewBinding.layoutHeader.ivHeaderMark, member)
            } else {
                holder.viewBinding.tvFromUserName.text = data.fromName
                holder.viewBinding.layoutHeader.ivHeader.loadImg("", data.fromName, "")
            }
        } catch (e: Exception) {
            holder.viewBinding.tvFromUserName.text = data.fromName
            holder.viewBinding.layoutHeader.ivHeader.loadImg("", data.fromName, "")
            "---------获取成员数据异常-${e.message.toString()}".logE()
        }
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemTxtChatLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.llContentLeft || view.id == R.id.tvContentLeft) {
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
        } else if (view.id == R.id.layout_header) {
            //长按头像
            showChatHeaderPopup(view, data)
//            onChatItemListener.onItemHeaderLongClick(data)
        }
        return true
    }

    override fun onChildClick(
        holder: BinderVBHolder<ItemTxtChatLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        if (view.id == R.id.tvContentLeft) {
            //item点击事件
            val itemBean = adapter.data[position] as ChatMessageBean
            if (itemBean.isEditMode) {
                val sel = itemBean.isSel
                itemBean.isSel = !sel
                adapter.notifyItemChanged(position)
            }
        } else if (view.id == R.id.layout_header) {
            //点击头像
            onChatItemListener.onItemHeaderClick(data)
        }
    }


    private fun showChatHeaderPopup(view: View, data: ChatMessageBean) {
        var isShowViewDelGroup = false
        var isShowViewMute = false
        ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)
            ?: return context.getString(R.string.cichengyuanbuzaiqunzhong).toast()
        //被长按头像成员在群的角色
        var memberRole = ChatDao.getGroupDb().getGroupRoleInfoById(data.groupId, data.from)
        //本人(操作人在群的角色)
        var role = ChatDao.getGroupDb().getGroupRoleInfoById(data.groupId)
        //Owner 群主，admin 管理员，Normal 普通群成员
        when (role.lowercase()) {
            "Owner".lowercase() -> {//操作人 是群主
                isShowViewDelGroup = true
                isShowViewMute = true
            }
            "Admin".lowercase() -> {//操作人 是管理员
                when (memberRole.lowercase()) {
                    "memberRole".lowercase() -> {//被点击头像的 是群主
                        isShowViewDelGroup = false
                        isShowViewMute = false
                    }
                    "Admin".lowercase() -> {//被点击头像的 是管理员
                        isShowViewDelGroup = false
                        isShowViewMute = false
                    }
                    "Normal".lowercase() -> {//被点击头像的 是普通成员
                        isShowViewDelGroup = true
                        isShowViewMute = true
                    }
                }
            }
            "Normal".lowercase() -> {//操作人 是普通成员
                isShowViewDelGroup = false
                isShowViewMute = false
            }
        }
        var chpw = ChatHeaderPopupWindow(context, onItemClickListener = {
            onChatItemListener.onItemHeaderLongClick(it, data)
        })
        chpw.setShowDelGroup(isShowViewDelGroup)
        chpw.setShowMute(isShowViewMute)
        chpw.setStateMute(
            ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)?.allowSpeak == "N"
        )
        chpw.showPopup(view, SizeUtils.dp2px(-22f), SizeUtils.dp2px(-24f), Gravity.RIGHT)
    }
}