package com.ym.chat.utils;

import android.webkit.WebSettings;

public class WebViewUtils {

    /**
     * webview设置
     */
    public static void initWebViewSetting(WebSettings mWebSettings) {
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setAllowContentAccess(true);
        mWebSettings.setAppCacheEnabled(false);
        mWebSettings.setBuiltInZoomControls(false);
        mWebSettings.setUseWideViewPort(true);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setDomStorageEnabled(true);//开启本地DOM存储
        mWebSettings.setLoadsImagesAutomatically(true); // 加载图片
        mWebSettings.setMediaPlaybackRequiresUserGesture(false);//播放音频，多媒体需要用户手动？设置为false为可自动播放
    }
}
