package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.EncodeBean
import com.ym.chat.bean.VersionBean
import com.ym.chat.rxhttp.UserRepository

/***
 * 设置
 */
class SetViewModel : BaseViewModel() {
    val changedPawLiveData = MutableLiveData<LoadState<BaseBean<String>>>()
    val changedStatusLiveData = MutableLiveData<LoadState<LoginBean>>()
    val loginOutLiveData = MutableLiveData<LoadState<LoginBean>>()
    val memberIdGetUserInfo = MutableLiveData<LoadState<LoginBean>>()
    val appVersion = MutableLiveData<LoadState<VersionBean>>()
    val refereeLink = MutableLiveData<LoadState<EncodeBean>>()
    val modifyPhoneLiveData = MutableLiveData<LoadState<BaseBean<String>>>()

    /**
     * 修改用户密码
     */
    fun changedPaw(
        oldPaw: String,
        newPaw1: String, newPaw2: String
    ) {
        if (changedPawLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.changedUserPwd(oldPaw, newPaw1, newPaw2)
            if (result.code == SUCCESS) {
                changedPawLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                changedPawLiveData.value = LoadState.Fail(dataOld = result)
            }
        }, onError = {
            changedPawLiveData.value = LoadState.Fail(exc = Exception("修改密码失败"))
        }, onStart = {
            changedPawLiveData.value = LoadState.Loading()
        })
    }

    /**
     * 修改会员状态
     * 状态:可用 enable,禁用 disable,锁定 locked
     */
    fun changedStatus(status: String) {
        if (changedStatusLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.changeModifyStatus(status)
            if (result.code == SUCCESS) {
                changedStatusLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    changedStatusLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    changedStatusLiveData.value = LoadState.Fail(exc = Exception("修改会员状态失败"))
                }
            }
        }, onError = {
            changedStatusLiveData.value = LoadState.Fail(exc = Exception("修改会员状态失败"))
        }, onStart = {
            changedStatusLiveData.value = LoadState.Loading()
        })
    }

    /**
     * 登出
     */
    fun loginOut() {
        if (loginOutLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.loginOut()
            if (result.code == SUCCESS) {
                loginOutLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    loginOutLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    loginOutLiveData.value = LoadState.Fail(exc = Exception("退出失败"))
                }
            }
        }, onError = {
            loginOutLiveData.value = LoadState.Fail(exc = Exception("退出失败"))
        }, onStart = {
            loginOutLiveData.value = LoadState.Loading()
        })
    }


    /**
     *根据会员id获取会员信息
     */
    fun memberIdGetUserInfo(memberId: String) {
        if (memberIdGetUserInfo.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.memberIdGetUserInfo(memberId)
            if (result.code == SUCCESS) {
                memberIdGetUserInfo.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    if(result.code == 9003){
                        //查询的是自己
                        memberIdGetUserInfo.value = LoadState.Fail(exc = Exception(""))
                    }else {
                        memberIdGetUserInfo.value = LoadState.Fail(exc = Exception(errorInfo))
                    }
                } else {
                    memberIdGetUserInfo.value = LoadState.Fail(exc = Exception("获取会员信息异常"))
                }
            }
        }, onError = {
            memberIdGetUserInfo.value = LoadState.Fail(exc = Exception("获取会员信息异常"))
        }, onStart = {
            memberIdGetUserInfo.value = LoadState.Loading()
        })
    }

    /**
     *获取app版本信息
     */
    fun getAppVersion() {
        if (appVersion.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.getAppVersion()
            if (result.code == SUCCESS) {
                appVersion.value = LoadState.Success(result.data)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    appVersion.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    appVersion.value = LoadState.Fail(exc = Exception("获取版本信息异常"))
                }
            }
        }, onError = {
            appVersion.value = LoadState.Fail(exc = Exception("获取版本信息异常"))
        }, onStart = {
            appVersion.value = LoadState.Loading()
        })
    }

    /**
     *获取友聊号的秘文
     */
    fun getRefereeLink() {
        if (refereeLink.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.getRefereeLink()
            if (result.code == SUCCESS) {
                refereeLink.value = LoadState.Success(result.data)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    refereeLink.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    refereeLink.value = LoadState.Fail(exc = Exception("获取数据异常"))
                }
            }
        }, onError = {
            refereeLink.value = LoadState.Fail(exc = Exception("获取数据异常"))
        }, onStart = {
            refereeLink.value = LoadState.Loading()
        })
    }


    /**
     * 修改用户信息
     */
    fun modifyPhone(
        mobile: String,
        mobileCode: String,
        password: String
    ) {
        if (modifyPhoneLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.modifyPhone(mobile, mobileCode, password)
            if (result.code == SUCCESS) {
                modifyPhoneLiveData.value = LoadState.Success(result)
            } else {
                modifyPhoneLiveData.value =  LoadState.Fail(dataOld = result)
            }
        }, onError = {
            modifyPhoneLiveData.value = LoadState.Fail(exc = Exception("修改用户信息失败"))
        }, onStart = {
            modifyPhoneLiveData.value = LoadState.Loading()
        })
    }

}