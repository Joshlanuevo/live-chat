package com.ym.chat.bean

import android.graphics.Color
import android.view.Gravity
import com.ym.base.ext.xmlToColor
import com.ym.chat.R


data class TxtSimpleBean(
        val txt: String,
        val txtColor: Int = R.color.transfer_bind_hint_gray.xmlToColor(),
        val heightDp: Float = 76f,
        val textSizeSp: Float = 14f,
        val gravity: Int = Gravity.CENTER,
        var paddingTopPx: Int = 0,
        var paddingStartPx: Int = 0,
        var paddingEndPx: Int = 0,
        var paddingBottomPx: Int = 0,
        var bold: Boolean = false,
        var bgColor: Int = Color.TRANSPARENT,
)
