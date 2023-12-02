package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.Utils
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.chat.R
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.UserRepository
import com.ym.chat.utils.ChatUtils

/**
 * 登录
 */
class LoginViewModel : BaseViewModel() {
    val loginLiveData = MutableLiveData<LoadState<LoginBean>>()
    val checkVerifyCodeData = MutableLiveData<LoadState<LoginBean>>()
    val sendVerifyCodeData = MutableLiveData<LoadState<Boolean>>()
    val forgetStatusLiveData = MutableLiveData<LoadState<LoginBean>>()

    /**
     * 登录
     */
    fun login(mobile: String, pwd: String, username: String = "") {
        if (loginLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.login(mobile, pwd, username);
            if (result.code == SUCCESS) {
                //初始化数据库
                ChatDao.initDb(result.data.username) { callback ->
                    loginLiveData.value = LoadState.Success(result)
                }
            } else {
                val errorInfo = result
//                if (!TextUtils.isEmpty(errorInfo)) {
//
//                } else {
//                    loginLiveData.value = LoadState.Fail(exc = Exception("登录失败"))
//                }
                loginLiveData.value = LoadState.Fail(dataOld = result)
            }
        }, onError = {
            loginLiveData.value = LoadState.Fail(exc = Exception("登录失败"))
        }, onStart = {
            loginLiveData.value = LoadState.Loading()
        })
    }


    //多端登录发送验证码
    fun sendVerifyCode(codeOrMobile: String) {
        if (sendVerifyCodeData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.sendVerifyCode(codeOrMobile)
            if (result.code == SUCCESS) {
                sendVerifyCodeData.value = LoadState.Success(result.data)
            }else {
                if (!TextUtils.isEmpty(result.info)) {
                    result?.info.toast()
                    sendVerifyCodeData.value = LoadState.Fail()
                } else {
                    ChatUtils.getString(R.string.验证码发送失败).toast()
                }
            }
        }, onError = {
            sendVerifyCodeData.value = LoadState.Fail(exc = Exception("获取验证码失败"))
        }, onStart = {
            sendVerifyCodeData.value = LoadState.Loading()
        })
    }

    /**
     * 多端登录校验验证码
     */
    fun checkVerifyCode(codeOrMobile: String, verifyCode: String) {
        if (checkVerifyCodeData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.checkVerifyCode(codeOrMobile, verifyCode);
            if (result.code == SUCCESS) {
                ChatDao.initDb(result.data.username) { callback ->
                    checkVerifyCodeData.value = LoadState.Success(result)
                }

            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    checkVerifyCodeData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    checkVerifyCodeData.value = LoadState.Fail(exc = Exception("验证码错误"))
                }
            }
        }, onError = {
            checkVerifyCodeData.value = LoadState.Fail(exc = Exception("验证码错误"))
        }, onStart = {
            checkVerifyCodeData.value = LoadState.Loading()
        })
    }


    /**
     * 重置用户密码
     */
    fun forgetPaw(
        mobile: String,
        mobileCode: String,
        newPaw1: String, newPaw2: String
    ) {
        if (forgetStatusLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.forgetUserPwd(mobile, mobileCode, newPaw1, newPaw2)
            if (result.code == SUCCESS) {
                forgetStatusLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    forgetStatusLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    forgetStatusLiveData.value = LoadState.Fail(exc = Exception("重置密码失败"))
                }
            }
        }, onError = {
            forgetStatusLiveData.value = LoadState.Fail(exc = Exception("重置密码失败"))
        }, onStart = {
            forgetStatusLiveData.value = LoadState.Loading()
        })
    }
}