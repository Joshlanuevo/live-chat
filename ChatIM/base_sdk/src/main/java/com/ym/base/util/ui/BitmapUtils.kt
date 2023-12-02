package com.ym.base.util.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import java.io.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 */
object BitmapUtils {

    /**
     * 从Uri中读取数据编码成base64
     */
    fun readUriToBase64(context: Context, uri: Uri):String?{
        var result: String? = null
        var baos: ByteArrayOutputStream? = null
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if(inputStream != null){
            baos = ByteArrayOutputStream()
            var count = 0
            val temp = ByteArray(1024)
            while (true){
                count = inputStream.read(temp)
                if(count<=0)break
                baos.write(temp,0,count)
            }
            inputStream.close()

            val bitmapBytes: ByteArray = baos.toByteArray()
            result = "data:image/jpg;base64,"+ Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
////             【不要删除，用于输出Base64编码格式的图片，方便开发检测】
//            var output = File(context.cacheDir.path+File.separator+"base64.txt")
//            var outputStream = FileOutputStream(output)
//            outputStream.write(result.toByteArray())
//            outputStream.flush()
//            outputStream.close()
        }
        return result
    }

}