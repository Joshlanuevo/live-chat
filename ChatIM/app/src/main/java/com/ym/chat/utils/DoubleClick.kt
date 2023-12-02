package com.ym.chat.util

import android.os.SystemClock

class DoubleClick {
    private var mLastClickTime: Long = 0

    fun isDoubleClick(): Boolean {
        return isDoubleClick(300)
    }

    fun isDoubleClick(spaceTime: Long): Boolean {
        val currentTime = SystemClock.elapsedRealtime()  //当前点击时时间
        if (currentTime - mLastClickTime <= spaceTime && mLastClickTime != 0L) {
            return true
        }
        mLastClickTime = currentTime
        return false
    }

    fun isNotFastClick(): Boolean {
        return isNotFastClick(300)
    }

    fun isNotFastClick(spaceTime: Long): Boolean {
        val currentTime = SystemClock.elapsedRealtime()  //当前点击时时间
        val isNotFastClick = currentTime - mLastClickTime > spaceTime     //是否允许点击
        if (isNotFastClick) {
            mLastClickTime = currentTime
        }
        return isNotFastClick
    }
}