package com.ym.chat.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.lang.Exception
import java.math.BigDecimal

object DataCleanManagerUtils {

    /**
     * 获取缓存大小
     *
     * @param context
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    fun getTotalCacheSize(context: Context): String? {
        var cacheSize: Long = getFolderSize(context.cacheDir)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (context.externalCacheDir != null) {
                cacheSize += getFolderSize(context.externalCacheDir!!)
            }
        }
        return getFormatSize(cacheSize)
    }

    /**
     * 清空缓存
     *
     * @param context
     */
    fun clearAllCache(context: Context): Boolean {
        var b = false
        deleteDir(context.cacheDir)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            b = context.externalCacheDir?.let { deleteDir(it) } == true
        }
        return b
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir != null && dir.isDirectory) {
            val children: Array<String> = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir!!.delete()
    }


    // 获取文件
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    @Throws(Exception::class)
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            for (i in fileList.indices) {
                // 如果下面还有文件
                size = if (fileList[i].isDirectory) {
                    size + getFolderSize(fileList[i])
                } else {
                    size + fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    private fun getFormatSize(size: Long): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
//            return size + "Byte";
            return "0.0KB"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(kiloByte.toString())
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString().toString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(megaByte.toString())
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString().toString() + "M"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(gigaByte.toString())
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                .toPlainString().toString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
            .toString() + "TB"
    }


}