package com.ym.chat.db

import android.util.Log
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.bean.NotifyBean
import com.ym.chat.service.WebsocketWork
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.CommandType
import org.json.JSONObject

/**
 * 系统通知存储 db
 */
class NotifyDb {

    /**
     * 存储单个通知消息
     * type 0
     * systemType 1 系统通知 0意见反馈
     */
    fun saveSystemNotifyMsgByJson(
        notifyMsg: JSONObject,
        systemType: Int = 0,
        msgReadState: Int,
        isGetGroupMsg: Boolean = true
    ) {
        Log.d("getSysNitice", "saveSystemNotifyMsgByJson")
        if (notifyMsg != null) {
            val id = notifyMsg.optString("id")
            val jsonData = notifyMsg.optJSONObject("data")
            val memberId = jsonData.optString("memberId")

            //新通知
            if (memberId == MMKVUtils.getUser()?.id) {
                val time = notifyMsg.optLong("createTime")
                val contentTitle = jsonData.optString("title")
                val content = jsonData.optString("content")
                val contentBriefly = jsonData.optString("contentBriefly")

                var notify = getNotifyMsgById(id)
                if (notify == null) {//只有不存在，才保存
                    var notifyBean = NotifyBean(
                        id = id,
                        title =  ChatUtils.getString(R.string.xitongtongzhi),
                        type = 0,
                        createTime = time,
                        contentTitle = contentTitle,
                        contentBriefly = contentBriefly,
                        systemType = systemType,
                        content = content,
                        msgReadState = msgReadState,
                    )
                    saveNotifyMsg(notifyBean, isGetGroupMsg)
                }
            }
        }
    }

    /**
     * 存储单个通知消息
     * 1 新设备验证码
     */
    fun saveNotifyMsgByJson(notifyMsg: JSONObject, isGetGroupMsg: Boolean = true) {
        if (notifyMsg != null) {
            val time = notifyMsg.optLong("createTime")
            val id = notifyMsg.optString("id")
            val jsonData = notifyMsg.optJSONObject("data")
            val keepTime = jsonData.optString("keepTime")
            val verfiyCode = jsonData.optString("verfiyCode")
            val deviceDescription = jsonData.optString("deviceDescription")

            var notify = getNotifyMsgById(id)
            if (notify == null) {//只有不存在，才保存
                var notifyBean = NotifyBean(
                    id = id,
                    title = ChatUtils.getString(R.string.yanzhengtongzhi),
                    type = 1,
                    createTime = time,
                    keepTime = keepTime,
                    verfiyCode = verfiyCode,
                    deviceDescription = deviceDescription,
                )
                saveNotifyMsg(notifyBean, isGetGroupMsg)
            }
        }
    }


    /**
     * 存储单个通知消息
     * 2 添加好友申请 通知消息
     */
    fun saveNotifyFriendMsg(
        id: String,
        friendMemberId: String,
        memberId: String,
        status: String,
        msgReadState: Int = 1,
        name: String,
        headerUrl: String,
        time: Long
    ) {
        var notify = getNotifyMsgById(id)
        if (notify == null) {
            var notifyBean = NotifyBean(
                id = id,
                chatType = "Friend",
                title = getTypeTitle("Friend"),
                content = ChatUtils.getString(R.string.xiwangyutachengweihaoyou),
                from = memberId,
                to = friendMemberId,
                type = 2,
                verifyType = getVerifyType(status),
                createTime = time,
                msgReadState = msgReadState,
                friendMemberName =  name,
                friendMemberHeadUrl = headerUrl
            )
            saveNotifyMsg(notifyBean, false)
        }
    }

