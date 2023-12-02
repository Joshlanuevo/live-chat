package com.ym.chat.utils

import com.ym.chat.widget.ateditview.AtUserHelper
import java.util.regex.Pattern

//import com.lqr.emoji.EmojiManager
//import com.lqr.emoji.MoonUtils

object EmojiUtils {

    //小米11系统表情格式
    val emojiFormat = "\\[([^\\[\\]]+)\\](查看表情)"

    /**
     * 是否图片
     */
    fun isImageBySys(string: String): Boolean {
        val matcher = Pattern.compile(emojiFormat).matcher(string)
        return matcher.find()
    }

    /**
     * 提取图url
     */
    fun getImageUrl(string: String): String {
        val matcher = Pattern.compile(emojiFormat).matcher(string)
        matcher.find()
        val sss = matcher.group()
        val s = matcher.group(1)
        return s
    }
}