package com.ym.chat.utils

import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ym.base.util.save.MMKVUtils

/**
 * 管理当前项目配置
 */
object AppManager {
    val screenWidth by lazy { ScreenUtils.getAppScreenWidth() }
    val screenHeight by lazy { ScreenUtils.getAppScreenHeight() }
    val statusBarHeight by lazy { BarUtils.getStatusBarHeight() }
    var isMsgNotice: Boolean = MMKVUtils.mmkv?.getBoolean("isMsgNotice", true) ?: true
        set(value) {
            field = value
            MMKVUtils.mmkv?.putBoolean("isMsgNotice", field)
        }
    var isMsgRinging: Boolean = MMKVUtils.mmkv?.getBoolean("isMsgRinging", true) ?: true
        set(value) {
            field = value
            MMKVUtils.mmkv?.putBoolean("isMsgRinging", field)
        }
    var isMsgShock: Boolean = MMKVUtils.mmkv?.getBoolean("isMsgShock", true) ?: true
        set(value) {
            field = value
            MMKVUtils.mmkv?.putBoolean("isMsgShock", field)
        }

}