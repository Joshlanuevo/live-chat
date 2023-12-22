package com.ym.chat.service

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logD
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.utils.ACache
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.MD5
import com.ym.chat.utils.MsgType
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import rxhttp.RxHttp
import rxhttp.toFlow
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.utils.GsonUtil
import java.io.File
import java.util.*

/**
 * 发送媒体消息的work
 *
 */
class SendMediaWork(
    val context: Context,
    parameters: WorkerParameters
) :
    Worker(context, parameters) {

    override fun doWork(): Result {

        //获取输入参数
        val localPathStr = inputData.getString("localPath")
        val toId = inputData.getString("toId")
        val chatType = inputData.getString("chatType")
        val fileType = inputData.getString("fileType")
        val fileSize = inputData.getString("fileSize")
        val width = inputData.getInt("width", 0)
        val height = inputData.getInt("height", 0)
        val isResend = inputData.getBoolean("isResend", false)
        val msgId = inputData.getLong("msgId", 0)
        val parenMsgId = inputData.getString("parenMsgId")

        if (isResend) {
            //重发消息
            reSendMsg(msgId, parenMsgId)
        } else {
            //正常发送
            normalSend(localPathStr, toId, chatType, fileType, width, height, parenMsgId, fileSize)
        }
        return Result.success()
    }

    /**
     * 重发媒体消息
     */
    private fun reSendMsg(msgId: Long?, parenMsgId: String?) {
        msgId?.let {

            //从数据库查询该消息
            ChatDao.getChatMsgDb().getMsgByDbId(msgId)?.let { chatMessageBean ->
                //修改发送状态
                ChatDao.getChatMsgDb().updateMsgSendState(
                    0,
                    chatMessageBean.to,
                    chatMessageBean.from,
                    chatMessageBean.createTime
                )

                //发送消息的参数
                val to = if (chatMessageBean.chatType == ChatType.CHAT_TYPE_FRIEND) {
                    chatMessageBean.to
                } else {
                    chatMessageBean.groupId
                }
                val params = createSendParams(
                    chatMessageBean.localPath,
                    to,
                    chatMessageBean.chatType
                ).apply {
                    msgType = chatMessageBean.msgType
                    parentMessageId = chatMessageBean.parentMessageId
                }

                val cacheUrl =
                    ACache.get(Utils.getApp()).getAsString(MD5.MD516(chatMessageBean.localPath))
                if (!TextUtils.isEmpty(cacheUrl)) {
                    //有缓存，直接取缓存使用
                    val result =
                        GsonUtil.fromJson<UploadResultBean>(cacheUrl, UploadResultBean::class.java)
                    when (chatMessageBean.msgType) {
                        MsgType.MESSAGETYPE_VIDEO -> {
                            //视频消息
                            val width = chatMessageBean.videoInfo?.width
                            val height = chatMessageBean.videoInfo?.height
                            sendVideoMsg(
                                result,
                                width ?: 0,
                                height ?: 0,
                                params,
                                chatMessageBean,
                                parenMsgId
                            )
                        }
                        MsgType.MESSAGETYPE_PICTURE -> {
                            //图片消息
                            val imageMsg =
                                GsonUtils.fromJson(chatMessageBean.content, ImageBean::class.java)
                            sendImageMsg(
                                result,
                                imageMsg.width,
                                imageMsg.height,
                                params,
                                chatMessageBean,
                                parenMsgId
                            )
                        }
                        MsgType.MESSAGETYPE_VOICE -> {
                            //音频消息
                            val jsonObject = JSONObject(chatMessageBean.content)
                            val time = jsonObject.optInt("time")
                            sendAudioMsg(result, params, chatMessageBean, time ?: 0, parenMsgId)
                        }
                        MsgType.MESSAGETYPE_FILE -> {
                            //文件消息
                            val fileMsg =
                                GsonUtils.fromJson(chatMessageBean.content, FileMsgBean::class.java)
                            sendFileMsg(result, fileMsg.size, params, chatMessageBean, parenMsgId)
                        }
                    }
                } else {
                    val fileType = when (chatMessageBean.msgType) {
                        MsgType.MESSAGETYPE_VIDEO -> {
                            //视频消息
                            "Video"
                        }
                        MsgType.MESSAGETYPE_PICTURE -> {
                            //图片消息
                            "Picture"
                        }
                        MsgType.MESSAGETYPE_VOICE -> {
                            //音频消息
                            "Audio"
                        }
                        MsgType.MESSAGETYPE_FILE -> {
                            //文件消息
                            "File"
                        }
                        else -> ""
                    }
                    uploadFile(chatMessageBean.localPath, fileType ?: "", {
                        //通知页面更新上传进度
                        chatMessageBean.fileUploadProgress = it.progress
                        LiveEventBus.get(
                            EventKeys.EVENT_UPLOAD_PROGRESS,
                            ChatMessageBean::class.java
                        )
                            .post(chatMessageBean)
                    }, { result ->
                        when (chatMessageBean.msgType) {
                            MsgType.MESSAGETYPE_VIDEO -> {
                                //视频消息
                                val width = chatMessageBean.videoInfo?.width
                                val height = chatMessageBean.videoInfo?.height
                                sendVideoMsg(
                                    result,
                                    width ?: 0,
                                    height ?: 0,
                                    params,
                                    chatMessageBean,
                                    parenMsgId
                                )
                            }
                            MsgType.MESSAGETYPE_PICTURE -> {
                                //图片消息
                                val imageMsg =
                                    GsonUtils.fromJson(
                                        chatMessageBean.content,
                                        ImageBean::class.java
                                    )
                                sendImageMsg(
                                    result,
                                    imageMsg.width,
                                    imageMsg.height,
                                    params,
                                    chatMessageBean,
                                    parenMsgId
                                )
                            }
                            MsgType.MESSAGETYPE_VOICE -> {
                                //音频消息
                                val jsonObject = JSONObject(chatMessageBean.content)
                                val time = jsonObject.optInt("time")
                                sendAudioMsg(result, params, chatMessageBean, time ?: 0, parenMsgId)
                            }
                            MsgType.MESSAGETYPE_FILE -> {
                                //文件消息
                                val fileMsg =
                                    GsonUtils.fromJson(
                                        chatMessageBean.content,
                                        FileMsgBean::class.java
                                    )
                                sendFileMsg(
                                    result,
                                    fileMsg.size,
                                    params,
                                    chatMessageBean,
                                    parenMsgId
                                )
                            }
                        }
                    }, {
                        //上传失败
                        //消息发送状态置为发送失败
                        ChatDao.getChatMsgDb().updateMsgSendState(
                            2,
                            chatMessageBean.to,
                            chatMessageBean.from,
                            chatMessageBean.createTime
                        )
                    })
                }
            }
        }
    }

    /**
     * 正常发送消息
     */
    private fun normalSend(
        localPathStr: String?,
        toId: String?,
        chatType: String?,
        fileType: String?,
        width: Int,
        height: Int,
        parenMsgId: String?,
        fileSize: String?
    ) {
        //发送消息的参数
        val params = createSendParams(localPathStr ?: "", toId ?: "", chatType ?: "").apply {
            msgType = getMsgType(fileType ?: "")
            parentMessageId = parenMsgId ?: ""
        }

        //本地存储消息的参数
        val chatMsg = copyToChatMsgBean(params).apply {
            sendState = 0
            localPath = localPathStr ?: ""
            uuid = params.uuid
            if (!TextUtils.isEmpty(parenMsgId)) {
                parentMessageId = parenMsgId ?: ""
                operationType = MsgType.MESSAGETYPE_REPLY
            }
            content = if (msgType == MsgType.MESSAGETYPE_VOICE) {
                //语音消息
                GsonUtil.toJson(AudioMsgBean(width, localPathStr ?: ""))
            } else if (msgType == MsgType.MESSAGETYPE_PICTURE) {
                //图片消息
                GsonUtil.toJson(ImageBean(width, height).apply {
                    url = localPathStr ?: ""
                })
            } else if (msgType == MsgType.MESSAGETYPE_VIDEO) {
                //视频消息
                GsonUtil.toJson(VideoMsgBean(width, height).apply {
                    this.url = localPath ?: ""
                    this.coverUrl = localPathStr ?: ""
                })
            } else if (msgType == MsgType.MESSAGETYPE_FILE) {
                val index = localPath.lastIndexOf("/")
                val indexPoint = localPath.lastIndexOf(".")
                val name = localPath.substring(index + 1, localPath.length)
                val suffix = localPath.substring(indexPoint + 1, localPath.length)
                GsonUtil.toJson(FileMsgBean(suffix, name, fileSize ?: "").apply {
                    url = localPath
                })
            } else {
                ""
            }
        }
        try {
            //发送Event，预显示文件
            LiveEventBus.get(EventKeys.EVENT_SEND_MSG, ChatMessageBean::class.java).post(chatMsg)

            //保存到数据库
            val tempMsg = ChatDao.getChatMsgDb().saveChatMsg(chatMsg)

            val cacheUrl = ACache.get(Utils.getApp()).getAsString(MD5.MD516(localPathStr))
            if (!TextUtils.isEmpty(cacheUrl)) {
                //有缓存，直接取缓存使用
                val result =
                    GsonUtil.fromJson<UploadResultBean>(cacheUrl, UploadResultBean::class.java)
                when (fileType) {
                    "Video" -> {
                        //视频消息
                        sendVideoMsg(result, width, height, params, tempMsg, parenMsgId)
                    }
                    "Picture" -> {
                        //图片消息
                        sendImageMsg(result, width, height, params, tempMsg, parenMsgId)
                    }
                    "Audio" -> {
                        //音频消息
                        sendAudioMsg(result, params, tempMsg, width, parenMsgId)
                    }
                    "File" -> {
                        //文件消息
                        sendFileMsg(result, fileSize ?: "", params, chatMsg, parenMsgId)
                    }
                }
            } else {
                uploadFile(localPathStr ?: "", fileType ?: "", {
                    //通知页面更新上传进度
                    chatMsg.fileUploadProgress = it.progress
                    LiveEventBus.get(EventKeys.EVENT_UPLOAD_PROGRESS, ChatMessageBean::class.java)
                        .post(chatMsg)
                }, { result ->
                    if (result?.data == null) {
                        //上传文件与实际不匹配，上传失败
                        //消息发送状态置为发送失败
                        ChatDao.getChatMsgDb().updateMsgSendState(
                            2,
                            chatMsg.to,
                            chatMsg.from,
                            chatMsg.createTime
                        )
                    } else {
                        when (fileType) {
                            "Video" -> {
                                //视频消息
                                sendVideoMsg(result, width, height, params, chatMsg, parenMsgId)
                            }
                            "Picture" -> {
                                //图片消息
                                sendImageMsg(result, width, height, params, chatMsg, parenMsgId)
                            }
                            "Audio" -> {
                                //音频消息
                                sendAudioMsg(result, params, chatMsg, width, parenMsgId)
                            }
                            "File" -> {
                                //文件消息
                                sendFileMsg(result, fileSize ?: "", params, chatMsg, parenMsgId)
                            }
                        }
                    }
                }, {
                    //上传失败
                    //消息发送状态置为发送失败
                    ChatDao.getChatMsgDb().updateMsgSendState(
                        2,
                        chatMsg.to,
                        chatMsg.from,
                        chatMsg.createTime
                    )
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取消息类型
     */
    private fun getMsgType(fileType: String): String {
        return when (fileType) {
            "Video" -> {
                //视频消息
                MsgType.MESSAGETYPE_VIDEO
            }
            "Picture" -> {
                //图片消息
                MsgType.MESSAGETYPE_PICTURE
            }
            "Audio" -> {
                MsgType.MESSAGETYPE_VOICE
            }
            "File" -> {
                MsgType.MESSAGETYPE_FILE
            }
            else -> ""
        }
    }

    /**
     * 发送图片消息
     */
    private fun sendImageMsg(
        result: UploadResultBean, width: Int, height: Int,
        params: SendMsgParams, chatMsg: ChatMessageBean, parenMsgId: String?
    ) {

        //保存远程文件地址
        chatMsg.servicePath = result.data.filePath
        chatMsg.isUpload = true

        //拼装图片消息
        val imgContent = GsonUtil.toJson(ImageBean(width, height).apply {
            url = result.data.filePath
        })
        params.content = imgContent
        chatMsg.content = imgContent

        //更新状态为已上传
        ChatDao.getChatMsgDb()
            .updateMsgFileUpload(chatMsg.to, chatMsg.from, chatMsg.createTime, true, imgContent)

        //生成会话列表
        if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
            ChatDao.getConversationDb().saveGroupConversation(
                chatMsg.groupId,
                chatMsg.content,
                chatMsg.msgType,
                fromId = MMKVUtils.getUser()?.id ?: ""
            )
        } else {
            ChatDao.getConversationDb().saveFriendConversation(
                chatMsg.to,
                chatMsg.content,
                chatMsg.msgType
            )
        }

        //保存消息到数据库
//        msgDb.saveChatMsg(chatMsg)

        //页面显示
//        sendMsgResult.value = chatMsg
//        "--发送图片消息==${GsonUtil.toJson(params)}".logD()
        //2、发送消息
        WebsocketWork.WS.sendMsg(
            GsonUtil.toJson(params), success = { msgId ->
                ChatDao.getChatMsgDb().updateMsgSendState(1, chatMsg.dbId, msgId)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSendState(2, chatMsg.dbId, "")
            }, parentMsgId = parenMsgId ?: ""
        )
    }

    /**
     * 发送语音文件消息
     */
    private fun sendAudioMsg(
        result: UploadResultBean,
        params: SendMsgParams,
        chatMsg: ChatMessageBean,
        time: Int,
        parenMsgId: String?
    ) {
        if (result.data != null) {
            params.content = result.data.filePath
        }
        //chatMsg.content = result.data.filePath
        chatMsg.isUpload = true

        //拼装图片消息
        val audioContent = GsonUtil.toJson(AudioMsgBean(time, result.data.filePath))
        params.content = audioContent
        chatMsg.content = audioContent

        //更新状态为已上传
        ChatDao.getChatMsgDb()
            .updateMsgFileUpload(chatMsg.to, chatMsg.from, chatMsg.createTime, true, audioContent)

        //生成会话列表
        if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
            ChatDao.getConversationDb().saveGroupConversation(
                chatMsg.groupId,
                chatMsg.content,
                chatMsg.msgType,
                fromId = MMKVUtils.getUser()?.id ?: ""
            )
        } else {
            ChatDao.getConversationDb().saveFriendConversation(
                chatMsg.to,
                chatMsg.content,
                chatMsg.msgType
            )
        }

        //页面显示
//        sendMsgResult.value = chatMsg
        "--发送语音消息==${GsonUtil.toJson(params)}".logD()
        //2、发送消息
        WebsocketWork.WS.sendMsg(
            GsonUtil.toJson(params), success = {
                ChatDao.getChatMsgDb().updateMsgSendState(1, chatMsg.dbId, it)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSendState(2, chatMsg.dbId, "")
            }, parentMsgId = parenMsgId ?: ""
        )
    }

    /**
     * 发送视频消息
     */
    private fun sendVideoMsg(
        result: UploadResultBean, width: Int, height: Int,
        params: SendMsgParams, chatMsg: ChatMessageBean, parenMsgId: String?
    ) {
        //拼装图片消息
        val videoBean = VideoMsgBean(
            width,
            height
        ).apply {
            this.url = result.data.filePath
            this.coverUrl = result.data.thumbnail
        }

        val audioContent = GsonUtil.toJson(videoBean)
        params.content = audioContent

        //更新状态为已上传
        ChatDao.getChatMsgDb()
            .updateMsgFileUpload(chatMsg.to, chatMsg.from, chatMsg.createTime, true, audioContent)

        //生成会话列表
        if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
            ChatDao.getConversationDb().saveGroupConversation(
                chatMsg.groupId,
                chatMsg.content,
                chatMsg.msgType,
                fromId = MMKVUtils.getUser()?.id ?: ""
            )
        } else {
            ChatDao.getConversationDb().saveFriendConversation(
                chatMsg.to,
                chatMsg.content,
                chatMsg.msgType
            )
        }
        "--发送视频消息==${GsonUtil.toJson(params)}".logD()
        //正式发送消息
        WebsocketWork.WS.sendMsg(
            GsonUtil.toJson(params), success = {
                ChatDao.getChatMsgDb().updateMsgSendState(1, chatMsg.dbId, it)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSendState(2, chatMsg.dbId, "")
            }, parentMsgId = parenMsgId ?: ""
        )
    }

    /**
     * 发送文件消息
     */
    private fun sendFileMsg(
        result: UploadResultBean, size: String,
        params: SendMsgParams, chatMsg: ChatMessageBean, parenMsgId: String?
    ) {
        //拼装图片消息
        val fileMsg = FileMsgBean(
            result.data.fileSuffix,
            result.data.fileName,
            size
        ).apply {
            this.url = result.data.filePath
        }

        val fileContent = GsonUtil.toJson(fileMsg)
        params.content = fileContent

        //更新状态为已上传
        ChatDao.getChatMsgDb()
            .updateMsgFileUpload(chatMsg.to, chatMsg.from, chatMsg.createTime, true, fileContent)

        //生成会话列表
        if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
            ChatDao.getConversationDb().saveGroupConversation(
                chatMsg.groupId,
                chatMsg.content,
                chatMsg.msgType,
                fromId = MMKVUtils.getUser()?.id ?: ""
            )
        } else {
            ChatDao.getConversationDb().saveFriendConversation(
                chatMsg.to,
                chatMsg.content,
                chatMsg.msgType
            )
        }
        "--发送文件消息==${GsonUtil.toJson(params)}".logD()
        //正式发送消息
        WebsocketWork.WS.sendMsg(
            GsonUtil.toJson(params), success = {
                ChatDao.getChatMsgDb().updateMsgSendState(1, chatMsg.dbId, it)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSendState(2, chatMsg.dbId, "")
            }, parentMsgId = parenMsgId ?: ""
        )
    }


    /**
     * 上传文件
     * 图片：
     * 语音：
     * 视频：
     */
    private fun uploadFile(
        path: String,
        fileType: String,
        progress: (Progress) -> Unit,
        success: (UploadResultBean) -> Unit,
        error: () -> Unit
    ) {
        //上传图片,并发送
        GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            try {
                RxHttp.postForm(ApiUrl.Chat.uploadFile)
                    .setCacheMode(CacheMode.ONLY_NETWORK)
                    .readTimeout(2 * 60 * 1000)
                    .writeTimeout(2 * 60 * 1000)
                    .connectTimeout(2 * 60 * 1000)
                    .add(
                        "fileType", if (fileType == "Audio" || fileType == "File") {
                            "Other"
                        } else {
                            fileType
                        }
                    )
                    .addFile("file", File(path))
                    .toFlow<UploadResultBean> {
                        //进度回调
                        progress?.invoke(it)
                        Log.d("上传文件", "上传进度${it.progress}")
                    }
                    .catch {
                        //异常回调
                        error?.invoke()
                        Log.d("上传文件", "上传失败")
                    }.collect { result ->
                        if (result.code == 200) {
                            //成功回调
                            if (fileType == "Video") {
                                ACache.get(Utils.getApp())
                                    .put(MD5.MD516(path), GsonUtil.toJson(result))
                            }
                            success.invoke(result)
                        } else {
                            error.invoke()
                            Log.d("上传文件", "上传失败")
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 创建发送消息的Bean
     */
    private fun createSendParams(contentStr: String, toId: String, type: String): SendMsgParams {
        return SendMsgParams().apply {
            cmd = 11
            content = contentStr
            from = MMKVUtils.getUser()?.id ?: ""
            uuid = UUID.randomUUID().toString()
            chatType = type
            when (type) {
                //单聊
                ChatType.CHAT_TYPE_FRIEND -> to = toId
                //群聊
                ChatType.CHAT_TYPE_GROUP -> groupId = toId
                //群发消息
                ChatType.CHAT_TYPE_GROUP_SEND -> chatType = ChatType.CHAT_TYPE_FRIEND
            }
        }
    }

    /**
     * 复制成chatMessageBean
     */
    private fun copyToChatMsgBean(sendMsgParams: SendMsgParams): ChatMessageBean {
        return ChatMessageBean(0).apply {
            from = sendMsgParams.from
            msgType = sendMsgParams.msgType
            chatType = sendMsgParams.chatType
            content = sendMsgParams.content
            groupId = sendMsgParams.groupId
            cmd = sendMsgParams.cmd
            uuid = sendMsgParams.uuid
            to = sendMsgParams.to
            createTime = sendMsgParams.createTime
            dir = 1
        }
    }
}