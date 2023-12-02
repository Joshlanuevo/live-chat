package com.ym.chat.widget

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatScrollListener(
    val firstShowChange: ((firstShowPosition: Int) -> Unit),
    val lastItemShowChange: ((lastShowPosition: Int) -> Unit),
    val scrollYListener: ((dy: Int) -> Unit),
    val onScrollStateChanged: ((recyclerView: RecyclerView, newState: Int) -> Unit),
) :
    RecyclerView.OnScrollListener() {
    private var firstShowPosition: Int = 0//第一个完整显示item 的序列号
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        val manager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        //最后一个显示
        val lastShowPosition = manager.findLastVisibleItemPosition()

        //第一个显示
        firstShowPosition = manager.findFirstVisibleItemPosition()

        firstShowChange.invoke(firstShowPosition)
        lastItemShowChange.invoke(lastShowPosition)

        scrollYListener.invoke(dy)

        super.onScrolled(recyclerView, dx, dy)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        onScrollStateChanged.invoke(recyclerView, newState)
        super.onScrollStateChanged(recyclerView, newState)
    }
}