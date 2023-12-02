package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.bean.AccountBean
import com.ym.chat.bean.RecordDateBean
import com.ym.chat.bean.SendGroupMsgBean
import com.ym.chat.bean.SendRecordBean
import com.ym.chat.databinding.ItemSendMsgGroupBinding
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.TimeUtils

/**
 *群发消息
 * 列表 item
 */
class SendGroupMsgItem(val onItemClickListener: ((sendMsgBean: SendRecordBean) -> Unit)? = null) :
    QuickViewBindingItemBinder<SendRecordBean, ItemSendMsgGroupBinding>() {

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemSendMsgGroupBinding.inflate(layoutInflater, parent, false)

    override fun convert(
        holder: BinderVBHolder<ItemSendMsgGroupBinding>,
        sendMsgBean: SendRecordBean
    ) {
        holder.viewBinding.tvSendNmb.text = "${sendMsgBean.receiverId?.let { getSendNmb(it) }}${context.getString(
            R.string.个接收方)}："
        holder.viewBinding.tvSendTime.text = sendMsgBean.sendTime?.let {
            TimeUtils.getStringDateMd(
                it
            )
        }
        holder.viewBinding.tvSendNames.text = sendMsgBean.receiverName?.replace(";", "、")
        holder.viewBinding.tvSendContent.text =
            when (sendMsgBean.msgType) {
                MsgType.MESSAGETYPE_TEXT -> sendMsgBean.content
                MsgType.MESSAGETYPE_PICTURE -> context.getString(R.string.发送了一张图片)
                MsgType.MESSAGETYPE_VIDEO -> context.getString(R.string.发送了一个视频)
                MsgType.MESSAGETYPE_VOICE ->context.getString(R.string.发送了一段语音)
                else -> ""
            }
        holder.viewBinding.tvBtnSendMsg.click {
            onItemClickListener?.invoke(sendMsgBean)
        }
    }

    /**
     * 根据字符串获取 发送的人数
     */
    private fun getSendNmb(receiverId: String): Int {
        var size = 0
        if (receiverId.isNotBlank()) {
            size = if (receiverId.contains(";")) {
                var receiverIds = receiverId.split(";")
                receiverIds.size
            } else {
                1
            }
        }
        return size
    }

}