package com.ym.base.rxhttp.parser

import android.text.TextUtils
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.JsonSyntaxException
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.rxhttp.ErrorMsgResponse
import com.ym.base.rxhttp.utils.ParserUtils
import com.ym.base.util.save.MMKVUtils
import okhttp3.Response
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.parse.AbstractParser
import java.lang.reflect.Type

/**
 * Author:yangcheng
 * Date:2020-10-7
 * Time:10:07
 */
@Parser(name = "OtherJson")
open class OtherJsonParser<T> : AbstractParser<T> {
    /**
     * 此构造方法适用于任意Class对象，但更多用于带泛型的Class对象，如：List<Student>
     *
     * 用法:
     * Java: .asParser(new ResponseParser<List<Student>>(){})
     * Kotlin: .asParser(object : ResponseParser<List<Student>>() {})
     *
     * 注：此构造方法一定要用protected关键字修饰，否则调用此构造方法将拿不到泛型类型
     */
    protected constructor() : super()

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
    override fun onParse(response: Response): T {

        if (!response.isSuccessful) {
            if (response.code == 401) {
                //登陆token失效
                MMKVUtils.clearUserInfo()
                LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
            }
            val s = "--后端接口异常code=${response.code}---url=${response.request.url}"
            s.logE()
//            ApiErrorForat.handleResponseHttpError(response)
        }

        //登录获取token
        if (response.request.url.toString()
                .contains("memberInfo/login") || response.request.url.toString()
                .contains("memberInfo/register")
        ) {
            //登录获取token
            val token = response.header("Authorization", "")
            if(!TextUtils.isEmpty(token)){
                MMKVUtils.saveToken(token)
            }
        }

        //200响应成功
        val result: String? = ParserUtils.parseResult(response)
        return try {
            GsonUtils.fromJson(result, mType)
        } catch (e: Exception) {
            try {
                var errorBean = GsonUtils.fromJson(result, ErrorMsgResponse::class.java)
                if (!errorBean.status) {
                    if (errorBean.data == "token") {
                        //token失效 清除用户信息 跳转到登录界面
                        MMKVUtils.clearUserInfo()
                        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java)
                            .post(false)
//                            ARouter.getInstance().build(ArouterConstnts.LOGIN_ACT)
//                                .navigation()
                        "登录状态失效".toast()
                    } else {
                        errorBean.data.toast()
                    }
                }
            } catch (e: Exception) {
                //Json解析过程中发生的错误都这么处理
                val errorString = StringBuilder("OtherJsonParser解析 Url: ").append("\n")
                    .append(response.request.url.toString()).append("\n")
                    .append("转化为 Json 失败").append("\n")
                    .append(e.message).toString()
                throw JsonSyntaxException(errorString, e).apply { this.printStackTrace() }
            }
            throw JsonSyntaxException("第一次解析Json错误", e).apply { this.printStackTrace() }
        }
    }
}