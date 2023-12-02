package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.chat.bean.RegisterConfigBean
import com.ym.chat.db.ChatDao
import com.ym.chat.enum.SendCodeType
import com.ym.chat.rxhttp.UserRepository

/***
 * 注册
 */
class RegisterViewModel : BaseViewModel() {
    val loginLiveData = MutableLiveData<LoadState<LoginBean>>()
    val getCodeLiveData = MutableLiveData<LoadState<LoginBean>>()
    val registerType = MutableLiveData<LoadState<RegisterConfigBean>>()

    /**
     * 注册
     */
    fun register(mobile: String, pwd: String, code: String, yqm: String?, userName: String) {
        if (loginLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.register(mobile, pwd, code, yqm, userName);
            if (result.code == SUCCESS) {
                //初始化数据库
                ChatDao.initDb(result.data.username) { callback ->
                    loginLiveData.value = LoadState.Success(result)
                }
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    loginLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    loginLiveData.value = LoadState.Fail(exc = Exception("注册失败"))
                }
            }
        }, onError = {
            loginLiveData.value = LoadState.Fail(exc = Exception("注册失败"))
        }, onStart = {
            loginLiveData.value = LoadState.Loading()
        })
    }

    //获取验证码
    fun getCode(account: String, sendCodeType: SendCodeType) {
        if (getCodeLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.getCode(account, sendCodeType)
            if (result.code == SUCCESS) {
                getCodeLiveData.value = LoadState.Success(result)
            } else {
                if (!TextUtils.isEmpty(result.info)) {
                    result?.info.toast()
                    getCodeLiveData.value = LoadState.Fail()
                } else {
                    "验证码已发送失败".toast()
                }
            }
        }, onError = {
            getCodeLiveData.value = LoadState.Fail(exc = Exception("获取验证码失败"))
        }, onStart = {
            getCodeLiveData.value = LoadState.Loading()
        })
    }

    //获取注册方式
    fun getRegisterType() {
        requestLifeLaunch({
            val result = UserRepository.isMobileRegister()
            if (result.code == SUCCESS) {
                registerType.value = LoadState.Success(result.data)
            } else {
                registerType.value = LoadState.Fail()
            }
        }, {
            registerType.value = LoadState.Fail()
            it.printStackTrace()
        })
    }
}