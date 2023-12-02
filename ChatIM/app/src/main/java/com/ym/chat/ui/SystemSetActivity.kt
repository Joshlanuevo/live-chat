package com.ym.chat.ui

import android.content.Intent
import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.ActivityShareCodeBinding
import com.ym.chat.databinding.ActivitySystemSetBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/21 10:36
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 系统设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class SystemSetActivity : BaseActivity() {
    private val bindView: ActivitySystemSetBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.xitongshezhi)
        }
        bindView.llFontSet.click {
            //字体设置
            startActivity(Intent(this, FontSetActivity::class.java))
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}