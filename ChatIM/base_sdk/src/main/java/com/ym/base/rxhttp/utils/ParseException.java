package com.ym.base.rxhttp.utils;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

/**
 * User: ljx
 * Date: 2018/10/23
 * Time: 22:29
 */
public class ParseException extends IOException {

    private final String errorCode;

    private final String requestMethod; //请求方法，Get/Post等
    private final String requestUrl; //请求Url及参数
    private final Headers responseHeaders; //响应头

    public ParseException(@NonNull String code, String message, Response response) {
        super(message);
        errorCode = code;

        Request request = response.request();
        requestMethod = request.method();
        requestUrl = request.url().toString();
        responseHeaders = response.headers();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return errorCode;
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" +
                "\n\n" + requestMethod + ": " + requestUrl +
                "\n\nCode = " + errorCode + " message = " + getMessage() +
                "\n\n" + responseHeaders;
    }
}
