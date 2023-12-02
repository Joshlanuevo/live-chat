package com.ym.chat.ui

import com.ym.base.ext.mContentView
import com.ym.base.mvvm.BaseActivity
import com.ym.chat.R
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.DialogControl
import com.ym.chat.dialog.DialogFactory
import com.ym.chat.dialog.HintDialog

/**
 *
 *
 * 主要封装实现功能
 *  1、异步视图加载
 */
open abstract class LoadingActivity : BaseActivity() {

    var loadingDialog: DialogControl? = null
    var hintDialog: HintDialog? = null

    fun showLoading(cancelTouch: Boolean, strHint: String = "") {
        if (loadingDialog == null) {
            loadingDialog = DialogFactory.createLoadingDialog(this, strHint, cancelTouch)
        }
        loadingDialog?.show()
    }

    fun showLoading(strHint: String = "") {
        if (loadingDialog == null) {
            loadingDialog = DialogFactory.createLoadingDialog(this, strHint)
        }
        loadingDialog?.show()
    }

    fun hideLoading() {
        try {
            loadingDialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideLoadingDelay(delay: Long) {
        mContentView.postDelayed({ hideLoading() }, delay)
    }

    override fun finish() {
        hideLoading()
        super.finish()
    }

    override fun onDestroy() {
        hideLoading()
        hideHintLoading()
        super.onDestroy()
    }


    fun showHintDialog(strName: String) {
        if (hintDialog == null)
            hintDialog = HintDialog(
                getString(R.string.zhuyi),
                String.format(getString(R.string.移出群提示),strName),
                isShowBtnCancel = false,
                isCanTouchOutsideSet = false,
                iconId = R.drawable.ic_hint_delete,
                callback = object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        finish()
                    }
                }
            )
        hintDialog?.show(supportFragmentManager, "HintDialog")
    }

    private fun hideHintLoading() {
        try {
            hintDialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}