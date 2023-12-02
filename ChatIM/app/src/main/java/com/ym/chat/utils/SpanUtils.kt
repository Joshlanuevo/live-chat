package com.ym.chat.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.ym.base.widget.span.RadiusBackgroundSpan
import com.ym.chat.R


object SpanUtils {
    //获取简单带颜色的span
    fun getSpanSimple(text: String, colorId: Int = ColorUtils.getColor(R.color.red_span), bold: Boolean = false): SpannableString {
        val color = ForegroundColorSpan(colorId)
        val title = SpannableString(text)
        title.setSpan(color, 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (bold) title.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        else title.setSpan(StyleSpan(Typeface.NORMAL), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return title
    }

    //带圆角背景的span
    fun getRadiusBgSpan(text: String, textSizePx: Int, bgRadiusPx: Int, foreColor: Int, bgColor: Int, bold: Boolean = false): SpannableString {
        val ss = SpannableString(text)
        val style = StyleSpan(Typeface.BOLD)
        ss.setSpan(
            RadiusBackgroundSpan(
                bgColor, foreColor, SizeUtils.dp2px(4f), SizeUtils.dp2px(1f),
                SizeUtils.dp2px(1f), bgRadiusPx, textSizePx
            ), 0, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (bold) ss.setSpan(style, 0, ss.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return ss
    }
}