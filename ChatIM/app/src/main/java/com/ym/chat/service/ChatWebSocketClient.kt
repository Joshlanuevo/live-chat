package com.ym.chat.service

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.Utils
import com.google.common.reflect.TypeToken
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.constant.EventKeys.UPDATE_NOTIFY
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.util.other.QueueUtil
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.app.BaseApp
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupActionBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.rxhttp.UserRepository
import com.ym.chat.utils.*
import com.ym.chat.utils.CommandType.DELETE_NOTIFY_MSG
import com.ym.chat.utils.StringExt.decodeContent
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

class ChatWebSocketClient(
    serverUri: URI?,
    val context: Context,
    private val onConnected: (() -> Unit)? = null,
    private val onClose: (() -> Unit)? = null,
    private val onError: (() -> Unit)? = null,
    private val onReceiverHeart: (() -> Unit)? = null,
) :
    WebSocketClient(serverUri) {

    /**
     * 消息队列处理收到消息
     */
    private val quenUtils = QueueUtil { msg ->
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
        handshakedata?.httpStatusMessage?.let { Log.d("ChatWebSocketClient", it) }
    }

    override fun onMessage(message: String?) {
        if (!TextUtils.isEmpty(message)) {
            message?.let {
//                Log.d("ChatWebSocketClient", "收到消息<<<$it")
                val msg = it.decodeContent()
                Log.d("ChatWebSocketClient", "收到消息<<<${msg}")

                //保存到待处理队列
                quenUtils.enqueueAction(msg)
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
                        getConverList()

                        //获取好友申请通知
                        ChatUtils.getFriendNotifyList()

                        //获取系统消息
                        getSysNitice()
                    } else if (code == 10008) {
                        //登陆失败
                        ChatUtils.getString(R.string.ws登陆失败).toast()
                        MMKVUtils.clearUserInfo()
                        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                    }
                }

                CommandType.HEART_PACKAGE -> {
                    //心跳包
                    onReceiverHeart?.invoke()
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
                CommandType.DELETE_MSG -> {
                    //远程销毁单条消息
                    val jsonData = jsonObject.optJSONObject("data")
                    val id = jsonData.optString("id")
                    ChatDao.getChatMsgDb().delMessageByServiceId(id)

                    LiveEventBus.get(EventKeys.DEL_MSG_ONE, String::class.java)
                        .post(id)
                }
                CommandType.FRIEND_DATA -> {
                    try {
                        //好友数据和群数据
                        val code = jsonObject.optInt("code")
                        val data = jsonObject.optJSONObject("data")
                        //好友数据
                        val friends = data.optJSONArray("friends")
                        if (friends != null && friends.length() > 0) {
                            for (index in 0 until friends.length()) {
                                val tempObjec = friends.optJSONObject(index)
                                //好友分组ID
                                val groupId = tempObjec.optString("groupId")
                                //好友分组名称
                                val name = tempObjec.optString("name")
                                val usersStr = tempObjec.optString("users")
                                val type =
                                    object :
                                        TypeToken<MutableList<ChatMessageBean>>() {}.type
                                val friendList =
                                    GsonUtils.fromJson<MutableList<FriendListBean>>(
                                        usersStr,
                                        type
                                    )
                            }
                        }
                        //群数据
                        val groups = data.optJSONArray("groups")
                        if (groups != null && groups.length() > 0) {
                            for (index in 0 until groups.length()) {
                                val tempObjec = groups.optJSONObject(index)
                                //群头像
                                val avatar = tempObjec.optString("avatar")
                                //群ID
                                val groupId = tempObjec.optString("groupId")
                                //群名称
                                val name = tempObjec.optString("name")
                                //群信息数据
                                val extras = tempObjec.optString("extras")
                                //群成员
                                val usersStr = tempObjec.optString("users")
                                val type =
                                    object :
                                        TypeToken<MutableList<ChatMessageBean>>() {}.type
                                val friendList =
                                    GsonUtils.fromJson<MutableList<FriendListBean>>(
                                        usersStr,
                                        type
                                    )
                            }
                        }
                    } catch (e: Exception) {
                        "数据解析异常=${jsonObject.toString()}".logE()
                    }
                }
                CommandType.GROUP_ADD_MEMBER -> {

                    //群成员被邀请加入群组
                    val data = jsonObject.optJSONObject("data")
                    val msgCreateTime = jsonObject.optLong("createTime")
                    val id = jsonObject.optString("id")
                    val groupIdStr = data.optString("groupId")
                    val name = data.optString("operatorName")//操作人
                    val operatorId = data.optString("operatorId")//操作人ID
                    val memberName = data.optString("name")//被移除人名字
                    val userId = data.optString("memberId")//被移除人id

                    ChatDao.getConversationDb().saveGroupConversation(
                        groupIdStr,
                        "",
                        MsgType.MESSAGETYPE_TEXT
                    )

                    //生成提示语
                    val content =
                        if (userId == MMKVUtils.getUser()?.id) {
                            //被邀请人是自己
                            if (ImCache.isUpdateNotifyMsg)
                                ChatDao.syncFriendAndGroupToLocal(
                                    isSyncFriend = false,
                                    isSyncGroup = true
                                )

                            //生成会话
                            ChatDao.getConversationDb().saveGroupConversation(
                                groupIdStr,
                                "您被${name}邀请入群",
                                MsgType.MESSAGETYPE_TEXT
                            )

                            "您被${name}邀请入群"
                        } else if (operatorId == MMKVUtils.getUser()?.id) {
                            //我邀请了别人
                            "您邀请了${memberName}入群"
                        } else {
                            //非自己，吃瓜群众看到的提示
                            "${name}邀请${memberName}入群"
                        }
//                            createGroupNotice(groupIdStr, content, id, msgCreateTime)

                    //添加群成员
                    LiveEventBus.get(EventKeys.GROUP_ADD_MEMBER, String::class.java)
                        .post(message)
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
                            .updateMsgByTargtId(
                                delTarget,
                                ChatUtils.getString(R.string.yuanchengxiaoxiyibeixiaohui)
                            )

                        LiveEventBus.get(EventKeys.DEL_MSG_ALL, String::class.java)
                            .post(delTarget)
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
                                .updateMsgByTargtId(
                                    groupId,
                                    ChatUtils.getString(R.string.yuanchengxiaoxiyibeixiaohui)
                                )

                            LiveEventBus.get(EventKeys.DEL_MSG_ALL, String::class.java)
                                .post(groupId)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                CommandType.OTHER_LOGIN -> {
                    //KickOut: '您的账号已在别处登录'  Disable: '您已被后台管理员封禁'
                    try {
                        val jsonData = jsonObject.optJSONObject("data")
                        val type =
                            jsonData.optString("type")//type=   KickOut: '您的账号已在别处登录'  Disable: '您已被后台管理员封禁'
                        MMKVUtils.clearUserInfo()
                        ImCache.KillOutType = type//记录被踢原因
                        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
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
                        if (ImCache.isUpdateNotifyMsg)
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

                CommandType.EDIT_USER_NAME_AND_HEADER -> {
                    //多端同步个人信息 头像 名字改变
                    ChatUtils.getUserInfo(true)
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
                                String.format(ChatUtils.getString(R.string.成为好友), name),
                                MsgType.MESSAGETYPE_TEXT
                            )
                        } else {
                            //操作者是别人
                            var name =
                                if (!jsonData.isNull("memberName")) jsonData.optString("memberName") else "Ta"
                            ChatDao.getConversationDb().saveFriendConversation(
                                memberId,
                                String.format(ChatUtils.getString(R.string.成为好友), name),
                                MsgType.MESSAGETYPE_TEXT
                            )
                            //存储好友系统通知
                            ChatDao.getConversationDb()
                                .updateNotifyLastMsg(
                                    msgType = 0,
                                    content = ChatUtils.getString(R.string.好友申请),
                                    msgTime = System.currentTimeMillis()
                                )
                        }
                    } catch (e: Exception) {
                    }
                }

                CommandType.GROUP_ADMIN_ADD_MEMBER -> {
                    //群管理员邀请人入群
                    getAddMemberListToGroup()
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
                        getConverList()
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
                    ChatUtils.getString(R.string.已清除本地缓存).toast()
                }

                CommandType.SYSTEM_MSG, CommandType.DEVICE_LOGIN_VERIFICATION, CommandType.SYSTEM_FEEDBACK_MSG, DELETE_NOTIFY_MSG, CommandType.NOTIFICATION_COUNT -> {
                    //获取好友申请通知
                    ChatUtils.getFriendNotifyList()
                    //获取系统通知
                    getSysNitice()
                    LiveEventBus.get(UPDATE_NOTIFY).post("")
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

                CommandType.CHAT, CommandType.CHAT_REPLY -> {
                    //聊天消息
                    val data = jsonObject.optString("data")
                    val chatMsg = GsonUtils.fromJson(data, ChatMessageBean::class.java)
                    //给自己发的消息
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
                CommandType.MSG_EDIT -> {
                    //聊天消息
                    val data = jsonObject.optString("data")
                    val chatMsg = GsonUtils.fromJson(data, ChatMessageBean::class.java)
                    if (!chatMsg.parentMessageId.isNullOrEmpty()) {
                        ChatDao.getChatMsgDb()
                            .queryMsgByIdBeforeDate(chatMsg.parentMessageId)
                            .let {
                                if (it != null) {
                                    it.content = chatMsg.content
                                    it.editId = chatMsg.id
                                    it.msgType = chatMsg.msgType
                                    it.operationType = chatMsg.operationType
                                    ChatUtils.saveEditMsg(
                                        it,
                                        chatMsg.from == MMKVUtils.getUser()?.id
                                    )
                                }
                            }
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
                    var groupName = ""
                    if (!jsonData.isNull("groupName"))
                        groupName = jsonData.optString("groupName")

                    "-----${createTime}---${createTime11}-----\n${jsonData.toString()}".logE()
                    //生成提示语
                    if (userId != MMKVUtils.getUser()?.id) {
                        //非自己
                        //生成会话列表数据
                        ChatDao.getConversationDb().saveGroupConversation(
                            groupIdStr,
                            "${nick}创建「${groupName}」群",
                            MsgType.MESSAGETYPE_TEXT
                        )
//                                createGroupNotice(groupIdStr, "您被${nick}邀请入群", id, createTime)
                    } else {
                        var groupInfo = ChatDao.getGroupDb().getGroupInfoById(groupIdStr)
                        if (groupInfo != null) {
                            //说明这个群是移动端操作的，不需要处理
                        } else {
                            //说明这个群是pc操作的
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
                                    operatorName = ChatUtils.getString(R.string.wo)
                                }
                                var content = when (operationType) {
                                    "Add" -> "${operatorName}${
                                        ChatUtils.getString(R.string.设置了一条置顶消息)
                                    }"

                                    "Delete" -> "${operatorName}${
                                        ChatUtils.getString(R.string.删除了一条置顶消息)
                                    }"

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
                                "${
                                    ChatUtils.getString(R.string.您把)
                                }${memberName}${ChatUtils.getString(R.string.移出了群组)}"
                            } else if (memberId == MMKVUtils.getUser()?.id) {
                                //我被别人踢出了群聊
                                LiveEventBus.get(
                                    EventKeys.EVENT_REFRESH_CONTACT_LOCAL,
                                    Boolean::class.java
                                )
                                    .post(true)
                                "${ChatUtils.getString(R.string.您被)}${name}${
                                    ChatUtils.getString(R.string.移出了群组)
                                }"
                            } else {
                                //吃瓜群众看到的提示，非自己
                                "${memberName}${
                                    ChatUtils.getString(R.string.被)
                                }${name}${ChatUtils.getString(R.string.移出了群组)}"
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
     * 获取会话列表
     */
    fun getConverList() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val sessionList = ChatRepository.getSessionList()
                val jsonObject = JSONObject(sessionList)
                val code = jsonObject.optInt("code")
                if (code == 200) {
                    val dataArray = jsonObject.optJSONArray("data")
                    val serviceChatId = mutableListOf<String>()
                    val localConverList = ChatDao.getConversationDb().getConversationListNotSystem()
                    if (dataArray != null && dataArray.length() > 0) {
                        for (i in 0 until dataArray.length()) {
                            val array = dataArray.optJSONObject(i)
                            var topMessage = array.optString("topMessage")
                            val topMessageTime = array.optLong("topMessageTime")
                            val lastMessageTime = array.optLong("lastMessageTime")
                            val unreadCount = array.optInt("unreadCount", -1)
                            val type = array.optString("type")
                            val name = array.optString("name")
                            val friendMemberId = array.optString("friendMemberId")
                            val signType = array.optString("signType", "read")
                            var messageNotice = array.optString("messageNotice", "y")
                            val groupId = array.optString("groupId")
                            val lastMessageContent = array.optString("lastMessageContent")

                            if (TextUtils.isEmpty(topMessage)) {
                                //防止后端返回其他不规范数据
                                topMessage = "N"
                            }
                            if (TextUtils.isEmpty(messageNotice)) {
                                messageNotice = "N"
                            }

                            //解析最后一条消息
                            var lastMsgContent = ""
                            var lastMsgType = ""
                            try {
                                var decodeStr = lastMessageContent.decodeContent()
                                if (!TextUtils.isEmpty(decodeStr)) {
                                    val jsonObject = JSONObject(decodeStr)
                                    val comd = jsonObject.optInt("command")
                                    if (comd == 11) {
                                        val data = jsonObject.optString("data")
                                        val lastMsg = GsonUtils.fromJson<ChatMessageBean>(
                                            data,
                                            ChatMessageBean::class.java
                                        )
                                        lastMsgContent = lastMsg.content
                                        lastMsgType = lastMsg.msgType
                                    }
                                }
                            } catch (e: Exception) {
                                Log.d("ChatWebSocketClient", "${lastMessageContent}解析异常")
                                e.printStackTrace()
                            }

                            if (type == ChatType.CHAT_TYPE_FRIEND) {
                                //好友
                                serviceChatId.add(friendMemberId)
                                ChatDao.getConversationDb()
                                    .saveFriendConversation(
                                        friendMemberId,
                                        lastMsgContent,
                                        lastMsgType,
                                        msgTime = lastMessageTime,
                                        isTop = (topMessage.lowercase()) == "y",
                                        serviceMsgCount = unreadCount,
                                        isMute = (messageNotice.lowercase()) == "n",
                                        isRead = (signType.lowercase()) == "read",
                                    )
                            } else {
                                //群聊
                                serviceChatId.add(groupId)
                                ChatDao.getConversationDb()
                                    .saveGroupConversation(
                                        groupId,
                                        lastMsgContent,
                                        lastMsgType,
                                        msgTime = lastMessageTime,
                                        isTop = (topMessage.lowercase()) == "y",
                                        serviceMsgCount = unreadCount,
                                        isMute = (messageNotice.lowercase()) == "n",
                                        isRead = (signType.lowercase()) == "read",
                                    )
                            }
                        }
                    }

                    val delData =
                        localConverList.filterNot { f -> serviceChatId.any { a -> a == f.chatId } }

                    //删除会话
                    if (delData.size > 0) {
                        delData.forEach {
                            ChatDao.getConversationDb().delConverByTargtId(it.chatId)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                    if (result?.code == 200) {
                        //保存群成员数据
//                        ChatDao.getGroupDb().updateMemByGroupId(group.chatId, result?.data)
//                        tempList.addAll(result?.data)
                        val groupMemberData = result.data
                        if (groupMemberData != null) {
                            ChatDao.getGroupDb().updateMemByGroupId(group.chatId, groupMemberData)
                            tempList.addAll(groupMemberData)
                        }
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
                            Log.d("ChatWebSocketClient", "Http收到消息<<<${str}")
                            ChatUtils.processMsg(str)
                            contentIds.add(JSONObject(str).optString("id"))
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

                    //删除所有数据
                    nofityDb.delAllNotifyMsg()

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
        ex?.message?.let { Log.d("ChatWebSocketClient", it) }
        onError?.invoke()
    }
}