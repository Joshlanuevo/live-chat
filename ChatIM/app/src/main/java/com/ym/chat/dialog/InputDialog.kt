package com.ym.chat.dialog

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.Utils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.chat.R
import com.ym.chat.databinding.DialogInputBinding
import com.ym.chat.utils.NetUtils
import com.ym.chat.utils.PatternUtils


class InputDialog(context: Context) : BaseBindFragmentDialog<DialogInputBinding>() {
    //<editor-fold defaultstate="collapsed" desc="变量">
    var mCall: ((groupName: String) -> Unit)? = null
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="XML">
    override fun loadViewBinding(inflater: LayoutInflater, parent: FrameLayout) =
        DialogInputBinding.inflate(inflater, parent, true)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="初始化">
    override fun initView() {
        viewBinding?.let { vb ->
            vb.tvCancel.pressEffectAlpha()
            vb.tvConfirm.pressEffectAlpha()
            vb.tvCancel.click { dismissAllowingStateLoss() }
            vb.etName.filters = arrayOf(InputFilter.LengthFilter(50), PatternUtils.zhNumberLetter)
            vb.tvConfirm.click {
                KeyboardUtils.hideSoftInput(vb.etName)
                if (!NetUtils.checkNetToast()) return@click
                val name = vb.etName.text.toString().trim()
                if (name.isBlank()) {
                    Toast.makeText(context, ActivityUtils.getTopActivity().getString(R.string.qunzumingchengbunengweikong), Toast.LENGTH_SHORT).show()
                    return@click
                }
//                else if (name.length < 3) {
//                    Toast.makeText(context, "群组名称长度不能低于3位", Toast.LENGTH_SHORT).show()
//                    return@click
//                } else if (!PatternUtils.isGroupNameMatcher(name)) {
//                    Toast.makeText(context, "群组名称格式错误", Toast.LENGTH_SHORT).show()
//                    return@click
//                }
                mCall?.invoke(name)
            }
        }
    }
    //</editor-fold>
}

//DSL style
inline fun inputDialog(context: Context,fragmentManager: FragmentManager, dsl: InputDialog.() -> Unit) {
    val dialog = InputDialog(context)
    dialog.canTouchOutside = false
    dialog.mWidth = (ScreenUtils.getScreenWidth() * 340f / 375).toInt() + 3//3为阴影+误差
    dialog.apply(dsl).show(fragmentManager, InputDialog::class.java.name)
}