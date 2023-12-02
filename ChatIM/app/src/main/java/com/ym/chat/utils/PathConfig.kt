package com.ym.chat.utils

import com.blankj.utilcode.util.PathUtils
import java.io.File

/**
 * @Description
 * @Author：CASE
 * @Date：2021-04-26
 * @Time：13:18
 */
object PathConfig {
    val AUDIO_CACHE_DIR = PathUtils.getExternalAppCachePath() + File.separator + "Audio"
    //临时文件存放目录，如下载的APK等
    val TEMP_CACHE_DIR = PathUtils.getExternalAppCachePath() + File.separator + "Temp"
    //视频播放缓存地址
    val VIDEO_CACHE_DIR: String = PathUtils.getExternalAppMoviesPath() + File.separator + "VideoExo"
    //视频封面保存地址
    val VIDEO_COVER_CACHE_DIR = PathUtils.getExternalAppPicturesPath() + File.separator + "VideoCover"
    //临时压缩视频存放目录
    val TEMP_VIDEO_DIR = PathUtils.getExternalAppCachePath() + File.separator + "TempVideo"
    //聊天界面保存图片视频
    val GROUP_VIDEO_PICTURE_DIR = PathUtils.getExternalAppCachePath() + File.separator + "GroupVideoAndPicture"
}