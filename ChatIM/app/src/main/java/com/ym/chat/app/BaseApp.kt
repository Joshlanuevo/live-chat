package com.ym.chat.app

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.ym.chat.db.AccountDao
import com.ym.chat.ui.HomeActivity
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import com.ym.chat.utils.LanguageUtils

class BaseApp : Application() {

    companion object {
        var appContent: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        initData()
        registerActivityLifecycleCallbacks(LanguageUtils.callbacks)
    }

    /***
     * 两个项目 共有的初始化可以放在这个方法里进行
     */
    private fun initData() {

        appContent = applicationContext

        Logger.addLogAdapter(AndroidLogAdapter())

        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ -> ClassicsHeader(context) }

        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ -> ClassicsFooter(context) }

        //初始化账号管理数据
        AccountDao.initAccDb()
        EmojiManager.install(IosEmojiProvider())

//        Thread.setDefaultUncaughtExceptionHandler(restartHandler) // 程序崩溃时触发线程  以下用来捕获程序崩溃异常
    }

    // 创建服务用于捕获崩溃异常
    private val restartHandler: Thread.UncaughtExceptionHandler =
        Thread.UncaughtExceptionHandler { thread, ex ->
            restartApp() //发生崩溃异常时,重启应用
        }

    //重启App
    private fun restartApp() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
        Process.killProcess(Process.myPid()) //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
    }

}