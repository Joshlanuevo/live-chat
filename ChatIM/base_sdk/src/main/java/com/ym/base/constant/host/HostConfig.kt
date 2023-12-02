package com.ym.base.constant.host

abstract class HostConfig {
    /**
     * 文件请求地址域名池
     */
    abstract fun getDlHostList(): MutableList<String>

    /**
     * h5请求地址域名池
     */
    abstract fun getH5HostList(): MutableList<String>

    /**
     * 主要API请求地址域名池
     */
    abstract fun getYmHostList(): MutableList<String>

    /**
     * json文件域名池
     */
    abstract fun getJsonHostList(): MutableList<String>

    /**
     * 云雀域名池
     */
    abstract fun getYqHostList(): MutableList<String>

    /**
     * 默认写死的获取域名池json
     */
    abstract fun getDefaultHostJson(): MutableList<String>

    /**
     * 主要api默认host
     */
    abstract fun getApiDefaultHost(): String

    /**
     * h5跳转地址默认host
     */
    abstract fun getH5DefaultHost(): String

    /**
     * 云雀默认host
     */
    abstract fun getYqDefaultHost(): String

    /**
     *dl默认host
     */
    abstract fun getDlDefaultHost(): String
}