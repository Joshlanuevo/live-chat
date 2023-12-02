package com.ym.base.widget.ext

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter

/**
 * Author:case
 * Date:2020/8/11
 * Time:17:33
 */


fun RecyclerView.initLinearManager(mAdapter: BaseQuickAdapter<*, *>) {
    layoutManager = LinearLayoutManager(context).apply {
        orientation = LinearLayoutManager.VERTICAL
    }
    adapter = mAdapter
}

fun RecyclerView.initGridManager(mAdapter: BaseQuickAdapter<*, *>, spanCount:Int ) {
    layoutManager = GridLayoutManager(context,spanCount).apply {
    }
    adapter = mAdapter
}
