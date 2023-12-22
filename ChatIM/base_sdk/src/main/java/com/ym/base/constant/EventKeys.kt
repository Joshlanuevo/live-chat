package com.ym.base.constant


object EventKeys {
    //登入登出
    const val LOGIN_OR_OUT = "LOGIN_OR_OUT"
    //退出登陆页面的类型
    const val LOGIN_OR_OUT_TYPE = "LOGIN_OR_OUT_TYPE"

    //更新用户姓名
    const val EDIT_USER_NAME = "edit_user_name"
    const val MSG_KEYWORD = "msgKeyword"

    //更新用户信息
    const val EDIT_USER = "edit_user"
    const val UPDATE_CONVER_END = "UPDATE_CONVER_END"
    //是否显示成员名
    const val HIDE_MEMBER = "hideMember"
    //编辑消息更新
    const val EDIT_UPDATE = "editUpdate"
    const val CONNECT = "connect"
    //更新用户头像
    const val EDIT_USER_HEAD = "edit_user_head"

    //更新好友设置 是否免打扰
    const val EDIT_USER_MESSAGE_NOTICE = "edit_user_message_notice"

    //更新好友备注
    const val EDIT_FRIEND_REMARK_NOTICE = "edit_friend_remark_notice"

    //更新好友信息
    const val EDIT_FRIEND_NOTICE = "edit_friend_notice"

    //更新群设置
    const val EDIT_GROUP_INFO = "edit_group_info"

//    //成员被邀请进群成功
//    const val ADD_MEMBER_GROUP = "add_member_group"

    //成员被踢出群聊ws
    const val DEL_MEMBER_GROUP = "del_member_group"

    //添加群成员ws
    const val GROUP_ADD_MEMBER = "group_add_member"

    //后台解散群 ws
    const val SYSTEM_DEL_GROUP = "system_del_group"

    //管理员踢人
    const val ADMIN_DEL_MEMBER_GROUP = "admin_del_member_group"

    //编辑成员信息 刷新数据广播
    const val ADMIN_EDIT_MEMBER_GROUP = "admin_edit_member_group"

    //编辑成员信息 刷新数据广播
    const val ADMIN_EDIT_MEMBER_GROUP_1 = "admin_edit_member_group_1"

    //编辑成员信息 刷新数据广播
    const val ADMIN_EDIT_MEMBER_GROUP_2 = "admin_edit_member_group_2"

    //成员退出群聊  管理员解散群
    const val EXIT_GROUP = "exit_group"

    //后台更新了敏感词
    const val UPDATE_SENSITIVE_WORD_MSG = "update_sensitive_word_msg"

    //其他端更新了gif图片
    const val UPDATE_GIF_MSG = "update_gif_msg"

    //后台解散群
    const val DELETE_GROUP = "delete_group"

    //成员退出群聊  管理员解散群
    const val CLEAR_GROUP_MSG = "clear_group_msg"

    //添加好友成功
    const val ADD_FRIEND = "add_friend"

    //刷新好友跟群列表数据回调
    const val REFRESH_FRIEND_GROUP = "refresh_friend_group"

    //添加群成员成功
    const val ADD_GROUP_MEMBER = "add_group_member"

    //http请求报错
    const val HttpErrorEvent = "HttpErrorEvent"

    //绑定成功
    const val BIND_SUC = "BIND_SUC"

    //暗黑模式
    const val BLACK_MODE = "BLACK_MODE"
    const val BLACK_MODE_INDEX = "BLACK_MODE_INDEX"

    //沙巴token
    const val SABA_TOKEN = "sabaToken"

    //删除一条好友通知消息
    const val DELETE_NOTIFY_MSG = "delete_notify_msg"

    //更新系统通知
    const val UPDATE_NOTIFY = "update_notify"

    //系统验证通知消息
    const val UPDATE_NOTIFY_MSG = "update_notify_msg"

    //更新首页系统通知数量
    const val UPDATE_COUNT = "update_count"

    //通知请求全部同意消息
    const val OPERATE_NOTIFY_MSG = "operate_notify_msg"

    //消息发送状态更新
    const val SEND_STATE_UPDATE = "SEND_STATE_UPDATE"

    //消息已读
    const val MSG_READ_EVENT = "MSG_READ_EVENT"

    //更新会话列表数据
    const val UPDATE_CONVER = "UPDATE_CONVER"

    //获取历史消息开始
    const val GET_HIS_START = "get_his_start"

    //获取历史消息完成
    const val GET_HIS_COMPLETE = "get_his_complete"

    const val RE_SEND = "re_send"

    //ws连接状态
    const val WS_STATUS = "mqttStatus"

    const val LANGUAGE = "LANGUAGE"

    //新消息
    const val MSG_NEW = "MSG_NEW"

    //消息编辑
    const val MSG_EDIT = "MSG_EDIT"

    //撤回消息
    const val CHEHUI_MSG = "CHEHUI_MSG"

    //删除好友
    const val DEL_FRIEND_ACTION = "DEL_FRIEND_ACTION"

    //群聊事件
    const val GROUP_ACTION = "GROUP_ACTION"

    //群主转让
    const val TRANSFER_GROUP = "TRANSFER_GROUP"

    //刷新缓存数据
    const val REFRESH_CONVER = "REFRESH_CONVER"

    //远程销毁单条消息
    const val DEL_MSG_ONE = "DEL_MSG_ONE"

    //远程销毁所有消息，关闭聊天也没
    const val DEL_MSG_ALL = "DEL_MSG_ALL"

    //ws消息置顶
    const val MSG_TOP_WS = "MSG_TOP_WS"

    //通知好友列表从本地数据库刷新数据
    const val EVENT_REFRESH_CONTACT_LOCAL = "refreshFromLocal"

    //发送消息预显示
    const val EVENT_SEND_MSG = "eventSendMsg"

    //上传进度
    const val EVENT_UPLOAD_PROGRESS = "eventUploadProgress"

    //收藏上传进度
    const val EVENT_COLLECT_UPLOAD_PROGRESS = "eventCollectUploadProgress"

    //发送消息状态改变
    const val EVENT_SEND_MSG_CHANGED_STATE = "eventSendMsgChangedState"

    //收藏 重发 媒体消息
    const val EVENT_RESEND_SEND_MSG_CHANGED_STATE = "eventResendSendMsgChangedState"
    //接收命令好友 是否在线 群组有多少人在线
    const val GET_FRIEND_GROUP_LINE_MSG = "GET_FRIEND_GROUP_LINE_MSG"

    //gif图播放
    const val UPDATE_GIF = "UPDATE_GIF"
    //gif发送结果
    const val SENDGIF = "SendGif"

    //文件下载进度
    const val FILE_DOWNLOAD_PROCESS = "FILE_DOWNLOAD_PROCESS"

    //host地址
    const val BASE_HOSTURL = "baseHostUrlYl"
    const val WS_HOSTURL = "baseWsUrlYl"
}