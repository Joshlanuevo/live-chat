package com.ym.chat.bean

/**
 * @Description
 * @Author：CASE
 * @Date：2021-08-14
 * @Time：13:44
 */
class VideoMsgBean(
//    {"coverUrl":"","videoUrl": "", "time": 100, "size": 100} coverUrl:视频封面url size单位:kb   time单位:秒
    val url: String = "",//地址
    val coverUrl: String = "",//封面
    val width: Int = 0,//视频宽
    val height: Int = 0,//视频高
//    val time: Long = 0,//时长(s)
//    val size: Long = 0,//大小(kb)
)