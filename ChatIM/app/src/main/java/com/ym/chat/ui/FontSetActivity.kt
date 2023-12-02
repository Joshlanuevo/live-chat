package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.databinding.ActivityFontSetBinding
import com.ym.chat.databinding.ActivityShareCodeBinding
import com.ym.chat.databinding.ActivitySystemSetBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/21 10:36
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 字体大小设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class FontSetActivity : BaseActivity() {
    private val bindView: ActivityFontSetBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = "字体大小"
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}