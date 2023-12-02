package com.ym.chat.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import com.blankj.utilcode.util.Utils
import com.ym.base.ext.saveToAlbum
import com.ym.base.ext.toast
import com.ym.chat.R
import java.io.*


/**
 * 获取界面view 截图
 *  并保存到本地相册
 */
object BitmapUtils {

    /**
     * 截取view 图片
     */
    fun viewSnapshot(view: View) {
        //使控件可以保存
        view.isDrawingCacheEnabled = true
        //获取缓存bitmap
        var bitmap: Bitmap = view.drawingCache
        bitmap = Bitmap.createBitmap(bitmap)
        //使控件可以保存
        view.isDrawingCacheEnabled = false
    }

    /**
     * 截取activity界面
     * 并保存到本地相册
     */
    fun saveScreenshotFromActivity(context: Activity) {
        val view: View = context.window.decorView
        view.isDrawingCacheEnabled = true
        val bitmap = view.drawingCache
        saveImageToGallery(bitmap, context)

        //回收资源
        view.isDrawingCacheEnabled = false
        view.destroyDrawingCache()
    }

    /**
     * 截取view
     * 并保存到本地相册
     */
    fun saveScreenshotFromView(view: View, context: Activity) {
        try {
            val w: Int = view.width
            val h: Int = view.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c =  Canvas(bitmap)
            c.drawColor(Color.WHITE)
            view.draw(c);
            val url =
                bitmap?.saveToAlbum(context, System.currentTimeMillis().toString() + ".jpg", null)
            url?.let {
                ChatUtils.getString(R.string.图片保存到本地相册).toast()
            }
            //回收资源
            view.isDrawingCacheEnabled = false
            view.destroyDrawingCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 截取view
     * 并保存到本地相册
     * 并获取 path
     */
    fun saveScreenshotFromViewToFile(view: View, context: Activity): File {
        view.isDrawingCacheEnabled = true
        val bitmap = view.drawingCache
        var path = getImageToGalleryToFile(bitmap, context)

        //回收资源
        view.isDrawingCacheEnabled = false
        view.destroyDrawingCache()
        return path
    }

    open fun getBitmapFromView(view: View): Bitmap? {
        var bitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap


    }

    private fun getImageToGalleryToFile(bmp: Bitmap, context: Activity): File {
        val strPath = context.getExternalFilesDir(null)
            .toString() + "/shareData/" + System.currentTimeMillis().toString() + ".jpg"
        var file = File(strPath)
        getImageToGalleryFile(file, bmp, context)
        return file
    }


    private fun getImageToGalleryFile(file: File, bmp: Bitmap, context: Activity): File {
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    fun saveImageToGallery(bmp: Bitmap, context: Activity): File {
        val appDir = File(getDCIM())
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()

            // 通知图库更新
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.parse("file://" + getDCIM())
                )
            )
            ChatUtils.getString(R.string.图片保存到本地相册).toast()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }


    //fileName为需要保存到相册的图片名
    fun insert2Album(context: Context, inputStream: InputStream, fileName: String) {
        if (inputStream == null)
            return;
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //RELATIVE_PATH 字段表示相对路径-------->(1)
            contentValues.put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES
            )
        } else {
            val dstPath =
                "${Environment.getExternalStorageDirectory()}" + File.separator + "${Environment.DIRECTORY_PICTURES}"
            File.separator + fileName;
            //DATA字段在Android 10.0 之后已经废弃
            contentValues.put(MediaStore.Images.ImageColumns.DATA, dstPath);
        }

        //插入相册------->(2)
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        );

        //写入文件------->(3)
        write2File(context, uri, inputStream);
    }


    //uri 关联着待写入的文件
    //inputStream 表示原始的文件流
    private fun write2File(context: Context, uri: Uri?, inputStream: InputStream?) {
        if (uri == null || inputStream == null) return
        try {
            //从Uri构造输出流
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            var inCache = ByteArray(1024)
            var len = 0
            do {
                //从输入流里读取数据
                len = inputStream.read(inCache)
                if (len != -1) {
                    outputStream?.write(inCache, 0, len)
                    outputStream?.flush()
                }
            } while (len != -1)
            inputStream.close()
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 保存图片到指定路径
     *
     * @param context
     * @param bitmap   要保存的图片
     * @param fileName 自定义图片名称
     * @return
     */
    fun saveImageToGallery(context: Context, bitmap: Bitmap, fileName: String?): Boolean {
        // 保存图片至指定路径
        val storePath =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + "qrcode"
        val appDir = File(storePath)
        if (!appDir.exists()) {
            appDir.mkdir()
        }
        val file = File(appDir, fileName)
        try {
            val fos = FileOutputStream(file)
            //通过io流的方式来压缩保存图片(80代表压缩20%)
            val isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            fos.close()

            //发送广播通知系统图库刷新数据
            val uri = Uri.fromFile(file)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            return isSuccess
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }


    private fun getDCIM(): String? {
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            return ""
        }
        var path: String = Environment.getExternalStorageDirectory().path.toString() + "/dcim/"
        if (File(path).exists()) {
            return path
        }
        path = Environment.getExternalStorageDirectory().path.toString() + "/DCIM/"
        val file = File(path)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return ""
            }
        }
        return path
    }


}