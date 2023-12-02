package com.ym.base.rxhttp.parser

import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.constant.HostManager
import com.ym.base.constant.HttpErrorBean
import com.ym.base.ext.getHost
import com.ym.base.rxhttp.utils.HttpException
import okhttp3.Response

object ApiErrorForat {
    var realApiErrorImp: ApiErrorImp? = null
    fun handleRequestError(e: Throwable) {
        realApiErrorImp?.handleRequestError(e)
    }

    @Throws(HttpException::class)
    fun handleResponseHttpError(response: Response) {
        //请求错误
        val httpError = HttpErrorBean(response.request.url.toString(), response.code)
        LiveEventBus.get(EventKeys.HttpErrorEvent, HttpErrorBean::class.java).post(httpError)
        realApiErrorImp?.handleResponseHttpError(response)
    }

    fun handleResponseBeanError(errCode: Int?, errMsg: String?) {
        realApiErrorImp?.handleResponseBeanError(errCode, errMsg)
    }

    fun handleErrorUI(e: Throwable) {
        realApiErrorImp?.handleErrorUI(e)
    }
}

interface ApiErrorImp {
    /**
     * 处理网络请求发起到服务器未正式响应之间发生的错误,
     * 如:等待服务器响应的过程中服务器单方面中断流 会报 EOFException
     */
    fun handleRequestError(e: Throwable)

    /**处理网络服务器响应后服务器校验失败,未返回正确json的情况*/
    @Throws(HttpException::class)
    fun handleResponseHttpError(response: Response)

    /**处理网络服务器响应后服务器校验通过,返回正确json的情况,但是json中业务失败的情况狂*/
    fun handleResponseBeanError(errCode: Int?, errMsg: String?)

    /**处理网络整个请求完成后最后一步,是否把异常显示给用户看的方法*/
    fun handleErrorUI(e: Throwable?)
}