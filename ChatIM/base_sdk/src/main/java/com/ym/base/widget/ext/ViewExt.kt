package com.ym.base.widget.ext

import android.R.attr
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.*
import android.graphics.drawable.GradientDrawable.Orientation.*
import android.view.*
import androidx.coordinatorlayout.widget.ViewGroupUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.ym.base.ext.CanUseInt
import com.ym.base.ext.getAllChildFragments
import com.ym.base.util.other.PressEffectHelper
import com.ym.base_sdk.R
import java.lang.ref.WeakReference

/**
 * Author:yangcheng
 * Date:2020/8/11
 * Time:17:33
 */
//点击事件
inline fun View.click(fastClick: Long = 300, crossinline funClick: (view: View) -> Unit): View {
    this.setOnClickListener {
        val tag = this.getTag(R.id.id_tag_click)
        if (tag == null || System.currentTimeMillis() - tag.toString().toLong() > fastClick) {
            this.setTag(R.id.id_tag_click, System.currentTimeMillis())
            funClick.invoke(it)
        }
    }
    return this
}

//单击+双击事件
inline fun View.clickAndDouble(
    crossinline funClick: (view: View) -> Unit,
    crossinline funDoubleClick: (view: View) -> Unit
) {
    val clickView = this
    clickView.setOnClickListener {
        val tag1 = clickView.getTag(R.id.id_tag_click)
        val tag2 = clickView.getTag(R.id.id_tag_click_double)
        //触发点击后600ms内不再触发点击
        if (tag2 != null && System.currentTimeMillis() - tag2.toString()
                .toLong() < 600
        ) return@setOnClickListener
        //单击
        if (tag1 == null || System.currentTimeMillis() - tag1.toString().toLong() > 600) {
            clickView.setTag(R.id.id_tag_click, System.currentTimeMillis())
            //倒计时执行单击
            val runnable = Runnable {
                clickView.getTag(R.id.id_tag_click_runnable)
                    ?.let { r -> clickView.removeCallbacks(r as Runnable) }
                clickView.setTag(R.id.id_tag_click_double, System.currentTimeMillis())
                funClick.invoke(clickView)
            }
            clickView.setTag(R.id.id_tag_click_runnable, runnable)
            clickView.postDelayed(runnable, 220)
        } else if (System.currentTimeMillis() - tag1.toString().toLong() <= 220) { //双击
            //取消单击，执行双击
            clickView.getTag(R.id.id_tag_click_runnable)
                ?.let { r -> clickView.removeCallbacks(r as Runnable) }
            clickView.setTag(R.id.id_tag_click_double, System.currentTimeMillis())
            funDoubleClick.invoke(clickView)
        }
    }
}

//显示
fun View.visible() {
    this.visibility = View.VISIBLE
}

//不显示，但占位
fun View.invisible() {
    this.visibility = View.INVISIBLE
}

//不显示，不占位
fun View.gone() {
    this.visibility = View.GONE
}

//显示或者不显示且不占位
fun View.visibleGone(visible: Boolean) = if (visible) visible() else gone()

//显示或者不显示但占位
fun View.visibleInvisible(visible: Boolean) = if (visible) visible() else invisible()

//设置按下效果为改变透明度
fun View.pressEffectAlpha(pressAlpha: Float = 0.7f): View {
    PressEffectHelper.alphaEffect(this, pressAlpha)
    return this
}

//设置按下效果为改变背景色
fun View.pressEffectBgColor(
    bgColor: Int = Color.parseColor("#f7f7f7"),
    topLeftRadiusDp: Float = 0f,
    topRightRadiusDp: Float = 0f,
    bottomRightRadiusDp: Float = 0f,
    bottomLeftRadiusDp: Float = 0f
) {
    PressEffectHelper.bgColorEffect(
        this,
        bgColor,
        topLeftRadiusDp,
        topRightRadiusDp,
        bottomRightRadiusDp,
        bottomLeftRadiusDp
    )
}

//关闭按下效果
fun View.pressEffectDisable() {
    this.setOnTouchListener(null)
}

//从父控件移除
fun View.removeParent() {
    val parentTemp = parent
    if (parentTemp is ViewManager) parentTemp.removeView(this)
}

