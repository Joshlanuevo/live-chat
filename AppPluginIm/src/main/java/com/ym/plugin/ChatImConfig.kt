package com.ym.plugin

/**
 * 友聊 App 配置
 */
class ChatImConfig : AppConfig() {
    //版本code
    override var versionCode = 109

    //版本名称，开发的版本号固定0.0.0
    //当前测试环境版本 0.3.8
    //当前生产环境版本 1.0.3
    override var versionName = "1.0.1"

    //包名
    override var applicationId = "com.youliao.chat"

    //app名称（手机桌面显示）
    override var appName = "友聊"
}