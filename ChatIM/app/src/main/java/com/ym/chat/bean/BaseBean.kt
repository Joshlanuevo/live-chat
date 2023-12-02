package com.ym.chat.bean

import java.io.Serializable


data class BaseBean<T>(
    var info: String = "",
    var code: Int = 0,
    val data: T
) : Serializable