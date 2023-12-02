package com.ym.base.rxhttp.utils;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 日志拦截
 */
public class LogInterceptor implements Interceptor {
    private final Charset UTF8 = Charset.forName("UTF-8");

    private final String TAG = "FORMAT";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //向服务器发送的数据
        RequestBody requestBody = request.body();

        String body = null;
        
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            body = buffer.readString(charset);
        }

        Log.e(TAG, "║ StartRequest:" + request.url());

        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        String rBody = null;

        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);  // Buffer the entire body.
        Buffer buffer = source.buffer();

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                e.printStackTrace();
            }
        }
        rBody = buffer.clone().readString(charset);

        printLine(TAG, true);
        Log.e(TAG, "║ " + "Request:");
        Log.e(TAG, "║ " + "     method:" + request.method() );
        Log.e(TAG, "║ " + "     url:" + request.url());
        Log.e(TAG, "║ " + "     headers: " + request.headers());
        Log.e(TAG, "║ " + "     body:" + body );
        Log.e(TAG, "║ " + "Response:");
        Log.e(TAG, "║ " + "     time:" + tookMs);
        Log.e(TAG, "║ " + "     code:" + response.code());
        Log.e(TAG, "║ " + "     msg:" + response.message());
        Log.e(TAG, "║ " + "     body: ");
        printJson(TAG, rBody);

        return response;
    }

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.e(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Log.e(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

    public static void printJson(String tag, String msg) {
        String message;
        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(4);//最重要的方法，就一行，返回格式化的json字符串，其中的数字4是缩进字符数
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(4);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }
//        printLine(tag, true);
        message = LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            if(line.isEmpty())continue;
            Log.e(tag, "║       " + line);
        }
        printLine(tag, false);
    }
}
