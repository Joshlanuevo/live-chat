package com.ym.chat.service

import android.content.Context
import androidx.work.*
import com.blankj.utilcode.util.Utils
import com.ym.chat.bean.RecordBean
import com.ym.chat.utils.ImCache
import java.util.*

object WebsocketServiceManager {

    var workId: UUID? = null
    var workTag: String = "websocketWork"
    private val sendMediaTask = mutableListOf<UUID>()

    /**
     * 上传gif图片
     */
    fun uploadGif(localPath: String, width: Int, heigh: Int) {

        //输入给work的参数
        val inputData: Data = Data.Builder()
            .putString("localPath", localPath)
            .putInt("width", width)
            .putInt("heigh", heigh)
            .build()

        val constraints: Constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)//有网络才执行
            .setRequiresBatteryNotLow(false) //指定设备电池是否不应低于临界阈值
            .setRequiresStorageNotLow(false) //指定设备可用存储是否不应低于临界阈值
            .build()
        val request =
            OneTimeWorkRequest.Builder(UploadGifWork::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        sendMediaTask.add(request.id)

        WorkManager.getInstance(Utils.getApp())
            .enqueueUniqueWork(workTag, ExistingWorkPolicy.APPEND, request)
    }

    /**
     * 重发媒体消息
     */
    fun reSendMediaMsg(msgDbId: Long) {

        //输入给work的参数
        val inputData: Data = Data.Builder()
            .putBoolean("isResend", true)
            .putLong("msgId", msgDbId)
            .build()

        val constraints: Constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)//有网络才执行
            .setRequiresBatteryNotLow(false) //指定设备电池是否不应低于临界阈值
            .setRequiresStorageNotLow(false) //指定设备可用存储是否不应低于临界阈值
            .build()
        val request =
            OneTimeWorkRequest.Builder(SendMediaWork::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        sendMediaTask.add(request.id)

        WorkManager.getInstance(Utils.getApp())
            .enqueueUniqueWork(workTag, ExistingWorkPolicy.APPEND, request)
    }

    /**
     * 发送媒体消息
     */
    fun sendMediaMsg(
        localPath: String, toId: String, chatType: String,
        fileType: String, w: Int = 0, h: Int = 0, parentMsgId: String = "",fileSize:String = ""
    ) {

        //输入给work的参数
        val inputData: Data = Data.Builder()
            .putString("localPath", localPath)
            .putString("toId", toId)
            .putString("chatType", chatType)
            .putString("fileType", fileType)
            .putString("fileSize", fileSize)
            .putInt("width", w)
            .putInt("height", h)
            .putString("parenMsgId", parentMsgId)
            .build()

        val constraints: Constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)//有网络才执行
            .setRequiresBatteryNotLow(false) //指定设备电池是否不应低于临界阈值
            .setRequiresStorageNotLow(false) //指定设备可用存储是否不应低于临界阈值
            .build()
        val request =
            OneTimeWorkRequest.Builder(SendMediaWork::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        sendMediaTask.add(request.id)

        WorkManager.getInstance(Utils.getApp())
            .enqueueUniqueWork(workTag, ExistingWorkPolicy.APPEND, request)
    }

    //<editor-fold defaultstate="collapsed" desc="发送收藏里面的媒体消息">
    /**
     * 重发 收藏媒体消息
     */
    fun reSendMediaMsg(msg: RecordBean) {
        //输入给work的参数
        val inputData: Data = Data.Builder()
            .putBoolean("isResend", true)
            .putString("content", msg.content)
            .putString("fileType", msg.type)
            .putString("createTime", msg.createTime)
            .build()

        val constraints: Constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)//有网络才执行
            .setRequiresBatteryNotLow(false) //指定设备电池是否不应低于临界阈值
            .setRequiresStorageNotLow(false) //指定设备可用存储是否不应低于临界阈值
            .build()
        val request =
            OneTimeWorkRequest.Builder(SendCollectMediaWork::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        sendMediaTask.add(request.id)

        WorkManager.getInstance(Utils.getApp())
            .enqueueUniqueWork(workTag, ExistingWorkPolicy.APPEND, request)
    }

    /**
     * 发送收藏媒体消息
     */
    fun sendCollectMediaMsg(
        localPath: String,
        fileType: String, w: Int = 0, h: Int = 0
    ) {

        //输入给work的参数
        val inputData: Data = Data.Builder()
            .putString("localPath", localPath)
            .putString("fileType", fileType)
            .putInt("width", w)
            .putInt("height", h)
            .build()

        val constraints: Constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)//有网络才执行
            .setRequiresBatteryNotLow(false) //指定设备电池是否不应低于临界阈值
            .setRequiresStorageNotLow(false) //指定设备可用存储是否不应低于临界阈值
            .build()
        val request =
            OneTimeWorkRequest.Builder(SendCollectMediaWork::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        sendMediaTask.add(request.id)

        WorkManager.getInstance(Utils.getApp())
            .enqueueUniqueWork(workTag, ExistingWorkPolicy.APPEND, request)
    }
    //</editor-fold>

    /**
     *连接ws
     */
    fun connect(context: Context) {
//        val intent = Intent(context, WebsocketService::class.java)
//        intent.action = WebsocketService.WS.CONNECT
//        startService(context, intent)

        val constraints: Constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)//有网络才执行
            .setRequiresBatteryNotLow(false) //指定设备电池是否不应低于临界阈值
            .setRequiresStorageNotLow(false) //指定设备可用存储是否不应低于临界阈值
            .build()
        val request =
            OneTimeWorkRequest.Builder(WebsocketWork::class.java)
                .setConstraints(constraints)
                .addTag("websocket")
                .build()
        workId = request.id

        WorkManager.getInstance(context)
            .enqueueUniqueWork(workTag, ExistingWorkPolicy.REPLACE, request)
    }

    /**
     *重连接ws
     */
    fun reConnect(context: Context) {
//        val intent = Intent(context, WebsocketService::class.java)
//        intent.action = WebsocketService.WS.RECONNECT
//        startService(context, intent)
    }

    /**
     * 关闭ws连接，当退出登陆时需要调用
     */
    fun closeConnect(context: Context) {
        //断开连接
        WebsocketWork.WS.close()
//        WorkManager.getInstance(context).cancelAllWorkByTag("websocket")
//        workId?.let {
//            WorkManager.getInstance(context).cancelWorkById(it)
//        }

//        val intent = Intent(context, WebsocketService::class.java)
//        intent.action = WebsocketService.WS.CLOSE
//        startService(context, intent)
    }
}