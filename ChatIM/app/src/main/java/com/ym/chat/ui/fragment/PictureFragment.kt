package com.ym.chat.ui.fragment

import com.dylanc.viewbinding.binding
import com.luck.picture.lib.dialog.PictureLoadingDialog
import com.luck.picture.lib.listener.OnImageCompleteCallback
import com.ym.base.image.ImageEngine
import com.ym.chat.R
import com.ym.chat.databinding.FragmentPictureBinding

/**
 * 多张 图片加载
 */
class PictureFragment(var strUrl:String) : LoadingFragment(R.layout.fragment_picture) {
    private val bindView: FragmentPictureBinding by binding()
    override fun initView() {
        bindView
    }

    override fun requestData() {
        context?.let {
            ImageEngine().loadImage(
                it,
                strUrl,
                bindView.previewImage,
                bindView.longImg,
                object : OnImageCompleteCallback {
                    override fun onShowLoading() {
                        showPleaseDialog()
                    }

                    override fun onHideLoading() {
                        dismissDialog()
                    }
                })
        }
    }

    override fun observeCallBack() {
    }

    var mLoadingDialog: PictureLoadingDialog? = null
    fun showPleaseDialog() {
        try {
            if (activity?.isFinishing == true) {
                if (this.mLoadingDialog == null) {
                    this.mLoadingDialog = PictureLoadingDialog(context)
                }
                if (this.mLoadingDialog!!.isShowing) {
                    this.mLoadingDialog!!.dismiss()
                }
                this.mLoadingDialog!!.show()
            }
        } catch (var2: java.lang.Exception) {
            var2.printStackTrace()
        }
    }

    fun dismissDialog() {
        if (activity?.isFinishing == true) {
            try {
                if (mLoadingDialog != null && mLoadingDialog!!.isShowing) {
                    mLoadingDialog!!.dismiss()
                }
            } catch (var2: java.lang.Exception) {
                mLoadingDialog = null
                var2.printStackTrace()
            }
        }
    }
}