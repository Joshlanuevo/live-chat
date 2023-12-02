package com.ym.chat.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import kotlin.math.min


class MaxHeightNestedScrollView @JvmOverloads constructor(c: Context, a: AttributeSet? = null, d: Int = 0) : NestedScrollView(c, a, d) {
    private val mMaxHeight = 240
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val child = getChildAt(0)
        child.measure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(child.measuredWidth, min(child.measuredHeight, mMaxHeight))
    }
}