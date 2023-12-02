package com.ym.chat.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.Utils
import com.ym.base.ext.logD
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.CollectBean
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.UploadResultBean
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.CollectRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.rxhttp.UserRepository
import com.ym.chat.utils.ACache
import com.ym.chat.utils.MD5
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import rxhttp.RxHttp
import rxhttp.toFlow
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.utils.GsonUtil
import java.io.File

/**
 * 收藏列表 Viewmodel
 */
class FeedbackModel : BaseViewModel() {

    //收藏列表数据回调
    val feedbackResult = MutableLiveData<LoadState<LoginBean>>()

    /**
     * 获取收藏数据列表
     */
    fun sendFeedBack(
        content: String,
        fileUrlList: List<String>,
        type: String) {
        if (feedbackResult.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = UserRepository.sendFeedBack(content=content, fileUrlList = fileUrlList, type = type)
            if (result.code == SUCCESS) {
                feedbackResult.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    feedbackResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    feedbackResult.value = LoadState.Fail(exc = Exception("建议反馈失败"))
                }
            }
        }, onError = {
            feedbackResult.value = LoadState.Fail(exc = Exception("建议反馈失败"))
        }, onStart = {
            feedbackResult.value = LoadState.Loading()
        })
    }


    /**
     * 上传文件
     * 图片：
     * 语音：
     * 视频：
     */
    fun uploadFile(
        path: String,
        fileType: String,
        progress: (Progress) -> Unit,
        success: (UploadResultBean) -> Unit,
        error: () -> Unit
    ) {
        //上传图片,并发送
        requestLifeLaunch({
            RxHttp.postForm(ApiUrl.Chat.uploadFile)
                .add("fileType", fileType)
                .addFile("file", File(path))
                .toFlow<UploadResultBean> {
                    //进度回调
                    progress?.invoke(it)
                }
                .catch {
                    //异常回调
                    error?.invoke()
                }.collect { result ->
                    //成功回调
                    success?.invoke(result)
                }
        }, onError = {
            error?.invoke()
            it.printStackTrace()
        }, onStart = {
        })
    }
}


