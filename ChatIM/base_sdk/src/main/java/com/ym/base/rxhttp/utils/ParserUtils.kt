package com.ym.base.rxhttp.utils

import okhttp3.*
import okio.Buffer
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*

/**
 *
 * author     : yangcheng
 * since       : 2020/11/14 14:27
 * 网络请求发起的地方 okhttp3/internal/connection/RealCall.kt:201
 */
object ParserUtils {

    @Throws(ParseException::class)
    fun parseResult(response : Response) : String? {
        val result : String? = parseContent(response)
//        if (result.isNullOrEmpty()) {
//            val errorString = StringBuilder("Response解析失败").append("\n")
//                .append("Url: ").append(response.request.url.toString()).append("\n")
//                .append("解析结果为空,可能为响应体编码错误,压缩错误,等").toString()
//            throw ParseException("",errorString,response).apply { }
//        }
        return result
    }

    /**
     * 解析服务器响应的内容
     *
     * @param responseBody [ResponseBody]
     * @param encoding 编码类型
     * @param clone 克隆后的服务器响应内容
     * @return 解析后的响应结果
     */
    @Throws(Exception::class)
    fun parseContent(response : Response) : String? {
        val responseBody = response.newBuilder().build().body
        return responseBody?.contentType()?.takeIf { isParseable(it) }?.run {
            val charset = convertCharset(this.charset() ?: Charsets.UTF_8)

            //val clone = Buffer().apply { it.source().readAll(this) }
            val clone = responseBody.source().apply { request(Long.MAX_VALUE) }.buffer.clone()
            response.headers["Content-Encoding"]?.let { encoding ->
                when {
                    encoding.equals("gzip",ignoreCase = true) -> {
                        //content 使用 gzip 压缩
                        ZipHelper.decompressForGzip(clone.readByteArray()) //解压
                    }
                    encoding.equals("zlib",ignoreCase = true) -> {
                        //content 使用 zlib 压缩
                        ZipHelper.decompressToStringForZlib(clone.readByteArray(),charset) //解压
                    }
                    else -> {
                        clone.readString(charset)
                    }
                }
            } ?: clone.readString(charset)
        }
    }

    /**
     * 解析请求服务器的请求参数
     *
     * @param request [Request]
     * @return 解析后的请求信息
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun parseParams(request : Request) : String? {
        val requestBody = request.newBuilder().build().body
        return requestBody?.contentType()?.takeIf { isParseable(it) }?.run {
            val charset = convertCharset(this.charset() ?: Charsets.UTF_8)

            val requestString = Buffer().apply { requestBody.writeTo(this) }.readString(charset)
            try {
                requestString.takeIf { UrlEncoderUtils.hasUrlEncoded(it) }?.let {
                    //某些手机上会因为系统框架的不同,内置的URLDecoder不同可能会对Charset的UTF-8解析失败,报出IllegalCharsetNameException
                    //所以try catch的时候不仅仅要拦截IOException,而是使用Exception全拦截,长见识了
                    URLDecoder.decode(it,charset.toString())
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            requestString
        }
    }

    /**
     * 是否可以解析
     *
     * @param mediaType [MediaType]
     * @return `true` 为可以解析
     */
    fun isParseable(mediaType : MediaType) : Boolean {
        return (isText(mediaType) || isPlain(mediaType)
                || isJson(mediaType) || isForm(mediaType)
                || isHtml(mediaType) || isXml(mediaType))|| isStream(mediaType)
    }

    fun isText(mediaType : MediaType) : Boolean {
        return "text" == mediaType.type
    }

    fun isPlain(mediaType : MediaType) : Boolean {
        return mediaType.subtype.toLowerCase(Locale.ROOT).contains("plain")
    }

    fun isJson(mediaType : MediaType) : Boolean {
        return mediaType.subtype.toLowerCase(Locale.ROOT).contains("json")
    }

    fun isXml(mediaType : MediaType) : Boolean {
        return mediaType.subtype.toLowerCase(Locale.ROOT).contains("xml")
    }

    fun isHtml(mediaType : MediaType) : Boolean {
        return mediaType.subtype.toLowerCase(Locale.ROOT).contains("html")
    }

    fun isForm(mediaType : MediaType) : Boolean {
        return mediaType.subtype.toLowerCase(Locale.ROOT).contains("x-www-form-urlencoded")
    }

    fun isStream(mediaType : MediaType) : Boolean {
        return "octet-stream" == mediaType.subtype
    }

    /**抄过来的,难道是为了防止有些接口返回的字符类型不和规则*/
    fun convertCharset(charset : Charset?) : Charset {
        return try {
            //在部分手机上Charset.forName会报错  
            //java.nio.charset.IllegalCharsetNameException
            //java.nio.charset.CharsetICU[UTF-8]
            //解析原始报错行  java.nio.charset.Charset.checkCharsetName(Charset.java:201)
            //             java.nio.charset.Charset.forName(Charset.java:295)
            //相关类似源码可以看 https://github.com/cocowobo/android-wear-decompile/blob/eb8ad0d8003c5a3b5623918c79334290f143a2a8/decompiled/java/nio/charset/Charset.java
            charset?.let {
                val string = it.toString()
                val i = string.indexOf("[")
                return if (i == -1) it else Charset.forName(
                  string.substring(
                    i + 1,
                    string.length - 1
                  )
                )
            } ?: Charsets.UTF_8
        } catch (e : Exception) {
            Charsets.UTF_8
        }
    }
}