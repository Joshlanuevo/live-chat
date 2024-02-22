package com.ym.chat.update

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.*
import com.ym.base.ext.launchError
import com.ym.base.ext.xmlToString
import com.ym.base.ext.xmlToast
import com.ym.base.rxhttp.RxHttpConfig
import com.ym.chat.R
import com.ym.chat.app.LiveDataConfig
import com.ym.chat.app.StringConstants
import com.ym.chat.utils.PathConfig
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import rxhttp.RxHttp
import rxhttp.toFlow
import java.io.File
import java.util.*


open class YmUpdateService : JobIntentService() {
    //<editor-fold defaultstate="collapsed" desc="外部跳转">
    companion object {
        private const val JOB_ID = 1000
        private const val DOWNLOAD_PATH = "download_path"
        private const val DOWNLOAD_VERSION = "download_version"
        private const val DOWNLOAD_SHOW_NOTIFICATION = "download_show_notification"
        private var downIDs: MutableList<Int> = mutableListOf() //正在下载的通知id
        fun startIntent(path: String, version: String, showNotification: Boolean = false) {
            val intent = Intent(Utils.getApp(), YmUpdateService::class.java)
            intent.putExtra(DOWNLOAD_PATH, path)
            intent.putExtra(DOWNLOAD_VERSION, version)
            intent.putExtra(DOWNLOAD_SHOW_NOTIFICATION, showNotification)
            enqueueWork(Utils.getApp(), YmUpdateService::class.java, JOB_ID, intent)
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="变量">
    //下载的百分比
    private var mPercent = 0f

    //通知管理
    private var mNotificationManager: NotificationManager? = null

    //通知创建类
    private var mBuilder: NotificationCompat.Builder? = null

    //上次的下载量(方便计算下载速度)
    private var mLastBytes: Long = 0

    //上次的时间
    private var mLastTime: Long = 0

    //下载速度
    private var mSpeed: Long = 0

    //通知栏数据设置
    private var mRemoteViews: RemoteViews? = null

    //文件下载保存的文件夹
    private val mFileDir = PathConfig.TEMP_CACHE_DIR

    //是否显示通知栏
    private var needShowNotification = false

    //总大小
    private var mTotalSize = 0L

    //apk下载地址
    private var mApkUrl = ""
    private var mApkVersion = ""

    //渠道id 安卓8.0 https://blog.csdn.net/MakerCloud/article/details/82079498
    private val UPDATE_CHANNEL_ID = AppUtils.getAppPackageName() + ".update.channel.id"
    private val UPDATE_CHANNEL_NAME = AppUtils.getAppPackageName() + ".update.channel.name"

    //通知
    private var notificationID = 0
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="处理打开Service的命令">
    @SuppressLint("MissingPermission")
    override fun onHandleWork(intent: Intent) {
        if (mNotificationManager == null) {
            mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    UPDATE_CHANNEL_ID,
                    UPDATE_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ) //等级太高会一直响
                channel.setSound(null, null)
                mNotificationManager?.createNotificationChannel(channel)
            }
        }
        intent.let {
            mApkUrl = it.getStringExtra(DOWNLOAD_PATH) ?: ""
            mApkVersion = it.getStringExtra(DOWNLOAD_VERSION) ?: ""
            if (downIDs.contains(mApkUrl.hashCode())) return
            notificationID = mApkUrl.hashCode()
            downIDs.add(mApkUrl.hashCode())
            val downloadUrl: String = mApkUrl
            val downloadVersion: String = mApkVersion
            needShowNotification = it.getBooleanExtra(DOWNLOAD_SHOW_NOTIFICATION, false)
            val downLoadName = EncryptUtils.encryptMD5ToString("$downloadUrl-$downloadVersion")
            val apkName = "$downLoadName.apk"
            if (File(mFileDir, apkName).exists()) {
                showSuccess(File(mFileDir, apkName).path)
                AppUtils.installApp(File(mFileDir, apkName).path)
//                NotificationUtils.setNotificationBarVisibility(false)
                return
            }
            showNotification()
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
                        updateProgress(
                            downSize + currentSize,
                            totalSize + downSize
                        )
                    }.catch {
                        //下载失败，处理相关逻辑
                        showFail(downloadUrl)
                    }.collect {
                        //下载成功，处理相关逻辑
                        FileUtils.rename(tempFile, apkName)
                        showSuccess(File(mFileDir, apkName).path)
                        AppUtils.installApp(File(mFileDir, apkName).path)
//                    NotificationUtils.setNotificationBarVisibility(false)
                    }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="需要下载时显示APP下载信息">
    //显示下载通知
    private fun showNotification() {
        //发送进度
        launchError { LiveDataConfig.downProgressLiveData.value = 0f }
        if (needShowNotification) {
            initRemoteViews()
            mRemoteViews?.let {
                val sb = SpannableStringBuilder()
                sb.append("${R.string.now_downloading.xmlToString()}\"")
                sb.append(
                    com.ym.chat.utils.SpanUtils.getSpanSimple(
                        text = AppUtils.getAppName(),
                        bold = false
                    )
                )
                sb.append("\"${R.string.new_version.xmlToString()}")
                it.setTextViewText(R.id.notice_update_title, sb)
                it.setTextViewText(R.id.notice_update_percent, "0%")
                it.setProgressBar(R.id.notice_update_progress, 100, 0, false)
                it.setTextViewText(R.id.notice_update_speed, "")
                it.setTextViewText(R.id.notice_update_size, "")
                initBuilder(it)
                mNotificationManager?.notify(notificationID, mBuilder?.build())
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="通知栏更新下载进度">
    //每次刷新的时间间隔
    private val interval = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) 500 else 250

    //更新下载进度
    private fun updateProgress(offsetSize: Long, totalSize: Long) {
        mTotalSize = totalSize
        mPercent = offsetSize * 100f / totalSize
        //发送进度
        launchError { LiveDataConfig.downProgressLiveData.value = 99.9f.coerceAtMost(mPercent) }
        if (mLastTime == 0L || mLastBytes == 0L) {
            mSpeed = offsetSize / 1000
            mLastTime = System.currentTimeMillis()
            mLastBytes = offsetSize
        } else if (System.currentTimeMillis() - mLastTime >= interval || offsetSize > totalSize) {
            mSpeed = (offsetSize - mLastBytes) / (System.currentTimeMillis() - mLastTime) * 1000
            mLastTime = System.currentTimeMillis()
            mLastBytes = offsetSize
        } else {
            return
        }
        if (needShowNotification) {
            mRemoteViews?.let {
                it.setProgressBar(R.id.notice_update_progress, 100, mPercent.toInt(), false)
                val progress = String.format(Locale.getDefault(), "%.1f", mPercent) + "%"
                it.setTextViewText(R.id.notice_update_percent, progress)
                val speedStr = byte2FitMemorySize(mSpeed) + "/s"
                it.setTextViewText(R.id.notice_update_speed, speedStr)
                val curSizeStr = byte2FitMemorySize(offsetSize)
                val totalSizeStr = byte2FitMemorySize(totalSize)
                it.setTextViewText(R.id.notice_update_size, "$curSizeStr/$totalSizeStr")
                mNotificationManager?.notify(notificationID, mBuilder?.build())
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="显示更新成功">
    //下载成功后读取APK的包名以监听该APK的安装
    private var downApkPackageName: String = ""

    //下载成功
    private fun showSuccess(filePath: String) {
        downApkPackageName = AppUtils.getApkInfo(filePath)?.packageName ?: ""
        downIDs.remove(mApkUrl.hashCode())
        //发送进度
        launchError { LiveDataConfig.downProgressLiveData.value = 100f }
        if (needShowNotification) {
            initRemoteViews()
            mRemoteViews?.let {
                val sb = SpannableStringBuilder()
                sb.append("\"")
                sb.append(
                    com.ym.chat.utils.SpanUtils.getSpanSimple(
                        text = AppUtils.getAppName(),
                        bold = false
                    )
                )
                sb.append("\"${R.string.downloaded_click_install.xmlToString()}")
                it.setTextViewText(R.id.notice_update_title, sb)
                it.setProgressBar(R.id.notice_update_progress, 100, 100, false)
                it.setTextViewText(R.id.notice_update_percent, "100%")
                if (mTotalSize == 0L) mTotalSize = File(filePath).length()
                val totalSizeStr = byte2FitMemorySize(mTotalSize)
                it.setTextViewText(R.id.notice_update_size, "$totalSizeStr/$totalSizeStr")
                val intentInstall =
                    Intent(Utils.getApp(), NotificationBroadcastReceiver::class.java)
                intentInstall.action = StringConstants.Update.INTENT_KEY_INSTALL_APP
                intentInstall.putExtra(StringConstants.Update.INTENT_KEY_INSTALL_PATH, filePath)
                intentInstall.putExtra(StringConstants.Update.INTENT_KEY_UPDATE_ID, notificationID)
                intentInstall.putExtra(StringConstants.Update.INTENT_KEY_RETRY_PATH, mApkUrl)
                intentInstall.putExtra(StringConstants.Update.INTENT_KEY_RETRY_VERSION, mApkVersion)
                val intent = PendingIntent.getBroadcast(
                    Utils.getApp(),
                    notificationID,
                    intentInstall,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                it.setOnClickPendingIntent(R.id.notice_update_layout, intent)
                initBuilder(it)
                mNotificationManager?.notify(notificationID, mBuilder?.build())
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="显示更新失败">
    //更新失败
    private fun showFail(path: String) {
        R.string.download_fail_click_retry.xmlToast()
        //发送进度
        launchError { LiveDataConfig.downProgressLiveData.value = -1f }
        downIDs.remove(mApkUrl.hashCode())
        if (needShowNotification) {
            initRemoteViews()
            mRemoteViews?.let {
                val sb = SpannableStringBuilder()
                sb.append("\"")
                sb.append(
                    com.ym.chat.utils.SpanUtils.getSpanSimple(
                        text = AppUtils.getAppName(),
                        bold = false
                    )
                )
                sb.append("\"${R.string.download_fail_click_retry.xmlToString()}")
                it.setTextViewText(R.id.notice_update_title, sb)
                val intentInstall =
                    Intent(Utils.getApp(), NotificationBroadcastReceiver::class.java)
                intentInstall.action = StringConstants.Update.INTENT_KEY_APK_DOWNLOAD_ERROR
                intentInstall.putExtra(StringConstants.Update.INTENT_KEY_RETRY_PATH, mApkUrl)
                intentInstall.putExtra(StringConstants.Update.INTENT_KEY_RETRY_VERSION, mApkVersion)
                val intent = PendingIntent.getBroadcast(
                    Utils.getApp(),
                    notificationID,
                    intentInstall,
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
                it.setOnClickPendingIntent(R.id.notice_update_layout, intent)
                initBuilder(it)
                mNotificationManager?.notify(notificationID, mBuilder?.build())
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="通知栏显示相关初始化">
    //初始化RemoteViews
    private fun initRemoteViews() {
        if (mRemoteViews == null) { //防止直接走失败
            mRemoteViews = RemoteViews(packageName, R.layout.notification_update)
            mRemoteViews?.setImageViewResource(R.id.notice_update_icon, R.mipmap.ic_launcher)
        }
    }

    //初始化Builder
    private fun initBuilder(remoteViews: RemoteViews) {
        if (mBuilder == null) {
            mBuilder = NotificationCompat.Builder(Utils.getApp(), UPDATE_CHANNEL_ID)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.FLAG_AUTO_CANCEL)
                .setSound(null)
                .setOngoing(true) //将Ongoing设为true 那么notification将不能滑动删除
                .setAutoCancel(false) //将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.ic_msg_left)
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="文件大小显示处理">
    private fun byte2FitMemorySize(byteNum: Long): String {
        return when {
            byteNum < 0 -> "0KB"
            byteNum < 1024 -> String.format(Locale.getDefault(), "%.2fB", byteNum.toDouble())
            byteNum < 1048576 -> String.format(
                Locale.getDefault(),
                "%.2fKB",
                byteNum.toDouble() / 1024
            )
            byteNum < 1073741824 -> String.format(
                Locale.getDefault(),
                "%.2fMB",
                byteNum.toDouble() / 1048576
            )
            else -> String.format(Locale.getDefault(), "%.3fGB", byteNum.toDouble() / 1073741824)
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="注册和移除关听">
    override fun onCreate() {
        super.onCreate()
        registerAppInstall()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        unregisterAppInstall()
        super.onTaskRemoved(rootIntent)
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="监听安装">
    private var hasRegister = false
    private fun registerAppInstall() {
        if (!hasRegister) {
            hasRegister = true
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            intentFilter.addDataScheme("package")
            Utils.getApp().registerReceiver(mInstallAppBroadcastReceiver, intentFilter)
        }
    }

    private fun unregisterAppInstall() {
        mNotificationManager?.cancel(notificationID)
        downIDs.remove(mApkUrl.hashCode())
        if (hasRegister) {
            hasRegister = false
            Utils.getApp().unregisterReceiver(mInstallAppBroadcastReceiver)
        }
    }

    private val mInstallAppBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null && TextUtils.equals(Intent.ACTION_PACKAGE_ADDED, intent.action)) {
                intent.data?.schemeSpecificPart?.let { packageName ->
                    if (packageName == AppUtils.getAppPackageName()) {
                        unregisterAppInstall()
                    } else if (packageName == downApkPackageName) { //兼容正式和测试包名不一样
                        unregisterAppInstall()
                        AppUtils.exitApp()
                    }
                }
            }
        }
    }
    //</editor-fold>
}