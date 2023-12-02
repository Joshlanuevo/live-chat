package com.ym.chat.utils

import android.annotation.SuppressLint
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.Utils
import com.ym.base.ext.toast
import com.ym.base.ext.xmlToast
import com.ym.chat.R


class NetUtils {
    companion object {
        @SuppressLint("MissingPermission")
        fun checkNetToast(): Boolean {
            return try {
                if (NetworkUtils.isConnected()) {
                    true
                } else {
                    ChatUtils.getString(R.string.网络连接不可用).toast()
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}