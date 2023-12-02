package com.ym.chat.update

import android.app.DownloadManager
import android.os.SystemClock
import android.util.Log
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.utils.DownloadUtil

class UpdateProcessTask(
    private val downloadManager: DownloadManager,
    private val downloadId: Long,
    val item: ChatMessageBean
) : Runnable {

    override fun run() {
        do {
            val sizeArray = DownloadUtil.getBytesAndStatus(downloadManager, downloadId)
            if (sizeArray[0] <= 0) {
                sizeArray[0] = 0
            }
            if (sizeArray[1] <= 0) {
                sizeArray[1] = 1
            }
            item.downloadProcess = sizeArray[0].toFloat() / sizeArray[1]
            LiveEventBus.get(EventKeys.FILE_DOWNLOAD_PROCESS, ChatMessageBean::class.java)
                .post(item)
            SystemClock.sleep(200)
        } while (sizeArray[0] < sizeArray[1])

    }
}

