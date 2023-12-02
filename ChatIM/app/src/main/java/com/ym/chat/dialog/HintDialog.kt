package com.ym.chat.dialog

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.load
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.DialogClearCacheBinding
import com.ym.chat.ext.roundLoad

/**
 * 提示dialog
 */
class HintDialog(
    var title: String,
    var content: String,
    var callback: ConfirmDialogCallback,
    var iconId: Int = R.drawable.ic_dialog_top,//默认本地头像
    var headUrl: String? = "",//头像
    var isShowBtnCancel: Boolean = true,//是否显示取消按钮
    var isCanTouchOutsideSet: Boolean = true,//是否点击空白 退出dialog
    var isShowHeader: Boolean = false,//是否显示标题头像
    var isTitleTxt: Boolean = false,//是否标题按钮显示确定文字
    var isSettingTitleColor: Boolean = false,//是否设置标题颜色
    var cancel: CancelDialogCallback? = null,
) :
    BaseBindFragmentDialog<DialogClearCacheBinding>() {

    override fun loadViewBinding(
        inflater: LayoutInflater,
        parent: FrameLayout
    ): DialogClearCacheBinding = DialogClearCacheBinding.inflate(inflater, parent, true)

    override fun initView() {
        viewBinding?.run {
            if (isShowBtnCancel) btnCancel.visible()
            if (!TextUtils.isEmpty(title)) {
                tvTitle.visible()
                tvTitle.text = title
                if(isSettingTitleColor){
                    context?.getColor(R.color.text_red)?.let { tvTitle.setTextColor(it) }
                }
            }
            if (!TextUtils.isEmpty(content)) {
                tvContent.text = content
            }
            if (isShowHeader) {
                cvHeader.visible()
                if (headUrl?.isNotBlank() == true) ivTop.load(headUrl) else ivTop.load(
                    iconId
                )
            }
            if (isTitleTxt) {
                btnConfirm.text = title
            }
            btnConfirm.click {
                callback.onItemClick()
                dismiss()
            }
            btnCancel.click {
                if(cancel!=null){
                    cancel?.onItemClick();
                }
                dismiss()
            }
        }
        canTouchOutside = isCanTouchOutsideSet
    }
}
