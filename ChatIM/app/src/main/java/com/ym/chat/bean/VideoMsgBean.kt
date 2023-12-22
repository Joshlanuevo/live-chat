package com.ym.chat.bean

import com.ym.chat.rxhttp.ApiUrl

/**
 * @Description
 * @Author：CASE
 * @Date：2021-08-14
 * @Time：13:44
 */
class VideoMsgBean(
//    {"coverUrl":"","videoUrl": "", "time": 100, "size": 100} coverUrl:视频封面url size单位:kb   time单位:秒
    val width: Int = 0,//视频宽
    val height: Int = 0,//视频高
//    val time: Long = 0,//时长(s)
//    val size: Long = 0,//大小(kb)
) {
    //地址
    var url: String = ""
        get() {
            if (!field.startsWith("http")){
                return field
            }else{
                val host = ApiUrl.baseApiUrl
                val lastIndex1 = field.lastIndexOf("/")
                val lastIndex2 = field.lastIndexOf("/", lastIndex1-1)
                val fileName = field.substring(lastIndex2)
                return host + ApiUrl.suffix+fileName
            }
        }

    //封面
    var coverUrl: String = ""
        get() {
            try {
                if (field.startsWith("http")) {
                    val host = ApiUrl.baseApiUrl
                    val lastIndex1 = field.lastIndexOf("/")
                    val lastIndex2 = field.lastIndexOf("/", lastIndex1 - 1)
                    val fileName = field.substring(lastIndex2)
                    return host + ApiUrl.suffix + fileName
                } else {
                    return field
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return field
            }
        }
}