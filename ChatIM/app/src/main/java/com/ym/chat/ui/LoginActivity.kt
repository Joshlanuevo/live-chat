package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.invisible
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.base.widget.ext.visible
import com.ym.chat.BuildConfig
import com.ym.chat.R
import com.ym.chat.bean.AccountBean
import com.ym.chat.databinding.ActivityLoginNewBinding
import com.ym.chat.db.AccountDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.dialog.LoginVerificationDialog
import com.ym.chat.popup.AccountPopupWindow
import com.ym.chat.service.WebsocketServiceManager
import com.ym.chat.utils.ImCache
import com.ym.chat.utils.MD5
import com.ym.chat.utils.PatternUtils
import com.ym.chat.utils.ServiceErrorCode.toMsg
import com.ym.chat.viewmodel.LoginViewModel


/**
 * 登录页面
 */
class LoginActivity : LoadingActivity() {
    private val bindView: ActivityLoginNewBinding by binding()
    private val mViewModel: LoginViewModel = LoginViewModel()
    private var accountType = 0//输入账号类型，0：手机号，1：友聊账号
    private var accountStr = ""
    private var accountPwd = ""
    private var inType = 0//当从那个界面跳转过来的类型，默认(0)、切换自定账号(1)、切换新账号(2)
    private var mobile = ""//切换指定账号的mobile
    private var loginVerifyDialog: LoginVerificationDialog? = null

