package com.ym.chat.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaMetadataRetriever
import com.blankj.utilcode.util.Utils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.ym.base.ext.*
import com.ym.base.image.ImageEngine
import com.ym.chat.R
import java.io.File
import java.io.FileInputStream

object ImageUtils {
    //去选择图片或者视频
    fun goSelImg(
        act: Activity,
        maxSelectNum: Int = 9,
        enableCrop: Boolean = false,//是否裁切
        isGif: Boolean = true,
        isWebP: Boolean = false,
        onResultCallBack: ((localPath: String, w: Int, h: Int, time: Long, listSize: Int) -> Unit)? = null,
    ) {
        //https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-Api%E8%AF%B4%E6%98%8E
        PictureSelector.create(act)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(ImageEngine())
            .selectionMode(PictureConfig.MULTIPLE)
            .maxSelectNum(maxSelectNum)
            .queryMaxFileSize(10f)//设置最大视频
            .isGif(isGif)
            .isWebp(isWebP)
            .isCamera(false) //列表是否显示拍照按钮
            .isEnableCrop(enableCrop)//是否裁切
            .circleDimmedLayer(enableCrop)//是否裁切圆形
            .freeStyleCropEnabled(enableCrop)// 裁剪框是否可拖拽
            .scaleEnabled(enableCrop)// 裁剪是否可放大缩小图片
            .isCompress(true)
            .setCircleDimmedBorderColor(R.color.app_yellow.xmlToColor())
            .isPageStrategy(true, PictureConfig.MAX_PAGE_SIZE, true) //过滤掉已损坏的图片
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(list: MutableList<LocalMedia>?) {
                    val localMedia = list?.takeIf { it.isNotEmpty() }
                    val time = System.currentTimeMillis()
                    localMedia?.forEach { img ->
                        img?.let {
                            onResultCallBack?.invoke(
                                loadNetImgOrSelectImg(it),
                                img.width,
                                img.height,
                                time,
                                list.size
                            )
                        }
                    }
                }

                override fun onCancel() {
                    ChatUtils.getString(R.string.已取消).toast()
                }

            })
    }

    //选择gif图
    fun goSelImgAndGif(
        act: Activity,
        maxSelectNum: Int,
        onResultCallBack: ((localPath: String, w: Int, h: Int, time: Long, size: Int) -> Unit)? = null
    ) {
        //https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-Api%E8%AF%B4%E6%98%8E
        PictureSelector.create(act)
            .openGallery(PictureMimeType.ofImage())
            .imageEngine(ImageEngine())
            .selectionMode(PictureConfig.MULTIPLE)
            .maxSelectNum(maxSelectNum)
            .queryMaxFileSize(10f)//设置最大视频
            .isGif(true)
            .isWebp(false)
            .isCamera(false) //列表是否显示拍照按钮
            .isEnableCrop(false)//是否裁切
            .circleDimmedLayer(false)//是否裁切圆形
            .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
            .scaleEnabled(false)// 裁剪是否可放大缩小图片
            .isCompress(true)
//            .querySpecifiedFormatSuffix(PictureMimeType.ofGIF())
            .setCircleDimmedBorderColor(R.color.app_yellow.xmlToColor())
            .isPageStrategy(true, PictureConfig.MAX_PAGE_SIZE, true) //过滤掉已损坏的图片
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(list: MutableList<LocalMedia>?) {
                    val localMedia = list?.takeIf { it.isNotEmpty() }
                    val time = System.currentTimeMillis()
                    localMedia?.forEach { img ->
                        img?.let {
                            onResultCallBack?.invoke(
                                loadNetImgOrSelectImg(it),
                                img.width,
                                img.height,
                                time, localMedia.size
                            )
                        }
                    }
                }

                override fun onCancel() {
                    ChatUtils.getString(R.string.已取消).toast()
                }
            })
    }

    //去拍照
    fun goCamera(
        act: Activity,
        enableCrop: Boolean = false,//是否裁切
        onResultCallBack: ((localPath: String, w: Int, h: Int, time: Long, listSize: Int) -> Unit)? = null
    ) {
        //https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-Api%E8%AF%B4%E6%98%8E
        PictureSelector.create(act)
            .openCamera(PictureMimeType.ofImage())
            .imageEngine(ImageEngine())
            .isGif(false)
            .isEnableCrop(enableCrop)//是否裁切
            .circleDimmedLayer(enableCrop)//是否裁切圆形
            .freeStyleCropEnabled(enableCrop)// 裁剪框是否可拖拽
            .scaleEnabled(enableCrop)// 裁剪是否可放大缩小图片
            .setCircleDimmedBorderColor(R.color.app_yellow.xmlToColor())
            .maxSelectNum(1)
            .queryMaxFileSize(10f)
            .isCompress(true)
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(list: MutableList<LocalMedia>?) {
                    val localMedia = list?.takeIf { it.isNotEmpty() }?.get(0)
                    if (localMedia == null) {
                        ChatUtils.getString(R.string.选择的结果有误).toast()
                        null
                    } else {
                        val localMedia = list?.takeIf { it.isNotEmpty() }
                        val time = System.currentTimeMillis()
                        localMedia?.forEach { img ->
                            img?.let {
                                onResultCallBack?.invoke(
                                    loadNetImgOrSelectImg(it),
                                    img.width,
                                    img.height,
                                    time,
                                    localMedia.size
                                )
                            }
                        }
                    }
                }

                override fun onCancel() {
                    ChatUtils.getString(R.string.已取消).toast()
                }

            })
    }

    /**
     * 录制视频
     */
    fun goRecordVideo(
        act: Activity,
        onResultCallBack: ((localPath: String, w: Int, h: Int, time: Long) -> Unit)? = null
    ) {
        val permissions = mutableListOf(Permission.CAMERA, Permission.RECORD_AUDIO)
        val has = XXPermissions.isGranted(act, permissions)
        if (!has) {
            XXPermissions.with(act)
                .permission(permissions)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                        if (all) {
                            startRecordVideo(act, onResultCallBack)
                        }
                    }

                    override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                        permissions?.let { list ->
                            list.firstOrNull()?.let { p ->
                                when (p) {
                                    Permission.RECORD_AUDIO -> {
                                        "需要打开录音权限".toast()
                                        XXPermissions.startPermissionActivity(
                                            act,
                                            Permission.RECORD_AUDIO
                                        )
                                    }
                                    Permission.CAMERA -> {
                                        ChatUtils.getString(R.string.需要打开拍照权限).toast()
                                        XXPermissions.startPermissionActivity(
                                            act,
                                            Permission.CAMERA
                                        )
                                    }
                                    else -> {

                                    }
                                }
                            }
                        }
                    }
                })
        } else {
            startRecordVideo(act, onResultCallBack)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun startRecordVideo(
        act: Activity,
        onResultCallBack: ((localPath: String, w: Int, h: Int, time: Long) -> Unit)?
    ) {
        //https://github.com/LuckSiege/PictureSelector/wiki/PictureSelector-Api%E8%AF%B4%E6%98%8E
        PictureSelector.create(act)
            .openGallery(PictureMimeType.ofVideo())
            .imageEngine(ImageEngine())
            .isCamera(true)
            .queryMaxFileSize(16f)//设置最大视频
            .isPageStrategy(true, PictureConfig.MAX_PAGE_SIZE, true) //过滤掉已损坏的
            .maxSelectNum(1)
            .recordVideoSecond(30)
            .videoMaxSecond(31)
            .videoMinSecond(1)
            .isPreviewVideo(true)
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(list: MutableList<LocalMedia>?) {
                    val localMedia = list?.firstOrNull()
                    if (localMedia == null) {
                        ChatUtils.getString(R.string.选择的结果有误).toast()
                    } else {
                        val path = localMedia.path
                        val file = path.toFile()
                        if (file?.exists() == true) {

                            //读取视频尺寸和旋转角度
                            val mMetadataRetriever = MediaMetadataRetriever()
                            var widthVideo = localMedia.width
                            var heightVideo = localMedia.height
                            try {
                                mMetadataRetriever.setDataSource(file.path)
                                val videoRotation =
                                    mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                                        ?: "0"
                                val videoHeight =
                                    mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                                        ?: "0"
                                val videoWidth =
                                    mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                                        ?: "0"
                                mMetadataRetriever.release()
                                widthVideo =
                                    if (Integer.parseInt(videoRotation) == 90 || Integer.parseInt(
                                            videoRotation
                                        ) == 270
                                    ) {
                                        //角度不对需要宽高调换
                                        heightVideo = videoWidth.toInt()
                                        videoHeight.toInt()
                                    } else {
                                        heightVideo = videoHeight.toInt()
                                        videoWidth.toInt()
                                    }

                                onResultCallBack?.invoke(
                                    file.path,
                                    widthVideo,
                                    heightVideo,
                                    localMedia.duration / 1000
                                )
                                "-----选择视频的大小 size=${getFileSize(file) / 1024 / 1024}M---time=${localMedia.duration / 1000}秒".logD()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onCancel() {
                    ChatUtils.getString(R.string.已取消).toast()
                }

            })
    }

    fun loadNetImgOrSelectImg(localMedia: LocalMedia): String {
        return if (localMedia != null) {
            val media = localMedia!!
            when {
                // 裁剪过
                media.isCut && !media.isCompressed -> media.cutPath
                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                media.isCompressed -> media.compressPath
                // AndroidQ特有path
                media.androidQToPath.isNotEmpty() -> media.androidQToPath
                // 原图
                else -> media.path
            }
        } else {
            ""
        }
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private fun getFileSize(file: File): Long {
        return try {
            var size: Long = 0
            if (file.exists()) {
                var fis = FileInputStream(file)
                size = fis.available()?.toLong() ?: 0L
            } else {
                file.createNewFile()
            }
            size
        } catch (e: Exception) {
            0
        }
    }
}