package com.ym.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.chat.rxhttp.MovementRepository

/**
 * 发现ViewModel
 */
class MovementModel : BaseViewModel() {

    val urlResult = MutableLiveData<LoadState<String>>()

    /**
     * 获取收藏数据列表
     */
    fun getFindUrl() {
        requestLifeLaunch({
            //发现url
            val findUrl = MovementRepository.getFindUrl()
            if (findUrl.code == SUCCESS) {
                urlResult.value = LoadState.Success(findUrl.data)
            } else {
                urlResult.value = LoadState.Fail()
            }
        }, {
            it.printStackTrace()
            urlResult.value = LoadState.Fail()
        })
    }
}


