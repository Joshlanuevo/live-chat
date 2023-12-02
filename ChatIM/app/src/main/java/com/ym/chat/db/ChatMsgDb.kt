package com.ym.chat.db

import android.text.TextUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys.MSG_READ_EVENT
import com.ym.base.constant.EventKeys.SEND_STATE_UPDATE
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.ChatMessageBean_
import com.ym.chat.bean.LastMsgBean
import com.ym.chat.service.WebsocketWork
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.TimeUtils
import io.objectbox.exception.UniqueViolationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * 聊天记录
 */
class ChatMsgDb {

    /**
     * 存储消息数据
     */
    fun saveChatMsg(
        message: ChatMessageBean
    ): ChatMessageBean {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {

            val dbId = put(message)
            message.dbId = dbId

            closeThreadResources()

            message
        }
    }

    /**
     * 更新最后一条消息ID
     */
    fun updateLastMsgId(msgId: String) {
        ChatDao.mBoxStore.boxFor(LastMsgBean::class.java).run {
            val temp = query().build().findFirst()
            if (temp == null) {
                put(LastMsgBean(lastMsgId = msgId))
            } else {
                temp.lastMsgId = msgId
                put(temp)
            }
            closeThreadResources()
        }
    }

    /**
     * 获取最后一条消息
     */
    fun getAllLastId(): String {
        return ChatDao.mBoxStore.boxFor(LastMsgBean::class.java).run {
            query().build().findFirst()?.lastMsgId ?: "0"
        }
    }

