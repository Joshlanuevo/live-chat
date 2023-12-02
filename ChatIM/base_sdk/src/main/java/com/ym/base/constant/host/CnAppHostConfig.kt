package com.ym.base.constant.host

import com.ym.base_sdk.BuildConfig

/**
 * 国内全站app，地址池
 */
class CnAppHostConfig : HostConfig() {
    override fun getDlHostList(): MutableList<String> {
        return mutableListOf(
            "dl.afafay.xyz",
            "dl.afafaystar.xyz",
            "dl.myfaow.xyz",
            "dl.wefaow.xyz",
            "dl.yafds.xyz",
        )
    }

    override fun getH5HostList(): MutableList<String> {
        return getYmHostList()
    }

    override fun getYmHostList(): MutableList<String> {
        return mutableListOf(
            "api.afafay.xyz",
            "api.afafaystar.xyz",
            "api.myfaow.xyz",
            "api.wefaow.xyz",
            "api.yafds.xyz",
        )
    }

    override fun getJsonHostList(): MutableList<String> {
        return mutableListOf(
            "dl.afafay.xyz",
            "dl.afafaystar.xyz",
            "dl.myfaow.xyz",
            "dl.wefaow.xyz",
            "dl.yafds.xyz",
        )
    }

    override fun getYqHostList(): MutableList<String> {
        return mutableListOf(
            "yq.afafay.xyz",
            "yq.afafaystar.xyz",
            "yq.myfaow.xyz",
            "yq.wefaow.xyz",
            "yq.yafds.xyz",
        )
    }

    override fun getDefaultHostJson(): MutableList<String> {
        return mutableListOf(
            "https://dl.ymenet.xyz/app/json/domain.json",
            "https://dl.afafay.xyz/app/json/domain.json",
            "https://dl.afafaystar.xyz/app/json/domain.json",
            "https://dl.myfaow.xyz/app/json/domain.json",
            "https://dl.wefaow.xyz/app/json/domain.json",
            "https://dl.yafds.xyz/app/json/domain.json",
        )
    }

    override fun getApiDefaultHost(): String {
        return when (BuildConfig.type) {
            //生产环境
            "release" -> "https://api.afafay.xyz/"
            //uat环境
            "uat" -> "https://api.afafay.xyz/"
            //测试环境
            "debug" -> "http://h5.yostata.xyz/"
            else -> "https://api.afafay.xyz/"
        }
    }

    override fun getH5DefaultHost(): String {
        return getApiDefaultHost()
    }

    override fun getYqDefaultHost(): String {
        return when (BuildConfig.type) {
            //生产环境
            "release" -> "https://yq.kokvn10.com/iosign/"
            //uat环境
            "uat" -> "https://yq.kokvn10.com/iosign/"
            //测试环境
            "debug" -> "https://iosqianm.ystata.xyz/iosign/"
            else -> "https://yq.kokvn10.com/iosign/"
        }
    }

    override fun getDlDefaultHost(): String {
        return when (BuildConfig.type) {
            //生产环境
            "release" -> "https://dl.afafay.xyz/"
            //uat环境
            "uat" -> "https://dl.afafay.xyz/"
            //测试环境
            "debug" -> "https://dl.yostata.xyz/"
            else -> "https://dl.afafay.xyz/"
        }
    }
}