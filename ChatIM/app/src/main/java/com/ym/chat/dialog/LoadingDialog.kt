package com.ym.chat.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.ym.base.widget.ext.clearLoad
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.utils.ImageLoaderUtils.imageLoadGif
import java.lang.Exception

class LoadingDialog : DialogControl {
    private var loadingDialog: Dialog? = null
    private var anim: Animation? = null
    private var ivLoading: LottieAnimationView? = null

    constructor(activity: Activity?, strHint: String?) {
        if (loadingDialog == null && activity != null) {
            loadingDialog = createLoadingDialog(activity, strHint)
        }
    }

    constructor(activity: Activity?, strHint: String?, cancelTouch: Boolean) {
        if (loadingDialog == null && activity != null) {
            loadingDialog = createLoadingDialog(activity, strHint)
        }
        val dialog = loadingDialog
        if (dialog != null) {
            dialog.setCancelable(cancelTouch)
            loadingDialog!!.setCanceledOnTouchOutside(cancelTouch)
        }
    }

    @SuppressLint("ResourceType")
    private fun createLoadingDialog(context: Context, strHint: String?): Dialog {
        val inflate = LayoutInflater.from(context).inflate(R.layout.loading_dialog2, null)
        ivLoading = inflate.findViewById(R.id.loading)
        val tvTip = inflate.findViewById<TextView>(R.id.tvTip)
        if (!strHint.isNullOrBlank()) {
            tvTip.visible()
            tvTip.text = strHint
        }
//        anim = AnimationUtils.loadAnimation(context, R.drawable.animation_rotate)
//        anim?.fillAfter = true //设置旋转后停止
//        ivLoading?.startAnimation(anim)

//        ivLoading.load(R.drawable.new_loding)
//        imageLoadGif(ivLoading as ImageView, R.drawable.new_loding)
        val transparentDialog = TransparentDialog(context, R.style.loading_dialog)
        transparentDialog.setCancelable(true)
        transparentDialog.setContentView(
            inflate.findViewById(R.id.dialog_view2),
            LinearLayout.LayoutParams(-1, -1)
        )
        transparentDialog.setCanceledOnTouchOutside(false)
        return transparentDialog
    }

    override fun dismiss() {
        try {
            val dialog = loadingDialog
            if (dialog != null && dialog.isShowing) {
                ivLoading?.clearAnimation()
                loadingDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun show() {
        try {
            if (loadingDialog != null) {
                if (loadingDialog!!.isShowing) {
                    loadingDialog!!.dismiss()
                }
//                ivLoading?.startAnimation(anim)
                ivLoading?.playAnimation()
                loadingDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDialogCancelable(z: Boolean, z2: Boolean) {
        val dialog = loadingDialog
        if (dialog != null) {
            dialog.setCancelable(z)
            loadingDialog!!.setCanceledOnTouchOutside(z2)
        }
    }
}