package com.ym.chat.rxhttp

import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.google.gson.reflect.TypeToken
import com.ym.base.ext.launchError
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.BuildConfig
import com.ym.chat.bean.*
import com.ym.chat.utils.AESOperator
import com.ym.chat.utils.DeviceInfoUtils
import com.ym.chat.utils.VPNContants
import com.ym.chat.utils.VPNContants.SPLASH_IMAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Response
import org.jetbrains.anko.custom.async
import org.json.JSONObject
import rxhttp.*
import rxhttp.wrapper.cahce.CacheMode
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.resume

/**
 * @Description
 * @Author
 * @Date：2021-07-14
 * @Time：14:16
 */
object VPNRepository : BaseRepository() {
    /**
     * 获取IM oss数据
     */
    suspend fun getIMOssConfig(): String {
        val ossUrl = if (TextUtils.isEmpty(BuildConfig.ossUrl)) {
            //没有动态配置，使用默认的
            ApiUrl.getOssConfig
        } else {
            //动态配置了，使用动态配置的
            BuildConfig.ossUrl
        }
        return RxHttp.get(ossUrl)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

//    suspend fun getActionHost(): MutableList<String> {
//        return RxHttp.get(ApiUrl.VPN.getActionHostUrl)
//            .setCacheMode(CacheMode.ONLY_NETWORK)
//            .toStr()
//            .await()
//    }
}