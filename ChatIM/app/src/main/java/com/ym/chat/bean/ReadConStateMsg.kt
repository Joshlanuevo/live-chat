package com.ym.chat.bean

/**
 * 消息已读回执
 */
data class ReadConStateMsg(
    val cmd: Int,
    val from: String,
    val to: String,
    val groupId: String,
    val chatType: String,
    /**
     * 标识类型
     * unread:  标记未读
     * read: 标记已读
     */
    val signType: String,
)