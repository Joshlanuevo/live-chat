package com.ym.chat.viewmodel

import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.ym.base.ext.logE
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.rxhttp.FriendRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.utils.AesUtils
import com.ym.chat.utils.CommandType
import org.json.JSONObject

/**
 * 通知列表ViewModel
 */
class NotifyViewModel : BaseViewModel() {

    val deleteNotify = MutableLiveData<LoadState<SimpleBean>>()
    val getSystemMsg = MutableLiveData<LoadState<Boolean>>()
    val getFriendNotifyInfo = MutableLiveData<LoadState<MutableList<NotifyBean>>>()

    /**
     *获取好友申请列表通知
     */
    fun getFriendNotifyInfo() {
        if (getFriendNotifyInfo.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = FriendRepository.getFriendNotifyInfo()
            if (result.code == SUCCESS) {
                ChatDao.getNotifyDb().delAllNotifyMsg(2)
                result.data?.records?.forEach {
                    ChatDao.getNotifyDb()
                        .saveNotifyFriendMsg(it.id,it.friendMemberId,it.memberId,it.status, name = it.name,
                            headerUrl = it.headUrl,    time = it.createTime.toLong(),)
                }
                getFriendNotifyInfo.value = LoadState.Success(ChatDao.getNotifyDb().getNotifyMsgList(2))
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    getFriendNotifyInfo.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    getFriendNotifyInfo.value = LoadState.Fail(exc = Exception("获取好友申请列表失败"))
                }
            }
        }, onError = {
            getFriendNotifyInfo.value = LoadState.Fail(exc = Exception("获取好友申请列表失败"))
        }, onStart = {
            getFriendNotifyInfo.value = LoadState.Loading()
        })
    }

    /**
     * 获取系统通知
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSysNotice() {
        requestLifeLaunch({
            val result = ChatRepository.getHisNoticeMsg()
            val jsonResult = JSONObject(result)
            val code = jsonResult.optInt("code")
            if (code == 200) {
                //删除所有数据
                ChatDao.getNotifyDb().delAllNotifyMsg()

                val dataArray = jsonResult.optJSONArray("data")
                if (dataArray != null && dataArray.length() > 0) {
                    for (i in 0 until dataArray.length()) {
                        try {

                            val dataObject = dataArray.optJSONObject(i)
                            val command = dataObject.optInt("command")
                            val readFlag = dataObject.optString("readFlag")
                            val msgReadState = if (readFlag == "Read") {
                                1
                            } else {
                                0
                            }
                            val content = dataObject.optString("content")
                            val msgContent = decodeContent(content)
                            when (command) {
                                CommandType.SYSTEM_MSG -> {
                                    //系统通知
                                    ChatDao.getNotifyDb()
                                        .saveSystemNotifyMsgByJson(
                                            JSONObject(msgContent),
                                            1,
                                            msgReadState,
                                            isGetGroupMsg = false
                                        )
                                }
                                CommandType.DEVICE_LOGIN_VERIFICATION -> {
                                    //新设备验证码
                                    ChatDao.getNotifyDb()
                                        .saveNotifyMsgByJson(JSONObject(msgContent),
                                            isGetGroupMsg = false)
                                }
                                CommandType.SYSTEM_FEEDBACK_MSG -> {
                                    //意见反馈系统通知
                                    ChatDao.getNotifyDb()
                                        .saveSystemNotifyMsgByJson(
                                            JSONObject(msgContent),
                                            msgReadState = msgReadState,
                                            isGetGroupMsg = false
                                        )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                getSystemMsg.value = LoadState.Success(true)
            }
        }, {
            getSystemMsg.value = LoadState.Fail()
            it.printStackTrace()
        })
    }

    /**
     * 解密
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun decodeContent(key: String?): String {
        return String(
            AesUtils.decode(
                MMKVUtils.getUser()?.id,
                MMKVUtils.getUser()?.code,
                key?.toByteArray()
            )
        )
    }

    /**
     *删除群通知消息
     */
    fun deleteGroupNotify(notifyIdList: MutableList<String>) {
        if (deleteNotify.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.deleteGroupNotify(notifyIdList)
            if (result.code == SUCCESS) {
                deleteNotify.value = LoadState.Success(result.data)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    deleteNotify.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    deleteNotify.value = LoadState.Fail(exc = Exception("删除失败"))
                }
            }
        }, onError = {
            deleteNotify.value = LoadState.Fail(exc = Exception("删除失败"))
        }, onStart = {
            deleteNotify.value = LoadState.Loading()
        })
    }

    /**
     *删除
     *多条系统通知消息
     */
    fun deleteSystemNotify(notifyIdList: MutableList<String>) {
        if (deleteNotify.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.deleteMessageBatch(notifyIdList)
            if (result.code == SUCCESS) {
                deleteNotify.value = LoadState.Success(result.data)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    deleteNotify.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    deleteNotify.value = LoadState.Fail(exc = Exception("删除失败"))
                }
            }
        }, onError = {
            deleteNotify.value = LoadState.Fail(exc = Exception("删除失败"))
        }, onStart = {
            deleteNotify.value = LoadState.Loading()
        })
    }
}