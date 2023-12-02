package com.ym.chat.ui.fragment

import androidx.annotation.LayoutRes
import com.ym.base.ext.*
import com.ym.base.mvvm.BaseFragment
import com.ym.chat.dialog.LoadingDialog
import com.ym.chat.dialog.DialogControl

/**
 *
 *
 * 主要封装实现功能
 *  1、异步视图加载
 *  2、配合BaseActivityView完成单Activity多Fragment的堆栈管理
 *  3、封装界面切入与退出动画
 */
open abstract class LoadingFragment(@LayoutRes contentLayoutId: Int) :
    BaseFragment(contentLayoutId) {
    protected var loadingDialog: DialogControl? = null

    private var isLoaded = false

    override fun onResume() {
        super.onResume()
        //增加了Fragment是否可见的判断
        if (!isLoaded && !isHidden) {
            lazyInit()
            isLoaded = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
    }

    open fun lazyInit() {}

    //<editor-fold defaultstate="collapsed" desc="简单的加载中">
    fun showLoading(cancelTouch: Boolean, strHint: String = "") {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(activity, strHint, cancelTouch)
        }
        loadingDialog!!.show()
    }

    fun showLoading(strHint: String = "") {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(activity, strHint)
        }
        loadingDialog!!.show()
    }

    fun hideLoading() {
        loadingDialog?.dismiss()
    }

    fun hideLoadingDelay(delay: Long) {
        requireActivity().mContentView.postDelayed({ hideLoading() }, delay)
    }
    //</editor-fold>
}