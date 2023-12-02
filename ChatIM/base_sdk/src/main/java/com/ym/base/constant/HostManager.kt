package com.ym.base.constant

import com.ym.base.constant.host.CnAppHostConfig
import com.ym.base.constant.host.HostConfig
import com.ym.base.constant.host.YnAppHostConfig
import com.ym.base.util.save.MMKVUtils
import com.ym.base_sdk.BuildConfig

object HostManager {

    //链路切换开关,true开启线路切换，自动选择最优；fasle关闭线路切换，使用默认值
    val isOpenLinkSwitch = BuildConfig.type == "release"

    //第三方接口host
    /**
     * IM体育的
     */
    val IM_API = "http://ipis-ykyule.imapi.net/" //IM体育正式环境
    //val IM_API = "http://ipis-ykyule-test.imapi.net/" //IM体育地址-测试环境

    /**
     * 沙巴体育的
     */
//    val Saba_API = "https://apistaging.wx7777.com/" //Saba体育地址新文档
    val Saba_Version = "beta" //Saba体育版本，新文档需要使用
//    val Saba_API = "https://bobsbapi.pl773.com/" //BOB的Saba体育地址
    val Saba_API = "https://j7s8tfd.bw6688.com/" //saba测试环境地址
//    val Saba_API = "https://j7s8ofd.bw6688.com/" //saba正式环境地址


    //越南站连接host
    object YnHost {
        val apiUrlDefault = getApiConfig().getApiDefaultHost()
        val dlUrlDefault = getApiConfig().getDlDefaultHost()
        val yunQueUrlDefault = getApiConfig().getYqDefaultHost()


        //每次动态从MMKV读取，需要修改，保存新值即可
        val dlUrl get() = MMKVUtils.getHostDl() //文件地址(图片、Json等)
        val h5Url get() = MMKVUtils.getHostApi() //YM-H5地址
        val YUN_QUE get() = MMKVUtils.getHostYunQue()//云雀地址
        val ymUrl get() = MMKVUtils.getHostApi() //YM新地址

        /**
         * Ym主要API
         */
        var hostsApi = getApiConfig().getYmHostList()

        /**
         * 静态json文件
         */
        var hostsDl = getApiConfig().getDlHostList()

        /**
         * 云雀
         */
        var yunQueH5 = getApiConfig().getYqHostList()

        //动态获取Host文件内容
        var hostJson = getApiConfig().getDefaultHostJson()
    }

    private fun getApiConfig(): HostConfig {
        return CnAppHostConfig()
    }
}