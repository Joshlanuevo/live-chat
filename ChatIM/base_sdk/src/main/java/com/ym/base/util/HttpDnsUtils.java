package com.ym.base.util;

import static java.net.InetAddress.getAllByName;
import static java.net.InetAddress.getByName;

import android.util.Log;

import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.local.Resolver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

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
 * @createDate 2022.4.16 10:45 上午
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
public class HttpDnsUtils implements Dns {

    private String TAG = "HttpDnsUtils";
    private DnsManager dnsManager;

    public HttpDnsUtils() throws UnknownHostException {
        IResolver[] resolvers = new IResolver[1];
        resolvers[0] = new Resolver(getByName("10.4.12.252"));//test 10.4.12.252
        dnsManager = new DnsManager(NetworkInfo.normal, resolvers);
        Log.d(TAG, "-----初始化 HttpDnsUtils-----");
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (dnsManager == null)  //当构造失败时使用默认解析方式
            return Dns.SYSTEM.lookup(hostname);
        Log.d(TAG, "------dnsManager=" + dnsManager);
        try {
            String[] ips = dnsManager.query(hostname);  //获取HttpDNS解析结果
            if (ips == null || ips.length == 0) {
                return Dns.SYSTEM.lookup(hostname);
            }

            List<InetAddress> result = new ArrayList<>();
            for (String ip : ips) {  //将ip地址数组转换成所需要的对象列表
                result.addAll(Arrays.asList(getAllByName(ip)));
            }
            Log.d(TAG, "------ip size=" + result.size());
            //在返回result之前，我们可以添加一些其他自己知道的IP
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //当有异常发生时，使用默认解析
        return Dns.SYSTEM.lookup(hostname);
    }
}