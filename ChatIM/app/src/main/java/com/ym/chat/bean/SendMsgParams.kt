package com.ym.chat.bean

data class SendMsgParams(
    // 发送用户id;
    var from: String = "",

    //目标用户id;
    var to: String = "",

    //消息类型
    var msgType: String = "",

    //聊天类型;(如公聊、私聊)
    var chatType: String = "",

    //消息内容;
    var content: String = "",

    //消息发到哪个群组;
    var groupId: String = "",

    //ws指令
    var cmd: Int = 0,

    //创建时间
    var createTime: Long = System.currentTimeMillis(),

    //
    var id: String = "",
    //被回复消息的id
    var parentMessageId: String = "",
    //客户端消息uuid
    var uuid: String = ""
)