    companion object {
        const val USER_AGREEMENT_URL = "https://72s75f.axshare.com"//用户协议
        const val PRIVACY_POLICY_URL = "https://j11k4n.axshare.com"//隐私政策
        val IN_TYPE = "inType"
        val MOBILE = "mobile"

        /**
         * 当从那个界面跳转过来的类型，默认(0)、切换指定账号(1)、切换新账号(2)
         */
        fun start(
            context: Context,
            inType: Int = 0,
            mobile: String = ""
        ) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(IN_TYPE, inType)
            intent.putExtra(MOBILE, mobile)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        bindView.tvAppName.text =getString(R.string.app_name)
        bindView.tvInfo.text = getString(R.string.app_name) + getString(R.string.让通信如此简单)
        bindView.tvMobileLogin.text = getString(R.string.app_name) + getString(R.string.zhanghaodenglu)
        bindView.tvMobileLogin.click {
//            //手机账号登录
//            bindView.tvMobileLogin.setTextColor(getColor(R.color.color_main))
//            bindView.tvAccountLogin.setTextColor(getColor(R.color.color_gray_text))
//            bindView.tvAccountLogin.textSize = 14f
////            bindView.etAccount.filters = arrayOf<InputFilter>(object : LengthFilter(11) {})
//            bindView.viewCode.visible()
//            bindView.etAccount.hint = "请输入账号/手机号"
//            bindView.etAccount.setText("")
//            bindView.etAccount.inputType = InputType.TYPE_CLASS_TEXT
//            bindView.linePhone.visible()
//            bindView.lineCode.invisible()
//            accountType = 0

        }
        bindView.tvAccountLogin.click {
//            //友聊账号登录
//            bindView.tvAccountLogin.setTextColor(getColor(R.color.color_main))
//            bindView.lineCode.visible()
//            bindView.linePhone.invisible()
//            bindView.tvMobileLogin.setTextColor(getColor(R.color.color_gray_text))
//            bindView.tvMobileLogin.textSize = 14f
////            bindView.etAccount.filters = arrayOf<InputFilter>(object : LengthFilter(15) {})
//            bindView.etAccount.hint = "请输入友聊号"
//            bindView.etAccount.setText("")
//            bindView.etAccount.inputType = InputType.TYPE_CLASS_TEXT
//            accountType = 1
        }
        bindView.tvUserAgreement.click {
            //用户协议
//            startWebPage(USER_AGREEMENT_URL)
            WebActivity.start(this, USER_AGREEMENT_URL, getString(R.string.用户协议))
        }
        bindView.tvPrivacyPolicy.click {
            //隐私政策
//            startWebPage(PRIVACY_POLICY_URL)
            WebActivity.start(this, PRIVACY_POLICY_URL, getString(R.string.隐私政策))
        }
        bindView.ivAccountDown.click {
            //下拉选择已登录的手机号
            var accounts = AccountDao.getAccounts()
            if (accounts.size > 0)
                showPopupWindow(bindView.etAccount, accounts)
        }
        bindView.pwdState.click {
            //眼睛显示隐藏
            showPwd(bindView.etPwd, bindView.pwdState)
        }
        bindView.BtnRegister.pressEffectAlpha().click {
            //去注册
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        bindView.tvForgetPwd.pressEffectAlpha().click {
            //找回密码
            startActivity(Intent(this, FindPwdActivity::class.java))
        }
        bindView.btnLogin.click {
            //登录
            accountStr = bindView.etAccount.text.toString().trim()
            accountPwd = bindView.etPwd.text.toString().trim()
            if (TextUtils.isEmpty(accountStr)) {
                if (accountType == 0) {
                    getString(R.string.账号不能为空).toast()
                } else {
                    getString(R.string.友聊号不能为空).toast()
                }
                return@click
            }
//            if (accountType == 0) {
//                if (!PatternUtils.isPhoneNumber(accountStr)) {
//                    "您输入的手机号不存在，请重新输入".toast()
//                    return@click
//                }
//            }
//            else {
//                if (!PatternUtils.isJinXin(accountStr)) {
//                    "您输入的友聊号不存在，请重新输入".toast()
//                    return@click
//                }
//            }

            if (TextUtils.isEmpty(accountPwd)) {
                getString(R.string.密码不能为空).toast()
                return@click
            }
            if (!PatternUtils.isPwdMatcher(accountPwd)) {
                getString(R.string.您输入的密码有误).toast()
                return@click
            }
            if (!bindView.cb1.isChecked) {
                getString(R.string.请阅读并同意用户协议和隐私政策).toast()
                return@click
            }
            mViewModel.login(
                if (accountType == 0) accountStr else "",
                MD5.MD532(accountPwd).lowercase(),
                if (accountType == 1) accountStr else ""
            )
        }

        intent?.let { intent ->
            inType = intent.getIntExtra(IN_TYPE, 0)
        }

        when (inType) {
            //如果以前登录过填入登录账号
            0 -> {
                var accounts = AccountDao.getAccounts() ?: mutableListOf<AccountBean>()
                if (accounts.size > 0) {
                    var account = accounts[accounts.size - 1]//最后登录的账号存在最后
                    accountType = account.accountType
                    showLoginView()
                    if (accountType == 0) bindView.etAccount.setText(account.mobile) else bindView.etAccount.setText(
                        account.showUsername()
                    )
                }
            }

            1 -> {
                intent?.let { intent ->
                    var userId = intent.getStringExtra(MOBILE).toString()//账号id
                    var accounts = AccountDao.getAccounts()
                    accounts.forEach {
                        if (it.id == userId) {
                            accountType = it.accountType
                            showLoginView()
                            accountStr = if (accountType == 0) it.mobile else it.showUsername()
                            bindView.etAccount.setText(accountStr)
                            accountPwd = it.password ?: ""
                            bindView.etPwd.setText(accountPwd)
                            if (accountPwd.isNullOrEmpty()) {
                                "登录密码没有找到".toast()
                                return@forEach
                            }
                            mViewModel.login(
                                if (accountType == 0) it.mobile else "",
                                MD5.MD532(accountPwd).lowercase(),
                                if (accountType == 1) it.showUsername() else ""
                            )
                            return@forEach
                        }
                    }
                }
            }

            2 -> {
                accountType = 0
                showLoginView()
            }
        }

        if (!TextUtils.isEmpty(ImCache.KillOutType)) {
            HintDialog(
                getString(R.string.通知),
                if (ImCache.KillOutType == "Disable") getString(R.string.您已被后台管理员封禁) else if (ImCache.KillOutType == "KickOut") getString(
                    R.string.您已经在其他设备登入
                ) else "",
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                    }
                },
                R.drawable.ic_hint_delete, isShowBtnCancel = false,
                isCanTouchOutsideSet = false
            ).show(supportFragmentManager, "HintDialog")
            ImCache.KillOutType = ""
        }

