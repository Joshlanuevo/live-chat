package com.ym.chat.db

import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.launchError
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.rxhttp.FriendRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.ImCache
import com.ym.chat.utils.LogHelp
import com.ym.chat.utils.StringExt.decodeContent
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import io.objectbox.annotation.Index
import kotlinx.coroutines.*
import org.json.JSONObject

/**
 * 数据库操作类
 */
object ChatDao {
    lateinit var mBoxStore: BoxStore
        private set

    /**
     * 初始化数据库
     */
    @SuppressLint("MissingPermission")
    fun initDb(userName: String, callFinish: ((suc: Boolean) -> Unit)? = null) {
        if (this::mBoxStore.isInitialized) {
            //如果已经被初始化了，把上一次close
            mBoxStore.close()
        }

        launchError(handler = { _, e ->
            "数据库初始化异常:${e.message}".logE()
            if (callFinish != null) {
                MMKVUtils.clearUserInfo()//登录时数据库异常，则清除登录数据
                callFinish.invoke(false)
            }
        }) {
            withContext(Dispatchers.IO) {
                MyObjectBox.builder().androidContext(Utils.getApp())
                    .name(userName + ApiUrl.currentType).build()
            }.let { tempBox ->
                mBoxStore = tempBox

                //初始化系统会话列表数据
                getConversationDb().initDefault()

                callFinish?.invoke(true)

                //开启浏览服务
                AndroidObjectBrowser(tempBox).start(Utils.getApp())

                //加载群数据，和好友数据
                syncFriendAndGroupToLocal()
            }
        }
    }

    /**
     * 获取会话数据操作数据类
     */
    fun getConversationDb(): ConversationDb {
        return ConversationDb()
    }


    /**
     * 获取会话数据操作数据类
     */
    fun getChatMsgDb(): ChatMsgDb {
        return ChatMsgDb()
    }

    /**
     * 获取好友数据操作数据类
     */
    fun getFriendDb(): FriendDb {
        return FriendDb()
    }

    /**
     * 获取群组数据操作数据类
     */
    fun getGroupDb(): GroupDb {
        return GroupDb()
    }

    /**
     * 获取系统通知数据操作数据类
     */
    fun getNotifyDb(): NotifyDb {
        return NotifyDb()
    }

    /**
     * 获取草稿数据库操作类
     */
    fun getDraftDb(): DraftDb {
        return DraftDb()
    }

    /**
     * 获取收藏数据库操作类
     */
    fun getCollectDb(): CollectDb {
        return CollectDb()
    }

    /**
     * 获取置顶数据库操作类
     */
    fun getMsgTopDb(): MsgTopDb {
        return MsgTopDb()
    }

    private var isSyncIng = false
    private var completeCount = 0

