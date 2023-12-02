package com.ym.base.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.tencent.mmkv.MMKV
import com.ym.base.ext.logI

/**
 * Author:yangcheng
 * Date:2020/8/12
 * Time:16:08
 */
class MMkvInit : AndroidStartup<Int>() {
    override fun callCreateOnMainThread() = false
    override fun waitOnMainThread() = false

    override fun create(context: Context): Int {
        MMKV.initialize(context)
        "MMKV初始化完成".logI()
        return 0
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return mutableListOf(UtilsInit::class.java)
    }
}