package com.ym.chat.bean

import com.ym.chat.utils.VPNContants
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class LogBean(
    @Id
    var id: Long = 0,
    //日志内容
    var message: String,
    //1：上传成功；2:上传中；其他上传失败
    var upState: Int = 0,
    //日志时间
    var date: String,
    //日志类型,error，warn，info，debug
    var type: String,
    //客户端类型 Android，IOS
    var clientType: String = "Android-${android.os.Build.BRAND} ${android.os.Build.MODEL}  IP:${VPNContants.realIp}",
    //项目名称
    var projectName: String = "友聊App",
    //日志环境，dev，test，staging，prod
    var environment: String = "staging",
)