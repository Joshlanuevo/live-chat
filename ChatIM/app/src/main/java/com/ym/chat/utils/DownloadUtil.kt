package com.ym.chat.utils

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity

object DownloadUtil {

    fun queryExist(context: Context, url: String): Pair<Boolean, Long?> {
        val manager =
            context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
        manager.query(query).use {
            while (it.moveToNext()) {
                if (it.getString(it.getColumnIndex(DownloadManager.COLUMN_URI))
                        .contains(url)
                ) {
                    val id =
                        it.getLong(it.getColumnIndex(DownloadManager.COLUMN_ID))
                    return Pair(true, id)
                }
            }
        }
        return Pair(false, null)
    }

    fun getMimeType(url: String?): String? {
        var type: String? = ""
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }


    // 查询下载进度，文件总大小多少，已经下载多少？
    private fun query(context: Context, id: Long) {
        val manager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadQuery = DownloadManager.Query()
        downloadQuery.setFilterById(id)
        val cursor: Cursor = manager.query(downloadQuery)
        if (cursor != null && cursor.moveToFirst()) {
            val fileName: Int = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)
            val fileUri: Int = cursor.getColumnIndex(DownloadManager.COLUMN_URI)
            val fn: String = cursor.getString(fileName)
            val fu: String = cursor.getString(fileUri)
            val totalSizeBytesIndex: Int =
                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val bytesDownloadSoFarIndex: Int =
                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            // 下载的文件总大小
            val totalSizeBytes: Int = cursor.getInt(totalSizeBytesIndex)

            // 截止目前已经下载的文件总大小
            val bytesDownloadSoFar: Int = cursor.getInt(bytesDownloadSoFarIndex)
//            Log.d(
//                this.javaClass.name,
//                "from $fu 下载到本地 $fn 文件总大小:$totalSizeBytes 已经下载:$bytesDownloadSoFar"
//            )
            cursor.close()
        }
    }


    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     *
     * @param downloadId
     * @return
     */
    fun getBytesAndStatus(downloadManager: DownloadManager, downloadId: Long): IntArray {
        val bytesAndStatus = intArrayOf(
            0, 1, 0
        )
        val query = DownloadManager.Query().setFilterById(downloadId)
        var cursor: Cursor? = null
        try {
            cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载文件大小
                bytesAndStatus[0] =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                //下载文件的总大小
                bytesAndStatus[1] =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                //下载状态
                bytesAndStatus[2] =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            }
        } finally {
            cursor?.close()
        }
        return bytesAndStatus
    }

}