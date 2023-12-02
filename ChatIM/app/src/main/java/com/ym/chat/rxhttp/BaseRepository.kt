package com.ym.chat.rxhttp

import com.blankj.utilcode.util.DeviceUtils
import com.ym.base.util.save.MMKVUtils
import java.util.*

/**
 * @Description
 * @Author：CASE
 * @Date：2021-07-14
 * @Time：14:12
 */
abstract class BaseRepository {
    //<editor-fold defaultstate="collapsed" desc="基础参数封装">
    //基础参数
    fun getBaseParams(addToken: Boolean = false): HashMap<String, Any> {
        return hashMapOf<String, Any>().apply {
//            MMKVUtils.getIMToken()?.let { token -> if (addToken) put("t", token) }//Token
//            put("lang", "zh-CN")//中文
//            put("device_no", DeviceUtils.getUniqueDeviceId())//设备号
        }
    }

    //基础Header
    fun getBaseHeaders(): HashMap<String, String> {
        return hashMapOf<String, String>().apply {
//            put("d", "27")// 26 ios 27 android
//            put("lang", "zh-CN")//中文
//            MMKVUtils.getIMToken()?.let { token -> put("t", token) }//Token
        }
    }
    //</editor-fold>
}