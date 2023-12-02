package com.ym.chat.db

import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.ConversationBean_
import com.ym.chat.bean.DelConBean
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.MsgType

/**
 * 会话表数据操作类
 */
class ConversationDb {

    private val TAG = "ConversationDb"

    /**
     * 初始化默认的系统会话列表数据，例如：我的收藏.......
     */
    fun initDefault() {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {

            //查询该数据是否存在
            val con = query().equal(ConversationBean_.type, 2)
                .and().equal(ConversationBean_.sysType, 1)
                .build().findFirst()
            //查询系统通知是否存在
            val isNotify = query().equal(ConversationBean_.type, 2)
                .and().equal(ConversationBean_.sysType, 2)
                .build().findFirst()

            if (con == null) {
                //初始化"我的收藏"
                var mineCollectConver = ConversationBean().apply {
                    name = ChatUtils.getString(R.string.wodeshoucang)
                    type = 2
                    sysType = 1
                    sysSort = 0
                    lastMsg = ChatUtils.getString(R.string.huanyingshiyong)
                }
                put(mineCollectConver)
                Log.d(TAG, ChatUtils.getString(R.string.chusihhuachenggong))
            }
            if (isNotify == null) {
                //初始化"系统通知"
                var notify = ConversationBean().apply {
                    name = ChatUtils.getString(R.string.xitongtongzhi)
                    type = 2
                    sysType = 2
                    sysSort = 1
                    lastMsg = ""
                }
                put(notify)
                Log.d(TAG, "系统通知初始化成功")
            }
            closeThreadResources()
        }
    }

