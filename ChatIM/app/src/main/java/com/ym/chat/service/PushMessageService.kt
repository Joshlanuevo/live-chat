package com.ym.chat.service

import android.app.Notification
import android.content.Context
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver


class PushMessageService: JPushMessageReceiver() {
    override fun getNotification(
        context: Context?,
        notificationMessage: NotificationMessage?
    ): Notification? {
        return super.getNotification(context, notificationMessage)
    }

    override fun onMessage(context: Context?, customMessage: CustomMessage?) {
    }
}