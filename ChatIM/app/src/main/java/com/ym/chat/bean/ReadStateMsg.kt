package com.ym.chat.bean

/**
 * 消息已读回执
 */
data class ReadStateMsg(
    val cmd: String,
    val from: String,
    val to: String,
    val id: String,
    val chatType: String,
)