package com.ym.chat.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys.SENDGIF
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.chat.R
import com.ym.chat.bean.UploadResultBean
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.MeExpressionRepository
import com.ym.chat.utils.ACache
import com.ym.chat.utils.MD5
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import rxhttp.RxHttp
import rxhttp.toFlow
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.utils.GsonUtil
import java.io.File

/**
 * 发送媒体消息的work
 *
 */
class UploadGifWork(
    val context: Context,
    parameters: WorkerParameters
) :
    Worker(context, parameters) {

    override fun doWork(): Result {
        //获取输入参数
        val localPathStr = inputData.getString("localPath")
        val fileType = inputData.getString("fileType")
        val width = inputData.getInt("width", 0)
        val height = inputData.getInt("heigh", 0)

        uploadFile(localPathStr ?: "", "Picture", {
        }, success = {
            addEmojGif(it.data.filePath,it.data.thumbnail, width, height)
        }, error = {
            "GIF图片,上传失败！".logE()
            LiveEventBus.get(SENDGIF).post(false)
        })
        return Result.success()
    }

    /**
     * 添加gif
     */
    private fun addEmojGif(url: String, thumbnail: String, width: Int, heigh: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val result =
                MeExpressionRepository.addEmojGIf(url, thumbnail,width, heigh)
            if (result.code == 200) {
                LiveEventBus.get(SENDGIF).post(true)
            } else if(result.code == 40102){
                context.getString(R.string.表情包数量超出限制).toast()
            } else {
                "GIF图片张,上传失败！".logE()
                LiveEventBus.get(SENDGIF).post(false)
            }
        }
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
                        "fileType", if (fileType == "Audio") {
                            "Other"
                        } else {
                            fileType
                        }
                    )
                    .addFile("file", File(path))
                    .toFlow<UploadResultBean> {
                        //进度回调
                        progress?.invoke(it)
//                        Log.d("上传文件", "上传进度${it.progress}")
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
}