package com.ym.chat.utils

import android.text.TextUtils
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.SPUtils
import com.ym.base.constant.HostManager
import com.ym.base.ext.getWebpUrl
import com.ym.base.ext.launchError
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.VPNRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import okhttp3.internal.wait
import org.junit.Test
import rxhttp.RxHttp
import rxhttp.toOkResponse
import rxhttp.wrapper.cahce.CacheMode
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object HostUtils {
    //<editor-fold defaultstate="collapsed" desc="内部调用">
    //获取响应最快的host
    fun getFastestHost(callBack: (host: String) -> Unit, hosts: MutableList<String>) {
        launchError(handler = { _, e ->
            LogHelp.i(e.toString())
//            callBack("")
            e.logE()
        }) {
            select<String> {

//                /**
//                 * 超时
//                 */
//                onTimeout(15000) {
//                    pingHost(ApiUrl.VPN.APP_H5_ADDRES)
//                }

                hosts.forEach { host ->
                    async {
                        pingHost(
                            host
                        )
                    }.onAwait { it }
                }
            }.let(callBack)
        }
    }

    /**
     * 检测活动大厅地址
     */
    fun checkActionFastHost(callBack: (host: String) -> Unit, hosts: MutableList<String>) {
        launchError(handler = { _, e ->
            LogHelp.i(e.toString())
//            callBack("")
            e.logE()
        }) {
            select<String> {

//                /**
//                 * 超时
//                 */
//                onTimeout(15000) {
//                    pingHostAction(ApiUrl.VPN.APP_EVENT_HALL)
//                }

                hosts.forEach { host ->
                    async {
                        pingHostAction(
                            host
                        )
                    }.onAwait { it }
                }
            }.let(callBack)
        }
    }

    /**
     * 超时使用默认的
     */
    fun getFastestHost(
        callBack: (host: String) -> Unit,
        hosts: MutableList<String>,
        defaltUrl: String
    ) {
        launchError(handler = { _, e ->
            LogHelp.i(e.toString())
            e.logE()
        }) {
            select<String> {

                /**
                 * 超时
                 */
                onTimeout(8000) {
//                    LogHelp.e("检测超时")
                    pingHost(defaltUrl)
                }

                hosts.forEach { host ->
                    async {
                        pingHost(
                            host
                        )
                    }.onAwait { it }
                }
            }.let(callBack)
        }
    }

    //获取响应最快的host
    private fun getFastestHost(callBack: (host: String) -> Unit, vararg hosts: String) {
        launchError(handler = { _, e -> e.logE() }) {
            select<String> {
                for (host in hosts) async {
                    pingHost(
                        host
                    )
                }.onAwait { it }
            }.let(callBack)
        }
    }

    //如果可以ping通则返回当前Host，否则抛出错误，防止拿到不可用的host
    @Throws(Exception::class)
    private suspend fun pingHost(host: String): String {
        return suspendCancellableCoroutine { con ->
            launchError(Dispatchers.IO) {
                val time = System.currentTimeMillis()
                try {
                    val response =
                        RxHttp.head(host).setCacheMode(CacheMode.ONLY_NETWORK).toOkResponse()
                            .await()
                    if (response.isSuccessful) {
                        val hasHeaderA =
                            response.headers.names().contains("A")
                        if (hasHeaderA) {
                            LogHelp.i("地址：${host} ==检测通过，耗时==${System.currentTimeMillis() - time}")
                            con.resume(host)
                        } else {
                            LogHelp.i("地址：${host} ==检测失败，缺少响应头A")
                            upHostState(host)
//                            con.resumeWithException(Exception("host is not available:$host"))
                        }
                    } else {
                        LogHelp.i("地址：${host} ==检测失败，${response.code}")
//                        con.resumeWithException(Exception("host is not available:$host"))
                        upHostState(host)
                    }
                } catch (e: Exception) {
                    LogHelp.i("地址：${host} ==检测失败== $e")
                    upHostState(host)
//                    con.resumeWithException(Exception("host is not available:$host"))
                }
            }
        }
    }

    //如果可以ping通则返回当前Host，否则抛出错误，防止拿到不可用的host
    @Throws(Exception::class)
    private suspend fun pingHostAction(host: String): String {
        return suspendCancellableCoroutine { con ->
            launchError(Dispatchers.IO) {
                val time = System.currentTimeMillis()
                try {
                    val response =
                        RxHttp.head(host).setCacheMode(CacheMode.ONLY_NETWORK).toOkResponse()
                            .await()
                    if (response.isSuccessful) {
                        LogHelp.i("活动大厅地址：${host} ==检测通过，耗时==${System.currentTimeMillis() - time}")
                        con.resume(host)
                    } else {
                        upHostState(host)
                        LogHelp.i("活动大厅地址：${host} ==检测失败,${response.code}")
//                        con.resumeWithException(Exception("host is not available:$host"))
                    }
                } catch (e: Exception) {
                    upHostState(host)
                    LogHelp.i("活动大厅地址：${host} ==检测失败==,$e")
//                    con.resumeWithException(Exception("host is not available:$host"))
                }
            }
        }
    }
    //</editor-fold>

    suspend fun upHostState(host: String) {
        val errUpLoad = SPUtils.getInstance(VPNContants.SPNAME).getString(
            VPNContants.ERROR_UPURL, ""
        )
        VPNContants.cacheHost.firstOrNull { it.domain == host }?.let {
            //异常上传
            var upUrl = ""
            if (errUpLoad.endsWith("/")) {
                upUrl = "${errUpLoad}${it.id}"
            } else {
                upUrl = "${errUpLoad}/${it.id}"
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="测试">
    @Test
    fun test() {
        getFastestHost(
            { "【${Thread.currentThread().name}】最快的Host:$it".logE() },
            "www.baidu.com",
            "www.google.com",
            "www.youtube.com",
            "www.tmall.com",
            "www.jianshu.com",
            "www.jikedaohang.com",
        )
        //结果打印：
        //2021-05-26 13:06:03.597 27039-27168/com.kokvn.app2 I/YM-: {www.baidu.com} 耗时=93
        //2021-05-26 13:06:03.598 27039-27039/com.kokvn.app2 I/YM-: 【main】最快的Host:www.baidu.com
        //2021-05-26 13:06:03.600 27039-27418/com.kokvn.app2 I/YM-: {www.youtube.com} 耗时=91
        //2021-05-26 13:06:03.705 27039-27336/com.kokvn.app2 I/YM-: {www.google.com} 耗时=200
        //2021-05-26 13:06:03.731 27039-27218/com.kokvn.app2 I/YM-: {www.jikedaohang.com} 耗时=223
        //2021-05-26 13:06:03.802 27039-27172/com.kokvn.app2 I/YM-: {www.jianshu.com} 耗时=295
        //2021-05-26 13:06:04.401 27039-27165/com.kokvn.app2 I/YM-: {www.tmall.com} 耗时=895
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="外部调用链路选择(成功后调用无效,没有网络调用无效)">
    //是否已经检查完链路
    private var sucCount = -1
    private var mJobPing: Job? = null
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="真正的链路选择逻辑">
    //判断是否请求完成
    var mFastestApi: String = ""
    var mFastestDl: String = ""
    var mFastestYunQue: String = ""

    //Host检查
    private fun pingLink() {
        val mListDl = HostManager.YnHost.hostsDl
        val mListApi = HostManager.YnHost.hostsApi
        val mListYunQue = HostManager.YnHost.yunQueH5
        mJobPing?.cancel()//先取消超时处理
        //检测最快Host(先检测云雀，因为进入引导页后就要调用统计)
        getFastestHost(callBack = {
            if (it.isNotBlank()) {
                "云雀最快域名:$it".logE()
                mFastestYunQue = "https://${it}/iosign/"
                MMKVUtils.saveHostYunQue(mFastestYunQue)
            } else if (it.isBlank()) "云雀最快域名未获取到".logE()
            sucCount++
            if (sucCount >= 3) {
                mJobPing?.cancel()
                sucCount = -1
            }
        }, mListYunQue)
        //将API放到DL里面，为了先从DL获取时间
        getFastestHost(callBack = {
            if (it.isNotBlank()) {
                "DL最快域名:$it".logE()
                mFastestDl = "https://${it}/"
                MMKVUtils.saveHostDl(mFastestDl)
            } else if (it.isBlank()) "DL最快域名未获取到".logE()
            sucCount++
            if (sucCount >= 3) {
                mJobPing?.cancel()
                sucCount = -1
            }
            //API放到DL里面是因为DL才能拿到服务器时间，而API需要依赖服务器时间
            getFastestHost(callBack = { u ->
                if (u.isNotBlank()) {
                    "Api最快域名:$u".logE()
                    mFastestApi = "https://${u}/"
                    MMKVUtils.saveHostApi(mFastestApi)
                } else if (u.isBlank()) "Api最快域名未获取到".logE()
                sucCount++
                if (sucCount >= 3) {
                    mJobPing?.cancel()
                    sucCount = -1
                }
            }, mListApi)
        }, mListDl)
        //防止有没有回调的ping，所以设置最大超时10秒
        mJobPing = launchError {
            withContext(Dispatchers.IO) { delay(10L * TimeConstants.SEC) }.let {
                if (isActive) sucCount = -1
            }
        }
    }

    //检查是否有正确回调
    private fun checkCompleteSuc(): Boolean {
        return mFastestApi.isNotBlank() && mFastestDl.isNotBlank() && mFastestYunQue.isNotBlank()
    }
    //</editor-fold>
}