    /**
     * 存储单个通知消息
     * 3 群成员申请入群
     */
    fun saveAddGroupMemberNotifyMsgByJson(notifyMsg: JSONObject, isGetGroupMsg: Boolean = true) {
        if (notifyMsg != null) {
            val time = notifyMsg.optLong("createTime")
            val updateTime = notifyMsg.optLong("updateTime")
            val id = notifyMsg.optString("id")
            val friendMemberId = notifyMsg.optString("friendMemberId")
            val friendMemberName = notifyMsg.optString("friendMemberName")
            val friendMemberHeadUrl = notifyMsg.optString("friendMemberHeadUrl")
            val memberId = notifyMsg.optString("memberId")
            val memberName = notifyMsg.optString("memberName")
            val status = notifyMsg.optString("status")
            val groupId = notifyMsg.optString("groupId")
            val chatType = notifyMsg.optString("type")
            val content = ""
            var type = getType(chatType)

            var notify = getNotifyMsgById(id)
            var notifyBean: NotifyBean? = null
            if (notify == null) {//如果不是重复的通知
                if (memberId != MMKVUtils.getUser()?.id) {//过滤群主自己拉成员的通知
                    notifyBean = NotifyBean(
                        id = id,
                        chatType = chatType,
                        title = getTypeTitle(chatType),
                        content = content,
                        from = memberId,
                        to = MMKVUtils.getUser()?.id,
                        type = type,
                        groupId = groupId,
                        verifyType = getVerifyType(status),
                        createTime = time,
                        updateTime = updateTime,
                        friendMemberId = friendMemberId,
                        friendMemberName = friendMemberName,
                        friendMemberHeadUrl = friendMemberHeadUrl,
                        memberId = memberId,
                        memberName = memberName,
                        status = status
                    )
                    notifyBean?.let { saveNotifyMsg(it, isGetGroupMsg) }
                }
            } else {//如果是重复的通知
                notifyBean = notify
                notifyBean?.status = status
                notifyBean?.verifyType = getVerifyType(status)
                ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
                    put(notifyBean)
                }

                //如果现在 停留在通知界面
                if (isGetGroupMsg)
                    LiveEventBus.get(EventKeys.UPDATE_NOTIFY_MSG).post(true)
            }
        }
    }

    fun getVerifyType(stateType: String): Int {
        return when (stateType.uppercase()) {
            "Accepted".uppercase() -> 1
            "Refused".uppercase() -> 2
            "Handled".uppercase() -> 3
            else -> 0
        }
    }

    private fun getType(chatType: String): Int {
        return when (chatType.uppercase()) {
            "VerfiyNotify".uppercase() -> 1
            "Friend".uppercase() -> 2
            "InviteGroup".uppercase(), "Group".uppercase() -> 3
            else -> 0
        }
    }

    fun getTypeTitle(chatType: String?): String {
        if (chatType == null) return ChatUtils.getString(R.string.xitongtongzhi)
        return when (chatType.uppercase()) {
            "VerfiyNotify".uppercase() -> ChatUtils.getString(R.string.yanzhengtongzhi)
            "Friend".uppercase() -> ChatUtils.getString(R.string.haoyoutongzhi)
            "InviteGroup".uppercase(), "Group".uppercase() -> ChatUtils.getString(R.string.qunzutongzhi)
            else -> ChatUtils.getString(R.string.xitongtongzhi)
        }
    }

    /**
     * 存储单个通知消息
     */
    fun saveNotifyMsg(notifyMsg: NotifyBean, isGetGroupMsg: Boolean = true) {
        ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            put(notifyMsg)
        }

        //更新主页 系统消息界面
        ChatDao.getConversationDb()
            .updateNotifyLastMsg(
                msgType = notifyMsg.type,
                content = getTypeTitle(notifyMsg.chatType),
                msgTime = notifyMsg.createTime
            )

        //如果现在 停留在通知界面
        if (isGetGroupMsg)
            LiveEventBus.get(EventKeys.UPDATE_NOTIFY_MSG).post(true)
    }

    /**
     * 更新单个通知消息
     */
    fun updateNotifyMsg(notifyMsg: NotifyBean) {
        ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            put(notifyMsg)
            closeThreadResources()
        }
    }

    /**
     * 获取所有 通知消息
     */
    fun getNotifyMsg(): MutableList<NotifyBean> {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().build().find()
            closeThreadResources()
            list
        }
    }

    /**
     * 获取所有 系统通知消息
     */
    fun getNotifySystemMsg(): MutableList<NotifyBean> {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.type == 0 || it.type == 1 }.build().find()
            closeThreadResources()
            list
        }
    }

    /**
     * 获取
     * 群通知消息 或者 好友请求通知消息
     */
    fun getNotifyMsgList(type: Int): MutableList<NotifyBean> {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.type == type }.build().find()
            closeThreadResources()
            list
        }
    }

    /**
     * 根据id获取单条通知消息
     */
    fun getNotifyMsgById(id: String): NotifyBean? {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.id == id }.build().find()
            closeThreadResources()
            list.firstOrNull()
        }
    }

    /**
     * 删除一条系统通知
     */
    fun delNotifyMsgById(id: String) {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.id == id }.build().find()
            closeThreadResources()
            if (list != null && list.size > 0) {
                val tempNotifyMsg = list[0]
                remove(tempNotifyMsg)
                closeThreadResources()
            }
        }
    }

    /**
     * 删除所有系统通知
     * type = 0 和 1
     */
    fun delAllNotifyMsg() {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.type == 0 || it.type == 1 }.build().find()
            closeThreadResources()
            if (list != null && list.size > 0) {
                remove(list)
                closeThreadResources()
            }
        }
    }

    /**
     * 根据类型
     * 删除系统通知
     */
    fun delAllNotifyMsg(type: Int) {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.type == type }.build().find()
            closeThreadResources()
            if (list != null && list.size > 0) {
                remove(list)
                closeThreadResources()
            }
        }
    }

    /**
     * 更新为已读/未读状态
     * state：0未读，1已读
     */
    fun updateRead(id: String, state: Int) {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.id == id }.build().find()
            list.forEach {
                it.msgReadState = state
                put(it)
            }
            //通知页面刷新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
            closeThreadResources()
        }
    }

    /**
     * 更新为已读/未读状态
     * state：0未读，1已读
     */
    fun updateNotifyRead(type: Int, state: Int) {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list = query().filter { it.type == type }.build().find()
            list.forEach {
                it.msgReadState = state
                put(it)
            }
            //通知页面刷新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
            closeThreadResources()
        }
    }

    /**
     * 校正群组通知数据已读状态
     */
    fun checkGroupData() {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {

            var listGroup =
                query().filter { it.msgReadState == 0 && (it.type == 3) }.build()
                    .find()

            val allList = mutableListOf<String>()

            //群组通知数据
            listGroup.forEach {
                if (it.verifyType != 0) {
                    it.msgReadState = 1
                    allList.add(it.id)
                    put(it)
                }
            }
            WebsocketWork.WS.sendSysNotifyReadState(allList)
            closeThreadResources()
        }
    }

    /**
     * 设置所有系统消息为已读(不包括好友和群组申请)
     */
    fun updateAllRead() {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var list =
                query().filter { it.msgReadState == 0 && (it.type == 0 || it.type == 1) }.build()
                    .find()

            val allList = mutableListOf<String>()
            list.forEach {
                it.msgReadState = 1
                allList.add(it.id)
                put(it)
            }

            WebsocketWork.WS.sendSysNotifyReadState(allList)
            closeThreadResources()
        }
    }

    /**
     * 获取所有未读消息
     */
    fun getUnReadMsgCount(): Int {
        return ChatDao.mBoxStore.boxFor(NotifyBean::class.java).run {
            var count = query().filter { it.msgReadState == 0 }.build().find()
            closeThreadResources()
            count.size
        }
    }

}