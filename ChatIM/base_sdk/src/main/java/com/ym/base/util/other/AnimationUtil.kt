package com.ym.base.util.other

import android.view.animation.Animation
import android.view.animation.TranslateAnimation

/**
 * view 显示动画
 */
object AnimationUtil {

    /**
     * 从控件所在位置移动到控件的底部
     * @return
     */
    fun moveToViewBottom(): TranslateAnimation? {
        val mHiddenAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 1.0f
        )
        mHiddenAction.duration = 300
        return mHiddenAction
    }

    /**
     * 从控件的底部移动到控件所在位置
     *
     * @return
     */
    fun moveToViewLocation(): TranslateAnimation? {
        val mHiddenAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        mHiddenAction.duration = 300
        return mHiddenAction
    }
}