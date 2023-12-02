package com.ym.chat.dialog

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.DialogHintSecondBinding
import com.ym.chat.databinding.DialogHintThreeBinding
import com.ym.chat.ext.loadImg

/**
 * 提示dialog
 * 群成员设置
 */
class HintSecondDialog(
    private var iconId: Int= R.drawable.icon_tip_error,
    var title: String,
    var content: String? = null,
    var confirmText: String? = null,
    var isCanTouchOutsideSet: Boolean = false,//是否点击空白 退出dialog
    private val onClickListener: (() -> Unit)? = null,
) :
    BaseBindFragmentDialog<DialogHintSecondBinding>() {

    override fun loadViewBinding(
        inflater: LayoutInflater,
        parent: FrameLayout
    ): DialogHintSecondBinding = DialogHintSecondBinding.inflate(inflater, parent, true)

    override fun initView() {
        viewBinding?.run {
            if (!TextUtils.isEmpty(title)) {
                tvTitle.text = title
            }
            if (!TextUtils.isEmpty(content)) {
                tvContent.text = content
            }
            if (!TextUtils.isEmpty(confirmText)) {
                tvConfirm.text = confirmText
            }
            ivTop.setImageResource(iconId)
//            tvConfirm.click {
//
//            }
        }
        canTouchOutside = isCanTouchOutsideSet

        viewBinding?.tvConfirm?.postDelayed(Runnable {
            onClickListener?.invoke()
            dismiss()
        },3000)
    }
}
