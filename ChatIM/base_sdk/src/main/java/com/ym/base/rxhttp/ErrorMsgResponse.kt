package com.ym.base.rxhttp

/**
 * 请求错误后  重写解析成 ErrorMsgResponse
 */
data class ErrorMsgResponse(
        val status: Boolean,
        val data: String? = "",
)
