package com.ym.chat.dialog

import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.load
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.DialogClearCacheBinding
import com.ym.chat.databinding.DialogSelectHintBinding
import com.ym.chat.ext.roundLoad

/**
 * 提示dialog
 */
class SelectHintDialog(
    var title: String,
    var content: String,
    var callback: ConfirmSelectDialogCallback,
    var iconId: Int = R.drawable.ic_mine_header,//默认本地头像
    var headUrl: String? = "",//头像
    var isShowBtnCancel: Boolean = true,//是否显示取消按钮
    var isShowLLSelectView: Boolean = false,//是否显示选择框
    var selectContent: String = "",//选择框 文字
    var isCanTouchOutsideSet: Boolean = true,//是否点击空白 退出dialog
    var isShowHeader: Boolean = false,//是否显示标题头像
    var isAdmin:Boolean = true //是否是管理员
) :
    BaseBindFragmentDialog<DialogSelectHintBinding>() {

    override fun loadViewBinding(
        inflater: LayoutInflater,
        parent: FrameLayout
    ): DialogSelectHintBinding = DialogSelectHintBinding.inflate(inflater, parent, true)

    override fun initView() {
        viewBinding?.run {
            if (isShowBtnCancel) btnCancel.visible()
            if (!TextUtils.isEmpty(title)) {
                tvTitle.visible()
                tvTitle.text = title
            }
            if (!TextUtils.isEmpty(content)) {
                tvContent.text = content
            }
            if (isShowLLSelectView) {
                llCb.visible()
                if (selectContent.isNotEmpty()) {
                    tvCb.text = selectContent
                }
            }
            if(isShowHeader){
                ivTop.visible()
                if (headUrl?.isNotBlank() == true) ivTop.roundLoad(headUrl) else ivTop.roundLoad(iconId)
            }
            cb.isEnabled = isAdmin
            btnConfirm.click {
                callback.onItemClick(cb.isChecked)
                dismiss()
            }
            btnCancel.click { dismiss() }
        }
        canTouchOutside = isCanTouchOutsideSet
    }
}

interface ConfirmSelectDialogCallback {
    fun onItemClick(isSelect: Boolean)
}