    /**
     * 删除所有聊天消息
     */
    fun delAllMsg() {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            remove(all)
            closeThreadResources()
        }
    }

    /**
     * 通过messageId删除单条消息
     */
    fun delMessage(msgId: Long, delCallBack: (() -> Unit)? = null) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {

            val tempMsg = get(msgId)
            //查询是不是该会话最后一条消息，如果是，则清除会话列表显示
            if (tempMsg.chatType == ChatType.CHAT_TYPE_FRIEND) {
                //单聊
                if (tempMsg.dir == 1) {
                    getMsgListByTarget(tempMsg.to)?.let {
                        if (it.last().dbId == msgId) {
                            //如果销毁的是最后一条消息，清空会话列表最后一条消息数据
                            ChatDao.getConversationDb().updateMsgByTargtId(tempMsg.to,ChatUtils.getString(
                                R.string.yuanchengxiaoxiyibeixiaohui))
                        }
                    }
                } else {
                    getMsgListByTarget(tempMsg.from)?.let {
                        if (it.last().dbId == msgId) {
                            //如果销毁的是最后一条消息，清空会话列表最后一条消息数据
                            ChatDao.getConversationDb().updateMsgByTargtId(tempMsg.to, ChatUtils.getString(
                                R.string.yuanchengxiaoxiyibeixiaohui))
                        }
                    }
                }
            } else if (tempMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
                //群聊
                getMsgListByGroupId(tempMsg.groupId)?.let {
                    if (it.last().dbId == msgId) {
                        //如果销毁的是最后一条消息，清空会话列表最后一条消息数据
                        ChatDao.getConversationDb().updateMsgByTargtId(tempMsg.groupId, ChatUtils.getString(
                            R.string.yuanchengxiaoxiyibeixiaohui))
                    }
                }
            }

            remove(msgId)
            delCallBack?.invoke()
        }
    }

    /**
     * 根据服务端消息ID，删除指定消息
     */
    fun delMessageByServiceId(serviceId: String, delCallBack: (() -> Unit)? = null) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val list =
                query().filter { it.id == serviceId || it.editId == serviceId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]

                //查询是不是该会话最后一条消息，如果是，则清除会话列表显示
                if (tempMsg.chatType == ChatType.CHAT_TYPE_FRIEND) {
                    //单聊
                    if (tempMsg.dir == 1) {
                        getMsgListByTarget(tempMsg.to)?.let {
                            if (it.last().id == serviceId) {
                                //如果销毁的是最后一条消息，清空会话列表最后一条消息数据
                                ChatDao.getConversationDb()
                                    .updateMsgByTargtId(tempMsg.to, ChatUtils.getString(
                                        R.string.yuanchengxiaoxiyibeixiaohui))
                            }
                        }
                    } else {
                        getMsgListByTarget(tempMsg.from)?.let {
                            if (it.last().id == serviceId) {
                                //如果销毁的是最后一条消息，清空会话列表最后一条消息数据
                                ChatDao.getConversationDb()
                                    .updateMsgByTargtId(tempMsg.from, ChatUtils.getString(
                                        R.string.yuanchengxiaoxiyibeixiaohui))
                            }
                        }
                    }
                } else if (tempMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
                    //群聊
                    getMsgListByGroupId(tempMsg.groupId)?.let {
                        if (it.last().id == serviceId) {
                            //如果销毁的是最后一条消息，清空会话列表最后一条消息数据
                            ChatDao.getConversationDb()
                                .updateMsgByTargtId(tempMsg.groupId, ChatUtils.getString(
                                    R.string.yuanchengxiaoxiyibeixiaohui))
                        }
                    }
                }

                remove(tempMsg)
                delCallBack?.invoke()
            }
        }
    }

    /**
     * 获取对方id查询聊天消息
     * targetId：对方id
     * To=对方，我给对方发的消息
     * From=对方，对方给我发的消息
     */
    fun getMsgListByTarget(targetId: String): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {

            //查询条件
            val queryCondition = ChatMessageBean_.chatType.equal(ChatType.CHAT_TYPE_FRIEND)
                .and(ChatMessageBean_.to.equal(targetId).or(ChatMessageBean_.from.equal(targetId)))
            val msgList =
                query(queryCondition).orderDesc(ChatMessageBean_.createTime).build().find()

            closeThreadResources()
            Collections.reverse(msgList)
            msgList

//            val msgList = query().filter {
//                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId)
//            }.build().find()
//            closeThreadResources()
//            msgList
        }
    }

    //每页数量
    companion object {
        val PAGE_SIZE = 1000L
    }

    /**
     * 分页获取对方id查询聊天消息
     * targetId：对方id
     * To=对方，我给对方发的消息
     * From=对方，对方给我发的消息
     */
    fun getMsgListByTargetPage(
        targetId: String,
        page: Long,
        pageSize: Long = PAGE_SIZE
    ): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询条件
            val queryCondition = ChatMessageBean_.chatType.equal(ChatType.CHAT_TYPE_FRIEND)
                .and(ChatMessageBean_.to.equal(targetId).or(ChatMessageBean_.from.equal(targetId)))
            val msgList =
                query(queryCondition).orderDesc(ChatMessageBean_.createTime).build()
                    .find(page * pageSize, pageSize)

            closeThreadResources()
            Collections.reverse(msgList)
            msgList
        }
    }

    /**
     * 检索指定消息id所在页码
     */
    fun getMsgCurrentPage(
        dbId: Long,
        callBack: (totalPage: Int, currentPage: Int, msgData: MutableList<ChatMessageBean>) -> Unit
    ) {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msg = get(dbId)
            if (msg != null) {
                val chatType = msg.chatType
                val targetId = getTargetId(msg)
                if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                    val queryCondition = ChatMessageBean_.chatType.equal(chatType)
                        .and(
                            ChatMessageBean_.to.equal(targetId)
                                .or(ChatMessageBean_.from.equal(targetId))
                        )

                    //总的id数量
                    val ids =
                        query(queryCondition).order(ChatMessageBean_.createTime).build().findIds()

                    //消息所在页数
                    val currentPage = getMsgIndex(dbId, ids)
                    if (currentPage >= 0) {
                        val totalBig =
                            BigDecimal("${ids.size}").divide(
                                BigDecimal("$PAGE_SIZE"),
                                BigDecimal.ROUND_UP
                            )
                        callBack.invoke(
                            totalBig.toInt(),
                            currentPage,
                            getMsgListByTargetPage(targetId, currentPage.toLong())
                        )
                    }
                } else if (chatType == ChatType.CHAT_TYPE_GROUP) {

                    val queryCondition = ChatMessageBean_.chatType.equal(chatType)
                        .and(ChatMessageBean_.groupId.equal(targetId))

                    //总的id数量
                    val ids =
                        query(queryCondition).order(ChatMessageBean_.createTime).build().findIds()

                    //消息所在页数
                    val currentPage = getMsgIndex(dbId, ids)
                    val totalBig =
                        BigDecimal("${ids.size}").divide(
                            BigDecimal("$PAGE_SIZE"),
                            BigDecimal.ROUND_UP
                        )
                    if (currentPage >= 0) {
                        val list = getMsgListByGroupIdPage(targetId, currentPage.toLong())
                        callBack.invoke(
                            totalBig.toInt(),
                            currentPage,
                            list
                        )
                    }
                }
            }
        }
    }

    /**
     * 获取id在指定页数
     */
    private fun getMsgIndex(dbId: Long, msgIds: LongArray): Int {
        val ids = msgIds.toMutableList()

        //总页数
        Collections.reverse(ids)

        val index = ids.indexOf(dbId)
        val totalBig =
            BigDecimal("${ids.size}").divide(BigDecimal("$PAGE_SIZE"), BigDecimal.ROUND_UP)
        return if (index >= 0) {
            val tempBig =
                BigDecimal("$index").divide(BigDecimal("${ids.size}"), 3, RoundingMode.HALF_UP)
            val res = tempBig.multiply(totalBig)
            val b = res.setScale(0, BigDecimal.ROUND_UP)
            val result = b.toInt() - 1
            if (result < 0) {
                0
            } else {
                result
            }
        } else {
            -1
        }
//
//
//        val totalSize = ids.size / PAGE_SIZE+0.5F
//        if (index >= 0) {
//            val tempBig = index / ids.size.toFloat()
//            val result = tempBig * totalSize
//            return (result + 0.5F).toInt()
//        } else {
//            return -1
//        }
    }

    /**
     * 获取targetId
     */
    private fun getTargetId(msg: ChatMessageBean): String {
        var targetId = ""
        if (msg.chatType == ChatType.CHAT_TYPE_FRIEND) {
            targetId = if (msg.dir == 0) {
                msg.from
            } else {
                msg.to
            }
        } else if (msg.chatType == ChatType.CHAT_TYPE_GROUP) {
            targetId = msg.groupId
        }
        return targetId
    }

    /**
     * 设置消息为已读,收到的消息
     */
    fun setMsgRead(targetId: String, msgReadState: Int = 1) {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId) && it.dir == 0
            }.build().find()
            msgList.forEach {
                //语音消息点击以后才变成已读
                if (it.msgReadState == 0) {
                    it.msgReadState = 1
                    WebsocketWork.WS.sendReadState(it.id, targetId, it.chatType, it.from)
                }
                put(it)
            }
            closeThreadResources()
        }
    }

    /**
     * 获取未读消息数量，收到的消息
     */
    fun getUnReadCount(targetId: String): Int {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId) && it.msgReadState == 0 && it.dir == 0
            }.build().find()
            closeThreadResources()
            msgList.size
        }
    }

    /**
     * 获取最后一条 已读消息 消息的id
     */
    fun getLastReadMsgId(groupId: String, targetId: String): String {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            if (groupId == ChatType.CHAT_TYPE_GROUP) {
                val msgList = query().filter {
                    it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == targetId && it.msgReadState == 1 && it.dir == 0
                }.build().find()
                closeThreadResources()
                msgList.last()?.id ?: ""
            } else {
                val msgList = query().filter {
                    it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId) && it.msgReadState == 1 && it.dir == 0
                }.build().find()
                closeThreadResources()
                msgList.last()?.id ?: ""
            }
        }
        return ""
    }

    /**
     * 获取未读消息数量
     */
    fun getUnReadCountByGroup(groupId: String): Int {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == groupId && it.msgReadState == 0
            }.build().find()
            closeThreadResources()
            msgList.size
        }
    }

    /**
     * 设置消息已读
     */
    fun setMsgRead(type: String, chatId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            if (type == ChatType.CHAT_TYPE_FRIEND) {
                //设置消息为已读
                ChatDao.getChatMsgDb().setMsgRead(chatId)
            } else if (type == ChatType.CHAT_TYPE_GROUP) {
                ChatDao.getChatMsgDb().setMsgReadByGoupId(chatId)
            }
        }
    }

    /**
     * 获取对方id查询聊天消息
     * targetId：对方id
     * 获取图片消息
     */
    fun getMsgListByTargetToPicture(targetId: String): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId) && it.msgType == MsgType.MESSAGETYPE_PICTURE
            }.build().find()
            closeThreadResources()
            msgList
        }
    }

    /**
     * 获取对方id查询聊天消息
     * targetId：对方id
     */
    fun getMsgListByTarget(
        targetId: String,
        page: Long,
        limit: Long
    ): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询条件
            val queryCondition = ChatMessageBean_.chatType.equal(ChatType.CHAT_TYPE_FRIEND)
                .and(ChatMessageBean_.to.equal(targetId).or(ChatMessageBean_.from.equal(targetId)))
            val msgList =
                query(queryCondition).orderDesc(ChatMessageBean_.id).build()
                    .find(page * limit, limit)

