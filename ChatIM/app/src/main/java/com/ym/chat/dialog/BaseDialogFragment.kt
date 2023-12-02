package com.ym.chat.dialog

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ActivityUtils
import com.ym.base.ext.logE
import com.ym.chat.ui.HomeActivity

/**
 * @Description 防止强制更新还弹出了其他窗口
 * @Author：CASE
 * @Date：2021-05-05
 * @Time：12:56
 */
open class BaseDialogFragment : DialogFragment() {
    //是否忽略强制更新弹窗
    var ignoreForceUpdate = false
    override fun show(manager: FragmentManager, tag: String?) {
        if (ignoreForceUpdate) {
            super.show(manager, tag)
        } else {
            var hasForceUpdate = false
            //找到主页
            ActivityUtils.getActivityList()?.filterIsInstance<HomeActivity>()?.lastOrNull()?.let { ac ->
                //找到更新弹窗
//                ac.supportFragmentManager.fragments.filterIsInstance<UpdateDialog>().lastOrNull()?.let { d ->
//                    hasForceUpdate = d.isForce
//                }
            }
            if (!hasForceUpdate) {
                try {//防止多次调用出现已添加的情况
                    super.show(manager, tag)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else "Has force update dialog，${this.javaClass.simpleName} is not show!!!".logE()
        }
    }
}