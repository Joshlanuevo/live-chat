package com.ym.base.constant

data class HttpErrorBean(
    //请求地址
    val requestUrl: String,
    //请求状态码
    val responseCode: Int
)