package com.ym.chat.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.DelConBean
import com.ym.chat.bean.ReadConStateMsg
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.service.WebsocketWork.WS.isNeedReconnect
import com.ym.chat.utils.AesUtils
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.CommandType.NOTIFICATION_COUNT
import com.ym.chat.utils.CommandType.SEND_NOTIFICATION_UNREAD_CHAT_MSG
import com.ym.chat.utils.ImCache
import com.ym.chat.utils.ImConnectSatus
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URI

/**
 * ws 服务
 */
class WebsocketWork(val context: Context, val parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    private val TAG = "WebsocketService"

    private var mSendHeartJob: Job? = null//心跳包
    private var mReconnectJob: Job? = null//重连

    private var receiveHeartTime = 0L//收到心跳包时间

    private var mReconnectCount = 0 //重连次数

    private var isConnect = false//是否连接

    private var isReconnecting = false//是否正在重连中

    var connectStatus = ImConnectSatus.CONNECTING  //ws连接状态

    /**
     * ws链接服务
     */
    object WS {

        //ws连接
        @SuppressLint("StaticFieldLeak")
        var wsClient: ChatWebSocketClient? = null
        var isNeedReconnect: Boolean = true//是否需要重连,true需要重连，false不需要重连

        /**
         * 解密
         */
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
         * 发送消息
         */
        fun sendMsg(
            content: String,
            success: ((msgId: String) -> Unit)? = null,
            faile: (() -> Unit)? = null,
            parentMsgId: String = ""
        ) {

//            Log.d("ChatWebSocketClient", "发送加密前：$content")
            if (TextUtils.isEmpty(parentMsgId)) {
                //普通发消息
                normalSend(content, success, faile)
            } else {
                //发回复消息
                replySend(content, success, faile)
            }
        }

        /**
         * 回复发消息
         */
        private fun replySend(
            content: String, success: ((msgId: String) -> Unit)? = null,
            faile: (() -> Unit)? = null,
        ) {
            Log.d("ChatWebSocketClient", "加密前：$content")
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                val str = String(
                    AesUtils.encode(
                        MMKVUtils.getUser()?.id,
                        MMKVUtils.getUser()?.code,
                        content.toByteArray()
                    )
                )
                try {
                    val result = ChatRepository.messageReply(str)
                    val jsonObject = JSONObject(result)
                    val code = jsonObject.optInt("code")
                    if (code == 200) {
                        //发送成功
                        val data = jsonObject.optString("data")
                        val chatMsg =
                            GsonUtils.fromJson(decodeContent(data), ChatMessageBean::class.java)
                        //消息发送成功
                        success?.invoke(chatMsg.id)
                    } else {
                        //发送失败
                        faile?.invoke()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //消息发送状态置为发送失败
                    faile?.invoke()
                }
            }
        }

        /**
         * 普通发消息
         */
        private fun normalSend(
            content: String, success: ((msgId: String) -> Unit)? = null,
            faile: (() -> Unit)? = null,
        ) {
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                val str = String(
                    AesUtils.encode(
                        MMKVUtils.getUser()?.id,
                        MMKVUtils.getUser()?.code,
                        content.toByteArray()
                    )
                )
//                Log.d("ChatWebSocketClient", "发送加密后：$str")
                try {
                    val result = ChatRepository.sendMsg(str)
                    val jsonObject = JSONObject(result)
                    val code = jsonObject.optInt("code")
                    if (code == 200) {
                        //发送成功
                        val data = jsonObject.optString("data")
                        val chatMsg =
                            GsonUtils.fromJson(decodeContent(data), ChatMessageBean::class.java)
                        //消息发送成功
                        success?.invoke(chatMsg.id)
                    } else {
                        //发送失败
                        faile?.invoke()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //消息发送状态置为发送失败
                    faile?.invoke()
                }
            }
        }


        /**
         * 发送消息
         */
        fun modifyMsg(
            content: String,
            success: ((msgId: String) -> Unit)? = null,
            faile: (() -> Unit)? = null
        ) {
            ("modifyMsg  $content").logE()
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                val str = String(
                    AesUtils.encode(
                        MMKVUtils.getUser()?.id,
                        MMKVUtils.getUser()?.code,
                        content.toByteArray()
                    )
                )
                try {
                    val result = ChatRepository.modifyMsg(str)
                    val jsonObject = JSONObject(result)
                    val code = jsonObject.optInt("code")
                    if (code == 200) {
                        //发送成功
                        val data = jsonObject.optString("data")
                        val chatMsg =
                            GsonUtils.fromJson(decodeContent(data), ChatMessageBean::class.java)
                        //消息发送成功
                        success?.invoke(chatMsg.id)
                    } else {
                        //发送失败
                        faile?.invoke()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //消息发送状态置为发送失败
                    faile?.invoke()
                }
            }
        }

        /**
         * 发送消息
         */
        fun replayMsg(
            content: String,
            success: ((msgId: String) -> Unit)? = null,
            faile: (() -> Unit)? = null
        ) {
            Log.d("回复消息", "加密前==$content")
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                val str = String(
                    AesUtils.encode(
                        MMKVUtils.getUser()?.id,
                        MMKVUtils.getUser()?.code,
                        content.toByteArray()
                    )
                )
                try {
                    Log.d("回复消息", "加密后==$str")
                    val result = ChatRepository.messageReply(str)
                    val jsonObject = JSONObject(result)
                    val code = jsonObject.optInt("code")
                    if (code == 200) {
                        //发送成功
                        val data = jsonObject.optString("data")
                        val chatMsg =
                            GsonUtils.fromJson(decodeContent(data), ChatMessageBean::class.java)
                        //消息发送成功
                        success?.invoke(chatMsg.id)
                    } else {
                        //发送失败
                        faile?.invoke()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //消息发送状态置为发送失败
                    faile?.invoke()
                }
            }
        }


        /**
         * 发送获取 好友是否在线
         */
        fun sendFriendInLine(id: String) {
            wsClient?.run {
                var content = "{\"cmd\":32,\"type\":\"${"Friend"}\",\"memberId\":\"$id\"}"
                Log.d("ChatWebSocketClient", "发送获 好友 是否在线>>>$content")
                try {
                    send(encodeContent(content))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * 发送获取 群在线成员数
         */
        fun sendGroupInLineNum(id: String) {
            wsClient?.run {
                var content = "{\"cmd\":32,\"type\":\"${"Group"}\",\"groupId\":\"$id\"}"
                Log.d("ChatWebSocketClient", "发送获取群成员 在线人数>>>$content")
                try {
                    send(encodeContent(content))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun encodeContent(content: String): String? {
            return String(
                AesUtils.encode(
                    MMKVUtils.getUser()?.id,
                    MMKVUtils.getUser()?.code,
                    content.toByteArray()
                )
            )
        }

        /**
         * 发送已读回执
         */
        fun sendReadState(msgId: String, targetId: String, chatType: String, from: String) {
            wsClient?.run {
                val params = mutableMapOf<String, Any>().apply {
                    if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                        put("cmd", 27)
                        put("from", targetId)
                        put("to", MMKVUtils.getUser()?.id ?: "")
                        put("messageId", msgId)
                        put("chatType", chatType)
                    } else {
                        put("cmd", 27)
                        put("groupId", targetId)
                        put("to", MMKVUtils.getUser()?.id ?: "")
                        put("messageId", msgId)
                        put("chatType", chatType)
                        put("from", from)
                    }
                }
                val content = GsonUtils.toJson(params)
                Log.d("ChatWebSocketClient", "发送消息>>>${content}")
                val str = String(
                    AesUtils.encode(
                        MMKVUtils.getUser()?.id,
                        MMKVUtils.getUser()?.code,
                        content.toByteArray()
                    )
                )
                Log.d("ChatWebSocketClient", "发送消息密文>>>${str}")
                send(str)
            }
        }

        /**
         * 发送已读回执
         *
         * 操作类型：是否标记未读，默认为false：标记已读
         */
        fun sendReadConState(
            from: String,
            groupId: String,
            chatType: String,
            unRead: String,
        ) {
            wsClient?.run {
                val readMsg =
                    ReadConStateMsg(
                        SEND_NOTIFICATION_UNREAD_CHAT_MSG,
                        from,
                        MMKVUtils.getUser()?.id ?: "",
                        groupId,
                        chatType,
                        unRead
                    )
                val content = GsonUtils.toJson(readMsg)
                Log.d("ChatWebSocketClient", "发送消息>>>${content}")
                val str = String(
                    AesUtils.encode(
                        MMKVUtils.getUser()?.id,
                        MMKVUtils.getUser()?.code,
                        content.toByteArray()
                    )
                )
                Log.d("ChatWebSocketClient", "发送消息密文>>>${str}")
                try {
                    send(str)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * 发送系统通知已读回执
         */
        fun sendSysNotifyReadState(readIds: MutableList<String>) {
            if (readIds.size > 0) {
                wsClient?.run {
                    val map = mutableMapOf<String, Any>().apply {
                        put("messageIdList", readIds)
                        put("chatType", "SystemNotify")
                        put("cmd", NOTIFICATION_COUNT)
                    }
//                val readMsg =
//                    ReadStateMsg("27", targetId, MMKVUtils.getUser()?.id ?: "", msgId, chatType)
                    val content = GsonUtils.toJson(map)
                    Log.d("ChatWebSocketClient", "发送消息>>>${content}")
                    val str = String(
                        AesUtils.encode(
                            MMKVUtils.getUser()?.id,
                            MMKVUtils.getUser()?.code,
                            content.toByteArray()
                        )
                    )
                    Log.d("ChatWebSocketClient", "发送消息密文>>>${str}")
                    try {
                        send(str)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        }

        /**
         * 更新会话信息
         */
        fun updateConver(delConBean: DelConBean) {
            wsClient?.run {
                val content = GsonUtils.toJson(delConBean)
                Log.d("ChatWebSocketClient", "发送消息>>>${content}")
                val str = String(
                    AesUtils.encode(
                        MMKVUtils.getUser()?.id,
                        MMKVUtils.getUser()?.code,
                        content.toByteArray()
                    )
                )
                Log.d("ChatWebSocketClient", "发送消息密文>>>${str}")
                try {
                    send(str)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        /**
         * 关闭连接
         */
        fun close() {
            wsClient?.run {
//                send(MMKVUtils.getUser()?.id)
                wsClient?.close()
                isNeedReconnect = false
            }
        }
    }


//    override fun onStopped() {
//        super.onStopped()
//        //移除网络监听
//        NetworkUtils.unregisterNetworkStatusChangedListener(onNetworkStatusChangedListener)
//    }

    //网络监听
    private var onNetworkStatusChangedListener =
        object : NetworkUtils.OnNetworkStatusChangedListener {
            override fun onDisconnected() {
                postConnectState(ImConnectSatus.NET_ERROR)
            }

            override fun onConnected(networkType: NetworkUtils.NetworkType?) {
                //发起重连
                connectStatus = ImConnectSatus.CONNECT_FAIL
                if (connectStatus != ImConnectSatus.RECONNECT && connectStatus != ImConnectSatus.SUCCESS) {
                    isNeedReconnect = true
                    reConnection()
                }
            }
        }

    override suspend fun doWork(): Result {

        //注册网络监听
        NetworkUtils.registerNetworkStatusChangedListener(onNetworkStatusChangedListener)

        setForeground(createForegroundInfo("友聊"))

        //初始化websocket
        initWebSocket()

        return Result.success()
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = "33"
        val title = "友聊服务运行中"
//        val cancel = applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
//            .addAction(android.R.drawable.ic_delete, intent)
            .build()

        return ForegroundInfo(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val name = ChatUtils.getString(R.string.xiaoxi)
        val descriptionText = ChatUtils.getString(R.string.消息描述)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("33", name, importance).apply {
            description = descriptionText
        }
        channel.setShowBadge(false)
        // Register the channel with the system
        var notificationManager = Utils.getApp()
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }

    /**
     * webSocket
     */
    fun initWebSocket() {
        if (MMKVUtils.getToken()?.isNotEmpty() == true) {
            var url = ApiUrl.websocketUrl + "?token=${MMKVUtils.getToken()}"
            Log.d("ChatWebSocketClient", "ws连接url==$url")
            LiveEventBus.get(EventKeys.WS_STATUS, ImConnectSatus::class.java)
                .post(ImConnectSatus.CONNECTING)
            val uri = URI.create(url)
            WS.wsClient = ChatWebSocketClient(uri, context, onConnected = {

                //取消重连任务
                mReconnectJob?.cancel()
                postConnectState(ImConnectSatus.SUCCESS)

                //获取本地好友数据和群数据
                if (ImCache.isUpdateNotifyMsg)
                    ChatDao.syncFriendAndGroupToLocal {
                        //获取离线消息
//                    getOfflineMsg()
                    }

                //连接成功,开始发送心跳包
                receiveHeartTime = System.currentTimeMillis()
                sendHeartPack()
                //获取用户信息
//                getUserInfo()
            }, onReceiverHeart = {
                receiveHeartTime = System.currentTimeMillis()
            }, onClose = {
                //连接已断开
                mSendHeartJob?.cancel()
                postConnectState(ImConnectSatus.CLOSE)
                //开启自动重连
                reConnection()
            }, onError = {
                //连接出现异常
                mSendHeartJob?.cancel()
                postConnectState(ImConnectSatus.CONNECT_FAIL)
                //开启自动重连
                reConnection()
            }).apply {
                connect()
            }
        }
    }

    /**
     * 发送心跳包
     */
    private fun sendHeartPack() {
        mSendHeartJob = GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            repeat(Int.MAX_VALUE) {
                delay(10 * 1000)
                val currentTime = System.currentTimeMillis()
                val difTime = currentTime - receiveHeartTime
                if (WS.wsClient != null && WS.wsClient?.isOpen == true) {
                    //ws发送心跳包,固定格式
                    var hearStr = "{\"cmd\":13,\"hbbyte\":\"-127\"}"
                    Log.d("ChatWebSocketClient", "发送心跳包>>>$hearStr")
                    try {
                        WS.wsClient?.send(encodeContent(hearStr))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    //ws已断开
                    mSendHeartJob?.cancel()
                    postConnectState(ImConnectSatus.CLOSE)

                    //开始重连任务
                    reConnection()
                }
            }
        }
    }

    /**
     * 延迟开始重连
     */
    @Synchronized
    private fun reConnection() {

        //已经开启了重连任务
        if (mReconnectJob?.isActive == true || isReconnecting) {
            mReconnectJob?.cancel()
        }

        if (isNeedReconnect) {
            mReconnectJob = GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {

                repeat(Int.MAX_VALUE) {
                    delay(2 * 1000)
                    if (connectStatus == ImConnectSatus.NET_ERROR || !isNeedReconnect) {
                        //无网络，放弃连接
                        isReconnecting = false
                        Log.d(
                            "ChatWebSocketClient",
                            "取消重连$connectStatus==$isNeedReconnect" + "wrokId:${WebsocketServiceManager.workId}"
                        )
                        cancel()
                    } else {
                        if (!isConnect) {
                            isReconnecting = true
                            mReconnectCount++
                            Log.d(
                                "ChatWebSocketClient",
                                "websocket正在进行第" + mReconnectCount + "次重连" + "wrokId:${WebsocketServiceManager.workId}"
                            )
                            postConnectState(ImConnectSatus.RECONNECT)

                            try {
                                WS.wsClient?.reconnect()
                            } catch (e: Exception) {
                                cancel()
                                e.printStackTrace()
                            }
                        } else {
                            postConnectState(ImConnectSatus.SUCCESS)
                            cancel()
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送mqtt连接状态
     */
    private fun postConnectState(state: ImConnectSatus) {
        Log.d("ChatWebSocketClient", "ws状态==$state")
        connectStatus = state
        LiveEventBus.get(EventKeys.WS_STATUS, ImConnectSatus::class.java).post(state)
        when (connectStatus) {
            ImConnectSatus.SUCCESS -> {
                //更改标记位
                isReconnecting = false
                isNeedReconnect = false
                isConnect = true
                //重置重连次数
                mReconnectCount = 0
            }
            ImConnectSatus.CONNECT_FAIL, ImConnectSatus.CLOSE -> {
                isReconnecting = false
                isNeedReconnect = true
                isConnect = false
            }
            ImConnectSatus.CONNECTING -> {
                isReconnecting = true
                isNeedReconnect = false
                isConnect = false
            }
            ImConnectSatus.RECONNECT -> {
                isReconnecting = true
                isNeedReconnect = false
                isConnect = false
            }
            ImConnectSatus.NET_ERROR -> {
                isReconnecting = false
                isNeedReconnect = false
                isConnect = false
            }
        }
    }

    /**
     * 加密
     */
    private fun encodeContent(content: String): String {
        return String(
            AesUtils.encode(
                MMKVUtils.getUser()?.id,
                MMKVUtils.getUser()?.code,
                content.toByteArray()
            )
        )
    }
}