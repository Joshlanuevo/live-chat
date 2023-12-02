package com.ym.chat.dialog

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.databinding.DialogHintThreeBinding
import com.ym.chat.ext.loadImg
import com.ym.chat.ext.roundLoad

/**
 * 提示dialog
 * 群成员设置
 */
class HintThreeDialog(
    var btnTitle: String? = null,
    var btnOneTitle: String? = null,
    var btnTwoTitle: String? = null,
    var isShowFirstBtn: Boolean = true,
    private val onClickListenerBtn: (() -> Unit)? = null,
    private val onClickListenerBtn1: (() -> Unit)? = null,
    private val onClickListenerBtn2: (() -> Unit)? = null,
) :
    BaseBindFragmentDialog<DialogHintThreeBinding>() {

    override fun loadViewBinding(
        inflater: LayoutInflater,
        parent: FrameLayout
    ): DialogHintThreeBinding = DialogHintThreeBinding.inflate(inflater, parent, true)

    override fun initView() {
        viewBinding?.run {

            if (!TextUtils.isEmpty(btnTitle)) {
                tvTitle2.text = btnTitle
            }
            if (!TextUtils.isEmpty(btnOneTitle)) {
                tvTitle1.text = btnOneTitle
            }
            if (!TextUtils.isEmpty(btnTwoTitle)) {
                tvTitle3.text = btnTwoTitle
            }
            if (isShowFirstBtn) llAdmin.visible()
            llMute.click {
                onClickListenerBtn?.invoke()
                dismiss()
            }
            llAdmin.click {
                onClickListenerBtn1?.invoke()
                dismiss()
            }
            llRemove.click {
                onClickListenerBtn2?.invoke()
                dismiss()
            }
        }
    }
}
