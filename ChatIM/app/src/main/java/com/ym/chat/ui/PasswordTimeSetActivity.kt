package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.ActivityPwdTimesetBinding
import com.ym.chat.databinding.ActivitySafetySetBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 密码时效性
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class PasswordTimeSetActivity : BaseActivity() {
    private val bindView: ActivityPwdTimesetBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.mimabaocunshixiao)
        }
        bindView.llNo.click {
            //不保存
            bindView.ivNo.setImageResource(R.drawable.cb_check)
            bindView.ivSeven.setImageResource(R.drawable.cb_uncheck)
            bindView.ivThirty.setImageResource(R.drawable.cb_uncheck)
            bindView.ivLongTime.setImageResource(R.drawable.cb_uncheck)
        }
        bindView.llSeven.click {
            //7天
            bindView.ivNo.setImageResource(R.drawable.cb_uncheck)
            bindView.ivSeven.setImageResource(R.drawable.cb_check)
            bindView.ivThirty.setImageResource(R.drawable.cb_uncheck)
            bindView.ivLongTime.setImageResource(R.drawable.cb_uncheck)
        }
        bindView.llThirty.click {
            //30天
            bindView.ivNo.setImageResource(R.drawable.cb_uncheck)
            bindView.ivSeven.setImageResource(R.drawable.cb_uncheck)
            bindView.ivThirty.setImageResource(R.drawable.cb_check)
            bindView.ivLongTime.setImageResource(R.drawable.cb_uncheck)
        }
        bindView.llLongtine.click {
            //永久
            bindView.ivNo.setImageResource(R.drawable.cb_uncheck)
            bindView.ivSeven.setImageResource(R.drawable.cb_uncheck)
            bindView.ivThirty.setImageResource(R.drawable.cb_uncheck)
            bindView.ivLongTime.setImageResource(R.drawable.cb_check)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}