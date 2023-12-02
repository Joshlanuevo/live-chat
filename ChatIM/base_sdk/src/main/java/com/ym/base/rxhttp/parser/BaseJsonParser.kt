package com.ym.base.rxhttp.parser

import com.blankj.utilcode.util.GsonUtils
import com.google.gson.JsonSyntaxException
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.ConstBeanCode
import com.ym.base.constant.EventKeys
import com.ym.base.rxhttp.BaseBean
import com.ym.base.rxhttp.utils.ParserUtils
import com.ym.base.util.save.MMKVUtils
import okhttp3.Response
import org.json.JSONObject
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import java.lang.reflect.Type

/**
 * Author:yangcheng
 * Date:2020-10-7
 * Time:10:07
 */
@Parser(name = "BaseJson")
open class BaseJsonParser : AbstractParser<BaseBean<*>> {
    /**
     * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，如：List<Student>
     *
     * 用法:
     * Java: .asParser(new ResponseParser<List<Student>>(){})
     * Kotlin: .asParser(object : ResponseParser<List<Student>>() {})
     *
     */
    constructor() : super()

    /**
     * 此构造方法仅适用于不带泛型的Class对象，如: Student.class
     *
     * 用法
     * Java: .asParser(new ResponseParser<>(Student.class))   或者  .asResponse(Student.class)
     * Kotlin: .asParser(ResponseParser(Student::class.java)) 或者  .asResponse<Student>()
     */
    constructor(type: Type) : super(type)

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    override fun onParse(response: Response): BaseBean<*> {
        if (!response.isSuccessful) {
            ApiErrorForat.handleResponseHttpError(response)
        }
        if (response.request.url.toString()
                .contains("memberInfo/login") || response.request.url.toString()
                .contains("memberInfo/register")
        ) {
            //登录获取token
            val token = response.header("Authorization", "")
            if (token != null) MMKVUtils.saveToken(token)
        }
        val result: String? = ParserUtils.parseResult(response)

        try {
            result?.let { resStr ->
                var resStrObject = JSONObject(resStr)
                var dataValue = resStrObject.opt("data")
                if (dataValue is String && dataValue.equals("token")) {
                    //登录状态失效
                    LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                }
//                else if (dataValue is Int && dataValue == 403) {
//                    //403，登录受限
//                    LiveEventBus.get(EventKeys.LoginRestricted, Boolean::class.java).post(true)
//                }
            }
        }catch (e:java.lang.Exception){

        }
        //转换类型(转换为BaseResponse判断是否成功)
        return try {
            GsonUtils.fromJson<BaseBean<*>>(result, mType)
        } catch (e: Exception) {
            //Json解析过程中发生的错误都这么处理
            val errorString = StringBuilder("BaseJsonParser解析 Url: ").append("\n")
                .append(response.request.url.toString()).append("\n")
                .append("转化为BaseResponse失败").append("\n")
                .append(e.message).toString()
            throw JsonSyntaxException(errorString, e).apply { this.printStackTrace() }
        }.apply {
            when {
                code == null -> {
                    //Json解析过程中发生的错误都这么处理
                    val errorString = StringBuilder("BaseJsonParser解析 Url: ").append("\n")
                        .append(response.request.url.toString()).append("\n")
                        .append("转化为BaseResponse成功").append("\n")
                        .append("但是code未解析到或者解析为空字符串,请检查当前请求结果是否为BaseResponse.class结构").append("\n")
                        .toString()
                    throw ParseException("", errorString, response).also { it.printStackTrace() }
                }
                code != ConstBeanCode.SUCESS -> {
                    ApiErrorForat.handleResponseBeanError(code, message)
                    throw ParseException(
                        code.toString(),
                        message,
                        response
                    ).also { it.printStackTrace() }
                }
            }
        }
    }
}