package com.ym.chat.ui

import android.content.Intent
import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.ActivitySafetySetBinding
import com.ym.chat.dialog.showAppLockDialog

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 安全设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class SafetySetActivity : BaseActivity() {
    private val bindView: ActivitySafetySetBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.anquanshezhi)
        }
        bindView.llPwdTime.click {
            //密码时效性
            startActivity(Intent(this, PasswordTimeSetActivity::class.java))
        }
        bindView.llModifyPwd.click {
            //修改密码
            startActivity(Intent(this, ModifyPwdActivity::class.java))
        }
        bindView.llAppLock.click {
            //应用锁
            showAppLockDialog(supportFragmentManager) {}
        }
        bindView.llDeletAccount.click {
            //注销账号
            startActivity(Intent(this, DeleteAccountActivity::class.java))
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}