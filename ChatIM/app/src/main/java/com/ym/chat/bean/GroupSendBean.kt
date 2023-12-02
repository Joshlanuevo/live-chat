package com.ym.chat.bean

import java.io.Serializable

/**
 * 群发消息
 */
data class GroupSendBean(
    var content: String = "",
    val id: String = "",
    val memberId: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val sendTime: String = ""
) : Serializable
