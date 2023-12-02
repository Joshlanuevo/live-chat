package com.ym.base.widget.ext

import android.os.Build
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import coil.clear
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.Transformation
import com.luck.picture.lib.photoview.PhotoView
import com.ym.base.ext.logE
import com.ym.base.ext.toFile
import com.ym.base.image.NoHardTransformation
import com.ym.base.image.PlaceHolderUtils
import com.ym.base_sdk.R

/**
 * Author:yangcheng
 * Date:2020/8/12
 * Time:18:28
 */
const val myLoadCrossFade = 300
fun ImageView.clearLoad() {
    this.clear()
    setTag(R.id.id_suc_img, null)
}

fun ImageView.loadHead(url: String?, isNeedPlace: Boolean = true) {
    this.scaleType = ScaleType.CENTER_CROP
    if (url.isNullOrBlank()) {
        this.clearLoad()
        this.load(R.drawable.user_head_default)
    } else {
        if (getTag(R.id.id_suc_img) == url) return
        this.clearLoad()
        val iv = this
        val build = fun ImageRequest.Builder.() {
            crossfade(myLoadCrossFade)
            if (isNeedPlace) placeholder(R.drawable.user_head_default)
            error(R.drawable.user_head_default)
            listener(onError = { _, e -> "头像加载失败:${e.message}".logE() },
                onSuccess = { _, _ -> iv.setTag(R.id.id_suc_img, url) })
        }
        val f = url.toFile()
        if (f != null) iv.load(f, builder = build) else iv.load(url, builder = build)
    }
}

//正方形图片加载
fun ImageView.loadImgSquare(url: String?) {
    this.scaleType = ScaleType.CENTER_CROP
    if (url.isNullOrBlank()) {
        this.clearLoad()
        this.load(R.drawable.holder_error_s)
    } else {
        if (getTag(R.id.id_suc_img) == url) return
        this.clearLoad()
        val iv = this
        val build = fun ImageRequest.Builder.() {
            crossfade(myLoadCrossFade)
            placeholder(R.drawable.holder_loading_s)
            error(R.drawable.holder_error_s)
            listener(onError = { _, e -> "图片加载失败:${e.message}".logE() },
                onSuccess = { _, _ -> iv.setTag(R.id.id_suc_img, url) })
        }
        val f = url.toFile()
        if (f != null) iv.load(f, builder = build) else iv.load(url, builder = build)
    }
}

//横向图片加载(占位图尺寸查看"占位图说明.txt")
@RequiresApi(Build.VERSION_CODES.M)
fun ImageView.loadImgHorizontal(
    url: String?, ratio: Float, useCache: Boolean = true,
    @DrawableRes loadingRes: Int? = null, @DrawableRes errorRes: Int? = null,
) {
    if (url.isNullOrBlank()) {
        this.clearLoad()
        if (errorRes != null) this.load(errorRes) else this.load(
            PlaceHolderUtils.getErrorHolder(
                ratio
            )
        )
    } else {
        if (getTag(R.id.id_suc_img) == url) return
        this.clearLoad()
        val iv = this
        val build = fun ImageRequest.Builder.() {
            if (!useCache) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.DISABLED)
            }
            crossfade(myLoadCrossFade)
            if (loadingRes != null) placeholder(loadingRes) else placeholder(
                PlaceHolderUtils.getLoadingHolder(
                    ratio
                )
            )
            if (errorRes != null) error(errorRes) else error(PlaceHolderUtils.getErrorHolder(ratio))
            listener(
                onError = { _, e -> "图片加载失败:${e.message}".logE() },
                onSuccess = { _, _ -> iv.setTag(R.id.id_suc_img, url) })
        }
        val f = url.toFile()
        if (f != null) iv.load(f, builder = build) else iv.load(url, builder = build)
    }
}

//竖向图片加载
@RequiresApi(Build.VERSION_CODES.M)
fun ImageView.loadImgVertical(url: String?, ratio: Float) {
    if (url.isNullOrBlank()) {
        this.clearLoad()
        this.load(PlaceHolderUtils.getErrorHolder(ratio))
    } else {
        if (getTag(R.id.id_suc_img) == url) return
        this.clearLoad()
        val iv = this
        if (iv is PhotoView) iv.invisible()
        val build = fun ImageRequest.Builder.() {
            crossfade(myLoadCrossFade)
            placeholder(PlaceHolderUtils.getLoadingHolder(ratio))
            error(PlaceHolderUtils.getErrorHolder(ratio))
            listener(onError = { r, e ->
                if (iv is PhotoView) iv.visible()
                "图片加载失败:${r.data},e=${e.message ?: "null"}".logE()
            }) { _, _ ->
                iv.setTag(R.id.id_suc_img, url)
                if (iv is PhotoView) {
                    //iv.scale = iv.minimumScale
                    iv.visible()
                }
            }
        }
        val f = url.toFile()
        if (f != null) iv.load(f, builder = build) else iv.load(url, builder = build)
    }
}


//横向高斯模糊+黑白图片加载
fun ImageView.loadNoHard(url: String?) {
    val build = fun ImageRequest.Builder.() {
        //val list = mutableListOf<Transformation>()
        //list.add(NoHardTransformation(context))
        //transformations(list)
        allowHardware(false)
    }
    val f = url.toFile()
    if (f != null) load(f, builder = build) else load(url, builder = build)
}