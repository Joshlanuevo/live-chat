package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.rxhttp.CollectRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.service.WebsocketServiceManager
import com.ym.chat.service.WebsocketWork
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.StringExt.isAtMsg
import rxhttp.wrapper.utils.GsonUtil

/**
 * 收藏列表 Viewmodel
 */
class CollectModel : BaseViewModel() {

    //收藏列表数据回调
    val collectResult = MutableLiveData<LoadState<BaseBean<CollectBean>>>()

    //删除收藏列表数据回调
    val delCollectResult = MutableLiveData<LoadState<BaseBean<CollectBean>>>()

    //发送收藏信息
    val sendCollectResult = MutableLiveData<LoadState<BaseBean<RecordBean>>>()

    //消息发送状态更新
    val sendCollectMsgResult = MutableLiveData<RecordBean>()

    //批量编辑收藏消息
    val putCollectContent = MutableLiveData<LoadState<BaseBean<SimpleBean>>>()

    /**
     * 获取收藏数据列表
     */
    fun getCollectList(curPage: String, pageSize: String = "20") {
        if (collectResult.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = CollectRepository.getCollectList(curPage, pageSize)
            if (result.code == SUCCESS) {
                collectResult.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    collectResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    collectResult.value = LoadState.Fail(exc = Exception("获取收藏信息失败"))
                }
            }
        }, onError = {
            collectResult.value = LoadState.Fail(exc = Exception("获取收藏信息失败"))
        }, onStart = {
            collectResult.value = LoadState.Loading()
        })
    }

    /**
     * 批量删除收藏列表
     * 删除多个时，
     * @string  id与id用逗号隔开的字符串
     */
    fun delCollectList(favoriteIdList: MutableList<String>) {
        if (delCollectResult.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = CollectRepository.delCollectList(favoriteIdList)
            if (result.code == SUCCESS) {
                delCollectResult.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    delCollectResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    delCollectResult.value = LoadState.Fail(exc = Exception("删除收藏信息失败"))
                }
            }
        }, onError = {
            delCollectResult.value = LoadState.Fail(exc = Exception("删除收藏信息失败"))
        }, onStart = {
            delCollectResult.value = LoadState.Loading()
        })
    }

    /**
     * 批量编辑收藏内容
     * @string  id与id用逗号隔开的字符串
     */
    fun putCollectContent(content: String, favoriteIdList: MutableList<String>) {
        if (putCollectContent.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = CollectRepository.putCollectContent(content, favoriteIdList)
            if (result.code == SUCCESS) {
                putCollectContent.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    putCollectContent.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    putCollectContent.value = LoadState.Fail(exc = Exception("编辑收藏内容失败"))
                }
            }
        }, onError = {
            putCollectContent.value = LoadState.Fail(exc = Exception("编辑收藏内容失败"))
        }, onStart = {
            putCollectContent.value = LoadState.Loading()
        })
    }

    /**
     * 消息重发
     */
    fun reSendMsg(msgBean: RecordBean) {
        if (msgBean.type == MsgType.MESSAGETYPE_TEXT) {
            //删除本地存储的收藏消息
            ChatDao.getCollectDb().delCollectMsg(msgBean)
            //文本消息
            msgBean.sendState = 0
            msgBean.createTime = System.currentTimeMillis().toString()
            msgBean.createTimeEnd = msgBean.createTime
            LiveEventBus.get(EventKeys.EVENT_RESEND_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                .post(msgBean)
            collect(msgBean)
        } else {
            if (msgBean.isGif) {
                //删除本地存储的收藏消息
                ChatDao.getCollectDb().delCollectMsg(msgBean)
                //表情gif 图票消息
                msgBean.sendState = 0
                msgBean.createTime = System.currentTimeMillis().toString()
                msgBean.createTimeEnd = msgBean.createTime
                LiveEventBus.get(EventKeys.EVENT_RESEND_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                    .post(msgBean)
                collect(msgBean)
            } else {
                //删除本地存储的收藏消息
                ChatDao.getCollectDb().delCollectMsg(msgBean)
                //重发媒体消息
                msgBean.sendState = 0
                msgBean.createTime = System.currentTimeMillis().toString()
                msgBean.createTimeEnd = msgBean.createTime
                LiveEventBus.get(EventKeys.EVENT_RESEND_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                    .post(msgBean)
                WebsocketServiceManager.reSendMediaMsg(msgBean)
            }
        }
    }


    /**
     * 发送消息
     * content:消息内容
     * toFromId：对方id
     */
    fun sendMsg(contentStr: String) {
        //生成发送消息的Bean对象
        val params =
            createSendParams(contentStr, MMKVUtils.getUser()?.id ?: "", MsgType.MESSAGETYPE_TEXT)

        //消息保存到数据库，并且回调给聊天页面显示
        val collectMsg = copyToChatMsgBean(params).apply {
            //当前为发送中状态
            sendState = 0
        }
        sendCollectMsgResult.value = collectMsg
        collect(collectMsg)
    }

    /**
     * 发送文件消息
     */
    fun sendFileMsg(filePath: String, time: Int = 0) {
        //普通发消息，通过work完成文件上传，消息发送
        WebsocketServiceManager.sendCollectMediaMsg(filePath, "File", time, 0)
    }

    /**
     * 发送音频文件消息
     */
    fun sendAudioMsg(imgPath: String, time: Int) {
        //普通发消息，通过work完成文件上传，消息发送
        WebsocketServiceManager.sendCollectMediaMsg(imgPath, "Audio", time, 0)
    }

    /**
     * 发送视频消息
     */
    fun sendVideoMsg(
        imgPath: String, w: Int = 0, h: Int = 0
    ) {
        //普通发消息，通过work完成文件上传，消息发送
        WebsocketServiceManager.sendCollectMediaMsg(imgPath, "Video", w, h)
    }

    /**
     * 发送图片消息
     */
    fun sendImageMsg(
        imgPath: String, w: Int = 0, h: Int = 0
    ) {
        //普通发消息，通过work完成文件上传，消息发送
        WebsocketServiceManager.sendCollectMediaMsg(imgPath, "Picture", w, h)
    }

    /**
     * 发送表情gif图片消息
     */
    fun sendImageGifMsg(
        imgUrl: String, w: Int = 0, h: Int = 0
    ) {
        //拼装图片消息
        val imgContent = GsonUtil.toJson(ImageBean(w, h, imgUrl))
        //生成发送消息的Bean对象
        val params =
            createSendParams(imgContent, MMKVUtils.getUser()?.id ?: "", MsgType.MESSAGETYPE_PICTURE)

        //消息保存到数据库，并且回调给聊天页面显示
        val collectMsg = copyToChatMsgBean(params).apply {
            //当前为发送中状态
            sendState = 0
            isGif = true
        }
        sendCollectMsgResult.value = collectMsg
        collect(collectMsg)
    }


    /**
     * 收藏
     */
    fun collect(chatMsg: RecordBean) {
        requestLifeLaunch({
            //收藏
            val result = ChatRepository.collect(chatMsg.content, chatMsg.type)
            sendCollectResult.value = LoadState.Success(result)
            //生成会话列表
            ChatDao.getConversationDb()
                .updateCollectLastMsg(chatMsg.type, chatMsg.content, chatMsg.createTime.toLong())
            //删除本地存储的收藏消息
            ChatDao.getCollectDb().delCollectMsg(chatMsg)
            //上传成功
            chatMsg.sendState = 999
            chatMsg.id = result.data.id
            LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                .post(chatMsg)
        }, {
            it.printStackTrace()
            sendCollectResult.value = LoadState.Fail()
            chatMsg.sendState = 2
            LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                .post(chatMsg)
        }, {
            sendCollectResult.value = LoadState.Loading()
        })
    }

    /**
     * 创建发送消息的Bean
     */
    private fun createSendParams(
        contentStr: String,
        toId: String,
        msgTypeStr: String
    ): SendMsgParams {
        return SendMsgParams().apply {
            content = contentStr
            from = toId
            msgType = msgTypeStr
            createTime = System.currentTimeMillis()
        }
    }

    /**
     * 复制成chatMessageBean
     */
    private fun copyToChatMsgBean(sendMsgParams: SendMsgParams): RecordBean {
        return RecordBean(id = "0").apply {
            memberId = sendMsgParams.from
            type = sendMsgParams.msgType
            content = sendMsgParams.content
            createTime = sendMsgParams.createTime.toString()
        }
    }
}