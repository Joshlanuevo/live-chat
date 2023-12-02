/*
 * Copyright 2018 JessYan
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
package com.ym.base.rxhttp.utils

import android.text.TextUtils
import android.util.Log
import com.ym.base.rxhttp.utils.CharacterHandler.jsonFormat
import com.ym.base.rxhttp.utils.CharacterHandler.xmlFormat
import com.ym.base.rxhttp.utils.ParserUtils.isForm
import com.ym.base.rxhttp.utils.ParserUtils.isJson
import com.ym.base.rxhttp.utils.ParserUtils.isXml
import com.ym.base_sdk.BuildConfig
import okhttp3.Request
import okhttp3.Response
import java.net.URLDecoder

/**
 * ================================================
 * 对 OkHttp 的请求和响应信息进行更规范和清晰的打印, 此类为框架默认实现, 以默认格式打印信息, 若觉得默认打印格式
 * 并不能满足自己的需求, 可自行扩展自己理想的打印格式
 *
 * Created by JessYan on 25/01/2018 14:51
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object DefaultFormatPrinter {
    var isLog = BuildConfig.DEBUG

    /**
     * 打印网络请求信息, 当网络请求时 {[okhttp3.RequestBody]} 可以解析的情况
     */
    fun printJsonRequest(request : Request,bodyString : String?) {
        if (!isLog) return
        val tag = getTag(true)
        debugInfo(tag,REQUEST_UP_LINE)
        logLines(tag,arrayOf("URL: " + request.url),false)
        logLines(tag,getRequest(request),true)
        if (bodyString == null) {
            //当网络请求时 {[okhttp3.RequestBody]} 为 `null` 或不可解析的情况
            logLines(tag,OMITTED_REQUEST,true)
        } else {
            val bodyStringNew = request.body?.contentType()?.let {
                when {
                    isJson(it) -> jsonFormat(bodyString)
                    isXml(it) -> xmlFormat(bodyString)
                    else -> bodyString
                }
            }

            //当网络请求时 {[okhttp3.RequestBody]} 可以解析的情况
            val requestBodyString = StringBuilder()
                .append("Body:").append(LINE_SEPARATOR)
                .apply {
                    if (bodyStringNew.isNullOrEmpty()) {
                        append("未传参数")
                    } else {
                        append(bodyStringNew)
                    }
                }.split(LINE_SEPARATOR.toRegex()).toTypedArray()
            logLines(tag,requestBodyString,true)

            request.body?.contentType()?.takeIf { isForm(it) }?.run {
                val charset = ParserUtils.convertCharset(this.charset() ?: Charsets.UTF_8)

                bodyStringNew?.takeIf { it.isNotEmpty() && it.contains(RSAUtil.PARAMS_KEY) }?.run {
                    var realParams = replace("${RSAUtil.PARAMS_KEY}=","")

                    try {
                        realParams.takeIf { UrlEncoderUtils.hasUrlEncoded(it) }?.let {
                            //某些手机上会因为系统框架的不同,内置的URLDecoder不同可能会对Charset的UTF-8解析失败,报出IllegalCharsetNameException
                            //所以try catch的时候不仅仅要拦截IOException,而是使用Exception全拦截,长见识了
                            realParams = URLDecoder.decode(it,charset.toString())
                        }
                    } catch (e : Exception) {
                        e.printStackTrace()
                    }

                    if (RSAUtil.getInstance().mAppPrivateKeyStr.isNotEmpty()) {
                        realParams =
                            RSAUtil.getInstance().decodeByPrivateKey(realParams) ?: realParams
                    }
                    //当网络请求时 {[okhttp3.RequestBody]} 可以解析的情况
                    val realParamsString = StringBuilder()
                        .append(LINE_SEPARATOR)
                        .append("Body:")
                        .append(LINE_SEPARATOR)
                        .apply {
                            if (realParams.isEmpty()) {
                                append("请求参数反序列化失败")
                            } else {
                                append(jsonFormat(realParams))
                            }
                        }.split(LINE_SEPARATOR.toRegex()).toTypedArray()
                    logLines(tag,realParamsString,true)
                }
            }

        }
        debugInfo(tag,END_LINE)
    }

    /**
     * 打印网络响应信息, 当网络响应时 {[okhttp3.ResponseBody]} 可以解析的情况
     *
     * @param chainMs 服务器响应耗时(单位毫秒)
     * @param isSuccessful 请求是否成功
     * @param code 响应码
     * @param headers 请求头
     * @param contentType 服务器返回数据的数据类型
     * @param bodyString 服务器返回的数据(已解析)
     * @param segments 域名后面的资源地址
     * @param message 响应信息
     * @param responseUrl 请求地址
     */
    fun printJsonResponse(response : Response,chainMs : String,bodyString : String?) {
        if (!isLog) return
        val tag : String = getTag(false)
        val urlLine : Array<String> = arrayOf("URL: " + response.request.url.toString(),N)
        debugInfo(tag,RESPONSE_UP_LINE)
        logLines(tag,urlLine,true)
        logLines(tag,getResponse(response,chainMs),true)
        if (bodyString == null) {
            //当网络响应时 {[okhttp3.ResponseBody]} 为 `null` 或不可解析的情况
            logLines(tag,OMITTED_RESPONSE,true)
        } else {
            val bodyStringNew : String? = response.body?.contentType()?.let {
                when {
                    isJson(it) -> jsonFormat(bodyString)
                    isXml(it) -> xmlFormat(bodyString)
                    else -> bodyString
                }
            }
            //当网络请求时 {[okhttp3.RequestBody]} 可以解析的情况
            val responseBodyString = StringBuilder()
                .append("Body:").append(LINE_SEPARATOR)
                .append(bodyStringNew).split(LINE_SEPARATOR.toRegex()).toTypedArray()
            if (responseBodyString.size > 300) {
            val responseBodyString2 = StringBuilder()
                .append("Body: Json过大行数大于300行，不予格式化打印").append(LINE_SEPARATOR)
                .append(bodyString)
                logLinesSeparator(tag, arrayOf(responseBodyString2.toString()), true)
            } else {
                logLines(tag,responseBodyString,true)
            }
        }
        debugInfo(tag,END_LINE)
    }

    private const val TAG = "ArmsHttpLog"
    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"
    private val DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR
    private val OMITTED_RESPONSE = arrayOf(LINE_SEPARATOR,"没有响应体")
    private val OMITTED_REQUEST = arrayOf(LINE_SEPARATOR,"没有请求体")
    private const val N = "\n"
    private const val T = "\t"
    private const val REQUEST_UP_LINE =
        "   ┌────── Request ────────────────────────────────────────────────────────────────────────"
    private const val END_LINE =
        "   └───────────────────────────────────────────────────────────────────────────────────────"
    private const val RESPONSE_UP_LINE =
        "   ┌────── Response ───────────────────────────────────────────────────────────────────────"
    private const val CORNER_UP = "┌ "
    private const val CORNER_BOTTOM = "└ "
    private const val CENTER_LINE = "├ "
    private const val DEFAULT_LINE = "│ "
    private val NAME_SPACE = arrayOf("-A-","-R-","-M-","-S-")
    private val last : ThreadLocal<Int> = object : ThreadLocal<Int>() {
        override fun initialValue() : Int {
            return 0
        }
    }

    private fun isEmpty(line : String) : Boolean {
        return TextUtils.isEmpty(line) || N == line || T == line || TextUtils.isEmpty(line.trim())
    }

    /**
     * 对 `lines` 中的信息进行逐行打印
     *
     * @param withLineSize 为 `true` 时, 每行的信息长度不会超过110, 超过则自动换行
     */
    private fun logLines(tag : String,lines : Array<String>,withLineSize : Boolean) {
        for (line in lines) {
            val lineLength = line.length
            //解决AndroidStudio的logcat显示超长字符串的问题==一行打印3900个长度没问题.
            //https://blog.csdn.net/IWantToHitRen/article/details/70649659?utm_source=blogxgwz3
            val maxLongSize = if (withLineSize) 1100 else lineLength
            for (i in 0..lineLength / maxLongSize) {
                val start = i * maxLongSize
                var end = (i + 1) * maxLongSize
                end = if (end > line.length) line.length else end
                debugInfo(resolveTag(tag),DEFAULT_LINE + line.substring(start,end))
            }
        }
    }

    /**
     * 对 `lines` 中的信息进行逐行打印
     *
     * @param withLineSize 为 `true` 时, 每行的信息长度不会超过110, 超过则自动换行
     */
    private fun logLinesSeparator(tag : String,lines : Array<String>,withLineSize : Boolean) {
        for (line in lines) {
            val lineLength = line.length
            //解决AndroidStudio的logcat显示超长字符串的问题==一行打印3900个长度没问题.
            //https://blog.csdn.net/IWantToHitRen/article/details/70649659?utm_source=blogxgwz3
            val maxLongSize = if (withLineSize) 1100 else lineLength
            for (i in 0..lineLength / maxLongSize) {
                val start = i * maxLongSize
                var end = (i + 1) * maxLongSize
                end = if (end > line.length) line.length else end
                debugInfo(resolveTag(tag), DEFAULT_LINE + LINE_SEPARATOR + line.substring(start, end))
            }
        }
    }

    private fun computeKey() : String {
        if (last.get()!! >= 4) {
            last.set(0)
        }
        val s = NAME_SPACE[last.get()!!]
        last.set(last.get()!! + 1)
        return s
    }

    /**
     * 此方法是为了解决在 AndroidStudio v3.1 以上 Logcat 输出的日志无法对齐的问题
     *
     *
     * 此问题引起的原因, 据 JessYan 猜测, 可能是因为 AndroidStudio v3.1 以上将极短时间内以相同 tag 输出多次的 log 自动合并为一次输出
     * 导致本来对称的输出日志, 出现不对称的问题
     * AndroidStudio v3.1 此次对输出日志的优化, 不小心使市面上所有具有日志格式化输出功能的日志框架无法正常工作
     * 现在暂时能想到的解决方案有两个: 1. 改变每行的 tag (每行 tag 都加一个可变化的 token) 2. 延迟每行日志打印的间隔时间
     *
     *
     * [.resolveTag] 使用第一种解决方案
     */
    private fun resolveTag(tag : String) : String {
        return computeKey() + tag
    }

    private fun getRequest(request : Request) : Array<String> {
        val header = request.headers.toString()
        return StringBuilder()
            .append("Method: @").append(request.method).append(DOUBLE_SEPARATOR)
            .append("Headers : ")
            .apply {
                if (isEmpty(header)) {
                    append("暂无").append(LINE_SEPARATOR)
                } else {
                    append(LINE_SEPARATOR).append(dotHeaders(header))
                }
            }.split(LINE_SEPARATOR.toRegex()).toTypedArray()
    }

    private fun getResponse(response : Response,tookMs : String) : Array<String> {
        val segments : List<String> = response.request.url.encodedPathSegments
        val header = response.headers.toString()
        return StringBuilder()
            .append("请求资源地址 : ")
            .append((if (segments.isEmpty()) "" else "${slashSegments(segments)}")).append(
            LINE_SEPARATOR
          )
            .append("是否请求成功 : ").append(if (response.isSuccessful) "成功" else "失败")
            .append(LINE_SEPARATOR)
            .append("耗时 : ").append(tookMs).append("ms").append(LINE_SEPARATOR)
            .append("响应码 : ").append(response.code).append(LINE_SEPARATOR)
            .append("响应消息 : ")
            .apply {
                if (isEmpty(header)) {
                    append("暂无")
                } else {
                    append(response.message).append(DOUBLE_SEPARATOR)
                }
            }
            .append("Headers : ")
            .apply {
                if (isEmpty(header)) {
                    append("暂无").append(LINE_SEPARATOR)
                } else {
                    append(LINE_SEPARATOR).append(dotHeaders(header))
                }
            }.split(LINE_SEPARATOR.toRegex()).toTypedArray()

        //return log.split(LINE_SEPARATOR.toRegex()).toTypedArray()
    }

    private fun slashSegments(segments : List<String>) : String {
        val segmentString = StringBuilder()
        for (segment in segments) {
            segmentString.append("/").append(segment)
        }
        return segmentString.toString()
    }

    /**
     * 对 `header` 按规定的格式进行处理
     */
    private fun dotHeaders(header : String) : String {
        val headers = header.let { it.trim() }.split(LINE_SEPARATOR.toRegex()).toTypedArray()
        val builder = StringBuilder()
        var tag = "─ "
        if (headers.size > 1) {
            for (i in headers.indices) {
                tag = when (i) {
                  0 -> {
                    CORNER_UP
                  }
                  headers.size - 1 -> {
                    CORNER_BOTTOM
                  }
                    else -> {
                        CENTER_LINE
                    }
                }
                builder.append(tag).append(headers[i]).append(LINE_SEPARATOR)
            }
        } else {
            for (item in headers) {
                builder.append(tag).append(item).append(LINE_SEPARATOR)
            }
        }
        return builder.toString()
    }

    private fun getTag(isRequest : Boolean) : String {
        return if (isRequest) "$TAG-Request" else "$TAG-Response"
    }

    private fun debugInfo(tag : String?,msg : String?) {
        if (msg.isNullOrEmpty()) return
        Log.d(tag,msg)
    }
}