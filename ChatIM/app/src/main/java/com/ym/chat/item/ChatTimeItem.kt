package com.ym.chat.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.databinding.ItemChatTimeBinding
import com.ym.chat.utils.DateFormatter
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Description
 * @Author：
 * @Date：时间线显示
 * @Time：16:33
 */
class ChatTimeItem : QuickVBItemBinderPro<ChatMessageBean, ItemChatTimeBinding>() {

    @SuppressLint("SimpleDateFormat")
    private val currentYear = SimpleDateFormat("MM月dd日")

    @SuppressLint("SimpleDateFormat")
    private val other = SimpleDateFormat("yyyy年MM月dd日")

    @SuppressLint("SimpleDateFormat")
    private val mmhh = SimpleDateFormat("HH:mm")

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemChatTimeBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemChatTimeBinding>, data: ChatMessageBean) {
        holder.viewBinding.tvNoticeContent.text = ""
        when (data.msgType) {
            MsgType.MESSAGETYPE_TIME -> {
                //显示时间
                holder.viewBinding.tvNoticeContent.text = TimeUtils.formatTime(data.createTime)
            }
            MsgType.MESSAGETYPE_NOTICE -> {
                //通知类型
                holder.viewBinding.tvNoticeContent.text =
                    data.content + "  ${mmhh.format(data.createTime)}"
            }
        }
    }

//    /**
//     * 格式化时间
//     */
//    private fun formatTime(createTime: Long): String {
//        return when {
//            DateFormatter.isToday(Date(createTime)) -> {
//                val calendar = Calendar.getInstance()
//                calendar.time = Date(createTime)
//                val am_pm = calendar.get(Calendar.AM_PM)
//                if (am_pm == Calendar.AM) {
//                    "上午 ${mmhh.format(createTime)}"
//                } else {
//                    "下午 ${mmhh.format(createTime)}"
//                }
//            }
//            DateFormatter.isYesterday(Date(createTime)) -> {
//                "昨天 ${mmhh.format(createTime)}"
//            }
//            DateFormatter.isCurrentYear(Date(createTime)) -> {
//                //今年
//                currentYear.format(Date(createTime))
//            }
//            else -> {
//                //往年
//                other.format(Date(createTime))
//            }
//        }
//    }
}