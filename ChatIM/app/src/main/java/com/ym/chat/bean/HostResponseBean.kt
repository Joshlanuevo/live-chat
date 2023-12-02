package com.ym.chat.bean

data class HostResponseBean(
    val data:MutableList<HostUrlBean.HostBean>,
    val mainUrl:String,
    val spareUrl:String,
    val urgentUrl:String,
    val warnUrl:String,
)