    /**
     * 更新收藏最新一条消息
     */
    fun updateCollectLastMsg(
        msgType: String,
        content: String,
        msgTime: Long,
        isSendEvent: Boolean = true
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val targetConver = query().filter { it.sysType == 1 && it.type == 2 }.build().find()
            targetConver.let {
                val collectConver = it[0]
                collectConver.lastMsgType = msgType
                collectConver.lastMsg = content
                collectConver.lastTime = msgTime
                put(collectConver)
                closeThreadResources()

                if (isSendEvent) {
                    //通知页面刷新
                    LiveEventBus.get(EventKeys.UPDATE_CONVER).post(collectConver)
                }
            }
        }
    }

    /**
     * 根据chatId获取某一条会话消息
     */
    fun getCollectLastMsgByChatId(chatId: String): ConversationBean? {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val targetConver = query().filter { it.chatId == chatId }.build().find()
            closeThreadResources()
            return targetConver?.firstOrNull()
        }
    }


    /**
     * 更新通知最新一条消息
     */
    fun updateNotifyLastMsg(
        msgType: Int,
        content: String,
        msgTime: Long,
        isUpdateNotifyMsg: Boolean = false,//是否是手动修改系统消息列表
        isSendEvent: Boolean = true
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val targetConver = query().filter { it.sysType == 2 && it.type == 2 }.build().find()
            targetConver.let {
                val collectConver = it[0]
                collectConver.lastMsgType = msgType.toString()
                collectConver.lastMsg = content
                collectConver.lastTime = msgTime

                //收到消息,未读数+1
                if (!isUpdateNotifyMsg)
                    collectConver.msgCount += 1

                put(collectConver)
                closeThreadResources()

                if (isSendEvent) {
                    //通知页面刷新
                    LiveEventBus.get(EventKeys.UPDATE_CONVER).post(collectConver)
                }
            }
        }
    }

    /**
     * 存储会话数据
     */
    fun saveConversation(conversationBean: ConversationBean) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            put(conversationBean)
            closeThreadResources()
        }
    }

    /**
     * 存储好友会话数据
     */
    fun saveFriendConversation(
        targetId: String,
        lastMsgStr: String,
        lastMsgTypeStr: String,
        dir: Int = 1,
        isEdit: Boolean = false,
        msgTime: Long = -1,
        isTop: Boolean? = null,
        serviceMsgCount: Int = -1,
        isMute: Boolean? = null,
        isRead: Boolean = true,
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val targetConver = query().filter { it.chatId == targetId }.build().find()
            if (targetConver.size > 0) {
                //已经存在会话数据
                var temoConver = targetConver[0]
                temoConver.lastMsg = lastMsgStr
                temoConver.lastMsgType = lastMsgTypeStr
                if (msgTime > 0) {
                    temoConver.lastTime = msgTime
                } else {
                    temoConver.lastTime = System.currentTimeMillis()
                }
                if (serviceMsgCount >= 0) {
                    temoConver.msgCount = serviceMsgCount
                } else {
                    if (dir == 0 && !isEdit) {
                        var lastCount = temoConver.msgCount + 1
                        //收到消息,未读数+1
                        temoConver.msgCount = lastCount

                        sendConverCount(temoConver)
                    }
                }
                if (isTop != null) {
                    temoConver.isTop = isTop
                }

                isMute?.let {
                    temoConver.isMute = it
                }
                isRead?.let {
                    temoConver.isRead = isRead
                }
                put(temoConver)
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(temoConver)
            } else {
                //不存在会话数据
                var mineCollectConver = ConversationBean().apply {
//                name = friendName
//                img = friendHeadImg
                    chatId = targetId
                    type = 0
                    lastMsg = lastMsgStr
                    if (serviceMsgCount >= 0) {
                        msgCount = serviceMsgCount
                    } else {
                        if (dir == 0) {
                            //收到消息
                            msgCount = 1
                        }
                    }
                    lastMsgType = lastMsgTypeStr

                    if (msgTime > 0) {
                        lastTime = msgTime
                    } else {
                        lastTime = System.currentTimeMillis()
                    }

                }
                if (isTop != null) {
                    mineCollectConver.isTop = isTop
                }
                isMute?.let {
                    mineCollectConver.isMute = it
                }
                isRead?.let {
                    mineCollectConver.isRead = isRead
                }
                put(mineCollectConver)

                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
            }
            closeThreadResources()
        }
    }

    /**
     * 存储群聊会话数据
     */
    fun saveGroupConversation(
        targetId: String,
        lastMsgStr: String,
        lastMsgTypeStr: String,
        dir: Int = 1,
        isEdit: Boolean = false,
        fromId: String = "",
        msgTime: Long = -1,
        isTop: Boolean? = null,
        serviceMsgCount: Int = -1,
        isMute: Boolean? = null,
        isRead: Boolean? = null,
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val targetConver = query().filter { it.chatId == targetId }.build().find()
            if (targetConver.size > 0) {
                //已经存在会话数据
                var tempConver = targetConver[0]
                tempConver.lastMsg = lastMsgStr
                tempConver.lastMsgType = lastMsgTypeStr
                if (msgTime > 0) {
                    tempConver.lastTime = msgTime
                } else {
                    tempConver.lastTime = System.currentTimeMillis()
                }

                if (serviceMsgCount >= 0) {
                    tempConver.msgCount = serviceMsgCount
                } else {
                    if (dir == 0 && !isEdit) {
                        var lastCount = tempConver.msgCount + 1
                        //收到消息,未读数+1
                        tempConver.msgCount = lastCount
                        //更新会话数量
                        sendConverCount(tempConver)
                    }
                }

                if (!TextUtils.isEmpty(fromId)) {
                    tempConver.fromId = fromId
                }
                if (isTop != null) {
                    tempConver.isTop = isTop
                }
                isMute?.let {
                    tempConver.isMute = it
                }
                isRead?.let {
                    tempConver.isRead = isRead
                }
                put(tempConver)

                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempConver)
            } else {
                //不存在会话数据
                var mineCollectConver = ConversationBean().apply {
                    chatId = targetId
                    type = 1
                    lastMsg = lastMsgStr
                    lastMsgType = lastMsgTypeStr
                    if (serviceMsgCount >= 0) {
                        msgCount = serviceMsgCount
                    } else {
                        if (dir == 0) {
                            //收到消息
                            msgCount = 1
                        }
                    }
                    if (msgTime > 0) {
                        lastTime = msgTime
                    } else {
                        lastTime = System.currentTimeMillis()
                    }
                }
                if (!TextUtils.isEmpty(fromId)) {
                    mineCollectConver.fromId = fromId
                }
                if (isTop != null) {
                    mineCollectConver.isTop = isTop
                }
                isMute?.let {
                    mineCollectConver.isMute = it
                }
                isRead?.let {
                    mineCollectConver.isRead = isRead
                }
                put(mineCollectConver)

                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
            }
            closeThreadResources()
        }
    }

    /**
     *更新会话
     */
    private fun sendConverCount(bean: ConversationBean) {
        val delConBean = DelConBean().apply {
            cmd = 49
            memberId = MMKVUtils.getUser()?.id ?: ""
            operationType = "Report"
            unreadCount = bean.msgCount
        }
        if (bean.type == 0) {
            delConBean.friendMemberId = bean.chatId
            delConBean.type = ChatType.CHAT_TYPE_FRIEND
        } else if (bean.type == 1) {
            delConBean.groupId = bean.chatId
            delConBean.type = ChatType.CHAT_TYPE_GROUP
        }
//        WebsocketWork.WS.updateConver(delConBean)
    }

    /**
     * 更新会话置顶状态
     */
    fun setTopState(topState: Boolean, targetId: String) {
        return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val converList = query().filter { it.chatId == targetId }.build().find()
            if (converList != null && converList.size > 0) {
                val content = converList[0]
                content.isTop = topState
                put(content)
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(content)
            }
            closeThreadResources()
        }
    }

    /**
     * 查询会话数据
     */
    fun getConversationList(): MutableList<ConversationBean> {
        return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            //系统固定会话列表
            val sysConver = query().filter {
                it.type == 2
            }.build().find()

            //置顶会话
            var topConver = query().filter {
                it.type != 2 && it.isTop
            }.orderDesc(ConversationBean_.topTime).build().find()

            //其他会话
            var otherConver = query().filter {
                it.type != 2 && !it.isTop
            }.orderDesc(ConversationBean_.lastTime).build().find()

            closeThreadResources()

            mutableListOf<ConversationBean>().apply {
                //添加系统固定会话
                addAll(sysConver)
                //添加置顶会话
                addAll(topConver)
                //添加普通会话
                addAll(otherConver)
            }
        }
    }

    /**
     * 查询会话数据(不包含系统会话)
     */
    fun getConversationListNotSystem(): MutableList<ConversationBean> {
        return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {

            //置顶会话
            var topConver = query().filter {
                it.type != 2 && it.isTop
            }.orderDesc(ConversationBean_.topTime).build().find()

            //其他会话
            var otherConver = query().filter {
                it.type != 2 && !it.isTop
            }.orderDesc(ConversationBean_.lastTime).build().find()

            closeThreadResources()

            mutableListOf<ConversationBean>().apply {
                //添加置顶会话
                addAll(topConver)
                //添加普通会话
                addAll(otherConver)
            }
        }
    }

    /**
     * 查询所有置顶 会话数据
     */
    fun getConversationTopList(): MutableList<ConversationBean> {
        return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            //置顶会话
            var topConver = query().filter {
                it.type != 2 && it.isTop
            }.orderDesc(ConversationBean_.topTime).build().find()

            closeThreadResources()

            mutableListOf<ConversationBean>().apply {
                //添加置顶会话
                addAll(topConver)
            }
        }
    }

    /**
     * 查询没有置顶 会话数据
     */
    fun getConversationNotTopList(): MutableList<ConversationBean> {
        return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            //没有置顶会话
            var topConver = query().filter {
                it.type != 2 && !it.isTop
            }.orderDesc(ConversationBean_.topTime).build().find()

            closeThreadResources()

            mutableListOf<ConversationBean>().apply {
                //添加没有置顶会话
                addAll(topConver)
            }
        }
    }


    /**
     * 查询会话数据
     * 不要系统会话数据
     */
    fun getConversationNotSystemList(): MutableList<ConversationBean> {
        return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            //置顶会话
            var topConver = query().filter {
                it.type != 2 && it.isTop
            }.orderDesc(ConversationBean_.topTime).build().find()

            //其他会话
            var otherConver = query().filter {
                it.type != 2 && !it.isTop
            }.orderDesc(ConversationBean_.lastTime).build().find()

            closeThreadResources()

            mutableListOf<ConversationBean>().apply {
                //添加置顶会话
                addAll(topConver)
                //添加普通会话
                addAll(otherConver)
            }
        }
    }

    /**
     * 重置会话列表消息未读数
     */
    fun resetConverMsgCount(chatId: String) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val tempResult = query().filter { it.chatId == chatId }.build().find()
            if (tempResult != null && tempResult.size > 0) {
                val tempBean = tempResult[0]
                tempBean.msgCount = 0
                tempBean.isRead = true
                put(tempBean)
                closeThreadResources()

                //更新会话数量
                sendConverCount(tempBean)

                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempBean)
            }
        }
    }

    /**
     * 设置会话列表消息未读数
     */
    fun setConverMsgCount(chatId: String,count:Int) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val tempResult = query().filter { it.chatId == chatId }.build().find()
            if (tempResult != null && tempResult.size > 0) {
                val tempBean = tempResult[0]
                tempBean.msgCount = count
                put(tempBean)
                closeThreadResources()

                //更新会话数量
                sendConverCount(tempBean)

                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempBean)
            }
        }
    }

    /**
     * 重置系统通知会话列表消息未读数
     */
    fun resetNotifyConverMsgCount() {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val tempResult = query().filter { it.sysType == 2 && it.type == 2 }.build().find()
            if (tempResult != null && tempResult.size > 0) {
                val tempBean = tempResult[0]
                tempBean.msgCount = 0
                put(tempBean)
                closeThreadResources()

                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempBean)
            }
        }
    }

    /**
     * 获取系统通知会话列表消息未读数
     */
    fun getNotifyConverMsgCount(): Int {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val tempResult = query().filter { it.sysType == 2 && it.type == 2 }.build().find()
            if (tempResult != null && tempResult.size > 0) {
                val tempBean = tempResult[0]
                return tempBean.msgCount
            }
        }
        return 0
    }

    /**
     * 删除会话
     */
    fun delConver(id: Long) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val temoBean = get(id)
            remove(id)
            closeThreadResources()
            //通知页面刷新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
