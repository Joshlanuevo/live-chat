package com.ym.base.rxhttp

import android.content.Context
import com.blankj.utilcode.constant.MemoryConstants
import com.ym.base.constant.ConstTime
import com.ym.base.util.HttpDnsUtils
import okhttp3.OkHttpClient
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.ssl.HttpsUtils
import java.util.concurrent.TimeUnit

/**
 * Author:对Rxhttp的简单配置
 * Date:2020-10-6
 * Time:16:05
 */
object RxHttpConfig {

    //初始化RxHttp https://github.com/liujingxing/okhttp-RxHttp/wiki/%E5%88%9D%E5%A7%8B%E5%8C%96

    fun init(context: Context) {
        //设置debug模式，默认为false，设置为true后，发请求，过滤"RxHttp"能看到请求日志
        ////设置缓存目录为：Android/data/{app包名目录}/cache/RxHttpCache
        //val cacheDir = File(PathConfig.API_CACHE_DIR)
        //设置最大缓存为10M，缓存有效时长为7天，请求失败读取缓存
        val cacheMode = CacheMode.REQUEST_NETWORK_FAILED_READ_CACHE

        //非必须,只能初始化一次，第二次将抛出异常
        RxHttpPlugins.init(getRxhttpOkHttpClient())
            .setDebug(false)
            //缓存时需要生成由参数构成的key，如果参数中有当前时间的参数，则会导致缓存变得无意义，所以需要把这个会导致缓存无意义的参数剔除掉
            .setExcludeCacheKeys(
                "Connection",
                "Accept",
                "Content-Type",
                "Charset",
                "app-info",
                "platform",
            )
            //设置缓存目录为：Android/data/{app包名目录}/cache/RxHttpCache，设置最大缓存为10M，缓存有效时长为7天，请求失败读取缓存
            .setCache(
                context.codeCacheDir,
                10L * MemoryConstants.MB,
                cacheMode,
                ConstTime.WEEK_1
            )
    }

    //OkHttpClient
    fun getRxhttpOkHttpClient(): OkHttpClient {
        val builder = getOkHttpClient()
        builder.writeTimeout(15, TimeUnit.SECONDS)
        builder.readTimeout(15, TimeUnit.SECONDS)
        builder.connectTimeout(15, TimeUnit.SECONDS)
//        builder.dns(HttpDnsUtils())//接入HttpDNS
        builder.addInterceptor(HeaderInterceptor())
        builder.addInterceptor(RequestInterceptor())
        return builder.build()
    }

    //其他配置获取Okhttp对象
    fun getOkHttpClient(): OkHttpClient.Builder {
        val sslParams = HttpsUtils.getSslSocketFactory()
        val builder = OkHttpClient.Builder()
//            .dns(HttpDnsUtils())//接入HttpDNS
            .connectTimeout(ConstTime.NET_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(ConstTime.NET_TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(ConstTime.NET_TIME_OUT, TimeUnit.SECONDS)
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
            .hostnameVerifier { _, _ -> true } //忽略host验证
        return builder
    }

    //获取websocket的OkHttpClient
    fun getSocketOkHttpClient(): OkHttpClient {
        val sslParams = HttpsUtils.getSslSocketFactory()
        val builder = OkHttpClient.Builder()
            .pingInterval(15, TimeUnit.SECONDS)
            .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
            .hostnameVerifier { _, _ -> true }
            .retryOnConnectionFailure(true)
        return builder.build()
    }


}