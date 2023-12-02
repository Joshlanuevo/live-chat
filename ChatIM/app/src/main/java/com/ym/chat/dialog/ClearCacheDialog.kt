package com.ym.chat.dialog

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ym.base.widget.ext.click
import com.ym.chat.databinding.DialogClearCacheBinding

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
class ClearCacheDialog(
    var title: String,
    var content: String,
    var callback: ConfirmDialogCallback
) :
    BaseBindFragmentDialog<DialogClearCacheBinding>() {

    override fun loadViewBinding(
        inflater: LayoutInflater,
        parent: FrameLayout
    ): DialogClearCacheBinding = DialogClearCacheBinding.inflate(inflater, parent, true)

    override fun initView() {
        viewBinding?.run {
            if (!TextUtils.isEmpty(title)) {
                tvTitle.text = title
            }
            if (!TextUtils.isEmpty(title)) {
                tvContent.text = content
            }
            btnConfirm.click {
                callback.onItemClick()
                dismiss()
            }
            btnCancel.click { dismiss() }
        }
    }
}

interface ConfirmDialogCallback {
    fun onItemClick()
}

interface CancelDialogCallback {
    fun onItemClick()
}