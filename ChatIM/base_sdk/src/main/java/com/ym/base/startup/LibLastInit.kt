package com.ym.base.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.ym.base.ext.logI

/**
 * Author:yangcheng
 * Date:2020-10-6
 * Time:16:11
 */
class LibLastInit : AndroidStartup<Int>() {
    override fun callCreateOnMainThread() = false
    override fun waitOnMainThread() = false

    override fun create(context: Context): Int {
        "BaseLib初始化完成".logI()
        return 0
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return mutableListOf(RxHttpInit::class.java, CoilInit::class.java)
    }
}