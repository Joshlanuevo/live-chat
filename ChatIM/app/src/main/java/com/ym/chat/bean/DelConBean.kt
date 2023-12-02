package com.ym.chat.bean

/**
 * 删除会话 发ws消息
 */
data class DelConBean(
    /**
     * 命令号;
     */
    var cmd: Int = 0,

    /**
     * 会员ID;
     */
    var memberId: String = "",

    /**
     * 好友id
     */
    var friendMemberId: String = "",

    /**
     * 未读消息数量
     */
    var unreadCount: Int = 0,

    /**
     * 群组ID;
     */
    var groupId: String = "",

    /**
     * 类型: Friend("好友聊天会话"), Group("群组聊天会话");
     *
     */
    var type: String = "",

    //Add, Del, AddTop, DelTop
    var operationType: String = "",
)