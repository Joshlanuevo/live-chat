package com.ym.chat.dialog

import android.app.Activity
import com.ym.chat.dialog.DialogControl
import com.ym.chat.dialog.LoadingDialog

object DialogFactory {
    fun createLoadingDialog(activity: Activity?, strHint: String?): DialogControl {
        return LoadingDialog(activity, strHint)
    }

    fun createLoadingDialog(
        activity: Activity?,
        strHint: String?,
        cancelTouch: Boolean
    ): DialogControl {
        return LoadingDialog(activity, strHint, cancelTouch)
    }
}