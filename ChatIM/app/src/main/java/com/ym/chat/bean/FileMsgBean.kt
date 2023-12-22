package com.ym.chat.bean

import com.ym.chat.rxhttp.ApiUrl

class FileMsgBean(
//    {"suffix": "","url": "","name": "","size": 100}
    val suffix: String = "",//后缀
    val name: String = "",//文件名
    val size: String = "",//文件大小
){
    //文件地址
    var url:String = ""
        get() {
            val host = ApiUrl.baseApiUrl
            val lastIndex1 = field.lastIndexOf("/")
            val lastIndex2 = field.lastIndexOf("/", lastIndex1-1)
            val fileName = field.substring(lastIndex2)
            return host +ApiUrl.suffix+ fileName
        }
}