package com.ym.chat.utils

import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.ym.chat.bean.LogBean
import com.ym.chat.db.LogDb
import com.ym.chat.rxhttp.VPNRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object LogHelp {
    const val PUBLICTAG = "友聊App日志"
    fun e(info: String) {
        e(PUBLICTAG, info)
    }

    fun i(info: String) {
        i(PUBLICTAG, info)
    }

    fun d(info: String) {
        d(PUBLICTAG, info)
    }

    fun e(tag: String, info: String) {
        Log.e(tag, info)
//        LogDb.saveLog(
//            LogBean(
//                message = info,
//                date = getStrTime(),
//                type = "error"
//            )
//        )
//        uploadLog()
    }

    fun i(tag: String, info: String) {
        Log.i(tag, info)
//        LogDb.saveLog(
//            LogBean(
//                message = info,
//                date = getStrTime(),
//                type = "info"
//            )
//        )
//        uploadLog()
    }

    fun d(tag: String, info: String) {
        Log.d(tag, info)
//        LogDb.saveLog(
//            LogBean(
//                message = info,
//                date = getStrTime(),
//                type = "debug"
//            )
//        )
//        uploadLog()
    }

    val sf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    /**
     * 获取年月日时分秒
     */
    fun getStrTime(): String {
        return sf.format(Date())
    }
}