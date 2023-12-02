/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ym.base.rxhttp

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.GsonUtils
import com.ym.base.constant.ConstBeanCode
import com.ym.base.rxhttp.parser.ApiErrorForat
import com.ym.base.rxhttp.utils.DefaultFormatPrinter
import com.ym.base.rxhttp.utils.ParseException
import com.ym.base.rxhttp.utils.ParserUtils
import com.ym.base.rxhttp.utils.ParserUtils.parseParams
import com.ym.base.util.other.QueueUtil
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit.NANOSECONDS

/**
 * ================================================
 * 解析框架中的网络请求和响应结果,并以日志形式输出,调试神器
 *
 *
 * Created by JessYan on 7/1/2016.
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
class RequestInterceptor : Interceptor {

    companion object {
        val isPrint = true           //是否打印日志
        val isRetryToken = false     //是否请求前检查token和请求后对结果检查token并重新请求
        val isHandleResult = false   //是否对请求到的Response的内容就行判断处理抛出异常
        var refreshTokenError: IOException? = null
        val dontNeedTokenUrlList = mutableListOf<String>().apply {
            //添加无需刷新token的url地址到此集合
            /*add(RetrofitManager.BASE_URL + ConstantValueApi.login)*/
        }

        //上次token刷新时间
        private var SESSION_KEY_REFRESH_TIME = 0L

        fun addHeader(request: Request): Request {
            val builder = request.newBuilder()

            val headers: MutableMap<String,String> = HashMap()
            headers["X-device"] = DeviceUtils.getUniqueDeviceId()
            headers["X-client"] = "android"
            headers["X-version"] = AppUtils.getAppVersionName()
            headers["X-version-code"] = AppUtils.getAppVersionCode().toString() + ""
            /*UserLoginManager.token?.let {
                headers["X-access_token"] = it
                //addHeader(""X-access_token", "09748acf2a715d1558528a524b3b9633");
            }*/
            for ((mapKey,mapValue) in headers.entries) {
                builder.removeHeader(mapKey)
                builder.addHeader(mapKey,mapValue)
            }
            return builder.build()
        }

        fun getStaticHeaders(): MutableMap<String,String> {
            val headers: MutableMap<String,String> = HashMap()
            headers["X-device"] = DeviceUtils.getUniqueDeviceId()
            headers["X-client"] = "android"
            headers["X-version"] = AppUtils.getAppVersionName()
            headers["X-version-code"] = AppUtils.getAppVersionCode().toString() + ""
            /*UserLoginManager.token?.let {
                headers["X-access_token"] = it
                //addHeader(""X-access_token", "09748acf2a715d1558528a524b3b9633");
            }*/
            return headers
        }

    }

    val queueUtils: QueueUtil = QueueUtil {
        when (it) {
            is Request -> {
                DefaultFormatPrinter.printJsonRequest(it,parseParams(it))
            }
            is Response -> {
                val chainMs = it.request.headers["chainMs"] ?: "0"
                DefaultFormatPrinter.printJsonResponse(it,chainMs,ParserUtils.parseContent(it))
            }
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val request = chain.request()
        if (isRetryToken) {
            /*val firstOrNull =
                dontNeedTokenUrlList.firstOrNull { request.url.toString().contains(it) }
            //当前请求地址不属于无需token就可以访问的接口,并且token 为空
            if (firstOrNull == null && UserLoginManager.token.isNullOrBlank()) {
                refreshToken(request)
            }*/
        }
        //当前未开启token检验开关或者当前请求地址属于无需token就可以访问的接口
        return requestToResponse(chain,request)
    }

    private fun requestToResponse(chain: Chain,request: Request): Response {
        val requestTime = System.nanoTime()
        val response = try {
//            val requestNew = addHeader(request)
            //打印请求信息
            printRequest(request)
            chain.proceed(request)
        } catch (e: Exception) {
            val errorString = StringBuilder("请求失败 Url: ").append("\n")
                .append(request.url.toString()).append("\n")
                .append("错误信息:").append("\n")
                .append(e.message).append("\n")
                .toString()
            throw IOException(errorString,e.cause).apply {
                //传递下去的Exception请穿原始 Exception,新new出来的Exception.因为不明确异常类型,不适合传下去
                ApiErrorForat.handleRequestError(e)
                printStackTrace()
            }
        }
        val responseTime = System.nanoTime()
        return response.run {
            val bodyString: String? = ParserUtils.parseResult(response)
            printResponse(this,NANOSECONDS.toMillis(responseTime - requestTime))
            if (!isHandleResult) {
                //如果你使用的是retrofit,请将isHandleResult置为true表示走下面的逻辑处理Response
                //如果是Rxhttp,则不需要看下面的逻辑,只需要看具体的Parser对Response的处理即可
                return@run this
            }
            if (!response.isSuccessful) {
                ApiErrorForat.handleResponseHttpError(response)
            }
            try {
                GsonUtils.fromJson(bodyString,BaseBean::class.java)
            } catch (e: Exception) {
                //Json解析过程中发生的错误都这么处理
                val errorString = StringBuilder("BaseJsonParser解析 Url: ").append("\n")
                    .append(response.request.url.toString()).append("\n")
                    .append("转化为BaseResponse失败").append("\n")
                    .append(e.message).toString()
                throw IOException(errorString,e).apply { this.printStackTrace() }
            }.apply {
                when {
                    code == ConstBeanCode.LOGIN_INVALID -> {
                        ApiErrorForat.handleResponseBeanError(code,message)
                        if (isRetryToken && request.headers["retry"].isNullOrBlank()) {
                            refreshToken(request)
                            val builder = request.newBuilder().addHeader("retry","true")
                            return@run requestToResponse(chain,builder.build())
                        } else {
                            throw ParseException(
                                code.toString(),
                                message,
                                response
                            ).also { it.printStackTrace() }
                        }
                    }
                    code != ConstBeanCode.SUCESS -> {
                        ApiErrorForat.handleResponseBeanError(code,message)
                        throw ParseException(
                            code.toString(),
                            message,
                            response
                        ).also { it.printStackTrace() }
                    }
                }
            }
            this
        }
    }

    private fun printRequest(request: Request) {
        if (isPrint) {
            queueUtils.enqueueAction(request.newBuilder().build())
        }
    }

    private fun printResponse(response: Response,chainMs: Long) {
        if (isPrint) {
            queueUtils.enqueueAction(
                response.newBuilder().request(
                    response.request.newBuilder().addHeader("chainMs",chainMs.toString()).build()
                ).build()
            )
        }
    }

    //刷新token
    @Throws(IOException::class)
    private fun refreshToken(request: Request) {
        /* synchronized(this) {
             if (SystemClock.elapsedRealtime() - SESSION_KEY_REFRESH_TIME < 10000) {
                 //请求时间小于上次请求token被回应后的时间，说明token刷新接口需要休息，则无需再次刷新
                 if (UserLoginManager.token.isNullOrBlank()) {
                     val ioException = refreshTokenError
                     throw if (ioException != null) ioException else {
                         //暂时不能去刷新token,但是token是空,表示上次刷新的token无效,所以需要直接报错
                         val errorString = StringBuilder("请求报错 Url: ").append("\n")
                             .append(request.url.toString()).append("\n")
                             .append("请求失败,未查询到token").append("\n")
                             .toString()
                         HttpException(request,"500",errorString).apply { printStackTrace() }
                     }
                 } else {

                 }
             } else {
                 runBlocking {
                     launch {
                         refreshTokenError = try {
                             UserLoginManager.token = RetrofitManager.instance.create().login().data
                             null
                         } catch (e : Exception) {
                             IOException(e)
                         } finally {
                             SESSION_KEY_REFRESH_TIME = SystemClock.elapsedRealtime()
                         }
                     }
                 }
             }
         }*/
    }


}