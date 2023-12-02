package com.ym.chat.utils;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.ym.base.util.save.MMKVUtils;

import java.util.ArrayList;
import java.util.List;

public class GameWebViewClient extends com.just.agentweb.WebViewClient {

    private OnTitleStateListener mListener;

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
        String token = MMKVUtils.INSTANCE.getString("xtoken");
//        token = "XVWsyQMCL/AJrxaLqxdV+k5FmTx9F7367+fbQX1U+rvjLwzqwdqTXIspwzHPiQwy";
        if (!TextUtils.isEmpty(token)) {
            view.loadUrl("javascript: localStorage.setItem('xToken', '" + token + "');");
        }
        view.loadUrl("javascript: document.getElementsByClassName(‘dataType’)[0].style.display = ‘none’");
    }

    public interface OnTitleStateListener {
        void titleState(boolean isShow);
    }

    public GameWebViewClient(OnTitleStateListener listener) {
        mListener = listener;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//        LogHelp.INSTANCE.d("url==" + request.getMethod() + "==" + request.getUrl().toString() + "==" + request.getRequestHeaders());
        String token = request.getRequestHeaders().get("X-Token");
        if (!TextUtils.isEmpty(token)) {
            MMKVUtils.INSTANCE.putString("xtoken", token);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();//忽略证书错误继续加载页面
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        List<String> filterUrl = new ArrayList<>();
        filterUrl.add("form/payorder.html");
        filterUrl.add("http://goopay.la");
        filterUrl.add("HuangJinYeZhiFuRedirect");
        filterUrl.add("Pay_Index.html");
        filterUrl.add("HuiFengZhiFu5Redirect");
        filterUrl.add("payorderzz");
        filterUrl.add("pays/banktransferfirststep");
        filterUrl.add("MgX1H.html");
        filterUrl.add("tPcjG.html");
        filterUrl.add("form/payorderzz.html");
        filterUrl.add("page/pay");
        filterUrl.add("MobileDeposit");
        filterUrl.add("phonecharge");
        filterUrl.add("pay/match");
        filterUrl.add("alipay.com");
        filterUrl.add("order/place");
        filterUrl.add("api/order");
        filterUrl.add("SanBaoZhiFuRedirect");
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

}

