package com.ym.chat.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.chat.bean.AddEmojResultBean
import com.ym.chat.bean.EmojListBean
import com.ym.chat.bean.UploadResultBean
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.MeExpressionRepository
import com.ym.chat.service.WebsocketServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import rxhttp.RxHttp
import rxhttp.toFlow
import rxhttp.toOtherJson
import rxhttp.wrapper.entity.Progress
import java.io.File

class MeExpressionViewModel : BaseViewModel() {

    val emojListResult = MutableLiveData<MutableList<EmojListBean.EmojBean>>()

    //添加gif图
    val addEmojResult = MutableLiveData<LoadState<AddEmojResultBean>>()

    //删除gif图
    val delEmojResult = MutableLiveData<Boolean>()

    /**
     * 获取表情列表
     */
    fun getEmojList() {
        requestLifeLaunch({
            val emojResult = MeExpressionRepository.getEmojList()
            if (emojResult.code == SUCCESS) {
                emojListResult.value = emojResult.data.records
            } else {
                emojListResult.value = mutableListOf()
            }
        }, {
            emojListResult.value = mutableListOf()
            it.printStackTrace()
        })
    }

    /**
     * 删除gif
     */
    fun delEmoj(gifIds: MutableList<String>) {
        requestLifeLaunch({
            val emojResult = MeExpressionRepository.delEmoj(gifIds)
            if (emojResult.code == SUCCESS) {
                "删除成功".toast()
                delEmojResult.value = true
            } else {
                val dataStr = emojResult.data
                if (!TextUtils.isEmpty(dataStr)) {
                    dataStr.toast()
                } else {
                    "删除失败".toast()
                }
                delEmojResult.value = false
            }
        }, {
            delEmojResult.value = false
            it.printStackTrace()
        })
    }

    /**
     * 添加gif
     */
    fun addGif(imgPath: String, with: Int, height: Int) {
        //上传文件
        WebsocketServiceManager.uploadGif(imgPath, with, height)

//        requestLifeLaunch({
//
//            val result = RxHttp.postForm(ApiUrl.Chat.uploadFile)
//                .writeTimeout(10 * 1000)
//                .connectTimeout(10 * 1000)
//                .readTimeout(10 * 1000)
//                .setMultiMixed()
//                .add("fileType", "Picture")
//                .addFile("file", File(imgPath)).toOtherJson<UploadResultBean>().await()
//
//            if (result.code == SUCCESS) {
//                //成功回调
//                Log.d("上传文件", "上传成功==${result.data.filePath}")
//                val result =
//                    MeExpressionRepository.addEmojGIf(result.data.filePath, with, height)
//                if (result.code == SUCCESS) {
//                    "图片添加成功".toast()
//                    addEmojResult.value = LoadState.Success(result.data)
//                } else {
//                    addEmojResult.value = LoadState.Fail()
//                    "图片添加失败".toast()
//                }
//            } else {
//                "图片上传失败".toast()
//                addEmojResult.value = LoadState.Fail()
//            }
//
////            RxHttp.postForm(ApiUrl.Chat.uploadFile)
////                .writeTimeout(30 * 1000)
////                .connectTimeout(30 * 1000)
////                .readTimeout(30 * 1000)
////                .add("fileType", "Picture")
////                .addFile("file", File(imgPath))
////                .toFlow<UploadResultBean> {
////                    //进度回调
//////                    Log.d("上传文件", "上传进度${it.progress}")
////                }
////                .catch {
////                    //异常回调
////                    Log.d("上传文件", "上传失败")
////                    withContext(Dispatchers.Main) {
////                        "图片上传失败".toast()
////                        addEmojResult.value = LoadState.Fail()
////                    }
////                }.collect { result ->
////
////                }
//        }, onError = {
//            Log.d("上传文件", "上传失败$it")
//            "图片添加失败".toast()
//            addEmojResult.value = LoadState.Fail()
//            it.printStackTrace()
//        }, onStart = {
//        })
    }
}