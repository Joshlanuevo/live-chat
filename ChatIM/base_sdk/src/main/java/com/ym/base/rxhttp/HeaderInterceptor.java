package com.ym.base.rxhttp;

import android.text.TextUtils;

import com.ym.base.util.save.MMKVUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 统一管理公共请求头
 */
public class HeaderInterceptor implements Interceptor {
    private String TAG = "HeaderInterceptor_http";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request tempRequest = chain.request();
        Request.Builder builder = tempRequest.newBuilder();

        String token = MMKVUtils.INSTANCE.getToken();
        if (!TextUtils.isEmpty(token)) {
            builder.addHeader("Authorization", token);
        }
        return chain.proceed(builder.build());
    }
}