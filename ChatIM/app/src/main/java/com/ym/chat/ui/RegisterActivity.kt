package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.BuildConfig
import com.ym.chat.R
import com.ym.chat.databinding.ActivityRegisterBinding
import com.ym.chat.ui.fragment.RegisterStep1Fragment

/**
 * 注册页面
 */
class RegisterActivity : BaseActivity() {

    private val bindView: ActivityRegisterBinding by binding()

    private lateinit var fraStep1: RegisterStep1Fragment

    override fun initView() {
        bindView.tvInfo.text = "${getString(R.string.app_name)}" + " " + "${getString(R.string.让通信如此简单)}"
        bindView.tvAppName.text = getString(R.string.app_name)
        bindView.tvBack.click {
            finish()
        }

        fraStep1 = RegisterStep1Fragment()
        var fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fraRegContent, fraStep1).commitAllowingStateLoss()
    }

    //设置注册方式标题
    fun setRegisterTitle(title: String) {
        bindView.tvMobileLogin.text = title
    }

    override fun requestData() {
    }

    override fun observeCallBack() {

    }
}