        //重置密码输错次数，修改手机号与密码
        MMKVUtils.savePwdCount(0);
        MMKVUtils.savePhonePwdCount(0);
    }


    fun setViewImg(context: Context, id: Int, textView: TextView) {
        val drawableLeft = context.resources.getDrawable(id)
        drawableLeft.setBounds(0, 0, 60, 60)
        textView.compoundDrawablePadding = 15
        textView.setCompoundDrawables(
            drawableLeft,
            null, null, null
        )
    }


    /**
     * 跳转到网页
     */
    private fun startWebPage(url: String) {
        var intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun showLoginView() {
        if (accountType == 0) {
            //显示手机号码登录界面
            bindView.tvMobileLogin.setTextColor(getColor(R.color.color_main))
            bindView.tvMobileLogin.textSize = 16f
            bindView.linePhone.visible()
            bindView.lineCode.invisible()
            bindView.tvAccountLogin.setTextColor(getColor(R.color.color_gray_text))
            bindView.tvAccountLogin.textSize = 14f
            bindView.viewCode.visible()
//            bindView.etAccount.filters = arrayOf<InputFilter>(object : LengthFilter(11) {})
            bindView.etAccount.hint = "请输入账号/手机号"
            bindView.etAccount.setText("")
            bindView.etAccount.inputType = InputType.TYPE_CLASS_TEXT
        } else {
            //显示友聊号码登录界面
            bindView.tvAccountLogin.setTextColor(getColor(R.color.color_main))
            bindView.tvAccountLogin.textSize = 16f
            bindView.lineCode.visible()
            bindView.linePhone.invisible()
            bindView.tvMobileLogin.setTextColor(getColor(R.color.color_gray_text))
            bindView.tvMobileLogin.textSize = 14f
            bindView.tvMobileLogin.setCompoundDrawables(null, null, null, null)
            var drawable1 = resources.getDrawable(R.drawable.ic_login_jxh)
            drawable1.setBounds(0, 0, drawable1.minimumWidth, drawable1.minimumHeight)
            bindView.etAccount.setCompoundDrawables(drawable1, null, null, null)
//            bindView.etAccount.filters = arrayOf<InputFilter>(object : LengthFilter(15) {})
            bindView.etAccount.hint = getString(R.string.请输入友聊号)
            bindView.etAccount.setText("")
            bindView.etAccount.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

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
    override fun onResume() {
        super.onResume()
        WebsocketServiceManager.closeConnect(this)
    }
    override fun requestData() {
        //登陆失效之后，只是断开ws连接，并不会关闭服务
    }

    override fun observeCallBack() {
        //登录回调
        mViewModel.loginLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading(getString(R.string.登录中))
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    result.data?.let {
                        AccountDao.saveAccount(
                            it.data,
                            accountType,
                            accountStr,
                            accountPwd
                        )
                    }
                    getString(R.string.登录成功).toast()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (result.dataOld != null) {
                        if (result.dataOld?.code == 10036) {//移动端多端登录code
                            sendVerifyCode()
                            showLoginVerificationDialog(result.dataOld?.data?.deviceDescription)
                        } else {
                            if (!TextUtils.isEmpty(result.dataOld?.info)) {
                                result.dataOld?.code?.toMsg().toast()
                            } else {
                                getString(R.string.登录失败).toast()
                            }
                        }
                    } else {
                        getString(R.string.登录失败).toast()
                    }
                }
            }
        }

        //验证码发送回调
        mViewModel.sendVerifyCodeData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }

                is BaseViewModel.LoadState.Success -> {
                    loginVerifyDialog?.startTimer()
                }

                is BaseViewModel.LoadState.Fail -> {

                }
            }
        }
        //验证码校验回调
        mViewModel.checkVerifyCodeData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading(getString(R.string.登录中))
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    result.data?.let {
                        AccountDao.saveAccount(it.data, accountType, accountStr, accountPwd)
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
    }

    private fun sendVerifyCode() {
        mViewModel.sendVerifyCode(accountStr)
    }


    /**
     * 显示更多菜单
     */
    private var mAccPopWindow: AccountPopupWindow? = null
    private fun showPopupWindow(view: View, accounts: MutableList<AccountBean>) {
        mAccPopWindow = AccountPopupWindow(this, onItemClickListener = {
            bindView.etAccount.setText(it.mobile)
            mAccPopWindow?.dismiss()
        })
        accounts.reverse()//数据倒叙
        var a = accounts.filter { it.accountType == accountType }
        if (a != null) {
            var acc = mutableListOf<AccountBean>()
            acc.addAll(a)
            mAccPopWindow?.setData(acc)
            mAccPopWindow?.showAsDropDown(view, -SizeUtils.dp2px(10.0f), SizeUtils.dp2px(4.0f))
        }
    }


    /**
     * 退出登录 dialog
     */
    private fun showLogOutHintDialog() {
        HintDialog(
            getString(R.string.通知),
            getString(R.string.您已经在其他设备登入),
            object : ConfirmDialogCallback {
                override fun onItemClick() {

                }
            },
            R.drawable.ic_hint_delete,
            isShowBtnCancel = false,
            isCanTouchOutsideSet = false
        ).show(supportFragmentManager, "HintDialog")
    }

    /**
     * 登录验证码 dialog
     */
    private fun showLoginVerificationDialog(deviceName: String?) {
        loginVerifyDialog?.dismiss()
        loginVerifyDialog = LoginVerificationDialog(
            getString(R.string.dengluxuyaozhushebeiyanzheng),
            String.format(getString(R.string.验证码已发送到主设备), deviceName),
            onSendClickListener = {

                mViewModel.sendVerifyCode(accountStr)
            },
            onConfirmClickListener = {
                mViewModel.checkVerifyCode(accountStr, it)
            }
        )
        loginVerifyDialog?.show(supportFragmentManager, "HintDialog")
    }

}