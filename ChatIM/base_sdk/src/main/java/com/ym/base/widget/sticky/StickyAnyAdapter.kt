package com.ym.base.widget.sticky

import android.view.View
import com.chad.library.adapter.base.BaseBinderAdapter


abstract class StickyAnyAdapter(
    private val stickyBgColor: Int? = null,
    private val noStickyBgColor: Int? = null,
    list: MutableList<Any>? = null,
) : BaseBinderAdapter(list), StickyHeaderCallbacks {
    override fun setupStickyHeaderView(stickyHeader: View) {
        super.setupStickyHeaderView(stickyHeader)
        stickyBgColor?.let { stickyHeader.setBackgroundColor(it) }

    }

    override fun teardownStickyHeaderView(stickyHeader: View) {
        super.teardownStickyHeaderView(stickyHeader)
        noStickyBgColor?.let { stickyHeader.setBackgroundColor(it) }
    }
}