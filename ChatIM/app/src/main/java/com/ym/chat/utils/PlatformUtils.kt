package com.ym.chat.utils

import com.ym.chat.BuildConfig
import com.ym.chat.R

/**
 * @version V1.0
 * @createAuthor
 * @createDate  2022/8/19 20:55
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2022 Technology Co., Ltd. Inc. All rights reserved.
 */
object PlatformUtils {

    /**
     * 获取oss地址
     */
    fun getOssUrl(): String {
       return "https://youdao-dev.oss-cn-hongkong.aliyuncs.com/youyou.json";
    }

    /**
     * 获取启动页图片
     */
    fun getSplashImage(): Int {
       return -1;
    }
}