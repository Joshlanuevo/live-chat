package com.ym.chat.item

import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
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
import com.ym.chat.bean.ImageBean
import com.ym.chat.databinding.ItemImgChatRightBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.PictureActivity
import com.ym.chat.utils.*
import okhttp3.internal.filterList

/**
 * 图片item-右边
 */
class ChatImageRight(
    private val onChatItemListener: OnChatItemListener,
) : QuickVBItemBinderPro<ChatMessageBean, ItemImgChatRightBinding>() {

    init {
        addChildClickViewIds(R.id.tvContentRight)
        addChildLongClickViewIds(R.id.tvContentRight)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemImgChatRightBinding {
        return ItemImgChatRightBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(holder: BinderVBHolder<ItemImgChatRightBinding>, data: ChatMessageBean) {

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
                val imageMsg = GsonUtils.fromJson(data.content, ImageBean::class.java)
                val imageSize: IntArray = WeChatImageUtils.getImageSizeByOrgSizeToWeChat(
                    imageMsg.width,
                    imageMsg.height,
                    context
                )
                val width =
                    imageSize[0]//if (imageSize[0] in 1..400) imageSize[0] else SizeUtils.dp2px(200f)
                val height =
                    imageSize[1]//if (imageSize[1] in 1..400) imageSize[1] else SizeUtils.dp2px(200f)
                holder.viewBinding.tvContentRight.layoutParams.width = width
                holder.viewBinding.tvContentRight.layoutParams.height = height
                holder.viewBinding.consReply.layoutParams.width = width

                var imageUrl = imageMsg.url
                if (!data.isUpload && !TextUtils.isEmpty(data.localPath)) {
                    imageUrl = data.localPath
                }
                //有本地图片地址
//                    holder.viewBinding.tvContentRight.load(BitmapFactory.decodeFile(data.localPath)) {
//                        size(width, height)
//                    }
                if (imageUrl.lowercase().contains(".gif".lowercase())) {
                    Glide.with(context).asGif().load(imageUrl)
                        .listener(object : RequestListener<GifDrawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<GifDrawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: GifDrawable?,
                                model: Any?,
                                target: Target<GifDrawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }
                        })
                        .placeholder(R.drawable.image_chat_placeholder)
                        .error(R.drawable.ic_load_fail)
                        .into(holder.viewBinding.tvContentRight)
                } else {
                    Glide.with(context).load(imageMsg.url).error(R.drawable.ic_load_fail)
                        .placeholder(R.drawable.image_chat_placeholder)
                        .into(holder.viewBinding.tvContentRight)
                }

            } catch (e: Exception) {
                holder.viewBinding.consReply.layoutParams.width = 200
                holder.viewBinding.tvContentRight.layoutParams.width = 200
                holder.viewBinding.tvContentRight.layoutParams.height = 200
                holder.viewBinding.tvContentRight.load(R.drawable.image_chat_placeholder)
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
                holder.viewBinding.llContentRight.setBackgroundResource(R.drawable.shape_solid_green_8_8)
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
        holder: BinderVBHolder<ItemImgChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        try {
            val imageMsg = GsonUtils.fromJson(data.content, ImageBean::class.java)
            var msgType = 2
            var id = ""
            if (data.chatType == "Friend") {
                msgType = 0
                id = data.to
            } else if (data.chatType == "Group") {
                msgType = 1
                id = data.groupId
            }
            PictureActivity.start(context, imageMsg.url, msgType, id)
        } catch (e: Exception) {

        }

    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemImgChatRightBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.tvContentLeft || view.id == R.id.tvContentRight) {
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
