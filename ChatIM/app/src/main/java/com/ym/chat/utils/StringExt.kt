package com.ym.chat.utils

import com.blankj.utilcode.util.ApiUtils
import com.blankj.utilcode.util.Utils
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.rxhttp.ApiUrl
import java.util.regex.Pattern

object StringExt {
    //@消息规则
    val AT_PATTERN = "@\\[\\d{19}\\]"

    /**
     * 是否@消息
     */
    fun String.isAtMsg(): Boolean {
        return Pattern.compile(AT_PATTERN).matcher(this).find()
    }

    /**
     * 解密
     */
    fun String.decodeContent(): String {
        val userInfo = MMKVUtils.getUser()
        return String(
            AesUtils.decode(
                "woyouyiwangexiaomimijiubugaosuni",
                "doyouloveme",
                this.toByteArray()
            )
        )
    }


//    /**
//     * 群消息解密
//     */
//    fun String.decodeContent(groupId:String): String {
//        return String(
//            AesUtils.decode(
//                groupId,
//                "woyouyiwangexiaomimijiubugaosuni",
//                this.toByteArray()
//            )
//        )
//    }

    /**
     * 显示在群内的名称
     */
    fun String.showInGroupName(role: String): String {
        return if (role.lowercase() == "owner") {
            "[${ChatUtils.getString(R.string.群主)}]$this"
        } else if (role.lowercase() == "admin") {
            "[${ChatUtils.getString(R.string.管理员)}]$this"
        } else {
            this
        }
    }

    /**
     * 文件服务器处理
     */
    fun String.toFileUrl(): String {
        return if (this.contains("https://image.gnit.vip")) {
            this.replace("https://image.gnit.vip", ApiUrl.baseApiUrl ?: "https://image.gnit.vip")
        } else {
            this
        }
    }

}