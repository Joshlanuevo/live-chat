package com.ym.chat.bean

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

/**
 * 系统通知 bean
 */
@Entity
data class DraftBean(
    @Id
    var dbId: Long = 0,
    @Unique
    var chatId: String? = "",
    var content: String? = "",
    var atContent:String? = "",//附加消息内容
    var type:Int = 0,//附加消息类型，1：回复，2：编辑
)