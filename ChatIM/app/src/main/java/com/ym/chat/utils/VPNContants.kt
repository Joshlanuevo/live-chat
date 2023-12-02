package com.ym.chat.utils

import android.os.Environment
import com.ym.chat.bean.HostUrlBean
import com.ym.chat.bean.UpdateVersionBean
import com.ym.chat.rxhttp.ApiUrl

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/30 16:02
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
object VPNContants {

    var uuid = System.currentTimeMillis()

    //首次检测网速阀值低于该数据时才能使用
    val NORMAL_NETTIME = 280

    //VPN节点列表字符串
    var vpnNodeListStr: MutableList<String> = mutableListOf()
    //缓存域名数据
    var cacheHost: MutableList<HostUrlBean.HostBean> = mutableListOf()

    //更新弹窗
    var updateReult: UpdateVersionBean? = null

    //公告地址
    var noticeH5Host: String? = null

    //包网地址
    var bwHostUrl = ""

    //当前真实IP
    var realIp = ""

    //当前使用节点IP
    var currentNodeIp = ""

    //SP名字
    val SPNAME = "Jlyl"

    //是否第一次启动
    val ISFIRST_START = "ISFIRST_START"

    //异常上报
    val ERROR_UPURL = "ERROR_UPURL"

    //正常上报
    val NORMAL_UPURL = "NORMAL_UPURL"

    //上一次可用节点
    val LAST_NODE = "LAST_NODE"

    //启动图片
    val SPLASH_IMAGE = "SPLASH_IMAGE"

    //主地址
    val MAIN_URL = "mainUrl"

    //应急地址
    val URGENT_URL = "urgentUrl"

    //启动图显示
    val SPLASH_INDEX = "SPLASH_INDEX"

    //缓存的VPN节点获取地址
    val CACHE_VPNAPI = "CACHE_VPNAPI"

    //apk存储地址
    val apkDownLoadFile = Environment.getExternalStorageDirectory().absolutePath + "/Download/amtk"
}