package com.ym.chat.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.rxLifeScope
import com.blankj.utilcode.constant.TimeConstants
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.FragmentRegisterStep1Binding
import com.ym.chat.db.AccountDao
import com.ym.chat.enum.SendCodeType
import com.ym.chat.ui.HomeActivity
import com.ym.chat.ui.LoginActivity
import com.ym.chat.ui.RegisterActivity
import com.ym.chat.ui.WebActivity
import com.ym.chat.utils.MD5
import com.ym.chat.utils.PatternUtils
import com.ym.chat.viewmodel.RegisterViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/17 16:02
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 注册第一步
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class RegisterStep1Fragment : LoadingFragment(R.layout.fragment_register_step1) {
    private val bindView: FragmentRegisterStep1Binding by binding()
    private val mViewModel: RegisterViewModel = RegisterViewModel()
    private var mJob: Job? = null
    private var accountStr = ""
    private var accountPwd = ""

    override fun initView() {
        bindView.tvSendCode.click {
            accountStr = bindView.etAccount.text.toString().trim()
            if (TextUtils.isEmpty(accountStr)) {
                getString(R.string.手机号不能为空).toast()
                return@click
            }
            if (!PatternUtils.isPhoneNumber(accountStr)) {
               getString(R.string.您输入的手机号不存在).toast()
                return@click
            }
            mViewModel.getCode(accountStr, SendCodeType.Register)
        }

        bindView.btnRegister.click {
            val yqm = bindView.etYqm.text.toString().trim()
            accountStr = bindView.etAccount.text.toString().trim()
//            val name = bindView.etName.text.toString().trim()
            accountPwd = bindView.etPwd.text.toString().trim()
            val code = bindView.etCode.text.toString().trim()

            if (TextUtils.isEmpty(accountStr)) {
                if (bindView.rlPhoneCode.visibility == View.VISIBLE) {
                    getString(R.string.手机号不能为空).toast()
                    return@click
                } else {
                    getString(R.string.账号不能为空).toast()
                    return@click
                }
            }
            if (!PatternUtils.isPhoneNumber(accountStr) && bindView.rlPhoneCode.visibility == View.VISIBLE) {
                getString(R.string.您输入的手机号不存在).toast()
                return@click
            } else {
                if (!PatternUtils.isPwdMatcher(accountStr)) {
                    getString(R.string.账号必须字母).toast()
                    return@click
                }
            }

            if (TextUtils.isEmpty(accountPwd)) {
                getString(R.string.密码不能为空).toast()
                return@click
            }
            if (!PatternUtils.isPwdMatcher(accountPwd)) {
                getString(R.string.密码必须字母).toast()
                return@click
            }
            if (bindView.rlPhoneCode.visibility == View.VISIBLE) {
                if (TextUtils.isEmpty(code)) {
                    getString(R.string.请获取验证码).toast()
                    return@click
                }
                if (!PatternUtils.isCodeMatcher(code)) {
                    getString(R.string.您输入的验证码不存在).toast()
                    return@click
                }
            }
            if (bindView.rlYqm.visibility == View.VISIBLE && TextUtils.isEmpty(yqm)) {
                getString(R.string.邀请码不能为空).toast()
                bindView.YqmHint.visible()
                return@click
            }
            if (!bindView.cb1.isChecked) {
                getString(R.string.请阅读并同意用户协议和隐私政策).toast()
                return@click
            }

//            if (TextUtils.isEmpty(yqm)) {
//                commDialog(parentFragmentManager) {
//                    mTxtTitle = "温馨提示"
//                    mTxtContent = "邀请码填写完整，您的上级导师可帮您申请活动奖金喔！"
//                    mTxtCancel = "填写邀请码"
//                    mTxtConfirm = "坚持注册"
//                    mCallConfirm = {
//                        //提交注册
//                        mViewModel.register(
//                            mobile = accountStr,
//                            pwd = MD5.MD532(accountPwd).lowercase(),
//                            code = code,
//                            yqm = yqm,
//                            name = ""
//                        )
//                    }
//                }
//            }else{
            if (bindView.rlPhoneCode.visibility == View.VISIBLE) {
                //提交注册
                mViewModel.register(
                    mobile = accountStr,
                    pwd = MD5.MD532(accountPwd).lowercase(),
                    code = code,
                    yqm = yqm,
                    userName = ""
                )
            } else {
                //提交注册
                mViewModel.register(
                    mobile = "",
                    pwd = MD5.MD532(accountPwd).lowercase(),
                    code = "",
                    yqm = yqm,
                    userName = accountStr
                )
            }
//            }

        }

        bindView.btnLogin.click {
            activity?.finish()
        }

        bindView.pwdState.click {
            //眼睛显示隐藏
            showPwd(bindView.etPwd, bindView.pwdState)
        }
        bindView.tvUserAgreement.click {
            //用户协议
//            startWebPage(LoginActivity.USER_AGREEMENT_URL)
            context?.let { it1 -> WebActivity.start(it1, LoginActivity.USER_AGREEMENT_URL, getString(R.string.用户协议)) }
        }
        bindView.tvPrivacyPolicy.click {
            //隐私政策
//            startWebPage(LoginActivity.PRIVACY_POLICY_URL)
            context?.let { it1 -> WebActivity.start(it1, LoginActivity.PRIVACY_POLICY_URL, getString(R.string.隐私政策)) }
        }
//        bindView.tvSelectCode.click {
////            "目前仅支持中国手机号".toast()
//            ToastUtils.showToastWithImg(activity, "目前仅支持中国手机号", R.drawable.dialog_close)
//        }

        bindView.etYqm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (!editable.isNullOrEmpty()) {
                    bindView.YqmHint.gone()
                }
            }
        })


        //继续上次倒计时
        val endTime = MMKVUtils.mmkv?.decodeLong("ForgetSmsTime", 0) ?: 0
        val currentTime = System.currentTimeMillis()
        if (endTime - currentTime > TimeConstants.SEC) countDown((endTime - currentTime) / 1000)
    }

    override fun requestData() {
        showLoading()
        mViewModel.getRegisterType()
    }

    /**
     * 跳转到网页
     */
    private fun startWebPage(url: String) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    //<editor-fold defaultstate="collapsed" desc="接口数据回调">
    override fun observeCallBack() {
        //注册回调
        mViewModel.loginLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.注册成功).toast()
                    result.data?.let {
                        AccountDao.saveAccount(
                            it.data,
                            mobile = accountStr,
                            password = accountPwd
                        )
                    }
                    startActivity(Intent(context, HomeActivity::class.java))
                    activity?.finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.注册失败).toast()
                    }
                }
            }
        }


        //注册回调
        mViewModel.getCodeLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (bindView.tvSendCode.text.toString() == getString(R.string.huoquyanzhengma)) {
                        bindView.tvSendCode.isEnabled = false//不能再点击
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

        //注册方式
        mViewModel.registerType.observe(this) {

            when (it) {
                is BaseViewModel.LoadState.Loading -> {
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (it.data?.mobileRegister == true) {
                        //使用手机号
                        bindView.etAccount.setHint(getString(R.string.qingshurushoujihao))
                        bindView.rlPhoneCode.visible()
                        bindView.etAccount.setInputType(InputType.TYPE_CLASS_TEXT)
                        (activity as RegisterActivity).setRegisterTitle(getString(R.string.手机号注册))
                    } else {
                        //使用username
                        bindView.etAccount.setHint(getString(R.string.请输入用户名))
                        bindView.rlPhoneCode.gone()
                        bindView.etAccount.setInputType(InputType.TYPE_CLASS_TEXT)
                        (activity as RegisterActivity).setRegisterTitle(getString(R.string.zhanghaozhuce))
                    }

                    if (it.data?.recommendCodeRegister == true) {
                        //开启填入邀请码
                        bindView.rlYqm.visible()
                    } else {
                        //关闭填入邀请码
                        bindView.rlYqm.gone()
                    }
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="设置密码显示和隐藏">
    private fun showPwd(edit: EditText, iv: ImageView) {
        edit.requestFocus()
        val show = edit.transformationMethod is PasswordTransformationMethod
        edit.transformationMethod =
            if (show) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        iv.setImageResource(if (show) R.drawable.ic_pwd_open else R.drawable.ic_pwd_close)
        edit.setSelection(edit.text.length)
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="倒计时">
    @SuppressLint("SetTextI18n")
    private fun countDown(sends: Long = 60) {
        mJob?.cancel()
        mJob = rxLifeScope.launch {
            for (i in sends downTo 0) {
                if (isActive) {
                    if (i == 0L) {
                        bindView.tvSendCode.text =getString(R.string.huoquyanzhengma)
                        bindView.tvSendCode.isEnabled = true//恢复点击
                    } else {
                        bindView.tvSendCode.text = "${i}s"
                        delay(1000)
                    }
                }
            }
        }
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="生命周期">

    override fun onDestroy() {
        super.onDestroy()
        mJob?.cancel()
    }
    //</editor-fold>
}