//            val msgList = query().filter {
//                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId)
//            }

            closeThreadResources()
            Collections.reverse(msgList)
            msgList
        }
    }

    /**
     * 根据对方id 查询聊天消息
     * targetId：对方id
     * searchContent 搜索的内容
     *
     * * 并查询离当前时间30天以内的数据
     */
    fun getMsgListByTarget(targetId: String, searchContent: String): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            var timeTo30 = TimeUtils.getTimeTo30Day()
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId)
                        && it.content.contains(searchContent) && (it.createTime > timeTo30)
                        && it.msgType == MsgType.MESSAGETYPE_TEXT || it.msgType == MsgType.MESSAGETYPE_AT
            }.build().find()
            closeThreadResources()
            msgList
        }
    }

    /**
     * 获取群组id查询聊天消息
     * groupId：群id
     */
    fun getMsgListByGroupId(groupId: String): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
//            val msgList = query().filter {
//                it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == groupId
//            }.build().find()
//            closeThreadResources()
//            msgList

            //查询条件
            val queryCondition = ChatMessageBean_.chatType.equal(ChatType.CHAT_TYPE_GROUP)
                .and(ChatMessageBean_.groupId.equal(groupId))

            val msgList =
                query(queryCondition).orderDesc(ChatMessageBean_.createTime).build().find()

            closeThreadResources()
            Collections.reverse(msgList)
            msgList
        }
    }

    /**
     * 分页获取群组id查询聊天消息
     * groupId：群id
     */
    fun getMsgListByGroupIdPage(
        groupId: String, page: Long, pageSize: Long = PAGE_SIZE
    ): MutableList<ChatMessageBean> {

        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询条件
            val queryCondition = ChatMessageBean_.chatType.equal(ChatType.CHAT_TYPE_GROUP)
                .and(ChatMessageBean_.groupId.equal(groupId))

            val msgList =
                query(queryCondition).orderDesc(ChatMessageBean_.createTime).build()
                    .find(page * pageSize, pageSize)

            closeThreadResources()
            Collections.reverse(msgList)
            msgList
        }
    }

    /**
     * 获取群组id查询聊天消息
     * groupId：群id
     */
    fun setMsgReadByGoupId(groupId: String) {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == groupId
            }.build().find()
            msgList.forEach {
                //语音消息点击以后才变成已读
                if (it.msgReadState == 0) {
                    it.msgReadState = 1
                    try {
                        WebsocketWork.WS.sendReadState(it.id, groupId, it.chatType, it.from)
                    } catch (e: Exception) {
                    }
                }
                put(it)
            }
            closeThreadResources()
        }
    }

    /**
     * 获取群组id查询聊天消息
     * groupId：群id
     * 获取所有图片
     */
    fun getMsgListByGroupIdToPicture(groupId: String): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == groupId && it.msgType == MsgType.MESSAGETYPE_PICTURE
            }.build().find()
            closeThreadResources()
            msgList
        }
    }

    /**
     * 根据群组id查询聊天消息
     * groupId：群id
     * searchContent 搜索的内容
     *
     * 并查询离当前时间30天以内的数据
     */
    fun getMsgListByGroupId(groupId: String, searchContent: String): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            var timeTo30 = TimeUtils.getTimeTo30Day()
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == groupId
                        && it.content.contains(searchContent)
                        && it.createTime > timeTo30
                        && (it.msgType == MsgType.MESSAGETYPE_TEXT || it.msgType == MsgType.MESSAGETYPE_AT)
            }.build().find()
            closeThreadResources()
            msgList
        }
    }

    /**
     * 查询所有 聊天消息
     * searchContent 搜索的内容
     * 并查询离当前时间30天以内的数据
     */
    fun getAllMsgListBySearchContent(searchContent: String): MutableList<ChatMessageBean> {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            var timeTo30 = TimeUtils.getTimeTo30Day()
            val msgList = query().filter {
                it.content.contains(searchContent)
                        && it.createTime > timeTo30
                        && (it.msgType == MsgType.MESSAGETYPE_TEXT|| it.msgType == MsgType.MESSAGETYPE_AT)
            }.build().find()
            closeThreadResources()
            msgList
        }
    }

    /**
     * 根据群组id 清空群消息
     * groupId：群id
     */
    @Synchronized
    fun delMsgListByGroupId(groupId: String) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == groupId
            }.build().find()
            remove(msgList)
            closeThreadResources()
        }
    }

    /**
     * 根据好友id 清空好友消息
     * friendId：好友id
     */
    @Synchronized
    fun delMsgListByFriendId(friendId: String) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            var meId = MMKVUtils.getUser()?.id ?: ""
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == friendId && it.from == meId || it.to == meId && it.from == friendId)
            }.build().find()
            remove(msgList)
            closeThreadResources()
        }
    }

    /**
     * 更新媒体消息文件上传状态
     */
    fun updateMsgFileUpload(
        toId: String,
        formId: String,
        createTime: Long,
        isUpload: Boolean,
        content: String = ""
    ) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            var tempMsgList = query().filter {
                it.to == toId && it.from == formId && it.createTime == createTime
            }.build().find()

            if (tempMsgList.size > 0) {
                tempMsgList[0].let {
                    //更改消息状态
                    it.isUpload = isUpload
                    if (!TextUtils.isEmpty(content)) {
                        it.content = content
                    }
                    put(it)
                }
            }
            closeThreadResources()
        }
    }

    /**
     * 消息是否已经存在
     */
    fun queryMsgExist(msgTime: Long, fromId: String, toId: String, isGroup: Boolean): Boolean {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            var tempMsgList = query().filter {
                if (isGroup) {
                    it.groupId == toId
                } else {
                    it.to == toId
                } && it.from == fromId && it.createTime == msgTime
            }.build().find()

            closeThreadResources()
            return tempMsgList.size > 0
        }
    }

    /**
     * 根据消息ID，查询指定消息
     */
    fun getMsgByDbId(dbId: Long): ChatMessageBean? {
        return ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            var tempMsgList = get(dbId)
            closeThreadResources()
            tempMsgList
        }
    }

    /**
     * 根据消息ID，查询指定消息
     */
    fun getMsgById(msgId: String): ChatMessageBean? {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            var tempMsgList = query().filter {
                it.id == msgId
            }.build().find()

            closeThreadResources()
            return tempMsgList.firstOrNull()
        }
    }

    /**
     * 根据消息ID，查询指定时间内消息
     */
    fun queryMsgByIdBeforeDate(msgId: String): ChatMessageBean? {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            var tempMsgList = query().filter {
                it.id == msgId
            }.build().find()

            closeThreadResources()
            return tempMsgList.firstOrNull()
        }
    }

    /**
     * 查询消息最大id
     */
    fun queryMsgMaxId(): Long {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            val dbId = query().build().property(ChatMessageBean_.dbId).max()
            closeThreadResources()
            return dbId
        }
    }

    /**
     * 更新消息状态
     */
    fun updateMsgSendState(
        sendState: Int,
        dbId: Long,
        id: String
    ) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            val tempMsg = get(dbId)
            if (tempMsg != null) {
                tempMsg.sendState = sendState
                if (!TextUtils.isEmpty(id)) {
                    tempMsg.id = id
                    tempMsg.isUpload
                }
                try {
                    put(tempMsg)
                } catch (e: UniqueViolationException) {
                    e.printStackTrace()
                }
                //发送事件，更新页面状态显示
                LiveEventBus.get(SEND_STATE_UPDATE).post(tempMsg)
            }

            closeThreadResources()
        }
    }

    /**
     * 根据消息id更新消息状态
     */
    fun updateMsgSend(
        sendState: Int,
        chatMsg: ChatMessageBean
    ) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            chatMsg.sendState = sendState
            try {
                put(chatMsg)
            } catch (e: UniqueViolationException) {
                e.printStackTrace()
            }
            //发送事件，更新页面状态显示
            LiveEventBus.get(SEND_STATE_UPDATE).post(chatMsg)
            closeThreadResources()
        }
    }

    /**
     * 更新消息状态
     */
    fun updateMsgSendState(
        sendState: Int,
        toId: String,
        formId: String,
        createTime: Long,
        id: String = ""
    ) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            var tempMsgList = query().filter {
                it.to == toId && it.from == formId && it.createTime == createTime
            }.build().find()

            if (tempMsgList.size > 0) {
                tempMsgList[0].let {
                    //更改消息状态
                    it.sendState = sendState
                    if (!TextUtils.isEmpty(id)) {
                        it.id = id
                    }
                    it.isUpload
                    put(it)

                    //发送事件，更新页面状态显示
                    LiveEventBus.get(SEND_STATE_UPDATE).post(it)
                }
            }
            closeThreadResources()
        }
    }

    /**
     * 更新数据库中，消息已读状态
     * 根据消息ID
     */
    fun updateMsgReadState(readState: Int, msgServiceId: String) {
        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            //查询出需要操作的消息
            var tempMsgList = query().filter {
                it.id == msgServiceId
            }.build().find()

            if (tempMsgList.size > 0) {
                tempMsgList[0].let {
                    //更改消息状态
                    it.msgReadState = readState
                    put(it)

                    if (it.dir == 1) {
                        //更新显示状态
                        LiveEventBus.get(MSG_READ_EVENT).post(it)
                    }
                }
            }
            closeThreadResources()
        }
    }

    /**
     * 更新数据库中，消息已读状态
     */
    fun updateMsgReadState(targetId: String) {

        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_FRIEND && (it.to == targetId || it.from == targetId) && it.msgReadState == 0
            }.build().find()

            msgList.forEach {
                it.msgReadState = 1
                put(it)
                if (it.dir == 1) {
                    //更新显示状态
                    LiveEventBus.get(MSG_READ_EVENT).post(it)
                }
                closeThreadResources()
            }
        }
    }

    /**
     * 更新数据库中，消息已读状态
     */
    fun updateMsgReadStateGroup(targetId: String) {

        ChatDao.mBoxStore.boxFor(ChatMessageBean::class.java).run {
            val msgList = query().filter {
                it.chatType == ChatType.CHAT_TYPE_GROUP && it.groupId == targetId && it.msgReadState == 0
            }.build().find()

            msgList.forEach {
                it.msgReadState = 1
                put(it)
                if (it.dir == 1) {
                    //更新显示状态
                    LiveEventBus.get(MSG_READ_EVENT).post(it)
                }
                closeThreadResources()
            }
        }
    }
}