package com.ym.chat.bean

data class KeyWordBean(
    val id: String,
    val content: String,
    val status: String,//Disable(禁用)  Enable(启用)
    val memberLevelId: String,
    val createTime: String,
)