package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.ActivityDeleteAccountBinding
import com.ym.chat.databinding.ActivityModifyInfoBinding
import com.ym.chat.databinding.ActivityPersonalBinding
import com.ym.chat.databinding.ActivityShareCodeBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 注销账号
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class DeleteAccountActivity : BaseActivity() {
    private val bindView: ActivityDeleteAccountBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.zhuxiaozhanghao)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}