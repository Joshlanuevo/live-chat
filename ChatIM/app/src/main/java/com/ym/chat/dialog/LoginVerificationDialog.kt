package com.ym.chat.dialog

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.dylanc.viewbinding.binding
import com.ym.base.widget.ext.click
import com.ym.chat.databinding.DialogHintVerificationBinding
import com.ym.chat.widget.view.SendClickListener

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/21 13:53
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 清理缓存弹窗
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class LoginVerificationDialog(
    var title: String,
    var content: String,
    var isCanTouchOutsideSet: Boolean = false,
    private val onSendClickListener: (() -> Unit)? = null,
    private val onConfirmClickListener: ((code: String) -> Unit)? = null
) :
    BaseBindFragmentDialog<DialogHintVerificationBinding>() {

    override fun loadViewBinding(
        inflater: LayoutInflater,
        parent: FrameLayout
    ): DialogHintVerificationBinding = DialogHintVerificationBinding.inflate(inflater, parent, true)

    override fun initView() {
        canTouchOutside = isCanTouchOutsideSet
        viewBinding?.run {
            if (!TextUtils.isEmpty(title)) {
                tvTitle.text = title
            }
            if (!TextUtils.isEmpty(title)) {
                tvContent.text = content
            }
            tvCode.setClickListener(object : SendClickListener {
                override fun onSendClick() {
                    onSendClickListener?.invoke()
                }
            })
            btnConfirm.click {
                val code = etCode.text.toString().trim()
                onConfirmClickListener?.invoke(code)
            }
            btnCancel.click { dismiss() }
            startTimer()
        }
    }

    fun startTimer() {
        viewBinding?.tvCode?.startTimer()
    }
}

