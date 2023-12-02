package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.base.util.save.LoginData
import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.SendGroupMsgBean
import com.ym.chat.rxhttp.SendGroupMsgRepository

/***
 * 群发消息
 */
class SendGroupMsgViewModel : BaseViewModel() {

    val sendGroupMsgList = MutableLiveData<LoadState<BaseBean<SendGroupMsgBean>>>()
    val delSendGroupMsg = MutableLiveData<LoadState<BaseBean<Boolean>>>()

    /**
     * 获取群发消息列表
     */
    fun getSendGroupMsg(curPage: String, pageSize: String = "20") {
        if (sendGroupMsgList.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = SendGroupMsgRepository.getSendGroupMsg(curPage,pageSize)
            if (result.code == SUCCESS) {
                sendGroupMsgList.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    sendGroupMsgList.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    sendGroupMsgList.value = LoadState.Fail(exc = Exception("获取群发消息列表失败"))
                }
            }
        }, onError = {
            sendGroupMsgList.value = LoadState.Fail(exc = Exception("获取群发消息列表失败"))
        }, onStart = {
            sendGroupMsgList.value = LoadState.Loading()
        })
    }

    /**
     * 清空群发消息列表
     */
    fun delSendGroupMsg() {
        if (delSendGroupMsg.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = SendGroupMsgRepository.delSendGroupMsg()
            if (result.code == SUCCESS) {
                delSendGroupMsg.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    delSendGroupMsg.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    delSendGroupMsg.value = LoadState.Fail(exc = Exception("清空群发消息列表失败"))
                }
            }
        }, onError = {
            delSendGroupMsg.value = LoadState.Fail(exc = Exception("清空群发消息列表失败"))
        }, onStart = {
            delSendGroupMsg.value = LoadState.Loading()
        })
    }

}