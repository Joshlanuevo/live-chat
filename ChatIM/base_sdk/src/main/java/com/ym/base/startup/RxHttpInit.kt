package com.ym.base.startup

import android.content.Context
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.ym.base.ext.logI
import com.ym.base.rxhttp.RxHttpConfig

/**
 * Author:yangcheng
 * Date:2020/8/13
 * Time:13:36AndroidStartup
 */
class RxHttpInit : AndroidStartup<Int>() {
    override fun callCreateOnMainThread() = false
    override fun waitOnMainThread() = false

    override fun create(context: Context): Int {
        RxHttpConfig.init(context)
        "RxHttp初始化完成".logI()
        return 0
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return mutableListOf(MMkvInit::class.java)
    }
}