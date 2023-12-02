package com.ym.chat.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;

/**
 * @version V1.0
 * @createAuthor ___         ___          ___
 * /  /\       /  /\        /  /\           ___
 * /  /::\     /  /:/       /  /::\         /__/|
 * /  /:/\:\   /__/::\      /  /:/\:\    __  | |:|
 * /  /:/~/::\  \__\/\:\    /  /:/~/::\  /__/\| |:|
 * /__/:/ /:/\:\    \  \:\  /__/:/ /:/\:\ \  \:\_|:|
 * \  \:\/:/__\/    \__\:\  \  \:\/:/__\/  \  \:::|
 * \  \::/         /  /:/   \  \::/        \  \::|
 * \  \:\        /__/:/     \  \:\         \  \:\
 * \  \:\       \__\/       \  \:\         \  \:\
 * \__\/                    \__\/          \__\/
 * @createDate 2022-03-23 3:30 下午
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
public class PopUtils {


    /**
     * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
     * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
     *
     * @param anchorView 呼出window的view
     * @return window显示的左上角的xOff, yOff坐标
     */
    public static int[] calculatePopWindowPos(final View anchorView) {
        final int anchorLoc[] = new int[2];
        final int type[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorWidth = anchorView.getWidth();
        final int anchorHeight = anchorView.getHeight();
//        Log.e("calculatePopWindowPos", "anchorHeight=" + anchorHeight + "-----anchorWidth=" + anchorWidth);
//        Log.e("calculatePopWindowPos", "anchorH=" + anchorLoc[1] + "-----anchorW=" + anchorLoc[0]);
        // 获取屏幕的高宽
        final int screenHeight = getScreenHeight(anchorView.getContext());
//        final int screenWidth = getScreenWidth(anchorView.getContext());
//        Log.e("calculatePopWindowPos", "screenHeight=" + screenHeight + "-----screenWidth=" + screenWidth);
//        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
//        final int windowHeight = SizeUtils.dp2px(60.0f);
//        final int windowWidth = SizeUtils.dp2px(200.0f);
        int popupHeight = 120;
        type[0] = 0;
//        Log.e("calculatePopWindowPos", "windowHeight=" + windowHeight + "-----s=" + (screenHeight - anchorHeight < windowHeight));
        final int windowTopHeight = SizeUtils.dp2px(120 + popupHeight);
//        final int windowMaxHeight = SizeUtils.dp2px(90 + 100 + 2 * 60);
//        Log.e("calculatePopWindowPos", "windowTopHeight=" + windowTopHeight+"--------windowMaxHeight="+windowMaxHeight);
        if (anchorLoc[1] > windowTopHeight) {
            //显示顶部
            type[0] = 0;
        } else if (screenHeight - anchorHeight < windowTopHeight) {
            //显示中间
                type[0] = 2;
                if (anchorLoc[1] > 0)
                    type[1] = windowTopHeight + popupHeight;
                else
                    type[1] = Math.abs(anchorLoc[1]) + windowTopHeight + popupHeight;
//                Log.e("calculatePopWindowPos", "type[1]=" + type[1]);
        } else if (anchorLoc[1] < windowTopHeight) {
            //显示底部
            type[0] = 1;
        } else {
            //显示顶部
            type[0] = 0;
        }
        return type;
    }

    /**
     * 获取屏幕高度(px)
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取屏幕宽度(px)
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
