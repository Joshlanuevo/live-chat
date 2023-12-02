package com.ym.chat.db

import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.FriendListBean
import com.ym.chat.utils.ImCache
import com.ym.chat.utils.ImCache.friendList
import io.objectbox.exception.UniqueViolationException

class FriendDb {
    /**
     * 存储单个好友
     */
    fun saveFriend(mutable: FriendListBean?) {
        ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {
            try {
                put(mutable)
            } catch (e: UniqueViolationException) {
                e.printStackTrace()
            }
            closeThreadResources()
        }
    }

    /**
     * 存储好友数据
     */
    fun saveFriendList(
        mutableList: MutableList<FriendListBean>?,
        callFinish: ((suc: Boolean) -> Unit)? = null
    ) {
        if (mutableList != null && mutableList.size > 0) {
            ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {
                removeAll()
                mutableList.forEach {
                    //重新保存最新拉回的数据
                    try {
                        put(it)
                    } catch (e: UniqueViolationException) {
                        e.printStackTrace()
                        "---存储好友重复的数据--${it.name}".logE()
                    }
                }
                callFinish?.invoke(true)
                //更新本地缓存数据
                friendList.clear()
                friendList.addAll(all)
                closeThreadResources()
            }
        }
    }

    /**
     * 通过好友id 获取好友备注
     */
    fun getFriendIdByRemark(friendId: String): String? {
        return getFriendById(friendId)?.remark
    }

    /**
     * 获取好友数据
     * 不包含系统账号的好友数据
     */
    fun getFriendList(): MutableList<FriendListBean> {
        return ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {
            var list = query().filter { it.memberLevelCode != "System" }.build().find()
            closeThreadResources()
            list
        }
    }

    /**
     *通过ID查询好友
     */
    fun getFriendById(friendId: String): FriendListBean? {
        if (friendList.size > 0) {
            val list = friendList.filter {
                it.id == friendId
            }
            return list.firstOrNull()
        } else {
            return ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {

                friendList.clear()
                friendList.addAll(all)

                val list = friendList.filter {
                    it.id == friendId
                }
                closeThreadResources()
                return list.firstOrNull()
            }
        }
    }

    /**
     *通过ID 查询是否是自己的好友
     */
    fun isFriendById(friendId: String): Boolean {
        ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {
            val result = query().filter { it.id == friendId }.build().find()
            if (result != null && result.size > 0) {
                return true
            }
            return false
        }
    }

    /**
     *通过ID 删除好友
     */
    fun delFriendById(friendId: String): Boolean {
        ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {
            val result = query().filter { it.id == friendId }.build().find()
            if (result != null && result.size > 0) {
                val tempFriend = result[0]
                remove(tempFriend)
                closeThreadResources()
                return true
            }
            closeThreadResources()
            return false
        }
    }

    /**
     * 查询是否对该好友设置消息免打扰
     */
    fun isMessageNotice(friendId: String): Boolean {
        return getFriendById(friendId)?.messageNotice == "Y"
    }

    /**
     * 更新好友免打扰状态
     */
    fun updateMessageNotice(friendId: String, value: String) {
        ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {

            //更新数据库
            val result = query().filter { it.id == friendId }.build().find().first()
            result.messageNotice = value
            put(result)

            //更新本地
            val list = friendList.filter { it.id == friendId }
            list[0].messageNotice = value
            closeThreadResources()

            //通知会话列表更新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
        }
    }

    /**
     * 更新好友备注
     */
    fun updateRemark(friendId: String, value: String) {
        ChatDao.mBoxStore.boxFor(FriendListBean::class.java).run {

            //更新数据库
            val result = query().filter { it.id == friendId }.build().find().first()
            result.remark = value
            put(result)

            //更新本地
            val list = friendList.filter { it.id == friendId }
            list[0].remark = value
            closeThreadResources()

            //通知会话列表更新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
            //通知好友列表刷新
            LiveEventBus.get(EventKeys.EVENT_REFRESH_CONTACT_LOCAL, Boolean::class.java)
                .post(true)
        }
    }
}