package com.ym.chat.ext

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.text.toSpanned
import com.blankj.utilcode.util.ColorUtils.getColor
import com.ym.base.ext.logD
import com.ym.chat.R


/**
 * 字符串
 * 设置指定字符不同的颜色
 */
inline fun String?.setColorAndString(str: String, colorId: Int = getColor(R.color.color_main)): SpannableString {
    return if (!this.isNullOrBlank()) {
        var spannableString = SpannableString(this)
        //如果字符中包含这个字符
        if (!str.isNullOrBlank() && this.contains(str)) {
            var start = this.indexOf(str)
            var end = this.indexOf(str) + str.length
            spannableString.setSpan(
                ForegroundColorSpan(colorId),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        spannableString
    } else SpannableString("")
}

/**
 * 字符串
 * 设置指定字符不同的颜色
 * 添加搜索前的字段
 */
inline fun String?.setColorAndString(
    str: String,
    colorId: Int,
    strFront: String = "",//前面的字段
): SpannableString {
    return if (!this.isNullOrBlank()) {
        var spannableString = SpannableString(this)
        //如果字符中包含这个字符
        if (!str.isNullOrBlank() && this.contains(strFront) && this.contains(str)) {
            var strNew = this.substring(strFront.length)
            if (!strNew.isNullOrBlank() && strNew.contains(str)) {
                var start = strNew.indexOf(str) + strFront.length
                var end = strNew.indexOf(str) + str.length + strFront.length
                spannableString.setSpan(
                    ForegroundColorSpan(colorId),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        spannableString
    } else SpannableString("")
}

/**
 * 字符串
 * 设置指定字符不同的颜色
 */
inline fun String?.setColorAndString(
    strStart: String,
    strEnd: String,
    colorId: Int
): SpannableString {
    return if (!this.isNullOrBlank()) {
        var spannableString = SpannableString(this)
        //如果字符中包含这个字符
        if (!strStart.isNullOrBlank() && this.contains(strStart)) {
            var start = this.indexOf(strStart)
            var end = this.indexOf(strStart) + strStart.length
            spannableString.setSpan(
                ForegroundColorSpan(colorId),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        //如果字符中包含这个字符
        if (!strEnd.isNullOrBlank() && this.contains(strEnd)) {
            var start = this.indexOf(strEnd)
            var end = this.indexOf(strEnd) + strEnd.length
            spannableString.setSpan(
                ForegroundColorSpan(colorId),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        spannableString
    } else SpannableString("")
}

inline fun String.getMimeType(): String? {
    val fileName = this.lowercase()
    return when {
        fileName.endsWith(".png") -> ".png"
        fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> ".jpg"
        fileName.endsWith(".webp") -> ".webp"
        fileName.endsWith(".gif") -> ".gif"
        fileName.endsWith(".mp4") -> ".mp4"
        else -> ".jpg"
    }
}