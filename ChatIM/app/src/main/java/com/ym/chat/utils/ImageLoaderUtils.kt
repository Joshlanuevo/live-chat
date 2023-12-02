package com.ym.chat.utils

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build.VERSION.SDK_INT
import android.view.View
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.transform.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


object ImageLoaderUtils {

    var gifImageLoader: ImageLoader? = null

    fun init(application: Application) {
        gifImageLoader = ImageLoader.Builder(application)
            .componentRegistry {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder())
                } else {
                    add(GifDecoder())
                }
            }.build()

    }


    /**
     * 根据图片类型加载图片
     * 包括 gif  svg
     */
    fun imageLoad(ivImage: ImageView?, advertisePic: String?) {
        var picUrl: String = ""
        if (!advertisePic.isNullOrEmpty() && advertisePic.length > 3)
            picUrl = advertisePic?.takeLast(3)?.toString() ?: ""
        when (picUrl) {
            "gif" -> {
                gifImageLoader?.let { ivImage?.load(advertisePic, it) }
            }
            else -> {
                ivImage?.load(advertisePic)
                {
                    transformations(
                        RoundedCornersTransformation(),
                        RoundedCornersTransformation(
                            topLeft = 20f,
                            topRight = 20f,
                            bottomLeft = 20f,
                            bottomRight = 20f
                        )
                    )
                }
            }
        }
    }

    /**
     * 根据图片类型加载图片gif
     */
    @JvmStatic
    fun imageLoadGif(ivImage: ImageView?, advertisePic: Int) {
        gifImageLoader?.let {
            ivImage?.load(advertisePic, it)
        }
    }

    /**
     * 检测是否白屏
     */
    fun checkIsEmptyPage(
        view: View,
        succeed: () -> Unit,
        error: () -> Unit,
        completed: () -> Unit
    ) {

        val w: Int = view.width
        val h: Int = view.height

        val bitmap = Bitmap.createBitmap(w / 2, h / 2, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        view.draw(c)

        var whitePixelCount = 0
        var rate = 0F

        if (bitmap == null) {
            error.invoke()
            completed.invoke()
            return
        }


        GlobalScope.launch(Dispatchers.IO) {
            val width = bitmap.width
            val height = bitmap.height

            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitmap.getPixel(x, y) == -1) {
                        whitePixelCount++
                    }
                }
            }

            if (whitePixelCount > 0) {
                rate = whitePixelCount * 100f / width / height
            }

            bitmap.recycle()

            GlobalScope.launch(Dispatchers.Main) {
                if (rate > 99) {
                    //page empty
                    error.invoke()
                } else {
                    succeed.invoke()
                }
                completed.invoke()
            }

        }
    }

}

