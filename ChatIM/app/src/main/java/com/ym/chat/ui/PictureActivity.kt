package com.ym.chat.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.GsonUtils
import com.dylanc.viewbinding.binding
import com.luck.picture.lib.config.PictureSelectionConfig
import com.ym.base.ext.*
import com.ym.base.mvvm.BaseActivity
import com.ym.base.rxhttp.RxHttpConfig
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.ImageBean
import com.ym.chat.databinding.*
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.ext.getMimeType
import com.ym.chat.ui.fragment.PictureFragment
import com.ym.chat.utils.PathConfig
import com.ym.chat.utils.WeChatImageUtils
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import rxhttp.RxHttp
import rxhttp.toFlow
import java.io.File
import java.io.Serializable


/**
 * 查看图片
 */
class PictureActivity : BaseActivity() {
    private val bindView: ActivityPictureBinding by binding()
    private var mFragments = mutableListOf<PictureFragment>()
    private var index = 1 //显示的第几页
    private var maxPage = 1//总页数
    private var pictureList: MutableList<ChatMessageBean>? = null
    private var pictureListStr: MutableList<String>? = null
    private var picType = 0  //0私聊 1群聊 2我的收藏

    companion object {
        val PICTURE_URL = "picture_url"
        val PICTURE_TYPE = "picture_type" //0私聊 1群聊 2我的收藏
        val PICTURE_ID = "picture_id" // 群id 或者好友 ID
        val PICTURE_COLLECT_URL_LIST = "picture_collect_url_list"
        fun start(
            context: Context,
            pictureUrl: String,
            pictureType: Int = 2,
            pictureId: String? = null,
            pictureCollectUrlList: MutableList<String>? = null
        ) {
            val intent = Intent(context, PictureActivity::class.java)
            intent.putExtra(PICTURE_TYPE, pictureType)
            intent.putExtra(PICTURE_URL, pictureUrl)
            if (pictureId != null)
                intent.putExtra(PICTURE_ID, pictureId)
            if (pictureCollectUrlList != null)
                intent.putExtra(PICTURE_COLLECT_URL_LIST, pictureCollectUrlList as Serializable)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyPermission()
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        bindView.ivTitleBack.click { finish() }
    }

    override fun requestData() {
        intent.let {
            var picUrl = it.getStringExtra(PICTURE_URL)
            picType = it.getIntExtra(PICTURE_TYPE, 2)
            var picId = it.getStringExtra(PICTURE_ID)
            bindView.ivSave.visible()
            pictureListStr =
                it.getSerializableExtra(PICTURE_COLLECT_URL_LIST) as MutableList<String>
            pictureListStr?.forEachIndexed { i, u ->
                mFragments.add(PictureFragment(u))
                if (picUrl == u) {
                    index = i + 1
                }
            }
            maxPage = mFragments?.size!!
        }

        // 页面滑动事件监听
        bindView.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                index = position + 1
                bindView.tvPicIndex.text = "$index/${maxPage}"
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
        bindView.viewPager.adapter = PictureFragmentStateAdapter(this, mFragments)
        bindView.viewPager.offscreenPageLimit = maxPage
        bindView.viewPager.setCurrentItem(index - 1, false)

        bindView.tvPicIndex.text = "$index/${maxPage}"
        bindView.ivSave.click {
            if (pictureListStr != null && pictureListStr?.size!! >= index) {
                downFile(pictureListStr!![index - 1])
            }
//            when (picType) {
//                0, 1 ->
//                    if (pictureList != null) {
//                        if (pictureList?.size!! >= index) {
//                            try {
//                                val imageMsg = GsonUtils.fromJson(
//                                    pictureList!![index - 1].content,
//                                    ImageBean::class.java
//                                )
//                                downFile(imageMsg.url)
//                            } catch (e: Exception) {
//                                "图片json异常".logE()
//                            }
//                        }
//                    }
//                2 -> {
//                    if (pictureListStr != null && pictureListStr?.size!! >= index) {
//                        downFile(pictureListStr!![index - 1])
//                    }
//                }
//            }
        }
    }

    /**
     * 处理下载操作
     */
    private fun downFile(url: String) {
        if (url.lowercase().contains(".gif")) {
            //如果是gif图片
            if (checkPermission()) {
                "图片已开始下载...".toast()
                down(url)
            }
        } else {
            //普通图片
            "图片已开始下载...".toast()
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                var bitmap =
                    WeChatImageUtils.GetImageInputStream(url)
                val url =
                    bitmap?.saveToAlbum(
                        this@PictureActivity,
                        System.currentTimeMillis()
                            .toString() + "${url.getMimeType()}",
                        null
                    )
                url?.let {
                    "图片已保存到本地相册".toast()
                }
            }
        }
    }

    override fun observeCallBack() {
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
                        this@PictureActivity,
                        tempFile.path,
                        downloadUrl.getMimeType()
                    )
                    runOnUiThread(Runnable {
                        if (isOk) {
                            "图片已保存到本地相册".toast()
                        } else {
                            "----图片已保存到本地相册异常".logE()
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
                "提示",
                "下载gif图之前必须要获取，保存文件的所有权限,是否允许确定",
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

    override fun onDestroy() {
        super.onDestroy()
        PictureSelectionConfig.destroy()
    }

    //<editor-fold defaultstate="collapsed" desc="init viewPagerAdapter">
    open class PictureFragmentStateAdapter(
        context: FragmentActivity,
        private val mFragments: MutableList<PictureFragment>
    ) : FragmentStateAdapter(context) {
        override fun getItemCount(): Int {
            return mFragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }
    }
//</editor-fold>
}