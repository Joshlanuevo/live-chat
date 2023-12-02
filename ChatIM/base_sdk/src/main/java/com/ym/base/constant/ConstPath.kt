package com.ym.base.constant

/**
 * Author:caiyoufei
 * Date:2016/7/26
 * Time:17:34
 */
object ConstPath {
    /**
     * 公司文件夹名称
     */
    const val COMPANY_FOLDER = "Download"

    /**
     * 公司文件夹名称
     */
    const val All_FOLDER = "Android"

    /**
     * APP文件夹名称
     */
    const val APP_FOLDER = "wallet"

    /**
     * 缓存的图片
     */
    const val IMAGES = "images"

    /**
     * 广告缓存的图片
     */
    const val AD_IMAGES = "ad_images"

    /**
     * 下载的文件(歌曲、APK等)
     */
    const val FILES = ".files"

    /**
     * 字幕文件信息
     */
    const val SUBTITLE = ".subtitles"

    /**
     * 滤镜文件(清理后重新解压)
     */
    const val FILTER = ".filter"

    /**
     * 录制相关文件夹 (非本地已合成的视频)
     */
    const val RECORDER = ".recorder"

    /**
     * 崩溃日志
     */
    const val CRASH = "crash"

    /**
     * 临时文件夹(用完之后会删除)
     */
    const val TEMP = "temp"

    /**
     * 本地保存的视频(不能被清除)
     */
    const val LOCAL_VIDEO = ".localVideo"

    /**
     * 裁切的图片
     */
    const val CROP = "crop"

    /**
     * 字幕资源默认文件夹
     */
    const val DEFAULT = ".default"

    /**
     * 缓存的视频文件(为了不让APP看到缓存视频,所以改名)每次打开APP自动删除太早的缓冲
     */
    const val VIDEO = ".default"

    /**
     * 阿里上传文件的目录
     */
    const val ALI_UPLOAD_FILE = "oss_record"

    /**
     * 秘钥Json 目录
     */
    const val KEY_STORE = "data"

    /**
     * BTC钱包文件 目录
     */
    const val BTC_STORE = "btcCach"

    /**
     * 更新文件夹 目录
     */
    const val UPDATA_APK = "updata"
}