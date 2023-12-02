package com.ym.plugin

/**
 * App基础配置基类
 */
abstract class AppConfig {

    //版本code
    abstract var versionCode: Int

    //版本名称
    abstract var versionName: String

    //包名
    abstract var applicationId: String

    //app名称（手机桌面显示）
    abstract var appName: String

}