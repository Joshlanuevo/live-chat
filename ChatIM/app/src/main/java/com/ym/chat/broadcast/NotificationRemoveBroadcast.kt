package com.ym.chat.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ym.chat.utils.ImCache

/**
 * 通知栏移除监听
 */
class NotificationRemoveBroadcast : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        p1?.let { intent ->
            val action = intent.action
            val targetId = intent.getStringExtra("targetId")
            if (action == "com.ym.chat.broadcast.NOTIFICATION_REMOVE") {
                //移除相应的消息
                ImCache.notifycationMsg[targetId]?.clear()
            }
        }

    }
}