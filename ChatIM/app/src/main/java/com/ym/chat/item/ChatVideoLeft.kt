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
import com.ym.base.ext.toast
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.VideoMsgBean
import com.ym.chat.databinding.ItemChatVideoLeftBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadHeader
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.popup.ChatHeaderPopupWindow
import com.ym.chat.ui.ChatActivity
import com.ym.chat.utils.*
import com.ym.chat.utils.StringExt.showInGroupName
import okhttp3.internal.filterList
import org.json.JSONObject

/**
 * @Description
 * @Author：CASE
 * @Date：2021-08-04
 * @Time：16:33
 */
class ChatVideoLeft(
    private val onChatItemListener: OnChatItemListener,
    private val onPlayClickListener: ((bean: ChatMessageBean) -> Unit)? = null,
) : QuickVBItemBinderPro<ChatMessageBean, ItemChatVideoLeftBinding>() {

    init {
        addChildLongClickViewIds(R.id.flContent)
        addChildClickViewIds(R.id.flContent, R.id.clVideo, R.id.layout_header)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemChatVideoLeftBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemChatVideoLeftBinding>, data: ChatMessageBean) {
        holder.viewBinding.let { vb ->

            if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
                //需要显示对方昵称和头像
                showHeader(holder, data)
            } else {
                //不需要现实对方昵称和头像
                vb.tvFromUserName.gone()
                vb.layoutHeader.ivHeader.gone()
                holder.viewBinding.llLeft.setBackgroundResource(R.drawable.shape_solid_white_8_8)
            }
            holder.viewBinding.tvTimeLeft.text = getTimeStr(data.createTime)
//            vb.ivCoverLeft.load(data.videoInfo?.coverUrl)

            val videoInfo = if (data.operationType == "Forward") {
                val c = JSONObject(data.content).optString("content")
//                val original = JSONObject(data.content).optString("original")
//                holder.viewBinding.tvFromUserName.visible()
//                holder.viewBinding.tvFromUserName.text = "消息转发来自：${original}"
                GsonUtils.fromJson(c, VideoMsgBean::class.java)
            } else {
                holder.viewBinding.tvFromUserName.gone()
                try {
                    val jsonObject = JSONObject(data.content)
                    val coverUrl = jsonObject.optString("coverUrl")
                    val width = jsonObject.optInt("width")
                    val height = jsonObject.optInt("height")
                    val url = jsonObject.optString("url")
                    VideoMsgBean(width,height).apply {
                        this.url = url
                        this.coverUrl = coverUrl
                    }
//                    GsonUtils.fromJson(data.content, VideoMsgBean::class.java)
                }catch (e:Exception){
                    null
                }
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
                    vb.ivCoverLeft.layoutParams.width = width
                    vb.ivCoverLeft.layoutParams.height = height
                    vb.consReply.layoutParams.width = width

                    vb.ivCoverLeft.load(this.coverUrl) {
                        placeholder(R.drawable.image_chat_placeholder)
                        error(R.drawable.image_chat_placeholder)

                    }
                } catch (e: Exception) {
                    vb.ivCoverLeft.layoutParams.width = 200
                    vb.ivCoverLeft.layoutParams.height = 200
                    vb.consReply.layoutParams.width = 200
                    vb.ivCoverLeft.load(R.drawable.image_chat_placeholder)
                }
            }

            //消息已读回执
            onChatItemListener.readCallBack(data)

            vb.ivSelect.setImageResource(if (data.isSel) R.drawable.ic_group_chat_member_select else R.drawable.ic_check_normal)

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
        }
    }
    /**
     * 显示头像
     */
    private fun showHeader(
        holder: BinderVBHolder<ItemChatVideoLeftBinding>,
        data: ChatMessageBean
    ) {
        holder.viewBinding.tvFromUserName.visible()
        holder.viewBinding.layoutHeader.ivHeader.visible()
        holder.viewBinding.layoutHeader.root.click {
            //点击头像
            onChatItemListener.onItemHeaderClick(data)
        }
        try {
            var member = ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)
            if (member != null) {
                var name = if (!TextUtils.isEmpty(member.nickRemark)){member.nickRemark}else{member.name}
                holder.viewBinding.tvFromUserName.text =
                    member.nickname.showInGroupName(member.role)
                holder.viewBinding.layoutHeader.ivHeader.apply {
                    setRoundRadius(72F)
                    setChatId(data.from)
                    setChatName(name)
                }.showUrl(member.headUrl)
//                Utils.showDaShenImageView(holder.viewBinding.layoutHeader.ivHeaderMark, member)
            } else {
                holder.viewBinding.tvFromUserName.text = data.fromName
                holder.viewBinding.layoutHeader.ivHeader.apply {
                    setRoundRadius(72F)
                    setChatId(data.from)
                    setChatName(data.fromName)
                }.showUrl(data.fromHead)
            }
        } catch (e: Exception) {
            holder.viewBinding.tvFromUserName.text = data.fromName
            holder.viewBinding.tvFromUserName.text = data.fromName
            holder.viewBinding.layoutHeader.ivHeader.apply {
                setRoundRadius(72F)
                setChatId(data.from)
                setChatName(data.fromName)
            }.showUrl(data.fromHead)
            "---------获取成员数据异常-${e.message.toString()}".logE()
        }
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemChatVideoLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.flContent) {
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
            //长按头像
            showChatHeaderPopup(view, data)
//            onChatItemListener.onItemHeaderLongClick(data)
        }
        return true
    }

    override fun onChildClick(
        holder: BinderVBHolder<ItemChatVideoLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        when (view.id) {
            R.id.flContent -> {
                if (data.isEditMode) onChatItemListener.onItemClick(data, position)
                else onPlayClickListener?.invoke(data)
            }
            R.id.clVideo -> {
                onChatItemListener.onItemClick(data, position)
            }
            R.id.layout_header -> {
                //点击头像
                onChatItemListener.onItemHeaderClick(data)
            }
        }
    }


    private fun showChatHeaderPopup(view: View, data: ChatMessageBean) {
        var isShowViewDelGroup = false
        var isShowViewMute = false
        ChatActivity.groupMemberList.firstOrNull { it.id==data.from }
//        ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)
//            ?: return "此成员已不在群中".toast()
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