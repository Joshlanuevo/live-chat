package com.ym.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * 安全区布局  自动添加状态栏填充
 */
class SafeLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var titleView: View

    init {
        var titleHeight = 0
        var resourceId =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            titleHeight = context.resources.getDimensionPixelSize(resourceId)
        }
        orientation = LinearLayout.VERTICAL
        titleView = View(context)
        var layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, titleHeight)
        titleView.layoutParams = layoutParams
        addView(titleView)
    }

}