//创建一个具有多重状态的 图片背景drawable 可以设置边框，这个我就没给设置每个状态图片的的角度了
fun View.setStateDrawable(
    normalColor: Int,
    normalColorEnd: Int,
    pressedColor: Int,
    pressedColorEnd: Int,
    noEnabledColor: Int,
    noEnabledColorEnd: Int,
    gradientTypeNormal: Int,
    gradientTypePressed: Int,
    gradientTypeNoEnabled: Int,
    strokeWidth: Int = 0,
    strokeNormalColor: Int = 0,
    strokePressedColor: Int = strokeNormalColor,
    strokeNoEnabledColor: Int = strokeNormalColor,
    radius: Float,
    radiusLeftTop: Float = 0f, radiusLeftBottom: Float = 0f,
    radiusRightTop: Float = 0f, radiusRightBottom: Float = 0f,
) {
    val radiusArrayOf = floatArrayOf(
        radiusLeftTop, radiusLeftTop,  //左上
        radiusRightTop, radiusRightTop,  //右上
        radiusRightBottom, radiusRightBottom,  //右下
        radiusLeftBottom, radiusLeftBottom //左下
    )
    val normal: Drawable = GradientDrawable()
        .apply { if (radius > 0) cornerRadius = radius else cornerRadii = radiusArrayOf }
        .apply {
            orientation = when (gradientTypeNormal) {
                top_bottom -> TOP_BOTTOM
                tr_bl -> TR_BL
                right_left -> RIGHT_LEFT
                br_tl -> BR_TL
                bottom_top -> BOTTOM_TOP
                bl_tr -> BL_TR
                left_right -> LEFT_RIGHT
                tl_br -> TL_BR
                else -> TOP_BOTTOM
            }
            colors = intArrayOf(normalColor, normalColorEnd)
            setStroke(strokeWidth, strokeNormalColor)
        }
    val pressed: Drawable = GradientDrawable()
        .apply { if (radius > 0) cornerRadius = radius else cornerRadii = radiusArrayOf }
        .apply {
            orientation = when (gradientTypePressed) {
                top_bottom -> TOP_BOTTOM
                tr_bl -> TR_BL
                right_left -> RIGHT_LEFT
                br_tl -> BR_TL
                bottom_top -> BOTTOM_TOP
                bl_tr -> BL_TR
                left_right -> LEFT_RIGHT
                tl_br -> TL_BR
                else -> TOP_BOTTOM
            }
            colors = intArrayOf(pressedColor, pressedColorEnd)
            setStroke(strokeWidth, strokePressedColor)
        }
    val enabled: Drawable = GradientDrawable()
        .apply { if (radius > 0) cornerRadius = radius else cornerRadii = radiusArrayOf }
        .apply {
            orientation = when (gradientTypeNoEnabled) {
                top_bottom -> TOP_BOTTOM
                tr_bl -> TR_BL
                right_left -> RIGHT_LEFT
                br_tl -> BR_TL
                bottom_top -> BOTTOM_TOP
                bl_tr -> BL_TR
                left_right -> LEFT_RIGHT
                tl_br -> TL_BR
                else -> TOP_BOTTOM
            }
            colors = intArrayOf(noEnabledColor, noEnabledColorEnd)
            setStroke(strokeWidth, strokeNoEnabledColor)
        }
    background = StateListDrawable().apply {
        addState(intArrayOf(attr.state_pressed), pressed) // 按下状态 , 设置按下的图片
        addState(intArrayOf(-attr.state_enabled), enabled) // 默认状态,默认状态下的图片
        addState(intArrayOf(), normal) // 不可点击状态
        //设置状态选择器过度动画/渐变选择器/渐变动画
        //        drawable.setEnterFadeDuration(500);
        //        drawable.setExitFadeDuration(500);
    }
}

//<editor-fold defaultstate="collapsed" desc="用于向调用者告知推荐使用值的自定义注解,和收集到的各种值">

//<editor-fold defaultstate="collapsed" desc="对于代码生成图片的渐变方法声明">
//    @IntRangeKt(top_bottom, tr_bl, right_left, br_tl, bottom_top, bl_tr, left_right, tl_br)
const val top_bottom = 0
const val tr_bl = 1
const val right_left = 2
const val br_tl = 3
const val bottom_top = 4
const val bl_tr = 5
const val left_right = 6
const val tl_br = 7
//</editor-fold>
//</editor-fold>

