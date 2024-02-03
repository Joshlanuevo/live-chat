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
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.xmlToColor
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ConversationBean
import com.ym.chat.databinding.ItemHomeMsgBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.PopUtils
import com.ym.chat.utils.TimeUtils
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener
import org.json.JSONArray
import org.json.JSONObject
import razerdp.basepopup.BasePopupWindow
import razerdp.basepopup.QuickPopupBuilder
import razerdp.basepopup.QuickPopupConfig
import razerdp.widget.QuickPopup

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

        //公共部分显示
        showPublic(data, holder.viewBinding)

        when (data.type) {
            2 -> {
                //系统会话
                showSystem(data, holder.viewBinding)
            }
            0 -> {
                //单聊会话
                showFriend(data, holder.viewBinding)
            }
            1 -> {
                //群会话显示
                showGroup(data, holder.viewBinding)
            }
        }
    }

    private fun showPublic(data: ConversationBean, viewBinding: ItemHomeMsgBinding) {

        //显示最后一次消息时间
        if (data.lastTime > 0) {
            viewBinding.tvTime.text = TimeUtils.formatTime(data.lastTime)
        } else {
            viewBinding.tvTime.text = ""
        }
        //消息未读数量(系统通知特殊处理)
        if (isConverList) {
            if (data.msgCount > 0 && data.type != 2) {
                viewBinding.tvMsgCount.visible()
                viewBinding.tvMsgCount.text = if (data.msgCount > 99) {
                    "99+"
                } else {
                    "${data.msgCount}"
                }
            } else {
                viewBinding.ivRead.gone()
                viewBinding.tvMsgCount.gone()
                if (!data.isRead) {
                    viewBinding.tvMsgCount.text = ""
                    viewBinding.tvMsgCount.visible()
                }
            }
        }

        //长按选中显示
        if (data.isLongDown) {
            viewBinding.ivGroupIcon.setImageResource(R.drawable.ic_group_icon_white)
            viewBinding.tvNickName.setTextColor(context.getColor(R.color.white))
            viewBinding.tvMsgPre.setTextColor(context.getColor(R.color.white))
            viewBinding.tvTime.setTextColor(context.getColor(R.color.white))
            viewBinding.chatItem.setBackgroundColor(R.color.color_main_pre.xmlToColor())
            if (data.isTop) {
                viewBinding.viewTop.visible()
                viewBinding.ivTop.visible()
            } else {
                viewBinding.viewTop.gone()
                viewBinding.ivTop.gone()
            }
        } else {
            viewBinding.ivGroupIcon.setImageResource(R.drawable.ic_group_icon_black)
            viewBinding.tvNickName.setTextColor(context.getColor(R.color.color_333333))
            viewBinding.tvMsgPre.setTextColor(context.getColor(R.color.color_AAAAAA))
            viewBinding.tvTime.setTextColor(context.getColor(R.color.color_AAAAAA))
            if (isConverList) {
                if (data.isTop) {
                    viewBinding.chatItem.setBackgroundColor(R.color.activity_bg_77.xmlToColor())
                    viewBinding.viewTop.visible()
                    viewBinding.ivTop.visible()
                } else {
                    viewBinding.chatItem.setBackgroundColor(R.color.white.xmlToColor())
                    viewBinding.viewTop.gone()
                    viewBinding.ivTop.gone()
                }
            }
        }
    }

    // Automatic translate text/message type depends on language mode // vannn //
    private fun showSystem(data: ConversationBean, viewBinding: ItemHomeMsgBinding) {
        viewBinding.ivSilence.gone()
        viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_red_10dp)
        val welcomeString = context.getString(R.string.huanyingshiyong)
        //系统会话
        viewBinding.tvFrom.gone()
        if (data.sysType == 1) {
            //我的收藏
            viewBinding.tvNickName.text = context.getString(R.string.wodeshoucang)
//            showLastMsg(viewBinding.tvMsgPre, data)
//            viewBinding.tvMsgPre.text = welcomeString
            viewBinding.tvHeader.gone()
            viewBinding.redPoint.gone()
            viewBinding.layoutHeader.apply {
                setRoundRadius(100F)
            }.showImageRes(R.drawable.msg_collect)
//            showCollectLastMsg(viewBinding.tvMsgPre, data)
//            viewBinding.tvMsgPre.text = welcomeString
            if (data.lastMsg == "") {
                viewBinding.tvMsgPre.text = welcomeString
            } else {
                showCollectLastMsg(viewBinding.tvMsgPre, data)
            }
        } else if (data.sysType == 2) {
            //系统通知
            viewBinding.tvNickName.text = context.getString(R.string.xitongtongzhi)
            viewBinding.tvMsgPre.text = data.lastMsg
            viewBinding.tvMsgCount.gone()
            viewBinding.tvHeader.gone()
            viewBinding.layoutHeader.apply {
                setRoundRadius(100F)
            }.showImageRes(R.drawable.ic_notify)
            viewBinding.tvMsgPre.text = data.lastMsg

            //未读消息
            val count = ChatDao.getNotifyDb().getUnReadMsgCount()
            if (count > 0) {
                viewBinding.redPoint.visible()
            } else {
                viewBinding.redPoint.gone()
            }
        }
    }

    private fun showFriend(data: ConversationBean, viewBinding: ItemHomeMsgBinding) {
        viewBinding.tvFrom.gone()
        viewBinding.ivGroupIcon.gone()

        //显示草稿
        if (!TextUtils.isEmpty(data.draftContent)) {
            viewBinding.tvDraft.visible()
            viewBinding.ivEdit.visible()
            viewBinding.tvMsgPre.text = data.draftContent
        } else {
            viewBinding.tvDraft.gone()
            showLastMsg(viewBinding.tvMsgPre, data)
        }

        if (data.lastMsg == "") {
            viewBinding.tvMsgPre.text = context.getString(R.string.聊天为空)
        }

        //免打扰设置
        if (isConverList) {
            if (data.isMute) {
                //开启了免打扰
                viewBinding.ivSilence.visible()
                viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_gray_10dp)
            } else {
                //关闭了免打扰
                viewBinding.ivSilence.gone()
                viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_red_10dp)
            }
        }

        //显示头像
        viewBinding.layoutHeader.apply {
            setRoundRadius(100F)
            setChatId(data.chatId)
            setChatName(data.name)
        }.showUrl(data.img)

        if (TextUtils.isEmpty(data.name)) {
            val friendInfo = ChatDao.getFriendDb().getFriendById(data.chatId)
            if (friendInfo != null) {
                viewBinding.tvNickName.text = friendInfo.nickname
                if (friendInfo.memberLevelCode == "System") {
                    viewBinding.ivSystemNotify.visible()
                }
                //显示头像
                viewBinding.layoutHeader.apply {
                    setRoundRadius(100F)
                    setChatId(friendInfo.friendMemberId)
                    setChatName(friendInfo.name)
                }.showUrl(data.img)
            }
        } else {
            viewBinding.tvNickName.text = data.name
            viewBinding.layoutHeader.apply {
                setRoundRadius(100F)
                setChatId(data.chatId)
                setChatName(data.name)
            }.showUrl(data.img)
        }
    }

    private fun showGroup(data: ConversationBean, viewBinding: ItemHomeMsgBinding) {
        viewBinding.ivGroupIcon.visible()

        //免打扰设置
        if (isConverList) {
            if (data.isMute) {
                //开启了免打扰
                viewBinding.ivSilence.visible()
                viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_gray_10dp)
            } else {
                //开启了通知提示
                viewBinding.ivSilence.gone()
                viewBinding.tvMsgCount.setBackgroundResource(R.drawable.bg_red_10dp)
            }
        }

        //显示名称
        if (!TextUtils.isEmpty(data.name)) {
            viewBinding.tvNickName.text = data.name
        } else {
            val groupInfo = ChatDao.getGroupDb().getGroupInfoById(data.chatId)
            if (groupInfo != null) {
                viewBinding.tvNickName.text = groupInfo.name
            } else {
                viewBinding.tvNickName.text = "${context.getString(R.string.群)} ${data.chatId}" // "群"
            }
        }

        //显示头像
        viewBinding.layoutHeader.apply {
            setRoundRadius(100F)
            setChatId(data.chatId)
            setChatName(data.name)
        }.showUrl(data.img)

        if (!TextUtils.isEmpty(data.draftContent)) {
            viewBinding.tvDraft.visible()
            viewBinding.ivEdit.visible()
            viewBinding.tvFrom.gone()
            viewBinding.tvMsgPre.text = data.draftContent
        } else {
            if (!TextUtils.isEmpty(data.fromId)) {
                try {
                    val member =
                        ChatDao.getGroupDb().getMemberInGroup(data.fromId, data.chatId)
//                            "------------from=${data.fromId}----groupId=${data.chatId}---${member?.nickname}".logE()
                    if (member != null) {
                        viewBinding.tvFrom.visible()
                        viewBinding.tvFrom.text = "${member.nickname}:"
                    } else {
//                            //获取该群的成员数据
//                            ChatDao.getGroupMemberList(data.chatId)
                        viewBinding.tvFrom.text = ""
                    }
                } catch (e: Exception) {
                }
            } else {
                viewBinding.tvFrom.gone()
                viewBinding.tvFrom.text = ""
            }
//            showLastMsg(viewBinding.tvMsgPre, data)
            if (TextUtils.isEmpty(data.lastMsg)) {
                viewBinding.tvMsgPre.text = context.getString(R.string.聊天为空)
            } else {
                showLastMsg(viewBinding.tvMsgPre, data)
            }
        }

