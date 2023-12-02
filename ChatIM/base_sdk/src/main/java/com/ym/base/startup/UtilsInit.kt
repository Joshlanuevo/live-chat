package com.ym.base.startup

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.Utils
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.ym.base.ext.logI

/**
 * @Description
 * @Author：CASE
 * @Date：2021-06-09
 * @Time：23:55
 */
class UtilsInit : AndroidStartup<Int>() {
    override fun callCreateOnMainThread() = true//正式版在非主线程会闪退
    override fun waitOnMainThread() = false

    override fun create(context: Context): Int {
        Utils.init(context as Application)
        "Utils初始化完成".logI()
        return 0
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return mutableListOf()
    }
}