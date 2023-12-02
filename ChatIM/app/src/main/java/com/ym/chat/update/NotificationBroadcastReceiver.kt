package com.ym.chat.update

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.ym.chat.app.StringConstants
import com.ym.chat.utils.NetUtils
import java.io.File


class NotificationBroadcastReceiver : BroadcastReceiver() {

  @SuppressLint("MissingPermission")
  override fun onReceive(context: Context, intent: Intent) {
    intent.action?.let {
      when (it) {
        //安装
        StringConstants.Update.INTENT_KEY_INSTALL_APP -> {
          //点击安装取消消息
          if (ActivityUtils.getActivityList().isNullOrEmpty()) (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
              .cancel(intent.getIntExtra(StringConstants.Update.INTENT_KEY_UPDATE_ID, 0))
          val path = intent.getStringExtra(StringConstants.Update.INTENT_KEY_INSTALL_PATH)
          if (path != null && File(path).exists()) {
            AppUtils.installApp(path)
//            NotificationUtils.setNotificationBarVisibility(false) //收起通知栏
          } else {
            val apkVersion = intent.getStringExtra(StringConstants.Update.INTENT_KEY_RETRY_VERSION)
            val apkUrl = intent.getStringExtra(StringConstants.Update.INTENT_KEY_RETRY_PATH)
            YmUpdateService.startIntent(apkUrl ?: "", apkVersion ?: "", true)
          }
        }
        //重试
        StringConstants.Update.INTENT_KEY_APK_DOWNLOAD_ERROR -> if (NetUtils.checkNetToast()) {
          val apkUrl = intent.getStringExtra(StringConstants.Update.INTENT_KEY_RETRY_PATH)
          val apkVersion = intent.getStringExtra(StringConstants.Update.INTENT_KEY_RETRY_VERSION)
          YmUpdateService.startIntent(apkUrl ?: "", apkVersion ?: "", true)
        }
      }
    }
  }
}