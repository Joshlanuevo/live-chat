package com.ym.chat.viewmodel

import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.other.QueueUtil
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.service.WebsocketServiceManager
import com.ym.chat.service.WebsocketWork
import com.ym.chat.utils.*
import com.ym.chat.utils.StringExt.decodeContent
import com.ym.chat.utils.StringExt.isAtMsg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import rxhttp.RxHttp
import rxhttp.toFlow
import rxhttp.wrapper.cahce.CacheMode
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.utils.GsonUtil
import java.io.File
import java.util.*

class ChatViewModel : BaseViewModel() {

    //消息发送状态更新
    val sendMsgResult = MutableLiveData<ChatMessageBean>()

    //收藏
    val collectResult = MutableLiveData<LoadState<BaseBean<RecordBean>>>()

    //@消息列表
    val atMessageList = MutableLiveData<MutableList<AtMessageInfoBean>>()

    //聊天记录表
    private val msgDb = ChatDao.getChatMsgDb()

    //会话数据表
    private val conversationBd = ChatDao.getConversationDb()

    //历史消息
    val msgList = MutableLiveData<MutableList<ChatMessageBean>>()

    //上拉或者下拉加载更多数据
    val loadMoreMsgList = MutableLiveData<MutableList<ChatMessageBean>>()

    val replayMsgList = MutableLiveData<MutableList<ChatMessageBean>>()

    //跳转到指定位置消息
    val jumpIndexMsgList = MutableLiveData<MutableList<ChatMessageBean>>()

    //群成员数据
    val groupMemberList = MutableLiveData<MutableList<GroupMemberBean>>()

    //群发消息
    val groupSendMsgResult = MutableLiveData<LoadState<GroupSendBean>>()

    //获取群置顶消息
    val getTopMsgList = MutableLiveData<LoadState<BaseBean<MutableList<MsgTopBean>>>>()

    //删除置顶消息
    val delTopMsg = MutableLiveData<LoadState<BaseBean<String>>>()

    //增加置顶消息
    val addTopMsg = MutableLiveData<LoadState<BaseBean<TopInfo>>>()

    //关键字屏蔽
    val keyWordResult = MutableLiveData<KeyWordBean>()

    //未读消息开始处
    val firstUnReadMsg = MutableLiveData<Int>()

    //群组信息
    val getGroupInfo = MutableLiveData<LoadState<GroupInfoBean>>()


    var friendIds = "" //群发消息 群发成员id 字符串
    var friendNames = ""//群发消息 群发成员name 字符串

    /**
     * 获取关键词屏蔽
     */
    fun getKeyWord() {
        requestLifeLaunch({
            val result = GroupRepository.getKeyWord()
            keyWordResult.value = result?.data!!
        }, {
            it.printStackTrace()
        })
    }

