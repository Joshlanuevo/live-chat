package com.ym.chat.ui

import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginData
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.invisible
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityModifyPhoneBinding
import com.ym.chat.db.AccountDao
import com.ym.chat.dialog.HintSecondDialog
import com.ym.chat.enum.SendCodeType
import com.ym.chat.utils.PatternUtils
import com.ym.chat.utils.ToastUtils
import com.ym.chat.viewmodel.RegisterViewModel
import com.ym.chat.viewmodel.SetViewModel
import com.ym.chat.widget.view.SendClickListener

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 修改密码
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ModifyPhoneActivity : LoadingActivity() {
    private val bindView: ActivityModifyPhoneBinding by binding()
    private val mCodeViewModel: RegisterViewModel = RegisterViewModel()
    private val mViewModel: SetViewModel = SetViewModel()
    private var user: LoginData? = null
    private var phone: String = ""
    private val ERROR_COUNT = 10

    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.修改手机号)
        }

        bindView.pwdState.click {
            //眼睛显示隐藏
            showPwd(bindView.etPwd, bindView.pwdState)
        }

        bindView.tvCountryCode.click {
            ToastUtils.showToastWithImg(
                this@ModifyPhoneActivity,
                getString(R.string.jinzhichizhongguoshoujihao),
                R.drawable.dialog_close
            )
        }


        bindView.tvCode.setClickListener(object : SendClickListener {
            override fun onSendClick() {
                val phone = bindView.etPhone.text.toString().trim()
                if (TextUtils.isEmpty(phone)) {
                    getString(R.string.请输入您的手机号).toast()
                    return
                }
                if (!PatternUtils.isPhoneNumber(phone)) {
                    getString(R.string.shoujihaocuowu).toast()
                    return
                }
                mCodeViewModel.getCode(phone, SendCodeType.EditMobile)
            }
        })

        bindView.btnCommit.click {
            val pwd = bindView.etPwd.text.toString().trim()
            phone = bindView.etPhone.text.toString().trim()
            val code = bindView.etCode.text.toString().trim()
            if (TextUtils.isEmpty(pwd)) {
                getString(R.string.qingshurunindemima).toast()
                return@click
            }
            if (TextUtils.isEmpty(phone)) {
                getString(R.string.请输入您的手机号).toast()
                return@click
            }
            if (!PatternUtils.isPhoneNumber(phone)) {
                getString(R.string.shoujihaocuowu).toast()
                return@click
            }
            if (TextUtils.isEmpty(code)) {
                getString(R.string.qingshuruyanzhengma).toast()
                return@click
            }
            mViewModel.modifyPhone(phone, code, pwd)
        }


    }


    private fun showPwd(edit: EditText, iv: ImageView) {
        edit.requestFocus()
        val show = edit.transformationMethod is PasswordTransformationMethod
        edit.transformationMethod =
            if (show) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        iv.setImageResource(if (show) R.drawable.ic_pwd_open else R.drawable.ic_pwd_close)
        edit.setSelection(edit.text.length)
    }

    override fun requestData() {
        user = MMKVUtils.getUser()
        showPhoneView()
    }


    fun showPhoneView() {
        if (user?.mobile.isNullOrBlank()) {
            bindView.tvPhoneBind.invisible()
        } else {
            bindView.tvPhone.text = user?.mobile
            bindView.tvPhoneBind.visible()
        }
    }

    override fun observeCallBack() {
        //获取验证码
        mCodeViewModel.getCodeLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.验证码已发送).toast()
                    bindView.tvCode.startTimer()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }

        mViewModel.modifyPhoneLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    ToastUtils.showToastWithImg(
                        this@ModifyPhoneActivity,
                        getString(R.string.手机号已绑定),
                        R.drawable.ic_dialog_success
                    )
                    val phoneStr = phone.substring(0, 3) + "*****" + phone.substring(
                        phone.length - 3,
                        phone.length
                    )
                    MMKVUtils.getUser()?.mobile = phoneStr
//                    LiveEventBus.get(EventKeys.EDIT_USER_NAME, String::class.java).post(name)
//                    MMKVUtils.getUser()?.let {
//                        AccountDao.saveAccount(
//                            it,
//                            mobile = phone
//                        )
//                    } //把修改的信息保存到本地账号
//                    finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (result.dataOld != null) {
                        if (result.dataOld?.code == 10033) {
                            showLoginOutDialog()
                        } else if (result.dataOld?.code == 10034) {
                            String.format(getString(R.string.您输入的密码错误),"${result.dataOld?.data}").toast()
                        } else {
                            if (!TextUtils.isEmpty(result.dataOld?.info)) {
                                result.dataOld?.info.toast()
                            } else {
                                getString(R.string.手机号修改失败).toast()
                            }
                        }

                    } else {
                        getString(R.string.手机号修改失败).toast()
                    }
                }
            }

        }

        mViewModel.loginOutLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //退出登录
                    MMKVUtils.clearUserInfo()
                    LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    MMKVUtils.clearUserInfo()
                    LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                }
            }
        }
    }

    private fun showLoginOutDialog() {
        HintSecondDialog(
            title = getString(R.string.通知),
            content =getString(R.string.请于1小时后再次登录操作),
            confirmText = getString(R.string.benxitongzidongdengchu),
            onClickListener = {
                logOut()
            },
        ).show(supportFragmentManager, "HintSecondDialog")
    }

    private fun logOut() {
        mViewModel.loginOut()
    }
}