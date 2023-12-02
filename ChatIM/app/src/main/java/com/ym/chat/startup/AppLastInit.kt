package com.ym.chat.startup

import android.content.Context
import com.dianping.logan.Logan
import com.dianping.logan.LoganConfig
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.ym.base.ext.logI
import com.ym.base.startup.LibLastInit
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.db.ChatDao
import me.jessyan.autosize.AutoSizeConfig
import java.io.File


/**
 * Author:yangcheng
 * Date:2020-10-6
 * Time:16:11
 */
class AppLastInit : AndroidStartup<Int>() {
    override fun callCreateOnMainThread() = false
    override fun waitOnMainThread() = true
    override fun create(context: Context): Int {
        "AppLastInit初始化完成".logI()
        var needChangeHost = false
        //字体sp不跟随系统大小变化
        AutoSizeConfig.getInstance().isExcludeFontScale = true

        //初始化数据库
        MMKVUtils.getUser()?.let { user ->
            ChatDao.initDb(user.username)
        }

        initLogAn(context)

        return 0
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return mutableListOf(LibLastInit::class.java)
    }

    private fun initLogAn(context: Context) {
        val config = LoganConfig.Builder()
            .setCachePath(context.getFilesDir().getAbsolutePath())
            .setPath(
                (context.getExternalFilesDir(null)?.getAbsolutePath()
                        + File.separator).toString() + "logan_v1"
            )
            .setEncryptKey16("0123456789012345".toByteArray())
            .setEncryptIV16("0123456789012345".toByteArray())
            .build()
        Logan.init(config)
    }
}