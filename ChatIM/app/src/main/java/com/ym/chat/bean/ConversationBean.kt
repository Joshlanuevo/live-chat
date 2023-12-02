package com.ym.chat.bean

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

/**
 * 会话列表Bean
 */
@Entity
data class ConversationBean(
    @Id
    var idInDb: Long = 0,

    //type=0（单聊会话），type=1（群聊会话），type=2（系统默认会话）
    var type: Int = -1,

    //系统固定会话类型，只有系统会话使用，普通会话无需关注该字段，
    // 1:我的收藏 2:系统通知
    var sysType: Int = 0,
    //系统固定会话排序字段
    var sysSort: Int = 0,

    //聊天页面用的ID,如果是群，则为groupId
    @Unique
    var chatId: String = "",

    //会话头像
    var img: String? = "",

    //会话名称
    var name: String = "",

    //最后一条消息内容
    var lastMsg: String = "",

    //最后一条消息时间
    var lastTime: Long = 0,

    //是否置顶
    var isTop: Boolean = false,

    //标记已读/未读
    var isRead: Boolean = true,

    //置顶时间
    var topTime: Long = 0,

    //置顶消息id
    var topId: String? = "",

    //未读消息数量
    var msgCount: Int = 0,

    //最后一条消息类型
    var lastMsgType: String = "",

    //消息来源id
    var fromId: String = "",

    //静音，免打扰状态
    var isMute: Boolean = false,

    //草稿内容
    var draftContent: String = "",

    //现在是否处于长按状态
    @Transient
    var isLongDown: Boolean = false
)