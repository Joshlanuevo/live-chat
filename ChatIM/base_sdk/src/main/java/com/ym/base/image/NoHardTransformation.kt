@file:Suppress("unused")

package com.ym.base.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.renderscript.RenderScript
import androidx.annotation.RequiresApi
import androidx.core.graphics.applyCanvas
import coil.bitmap.BitmapPool
import coil.size.Size
import coil.transform.Transformation

/**
 * A [Transformation] that applies a Gaussian blur to an image.
 *
 * @param context The [Context] used to create a [RenderScript] instance.
 * @param radius The radius of the blur.
 * @param sampling The sampling multiplier used to scale the image. Values > 1
 *  will downscale the image. Values between 0 and 1 will upscale the image.
 */
@RequiresApi(18)
/**针对coil图片加载库，完成网络加载图因为使用了硬件加速导致的截图报错的问题*/
class NoHardTransformation @JvmOverloads constructor(
    private val context: Context,
) : Transformation {

    override fun key(): String = "${NoHardTransformation::class.java.name}-"

    override suspend fun transform(pool: BitmapPool, input: Bitmap, size: Size): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val scaledWidth = input.width
        val scaledHeight = input.height
        val output = pool.get(scaledWidth, scaledHeight, input.config ?: Bitmap.Config.ARGB_8888)
        output.applyCanvas {
            drawBitmap(input, 0f, 0f, paint)
        }
        return output
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is NoHardTransformation &&
            context == other.context
    }

    override fun hashCode(): Int {
        var result = context.hashCode()
        return result
    }

    override fun toString(): String {
        return "BlurTransformation(context=$context, )"
    }

}
