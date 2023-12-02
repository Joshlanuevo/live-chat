package com.ym.chat.bean

import android.graphics.Color
import com.blankj.utilcode.util.SizeUtils

/**
 * Author：CASE
 * Date:2020/8/13
 * Time:22:31
 */
data class DividerBean(
    val heightPx: Int = SizeUtils.dp2px(1f),
    val bgColor: Int = Color.parseColor("#ebebeb"),
    val isShow: Boolean = true //是否显示这个线
) {
    var marginStart: Int = 0
    var marginEnd: Int = 0
    var tag: String = ""
}