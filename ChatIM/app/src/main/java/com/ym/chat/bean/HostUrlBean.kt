package com.ym.chat.bean

data class HostUrlBean(
    //包网地址
    val bwHost: MutableList<String>,
    //活动大厅地址
    val actionHost: MutableList<String>,
) {
    data class HostBean(
        val domain: String,
        val id: String,
        val serverIp: String,
        val status: String,
        val type: String,//Outsource包网地址，
    )
}