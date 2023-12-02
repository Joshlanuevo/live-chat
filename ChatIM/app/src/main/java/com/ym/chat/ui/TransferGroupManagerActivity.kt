package com.ym.chat.ui

import android.content.Intent
import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityGroupSignBinding
import com.ym.chat.databinding.ActivityGroupSignSetBinding
import com.ym.chat.databinding.ActivityGroupTransferManagerBinding
import com.ym.chat.databinding.ActivityUserChatBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/22 13:39
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 转让群主
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class TransferGroupManagerActivity : BaseActivity() {
    private val bindView: ActivityGroupTransferManagerBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.转让群主)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}