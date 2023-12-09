package com.ym.chat.item

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.ext.xmlToColor
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ConversationBean
import com.ym.chat.databinding.ItemHomeMsgBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.PopUtils
import com.ym.chat.utils.TimeUtils
import com.ym.chat.utils.Utils
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener
import razerdp.basepopup.BasePopupWindow
import razerdp.basepopup.QuickPopupBuilder
import razerdp.basepopup.QuickPopupConfig
import razerdp.widget.QuickPopup
import java.util.*

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/20 15:10
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 首页消息item
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class HomeMsgItem(
    private val onItemClickListener: ((bean: ConversationBean) -> Unit)? = null,
    private val onClickDeleteConver: ((bean: ConversationBean, position: Int) -> Unit)? = null,
    private val onClickOnTop: ((bean: ConversationBean, position: Int) -> Unit)? = null,
    private val onClickDelMsg: ((bean: ConversationBean, position: Int) -> Unit)? = null,
    private val onClickNotNotify: ((bean: ConversationBean, position: Int) -> Unit)? = null,
    private val onClickNotRead: ((bean: ConversationBean, position: Int) -> Unit)? = null,
) : QuickViewBindingItemBinder<ConversationBean, ItemHomeMsgBinding>() {

    init {
        addChildClickViewIds(R.id.chatItem)
        addChildLongClickViewIds(R.id.chatItem)
    }

    var isConverList = false

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemHomeMsgBinding {
        return ItemHomeMsgBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(holder: BinderVBHolder<ItemHomeMsgBinding>, data: ConversationBean) {

        if (data.lastTime > 0) {
            holder.viewBinding.tvTime.text = TimeUtils.formatTime(data.lastTime)
        } else {
            holder.viewBinding.tvTime.text = ""
        }
        //消息未读数量(系统通知特殊处理)
        if (isConverList) {
            if (data.msgCount > 0 && data.type != 2) {
                holder.viewBinding.tvMsgCount.visible()
                holder.viewBinding.tvMsgCount.text = if (data.msgCount > 99) {
                    "99+"
                } else {
                    "${data.msgCount}"
                }
            } else {
                holder.viewBinding.ivRead.gone()
                holder.viewBinding.tvMsgCount.gone()
                if (!data.isRead) {
                    holder.viewBinding.tvMsgCount.text = ""
                    holder.viewBinding.tvMsgCount.visible()
                }
            }
        }

        //显示已读未读图标
//        if (data.type != 2) {
//            holder.viewBinding.ivRead.visible()
//            if (data.msgCount > 0) {
//                holder.viewBinding.ivRead.setImageResource(R.drawable.ic_c_unread)
//            } else {
//                holder.viewBinding.ivRead.setImageResource(R.drawable.ic_c_read)
//            }
//        } else {
//            holder.viewBinding.ivRead.gone()
//        }

        //防止复用
        holder.viewBinding.ivSilence.gone()
        holder.viewBinding.tvDraft.gone()
        holder.viewBinding.ivEdit.gone()
        holder.viewBinding.ivGroupIcon.gone()
        holder.viewBinding.swipeMenu.isLeftSwipe = true
        holder.viewBinding.tvMsgPre.text = ""
        holder.viewBinding.tvNickName.text = ""
//        Glide.with(holder.viewBinding.root).load(R.drawable.msg_collect)
//            .into(holder.viewBinding.layoutHeader.ivHeader)
//        holder.viewBinding.layoutHeader.ivHeader.load(R.drawable.)
        holder.viewBinding.layoutHeader.ivHeaderMark.gone()
        holder.viewBinding.ivSystemNotify.gone()
//        holder.viewBinding.ivRead.gone()
        if (data.isLongDown) {
            holder.viewBinding.ivGroupIcon.setImageResource(R.drawable.ic_group_icon_white)
            holder.viewBinding.tvNickName.setTextColor(context.getColor(R.color.white))
            holder.viewBinding.tvMsgPre.setTextColor(context.getColor(R.color.white))
            holder.viewBinding.tvTime.setTextColor(context.getColor(R.color.white))
            holder.viewBinding.chatItem.setBackgroundColor(R.color.color_main_pre.xmlToColor())
            if (data.isTop) {
                holder.viewBinding.viewTop.visible()
                holder.viewBinding.ivTop.visible()
            } else {
                holder.viewBinding.viewTop.gone()
                holder.viewBinding.ivTop.gone()
            }
        } else {
            holder.viewBinding.ivGroupIcon.setImageResource(R.drawable.ic_group_icon_black)
            holder.viewBinding.tvNickName.setTextColor(context.getColor(R.color.color_333333))
            holder.viewBinding.tvMsgPre.setTextColor(context.getColor(R.color.color_AAAAAA))
            holder.viewBinding.tvTime.setTextColor(context.getColor(R.color.color_AAAAAA))
            if (isConverList) {
                if (data.isTop) {
                    holder.viewBinding.chatItem.setBackgroundColor(R.color.activity_bg_77.xmlToColor())
                    holder.viewBinding.viewTop.visible()
                    holder.viewBinding.ivTop.visible()
                } else {
                    holder.viewBinding.chatItem.setBackgroundColor(R.color.white.xmlToColor())
                    holder.viewBinding.viewTop.gone()
                    holder.viewBinding.ivTop.gone()
                }
            }
        }

        when (data.type) {
            2 -> {
                holder.viewBinding.ivSilence.gone()
                holder.viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_red_10dp)
                //系统会话
                holder.viewBinding.tvFrom.gone()
                if (data.sysType == 1) {
                    //我的收藏
                    holder.viewBinding.tvNickName.text = context.getString(R.string.wodeshoucang)
//                    holder.viewBinding.tvMsgPre.text = data.lastMsg
                    showLastMsg(holder.viewBinding.tvMsgPre, data)
                    holder.viewBinding.layoutHeader.ivHeader.load(R.drawable.msg_collect)
//                    holder.viewBinding.tvDelChat.gone()
                    showCollectLastMsg(holder.viewBinding.tvMsgPre, data)
                } else if (data.sysType == 2) {
                    //系统通知
                    holder.viewBinding.tvNickName.text = context.getString(R.string.xitongtongzhi)
                    holder.viewBinding.tvMsgPre.text = data.lastMsg
                    holder.viewBinding.layoutHeader.ivHeader.load(R.drawable.ic_notify)
                    holder.viewBinding.tvMsgPre.text = data.lastMsg

                    //未读消息
                    val count = ChatDao.getNotifyDb().getUnReadMsgCount()
                    if (count > 0) {
                        holder.viewBinding.tvMsgCount.visible()
                        holder.viewBinding.tvMsgCount.text = if (count > 99) {
                            "99+"
                        } else {
                            "$count"
                        }
                    } else {
                        holder.viewBinding.tvMsgCount.gone()
                    }
                }
            }
            0 -> {
                //单聊
                if (!TextUtils.isEmpty(data.draftContent)) {
                    holder.viewBinding.tvDraft.visible()
                    holder.viewBinding.ivEdit.visible()
                    holder.viewBinding.tvMsgPre.text = data.draftContent
                } else {
                    holder.viewBinding.tvDraft.gone()
                    showLastMsg(holder.viewBinding.tvMsgPre, data)
                }
                holder.viewBinding.tvFrom.gone()
                if (isConverList) {
                    if (data.isMute) {
                        //开启了免打扰
                        holder.viewBinding.ivSilence.visible()
                        holder.viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_gray_10dp)
                    } else {
                        //关闭了免打扰
                        holder.viewBinding.ivSilence.gone()
                        holder.viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_red_10dp)
                    }
                }
                val friendInfo = ChatDao.getFriendDb().getFriendById(data.chatId)
                if (friendInfo != null) {
                    holder.viewBinding.tvNickName.text = friendInfo.nickname
                    if (friendInfo.memberLevelCode == "System") {
                        holder.viewBinding.ivSystemNotify.visible()
                    }
                    holder.viewBinding.layoutHeader.ivHeader.loadImg(friendInfo)
                    Utils.showDaShenImageView(
                        holder.viewBinding.layoutHeader.ivHeaderMark,
                        friendInfo.displayHead == "Y",
                        friendInfo.levelHeadUrl
                    )
                } else {
                    holder.viewBinding.ivSilence.gone()
                    holder.viewBinding.tvNickName.text = ""
                    holder.viewBinding.layoutHeader.ivHeader.load(R.drawable.ic_mine_header)
                }
            }
            1 -> {
                holder.viewBinding.ivGroupIcon.visible()

                //群聊
                val groupInfo = ChatDao.getGroupDb().getGroupInfoById(data.chatId)
                if (groupInfo != null) {
                    holder.viewBinding.tvNickName.text = groupInfo.name
                    holder.viewBinding.layoutHeader.ivHeader.loadImg(
                        groupInfo.headUrl, groupInfo.name, R.drawable.ic_mine_header_group, true
                    )
                } else {
                    holder.viewBinding.tvNickName.text = ""
                    holder.viewBinding.layoutHeader.ivHeader.load(R.drawable.ic_mine_header_group)

//                    //重新获取一遍群组数据
//                    ChatDao.syncFriendAndGroupToLocal(false, true)
                }

                if (isConverList) {
                    if (data.isMute) {
                        //开启了免打扰
                        holder.viewBinding.ivSilence.visible()
                        holder.viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_gray_10dp)
                    } else {
                        //开启了通知提示
                        holder.viewBinding.ivSilence.gone()
                        holder.viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_red_10dp)
                    }
                }

                if (!TextUtils.isEmpty(data.draftContent)) {
                    holder.viewBinding.tvDraft.visible()
                    holder.viewBinding.ivEdit.visible()
                    holder.viewBinding.tvFrom.gone()
                    holder.viewBinding.tvMsgPre.text = data.draftContent
                } else {
                    if (!TextUtils.isEmpty(data.fromId)) {
                        try {
                            val member =
                                ChatDao.getGroupDb().getMemberInGroup(data.fromId, data.chatId)
//                            "------------from=${data.fromId}----groupId=${data.chatId}---${member?.nickname}".logE()
                            if (member != null) {
                                holder.viewBinding.tvFrom.visible()
                                holder.viewBinding.tvFrom.text = "${member.nickname}:"
                            } else {
//                            //获取该群的成员数据
//                            ChatDao.getGroupMemberList(data.chatId)
                                holder.viewBinding.tvFrom.text = ""
                            }
                        } catch (e: Exception) {
                        }
                    } else {
                        holder.viewBinding.tvFrom.gone()
                        holder.viewBinding.tvFrom.text = ""
                    }
                    showLastMsg(holder.viewBinding.tvMsgPre, data)
                }

            }
            else -> {
                holder.viewBinding.ivSilence.gone()
                holder.viewBinding.tvFrom.gone()
                holder.viewBinding.tvMsgPre.text = ""
                holder.viewBinding.tvNickName.text = context.getString(R.string.weizhi)
                holder.viewBinding.layoutHeader.ivHeader.load(R.drawable.ic_mine_header)
            }
        }
    }

    /**
     * 显示最后一条消息预览
     */
    private fun showLastMsg(textView: TextView, conver: ConversationBean) {
        when (conver.lastMsgType) {
            MsgType.MESSAGETYPE_TEXT -> {
                textView.text = conver.lastMsg
            }
            MsgType.MESSAGETYPE_AT -> {
                textView.text = AtUserHelper.parseAtUserLinkJx(conver.lastMsg,
                    ContextCompat.getColor(context, R.color.color_at), object :
                        AtUserLinkOnClickListener {
                        override fun ulrLinkClick(str: String?) {
                        }

                        override fun atUserClick(str: String?) {
                        }

                        override fun phoneClick(str: String?) {
                        }
                    })
            }
            MsgType.MESSAGETYPE_VIDEO -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.m_shipin)
            }
            MsgType.MESSAGETYPE_VOICE -> {
                textView.text =ActivityUtils.getTopActivity().getString(R.string.m_yuyin)
            }
            MsgType.MESSAGETYPE_PICTURE -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.m_tupian)
            }
            MsgType.MESSAGETYPE_FILE -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.m_wenjian)
            }
            MsgType.MESSAGETYPE_CONTACT -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.m_mingpian)
            }
            else -> {
                textView.text = conver.lastMsg
            }
        }
    }

    /**
     * 显示最后一条收藏消息预览
     */
    private fun showCollectLastMsg(textView: TextView, conver: ConversationBean) {
        when (conver.lastMsgType.uppercase()) {
            MsgType.MESSAGETYPE_TEXT.uppercase(), MsgType.MESSAGETYPE_AT -> {
                textView.text = conver.lastMsg
            }
            MsgType.MESSAGETYPE_VIDEO.uppercase() -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.shoucangleyiduanshipin)
            }
            MsgType.MESSAGETYPE_VOICE.uppercase() -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.shoucangleyiduanyuyin)
            }
            MsgType.MESSAGETYPE_PICTURE.uppercase() -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.shoucangleyiduantupian)
            }
            MsgType.MESSAGETYPE_FILE.uppercase() -> {
                textView.text = ActivityUtils.getTopActivity().getString(R.string.shoucangleyiduantupian)
            }
        }
    }

    override fun onChildClick(
        holder: BinderVBHolder<ItemHomeMsgBinding>,
        view: View,
        data: ConversationBean,
        position: Int
    ) {
        super.onChildClick(holder, view, data, position)
        if (view.id == R.id.chatItem) {
            //item点击事件
            when (data.type) {
                0 -> {
                    //单聊
                    val friendInfo = ChatDao.getFriendDb().getFriendById(data.chatId)
                    if (friendInfo != null) {
                        data.name = friendInfo.nickname
                        data.img = friendInfo.headUrl
                    }
                }
                1 -> {
                    //群聊
                    val groupInfo = ChatDao.getGroupDb().getGroupInfoById(data.chatId)
                    if (groupInfo != null) {
                        data.name = groupInfo.name
                        data.img = groupInfo.headUrl
                    }
                }
            }
            onItemClickListener?.invoke(data)
        }
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemHomeMsgBinding>,
        view: View,
        data: ConversationBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.chatItem) {
            if (data.sysType == 0) {

                data.isLongDown = true
                adapter.data[position] = data
                adapter.notifyItemChanged(position)

                val type = PopUtils.calculatePopWindowPos(view)
                val gravityType = when (type[0]) {
                    1 -> Gravity.BOTTOM
                    2 -> Gravity.TOP
                    else -> Gravity.TOP
                }
                showPopupWindow(context, view, data, position, gravityType)
            }
        }
        return super.onChildLongClick(holder, view, data, position)
    }

    /**
     * 显示更多菜单
     */
    private var mPopUpWindow: QuickPopup? = null
    fun showPopupWindow(
        context: Context,
        view: View,
        data: ConversationBean,
        position: Int,
        gravityType: Int = 0
    ) {
        mPopUpWindow = QuickPopupBuilder.with(context)
            .contentView(R.layout.popup_chat_msg)
            .config(
                QuickPopupConfig()
                    .offsetY(
                        if (gravityType == Gravity.BOTTOM) SizeUtils.dp2px(-20.0f) else SizeUtils.dp2px(
                            20.0f
                        )
                    )
                    .gravity(gravityType)
                    .backgroundColor(Color.TRANSPARENT)
                    .withClick(R.id.llZd) {
                        //置顶
                        onClickOnTop?.invoke(data, position)
                        mPopUpWindow?.dismiss()
                    }.withClick(R.id.llDel) {
                        //删除
                        onClickDeleteConver?.invoke(data, position)
                        mPopUpWindow?.dismiss()
                    }.withClick(R.id.llDelMsg) {
                        //清除消息
                        onClickDelMsg?.invoke(data, position)
                        mPopUpWindow?.dismiss()
                    }.withClick(R.id.llNotNotify) {
                        //静音
                        onClickNotNotify?.invoke(data, position)
                        mPopUpWindow?.dismiss()
                    }.withClick(R.id.llNotRead) {
                        //标记未读
                        onClickNotRead?.invoke(data, position)
                        mPopUpWindow?.dismiss()
                    }).build()
        mPopUpWindow?.showPopupWindow(view)
        mPopUpWindow?.onDismissListener = object :
            BasePopupWindow.OnDismissListener() {
            override fun onDismiss() {
                data.isLongDown = false
                adapter.data[position] = data
                adapter.notifyItemChanged(position)
            }
        }
        val linMsgPopup = mPopUpWindow?.findViewById<LinearLayout>(R.id.linMsgPopup)
        val tvZd = mPopUpWindow?.findViewById<TextView>(R.id.tvZd)
        val ivZd = mPopUpWindow?.findViewById<ImageView>(R.id.ivZd)
        val tvNotNotify = mPopUpWindow?.findViewById<TextView>(R.id.tvNotNotify)
        val ivJy = mPopUpWindow?.findViewById<ImageView>(R.id.ivJy)
        val vNotRead = mPopUpWindow?.findViewById<View>(R.id.vNotRead)
        val llNotRead = mPopUpWindow?.findViewById<LinearLayout>(R.id.llNotRead)
        val tvNotRead = mPopUpWindow?.findViewById<TextView>(R.id.tvNotRead)
        val ivReadTg = mPopUpWindow?.findViewById<ImageView>(R.id.ivReadTg)
        val llDelMsg = mPopUpWindow?.findViewById<LinearLayout>(R.id.llDelMsg)
        val vDelMsg = mPopUpWindow?.findViewById<View>(R.id.vDelMsg)
        linMsgPopup?.click {
            mPopUpWindow?.dismiss()
        }
        if (data.isTop) {
            tvZd?.text = ActivityUtils.getTopActivity().getString(R.string.quxiaozhiding)
            ivZd?.setImageResource(R.drawable.ic_msg_zd_1)
        }
        var isMessageNotice = data.isMute
        if (isMessageNotice) {
            tvNotNotify?.text = ActivityUtils.getTopActivity().getString(R.string.quxiaojinyin)
            ivJy?.setImageResource(R.drawable.ic_msg_jy_1)
        }
//        vNotRead?.visible()
//        llNotRead?.visible()
//        if (data.msgCount > 0) {
//            tvNotRead?.text = "标记已读"
//        } else {
//            if (data.isRead) {
//                tvNotRead?.text = "标记未读"
//                ivReadTg?.setImageResource(R.drawable.ic_msg_yd)
//            } else
//                tvNotRead?.text = "标记已读"
//        }

        llDelMsg?.visible()
        vDelMsg?.visible()

//        if (data.type == 0) {
//            //私聊消息
//            if (MMKVUtils.isAdmin()) {
//                llDelMsg?.visible()
//                vDelMsg?.visible()
//            }
//        } else {
//            //群聊消息
//            if (ChatDao.getGroupDb().getGroupRoleInfoById(data.chatId)
//                    ?.lowercase() != "Normal".lowercase()
//            ) {
//                llDelMsg?.visible()
//                vDelMsg?.visible()
//            }
//        }
    }
}