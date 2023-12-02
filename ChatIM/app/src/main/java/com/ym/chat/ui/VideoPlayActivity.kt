package com.ym.chat.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.EncryptUtils
import com.dylanc.viewbinding.binding
import com.ym.base.ext.*
import com.ym.base.mvvm.BaseActivity
import com.ym.base.rxhttp.RxHttpConfig
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityVideoPlayBinding
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.ext.getMimeType
import com.ym.chat.utils.PathConfig
import com.ym.chat.utils.WeChatImageUtils
import com.ym.chat.widget.video.MyExoPlayerFactory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import rxhttp.RxHttp
import rxhttp.toFlow
import xyz.doikki.videocontroller.StandardVideoController
import xyz.doikki.videoplayer.player.AndroidMediaPlayerFactory
import java.io.File

/**
 * @Description
 * @Author：CASE
 * @Date：2021-08-14
 * @Time：16:22
 */
class VideoPlayActivity : BaseActivity() {

    //<editor-fold defaultstate="collapsed" desc="外部跳转">
    companion object {
        private const val INTENT_KEY_URL = "INTENT_KEY_URL"
        fun startActivity(context: Context, url: String) {
            context.startActivity(Intent(context, VideoPlayActivity::class.java).apply {
                putExtra(INTENT_KEY_URL, url)
            })
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="XML">
    private val bindView: ActivityVideoPlayBinding by binding()
    private var videoUrl = ""

    //</editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        applyPermission()
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        //设置信号栏颜色值
        window.statusBarColor = Color.BLACK
        //设置状态栏字体颜色
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        (bindView.ivBack.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin =
            BarUtils.getStatusBarHeight()
        bindView.ivBack.pressEffectAlpha()
        bindView.ivBack.setColorFilter(Color.WHITE)
        bindView.ivBack.click { onBackPressed() }
        intent?.getStringExtra(INTENT_KEY_URL)?.let { url ->
            videoUrl = url
            bindView.videoView.setLooping(true)
            if (url.lowercase().startsWith("http")) {
                bindView.videoView.setPlayerFactory(MyExoPlayerFactory())
            } else {
                bindView.videoView.setPlayerFactory(AndroidMediaPlayerFactory())
            }
            bindView.videoView.setUrl(url) //设置视频地址
            val controller = StandardVideoController(mContext)
            controller.addDefaultControlComponent("", false)
            bindView.videoView.setVideoController(controller) //设置控制器
            bindView.videoView.start() //开始播放，不调用则不自动播放
        }
        if (MMKVUtils.isAdmin()) {
            bindView.ivSave.visible()
        }
        bindView.ivSave.click {
            if (checkPermission()) {
                "视频正在下载中...".toast()
                down(videoUrl)
            }
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }

    override fun onPause() {
        super.onPause()
        bindView.videoView.pause()
    }

    override fun onResume() {
        super.onResume()
        bindView.videoView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        bindView.videoView.release()
    }

    override fun onBackPressed() {
        if (!bindView.videoView.onBackPressed()) {
            super.onBackPressed()
        }
    }

    //<editor-fold defaultstate="collapsed" desc="下载文件相关">
    //文件下载保存的文件夹
    private val mFileDir = PathConfig.GROUP_VIDEO_PICTURE_DIR
    private fun down(downloadUrl: String) {
        var isOk = false
        val downLoadName = EncryptUtils.encryptMD5ToString("${System.currentTimeMillis()}")
        val fileName = "$downLoadName${downloadUrl.getMimeType()}"
        if (File(mFileDir, fileName).exists()) {
            "------文件已下载".logD()
            //已下载成功
            return
        }
        //RxHttp 下载
        val tempFile = File(mFileDir, downLoadName)
        val downSize = if (tempFile.exists()) tempFile.length() else 0L
        launchError {
            RxHttp.get(downloadUrl)
                .setOkClient(RxHttpConfig.getOkHttpClient().build()) //不要加log打印，否则文件太大要OOM
                .setRangeHeader(downSize) //设置开始下载位置，结束位置默认为文件末尾,如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                .toFlow(tempFile.path) { progress ->
                    //下载进度回调,0-100，仅在进度有更新时才会回调
                    val currentProgress = progress.progress //当前进度 0-100
                    val currentSize: Long = progress.currentSize  //当前已下载的字节大小
                    val totalSize = progress.totalSize //要下载的总字节大小
//                    "------文件--下载${currentProgress}".logE()
                }.catch {
                    //下载失败，处理相关逻辑
                    "------文件下载失败".logE()
                }.collect {
                    "------文件已下载成功".logD()
                    //下载成功，处理相关逻辑
                    isOk = WeChatImageUtils.saveGifFile(
                        this@VideoPlayActivity,
                        tempFile.path,
                        downloadUrl.getMimeType()
                    )
                    runOnUiThread(Runnable {
                        if (isOk) {
                            "视频已保存到本地相册".toast()
                        } else {
                            "----视频已保存到本地相册异常".logE()
                        }
                    })
                }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="获取存储权限相关">
    var permissions =
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
        )
    private var mPermissionList = mutableListOf<String>()
    private val PERMISSION_REQUEST = 1

    /***
     * android 11 以上手机必须要获取所有存储权限
     */
    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                "此手机是Android 11或更高的版本，且已获得访问所有文件权限".logD()
                applyPermission()
                true
            } else {
                "此手机是Android 11或更高的版本，且没有访问所有文件权限".logD()
                showHintDialog()
                false
            }
        } else {
            "此手机版本小于Android 11，版本为：API \${Build.VERSION.SDK_INT}，不需要申请文件管理权限".logD()
            applyPermission()
            true
        }
    }

    private fun applyPermission() {
        mPermissionList.clear()

        //判断哪些权限未授予
        for (i in permissions.indices) {
            if (ContextCompat.checkSelfPermission(this, permissions[i])
                != PackageManager.PERMISSION_GRANTED
            ) {
                mPermissionList.add(permissions[i])
            }
        }
        /**
         * 判断是否为空
         */
        if (mPermissionList.isEmpty()) { //未授予的权限为空，表示都授予了
            "-------权限已获取".logD()
        } else { //请求权限方法
            val permissions: Array<String> = mPermissionList.toTypedArray<String>() //将List转为数组
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
            "------重新请求权限-=${permissions.size}".logD()
        }
    }


    /**
     * 响应授权
     * 这里不管用户是否拒绝，都进入首页，不再重复申请权限
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                "------重新获取到权限".logD()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="提示dialog">
    var hintDialog: HintDialog? = null
    private fun showHintDialog() {
        if (hintDialog == null)
            hintDialog = HintDialog(
                getString(R.string.提示),
                getString(R.string.下载视频之前必须要获取),
                isShowBtnCancel = false,
                isCanTouchOutsideSet = false,
                iconId = R.drawable.ic_hint_delete,
                callback = object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                        hideHintLoading()
                    }
                }
            )
        hintDialog?.show(supportFragmentManager, "HintDialog")
    }

    private fun hideHintLoading() {
        try {
            hintDialog?.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //</editor-fold>
}