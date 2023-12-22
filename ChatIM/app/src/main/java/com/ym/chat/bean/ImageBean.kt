package com.ym.chat.bean

import com.ym.chat.rxhttp.ApiUrl

data class ImageBean(
    //图片宽
    val width: Int,
    //图片高
    val height: Int,
) {
    //图片地址
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
}