package com.ym.chat.bean

/**
 * 群聊通知事件
 */
data class GroupActionBean(
    val groupId: String,
    val messageType: String,
    val operatorName: String,
    val operatorId: String,
    val setValue: String,
    val targetName: String,
    val targetId: String,
)