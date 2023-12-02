package com.ym.chat.bean

/**
 * 版本信息
 */
data class VersionBean(
    val createTime: String,
    val creator: String,
    val downloadUrl: String,
    val id: String,
    val mustUpdate: String,
    val type: String,
    val versionDesc: String,
    val versionNo: String,
)