package com.ym.base.rxhttp;

import com.ym.base.SabaToken;
import com.ym.base.constant.HostManager;
import com.ym.base.ext.StringExtKt;
import com.ym.base.rxhttp.parser.OtherJsonParser;
import com.ym.base.util.save.MMKVUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import rxhttp.RxHttp;

import static com.ym.base.constant.EventKeys.SABA_TOKEN;

/**
 * 沙巴体育token失效问题处理
 */
public class TokenInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request tempRequest = chain.request();
        Request.Builder builder = tempRequest.newBuilder();

        return chain.proceed(builder.build());
    }

    private void refreshSabaToken() {
        synchronized (this) {
            try {
                SabaToken token = (SabaToken) RxHttp.postJson(HostManager.INSTANCE.getSaba_API() + "refreshToken")
                        .add("vendor_id", "tyt1ouap9i")
                        .add("vendor_member_id", "kvn" + MMKVUtils.INSTANCE.getUserName()).execute(new OtherJsonParser<SabaToken>(SabaToken.class));
                MMKVUtils.putObject(SABA_TOKEN, token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}