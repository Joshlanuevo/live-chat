package com.ym.base.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.rxLifeScope
import com.ym.base.ext.CanUseString
import com.ym.base.rxhttp.parser.ApiErrorForat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Author:yangcheng
 * Date:2020-10-6
 * Time:15:47
 */
open class BaseViewModel : ViewModel() {
    //服务端返回业务处理成功code码
    val SUCCESS = 200
    sealed class LoadState<T>(
        val hasMore: Boolean = false,
        val isRefresh: Boolean = false,
        val isPull2Refresh: Boolean = false,
        var exc: Throwable? = null,
        val data: T? = null,
        val dataOld: T? = null,
    ) {

        @CanUseString(
            "只有list加载'必须',所有参数",
            "情况一.一:刷新第一页,并且是下拉刷新,此时不可关闭下拉刷新,并且无需加入loadingView,展示的是下拉刷新动画",
            "情况1.1:用户确实是手动下拉刷新时.必然给用户看到下拉动画",
            "情况一.二:刷新第一页,并且是下拉刷新,此时不可关闭下拉刷新,并且无需加入loadingView,如果有需要可以也不展示下拉刷新动画",
            "情况1.2:用户并不是手动下拉刷新(但是依然传入true),而是发帖后返回列表,自动静默刷新列表,此时什么动画都不给用户看,",
            "情况二.一:刷新第一页,不是下拉刷新,此时需要关闭下拉刷新,如果刷新前无数据,则需要加入loadingView",
            "情况2.1:用户并不是手动下拉刷新,而是点击重试,,",
            "情况二.二:刷新第一页,不是下拉刷新,此时需要关闭下拉刷新,如果刷新前有,则不需要加入loadingView",
            "情况2.2:用户并不是手动下拉刷新,而是发帖后返回列表,自动静默刷新列表,此时什么动画都不给用户看,",
            "注意情况2.2和1.2,效果类似,但是1.2时因为传入isRefresh为true导致没添加loadingView," +
                    "而2.2是因为列表之前有数据.所以没添加loadingView,两者还区分于是否在执行中关闭了下拉刷新",
        )
        class Loading<T>(isRefresh: Boolean = false,isPull2Refresh: Boolean = false,data: T? = null) :
            LoadState<T>(isRefresh = isRefresh,isPull2Refresh = isPull2Refresh,data = data)

        @CanUseString(
            "只有list加载'必须'传入 isRefresh 是否为下拉刷新,且传入加载前的旧数据",
            "加载失败,无条件打开下拉刷新,同时根据isRefresh判断是否为刷新第一页造成的失败,是则一定关闭加载更多" +
                    "同时关闭所有刷新控件的动画,loadingView随着适配器item更新被移除,而加载失败的view是否" +
                    "添加到item则根据旧list数据是否为空,为空则添加失败view,否则不添加.",
        )
        class Fail<T>(exc: Throwable? = null,isRefresh: Boolean = false,dataOld: T? = null) : LoadState<T>(exc = exc,isRefresh = isRefresh,dataOld = dataOld)

        @CanUseString(
            "只有list加载'必须传入 dataOld 来校验是否为空判断之前是否是 加载失败,加载中",
            "只有list加载 必须传入 isRefresh 是否为下拉刷新 和 ",
            "hasMore 是否还有 上拉加载更多,默认为true,防止漏写了导致因为false无法加载更多"
        )
        class Success<T>(
            data: T? = null,
            dataOld: T? = null,
            isRefresh: Boolean = false,
            isPull2Refresh: Boolean = false,
            hasMore: Boolean = true,
        ) : LoadState<T>(
            data = data,dataOld = dataOld,isRefresh = isRefresh,
            isPull2Refresh = isPull2Refresh,hasMore = hasMore
        )
    }

    fun requestLifeLaunch(
        block: suspend CoroutineScope.() -> Unit,
        onError: ((Throwable) -> Unit) = { e -> ApiErrorForat.handleErrorUI(e) },
        onStart: (() -> Unit)? = null,
        onFinally: (() -> Unit)? = null,
    ) : Job {
        return rxLifeScope.launch(block,onError,onStart,onFinally)
    }

}