package com.ym.chat.broadcast

import android.os.Build
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ym.chat.service.PushMessageService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.action != null) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                // Start your service here when the device boots up
                val serviceIntent = Intent(context, PushMessageService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}