    /**
     * 同步本地好友数据、群组数据
     * isSyncFriend:是否同步好友数据
     * isSyncGroup:是否同步群组数据
     * isEventUpdateConver:是否发送事件，更新会话列表
     */
    fun syncFriendAndGroupToLocal(
        isSyncFriend: Boolean = true,
        isSyncGroup: Boolean = true,
        isEventUpdateConver: Boolean = false,
        complete: ((suc: Boolean) -> Unit)? = null,
    ) {
        if (isSyncIng) return

        GlobalScope.launch(Dispatchers.IO) {
            isSyncIng = true
            //开始同步好友数据
            if (isSyncFriend) {
                try {
                    val result = FriendRepository.getFriendList(MMKVUtils.getUser()?.id ?: "")
                    if (result.code == 200 && result.data.isNotEmpty()) {
                        getFriendDb().saveFriendList(result.data)

                        if(isEventUpdateConver){
                            //更新会话列表
                            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
                        }
                    }

                    if (!isSyncGroup) {
                        isSyncIng = false
                        LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                            .post(false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    //同步服务器好友异常
                    if (!isSyncGroup) {
                        isSyncIng = false
                        LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                            .post(false)
                    }
                }
            }

            if (isSyncGroup) {
                //开始同步群数据
                try {
                    val result = GroupRepository.getMyAllGroup()
                    if (result.code == 200) {

                        var groupInfoBeanList = mutableListOf<GroupInfoBean>()
                        result.data?.forEach {
                            groupInfoBeanList.add(groupInfoBeanList(it))
                        }
                        //持久化到数据库
                        getGroupDb().saveGroupList(groupInfoBeanList)

                        if(isEventUpdateConver){
                            //更新会话列表
                            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
                        }

                        //更新群组免打扰状态
                        val comparisonResult = result.data?.size?.compareTo(0) ?: 0
                        if (comparisonResult > 0) {
                            //更新群成员数据到本地
                            isSyncIng = false
                            LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                                .post(true)
//                            completeCount = 0
//                            result.data.forEach { group ->
//                                //获取群数据
//                                getGroupMem(group, complete,result.data)
//                            }
                        } else {
                            isSyncIng = false
                            LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                                .post(false)
                        }
                    } else {
                        isSyncIng = false
                        LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                            .post(false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isSyncIng = false
                    LogHelp.d(
                        "数据同步",
                        "异常：$e"
                    )
                    LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                        .post(false)
                }
            }
        }
    }

    private fun groupInfoBeanList(it: NewGroupInfoBean): GroupInfoBean {
        return GroupInfoBean(
            allowSpeak = it.allowSpeak ?: "",
            autoSign = it.autoSign ?: "",
            code = it.code ?: "",
            description = it.description ?: "",
            destroyAfterRead = it.destroyAfterRead ?: "",
            destroyMessage = it.destroyMessage ?: "",
            groupFileSize = it.groupFileSize ?: "",
            createTime = it.createTime ?: "",
            updateTime = it.updateTime ?: "",
            headUrl = it.headUrl ?: "",
            id = it.id ?: "",
            leaveNoticeAdmin = it.leaveNoticeAdmin ?: "",
            lookGroupInfo = it.lookGroupInfo ?: "",
            lookMember = it.lookMember ?: "",
            memberDisplay = it.memberDisplay ?: "",
            messageStoreDays = it.messageStoreDays ?: "",
            modifyGroupNickname = it.modifyGroupNickname ?: "",
            name = it.name ?: "",
            newOwnerId = it.newOwnerId ?: "",
            notice = it.notice ?: "",
            ownerId = it.ownerId ?: "",
            publicSignInfo = it.publicSignInfo ?: "",
            screenshot = it.screenshot ?: "",
            sendBlackboardNews = it.sendBlackboardNews ?: "",
            sendVisitingCard = it.sendVisitingCard ?: "",
            signActivity = it.signActivity ?: "",
            signBeginDate = it.signBeginDate ?: "",
            signWords = it.signWords ?: "",
            speechFrequency = it.speechFrequency ?: "",
            status = it.status ?: "",
            roleType = it.groupMemberVO?.role ?: "",
            messageNotice = it.groupMemberVO?.messageNotice ?: "",
            memberAllowSpeak = it.groupMemberVO?.allowSpeak ?: ""
        )
    }

    /**
     *查询群成员列表
     */
    fun getGroupMemberList(groupId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = GroupRepository.getGroupMemberList(groupId)
                if (result.code == 200) {
                    //存储群成员到本地数据库
                    getGroupDb().saveGroupMemberList(result.data, groupId)
                    //通知页面刷新
                    LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var start: Long = 0L

    suspend fun getGroupMem(
        group: GroupInfoBean, complete: ((suc: Boolean) -> Unit)? = null,
        mutableList: MutableList<GroupInfoBean>
    ) {
        try {
            val groupMemResult = GroupRepository.getGroupMemberList(group.id)
            completeCount++
            if (groupMemResult?.data != null && groupMemResult?.data.size > 0) {
                getGroupDb().saveGroupMemberList(groupMemResult?.data, group.id)
                val list = groupMemResult.data
                if (list != null && list.size > 0) {
                    list.filter { it.id == MMKVUtils.getUser()?.id }.firstNotNullOfOrNull { g ->
                        //更新群组的免打扰状态
                        mBoxStore.boxFor(GroupInfoBean::class.java).run {
                            //更新数据库
                            query().filter { it.id == group.id }.build().find()
                                .firstNotNullOfOrNull { it1 ->
                                    it1.messageNotice = g.messageNotice
                                    it1.roleType = g.role
                                    put(it1)
                                }
                            closeThreadResources()
                        }

                        //更新本地
                        ImCache.groupList.filter { it.id == group.id }
                            .firstNotNullOfOrNull { group ->
                                group.messageNotice = g.messageNotice
                                group.roleType = g.role
                            }
                    }
                }
            } else {
                LogHelp.e(
                    "数据同步",
                    "没有群成员数据，群名：${group.name}，群ID:${group.id}"
                )
            }
            if (completeCount >= mutableList.size) {
                isSyncIng = false
                //完成数据同步
                LiveEventBus.get(
                    EventKeys.EVENT_REFRESH_CONTACT_LOCAL,
                    Boolean::class.java
                ).post(true)
                //通知会话列表更新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
                complete?.invoke(true)
                val end = System.currentTimeMillis()
                var diff = end - start
                LogHelp.d("数据同步", "已完成。耗时:${diff}ms")
            }
        } catch (e: Exception) {
            completeCount++
            LogHelp.d("数据同步", "count==${completeCount},total==${mutableList.size}==$e")
            if (completeCount >= mutableList.size) {
                isSyncIng = false
                //完成数据同步
                LiveEventBus.get(
                    EventKeys.EVENT_REFRESH_CONTACT_LOCAL,
                    Boolean::class.java
                ).post(true)
                //通知会话列表更新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
                LogHelp.d("数据同步", "已完成")
            }
            e.printStackTrace()
        }
    }

    /**
     * 获取会话列表
     */
    fun getConverList() {
//        LiveEventBus.get(EventKeys.UPDATE_CONVER_START).post(null)
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
                            val friendMemberId = array.optString("friendMemberId")
                            val signType = array.optString("signType", "read")
                            var messageNotice = array.optString("messageNotice", "y")
                            val groupId = array.optString("groupId")
                            val lastMessageContent = array.optString("lastMessageContent")
                            val serViceName = array.optString("name")
                            val headUrl = array.optString("headUrl")

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
                                        if (ChatUtils.msgContentHasKeyWork(lastMsg.content)) {
                                            lastMsgContent = ""
                                        } else {
                                            lastMsgContent = lastMsg.content
                                        }
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
                                        nameStr = serViceName,
                                        imgStr = headUrl, isUpdateUI = false
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
                                        nameStr = serViceName,
                                        imgStr = headUrl, isUpdateUI = false
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

                    getAtIds()
                }
            } catch (e: Exception) {
                LiveEventBus.get(EventKeys.UPDATE_CONVER_END).post(null)
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取@数据
     */
    fun getAtIds() {
        GlobalScope.launch(Dispatchers.IO) {
            //@消息数据
            try {
                val tempResult = ChatRepository.getAtMsgList()
                ImCache.atConverMsgList.clear()
                ImCache.atConverMsgList.addAll(tempResult.data)
                LiveEventBus.get(EventKeys.UPDATE_CONVER_END).post(null)
            } catch (e: Exception) {
                e.printStackTrace()
                LiveEventBus.get(EventKeys.UPDATE_CONVER_END).post(null)
            }
        }
    }
}