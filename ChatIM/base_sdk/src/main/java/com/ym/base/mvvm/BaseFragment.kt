package com.ym.base.mvvm

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.ArouterConstnts
import com.ym.base.constant.EventKeys
import com.ym.base.util.save.MMKVUtils

/**
 *
 *
 * 主要封装实现功能
 *  1、异步视图加载
 *  2、配合BaseActivityView完成单Activity多Fragment的堆栈管理
 *  3、封装界面切入与退出动画
 */
open abstract class BaseFragment(@LayoutRes contentLayoutId: Int) :
    Fragment(contentLayoutId) {

    companion object{
        var isLogin: Boolean =  !MMKVUtils.getUserId().isNullOrBlank()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        //监听登录退出
        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).observe(this) {
            isLogin = it
        }

        initView()

        requestData()

        observeCallBack()
    }

    /**
     *
     * 用来未登录 点击事件 判断
     * 返回true 跳转到Login  返回false 继续
     * **/
    fun orGoLogin():Boolean{
        return if (!isLogin){
            LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
            true
        }else{
            false
        }
    }
    abstract fun initView()

    abstract fun requestData()

    abstract fun observeCallBack()

    //部分Activity要刷新子页面时调用(重写后需要返回true)
    open fun refreshApi() = false
}