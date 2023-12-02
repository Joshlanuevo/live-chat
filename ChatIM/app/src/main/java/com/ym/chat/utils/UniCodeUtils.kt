package com.ym.chat.utils

/**
 * @version V1.0
 * @createAuthor
 *       ___         ___          ___
 *      /  /\       /  /\        /  /\           ___
 *     /  /::\     /  /:/       /  /::\         /__/|
 *    /  /:/\:\   /__/::\      /  /:/\:\    __  | |:|
 *   /  /:/~/::\  \__\/\:\    /  /:/~/::\  /__/\| |:|
 *  /__/:/ /:/\:\    \  \:\  /__/:/ /:/\:\ \  \:\_|:|
 *  \  \:\/:/__\/    \__\:\  \  \:\/:/__\/  \  \:::|
 *   \  \::/         /  /:/   \  \::/        \  \::|
 *    \  \:\        /__/:/     \  \:\         \  \:\
 *     \  \:\       \__\/       \  \:\         \  \:\
 *      \__\/                    \__\/          \__\/
 * @createDate  2022/01/31 5:07 下午
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
object UniCodeUtils {

    /**
     * 补全length位，不够的在后面加0
     * @param str
     * @return
     */
    fun upToNStringInBack(str: String, length: Int): String {
        var result = StringBuilder()
        if (str.length < length) {
            result.append(str)
            for (i in 0 until length - str.length) {
                result.append("0")
            }
        } else {
            result = StringBuilder(str)
        }
        return result.toString()
    }

    /**
     * 将 unicode 转换成字符串，不带"\\u"
     */
    fun unicodeNoPrefixToUtf8(str: String): String {
        val builder = StringBuilder()
        val offset = str.length % 4
        var value = str
        if (offset != 0) {
            value = upToNStringInBack(str, str.length + (4 - offset))
        }
        for (i in value.indices step 4) {
            val end = i + 4
            if (str.length >= end) {
                val item = value.substring(i, end)
                val data = Integer.parseInt(item, 16)
                if (data != 0) builder.append(data.toChar())
            }
        }
        return builder.toString()
    }

    /**
     * 将 unicode 转换成字符串，带"\\u"
     */
    fun unicodeToUtf8(str: String): String {
        return unicodeNoPrefixToUtf8(str.replace("\\u", ""))
    }

    /**
     * 将字符转换成 unicode，不带"\\u"
     */
    fun utf8ToUnicodeNoPrefix(str: String): String {
        val builder = StringBuilder()
        for (c in str.iterator()) {
            val item = c.toInt().toString()
            builder.append(item)
        }
        return builder.toString()
    }

}