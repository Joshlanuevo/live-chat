package com.ym.chat.dialog

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.ym.chat.databinding.DialogAppLockBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/20 17:27
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class AppLockDialog : BaseBindFragmentDialog<DialogAppLockBinding>() {
    override fun loadViewBinding(
        inflater: LayoutInflater,
        parent: FrameLayout
    ): DialogAppLockBinding = DialogAppLockBinding.inflate(inflater, parent, true)

    override fun initView() {
    }
}

//防止重复弹窗
var mLastBetOrderDialog = 0L

//DSL style
inline fun showAppLockDialog(
    fragmentManager: FragmentManager,
    dsl: AppLockDialog.() -> Unit
) {
    if (System.currentTimeMillis() - mLastBetOrderDialog < 600) return
    mLastBetOrderDialog = System.currentTimeMillis()
    val dialog = AppLockDialog()
    dialog.apply(dsl).show(fragmentManager, AppLockDialog::class.java.name)
}