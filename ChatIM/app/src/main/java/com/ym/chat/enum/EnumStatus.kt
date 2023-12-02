package com.ym.chat.enum


/**
 * 申请 好友  入群状态
 * 状态:待处理 pending,已接受 accepted,已拒绝 refused
 */
enum class AskStatus(var status: String) {
    Pending("Pending"),
    Accepted("Accepted"),
    Refused("Refused")
}


/**
 * 申请 好友  入群 类型
 * 类型:加好友 friend,加群 group,邀请加群 inviteGroup
 */
enum class AskType(var status: String) {
    friend("friend"),
    group("group"),
    inviteGroup("inviteGroup")
}


/**
 * 群成员身份
 * 普通成员 normal,管理员 admin,群主 owner
 */
enum class GroupMemberType(var status: String) {
    normal("Normal"),
    admin("Admin"),
    owner("Owner")
}


/**
 * 发送短信
 *短信类型 注册 Register, 登录 Login, 更换密码 ChangePassword, 重置密码 ForgetPassword
 */
enum class SendCodeType(var status: String) {
    Register("Register"),
    Login("Login"),
    ChangePassword("ChangePassword"),
    ForgetPassword("ForgetPassword"),
    EditMobile("EditMobile")
}

/**
 * 发送消息类型
 *类型文本 text,语音消息 voiceMsg,视频消息 videoMsg,表情 emoji,图片 picture,名片 visitingCard,文件 file
 */
enum class SendMsgType(var type: String) {
    text("text"),
    voiceMsg("voiceMsg"),
    videoMsg("videoMsg"),
    emoji("emoji"),
    picture("picture"),
    visitingCard("visitingCard"),
    file("file")
}