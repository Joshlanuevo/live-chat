package com.ym.base.startup

import android.content.Context
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.fetch.VideoFrameFileFetcher
import coil.fetch.VideoFrameUriFetcher
import coil.util.CoilUtils
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup
import com.ym.base.ext.logI
import com.ym.base.rxhttp.utils.SslContextFactory
import okhttp3.OkHttpClient
import javax.net.ssl.X509TrustManager

/**
 * Author:yangcheng
 * Date:2020-12-9
 * Time:14:19
 */
class CoilInit : AndroidStartup<Int>() {

    override fun callCreateOnMainThread() = false
    override fun waitOnMainThread() = false
    override fun create(context: Context): Int {
        val imageLoader = ImageLoader.Builder(context)
            .crossfade(300)
            .okHttpClient {
                val mSSLTrustManager = SslContextFactory.getSSLTrustManager()
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(context))
                    .sslSocketFactory(SslContextFactory.getSSLSocketFactory(mSSLTrustManager), mSSLTrustManager[0] as X509TrustManager)
                    .hostnameVerifier(SslContextFactory.SafeHostnameVerifier())
                    .build()
            }
            .componentRegistry {
                add(VideoFrameFileFetcher(context))
                add(VideoFrameUriFetcher(context))
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder())
                } else {
                    add(GifDecoder())
                }
            }
            .build()
        Coil.setImageLoader(imageLoader)
        "Coil初始化完成".logI()
        return 0
    }

    override fun dependencies(): List<Class<out Startup<*>>> {
        return mutableListOf(MMkvInit::class.java)
    }
}