//        //@消息显示
//        ImCache.atConverMsgList.forEach { atMessageInfoBean ->
//            if (atMessageInfoBean.sessionId == data.chatId) {
//                viewBinding.ivAtTag.visible()
//                return
//            }
//        }

    }

    /**
     * 显示最后一条消息预览
     */
    private fun showLastMsg(textView: TextView, conver: ConversationBean) {
        if (conver.lastMsg.startsWith("[") && conver.lastMsg.endsWith("]")) {
            //转发的消息
            val jsonArray = JSONArray(conver.lastMsg)
            var msgType = ""
            var content = ""
            if (jsonArray.length() > 1) {
                val jsobj = jsonArray.getJSONObject(jsonArray.length() - 1)
                msgType = jsobj.optString("msgType")
                content = jsobj.optString("content")
            } else {
                if (jsonArray.length() > 0) {
                    val jsobj = jsonArray.getJSONObject(0)
                    msgType = jsobj.optString("msgType")
                    content = jsobj.optString("content")
                }
            }
            textView.text = getShowLastMsg(msgType, content)
        } else {
            textView.text = getShowLastMsg(conver.lastMsgType, conver.lastMsg)
        }
    }

    private fun getShowLastMsg(msgType: String, lastMsgContent: String): CharSequence {
        return when (msgType) {
            MsgType.MESSAGETYPE_TEXT -> {
                lastMsgContent
            }
            MsgType.MESSAGETYPE_AT -> {
                AtUserHelper.parseAtUserLinkJx(lastMsgContent,
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
            // Automatic translate text/message type depends on language mode // vannn //
            MsgType.MESSAGETYPE_VIDEO -> {
                context.getString(R.string.m_shipin)
            }
            MsgType.MESSAGETYPE_VOICE -> {
                context.getString(R.string.m_yuyin)
            }
            MsgType.MESSAGETYPE_PICTURE -> {
                context.getString(R.string.m_tupian)
            }
            MsgType.MESSAGETYPE_FILE -> {
                context.getString(R.string.m_wenjian)
            }
//            MsgType.MESSAGETYPE_VIDEO -> {
//                "[视频]"
//            }
//            MsgType.MESSAGETYPE_VOICE -> {
//                "[语音]"
//            }
//            MsgType.MESSAGETYPE_PICTURE -> {
//                "[图片]"
//            }
//            MsgType.MESSAGETYPE_FILE -> {
//                "[文件]"
//            }
            MsgType.MESSAGETYPE_CONTACT -> {
                val jsonObject = JSONObject(lastMsgContent)
                val shareMemberName = jsonObject.optString("shareMemberName")
                "推荐了用户${shareMemberName}"
            }
            else -> {
                lastMsgContent
            }
        }
    }

    /**
     * 显示最后一条收藏消息预览
     */
    // Automatic translate text/message type depends on language mode // vannn //
    private fun showCollectLastMsg(textView: TextView, conver: ConversationBean) {
        when (conver.lastMsgType.uppercase()) {
            MsgType.MESSAGETYPE_TEXT.uppercase(), MsgType.MESSAGETYPE_AT -> {
                textView.text = conver.lastMsg
            }
            MsgType.MESSAGETYPE_VIDEO.uppercase() -> {
                textView.text = context.getString(R.string.shoucangleyiduanshipin)
            }
            MsgType.MESSAGETYPE_VOICE.uppercase() -> {
                textView.text = context.getString(R.string.shoucangleyiduanyuyin)
            }
            MsgType.MESSAGETYPE_PICTURE.uppercase() -> {
                textView.text = context.getString(R.string.shoucangleyiduantupian)
            }
            MsgType.MESSAGETYPE_FILE.uppercase() -> {
                textView.text = context.getString(R.string.shoucangleyiduanwenjian)
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
        // Pin message and automatic translate text depends on language mode // vannn //
        if (data.isTop) {
            tvZd?.text = context.getString(R.string.zhidingxiaoxi)
            ivZd?.setImageResource(R.drawable.ic_msg_zd_1)
        }
        // Check if the message is muted and automatic translate text depends on language mode // vannn //
        var isMessageNotice = data.isMute
        if (isMessageNotice) {
            tvNotNotify?.text = context.getString(R.string.jinyinxiaoxi)
            ivJy?.setImageResource(R.drawable.ic_msg_jy_1)
        }
//        if (data.isTop) {
//            tvZd?.text = "取消置顶"
//            ivZd?.setImageResource(R.drawable.ic_msg_zd_1)
//        }
//        var isMessageNotice = data.isMute
//        if (isMessageNotice) {
//            tvNotNotify?.text = "取消静音"
//            ivJy?.setImageResource(R.drawable.ic_msg_jy_1)
//        }
    }
}