//            val tempResult = query().filter { it.chatId == chatId }.build().find()
//            if (tempResult != null && tempResult.size > 0) {
//                val tempBean = tempResult[0]
//                tempBean.msgCount = 0
//                put(tempBean)
//
//            }
        }
    }

    /**
     * 删除所有会话消息
     */
    fun delConver() {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            remove(all)
            closeThreadResources()
            //通知页面刷新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
        }
    }

    /**
     * 删除会话
     */
    fun delConverByTargtId(targetId: String) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val list = query().filter { it.chatId == targetId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]
                remove(tempMsg)
                closeThreadResources()
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
            }
        }
    }

    /**
     * 修改会话列表最后一条消息显示
     */
    fun updateMsgByTargtId(
        targetId: String,
        lastMsg: String,
        lastMsgType: String = MsgType.MESSAGETYPE_TEXT
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val list = query().filter { it.chatId == targetId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]
                val count = tempMsg.msgCount - 1
                tempMsg.msgCount = if (count >= 0) count else 0
                tempMsg.lastMsg = lastMsg
                tempMsg.lastMsgType = lastMsgType
                tempMsg.isRead = true
                tempMsg.fromId = ""
                put(tempMsg)
                closeThreadResources()
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempMsg)
            }
        }
    }

    /**
     * 修改会话列表最后一条草稿消息显示
     */
    fun updateDraftMsgByTargtId(
        targetId: String,
        draftContent: String
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val list = query().filter { it.chatId == targetId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]
                val count = tempMsg.msgCount - 1
                tempMsg.msgCount = if (count >= 0) count else 0
                tempMsg.draftContent = draftContent
                tempMsg.isRead = true
//                tempMsg.fromId = ""
                put(tempMsg)
                closeThreadResources()
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempMsg)
            }
        }
    }

    /**
     * 查询消息未读数
     */
    fun getConverunReadCount(): Int {
        try {
            return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
                var count = 0
                all.map {
                    if (it.type != 2) {
                        count += it.msgCount
                    }
                }
                val countSys = ChatDao.getNotifyDb().getUnReadMsgCount()

                val total = count + countSys
                total
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    /**
     * 修改会话列表
     *  是否禁音
     */
    fun updateConversationMsgMuteByTargetId(
        TargetId: String,
        isMute: Boolean,
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val list = query().filter { it.chatId == TargetId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]
                tempMsg.isMute = isMute
                put(tempMsg)
                closeThreadResources()
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempMsg)
            }
        }
    }

    /**
     * 修改会话列表
     *  消息置顶
     */
    fun updateConversationMsgByTargetId(
        TargetId: String,
        topId: String,
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val list = query().filter { it.chatId == TargetId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]
                tempMsg.isTop = true
                tempMsg.topTime = System.currentTimeMillis()
                tempMsg.topId = topId
                put(tempMsg)
                closeThreadResources()
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempMsg)
            }
        }
    }

    /**
     * 修改会话列表
     *  取消置顶
     */
    fun cancelConversationMsgByTargetId(
        TargetId: String
    ) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val list = query().filter { it.chatId == TargetId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]
                tempMsg.isTop = false
                tempMsg.topTime = 0
                tempMsg.topId = ""
                put(tempMsg)
                closeThreadResources()
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempMsg)
            }
        }
    }


    /**
     * 获取置顶消息的条数
     */
    fun getConverTopMsgCount(): Int {
        return ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val tempResult = query().filter { it.isTop }.build().find()
            if (tempResult != null && tempResult.size > 0) {
                tempResult.size
            } else {
                0
            }
        }
    }

    fun setConversationRead(TargetId: String, isRead: Boolean) {
        ChatDao.mBoxStore.boxFor(ConversationBean::class.java).run {
            val list = query().filter { it.chatId == TargetId }.build().find()
            if (list != null && list.size > 0) {
                val tempMsg = list[0]
                tempMsg.isRead = isRead
                put(tempMsg)
                closeThreadResources()
                //通知页面刷新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(tempMsg)
            }
        }
    }
}