package com.ym.chat.bean

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class LastMsgBean(
    @Id
    var id: Long = 0,
    var lastMsgId: String
)