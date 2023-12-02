package com.ym.chat.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.core.app.RemoteInput
import com.blankj.utilcode.util.Utils
import com.google.android.exoplayer2.util.NotificationUtil
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.SendMsgParams
import com.ym.chat.db.ChatDao
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ImCache
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.NotificationUtils
import com.ym.chat.viewmodel.ChatViewModel
import rxhttp.wrapper.utils.GsonUtil
import java.util.*

class ReplyService : IntentService("ReplyService") {
    override fun onHandleIntent(intent: Intent?) {
        intent?.run {
            val remoteInput: Bundle = RemoteInput.getResultsFromIntent(intent)
            //获取通知栏回复输入的内容
            val message = remoteInput.getCharSequence("KEY_TEXT_REPLY").toString()
            if (TextUtils.isEmpty(message)) {
                return
            }

            val toId = intent.getStringExtra("toId");
            val chatType = intent.getStringExtra("chatType")

            if (!TextUtils.isEmpty(toId) && !TextUtils.isEmpty(chatType)) {
                sendMsg(message, toId ?: "", chatType ?: "")
            }

            //清空通知栏，该聊天数据
            NotificationUtils.clearNotification(toId ?: "")

        }
    }

    /**
     * 发送消息
     * content:消息内容
     * toFromId：对方id，如果chatTyoe为群，则为群组ID
     * chatType：单聊(ChatType.CHAT_TYPE_FRIEND)或者群聊(ChatType.CHAT_TYPE_GROUP)
     */
    fun sendMsg(contentStr: String, toId: String, type: String) {

        //生成发送消息的Bean对象
        val params = createSendParams(contentStr, toId, type).apply {
            msgType = MsgType.MESSAGETYPE_TEXT
        }

        //消息保存到数据库，并且回调给聊天页面显示
        val chatMsg = copyToChatMsgBean(params).apply {
            //当前为发送中状态
            sendState = 0
        }

        val dbBean = ChatDao.getChatMsgDb().saveChatMsg(chatMsg)
//        sendMsgResult.value = dbBean

        //生成会话列表
        if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
            ChatDao.getConversationDb().saveGroupConversation(
                chatMsg.groupId,
                chatMsg.content,
                chatMsg.msgType,
                fromId = MMKVUtils.getUser()?.id ?: ""
            )
        } else {
            ChatDao.getConversationDb().saveFriendConversation(
                chatMsg.to,
                chatMsg.content,
                chatMsg.msgType
            )
        }

        //发送消息
        WebsocketWork.WS.sendMsg(
            GsonUtil.toJson(params), success = {
                ChatDao.getChatMsgDb().updateMsgSendState(1, chatMsg.dbId, it)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSendState(2, chatMsg.dbId, "")
            }
        )
    }

    /**
     * 创建发送消息的Bean
     */
    private fun createSendParams(contentStr: String, toId: String, type: String): SendMsgParams {
        return SendMsgParams().apply {
            cmd = 11
            content = contentStr
            from = MMKVUtils.getUser()?.id ?: ""
            chatType = type
            uuid = UUID.randomUUID().toString()
            when (type) {
                //单聊
                ChatType.CHAT_TYPE_FRIEND -> to = toId
                //群聊
                ChatType.CHAT_TYPE_GROUP -> groupId = toId
                //群发消息
                ChatType.CHAT_TYPE_GROUP_SEND -> chatType = ChatType.CHAT_TYPE_FRIEND
            }
        }
    }

    /**
     * 复制成chatMessageBean
     */
    private fun copyToChatMsgBean(sendMsgParams: SendMsgParams): ChatMessageBean {
        return ChatMessageBean(0).apply {
            from = sendMsgParams.from
            msgType = sendMsgParams.msgType
            chatType = sendMsgParams.chatType
            content = sendMsgParams.content
            groupId = sendMsgParams.groupId
            uuid = sendMsgParams.uuid
            cmd = sendMsgParams.cmd
            to = sendMsgParams.to
            createTime = sendMsgParams.createTime
            dir = 1
        }
    }
}