//创建一个具有渐变属性的 图片背景drawable 可以设置边框
fun View.setCustomDrawable(
    @CanUseInt(top_bottom, tr_bl, right_left, br_tl, bottom_top, bl_tr, left_right, tl_br)
    gradientType: Int,
    startColor: Int, endColor: Int, radius: Float,
    strokeWidth: Int = 0, strokeNormalColor: Int = 0,
    radiusLeftTop: Float = 0f, radiusLeftBottom: Float = 0f,
    radiusRightTop: Float = 0f, radiusRightBottom: Float = 0f,
) {
    val radiusArrayOf = floatArrayOf(
        radiusLeftTop, radiusLeftTop,  //左上
        radiusRightTop, radiusRightTop,  //右上
        radiusRightBottom, radiusRightBottom,  //右下
        radiusLeftBottom, radiusLeftBottom //左下
    )
    background = GradientDrawable()
        .apply { if (radius > 0) cornerRadius = radius else cornerRadii = radiusArrayOf }
        .apply {
            orientation = when (gradientType) {
                top_bottom -> TOP_BOTTOM
                tr_bl -> TR_BL
                right_left -> RIGHT_LEFT
                br_tl -> BR_TL
                bottom_top -> BOTTOM_TOP
                bl_tr -> BL_TR
                left_right -> LEFT_RIGHT
                tl_br -> TL_BR
                else -> TOP_BOTTOM
            }
            colors = intArrayOf(startColor, endColor)
            setStroke(strokeWidth, strokeNormalColor)
        }
}

//fun View.shake() {
//    val animation = AnimationUtils.loadAnimation(this.context,R.anim.shake)
//    animation.fillAfter = true
//    this.startAnimation(animation)
//}

//获取所有父类
fun View?.getMyParents(): MutableList<View> {
    val parents = mutableListOf<View>()
    var myParent: View? = this?.parent as? View //找父控件
    for (i in 0..Int.MAX_VALUE) {
        if (myParent != null) {
            parents.add(myParent) //添加到父控件列表
            myParent = myParent.parent as? View //继续向上查找父控件
        } else break //找不到View的父控件即结束
    }
    return parents
}

//找到View所在的fragment
fun View?.getMyFragment(): Fragment? {
    (this?.context as? FragmentActivity)?.let { ac ->
        //找到所有上级View
        val parents = getMyParents()
        //找到一级(activity嵌套的fragment)fragment
        val fragments = ac.supportFragmentManager.fragments
        //再找二级(fragment嵌套的fragment)fragment
        val list = mutableListOf<Fragment>()
        list.addAll(fragments)
        fragments.forEach { c -> list.addAll(c.getAllChildFragments()) }
        if (list.isNotEmpty()) for (i in list.size - 1 downTo 0) {
            list[i].view?.let { v -> if (parents.contains(v)) return list[i] }
        }
    }
    return null //如果都找不到，则应该不是放在fragment中，可能直接放在activity中了
}

//获取生命周期管理
fun View?.getMyLifecycleOwner(): LifecycleOwner? {
    return (this?.getMyFragment()) ?: (this?.context as? LifecycleOwner)
}

//扩大点击范围(https://github.com/wisdomtl/Layout_DSL/blob/master/app/src/main/java/taylor/com/dsl/Layout.kt)[原文：https://juejin.cn/post/6968237652017414151]
@SuppressLint("RestrictedApi")
fun View?.expand(dx: Int, dy: Int) {
    if (this == null) return
    val parentView = parent as? ViewGroup ?: return
    if (parentView.touchDelegate == null) parentView.touchDelegate = MultiTouchDelegate(delegateView = this)
    post {
        val rect = Rect()
        ViewGroupUtils.getDescendantRect(parentView, this, rect)
        rect.inset(-dx, -dy)
        (parentView.touchDelegate as? MultiTouchDelegate)?.delegateViewMap?.get()?.put(this, rect)
    }
}

//按压处理代理
private class MultiTouchDelegate(bound: Rect? = null, delegateView: View) : TouchDelegate(bound, delegateView) {
    var delegateViewMap = WeakReference(mutableMapOf<View, Rect>())
        get() {
            if (field.get() == null) field = WeakReference(mutableMapOf())
            return field
        }
    private var delegateView: View? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        var handled = false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> delegateView = findDelegateViewUnder(x, y)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> delegateView = null
        }
        delegateView?.let {
            event.setLocation(it.width / 2f, it.height / 2f)
            handled = it.dispatchTouchEvent(event)
        }
        return handled
    }

    private fun findDelegateViewUnder(x: Int, y: Int): View? {
        delegateViewMap.get()?.forEach { entry -> if (entry.value.contains(x, y)) return entry.key }
        return null
    }
}