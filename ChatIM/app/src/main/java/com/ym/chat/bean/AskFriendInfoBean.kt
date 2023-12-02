package com.ym.chat.bean

import java.io.Serializable

/**
 *申请 加好友 入群列表
 */
class AskFriendInfoBean(
    val createTime: String,
    val friendMemberId: String,
    val groupId: String,
    val id: String,
    val memberId: String,
    val status: String,
    val type: String,
    val updateTime: String
) : Serializable