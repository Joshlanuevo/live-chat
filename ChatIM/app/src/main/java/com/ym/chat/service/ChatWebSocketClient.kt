package com.ym.chat.service

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.constant.EventKeys.UPDATE_NOTIFY
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.util.other.QueueUtil
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.app.BaseApp
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.DelConBean
import com.ym.chat.bean.GroupActionBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.rxhttp.UserRepository
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.CommandType
import com.ym.chat.utils.CommandType.DELETE_NOTIFY_MSG
import com.ym.chat.utils.DataCleanManagerUtils
import com.ym.chat.utils.GNLog
import com.ym.chat.utils.ImCache
import com.ym.chat.utils.LogHelp
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.StringExt.decodeContent
import com.ym.chat.utils.ToastUtils
import com.ym.chat.viewmodel.FriendViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import javax.net.ssl.SSLParameters

class ChatWebSocketClient(
    serverUri: URI?,
    val context: Context,
    private val onConnected: (() -> Unit)? = null,
    private val onClose: (() -> Unit)? = null,
    private val onError: (() -> Unit)? = null,
    private val onReceiverHeart: (() -> Unit)? = null,
) :
    WebSocketClient(serverUri) {

    val receiverMsgIds = mutableListOf<String>()//消息id去重使用

    /**
     * 消息队列处理收到消息
     */
    private val quenUtils = QueueUtil { msg ->
        //处理消息队列里的事件
        if (msg is String && !TextUtils.isEmpty(msg)) {
            ChatUtils.processMsg(msg)
        }
    }

    /**
     * 更换url
     */
    fun changeUrl() {
        var url = ApiUrl.websocketUrl + "?token=${MMKVUtils.getToken()}"
        uri = URI.create(url)
    }

    /**
     * 消息队列处理收到消息
     */
    private val wsQueUtils = QueueUtil { msg ->
        //处理消息队列里的事件
        if (msg is String && !TextUtils.isEmpty(msg)) {
            processMsg(msg)
        }
    }

    /**
     * 消息队列处理收到消息
     */
    private val historyMsg = QueueUtil { msg ->
        //处理消息队列里的事件
        if (msg is JSONArray && msg.length() > 0) {
            popStackMsg(msg)
        }
    }

    override fun onSetSSLParameters(sslParameters: SSLParameters?) {
//        super.onSetSSLParameters(sslParameters)
    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                UserRepository.getUserInfo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        handshakedata?.httpStatusMessage?.let {
            GNLog.d("ws-onOpen:${it}")
            Log.d("ChatWebSocketClient", it)
        }
    }

    override fun onMessage(message: String?) {
        if (!TextUtils.isEmpty(message)) {
            message?.let {
                try {
                    val msg = it.decodeContent()
                    Log.d("ChatWebSocketClient", "收到消息<<<${msg}")
                    val jsonObject = JSONObject(msg)
                    val command = jsonObject.optInt("command")
                    if (command != 13) {
                        GNLog.netWork("收到WS消息<<<${msg}")
                    }
                    wsQueUtils.enqueueAction(msg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun processMsg(message: String) {
        if (!TextUtils.isEmpty(message)) {
            val jsonObject = JSONObject(message)
            val command = jsonObject.optInt("command")
            when (command) {
                CommandType.LOGIN_FEEDBACK -> {
                    //登陆反馈
                    val code = jsonObject.optInt("code")
                    if (code == 10007) {
                        //登陆成功
                        onConnected?.invoke()
                        getAddMemberListToGroup()

                        //获取会话列表
                        ChatDao.getConverList()

                        //删除所有系统通知数据
                        ChatDao.getNotifyDb().delAllNotifyMsg()
                        //获取好友申请通知
                        ChatUtils.getFriendNotifyList()
                        //获取系统消息
                        getSysNitice()
                    } else if (code == 10008) {
                        //登陆失败
                        "ws登陆失败".toast()
                        GNLog.netWork("ws登陆失败")
                        MMKVUtils.clearUserInfo()
                        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                    }
                }

                CommandType.HEART_PACKAGE -> {
                    //心跳包
                    onReceiverHeart?.invoke()
                }

                CommandType.DELETE_GROUP -> {
                    //后台解散一个群
                    try {
                        val jsonData = jsonObject.optJSONObject("data")
                        val groupId = jsonData.optString("groupId")
//                        //删除本地所有聊天数据
//                        ChatDao.getChatMsgDb().delMsgListByGroupId(groupId)
                        //删除本地会话数据
                        ChatDao.getConversationDb().delConverByTargtId(groupId)
                        //发广播更新群列表
                        LiveEventBus.get(EventKeys.DELETE_GROUP, String::class.java)
                            .post(groupId)
                        //发广播到聊天页面
                        LiveEventBus.get(EventKeys.SYSTEM_DEL_GROUP, String::class.java)
                            .post(groupId)
                    } catch (e: Exception) {
                    }
                }

                CommandType.JOIN_GROUP -> {
                    //新建入群
                    if (ImCache.isUpdateNotifyMsg)
                        ChatDao.syncFriendAndGroupToLocal(
                            isSyncFriend = false,
                            isSyncGroup = true,
                            isEventUpdateConver = true
                        )
                    val jsonData = jsonObject.optJSONObject("data")
                    val createTime = jsonObject.optLong("createTime")
                    val id = jsonObject.optString("id")
                    val createTime11 = jsonData.optLong("createTime")
                    val groupIdStr = jsonData.optString("groupId")
                    val userId = jsonData.optString("memberId")
                    val nick = jsonData.optString("name")
                    var groupName = jsonData.optString("groupName")

                    //生成提示语
                    if (userId != MMKVUtils.getUser()?.id) {
                        //非自己
                        //生成会话列表数据
                        ChatDao.getConversationDb().saveGroupConversation(
                            groupIdStr,
                            "${nick}创建「${groupName}」群",
                            MsgType.MESSAGETYPE_TEXT,
                            nameStr = groupName,
                            isMute = true,
                            fromId = ""
                        )

                        val delConBean = DelConBean().apply {
                            cmd = 49
                            memberId = MMKVUtils.getUser()?.id ?: ""
                            type = ChatType.CHAT_TYPE_GROUP
                            groupId = groupIdStr
                            operationType = "Add"
                        }
                        WebsocketWork.WS.updateConver(delConBean)
//                                createGroupNotice(groupIdStr, "您被${nick}邀请入群", id, createTime)
                    } else {
                        var groupInfo = ChatDao.getGroupDb().getGroupInfoById(groupIdStr)
                        //生成会话列表数据
                        ChatDao.getConversationDb().saveGroupConversation(
                            groupIdStr,
                            "您创建了群组",
                            MsgType.MESSAGETYPE_TEXT,
                            nameStr = groupName,
                            isMute = true,
                            fromId = MMKVUtils.getUser()?.id ?: ""
                        )

                        val delConBean = DelConBean().apply {
                            cmd = 49
                            memberId = MMKVUtils.getUser()?.id ?: ""
                            type = ChatType.CHAT_TYPE_GROUP
                            groupId = groupIdStr
                            operationType = "Add"
                        }
                        WebsocketWork.WS.updateConver(delConBean)

                        if (groupInfo != null) {
                            //说明这个群是移动端操作的，不需要处理
                        } else {
                            //说明这个群是pc操作的
                            //生成提示语
//                                    val bean = ChatMessageBean().apply {
//                                        to = ""
//                                        from = ""
//                                        sendState = 1
//                                        groupId = groupIdStr
//                                        chatType = ChatType.CHAT_TYPE_GROUP
//                                        this.createTime = System.currentTimeMillis()
//                                        msgType = MsgType.MESSAGETYPE_NOTICE
//                                        content = "您创建了「${groupName}」群:\n" +
//                                                "群人数可达1000人\n" +
//                                                "群置顶消息可设置五则\n" +
//                                                "对话信息保存七日\n" +
//                                                "群管理员可禁言、踢人"
//                                    }
//                                    //1、保存消息到数据库
//                                    ChatDao.getChatMsgDb().saveChatMsg(bean)
//
                        }
                    }
                }

                CommandType.NEWFRIEND_ADDME, CommandType.ADD_FRIEND -> {
                    //有人添加我为好友刷新本地好友数据
                    ChatDao.syncFriendAndGroupToLocal(
                        isSyncFriend = true,
                        isSyncGroup = false,
                        isEventUpdateConver = ImCache.isUpdateNotifyMsg
                    )
                    try {
                        val jsonData = jsonObject.optJSONObject("data")
                        var memberId = jsonData.optString("memberId")//操作者id
                        var friendMemberId = jsonData.optString("friendMemberId")//被添加者id
                        if (memberId == MMKVUtils.getUser()?.id) {
                            //操作者是自己
                            var name =
                                if (!jsonData.isNull("friendMemberName")) jsonData.optString(
                                    "friendMemberName"
                                ) else "Ta"
                            ChatDao.getConversationDb().saveFriendConversation(
                                friendMemberId,
                                "您已和 「${name}」成为好友",
                                MsgType.MESSAGETYPE_TEXT
                            )
                        } else {
                            //操作者是别人
                            var name =
                                if (!jsonData.isNull("memberName")) jsonData.optString("memberName") else "Ta"
                            ChatDao.getConversationDb().saveFriendConversation(
                                memberId,
                                "您已和 「${name}」成为好友",
                                MsgType.MESSAGETYPE_TEXT
                            )
                            //存储好友系统通知
                            ChatDao.getConversationDb()
                                .updateNotifyLastMsg(
                                    msgType = 0,
                                    content = "好友申请",
                                    msgTime = System.currentTimeMillis()
                                )
                        }
                    } catch (e: Exception) {
                    }
                }

                CommandType.DEL_FRIEND -> {
                    try {
                        val jsonData = jsonObject.optJSONObject("data")
                        var memberId = jsonData.optString("memberId")//操作者id
                        val friendMemberId =
                            jsonData.optString("friendMemberId")//别删除者Id
                        if (MMKVUtils.getUser()?.id == memberId) {
                            memberId = friendMemberId
                        }

                        ChatDao.syncFriendAndGroupToLocal(
                            isSyncFriend = true,
                            isSyncGroup = false
                        )
                        //清除会话历史记录
                        ChatDao.getChatMsgDb().delMsgListByFriendId(memberId)
                        //清除主页新消息窗口
                        ChatDao.getConversationDb().delConverByTargtId(memberId)
                        LiveEventBus.get(
                            EventKeys.DEL_FRIEND_ACTION,
                            String::class.java
                        ).post(memberId)
                    } catch (e: Exception) {
                        "数据解析异常=${jsonObject.toString()}".logE()
                    }
                }

                CommandType.OTHER_LOGIN -> {
                    //KickOut: '您的账号已在别处登录'  Disable: '您已被后台管理员封禁'
                    try {
                        GNLog.netWork("异地登录${message}")
                        val jsonData = jsonObject.optJSONObject("data")
                        val type =
                            jsonData.optString("type")//type=   KickOut: '您的账号已在别处登录'  Disable: '您已被后台管理员封禁'
                        MMKVUtils.clearUserInfo()
                        ImCache.KillOutType = type//记录被踢原因
                        com.blankj.utilcode.util.ToastUtils.showShort("收到31号消息")
                        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                CommandType.GROUP_ADMIN_ADD_MEMBER -> {
                    //群管理员邀请人入群
                    getAddMemberListToGroup()
                }

                CommandType.DELETE_MSG -> {
                    //远程销毁单条消息
//                    val jsonData = jsonObject.optJSONObject("data")
                    val id = jsonObject.optString("id")
//                            ChatDao.getChatMsgDb().delMessageByServiceId(id)
                    //获取会话列表
                    ChatDao.getConverList()

                    LiveEventBus.get(EventKeys.DEL_MSG_ONE, String::class.java)
                        .post(id)
                }

                CommandType.NOTIFICATION_DEL_CHAT_MSG -> {
                    //更新会话列表
                    val jsonObject = JSONObject(message)
                    val dataObject = jsonObject.optJSONObject("data")
                    val operationType = dataObject.optString("operationType")
                    if (operationType.lowercase() == "addtop" || operationType.lowercase() == "deltop") {
                        val type = dataObject.optString("type")
                        if (type == ChatType.CHAT_TYPE_FRIEND) {
                            //好友
                            val friendMemberId = dataObject.optString("friendMemberId")
                            ChatDao.getConversationDb()
                                .setTopState(operationType == "AddTop", friendMemberId)
                        } else {
                            //群聊
                            val groupId = dataObject.optString("groupId")
                            ChatDao.getConversationDb()
                                .setTopState(operationType == "AddTop", groupId)
                        }
                    } else if (operationType.lowercase() == "del") {
                        //删除会话
                        val type = dataObject.optString("type")
                        if (type == ChatType.CHAT_TYPE_FRIEND) {
                            //好友
                            val friendMemberId = dataObject.optString("friendMemberId")
                            ChatDao.getConversationDb()
                                .delConverByTargtId(friendMemberId)
                        } else {
                            //群聊
                            val groupId = dataObject.optString("groupId")
                            ChatDao.getConversationDb()
                                .delConverByTargtId(groupId)
                        }
                    } else if (operationType.lowercase() == "add") {
//                        getConverList()
                    } else if (operationType.lowercase() == "report") {
                        //同步数量
                        val type = dataObject.optString("type")
                        val unreadCount = dataObject.optInt("unreadCount")
                        if (type == ChatType.CHAT_TYPE_FRIEND) {
                            //好友
                            val friendMemberId = dataObject.optString("friendMemberId")
                            ChatDao.getConversationDb()
                                .setConverMsgCount(friendMemberId, unreadCount)
                        } else {
                            //群聊
                            val groupId = dataObject.optString("groupId")
                            ChatDao.getConversationDb()
                                .setConverMsgCount(groupId, unreadCount)
                        }
                    }
                }

                CommandType.NOTIFICATION_UNREAD_CHAT_MSG -> {
                    //接收 标记会话 ws 已读/未读
                    try {
                        val data = jsonObject.optJSONObject("data")
                        if (data != null) {
                            /**会话消息标记已读 未读*/
                            val chatType = data.optString("chatType")
                            val to = data.optString("to")
                            val from = data.optString("from")
                            val groupId = data.optString("groupId")
                            val signType = data.optString("signType")

                            var chatId = when (chatType) {
                                "Friend" -> {
                                    from
                                }

                                "Group" -> {
                                    groupId
                                }

                                else -> {
                                    ""
                                }
                            }
                            var isUnRead = signType.lowercase() == "unread"
                            if (isUnRead) {
                                //设置消息未读
                                ChatDao.getConversationDb()
                                    .setConversationRead(chatId, false)
                            } else {
                                //设置消息列表数已读
                                ChatDao.getChatMsgDb().setMsgRead(signType, chatId)
                                //设置会话消息已读
                                ChatDao.getConversationDb()
                                    .setConversationRead(chatId, true)
                                //清除消息个数
                                ChatDao.getConversationDb().resetConverMsgCount(chatId)
                            }
                        }
                    } catch (e: Exception) {
                    }
                }

                CommandType.GET_FRIEND_GROUP_LINE_MSG -> {
                    //获取到好友是否在线，以及在线人数
                    LiveEventBus.get(EventKeys.GET_FRIEND_GROUP_LINE_MSG, String::class.java)
                        .post(jsonObject.toString())
                }

                CommandType.DELETE_USER_MSG -> {
                    //清空所有本地消息
                    ChatDao.getChatMsgDb().delAllMsg()
                    //清空所有会话消息
                    ChatDao.getConversationDb().delConver()
                }

                CommandType.UPDATE_SENSITIVE_WORD_MSG -> {
                    //后台更新了敏感词
                    LiveEventBus.get(EventKeys.UPDATE_SENSITIVE_WORD_MSG, Boolean::class.java)
                        .post(true)
                }

                CommandType.UPDATE_GIF_MSG -> {
                    //其他端更新了gif图片
                    LiveEventBus.get(EventKeys.UPDATE_GIF_MSG, Boolean::class.java).post(true)
                }

                CommandType.CLEAR_LOCAL_CACHE_MSG -> {
                    //清除本地缓存
                    BaseApp.appContent?.let { it1 -> DataCleanManagerUtils.clearAllCache(it1) }
                    "已清除本地缓存".toast()
                }

                CommandType.SYSTEM_MSG, CommandType.DEVICE_LOGIN_VERIFICATION, CommandType.SYSTEM_FEEDBACK_MSG, DELETE_NOTIFY_MSG, CommandType.NOTIFICATION_COUNT -> {
                    //获取好友申请通知
                    ChatUtils.getFriendNotifyList()
                    //获取系统通知
                    getSysNitice()
                    LiveEventBus.get(UPDATE_NOTIFY).post("")
                }

                CommandType.MSG_EDIT -> {
                    //聊天消息
                    val data = jsonObject.optString("data")
                    val chatMsg = GsonUtils.fromJson(data, ChatMessageBean::class.java)
                    if (chatMsg.msgType == MsgType.MESSAGETYPE_TEXT || chatMsg.msgType == MsgType.MESSAGETYPE_AT) {
                        if (!MMKVUtils.isAdmin() && ChatUtils.msgContentHasKeyWork(
                                chatMsg.content
                            )
                        ) {
                            "过滤敏感词消息:${chatMsg}".logE()
                        } else {
                            //通知页面刷新已被编辑的消息
                            LiveEventBus.get(
                                EventKeys.EDIT_UPDATE,
                                ChatMessageBean::class.java
                            ).post(chatMsg)
                        }
                    } else {
                        //通知页面刷新已被编辑的消息
                        LiveEventBus.get(
                            EventKeys.EDIT_UPDATE,
                            ChatMessageBean::class.java
                        ).post(chatMsg)
                    }
                }

                CommandType.DEL_ALL_MSG_GROUP -> {
                    //远程销毁所有消息（群）
                    try {
                        val jsonData = jsonObject.optJSONObject("data")
                        val chatType = jsonData.optString("chatType")
                        if (chatType == ChatType.CHAT_TYPE_GROUP) {
                            val groupId = jsonData.optString("groupId")
                            //删除本地所有聊天数据
                            ChatDao.getChatMsgDb().delMsgListByGroupId(groupId)
                            //更新本地会话数据
                            ChatDao.getConversationDb()
                                .updateMsgByTargtId(groupId, "消息已被远程销毁")


                            ChatDao.getConverList()
//                                    val index = ImCache.atConverMsgList.indexOfFirst {
//                                        it.sessionId == groupId
//                                    }
//                                    if(index>=0){
//                                        ImCache.atConverMsgList.removeAt(index)
//                                    }

                            LiveEventBus.get(EventKeys.DEL_MSG_ALL, String::class.java)
                                .post(groupId)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                CommandType.DEL_ALL_MSG -> {
                    //远程销毁所有消息（好友）
                    val jsonData = jsonObject.optJSONObject("data")
                    val chatType = jsonData.optString("chatType")
                    if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                        val from = jsonData.optString("from")
                        val to = jsonData.optString("to")

                        var delTarget = ""
                        if (from == MMKVUtils.getUser()?.id) {
                            //消息是自己发送给自己的
                            delTarget = to
                        } else {
                            delTarget = from
                        }
                        //删除本地所有聊天数据
                        ChatDao.getChatMsgDb().delMsgListByFriendId(delTarget)
                        //更新本地会话数据
                        ChatDao.getConversationDb()
                            .updateMsgByTargtId(delTarget, "消息已被远程销毁")

                        LiveEventBus.get(EventKeys.DEL_MSG_ALL, String::class.java)
                            .post(delTarget)
                    }
                }

                CommandType.MSG_READ -> {
                    //消息已读回执
                    val data = jsonObject.optJSONObject("data")
                    if (data != null) {
                        val to = data.optString("to")
                        val messageId = data.optString("messageId")
                        val chatType = data.optString("chatType")
                        //更新数据库已读
                        if (chatType == ChatType.CHAT_TYPE_GROUP) {
                            val groupId = data.optString("groupId")
                            if (to == MMKVUtils.getUser()?.id) {
                                //重置会话消息未读数量为0
                                ChatDao.getConversationDb().resetConverMsgCount(groupId)
                                ChatDao.getChatMsgDb().updateMsgReadStateGroup(groupId)
                            } else {
                                ChatDao.getChatMsgDb().updateMsgReadStateGroup(groupId)
                            }
                        } else if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                            val from = data.optString("from")
                            if (to == MMKVUtils.getUser()?.id) {
                                //重置会话消息未读数量为0
                                ChatDao.getConversationDb().resetConverMsgCount(from)
                                ChatDao.getChatMsgDb().updateMsgReadState(from)
                            } else {
                                ChatDao.getChatMsgDb().updateMsgReadState(from)
                            }
                        }
                    }
                }

                CommandType.TOP_MSG -> {
                    //置顶消息
                    try {
                        val createTime = jsonObject.optLong("createTime")
                        val id = jsonObject.optString("id")
                        val jsonData = jsonObject.optJSONObject("data")
                        val type = jsonData.optString("type")
                        if (type == "Message") {//只有是群置顶消息
                            var operatorName = jsonData.optString("operatorName")
                            val operatorId = jsonData.optString("operatorId")
                            val groupId = jsonData.optString("groupId")
                            val operationType = jsonData.optString("operationType")//Delete  Add
                            if (operatorId.isNotEmpty()) {//如果为空说明是编辑置顶消息
                                //不为空时 生成通知
                                if (MMKVUtils.getUser()?.id == operatorId) {
                                    operatorName = "我"
                                }
                                var content = when (operationType) {
                                    "Add" -> "${operatorName}设置了一条置顶消息"
                                    "Delete" -> "${operatorName}删除了一条置顶消息"
                                    else -> ""
                                }
                                ChatUtils.createGroupNotice(
                                    groupId,
                                    content,
                                    id,
                                    createTime
                                )
                            }
                        }
                        LiveEventBus.get(EventKeys.MSG_TOP_WS, Boolean::class.java).post(true)
                    } catch (e: Exception) {
                    }
                }

                CommandType.GROUP_ACTION -> {
                    //群操作事件
                    val data = jsonObject.optString("data")
                    val createTime = jsonObject.optLong("createTime")
                    val id = jsonObject.optString("id")
                    try {
                        val groupAction =
                            GsonUtils.fromJson<GroupActionBean>(
                                data,
                                GroupActionBean::class.java
                            )
                        //处理数据库更新
                        ChatUtils.processGroupEvent(groupAction, id, createTime)

                        LiveEventBus.get(
                            EventKeys.GROUP_ACTION, GroupActionBean::class.java
                        ).post(groupAction)
                    } catch (e: Exception) {
                        "数据解析异常=${jsonObject.toString()}".logE()
                    }
                }

                CommandType.GROUP_DEL_MEMBER -> {

                    //群成员被移出群聊
                    val data = jsonObject.optJSONObject("data")
                    val msgCreateTime = jsonObject.optLong("createTime")
                    val msgId = jsonObject.optString("id")
                    val groupIdStr = data.optString("groupId")
                    val name = data.optString("operatorName")//操作人
                    val messageType = data.optString("messageType")//操作人
                    val operatorId = data.optString("operatorId")//操作人ID
                    val memberName = data.optString("name")//被移除人名字
                    val memberId = data.optString("memberId")//被移除人id

                    if (messageType == ChatType.Leave) {
                        //退群
                        //生成提示语
                        if (memberId == MMKVUtils.getUser()?.id) {
                            //我退出了群聊
                            ChatDao.getConversationDb().delConverByTargtId(groupIdStr)
                            LiveEventBus.get(
                                EventKeys.EVENT_REFRESH_CONTACT_LOCAL,
                                Boolean::class.java
                            ).post(true)
                        } else {
                            //吃瓜群众，看到别人退群了
//                                    createGroupNotice(
//                                        groupIdStr,
//                                        "${memberName}离开了群组",
//                                        msgId,
//                                        msgCreateTime
//                                    )
                        }
                    } else if (messageType == ChatType.DeleteGroupMember) {
                        //生成提示语
                        val bean = ChatMessageBean().apply {
                            to = ""
                            from = ""
                            sendState = 1
                            groupId = groupIdStr
                            id = msgId
                            chatType = ChatType.CHAT_TYPE_GROUP
                            createTime = msgCreateTime
                            msgType = MsgType.MESSAGETYPE_NOTICE
                            content = if (operatorId == MMKVUtils.getUser()?.id) {
                                //踢出成员管理员自己
                                "您把${memberName}移出了群组"
                            } else if (memberId == MMKVUtils.getUser()?.id) {
                                //我被别人踢出了群聊
                                LiveEventBus.get(
                                    EventKeys.EVENT_REFRESH_CONTACT_LOCAL,
                                    Boolean::class.java
                                )
                                    .post(true)
                                "您被${name}移出了群组"
                            } else {
                                //吃瓜群众看到的提示，非自己
                                "${memberName}被${name}移出了群组"
                            }
                        }

                        if (memberId == MMKVUtils.getUser()?.id) {
                            //删除会话数据，存储提示
                            try {
//                                        ChatDao.getChatMsgDb().saveChatMsg(bean)
                                ChatDao.getConversationDb().delConverByTargtId(groupIdStr)
                            } catch (e: Exception) {
                                "重复消息$message".logE()
                            }
                        } else {
//                                    saveMsg(bean)
//                                    createGroupNotice(
//                                        groupIdStr,
//                                        bean.content,
//                                        msgId,
//                                        msgCreateTime
//                                    )
                        }

                        //移除群成员
                        LiveEventBus.get(EventKeys.DEL_MEMBER_GROUP, String::class.java)
                            .post(message)
                    }
                }

                CommandType.CHAT, CommandType.CHAT_REPLY -> {
                    //聊天消息
                    val data = jsonObject.optString("data")
                    val chatMsg = GsonUtils.fromJson(data, ChatMessageBean::class.java)
                    //同步PC端过来的消息
                    if (chatMsg.from == MMKVUtils.getUser()?.id) {
                        val toId =
                            if (chatMsg.chatType == ChatType.CHAT_TYPE_FRIEND) chatMsg.to else chatMsg.groupId
                        if (chatMsg.from == chatMsg.to) {
//                        {"command":11,"createTime":1643004345060,"data":{"chatType":"Friend","content":"不会","createTime":1643004345060,"from":"2881308859587452928","id":"2898304818517590016","msgType":"Text","to":"2881308859587452928"}}
                            //群发的消息，不需要处理
                            return
                        }

                        ChatUtils.saveMsg(chatMsg, isSelfMsg = true)
                    } else {
                        ChatUtils.saveMsg(chatMsg)
                    }
                }

                CommandType.GROUP_ADD_MEMBER -> {

                    //群成员被邀请加入群组
//                    val data = jsonObject.optJSONObject("data")
//                    val msgCreateTime = jsonObject.optLong("createTime")
//                    val id = jsonObject.optString("id")
//                    val groupIdStr = data.optString("groupId")
//                    val name = data.optString("operatorName")//操作人
//                    val operatorId = data.optString("operatorId")//操作人ID
//                    val memberName = data.optString("name")//被移除人名字
//                    val userId = data.optString("memberId")//被移除人id
//                    val groupName = data.optString("groupName")//被移除人id
//
//                    //生成提示语
//                    val content =
//                        if (userId == MMKVUtils.getUser()?.id) {
//                            //被邀请人是自己
//                            if (ImCache.isUpdateNotifyMsg)
//                                ChatDao.syncFriendAndGroupToLocal(
//                                    isSyncFriend = false,
//                                    isSyncGroup = true
//                                )
//
//                            ChatDao.getGroupDb()
//                                .saveGroup(GroupInfoBean(id = groupIdStr, name = groupName))
//                            "您被${name}邀请入群"
//                        } else if (operatorId == MMKVUtils.getUser()?.id) {
//                            //我邀请了别人
//                            "您邀请了${memberName}入群"
//                        } else {
//                            //非自己，吃瓜群众看到的提示
//                            "${name}邀请${memberName}入群"
//                        }
//
//                    ChatDao.getConversationDb().saveGroupConversation(
//                        groupIdStr,
//                        content,
//                        MsgType.MESSAGETYPE_TEXT,
//                        nameStr = groupName
//                    )

//                            createGroupNotice(groupIdStr, content, id, msgCreateTime)

                    //添加群成员
                    LiveEventBus.get(EventKeys.GROUP_ADD_MEMBER, String::class.java)
                        .post(message)
                }

                CommandType.EDIT_USER_NAME_AND_HEADER -> {
                    //多端同步个人信息 头像 名字改变
                    ChatUtils.getUserInfo(true)
                }

                CommandType.NOTIFICATION_EDIT_FRIEND_INFO -> {
                    //更新了好友信息
                    try {
                        val jsonData = jsonObject.optJSONObject("data")
                        val friendMemberId = jsonData.optString("friendMemberId")
                        if (ImCache.isUpdateNotifyMsg)
                            FriendViewModel().getFriendList(true, friendMemberId)
                    } catch (e: Exception) {
                    }
                }

                CommandType.DELETE_GROUP_NOTIFY_MSG -> {
                    //多端同步删除群申请通知消息
                    try {
                        val messageIdList = jsonObject.optJSONArray("data")
                        if (messageIdList != null && messageIdList.length() > 0) {
                            for (index in 0 until messageIdList.length()) {
                                var notifyId = messageIdList[index]
                                if (notifyId is String)
                                    ChatDao.getNotifyDb().delNotifyMsgById(notifyId)
                            }
                            //发广播通知主页更新
                            LiveEventBus.get(
                                EventKeys.DELETE_NOTIFY_MSG,
                                String::class.java
                            ).post("")
                        }
                    } catch (e: Exception) {
                    }
                }

                else -> {
                    //其他消息，通过http接口获取
                    if (ImCache.isUpdateNotifyMsg) {
                        getMsgList()
                    }
                }
            }
        }
    }

    /**
     * 获取会话列表的最近20条数据
     */
    suspend fun getConverMsg() {
        ChatDao.getConversationDb().getConversationList().forEach {
            try {
                if (it.type == 0) {
                    val msgResult = ChatRepository.getHisMsg("0", "Down", 1, it.chatId, "", 1)
                    val jsonObject = JSONObject(msgResult)
                    handleMsg(jsonObject)
                } else if (it.type == 1) {
                    val msgResult = ChatRepository.getHisMsg("0", "Down", 1, "", it.chatId, 1)
                    val jsonObject = JSONObject(msgResult)
                    handleMsg(jsonObject)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 处理消息
     */
    private fun handleMsg(jsonObject: JSONObject) {
        val code = jsonObject.optInt("code")
        if (code == 200) {
            val dataObject = jsonObject.optJSONObject("data")
            val records = dataObject.optJSONArray("records")
            if (records != null && records.length() > 0) {
                for (index in 0 until records.length()) {
                    val record = records.optJSONObject(index)
                    val readFlag = record.optString("readFlag")//已读未读状态
                    val content = record.optString("content")
                    val decodMsg = content.decodeContent()
                    LogHelp.d("handleMsg", decodMsg)
//                    ChatUtils.processMsg(decodMsg)
                }
            }
        }
    }

    /**
     * 获取历史消息
     */
    suspend fun getHistoryMsgList(lastId: String = "") {
        try {
            var result = ChatRepository.getHistoryMsg(lastId)
            val jsonObject = JSONObject(result)
            val code = jsonObject.optInt("code")
            if (code == 200) {
                ImCache.isUpdateNotifyMsg = false
//                    "-------拉取离线消息获取数据开始是否允许通知=${ImCache.isUpdateNotifyMsg}---result=${result}".logD()

                val data = jsonObject.optJSONArray("data")
                var lastMsgId = ""
                if (data != null && data.length() > 0) {
                    historyMsg.enqueueAction(data)
                    val temoObject = data.getJSONObject(data.length() - 1)
                    lastMsgId = temoObject.optString("id")
                }

                //如果有>=1000条，再次获取
                if (data.length() >= 1000) {
                    //通知页面刷新
//                    LiveEventBus.get(EventKeys.MANDATORY_UPDATE_CONVER, String::class.java).post("")
                    getHistoryMsgList(lastMsgId)
                    Log.d("获取历史消息", "大于1000条")
                } else {
                    //上报已经获取回来的消息，里面的ID
                    ImCache.isUpdateNotifyMsg = true

                    //获取会话列表里面群组成员数据
                    getConverGroupMem()

                    //获取置顶会话
                    getTopConverList()

                    LiveEventBus.get(EventKeys.GET_HIS_COMPLETE, String::class.java).post("")
                }
            }
        } catch (e: Exception) {
            ImCache.isUpdateNotifyMsg = true
            //通知页面刷新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
            "-------拉取离线历史消息获取数据 处理数据异常".logE()
            e.printStackTrace()
        }
    }

    /**
     * 处理栈内消息
     */
    private fun popStackMsg(data: JSONArray) {
        try {
            for (index in 0 until data.length()) {
                val temoObject = data.getJSONObject(index)
                val content = temoObject.optString("content")
                val str = content.decodeContent()
                Log.d("ChatWebSocketClient", "Http收到历史消息<<<${str}")
                ChatUtils.processMsg(str, true)
                ChatDao.getChatMsgDb().updateLastMsgId(temoObject.optString("id"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取会话列表群成员数据
     */
    private fun getConverGroupMem() {
        val converList = ChatDao.getConversationDb().getConversationList()
        if (converList != null && converList.size > 0) {
            val groupIds = converList.filter { it.type == 1 }
            GlobalScope.launch(Dispatchers.IO) {
                groupIds.forEach { group ->
                    val result = GroupRepository.getGroupMemberList(group.chatId)
                    val tempList = mutableListOf<GroupMemberBean>()
                    if (result.code == 200) {
                        //保存群成员数据
                        ChatDao.getGroupDb().updateMemByGroupId(group.chatId, result.data)
                        tempList.addAll(result.data)
                    }
                }

                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
            }
        }
    }

    /**
     * 获取置顶会话
     */
    private fun getTopConverList() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = ChatRepository.getTopInfo()
                if (result.code == 200) {
                    /**云端置顶列表*/
                    var topList = result.data

                    /**获取本地置顶数据列表*/
                    var topMsgList = ChatDao.getConversationDb().getConversationTopList()

                    if (topList != null && topList.size > 0) {
                        /**获取本地没有置顶列表*/
                        var topMsgListNot = ChatDao.getConversationDb().getConversationNotTopList()

                        topList.forEachIndexed { index, msgTopBean ->
                            var id = msgTopBean.groupId
                            if (id.isNullOrBlank()) {
                                id = msgTopBean.friendMemberId ?: ""
                            }
                            /**没有置顶的加入置顶*/
                            topMsgListNot.forEachIndexed { i, c ->
                                //加入置顶
                                if (id == c.chatId) {
                                    c.isTop = true
//                                    c.topTime = msgTopBean.createTime
                                    c.topId = msgTopBean.id
                                    ChatDao.getConversationDb().saveConversation(c)
                                }
                            }
                        }

                        /**已置顶的，如果没有数据 取消置顶*/
                        topMsgList.forEachIndexed { i, c ->
                            var isTop = false
                            topList.forEachIndexed { index, msgTopBean ->
                                var id = msgTopBean.groupId
                                if (id.isNullOrBlank()) {
                                    id = msgTopBean.friendMemberId ?: ""
                                }
                                if (id == c.chatId) {
                                    isTop = true
                                }
                            }
                            if (!isTop) {
                                //已取消置顶
                                c.isTop = false
                                c.topTime = 0
                                c.topId = ""
                                ChatDao.getConversationDb().saveConversation(c)
                            }
                        }
                    } else {
                        /**已置顶的，如果没有数据 取消置顶*/
                        topMsgList.forEachIndexed { i, c ->
                            //取消置顶
                            c.isTop = false
                            c.topId = ""
                            ChatDao.getConversationDb().saveConversation(c)
                        }
                    }

                    //通知页面刷新
                    LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
                }
            } catch (e: Exception) {
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取未读消息
     * 实时消息
     */
    private fun getMsgList() {
        GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            try {
                val result = ChatRepository.getMsg()

                val jsonObject = JSONObject(result)
                val code = jsonObject.optInt("code")
                if (code == 200) {

                    val data = jsonObject.optJSONArray("data")
                    val contentIds = mutableListOf<String>()
                    if (data != null && data.length() > 0) {
                        for (index in 0 until data.length()) {
                            val temoObject = data.getJSONObject(index)
                            val content = temoObject.optString("content")
                            val groupId = temoObject.optString("groupId")
                            var str = content.decodeContent()
                            val mId = JSONObject(str).optString("id")
                            //过滤重复消息
                            if (receiverMsgIds.contains(mId)) {
                                "重复消息List:${str}".logE()
                            } else {
                                GNLog.netWork("Http收到消息<<<${str}")
                                Log.d("ChatWebSocketClient", "Http收到消息<<<${str}")
                                if (receiverMsgIds.size >= 100000000) {
                                    receiverMsgIds.clear()
                                }

                                receiverMsgIds.add(mId)

                                //保存到待处理队列
                                quenUtils.enqueueAction(str)
                                contentIds.add(mId)
                            }
                        }
                        val temoObject = data.getJSONObject(data.length() - 1)
                        ChatDao.getChatMsgDb().updateLastMsgId(temoObject.optString("id"))
                    }

                    //上报已经获取回来的消息，里面的ID
                    if (contentIds.size > 0) {
                        ChatRepository.messageAck(contentIds)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取未读群管理拉人入群
     */
    private fun getAddMemberListToGroup() {
        GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            try {
                val result = GroupRepository.getAddMemberListToGroup()
                val jsonObject = JSONObject(result)
                val code = jsonObject.optInt("code")
                if (code == 200) {
                    val data = jsonObject.optJSONArray("data")
                    if (data != null) {
//                        //先清空所有群组通知消息
//                        ChatDao.getNotifyDb().delAllNotifyMsg(3)
                        if (data.length() > 0) {
                            for (index in 0 until data.length()) {
                                val tObject = data.getJSONObject(index)
                                Log.d(
                                    "ChatWebSocketClient",
                                    "http拉取群管理拉人入群消息<<<${tObject.toString()}"
                                )
                                ChatDao.getNotifyDb()
                                    .saveAddGroupMemberNotifyMsgByJson(tObject, false)
                            }
                        }
                        //说明数据被清空了  如果现在 停留在通知界面
                        LiveEventBus.get(EventKeys.UPDATE_NOTIFY_MSG).post(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取系统通知
     */
    private fun getSysNitice() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = ChatRepository.getHisNoticeMsg()
                val jsonResult = JSONObject(result)
                val info = jsonResult.optString("code")
                if (info.lowercase() == "200") {
                    val nofityDb = ChatDao.getNotifyDb()

                    val dataArray = jsonResult.optJSONArray("data")
                    if (dataArray != null && dataArray.length() > 0) {
                        for (i in 0 until dataArray.length()) {
                            try {
                                val dataObject = dataArray.optJSONObject(i)
                                val command = dataObject.optInt("command")
                                val readFlag = dataObject.optString("readFlag")
                                val msgReadState = if (readFlag == "Read") {
                                    1
                                } else {
                                    0
                                }
                                val content = dataObject.optString("content")
                                val msgCotnent = content.decodeContent()
                                when (command) {
                                    CommandType.SYSTEM_MSG -> {
                                        //系统通知
                                        nofityDb.saveSystemNotifyMsgByJson(
                                            JSONObject(msgCotnent),
                                            1,
                                            msgReadState
                                        )
                                    }

                                    CommandType.DEVICE_LOGIN_VERIFICATION -> {
                                        //新设备验证码
                                        nofityDb.saveNotifyMsgByJson(JSONObject(msgCotnent))
                                    }

                                    CommandType.SYSTEM_FEEDBACK_MSG -> {
                                        //意见反馈系统通知
                                        nofityDb.saveSystemNotifyMsgByJson(
                                            JSONObject(msgCotnent),
                                            msgReadState = msgReadState
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    //说明数据被清空了  如果现在 停留在通知界面
                    LiveEventBus.get(EventKeys.DELETE_NOTIFY_MSG).post("")
                    LiveEventBus.get(EventKeys.UPDATE_COUNT).post("")
                }
            } catch (e: Exception) {
                //说明数据被清空了  如果现在 停留在通知界面
                LiveEventBus.get(EventKeys.DELETE_NOTIFY_MSG).post("")
                LiveEventBus.get(EventKeys.UPDATE_COUNT).post("")
                e.printStackTrace()
            }
        }
    }

    /**
     * 连接断开
     */
    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        if (reason != null) {
            GNLog.d("ws-onClose:" + "code=${code},reason=${reason}")
            Log.d("ChatWebSocketClient", reason)
        }
        //ws已断开连接，发送失败
//        "连接已断开".toast()
        onClose?.invoke()
    }

    /**
     * 连接出现异常
     */
    override fun onError(ex: Exception?) {

        ex?.message?.let {
            GNLog.d("ws-onError:$it")
            Log.d("ChatWebSocketClient", it)
        }
        onError?.invoke()
    }
}