package com.ym.chat.bean

data class UpdateResponse(
    var url: String = "",
    val version: String? = null, //服务器最新版本
    val versionCode: Long = 0, //服务器最新版本
    var remark: String? = null, //code异常对应的信息提示
    val isForcibly: Int = 0,  // 升级方式 0: 不检测升级，1: 推荐升级 ，2: 强制升级
)