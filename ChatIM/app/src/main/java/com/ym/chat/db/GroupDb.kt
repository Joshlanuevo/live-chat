package com.ym.chat.db

import android.os.Build
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.utils.ImCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupDb {
    /**
     * 存储一个群组信息
     */
    fun saveGroup(mutable: GroupInfoBean?) {
        ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
            mutable?.let {
                put(it)
                ImCache.groupList.add(it)
            }
            closeThreadResources()
        }
    }

    /**
     * 删除群数据
     */
    fun deleteGroup(groupId: String) {
        try {
            ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
                val groupResult = query().filter { it.id == groupId }.build().find()
                if (groupResult != null && groupResult.size > 0) {

                    //删除本地
                    val listBean = ImCache.groupList.filter { it.id == groupId }[0]
                    ImCache.groupList.remove(listBean)

                    //删除数据库
                    remove(groupResult.first().dbId)

                    closeThreadResources()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 存储群组列表数据
     */
    fun saveGroupList(
        mutableList: MutableList<GroupInfoBean>?,
        callFinish: ((suc: Boolean) -> Unit)? = null
    ) {
        if (mutableList != null && mutableList.size > 0) {
            ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
                removeAll()
                //重新保存最新拉回的数据
                put(mutableList)

                ImCache.groupList.clear()
                ImCache.groupList.addAll(mutableList)

                closeThreadResources()
                callFinish?.invoke(true)
            }
        }
    }

    /**
     * 通过群ID修改群名称
     */
    fun updateNoticeById(groupId: String, notice: String) {
        ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
            val groupResult = query().filter { it.id == groupId }.build().find()
            if (groupResult != null && groupResult.size > 0) {
                val group = groupResult[0]
                group.notice = notice
                put(group)
                closeThreadResources()

                //更新本地缓存数据
                val list = ImCache.groupList.filter { it.id == groupId }
                list[0].notice = notice

                //通知页面更新
                LiveEventBus.get(EventKeys.EDIT_GROUP_INFO, GroupInfoBean::class.java).post(group)

                //通知会话列表更新
//                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
            }
        }
    }

    /**
     * 通过群ID修改群名称
     */
    fun updateNameById(groupId: String, newGroupName: String) {
        ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
            val groupResult = query().filter { it.id == groupId }.build().find()
            if (groupResult != null && groupResult.size > 0) {
                val group = groupResult[0]
                group.name = newGroupName
                put(group)
                closeThreadResources()

                //更新本地缓存数据
                val list = ImCache.groupList.filter { it.id == groupId }
                list[0].name = newGroupName

                //通知页面更新
                LiveEventBus.get(EventKeys.EDIT_GROUP_INFO, GroupInfoBean::class.java).post(group)

                //通知会话列表更新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
            }
        }
    }

    /**
     * 通过群ID修改群头像
     */
    fun updateIconById(groupId: String, iconUrl: String) {
        ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
            val groupResult = query().filter { it.id == groupId }.build().find()
            if (groupResult != null && groupResult.size > 0) {
                val group = groupResult[0]
                group.headUrl = iconUrl
                put(group)
                closeThreadResources()

                //更新本地缓存数据
                val list = ImCache.groupList.filter { it.id == groupId }
                list[0].headUrl = iconUrl

                //通知页面更新
                LiveEventBus.get(EventKeys.EDIT_GROUP_INFO, GroupInfoBean::class.java).post(group)

                //通知会话列表更新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
            }
        }
    }

    /**
     * 获取群组数据
     */
    fun getGroupList(): MutableList<GroupInfoBean> {
        try {
            return ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
                var list = query().build().find()
                closeThreadResources()
                list
            }
        }catch (e:Exception){
            e.printStackTrace()
            return mutableListOf<GroupInfoBean>()
        }
    }

    /**
     *通过ID查询群组
     */
    fun getGroupInfoById(groupId: String): GroupInfoBean? {
        if (ImCache.groupList.size > 0) {
            //本地查询
            return ImCache.groupList.firstOrNull { it.id == groupId }
        } else {
            //数据库查
            return ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {

                ImCache.groupList.clear()
                ImCache.groupList.addAll(all)

                return ImCache.groupList.firstOrNull { it.id == groupId }
            }
        }
    }

    /**
     *通过群ID查询 本人在这个群的角色
     */
    fun getGroupRoleInfoById(groupId: String): String {
        var group = getGroupInfoById(groupId)
        return group?.roleType ?: ""
    }

    /**
     *通过群ID查询 指定成员的角色
     */
    fun getGroupRoleInfoById(groupId: String, memberId: String): String {
        var group = getMemberInGroup(memberId,groupId)
        return group?.role ?: ""
    }

    /**
     * 查询是否对该好友设置消息免打扰
     */
    fun isMessageNotice(groupId: String): Boolean {
        return getGroupInfoById(groupId)?.messageNotice == "N"
    }

    /**
     * 根据群id获取该群所有成员数据
     */
    fun getMemberByGroupId(groupId: String): MutableList<GroupMemberBean> {
        ChatDao.mBoxStore.boxFor(GroupMemberBean::class.java).run {
            //本地缓存数据查
            return query().filter { it.groupId == groupId }.build().find().toMutableList()
        }
    }

    /**
     * 根据群id删除所有群成员数据
     */
    fun updateMemByGroupId(groupId: String, mutableList: MutableList<GroupMemberBean>) {
        //更新缓存数据
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ImCache.groupMemberList.removeIf {
                it.groupId==groupId
            }
            ImCache.groupMemberList.addAll(mutableList)
        }
