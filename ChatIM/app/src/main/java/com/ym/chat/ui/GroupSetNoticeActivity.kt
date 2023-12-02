package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.ActivityGroupChatsetBinding
import com.ym.chat.databinding.ActivityGroupNoticeBinding
import com.ym.chat.databinding.ActivityUserChatBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/22 13:39
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 群公告，设置群号，设置群昵称
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class GroupSetNoticeActivity : BaseActivity() {
    private val bindView: ActivityGroupNoticeBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.qungonggao)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}