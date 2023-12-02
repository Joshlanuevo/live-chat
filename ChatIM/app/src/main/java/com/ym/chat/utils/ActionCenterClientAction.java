package com.ym.chat.utils;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.just.agentweb.WebViewClient;

import java.util.ArrayList;
import java.util.List;

public class ActionCenterClientAction extends WebViewClient {

    private OnTitleStateListener mListener;
    private OnStart onStartListener;

    public interface OnTitleStateListener {
        void titleState(boolean isShow);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
        if (onStartListener != null) {
            onStartListener.onstartLoad();
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    public ActionCenterClientAction(OnTitleStateListener listener, OnStart start) {
        mListener = listener;
        onStartListener = start;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();//忽略证书错误继续加载页面
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        //IP映射
//        if (WebActivityActivity.isOpenVPN)
//            request.getRequestHeaders().put("X-Tenant-Forward-For", GetNewWorkIPUtils.IP);
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onPageFinished(WebView view, String url) {

        List<String> filterUrl = new ArrayList<>();
        filterUrl.add("chatWindow.aspx");
        filterUrl.add("livelyhelp.chat");
        filterUrl.add("about:blank");
        filterUrl.add("move/mobile.html");
        filterUrl.add("/MgX1H.html");
        filterUrl.add("/tPcjG.html");
        filterUrl.add("1dbg.app");

        boolean isContains = false;
        if (!TextUtils.isEmpty(url)) {
            for (String s : filterUrl) {
                if (url.contains(s)) {
                    isContains = true;
                    break;
                }
            }
            if (isContains) {
                if (mListener != null) {
                    mListener.titleState(true);
                }
            } else {
                if (mListener != null) {
                    mListener.titleState(false);
                }
            }
        } else {
            if (mListener != null) {
                mListener.titleState(false);
            }
        }
        super.onPageFinished(view, url);
    }

    public interface OnStart {
        void onstartLoad();
    }

}


