package com.ym.chat.item

import android.graphics.drawable.ClipDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.invisible
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.FileMsgBean
import com.ym.chat.bean.ImageBean
import com.ym.chat.databinding.ItemFileChatLeftBinding
import com.ym.chat.databinding.ItemImgChatLeftBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.popup.ChatHeaderPopupWindow
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.PictureActivity
import com.ym.chat.utils.*
import com.ym.chat.utils.StringExt.showInGroupName
import okhttp3.internal.filterList

/**
 * 文件消息item-左边
 */
class ChatFileLeft(
    private val onChatItemListener: OnChatItemListener,
    private val onClickListener: ((bean: ChatMessageBean, position: Int) -> Unit)? = null,
) : QuickVBItemBinderPro<ChatMessageBean, ItemFileChatLeftBinding>() {

    init {
        addChildClickViewIds(R.id.layoutFile, R.id.layout_header)
        addChildLongClickViewIds(R.id.layoutFile, R.id.layout_header)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemFileChatLeftBinding {
        return ItemFileChatLeftBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(holder: BinderVBHolder<ItemFileChatLeftBinding>, data: ChatMessageBean) {
        try {

            if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
                //需要显示对方昵称和头像
                showHeader(holder, data)
            } else {
                //不需要现实对方昵称和头像
                holder.viewBinding.tvFromUserName.gone()
                holder.viewBinding.layoutHeader.flHeader.gone()
            }
            holder.viewBinding.tvTime.text = getTimeStr(data.createTime)
            val fileMsg = GsonUtils.fromJson(data.content, FileMsgBean::class.java)
            holder.viewBinding.tvName.text = fileMsg.name
            holder.viewBinding.tvSize.text = fileMsg.size

            val (isExit, id) = DownloadUtil.queryExist(context, fileMsg.url)
            if (isExit) {
                (holder.viewBinding.ivIcon2.drawable as ClipDrawable).level = 0
                holder.viewBinding.ivDownload.text = context.getString(R.string.dakai)
            } else {
                (holder.viewBinding.ivIcon2.drawable as ClipDrawable).level =
                    (10000 * (1 - data.downloadProcess)).toInt()
                holder.viewBinding.ivDownload.text = context.getString(R.string.xiazai)
            }

            //消息已读回执
            onChatItemListener.readCallBack(data)

            //显示回复
            if (!TextUtils.isEmpty(data.parentMessageId)) {
                //处理回复消息
                holder.viewBinding.let { view ->
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

    /**
     * 显示头像
     */
    private fun showHeader(holder: BinderVBHolder<ItemFileChatLeftBinding>, data: ChatMessageBean) {
//        holder.viewBinding.tvFromUserName.visible()
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

    override fun onChildClick(
        holder: BinderVBHolder<ItemFileChatLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        if (view.id == R.id.layout_header) {
            //长按头像
            onChatItemListener.onItemHeaderClick(data)
        } else if (view.id == R.id.layoutFile) {
            onClickListener?.invoke(data, position)
        }
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemFileChatLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.layoutFile) {
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
        } else if (view.id == R.id.layout_header) {
            //点击头像
            showChatHeaderPopup(view, data)
//            onChatItemListener.onItemHeaderLongClick(data)
        }
        return true
    }


    private fun showChatHeaderPopup(view: View, data: ChatMessageBean) {
        var isShowViewDelGroup = false
        var isShowViewMute = false
        ChatDao.getGroupDb().getMemberInGroup(data.from,data.groupId)
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
