package com.ym.chat.bean

import java.io.Serializable

/**
 * 群发消息实体类
 */
data class SendGroupMsgBean(
    val current: Int = 0,
    val pages: Int = 0,
    val records: MutableList<SendRecordBean>? = null,
    val searchCount: Boolean = true,
    val size: Int = 0,
    val total: Int = 0

) : Serializable

data class SendRecordBean(
    val content: String? = "",
    val createTimeBegin: String? = "",
    val createTimeEnd: String? = "",
    val id: String = "",
    val memberId: String? = "",
    val receiverId: String? = "",
    val receiverName: String? = "",
    val sendTime: String? = "",
    val updateTimeBegin: String? = "",
    val updateTimeEnd: String? = "",
    val msgType: String? = ""
) : Serializable