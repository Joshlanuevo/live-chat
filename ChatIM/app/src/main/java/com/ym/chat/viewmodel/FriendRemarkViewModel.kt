package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.chat.rxhttp.UserRepository

/***
 * 修改用户信息
 */
class FriendRemarkViewModel : BaseViewModel() {
    val editUserInfoLiveData = MutableLiveData<LoadState<LoginBean>>()

    /**
     * 修改用户信息
     */
    fun editUserInfo(name: String?=null,
                     headUrl:String?=null,
                     sign: String?=null,
                     gender: String?=null) {
        if (editUserInfoLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.editUserInfo(name,headUrl,sign,gender)
            if (result.code == SUCCESS) {
                editUserInfoLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    editUserInfoLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    editUserInfoLiveData.value = LoadState.Fail(exc = Exception("修改用户信息失败"))
                }
            }
        }, onError = {
            editUserInfoLiveData.value = LoadState.Fail(exc = Exception("修改用户信息失败"))
        }, onStart = {
            editUserInfoLiveData.value = LoadState.Loading()
        })
    }
}