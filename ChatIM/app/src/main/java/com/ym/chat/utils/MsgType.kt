package com.ym.chat.utils

object MsgType {
    // 阅后即焚
    const val MESSAGETYPE_DESTORY_AFTER_READ = "DestroyAfterRead"

    // 语音消息
    const val MESSAGETYPE_VOICE = "VoiceMsg"

    // 拆红包
    const val MESSAGETYPE_RECEIVE_RED_POCKET = "ReceiveRedPocketNotice"

    // 群通知
    const val MESSAGETYPE_GROUP_NOTICE = "GroupNotice"

    // Emoji
    const val MESSAGETYPE_EMOJI = "Emoji"

    // 视频消息
    const val MESSAGETYPE_VIDEO = "VideoMsg"

    //回复消息
    const val MESSAGETYPE_REPLY = "Reply"

    // 截屏
    const val MESSAGETYPE_NO_SHOT = "NoShot"

    // 签到消息
    const val MESSAGETYPE_SIGN = "Sign"

    // 好友通知
    const val MESSAGETYPE_FRIEND_NOTICE = "FriendNotice"

    // 图片消息
    const val MESSAGETYPE_PICTURE = "Picture"

    //文件消息
    const val MESSAGETYPE_FILE = "File"

    // 自定义gif图片消息
    const val MESSAGETYPE_PICTURE_GIF = "Picture_Gif"

    // 语音通话
    const val MESSAGETYPE_VOICE_CHAT = "VoiceChat"

    // 名片
    const val MESSAGETYPE_VISITING_CARD = "VisitingCard"

    // 评审
    const val MESSAGETYPE_REVIEW = "Review"

    // 系统通知
    const val MESSAGETYPE_SYSTEM_NOTICE = "SystemNotice"

    // 视频通话
    const val MESSAGETYPE_VIDEO_CHAT = "VideoChat"

    // 发红包
    const val MESSAGETYPE_SEND_RED_POCKET = "SendRedPocket"

    // 文本
    const val MESSAGETYPE_TEXT = "Text"

    //@消息
    const val MESSAGETYPE_AT = "AtText"

    // 投票
    const val MESSAGETYPE_VOTE = "Vote"

    //时间线（自定义消息类型）
    const val MESSAGETYPE_TIME = "messagetypeTime"

    //未读消息开始（自定义消息类型）
    const val MESSAGETYPE_UNREAD = "messagetypeUnread"


    //通知类型（自定义消息类型）
    const val MESSAGETYPE_NOTICE = "MESSAGETYPE_NOTICE"

    //群聊禁言
    const val ALLOWSPEAK = "AllowSpeak"

    //群聊禁言
    const val NOTICE = "Notice"

    //群聊禁言
    const val HEADERURL = "HeaderUrl"

    //群设置管理员
    const val SetMemberRole = "SetMemberRole"

    //群取消管理员
    const val CancelMemberRole = "CancelMemberRole"

    //群昵称修改
    const val GroupModifyName = "Name"

    //群头像修改
    const val HeaderUrl = "HeaderUrl"

    //群头像修改
    const val TransferGroup = "Transfer"

    //修改群公告
    const val Notice = "Notice"

    //单个成员禁言
    const val MemberAllowSpeak = "MemberAllowSpeak"
}

object ChatType {
    //单聊
    const val CHAT_TYPE_FRIEND = "Friend"

    //群聊
    const val CHAT_TYPE_GROUP = "Group"

    //群发消息
    const val CHAT_TYPE_GROUP_SEND = "GroupSend"

    //移除群成员
    const val DeleteGroupMember = "DeleteGroupMember"

    //退群
    const val Leave = "Leave"
}

object CommandType {
    //管理员邀请成员入群
    const val GROUP_ADMIN_ADD_MEMBER = 7

    //群成员被邀请加入某个群
    const val GROUP_ADD_MEMBER = 9

    //群成员被移出群
    const val GROUP_DEL_MEMBER = 10

    //聊天相关
    const val CHAT = 11//普通消息
    const val CHAT_REPLY = 37//回复消息

    //消息发送回执
    const val SEND_REBACK = 12

    //收到心跳包
    const val HEART_PACKAGE = 13

    //删除消息
    const val DELETE_MSG = 16

    //登陆反馈
    const val LOGIN_FEEDBACK = 6

    //有人添加我好友
    const val NEWFRIEND_ADDME = 24

    //删除好友
    const val DEL_FRIEND = 25

    //群操作事件
    const val GROUP_ACTION = 23

    //解散群
    const val DELETE_GROUP = 21

    //新建入群
    const val JOIN_GROUP = 22

    //离线消息
    const val OFFLINE_MSG = 20

    //远程销毁所有消息（群）
    const val DEL_ALL_MSG_GROUP = 29

    //远程销毁所有消息（好友）
    const val DEL_ALL_MSG = 30

    //发送命令好友 是否在线 群组有多少人在线
    const val SEND_FRIEND_GROUP_LINE = 32

    //接收命令好友 是否在线 群组有多少人在线
    const val GET_FRIEND_GROUP_LINE_MSG = 33

    //好友信息和群信息
    const val FRIEND_DATA = 18

    //异地登陆
    const val OTHER_LOGIN = 31

    //发送消息已读回执
    const val SEND_MSG_READ = 27

    //消息已读回执
    const val MSG_READ = 28

    //消息编辑
    const val MSG_EDIT = 34

    //系统通知
    const val SYSTEM_MSG = 35

    //新设备登录验证
    const val DEVICE_LOGIN_VERIFICATION = 36

    //清空所有消息
    const val DELETE_USER_MSG = 38

    //置顶消息
    const val TOP_MSG = 39

    //后台修改敏感词
    const val UPDATE_SENSITIVE_WORD_MSG = 40

    //意见反馈 系统通知
    const val SYSTEM_FEEDBACK_MSG = 41

    //清除本地缓存
    const val CLEAR_LOCAL_CACHE_MSG = 42

    //其他端更新了gif图片
    const val UPDATE_GIF_MSG = 43

    //多端同步删除系统通知消息
    const val DELETE_NOTIFY_MSG = 44

    //多端同步删除群申请通知消息
    const val DELETE_GROUP_NOTIFY_MSG = 45

    //多端同步个人信息 头像 名字改变
    const val EDIT_USER_NAME_AND_HEADER = 46

    //系统通知消息未读数量
    const val NOTIFICATION_COUNT = 47

    //好友信息修改
    const val NOTIFICATION_EDIT_FRIEND_INFO = 48

    //删除会话 ws 推送
    const val SEND_NOTIFICATION_DEL_CHAT_MSG = 49

    //删除会话 ws 推送
    const val NOTIFICATION_DEL_CHAT_MSG = 50

    //发送 标记会话 ws 已读/未读
    const val SEND_NOTIFICATION_UNREAD_CHAT_MSG = 51

    //接收 标记会话 ws 已读/未读
    const val NOTIFICATION_UNREAD_CHAT_MSG = 52
}

/**
 * 账号等级类型
 * Admin("管理员"),
Normal("普通"),
System("系统"),
 */
object MemberLevelType {
    //管理员
    const val MEMBER_LEVEL_ADMIN = "Admin"

    //普通
    const val MEMBER_LEVEL_NORMAL = "Normal"

    //系统
    const val MEMBER_LEVEL_SYSTEM = "System"
}