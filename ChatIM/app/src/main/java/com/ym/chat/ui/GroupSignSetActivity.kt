package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.ActivityGroupSignSetBinding
import com.ym.chat.databinding.ActivityUserChatBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/22 13:39
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 群签到设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class GroupSignSetActivity : BaseActivity() {
    private val bindView: ActivityGroupSignSetBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.群签到设置)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}