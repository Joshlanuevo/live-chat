package com.ym.chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.rxLifeScope
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.SizeUtils
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.chat.BuildConfig
import com.ym.chat.bean.AccountBean
import com.ym.chat.databinding.ActivityFindPwdBinding
import com.ym.chat.db.AccountDao
import com.ym.chat.enum.SendCodeType
import com.ym.chat.popup.AccountPopupWindow
import com.ym.chat.utils.MD5
import com.ym.chat.utils.PatternUtils
import com.ym.chat.R
import com.ym.chat.viewmodel.LoginViewModel
import com.ym.chat.viewmodel.RegisterViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * 找回密码
 */
class FindPwdActivity : LoadingActivity() {

    private val bindView: ActivityFindPwdBinding by binding()
    private val mViewModel: LoginViewModel = LoginViewModel()
    private val mRegisterViewModel: RegisterViewModel = RegisterViewModel()
    private var mJob: Job? = null
    private var mobile = ""
    private var pwd = ""

    override fun initView() {
        bindView.tvAppName.text = getString(R.string.app_name)
        bindView.tvInfo.text = getString(R.string.app_name)+getString(R.string.让通信如此简单)
        bindView.tvBack.pressEffectAlpha().click { finish() }
        bindView.tvCode.pressEffectAlpha().click {
            val phone = bindView.etAccount.text.toString().trim()
            if (TextUtils.isEmpty(phone)) {
                getString(R.string.手机号不能为空).toast()
                return@click
            }
            if (!PatternUtils.isPhoneNumber(phone)) {
                getString(R.string.您输入的手机号不存在).toast()
                return@click
            }
            mRegisterViewModel.getCode(phone, SendCodeType.ForgetPassword)
        }
//        bindView.ivAccountDown.click {
//            //下拉选择已登录的手机号
//            var accounts = AccountDao.getAccounts()
//            showPopupWindow(bindView.etAccount, accounts)
//        }
        bindView.pwdState.click {
            //眼睛显示隐藏
            showPwd(bindView.etPwd, bindView.pwdState)
        }

        bindView.pwdOkState.click {
            //眼睛显示隐藏
            showPwd(bindView.etPwdOk, bindView.pwdOkState)
        }

        bindView.btnLogin.pressEffectAlpha().click {
            mobile = bindView.etAccount.text.toString().trim()
            val code = bindView.etCode.text.toString().trim()
            pwd = bindView.etPwd.text.toString().trim()
            val pwdOk = bindView.etPwdOk.text.toString().trim()
            if (TextUtils.isEmpty(mobile)) {
                getString(R.string.手机号不能为空).toast()
                return@click
            }
            if (!PatternUtils.isPhoneNumber(mobile)) {
               getString(R.string.您输入的手机号不存在).toast()
                return@click
            }
            if (TextUtils.isEmpty(code)) {
                getString(R.string.请获取验证码).toast()
                return@click
            }
            if (!PatternUtils.isCodeMatcher(code)) {
                getString(R.string.您输入的验证码不存在).toast()
                return@click
            }
            if (TextUtils.isEmpty(pwd)) {
                getString(R.string.密码不能为空).toast()
                return@click
            }
            if (!PatternUtils.isPwdMatcher(pwd)) {
                getString(R.string.您输入的密码有误).toast()
                return@click
            }
            if (TextUtils.isEmpty(pwdOk)) {
                getString(R.string.确认密码不能为空).toast()
                return@click
            }
//            if (!PatternUtils.isPwdMatcher(pwdOk)) {
//                "您输入的确认密码有误，请重新输入".toast()
//                return@click
//            }
            if (pwdOk != pwd) {
                getString(R.string.两次密码不一致).toast()
                return@click
            }
            //处理登录操作
            mViewModel.forgetPaw(mobile, code, pwd, pwdOk)
        }

        //继续上次倒计时
        val endTime = MMKVUtils.mmkv?.decodeLong("ForgetSmsTime", 0) ?: 0
        val currentTime = System.currentTimeMillis()
        if (endTime - currentTime > TimeConstants.SEC) countDown((endTime - currentTime) / 1000)
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
        //登录回调
        mViewModel.loginLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    result.data?.let {
                        AccountDao.saveAccount(
                            it.data,
                            mobile = mobile, password = pwd
                        )
                    }
                    getString(R.string.登录成功).toast()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.登录失败).toast()
                    }
                }
            }
        }


        //验证码回调
        mRegisterViewModel.getCodeLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (bindView.tvCode.text.toString() == getString(R.string.huoquyanzhengma)) {
                        bindView.tvCode.isEnabled = false//不能再点击
                        MMKVUtils.mmkv?.encode(
                            "ForgetSmsTime",
                            System.currentTimeMillis() + 60 * TimeConstants.SEC
                        )
                        countDown()
                    }
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }

        //重置密码回调
        mViewModel.forgetStatusLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //重置密码成功  执行登录操作
                    mViewModel.login(mobile, MD5.MD532(pwd).lowercase())
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.重置密码失败).toast()
                    }
                }
            }
        }
    }


    //<editor-fold defaultstate="collapsed" desc="倒计时">
    @SuppressLint("SetTextI18n")
    private fun countDown(sends: Long = 60) {
        mJob?.cancel()
        mJob = rxLifeScope.launch {
            for (i in sends downTo 0) {
                if (isActive) {
                    if (i == 0L) {
                        bindView.tvCode.text = getString(R.string.huoquyanzhengma)
                        bindView.tvCode.isEnabled = true//恢复点击
                    } else {
                        bindView.tvCode.text = "${i}s"
                        delay(1000)
                    }
                }
            }
        }
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="生命周期">
    override fun finish() {
        super.finish()
        mJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob?.cancel()
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="设置密码显示和隐藏">
    private fun showPwd(edit: EditText, iv: ImageView) {
        edit.requestFocus()
        val show = edit.transformationMethod is PasswordTransformationMethod
        edit.transformationMethod =
            if (show) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        iv.setImageResource(if (show) R.drawable.ic_pwd_close else R.drawable.ic_pwd_open)
        edit.setSelection(edit.text.length)
    }
    //</editor-fold>

    /**
     * 显示更多菜单
     */
    private var mAccPopWindow: AccountPopupWindow? = null
    private fun showPopupWindow(view: View, accounts: MutableList<AccountBean>) {
        if (mAccPopWindow == null) {
            mAccPopWindow = AccountPopupWindow(this, onItemClickListener = {
                bindView.etAccount.setText(it.mobile)
                mAccPopWindow?.dismiss()
            })
            mAccPopWindow?.setData(accounts)
            mAccPopWindow?.showAsDropDown(view, -SizeUtils.dp2px(10.0f), SizeUtils.dp2px(4.0f))
        } else {
            if (mAccPopWindow?.isShowing == true)
                mAccPopWindow?.dismiss()
            else
                mAccPopWindow?.showAsDropDown(view, -SizeUtils.dp2px(10.0f), SizeUtils.dp2px(4.0f))
        }
    }
}