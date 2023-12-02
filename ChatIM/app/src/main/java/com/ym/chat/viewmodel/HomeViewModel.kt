package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.constant.TimeConstants
import com.ym.base.constant.EventKeys
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.CollectRepository
import com.ym.chat.rxhttp.VPNRepository
import com.ym.chat.utils.TimeUtils
import org.json.JSONArray
import org.json.JSONObject

class HomeViewModel : BaseViewModel() {
    val updateLiveData = MutableLiveData<LoadState<Void>>()

    //上次调用检查更新接口时间
    private var mLastTime = 0L
    fun checkUpdate(force: Boolean = true) {
        if (updateLiveData.value is LoadState.Loading) return
        if (!force && System.currentTimeMillis() < mLastTime + 3 * TimeConstants.MIN) return//防止更新接口请求太频繁，3分钟内才允许请求
        mLastTime = System.currentTimeMillis()
        requestLifeLaunch({
//            val result = UserRepository.checkUpdate()
//            if (result != null) {
//                updateLiveData.value = LoadState.Success(data = result)
//            } else {
//                updateLiveData.value = LoadState.Fail()
//            }
        }, onStart = {
            updateLiveData.value = LoadState.Loading()
        }, onError = { e ->
            updateLiveData.value = LoadState.Fail(exc = e)
            mLastTime = 0//请求失败，解除3分钟限制
        })
    }

    /**
     * 获取收藏数据
     * MsgType.MESSAGETYPE_TEXT -> "text"
     * MsgType.MESSAGETYPE_VOICE -> "voiceMsg"
     * MsgType.MESSAGETYPE_VIDEO -> "videoMsg"
     * MsgType.MESSAGETYPE_PICTURE -> "picture"
     */
    fun getCollectData() {
        requestLifeLaunch({
            val result = CollectRepository.getCollectList("1")
            if (result.code == SUCCESS) {
                val list = result.data.records
                if (list != null && list.size > 0) {
                    //最近一条收藏数据
                    val recentMsg = list[0]
                    //转化成msg的类型
                    val msgType = recentMsg.type
                    //更新最新一条收藏消息数据
                    ChatDao.getConversationDb()
                        .updateCollectLastMsg(
                            msgType,
                            recentMsg.content,
                            TimeUtils.strToLong(recentMsg.createTime),
                            isSendEvent = false
                        )
                }
            }
        }, {
            it.printStackTrace()
        })
    }

    /**
     * 获取oss配置
     */
    fun getOssConfig() {
        requestLifeLaunch({
            val ossConfig = VPNRepository.getIMOssConfig()
            if(ossConfig.startsWith("[")){
                //返回的数组
                val jsonArray = JSONArray(ossConfig)
                val jsonObject = jsonArray.optJSONObject(0)
                val hostUrl = jsonObject.optString("a")
                val wsUrl = jsonObject.optString("w")

                if (!TextUtils.isEmpty(hostUrl)) {
                    MMKVUtils.putString(EventKeys.BASE_HOSTURL + ApiUrl.currentType, hostUrl)
                }

                if (!TextUtils.isEmpty(wsUrl)) {
                    MMKVUtils.putString(EventKeys.WS_HOSTURL + ApiUrl.currentType, wsUrl)
                }
            }else{
                //返回的json对象
                val jsonObject = JSONObject(ossConfig)
                val hostUrl = jsonObject.optString("a")
                val wsUrl = jsonObject.optString("w")

                if (!TextUtils.isEmpty(hostUrl)) {
                    MMKVUtils.putString(EventKeys.BASE_HOSTURL + ApiUrl.currentType, hostUrl)
                }

                if (!TextUtils.isEmpty(wsUrl)) {
                    MMKVUtils.putString(EventKeys.WS_HOSTURL + ApiUrl.currentType, wsUrl)
                }
            }
        }, {
            it.printStackTrace()
        })
    }
}