package com.ym.chat.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.utils.ACache
import com.ym.chat.utils.MD5
import com.ym.chat.utils.MsgType
import com.ym.chat.viewmodel.CollectModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import rxhttp.RxHttp
import rxhttp.toFlow
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.utils.GsonUtil
import java.io.File

/**
 * 发送收藏媒体消息的work
 *
 */
class SendCollectMediaWork(
    val context: Context,
    parameters: WorkerParameters
) :
    Worker(context, parameters) {

    private var mViewModel = CollectModel()

    override fun doWork(): Result {
        //获取输入参数
        val localPathStr = inputData.getString("localPath")
        val fileType = inputData.getString("fileType")
        val width = inputData.getInt("width", 0)
        val height = inputData.getInt("height", 0)
        val isResend = inputData.getBoolean("isResend", false)
        val content = inputData.getString("content")
        val createTime = inputData.getString("createTime")

        if (isResend) {
            //重发消息
            reSendMsg(content, createTime, fileType, width, height)
        } else {
            //正常发送
            normalSend(localPathStr, fileType, width, height)
        }
        return Result.success()
    }

    /**
     * 重发媒体消息
     */
    private fun reSendMsg(
        content: String?,
        createTime: String?,
        fileType: String?,
        width: Int = 0,
        height: Int = 0
    ) {
        //发送消息的参数
        val params = createSendParams(
            content ?: "", createTime?.toLong() ?: 0
        ).apply {
            msgType = fileType ?: ""
        }

        //本地存储消息的参数
        val collectMsg = copyToRecordBean(params).apply {
            sendState = 0
            fileUploadProgress = 0
            try {
                localPath = if (type == MsgType.MESSAGETYPE_VOICE) {
                    //语音消息
                    GsonUtils.fromJson(content ?: "{}", AudioMsgBean::class.java).url
                } else if (type == MsgType.MESSAGETYPE_PICTURE) {
                    //图片消息
                    GsonUtils.fromJson(content ?: "{}", ImageBean::class.java).url
                } else if (type == MsgType.MESSAGETYPE_VIDEO) {
                    //视频消息
                    GsonUtils.fromJson(content ?: "{}", VideoMsgBean::class.java).url
                } else if (type == MsgType.MESSAGETYPE_FILE) {
                    //文件消息
                    GsonUtils.fromJson(content ?: "{}", FileMsgBean::class.java).url
                } else {
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//        LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
//            .post(collectMsg)
        //存储到本地表
        ChatDao.getCollectDb().addCollectMsg(collectMsg)

        uploadFile(collectMsg.localPath ?: "", fileType ?: "", {
            //通知页面更新上传进度
            collectMsg.fileUploadProgress = it.progress
            LiveEventBus.get(EventKeys.EVENT_COLLECT_UPLOAD_PROGRESS, RecordBean::class.java)
                .post(collectMsg)
        }, { result ->
            if (result?.data == null) {
                //上传文件与实际不匹配，上传失败
                collectMsg.sendState = 2
                //消息发送状态置为发送失败
                LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                    .post(collectMsg)
            } else {
                when (fileType) {
                    "Video" -> {
                        //视频消息
                        sendVideoMsg(result, width, height, params, collectMsg)
                    }
                    "Picture" -> {
                        //图片消息
                        sendImageMsg(result, width, height, params, collectMsg)
                    }
                    "Audio" -> {
                        //音频消息
                        sendAudioMsg(result, params, collectMsg, width)
                    }
                    "File" -> {
                        //文件消息
                        sendFileMsg(result, params, collectMsg, FileUtils.getSize(collectMsg.localPath ?: ""))
                    }
                }
            }
        }, {
            //上传失败
            collectMsg.sendState = 2
            //消息发送状态置为发送失败
            LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                .post(collectMsg)
        })

    }

    /**
     * 正常发送消息
     */
    private fun normalSend(
        localPathStr: String?,
        fileType: String?,
        width: Int,
        height: Int
    ) {
        //发送消息的参数
        val params = createSendParams(localPathStr ?: "").apply {
            msgType = getMsgType(fileType ?: "")
        }

        //本地存储消息的参数
        val collectMsg = copyToRecordBean(params).apply {
            sendState = 0
            localPath = localPathStr ?: ""
            try {
                content = if (type == MsgType.MESSAGETYPE_VOICE) {
                    //语音消息
                    GsonUtil.toJson(AudioMsgBean(width, localPathStr ?: ""))
                } else if (type == MsgType.MESSAGETYPE_PICTURE) {
                    //图片消息
                    GsonUtil.toJson(ImageBean(width, height, localPathStr ?: ""))
                } else if (type == MsgType.MESSAGETYPE_VIDEO) {
                    //视频消息
                    GsonUtil.toJson(VideoMsgBean(localPath ?: "", localPathStr ?: "",width,height))
                } else if (type == MsgType.MESSAGETYPE_FILE) {
                    //文件消息
                    if (localPath != null && localPath?.isNotEmpty() == true) {
                        val index = localPath!!.lastIndexOf("/")
                        val indexPoint = localPath!!.lastIndexOf(".")
                        val name = localPath?.length?.let { localPath!!.substring(index + 1, it) }
                        val suffix =
                            localPath?.length?.let { localPath?.substring(indexPoint + 1, it) }
                        GsonUtil.toJson(suffix?.let {
                            FileMsgBean(
                                it,
                                localPath!!,
                                name ?: "",
                                FileUtils.getSize(localPath) ?: ""
                            )
                        })
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            } catch (e: Exception) {

            }
        }

        //发送Event，预显示文件
        LiveEventBus.get(EventKeys.EVENT_SEND_MSG, RecordBean::class.java).post(collectMsg)
        //存储到本地表
        ChatDao.getCollectDb().addCollectMsg(collectMsg)
        "---------存储本地-${collectMsg}".logE()

        uploadFile(localPathStr ?: "", fileType ?: "", {
            //通知页面更新上传进度
            collectMsg.fileUploadProgress = it.progress
            LiveEventBus.get(EventKeys.EVENT_COLLECT_UPLOAD_PROGRESS, RecordBean::class.java)
                .post(collectMsg)
        }, { result ->
            if (result?.data == null) {
                //上传文件与实际不匹配，上传失败
                collectMsg.sendState = 2
                //消息发送状态置为发送失败
                LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                    .post(collectMsg)
            } else {
                when (fileType) {
                    "Video" -> {
                        //视频消息
                        sendVideoMsg(result, width, height, params, collectMsg)
                    }
                    "Picture" -> {
                        //图片消息
                        sendImageMsg(result, width, height, params, collectMsg)
                    }
                    "Audio" -> {
                        //音频消息
                        sendAudioMsg(result, params, collectMsg, width)
                    }
                    "File" -> {
                        //文件消息
                        sendFileMsg(
                            result,
                            params,
                            collectMsg,
                            FileUtils.getSize(localPathStr) ?: ""
                        )
                    }
                }
            }
        }, {
            //上传失败
            collectMsg.sendState = 2
            //消息发送状态置为发送失败
            LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
                .post(collectMsg)
        })

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
                //语音消息
                MsgType.MESSAGETYPE_VOICE
            }
            "File" -> {
                //文件消息
                MsgType.MESSAGETYPE_FILE
            }
            else -> ""
        }
    }

    /**
     * 发送文件消息
     */
    private fun sendFileMsg(
        result: UploadResultBean,
        params: SendMsgParams, collectMsg: RecordBean, fileSize: String
    ) {
        "---------UploadResultBean=${result}".logD()
        if (result?.data != null) {
            //保存远程文件地址
            collectMsg.servicePath = result.data.filePath
            collectMsg.isUpload = true

            //拼装文件消息
            val fileContent = GsonUtil.toJson(
                FileMsgBean(
                    result.data.fileSuffix,
                    result.data.filePath,
                    result.data.fileName,
                    fileSize
                )
            )
            params.content = fileContent
            collectMsg.content = fileContent

            mViewModel.collect(collectMsg)
        }
    }

    /**
     * 发送图片消息
     */
    private fun sendImageMsg(
        result: UploadResultBean, width: Int, height: Int,
        params: SendMsgParams, collectMsg: RecordBean
    ) {
        //保存远程文件地址
        collectMsg.servicePath = result.data.filePath
        collectMsg.isUpload = true

        //拼装图片消息
        val imgContent = GsonUtil.toJson(ImageBean(width, height, result.data.filePath))
        params.content = imgContent
        collectMsg.content = imgContent

        mViewModel.collect(collectMsg)
    }

    /**
     * 发送语音文件消息
     */
    private fun sendAudioMsg(
        result: UploadResultBean,
        params: SendMsgParams,
        collectMsg: RecordBean, time: Int
    ) {
        if (result.data != null) {
            params.content = result.data.filePath
        }

        //拼装语音消息
        val audioContent = GsonUtil.toJson(AudioMsgBean(time, result.data.filePath))
        params.content = audioContent
        collectMsg.content = audioContent

        mViewModel.collect(collectMsg)
    }

    /**
     * 发送视频消息
     */
    private fun sendVideoMsg(
        result: UploadResultBean, width: Int, height: Int,
        params: SendMsgParams, collectMsg: RecordBean
    ) {
        //拼装视频消息
        val videoBean = VideoMsgBean(
            result.data.filePath,
            result.data.thumbnail,
            width,
            height
        )

        val audioContent = GsonUtil.toJson(videoBean)
        collectMsg.content = audioContent

        mViewModel.collect(collectMsg)
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
                        //成功回调
                        if (fileType == "Video") {
                            ACache.get(Utils.getApp()).put(MD5.MD516(path), GsonUtil.toJson(result))
                        }
                        success?.invoke(result)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 创建发送消息的Bean
     */
    private fun createSendParams(
        contentStr: String,
        createTime: Long = System.currentTimeMillis(),
    ): SendMsgParams {
        return SendMsgParams().apply {
            content = contentStr
            this.createTime = createTime
        }
    }

    /**
     * 复制成chatMessageBean
     */
    private fun copyToRecordBean(sendMsgParams: SendMsgParams): RecordBean {
        return RecordBean(id = "0").apply {
            memberId = sendMsgParams.from
            type = sendMsgParams.msgType
            content = sendMsgParams.content
            createTime = sendMsgParams.createTime.toString()
        }
    }
}