package com.ym.chat.ui

import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseActivity
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityModifyPwdBinding
import com.ym.chat.databinding.ActivityPwdTimesetBinding
import com.ym.chat.databinding.ActivitySafetySetBinding
import com.ym.chat.dialog.HintSecondDialog
import com.ym.chat.utils.PatternUtils
import com.ym.chat.utils.ToastUtils
import com.ym.chat.viewmodel.ModifyNameViewModel
import com.ym.chat.viewmodel.SetViewModel
import java.util.logging.Logger

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
class ModifyPwdActivity : LoadingActivity() {
    private val bindView: ActivityModifyPwdBinding by binding()
    private val mViewModel: SetViewModel = SetViewModel()

    private val ERROR_COUNT = 10;
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.修改密码)
        }

        bindView.pwdState.click {
            //眼睛显示隐藏
            showPwd(bindView.etOldPwd, bindView.pwdState)
        }
        bindView.pwdNewState.click {
            //眼睛显示隐藏
            showPwd(bindView.etNewPwd, bindView.pwdNewState)
        }
        bindView.pwdconfirmState.click {
            //眼睛显示隐藏
            showPwd(bindView.etNewPwdConfirm, bindView.pwdconfirmState)
        }

        bindView.btnSubmit.pressEffectAlpha().click {

            var oldPwd = bindView.etOldPwd.text.toString().trim()
            var pwd = bindView.etNewPwd.text.toString().trim()
            val pwdOk = bindView.etNewPwdConfirm.text.toString().trim()
            if (TextUtils.isEmpty(oldPwd)) {
                getString(R.string.qingshurujiumima).toast()
                return@click
            }
            if (TextUtils.isEmpty(pwd)) {
                getString(R.string.请输入新密码).toast()
                return@click
            }
            if (!PatternUtils.isPwdMatcher(pwd)) {
                getString(R.string.新密码设置不符合规则).toast()
                return@click
            }
            if (TextUtils.isEmpty(pwdOk)) {
                getString(R.string.请输入确认新密码).toast()
                return@click
            }

            if (pwdOk != pwd) {
                getString(R.string.两次密码不一致).toast()
                return@click
            }
            mViewModel.changedPaw(oldPwd, pwd, pwdOk)

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
    }

    override fun observeCallBack() {
        //编辑用户信息
        mViewModel.changedPawLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    ToastUtils.showToastWithImg(
                        this@ModifyPwdActivity,
                        "密码已修改",
                        R.drawable.ic_dialog_success
                    )
                    finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
//                    bindView.tvOldPwdErrorHint.gone()
                    if (result.dataOld != null) {
                        if (result.dataOld?.code == 10033) {
                            showLoginOutDialog()
                        } else if (result.dataOld?.code == 10034) {
//                                bindView.tvOldPwdErrorHint.text =
//                                    "您输入的旧密码错误，剩下次数${result.dataOld?.data}次"
//                                bindView.tvOldPwdErrorHint.visible()

                            ToastUtils.showToastWithImg(
                                this@ModifyPwdActivity,
                                String.format(getString(R.string.您输入的旧密码错误),"${result.dataOld?.data}"),
                                R.drawable.ic_dialog_success
                            )
//                            "修改密码失败".toast()
                        } else {
                            if (!TextUtils.isEmpty(result.dataOld?.info)) {
                                result.dataOld?.info.toast()
                            } else {
                                getString(R.string.修改密码失败).toast()
                            }
                        }
                    } else {
                        getString(R.string.修改密码失败).toast()
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
            content = getString(R.string.请于1小时后再次登录操作),
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