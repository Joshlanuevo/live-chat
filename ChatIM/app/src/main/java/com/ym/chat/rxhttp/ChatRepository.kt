package com.ym.chat.rxhttp

import android.text.TextUtils
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.ui.ChatActivity.Companion.GET_HISTORY_PAGESIZE
import com.ym.chat.utils.ChatType.CHAT_TYPE_FRIEND
import rxhttp.RxHttp
import rxhttp.toOtherJson
import rxhttp.toStr
import rxhttp.wrapper.cahce.CacheMode

object ChatRepository : BaseRepository() {

    /**
     * 收藏
     */
    suspend fun collect(content: String, type: String): BaseBean<RecordBean> {
        return RxHttp.postJson(ApiUrl.Chat.collect)
            .add("content", content)
            .add("type", type)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<RecordBean>>()
            .await()
    }

    /**
     * 删除单条消息
     *  * 单聊
     *[params setObject:msg.msgID forKey:@“id”];
     *[params setObject:chatType forKey:@“chatType”];
     *[params setObject:msg.fromID forKey:@“from”];
     *[params setObject:msg.toID forKey:@“to”];
     *
     * 群聊
     *[params setObject:msg.msgID forKey:@“id”];
     *[params setObject:chatType forKey:@“chatType”];
     *[params setObject:msg.groupId forKey:@“groupId”];
     *
     * private String deleteMessageType;
     * 消息删除方式:双向删除 Bilateral, 单向删除 Unilateral
     *
     */
    suspend fun deleteMessage(
        id: String,
        chatType: String,
        groupId: String,
        from: String,
        to: String,
        deleteMessageType: String
    ): SimpleBean {
        return RxHttp.postJson(ApiUrl.Chat.deleteMessage).apply {
            if (chatType == CHAT_TYPE_FRIEND) {
                //单聊
                add("id", id)
                add("chatType", chatType)
                add("from", from)
                add("to", to)
            } else {
                //群聊
                add("id", id)
                add("chatType", chatType)
                add("groupId", groupId)
                add("from", from)
            }
            add("deleteMessageType", deleteMessageType)
        }.setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    /**
     * 发送消息
     */
    suspend fun sendMsg(message: String, groupId: String = ""): String {
        return RxHttp.postJson(ApiUrl.Chat.sendMsg).apply {
            if (!TextUtils.isEmpty(groupId)) {
                add("groupId", groupId)
            }
            add("content", message)
        }.setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 编辑消息
     */
    suspend fun modifyMsg(message: String): String {
        return RxHttp.putJson(ApiUrl.Chat.modifyMsg).apply {
            add("content", message)
        }.setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 获取实时消息
     */
    suspend fun getMsg(): String {
        return RxHttp.get(ApiUrl.Chat.realtimeMsgList)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 获取历史消息
     */
    suspend fun getHistoryMsg(lastMstId: String = ""): String {
        var lastId = lastMstId
        if (TextUtils.isEmpty(lastMstId)) {
            lastId = ChatDao.getChatMsgDb().getAllLastId()
        }
        return RxHttp.get(ApiUrl.Chat.historyMsgList)
            .addQuery("ackMessageId", lastId)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 获取系统通知
     */
    suspend fun getHisNoticeMsg(): String {
        return RxHttp.get(ApiUrl.Chat.hisNoticeMsg)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 分享联系人
     */
    suspend fun shareContact(
        receiverGroupId: String,
        receiverMemberId: String,
        shareMemberId: String
    ): BaseBean<String> {
        return RxHttp.postJson(ApiUrl.Chat.shareContact)
            .apply {
                if (!TextUtils.isEmpty(receiverGroupId)) {
                    add("receiverGroupId", receiverGroupId)
                } else if (!TextUtils.isEmpty(receiverMemberId)) {
                    add("receiverMemberId", receiverMemberId)
                }
            }
            .add("shareMemberId", shareMemberId)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

    /**
     * 获取历史消息
     */
    suspend fun getHisMsg(
        messageId: String = "",
        queryType: String,
        curPage: Int,
        friendId: String = "",
        groupId: String = "",
        pageSize: Int = GET_HISTORY_PAGESIZE
    ): String {
        return RxHttp.get(ApiUrl.Chat.getMsgFromService)
            .apply {
                if (!TextUtils.isEmpty(messageId)) {
                    addQuery("messageId", messageId)
                }
                addQuery("queryType", queryType)
                addQuery("curPage", curPage)
                addQuery("friendMemberId", friendId)
                addQuery("groupId", groupId)
                addQuery("pageSize", pageSize)
            }
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 根据id获取消息
     */
    suspend fun getMessageByIds(list: MutableList<String>): String {
        return RxHttp.postBody(ApiUrl.Chat.getMessageByIds).setBody(list)
            .setCacheMode(CacheMode.ONLY_NETWORK).toStr().await()
    }

    /**
     * 获取历史消息
     */
    suspend fun getAtMsgList(sessionId: String = ""): BaseBean<MutableList<AtMessageInfoBean>> {
        return RxHttp.get(ApiUrl.Chat.getAtMsgList)
            .apply {
                if (!TextUtils.isEmpty(sessionId)) {
                    addQuery("sessionId", sessionId)
                }
            }
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<MutableList<AtMessageInfoBean>>>()
            .await()
    }

    /**
     * 获取历史消息
     */
    suspend fun ackAtMessage(msgIds: MutableList<String>): String {
        return RxHttp.postBody(ApiUrl.Chat.ackAtMessage)
            .setBody(msgIds)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }


    /**
     * 已读消息回执
     */
    suspend fun messageAck(msgIds: MutableList<String>): String {
        return RxHttp.postBody(ApiUrl.Chat.messageAck)
            .setBody(msgIds)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 定向回复消息
     */
    suspend fun messageReply(strMsg: String): String {
        return RxHttp.putJson(ApiUrl.Chat.messageReply)
            .add("content", strMsg)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 新增会话
     */
    suspend fun sessionInfoAdd(params: MutableMap<String,String>): String {
        return RxHttp.postJson(ApiUrl.Chat.sessionInfoAdd)
            .addAll(params)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

    /**
     * 聊天界面消息
     * 添加置顶消息
     */
    suspend fun addTopInfo(msgId: String, groupId: String): BaseBean<TopInfo> {
        return RxHttp.postJson(ApiUrl.Chat.addTopInfo)
            .add("messageId", msgId)
            .add("groupId", groupId)
            .add("type", "Message")//会话 Session,消息 Message
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<TopInfo>>()
            .await()
    }

    /**
     * 会话消息
     * 添加置顶消息
     */
    suspend fun addTopInfoSession(
        friendMemberId: String? = null,
        groupId: String? = null
    ): BaseBean<TopInfo> {
        return RxHttp.postJson(ApiUrl.Chat.addTopInfo).apply {
            if (!friendMemberId.isNullOrEmpty()) {
                add("friendMemberId", friendMemberId)
            }
            if (!groupId.isNullOrEmpty()) {
                add("groupId", groupId)
            }
            add("type", "Session")//会话 Session,消息 Message
        }.setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<TopInfo>>()
            .await()
    }

    /**
     * 删除置顶消息
     */
    suspend fun delTopInfo(topIds: MutableList<String>): BaseBean<String> {
        return RxHttp.deleteBody(ApiUrl.Chat.delTopInfo)
            .setBody(topIds)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

    /**
     * 聊天界面消息
     * 获取置顶消息
     */
    suspend fun getTopInfo(groupId: String): BaseBean<MutableList<MsgTopBean>> {
        return RxHttp.get(ApiUrl.Chat.getTopInfo)
            .add("groupId", groupId)
            .add("type", "Message")//会话 Session,消息 Message
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<MutableList<MsgTopBean>>>()
            .await()
    }

    /**
     * 会话消息
     * 获取置顶消息
     */
    suspend fun getTopInfo(): BaseBean<MutableList<MsgTopBean>> {
        return RxHttp.get(ApiUrl.Chat.getTopInfo)
            .add("type", "Session")//会话 Session,消息 Message
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<MutableList<MsgTopBean>>>()
            .await()
    }

    /**
     * 获取会话列表数据
     */
    suspend fun getSessionList(): String {
        return RxHttp.get(ApiUrl.Chat.sessionList)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }

}