//        ChatDao.mBoxStore.boxFor(GroupMemberBean::class.java).run {
//            val list = query().filter { it.groupId == groupId }.build().find()
//            remove(list)
//            put(mutableList)
//
////            //更新缓存数据
////            ImCache.groupMemberList.clear()
////            ImCache.groupMemberList.addAll(all)
////            closeThreadResources()
//        }
    }

    /**
     * 存储群成员数据
     */
    fun saveGroupMemberList(
        mutableList: MutableList<GroupMemberBean>?,
        groupId: String?,
        callFinish: ((suc: Boolean) -> Unit)? = null
    ) {
        mutableList?.forEach {
            it.groupId = groupId?:""
        }
        if (mutableList != null && mutableList.size > 0) {
            //加一个同步锁
            ChatDao.mBoxStore.boxFor(GroupMemberBean::class.java).run {
                val list = query().filter { it.groupId == groupId }.build().find()
                remove(list)
                //重新保存最新拉回的数据
                put(mutableList)
                ImCache.groupMemberList.clear()
                ImCache.groupMemberList.addAll(all)
                closeThreadResources()
                callFinish?.invoke(true)
            }
        }
    }

    /**
     *通过ID查询群成员
     */
    fun getMemberById(id: String): GroupMemberBean? {
        if (ImCache.groupMemberList.size > 0) {
            //本地缓存数据查
            return ImCache.groupMemberList.lastOrNull {
                it.id == id
            }
        } else {
            //数据库查
            return ChatDao.mBoxStore.boxFor(GroupMemberBean::class.java).run {
                val result = query().filter { it.id == id }.build().find()
                ImCache.groupMemberList.clear()
                ImCache.groupMemberList.addAll(all)
                closeThreadResources()
                return result.lastOrNull()
            }
        }
    }

    /**
     *查询指定群组，指定成员
     */
    fun getMemberInGroup(id: String, groupId: String): GroupMemberBean? {
        if (ImCache.groupMemberList.size > 0) {
            //本地缓存数据查
            return ImCache.groupMemberList.firstOrNull {
                it.id == id && it.groupId == groupId
            }
        } else
        //数据库查
            return ChatDao.mBoxStore.boxFor(GroupMemberBean::class.java).run {
                val result = query().filter { it.id == id && it.groupId == groupId }.build().find()
                ImCache.groupMemberList.clear()
                ImCache.groupMemberList.addAll(all)
                closeThreadResources()
                return result.firstOrNull()
            }
    }


    /**
     * 更新群禁言状态
     */
    fun updateGroupMute(groupId: String, state: String) {
        return ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {
            val result = query().filter { it.id == groupId }.build().find()
            if (result != null && result.size > 0) {
                val group = result[0]
                //更新数据库禁言状态
                group.allowSpeak = state
                put(group)

                //更新本地缓存数据
                val list = ImCache.groupList.filter { it.id == groupId }
                list[0].allowSpeak = state

                closeThreadResources()
                //通知会话列表更新
                LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
            }
        }
    }

    /**
     * 更新好友免打扰状态
     */
    fun updateMessageNotice(groupId: String, value: String) {
        ChatDao.mBoxStore.boxFor(GroupInfoBean::class.java).run {

            //更新数据库
            val result = query().filter { it.id == groupId }.build().find().first()
            result.messageNotice = value
            put(result)

            //更新本地
            val list = ImCache.groupList.filter { it.id == groupId }
            list[0].messageNotice = value

            closeThreadResources()
            //通知会话列表更新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(ConversationBean())
        }
    }

    /**
     * 更新群成员 是否禁言
     */
    fun updateGroupMemberMute(groupId: String, memberId: String, value: String) {
        ChatDao.mBoxStore.boxFor(GroupMemberBean::class.java).run {
            val list =
                query().filter { it.groupId == groupId && it.memberId == memberId }.build().find()
            if (list != null && list.size > 0) {
                list[0].allowSpeak = value
                put(list[0])
            }
            //更新缓存数据
            ImCache.groupMemberList.clear()
            ImCache.groupMemberList.addAll(all)
            closeThreadResources()
        }
    }

}