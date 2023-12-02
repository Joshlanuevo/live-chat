package com.ym.base.rxhttp;

import android.text.TextUtils;

import com.ym.base.constant.HostManager;
import com.ym.base.ext.StringExtKt;
import com.ym.base.util.MurmurHash3;
import com.ym.base.util.PLATFORM_KEY;
import com.ym.base.util.XXTEA;
import com.ym.base_sdk.BuildConfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 处理加签加密拦截器
 */
public class SignaInterceptor implements Interceptor {

    private int SEED = 24;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        Request tempRequest = chain.request();
        Request.Builder builder = tempRequest.newBuilder();

        /**
         * 只处理我们自己接口
         */
        String requestUrl = tempRequest.url().toString();
        boolean has = false;
        String matchHost = "";
        String host = StringExtKt.getHost(HostManager.YnHost.INSTANCE.getApiUrlDefault());
//        String debugHost = StringExtKt.getHost(HostManager.YnHost.apiUrlDefaultDebug);
//        String debugRelease = StringExtKt.getHost(HostManager.YnHost.apiUrlDefault);
        if (requestUrl.contains(HostManager.YnHost.INSTANCE.getApiUrlDefault())) {//默认的地址，没有加到list
            matchHost = host;
            has = true;
        } else {//列表中的Host加签
            for (String s : HostManager.YnHost.INSTANCE.getHostsApi()) {
                if (requestUrl.contains(s)) {
                    matchHost = s;
                    has = true;
                    break;
                }
            }
        }
        if (has) {
            //处理入参加密
            if (tempRequest.method().equalsIgnoreCase("POST")) {
                //post请求，加密&加签
                RequestBody requestBody = tempRequest.body();
                if (requestBody instanceof FormBody) {
                    FormBody formBody = (FormBody) requestBody;

                    //取出原来的请求参数
                    StringBuffer tempSb = new StringBuffer();
                    for (int i = 0; i < formBody.size(); i++) {
                        if (tempSb.length() > 0) {
                            tempSb.append("&");
                        }
                        tempSb.append(formBody.encodedName(i));
                        tempSb.append("=");
                        tempSb.append(formBody.encodedValue(i));
                    }

                    if (tempSb.length() > 0) {
                        String result = XXTEA.encryptToBase64String(tempSb.toString(), getPlatForm().getKey());

                        //转换成新的body，放入加密入参
                        MediaType MEDIA_TYPE = MediaType.parse("text/plain; charset=UTF-8");
                        RequestBody newBody = RequestBody.create(MEDIA_TYPE, result);
                        builder.post(newBody);
                    }

                    //处理加签
                    String temeStamp = tempRequest.header("X-Ca-Timestamp");
                    String version = tempRequest.header("v");
                    int index = requestUrl.indexOf(matchHost) + matchHost.length();
                    String routeUrl = requestUrl.substring(index);

                    //拼接加签原内容：密文+key+时间戳+路由
                    StringBuffer signSb = new StringBuffer();
                    //密文
                    signSb.append(XXTEA.encryptToBase64String(tempSb.toString(), getPlatForm().getKey()));
                    //key
                    signSb.append(getPlatForm().getKey());
                    //时间戳
                    signSb.append(temeStamp);
                    //路由
                    signSb.append(routeUrl);
                    //版本号
                    signSb.append(version);

                    //加签请求头
                    String before = signSb.toString();
                    String after = MurmurHash3.hash_x86_128(before.getBytes(), before.length(), SEED, true);
                    builder.addHeader("X-Ca-Nonce", after);

                    //方便测试查看明文参数
                    if (BuildConfig.DEBUG) {
                        builder.addHeader("Test-Params", tempSb.toString());
                    }
                }
            } else {
                //get请求，只有加签
                //处理加签
                String temeStamp = tempRequest.header("X-Ca-Timestamp");
                String version = tempRequest.header("v");
                StringBuffer paramsSb = new StringBuffer();

                //取出路由
                int startIndex = requestUrl.indexOf(matchHost) + matchHost.length();
                int endIndex = requestUrl.indexOf("?");
                if (endIndex == -1) {
                    endIndex = requestUrl.length();
                }
                String routeUrl = requestUrl.substring(startIndex, endIndex);

                //取出参数
                Set<String> paramNames = tempRequest.url().queryParameterNames();
                for (String s : paramNames) {
                    String paramsValue = tempRequest.url().queryParameter(s);
                    if (paramsSb.length() > 0) {
                        paramsSb.append("&");
                    }
                    paramsSb.append(s);
                    paramsSb.append("=");
                    if (!TextUtils.isEmpty(paramsValue)) {
                        paramsSb.append(paramsValue);
                    }
                }

                //拼接加密原内容：key+时间戳+路由
                StringBuffer signSb = new StringBuffer();
                //key
                signSb.append(getPlatForm().getKey());
                //时间戳
                signSb.append(temeStamp);
                //路由
                signSb.append(routeUrl);
                if (paramsSb.length() > 0) {
                    signSb.append("?" + paramsSb.toString());
                }

                //版本号
                signSb.append(version);

                //加签请求头
                String before = signSb.toString();
                String after = MurmurHash3.hash_x86_128(before.getBytes(), before.length(), SEED, true);
                builder.addHeader("X-Ca-Nonce", after);
            }
        }

        return chain.proceed(builder.build());
    }

    private PLATFORM_KEY getPlatForm(){
        return PLATFORM_KEY.PLATFORM_27;
    }
}

