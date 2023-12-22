package com.ym.chat.utils

import android.util.Log
import com.dianping.logan.Logan
import com.ym.base.util.save.MMKVUtils

/**
 * @version V1.0
 * @createAuthor
 * @createDate  2022/9/24 16:30
 * @updateAuthor
 * @updateDate
 * @description 上报日志
 * @copyright copyright(c)2022 Technology Co., Ltd. Inc. All rights reserved.
 */
object GNLog {

    var hasAppendNew = false
    fun i(info: String) {
//        Log.d("GNLog", "IP:${MMKVUtils.getString("IP")}" + info)
        Logan.w(info, 2)
        Logan.f()
        hasAppendNew = true
    }

    fun netWork(info: String) {
//        Log.d("GNLog", "IP:${MMKVUtils.getString("IP")}" + info)
        Logan.w(info, 3)
        Logan.f()
        hasAppendNew = true
    }

    fun d(info: String) {
//        Log.d("GNLog", "IP:${MMKVUtils.getString("IP")}" + info)
        Logan.w(info, 4)
        Logan.f()
        hasAppendNew = true
    }
}