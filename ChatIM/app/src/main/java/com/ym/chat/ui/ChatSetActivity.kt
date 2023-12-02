package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.*

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 聊天设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ChatSetActivity : BaseActivity() {
    private val bindView: ActivityChatSetBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.聊天设置)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}