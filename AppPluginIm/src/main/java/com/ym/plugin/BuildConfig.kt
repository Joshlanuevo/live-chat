package com.ym.plugin


/**
 * App编译配置
 */
object BuildConfig {

    val compileSdkVersion = 30
    val buildToolsVersion = "30.0.3"
    val minSdkVersion = 23
    val targetSdkVersion = 30


    /**
     * 获取app配置
     */
    val app: AppConfig = ChatImConfig()
}