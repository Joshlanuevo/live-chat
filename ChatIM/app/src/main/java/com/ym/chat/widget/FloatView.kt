package com.ym.chat.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlin.math.abs

/**
 * 浮动窗口
 */
class FloatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var floatContainer: CustomContain
    private var moving = false

    private var viewTop = 0
    private var viewLeft = 0
    private var moveTop = -1
    private var moveLeft = -1
    private var isShow = false//是否是展开状态

    private var animation: ValueAnimator = ValueAnimator()
    private val animationDuration: Long = 200

    /**
     * 设置浮动控件
     */
    fun setFloatView(floatView: View) {
        floatContainer.removeAllViews()
        floatContainer.addView(floatView)
        requestLayout()
    }

    fun setOnClickListener() {

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        var floatView = if (childCount > 0) getChildAt(0) else null
        if (floatView == null) return

        //初始化显示位置
        if (moveLeft < 0) moveLeft = 0
        if (moveTop < 0) moveTop = measuredHeight / 2
        floatView.layout(
            moveLeft,
            moveTop,
            moveLeft + floatView.measuredWidth,
            moveTop + floatView.measuredHeight
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                moving = event.x > floatContainer.left &&
                        event.x < floatContainer.left + floatContainer.measuredWidth &&
                        event.y > floatContainer.top &&
                        event.y < floatContainer.top + floatContainer.measuredHeight
                if (moving) {
                    viewLeft = (event.x - floatContainer.left).toInt()
                    viewTop = (event.y - floatContainer.top).toInt()
                    animation.cancel()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (moving) {
                    moveTop = event.y.toInt() - viewTop
                    moveLeft = event.x.toInt() - viewLeft

                    moveTop = if (moveTop < 0) {
                        0
                    } else if (moveTop + floatContainer.measuredHeight > measuredHeight) {
                        measuredHeight - floatContainer.measuredHeight
                    } else {
                        moveTop
                    }
                    moveLeft = if (moveLeft < 0) {
                        0
                    } else if (moveLeft + floatContainer.measuredWidth > measuredWidth) {
                        if (!isShow) {
                            moveLeft
                        } else {
                            measuredWidth - floatContainer.measuredWidth
                        }
                    } else {
                        moveLeft
                    }
                    requestLayout()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isShow)
                    moveLeft = 0
                startStickyAnimation(moveLeft)
                moving = false
            }
        }

        for (index in 0 until childCount) {
            getChildAt(index).onTouchEvent(event)
        }

        if (moving) {
            return true
        }
        return false
    }

    private val animatorUpdate: ValueAnimator.AnimatorUpdateListener =
        object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                moveLeft = animation!!.animatedValue as Int
                requestLayout()
            }
        }

    private fun startStickyAnimation(startX: Int) {
        animation.cancel()
        var endX =
            if ((startX + floatContainer.measuredWidth / 2) > measuredWidth / 2) (measuredWidth - floatContainer.measuredWidth) else 0
        animation.setIntValues(startX, endX)
        animation.start()
    }

    init {
        floatContainer = CustomContain(context)
        var layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        floatContainer.layoutParams = layoutParams
        addView(floatContainer)

        animation.addUpdateListener(animatorUpdate)
        animation.duration = animationDuration
    }

    class CustomContain @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : FrameLayout(context, attrs, defStyleAttr) {
        var listener: OnClickListener? = null
        private var rememberX = 0.0f
        private var rememberY = 0.0f
        override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
            return super.dispatchTouchEvent(ev)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    rememberX = event.x
                    rememberY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - rememberX) < 10 || abs(event.y - rememberY) < 10) {
                        listener?.onClick(this)
                    }
                }
            }
            return false
        }
    }

    fun updateView() {
        moveLeft = 0
        requestLayout()
    }

    fun setViewShow(isShow: Boolean) {
        this.isShow = isShow
    }

}