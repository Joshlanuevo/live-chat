package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import coil.load
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FileMsgBean
import com.ym.chat.bean.ImageBean
import com.ym.chat.bean.MsgTopBean
import com.ym.chat.databinding.ItemMsgTopBinding
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.TimeUtils
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener

/**
 * 置顶消息item
 */
class MsgTopItem(
    var supportFragmentManager: FragmentManager,
    val onItemClickListener: ((msgTopBean: MsgTopBean) -> Unit)? = null,
    val onDelItemClickListener: ((msgTopBean: MsgTopBean) -> Unit)? = null
) :
    QuickViewBindingItemBinder<MsgTopBean, ItemMsgTopBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemMsgTopBinding.inflate(layoutInflater, parent, false)


    override fun convert(
        holder: BinderVBHolder<ItemMsgTopBinding>,
        data: MsgTopBean
    ) {
        holder.viewBinding.let {
            it.root.click {
                onItemClickListener?.invoke(data)
            }
            it.tvIndex.text = "${context.getString(R.string.zhidingxiaoxi)}#${data.index}"
//            it.tvTime.text = data.createTime
            it.ivDel.click {
                onDelItemClickListener?.invoke(data)
            }
//            if (adapter.data.size == holder.adapterPosition + 1) it.viewLine.gone() else it.viewLine.visible()
            if (data?.contentType != null)
                when (data?.contentType) {
                    MsgType.MESSAGETYPE_TEXT -> {
                        //文本消息
                        it.ivMsg.gone()
                        it.tvContent.text = AtUserHelper.parseAtUserLinkJx(data.content,
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
                    MsgType.MESSAGETYPE_AT -> {
                        //@消息
                        it.ivMsg.gone()
                        it.tvContent.text = AtUserHelper.parseAtUserLinkJx(data.content,
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
                    MsgType.MESSAGETYPE_PICTURE -> {
                        //图片消息
                        it.ivMsg.visible()
                        try {
                            val imageMsg = GsonUtils.fromJson(data.content, ImageBean::class.java)
                            it.ivMsg.load(imageMsg.url) {
                                placeholder(R.drawable.image_chat_placeholder)
                                error(R.drawable.image_chat_placeholder)
                            }
                        } catch (e: Exception) {
                        }
                        it.tvContent.text = context.getString(R.string.tupian)
                    }
                    MsgType.MESSAGETYPE_VIDEO -> {
                        //视频消息
                        it.ivMsg.visible()
                        data.videoInfo?.apply {
                            it.ivMsg.load(this.coverUrl) {
                                placeholder(R.drawable.image_chat_placeholder)
                                error(R.drawable.image_chat_placeholder)
                            }
                        }
                        it.tvContent.text = context.getString(R.string.视频)
                    }
                    MsgType.MESSAGETYPE_VOICE -> {
                        //语音消息
                        it.ivMsg.gone()
                        it.tvContent.text =  context.getString(R.string.语音消息)
                    }
                    MsgType.MESSAGETYPE_FILE -> {
                        //文件消息
                        it.ivMsg.visible()
                        it.ivMsg.load(R.drawable.icon_chat_file)
                        it.tvContent.text = context.getString(R.string.文件)
                    }
                }
        }
    }
}