    /**
     * 根据id获取消息数据
     */
    fun getMsgByIds(msgIds: MutableList<String>) {
        requestLifeLaunch({
            if (msgIds.isNotEmpty()) {
                //查询回复消息
                try {
                    val reResult = ChatRepository.getMessageByIds(msgIds)
                    val reJSONObject = JSONObject(reResult)
                    val resCode = reJSONObject.optInt("code")
                    if (resCode == 200) {
                        val resRecords = reJSONObject.optJSONArray("data")
                        val list = mutableListOf<ChatMessageBean>()
                        if (resRecords != null && resRecords.length() > 0) {
                            for (index in 0 until resRecords.length()) {
                                val repRecord = resRecords.optJSONObject(index)
                                val content = repRecord.optString("content")
                                var resDecodMsg = content.decodeContent()
                                val innerObj = JSONObject(resDecodMsg)
                                val resData = innerObj.optString("data");
                                val chatMsg =
                                    GsonUtils.fromJson(resData, ChatMessageBean::class.java)
                                list.add(chatMsg)
                                replayMsgList.value = list
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    replayMsgList.value = mutableListOf()
                }
            }
        }, {
            it.printStackTrace()
            replayMsgList.value = mutableListOf()
        })
    }

    //查询群组信息
    fun getGroupInfoByGroupId(groupId: String) {
        requestLifeLaunch({
            val result = GroupRepository.getGroupInfoByGroupId(groupId)
            getGroupInfo.value = LoadState.Success(result.data)
        }, {
            getGroupInfo.value = LoadState.Fail()
            it.printStackTrace()
        })
    }

    /**se
     * 加载更多数据
     */
    fun getLoadMoreMsg(
        chatType: String,
        messageId: String = "",
        queryType: String,
        targetId: String = ""
    ) {
        requestLifeLaunch({
            if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                val msgResult = ChatRepository.getHisMsg(messageId, queryType, 1, targetId, "")
                val jsonObject = JSONObject(msgResult)
                val result = handleMsg(jsonObject, queryType)
                loadMoreMsgList.value = result
            } else if (chatType == ChatType.CHAT_TYPE_GROUP) {
                val msgResult = ChatRepository.getHisMsg(messageId, queryType, 1, "", targetId)
                val jsonObject = JSONObject(msgResult)
                val result = handleMsg(jsonObject, queryType)
                loadMoreMsgList.value = result
            }
        }, {
            it.printStackTrace()
            loadMoreMsgList.value = mutableListOf()
        })
    }

    /**se
     * 跳转到指定消息
     */
    fun jumpIndexMsg(
        chatType: String,
        messageId: String = "",
        targetId: String = ""
    ) {
        requestLifeLaunch({
            if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                val msgResult = ChatRepository.getHisMsg(messageId, "reply", 1, targetId, "")
                val jsonObject = JSONObject(msgResult)
                val result = handleMsg(jsonObject, "reply", hasSaveDb = false)
                jumpIndexMsgList.value = result
            } else if (chatType == ChatType.CHAT_TYPE_GROUP) {
                val msgResult = ChatRepository.getHisMsg(messageId, "reply", 1, "", targetId)
                val jsonObject = JSONObject(msgResult)
                val result = handleMsg(jsonObject, "reply", hasSaveDb = false)
                jumpIndexMsgList.value = result
            }
        }, {
            it.printStackTrace()
            loadMoreMsgList.value = mutableListOf()
        })
    }

    /**
     * 处理消息
     */
    suspend private fun handleMsg(
        jsonObject: JSONObject,
        queryType: String,
        hasSaveDb: Boolean = true,
        localMsg: MutableList<ChatMessageBean> = mutableListOf()
    ): MutableList<ChatMessageBean> {
        val code = jsonObject.optInt("code")
        if (code == 200) {
            val dataObject = jsonObject.optJSONObject("data")
            val records = dataObject.optJSONArray("records")
            val list = mutableListOf<ChatMessageBean>()
            val msgIds = mutableListOf<String>()
            if (records != null && records.length() > 0) {
                for (index in 0 until records.length()) {
                    val record = records.optJSONObject(index)
                    val readFlag = record.optString("readFlag")//已读未读状态
                    val content = record.optString("content")
                    val groupId = record.optString("groupId")
                    var decodMsg = content.decodeContent()
                    var jsonObject: JSONObject? = null
                    try {
                        jsonObject = JSONObject(decodMsg)
                        val command = jsonObject.optInt("command")
                        when (command) {
                            CommandType.CHAT, CommandType.CHAT_REPLY -> {
                                //聊天消息
                                val data = jsonObject.optString("data")
                                val chatMsg = GsonUtils.fromJson(data, ChatMessageBean::class.java)
                                if (chatMsg.from == MMKVUtils.getUser()?.id) {
                                    //自己发的消息
                                    chatMsg.dir = 1
                                    chatMsg.sendState = 1
                                }
                                chatMsg.msgReadState = if (readFlag.lowercase() == "read") 1 else 0

                                if (!TextUtils.isEmpty(chatMsg.parentMessageId)) {
                                    msgIds.add(chatMsg.parentMessageId)
                                }
                                //文字消息，过滤掉敏感词
                                if (chatMsg.msgType == MsgType.MESSAGETYPE_TEXT || chatMsg.msgType == MsgType.MESSAGETYPE_AT) {
                                    if (!MMKVUtils.isAdmin() && ChatUtils.msgContentHasKeyWork(
                                            chatMsg.content
                                        )
                                    ) {
                                        "过滤敏感词消息:${chatMsg}".logE()
                                    } else {
                                        list.add(chatMsg)
                                    }
                                } else {
                                    list.add(chatMsg)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        "消息格式错误${decodMsg}".logE()
                        e.printStackTrace()
                    }
                }

                list.addAll(localMsg)
                list.sortBy { it.createTime }

                if (msgIds.isNotEmpty()) {
                    //查询回复消息
                    try {
                        val reResult = ChatRepository.getMessageByIds(msgIds)
                        val reJSONObject = JSONObject(reResult)
                        val resCode = reJSONObject.optInt("code")
                        if (resCode == 200) {
                            val resRecords = reJSONObject.optJSONArray("data")
                            if (resRecords != null && resRecords.length() > 0) {
                                for (index in 0 until resRecords.length()) {
                                    val repRecord = resRecords.optJSONObject(index)
                                    val content = repRecord.optString("content")
                                    var resDecodMsg = content.decodeContent()
                                    val innerObj = JSONObject(resDecodMsg)
                                    val resData = innerObj.optString("data");
                                    val chatMsg =
                                        GsonUtils.fromJson(resData, ChatMessageBean::class.java)
                                    list.forEach {
                                        if (it.parentMessageId == chatMsg.id) {
                                            it.replayParentMsg = chatMsg
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return list
        } else {
            return mutableListOf()
        }
    }

    /**
     * 获取历史消息数据
     */
    fun getMsgList(targetId: String, chatType: Int, page: Int = 0) {
        Log.d("ChatView", "page=$page")
        requestLifeLaunch({
            when (chatType) {
                0 -> {
                    /**
                     * 单聊
                     */
                    val msgResult = ChatRepository.getHisMsg("", "Down", 1, targetId, "")
                    val jsonObject = JSONObject(msgResult)
                    val list = handleMsg(
                        jsonObject,
                        "Down",
//                        localMsg = ChatDao.getChatMsgDb().getMsgListByTargetFail(targetId)
                    )

                    msgList.value = generateDateHeaders(list)
                }
                1 -> {
                    /**
                     * 群聊
                     */
                    //查询服务端数据
                    val msgResult = ChatRepository.getHisMsg("", "Down", 1, "", targetId)
                    val jsonObject = JSONObject(msgResult)
                    val list = handleMsg(
                        jsonObject,
                        "Down",
//                        localMsg = ChatDao.getChatMsgDb().getMsgListByGroupIdFail(targetId)
                    )
                    msgList.value = generateDateHeaders(list)
                }
                else -> {
                    //群发消息，不做处理
                }
            }
        }, {
            it.printStackTrace()
            //获取失败，加载本地数据
            if (chatType == 0) {
                //单聊
                msgList.value = generateDateHeaders(
                    ChatDao.getChatMsgDb().getMsgListByTargetPage(targetId, page.toLong())
                )
            } else if (chatType == 1) {
                //群聊
                msgList.value = generateDateHeaders(
                    ChatDao.getChatMsgDb().getMsgListByGroupIdPage(targetId, page.toLong())
                )
            } else {
                msgList.value = mutableListOf()
            }
        })
    }

    /**
     * 获取@消息
     */
    fun getAtMsg(chatId: String) {
        requestLifeLaunch({
            val tempResult = ChatRepository.getAtMsgList(chatId)
            if (tempResult.code == SUCCESS) {
                atMessageList.value = tempResult.data ?: mutableListOf()
            } else {
                atMessageList.value = mutableListOf()
            }
        }, onError = { e ->
            e.printStackTrace()
        })
    }

    /**
     * 上报已读@消息
     */
    fun upReadAtMsg(msgIds: MutableList<String>) {
        requestLifeLaunch({
            val tempResult = ChatRepository.ackAtMessage(msgIds)
        }, { e ->
            e.printStackTrace()
        })
    }

    /**
     * 更新会话信息
     */
    fun updateConver(targetId: String, chatType: String) {
        val delConBean = DelConBean().apply {
            cmd = 49
            memberId = MMKVUtils.getUser()?.id ?: ""
            type = chatType
            if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                friendMemberId = targetId
            } else if (chatType == ChatType.CHAT_TYPE_GROUP) {
                groupId = targetId
            }
            operationType = "Add"
        }
        WebsocketWork.WS.updateConver(delConBean)
    }


    var firstUnReadPosition = -1

    /**
     * 生成时间提示
     */
    fun generateDateHeaders(
        msgList: MutableList<ChatMessageBean>,
        isMaskUnRead: Boolean = true
    ): MutableList<ChatMessageBean> {
        val tempMsgList = mutableListOf<ChatMessageBean>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            msgList.removeIf { it.content == "时间提示" }
        }

        for (i in 0 until msgList.size) {
            val msg = msgList[i]

//            if (isMaskUnRead) {
//                //记录未读消息位置
//                maskUnRead(msg, i, tempMsgList)
//            }

            //添加回原来的消息
            tempMsgList.add(processSendTimeOut(msg))

            if (i == 0) {
                val bean = ChatMessageBean().apply {
                    to = msg.to
                    from = msg.from
                    sendState = 1
                    msgReadState = 1
                    chatType = msg.chatType
                    createTime = msg.createTime
                    msgType = MsgType.MESSAGETYPE_TIME
                    content = "时间提示信息"
                }
                tempMsgList.add(0, bean)
            } else if (msgList.size > i + 1) {
                val nextMsg = msgList.get(i + 1)

                createDateMsg(nextMsg, msg)?.let {
                    tempMsgList.add(it)
                }
            }

            //处理多条转发消息，拆分显示
            if (msg.operationType == "Forward") {
                //移除原有消息体
                tempMsgList.remove(msg)
                val str = msg.content
                val array = JSONArray(str)
                if (array != null && array.length() > 0) {
                    for (index in 0 until array.length()) {
                        val str = array.getString(index)
                        val msgObj = JSONObject(str)
                        val type = msgObj.optString("msgType")
                        val tempMsg = msg.copy()
                        tempMsg.msgType = type
                        tempMsg.content = str
                        if (msg.from == MMKVUtils.getUser()?.id) {
                            tempMsg.dir = 1
                        }
                        tempMsgList.add(tempMsg)
                    }
                }
            }
        }
        return tempMsgList
    }

    /**
     * 标记未读
     */
    private fun maskUnRead(
        msg: ChatMessageBean,
        i: Int,
        tempMsgList: MutableList<ChatMessageBean>
    ) {
        if (msg.msgReadState == 0 && msg.dir == 0) {
            if (msg.msgType != MsgType.MESSAGETYPE_TIME && firstUnReadPosition < 0) {
                firstUnReadPosition = i
                firstUnReadMsg.value = i
                val bean = ChatMessageBean().apply {
                    to = msg.to
                    from = msg.from
                    sendState = 1
                    chatType = msg.chatType
                    msgReadState = 1
                    createTime = msg.createTime
                    msgType = MsgType.MESSAGETYPE_UNREAD
                    content = "未读消息"
                }
                tempMsgList.add(bean)
            }
        }
    }

    /**
     * 处理发送超时接口
     */
    private fun processSendTimeOut(msg: ChatMessageBean): ChatMessageBean {
        if (msg.dir == 1) {
            val diffTime = System.currentTimeMillis() - msg.createTime
            when (msg.msgType) {
                MsgType.MESSAGETYPE_TEXT -> {
                    //文本消息，15s发送中认为超时
                    if (msg.sendState == 0 && diffTime >= 15 * 1000) {
                        msg.sendState = 2
//                        ChatDao.getChatMsgDb().updateMsgSendState(2, msg.dbId, "")
                    }
                }
                else -> {
                    if (msg.sendState == 0 && diffTime >= 2 * 60 * 1000) {
                        msg.sendState = 2
//                        ChatDao.getChatMsgDb().updateMsgSendState(2, msg.dbId, "")
                    }
                }
            }
        }
        return msg
    }

    /**
     * 生成时间提示语
     */
    fun createDateMsg(newMsg: ChatMessageBean, lastMsg: ChatMessageBean?): ChatMessageBean? {
        if (lastMsg == null) {
            return ChatMessageBean().apply {
                to = newMsg.to
                from = newMsg.from
                sendState = 1
                msgReadState = 1
                chatType = newMsg.chatType
                createTime = newMsg.createTime
                msgType = MsgType.MESSAGETYPE_TIME
                content = "时间提示信息"
            }
        } else {
            //方案1：同一天一起显示
            if (!DateFormatter.isSameDay(Date(lastMsg.createTime), Date(newMsg.createTime))) {
                return ChatMessageBean().apply {
                    to = newMsg.to
                    from = newMsg.from
                    sendState = 1
                    chatType = newMsg.chatType
                    msgReadState = 1
                    createTime = newMsg.createTime
                    msgType = MsgType.MESSAGETYPE_TIME
                    content = "时间提示信息"
                }
            } else {
                return null
            }

            //方案2：间隔固定时间显示
//            val diffTime = newMsg.createTime - lastMsg?.createTime
//            if (diffTime >= 30 * 60 * 1000) {
//                return ChatMessageBean().apply {
//                    to = newMsg.to
//                    from = newMsg.from
//                    sendState = 1
//                    chatType = newMsg.chatType
//                    createTime = newMsg.createTime
//                    msgType = MsgType.MESSAGETYPE_TIME
//                    content = "时间提示信息"
//                }
//            } else {
//                return null
//            }
        }
    }

    /**
     * 消息重发
     */
    fun reSendMsg(msgBean: ChatMessageBean) {
        if (msgBean.msgType == MsgType.MESSAGETYPE_TEXT || msgBean.msgType == MsgType.MESSAGETYPE_AT) {
            //文本消息
            val to = if (msgBean.chatType == ChatType.CHAT_TYPE_FRIEND) {
                msgBean.to
            } else {
                msgBean.groupId
            }
            val params = createSendParams(msgBean.content, to, msgBean.chatType).apply {
                msgType = if (msgBean.content.isAtMsg()) {
                    MsgType.MESSAGETYPE_AT
                } else {
                    MsgType.MESSAGETYPE_TEXT
                }
            }
            //发送消息
            WebsocketWork.WS.sendMsg(
                GsonUtil.toJson(params), success = { msgId ->
                    ChatDao.getChatMsgDb().updateMsgSendState(1, msgBean.dbId, msgId)
                }, faile = {
                    ChatDao.getChatMsgDb().updateMsgSendState(2, msgBean.dbId, "")
                }
            )
        } else {
            //重发媒体消息
            WebsocketServiceManager.reSendMediaMsg(msgBean.dbId)
        }
    }

    fun sendImageUrlMsg(
        url: String,
        toId: String,
        type: String,
        with: Int = 400,
        height: Int = 400,
        parentMsgIdStr: String = ""
    ) {
        //生成发送消息的Bean对象
        val params = createSendParams("", toId, type).apply {
            msgType = MsgType.MESSAGETYPE_PICTURE
            parentMessageId = parentMsgIdStr
        }

        //拼装图片消息
        val imgContent = GsonUtil.toJson(ImageBean(with, height).apply {
            this.url = url
        })

        //消息保存到数据库，并且回调给聊天页面显示
        val chatMsg = copyToChatMsgBean(params).apply {
            //当前为发送中状态
            sendState = 0
            parentMessageId = parentMsgIdStr
        }

        params.content = imgContent
        chatMsg.content = imgContent

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

        val dbBean = msgDb.saveChatMsg(chatMsg)
        sendMsgResult.value = dbBean

        //2、发送消息
        WebsocketWork.WS.sendMsg(
            GsonUtil.toJson(params), success = { msgId ->
                ChatDao.getChatMsgDb().updateMsgSendState(1, dbBean.dbId, msgId)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSendState(2, dbBean.dbId, "")
            }, parentMsgIdStr
        )

    }

    /**
     * 发送问候语
     */
    fun sendHello(str: String = "我们已经是好友了，赶快聊天吧!", toId: String) {
        //生成发送消息的Bean对象
        val params = createSendParams(str, toId, ChatType.CHAT_TYPE_FRIEND).apply {
            msgType = MsgType.MESSAGETYPE_TEXT
        }

        //发送消息
        WebsocketWork.WS.sendMsg(GsonUtil.toJson(params))
    }

    /**
     * 发送问候语
     */
    fun addSessionInfo(targetId: String) {
        val params = mutableMapOf<String, String>()
        params.put("type", "Friend")
        params.put("friendMemberId", targetId)
        WebsocketWork.WS.addSessionInfo(params)
    }

    /**
     * 发送消息
     * content:消息内容
     * toFromId：对方id，如果chatTyoe为群，则为群组ID
     * chatType：单聊(ChatType.CHAT_TYPE_FRIEND)或者群聊(ChatType.CHAT_TYPE_GROUP)
     * parentMsgId:回复消息用到，被回复的消息id
     */
    fun sendMsg(str: String, toId: String, type: String, parentMsgId: String = "") {

        var contentStr = str
        //生成发送消息的Bean对象
        val params = createSendParams(contentStr, toId, type).apply {
            msgType = if (contentStr.isAtMsg()) {
                MsgType.MESSAGETYPE_AT
            } else {
                MsgType.MESSAGETYPE_TEXT
            }
            parentMessageId = parentMsgId
        }

        //消息保存到数据库，并且回调给聊天页面显示
        val chatMsg = copyToChatMsgBean(params).apply {
            //当前为发送中状态
            sendState = 0
            if (!TextUtils.isEmpty(parentMsgId)) {
                parentMessageId = parentMsgId
                operationType = MsgType.MESSAGETYPE_REPLY
            }
        }

        when (type) {
            ChatType.CHAT_TYPE_GROUP_SEND -> {
                /**如果是群发消息
                 * 调用群发接口，发送消息
                 * 并发送成功以后保存到本地操作
                 */
                groupSendMsg(chatMsg.content, friendIds, friendNames, chatMsg)
            }
            else -> {
                /**如果是正常 发好友 群组消息**/
                val dbBean = msgDb.saveChatMsg(chatMsg)
                sendMsgResult.value = dbBean

                //生成会话列表
                if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
                    conversationBd.saveGroupConversation(
                        chatMsg.groupId,
                        chatMsg.content,
                        chatMsg.msgType,
                        fromId = MMKVUtils.getUser()?.id ?: ""
                    )
                } else {
                    conversationBd.saveFriendConversation(
                        chatMsg.to,
                        chatMsg.content,
                        chatMsg.msgType
                    )
                }

                //发送消息
                WebsocketWork.WS.sendMsg(
                    GsonUtil.toJson(params), success = { msgId ->
                        ChatDao.getChatMsgDb().updateMsgSendState(1, dbBean.dbId, msgId)
                    }, faile = {
                        ChatDao.getChatMsgDb().updateMsgSendState(2, dbBean.dbId, "")
                    }, parentMsgId
                )
            }
        }
    }

    /**
     * 发送消息
     * content:消息内容
     * toFromId：对方id，如果chatTyoe为群，则为群组ID
     * chatType：单聊(ChatType.CHAT_TYPE_FRIEND)或者群聊(ChatType.CHAT_TYPE_GROUP)
     */
    fun modifyMsg(chatMsg: ChatMessageBean) {
        //生成发送消息的Bean对象
        val params = createSendParams(chatMsg)
        ChatDao.getChatMsgDb().updateMsgSend(0, chatMsg)
        //发送消息
        WebsocketWork.WS.modifyMsg(
            GsonUtil.toJson(params), success = { msgId ->
                ChatDao.getChatMsgDb().updateMsgSend(1, chatMsg)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSend(2, chatMsg)
            })
    }

    /**
     * 发送文件
     */
    fun sendFileMsg(
        filepath: String,
        size: String,
        toId: String,
        type: String,
        parentMsgId: String = ""
    ) {
        if (type != ChatType.CHAT_TYPE_GROUP_SEND) {
            //普通发消息，通过work完成文件上传，消息发送
            WebsocketServiceManager.sendMediaMsg(
                filepath,
                toId,
                type,
                fileType = "File",
                parentMsgId = parentMsgId,
                fileSize = size
            )
        } else {
//            val params = createSendParams(imgPath, toId, type).apply {
//                msgType = MsgType.MESSAGETYPE_PICTURE
//            }
//
//            val chatMsg = copyToChatMsgBean(params).apply {
//                //保存本地路径
//                content = GsonUtil.toJson(ImageBean(w, h, imgPath))
//            }
//
//            //1、上传图片
//            uploadFile(imgPath, "Picture", progress = {}, success = { result ->
//
//                //保存远程文件地址
//                chatMsg.servicePath = result.data.filePath
//                chatMsg.isUpload = true
//
//                //拼装图片消息
//                val imgContent = GsonUtil.toJson(ImageBean(w, h, result.data.filePath))
//                params.content = imgContent
//                chatMsg.content = imgContent
//
//                groupSendMsg(chatMsg.content, friendIds, friendNames, chatMsg)
//            }, error = {
//                //文件上传失败
//            })
        }
    }

    /**
     * 发送图片消息
     */
    fun sendImageMsg(
        imgPath: String,
        toId: String,
        type: String,
        w: Int,
        h: Int,
        parentMsgId: String = ""
    ) {

        if (type != ChatType.CHAT_TYPE_GROUP_SEND) {
            //普通发消息，通过work完成文件上传，消息发送
            ChatUtils.sendTxtMsgTime = System.currentTimeMillis()
            WebsocketServiceManager.sendMediaMsg(imgPath, toId, type, "Picture", w, h, parentMsgId)
        } else {
            val params = createSendParams(imgPath, toId, type).apply {
                msgType = MsgType.MESSAGETYPE_PICTURE
            }

            val chatMsg = copyToChatMsgBean(params).apply {
                //保存本地路径
                content = GsonUtil.toJson(ImageBean(w, h).apply {
                    url = imgPath
                })
            }

            //1、上传图片
            uploadFile(imgPath, "Picture", progress = {}, success = { result ->

                //保存远程文件地址
                chatMsg.servicePath = result.data.filePath
                chatMsg.isUpload = true

                //拼装图片消息
                val imgContent = GsonUtil.toJson(ImageBean(w, h).apply {
                    url = result.data.filePath
                })
                params.content = imgContent
                chatMsg.content = imgContent

                groupSendMsg(chatMsg.content, friendIds, friendNames, chatMsg)
            }, error = {
                //文件上传失败
            })
        }
    }

    /**
     * 发送视频消息
     */
    fun sendVideoMsg(
        imgPath: String, toId: String,
        type: String, w: Int = 0, h: Int = 0, parentMsgId: String = ""
    ) {

        if (type != ChatType.CHAT_TYPE_GROUP_SEND) {
            //普通发消息，通过work完成文件上传，消息发送
            WebsocketServiceManager.sendMediaMsg(imgPath, toId, type, "Video", w, h, parentMsgId)
        } else {
            //群发消息
            val params = createSendParams(imgPath, toId, type).apply {
                msgType = MsgType.MESSAGETYPE_VIDEO
            }

            //本地存储消息的参数
            val chatMsg = copyToChatMsgBean(params).apply {
                localPath = imgPath
            }

            //1、上传视频
            uploadFile(imgPath, "Video", progress = {}, success = { result ->
                //chatMsg.content = result.data.filePath

                chatMsg.isUpload = true

                //拼装图片消息
                val audioContent =
                    GsonUtil.toJson(VideoMsgBean().apply {
                        this.url = result.data.filePath
                        this.coverUrl = result.data.thumbnail
                    })
                params.content = audioContent
                chatMsg.content = audioContent

                groupSendMsg(chatMsg.content, friendIds, friendNames, chatMsg)
            }, error = {
                chatMsg.sendState = 2
                sendMsgResult.value = chatMsg
            })
        }
    }

    /**
     * 发送音频文件消息
     */
    fun sendAudioMsg(imgPath: String, time: Int, toId: String, type: String, parentMsgId: String) {

        if (type != ChatType.CHAT_TYPE_GROUP_SEND) {
            //普通发消息，通过work完成文件上传，消息发送
            WebsocketServiceManager.sendMediaMsg(imgPath, toId, type, "Audio", time, 0, parentMsgId)
        } else {
            val params = createSendParams(imgPath, toId, type).apply {
                msgType = MsgType.MESSAGETYPE_VOICE
            }

            val chatMsg = copyToChatMsgBean(params)

            //1、上传语音文件
            uploadFile(imgPath, "Other", progress = {}, success = { result ->

                if (result.data != null) {
                    params.content = result.data.filePath
                }
                //chatMsg.content = result.data.filePath
                chatMsg.isUpload = true

                //拼装图片消息
                val audioContent = GsonUtil.toJson(AudioMsgBean(time, result.data.filePath))
                params.content = audioContent
                chatMsg.content = audioContent

                when (type) {
                    ChatType.CHAT_TYPE_GROUP_SEND -> {
                        /**如果是群发消息
                         * 调用群发接口，发送消息
                         * 并发送成功以后保存到本地操作
                         */
                        groupSendMsg(chatMsg.content, friendIds, friendNames, chatMsg)
                    }
                }
            }, error = {
                chatMsg.sendState = 2
                sendMsgResult.value = chatMsg
            })

        }
    }

    /**
     * 收藏
     */
    fun collect(content: String, type: String) {
        requestLifeLaunch({
            //收藏
            val result = ChatRepository.collect(content, type)
            collectResult.value = LoadState.Success(result)
        }, {
            it.printStackTrace()
            collectResult.value = LoadState.Fail()
        }, {
            collectResult.value = LoadState.Loading()
        })
    }

    /**
     * 上传文件
     * 图片：
     * 语音：
     * 视频：
     */
    fun uploadFile(
        path: String,
        fileType: String,
        progress: (Progress) -> Unit,
        success: (UploadResultBean) -> Unit,
        error: () -> Unit
    ) {

        val cacheUrl = ACache.get(Utils.getApp()).getAsString(MD5.MD516(path))
        if (!TextUtils.isEmpty(cacheUrl)) {
            //有缓存，直接取缓存使用
            val result = GsonUtil.fromJson<UploadResultBean>(cacheUrl, UploadResultBean::class.java)
            success?.invoke(result)
        } else {
            //上传图片,并发送
            requestLifeLaunch({
                RxHttp.postForm(ApiUrl.Chat.uploadFile)
                    .setCacheMode(CacheMode.ONLY_NETWORK)
                    .add("fileType", fileType)
                    .addFile("file", File(path))
                    .toFlow<UploadResultBean> {
                        //进度回调
                        progress?.invoke(it)
                        Log.d("上传文件", "上传进度${it.progress}")
                    }
                    .catch {
                        //异常回调
                        error?.invoke()
                        Log.d("上传文件", "上传失败")
                    }.collect { result ->
                        //成功回调
                        if (fileType == "Video") {
                            ACache.get(Utils.getApp()).put(MD5.MD516(path), GsonUtil.toJson(result))
                        }
                        success?.invoke(result)
                    }
            }, onError = {
                error?.invoke()
                it.printStackTrace()
            }, onStart = {
//            chatMsg.sendState = 0
//            sendMsgResult.value = chatMsg
            })
        }
    }

    /**
     * 创建发送消息的Bean
     */
    private fun createSendParams(contentStr: String, toId: String, type: String): SendMsgParams {
        return SendMsgParams().apply {
            cmd = 11
            content = contentStr
            from = MMKVUtils.getUser()?.id ?: ""
            uuid = UUID.randomUUID().toString()
            chatType = type
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
     * 创建发送消息的Bean
     */
    private fun createSendParams(chatMsg: ChatMessageBean): SendMsgParams {
        return SendMsgParams().apply {
            from = MMKVUtils.getUser()?.id ?: ""
            to = chatMsg.to
            msgType = chatMsg.msgType
            chatType = chatMsg.chatType
            content = chatMsg.content
            groupId = chatMsg.groupId
            uuid = chatMsg.uuid
            cmd = 11
            createTime = chatMsg.createTime
            id = chatMsg.id
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
            cmd = sendMsgParams.cmd
            uuid = sendMsgParams.uuid
            to = sendMsgParams.to
            createTime = sendMsgParams.createTime
            dir = 1
        }
    }

    /**
     * 获取群主或者管理员数据
     */
    fun getManagerList(groupId: String) {
        requestLifeLaunch({
            val result = GroupRepository.getGroupMemberList(groupId)
            val tempList = mutableListOf<GroupMemberBean>()
            if (result.code == SUCCESS) {
                //保存群成员数据
                ChatDao.getGroupDb().updateMemByGroupId(groupId, result.data)
                tempList.addAll(result.data)
            }
            groupMemberList.value = tempList
        }, {
            groupMemberList.value = ChatDao.getGroupDb().getMemberByGroupId(groupId)
        })
    }

    /**
     * 获取置顶消息
     */
    fun getTopInfoList(groupId: String) {
        requestLifeLaunch({
            val result = ChatRepository.getTopInfo(groupId)
            if (result.code == SUCCESS) {
                getTopMsgList.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    getTopMsgList.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    getTopMsgList.value = LoadState.Fail(exc = Exception("获取置顶消息失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            getTopMsgList.value = LoadState.Fail(exc = Exception("获取置顶消息失败"))
        }, onStart = {
            getTopMsgList.value = LoadState.Loading()
        })
    }

    /**
     * 删除置顶消息
     */
    fun delTopInfoList(topMsgIdList: MutableList<String>) {
        requestLifeLaunch({
            val result = ChatRepository.delTopInfo(topMsgIdList)
            if (result.code == SUCCESS) {
                delTopMsg.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    delTopMsg.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    delTopMsg.value = LoadState.Fail(exc = Exception("删除置顶消息失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            delTopMsg.value = LoadState.Fail(exc = Exception("删除置顶消息失败"))
        }, onStart = {
            delTopMsg.value = LoadState.Loading()
        })
    }

    /**
     * 增加置顶消息
     */
    fun addTopInfoList(msgId: String, groupId: String) {
        requestLifeLaunch({
            val result = ChatRepository.addTopInfo(msgId, groupId)
            if (result.code == SUCCESS) {
                addTopMsg.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    "$errorInfo".toast()
                    addTopMsg.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    addTopMsg.value = LoadState.Fail(exc = Exception("添加置顶消息失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            addTopMsg.value = LoadState.Fail(exc = Exception("添加置顶消息失败"))
        }, onStart = {
            addTopMsg.value = LoadState.Loading()
        })
    }

    /**
     * 发送群发消息
     */
    private fun groupSendMsg(
        content: String,
        groupIdList: String,
        groupNameList: String,
        chatMsg: ChatMessageBean
    ) {
        if (groupSendMsgResult.value is LoadState.Loading) return
        requestLifeLaunch({
            val result =
                GroupRepository.groupSendMsg(
                    content,
                    groupIdList,
                    groupNameList,
                    chatMsg.msgType
                )
            if (result.code == SUCCESS) {
                //群发消息成功
                groupSendMsgResult.value = LoadState.Success(result.data)
                //根据ID，保存数据到本地
//                if (friendIds.isNotBlank()) {
//                    chatMsg.id = result.data.id
//                    if (friendIds.contains(";")) {
//                        //群发多个好友的处理
//                        var idString = friendIds.split(";")
//                        idString.forEach {
//                            queueUtils.enqueueAction(copyToChatMsgBean(chatMsg, it))//放在消息队列，一个一个保存
//                        }
//                    } else {
//                        queueUtils.enqueueAction(copyToChatMsgBean(chatMsg, friendIds))
//                    }
//                }
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    groupSendMsgResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    groupSendMsgResult.value = LoadState.Fail(exc = Exception("发送群发消息发送失败"))
                }
            }
        }, onError = {
            groupSendMsgResult.value = LoadState.Fail(exc = Exception("发送群发消息发送失败"))
        }, onStart = {
            groupSendMsgResult.value = LoadState.Loading()
        })
    }

    /**
     * 复制成chatMessageBean
     */
    private fun copyToChatMsgBean(chatMsg: ChatMessageBean, friendId: String): ChatMessageBean {
        var newChatMsg = ChatMessageBean()
        newChatMsg.to = friendId
        newChatMsg.id = System.currentTimeMillis().toString()
        newChatMsg.chatType = chatMsg.chatType
        newChatMsg.cmd = chatMsg.cmd
        newChatMsg.from = chatMsg.from
        newChatMsg.content = chatMsg.content
        newChatMsg.msgType = chatMsg.msgType
        newChatMsg.createTime = chatMsg.createTime
        newChatMsg.dir = chatMsg.dir
        newChatMsg.uuid = chatMsg.uuid
        newChatMsg.sendState = 1
        newChatMsg.servicePath = chatMsg.servicePath
        newChatMsg.isUpload = chatMsg.isUpload
        return newChatMsg
    }

    /**
     * 转发消息,收藏转发消息使用
     *    MsgType.MESSAGETYPE_TEXT -> "text"
     *  MsgType.MESSAGETYPE_VOICE -> "voiceMsg"
     * MsgType.MESSAGETYPE_VIDEO -> "videoMsg"
     *MsgType.MESSAGETYPE_PICTURE -> "picture"
     *
     */
    fun forwardMsg(reordBean: RecordBean, targetId: String, chatType: String) {
        when (reordBean.type.uppercase()) {
            "text".uppercase(), "AtText".uppercase() -> {
                //文本消息
                sendMsg(reordBean.content, targetId, chatType)
            }
            "voiceMsg".uppercase() -> {
                //语音消息
                forwardMuilteMsg(reordBean, targetId, chatType, MsgType.MESSAGETYPE_VOICE)
            }
            "videoMsg".uppercase() -> {
                //视频消息
                forwardMuilteMsg(reordBean, targetId, chatType, MsgType.MESSAGETYPE_VIDEO)
            }
            "picture".uppercase() -> {
                //图片消息
                forwardMuilteMsg(reordBean, targetId, chatType, MsgType.MESSAGETYPE_PICTURE)
            }
            "File".uppercase() -> {
                //文件消息
                forwardMuilteMsg(reordBean, targetId, chatType, MsgType.MESSAGETYPE_FILE)
            }
        }
    }

    /**
     * 转发多媒体消息
     */
    private fun forwardMuilteMsg(
        recordBean: RecordBean,
        targetId: String,
        chatType: String,
        sendMsgType: String
    ) {
        val params = createSendParams(recordBean.content, targetId, chatType).apply {
            msgType = sendMsgType
        }

        val chatMsg = copyToChatMsgBean(params).apply {
            //保存本地路径
            content = recordBean.content
        }

        //生成会话列表
        if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
            conversationBd.saveGroupConversation(
                chatMsg.groupId,
                chatMsg.content,
                chatMsg.msgType,
                fromId = MMKVUtils.getUser()?.id ?: ""
            )
        } else {
            conversationBd.saveFriendConversation(
                chatMsg.to,
                chatMsg.content,
                chatMsg.msgType
            )
        }

        chatMsg.isUpload = true
        //保存消息到数据库
        val tempMsg = msgDb.saveChatMsg(chatMsg)

        //页面显示
        sendMsgResult.value = chatMsg

        //2、发送消息
        WebsocketWork.WS.sendMsg(
            GsonUtil.toJson(params), success = { msgId ->
                ChatDao.getChatMsgDb().updateMsgSendState(1, tempMsg.dbId, msgId)
            }, faile = {
                ChatDao.getChatMsgDb().updateMsgSendState(2, tempMsg.dbId, "")
            }
        )
    }

    /**
     * 删除消息
     */
    fun deleteMessage(
        id: String,
        chatType: String,
        groupId: String,
        from: String,
        to: String,
        deleteMessageType: String
    ) {
        requestLifeLaunch({
            ChatRepository.deleteMessage(id, chatType, groupId, from, to, deleteMessageType)
        }, {
            it.printStackTrace()
        })
    }

    /**
     * 发送消息已读回执
     */
    fun sendReadState(msgId: String, targetId: String, chatType: String, from: String) {
        try {
            if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                WebsocketWork.WS.sendReadState(msgId, targetId, chatType, from)
            } else if (chatType == ChatType.CHAT_TYPE_GROUP) {
                WebsocketWork.WS.sendReadState(msgId, targetId, chatType, from)
            }
            //更新数据库状态
            ChatDao.getChatMsgDb().updateMsgReadState(1, msgId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}