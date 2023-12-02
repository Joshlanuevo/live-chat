package com.ym.chat.db

import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.MsgTopBean

class MsgTopDb {

    /**
     * 增加置顶消息
     */
    fun addMsgTop(msgTop: MsgTopBean?) {
        ChatDao.mBoxStore.boxFor(MsgTopBean::class.java).run {
            try {
                put(msgTop)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            closeThreadResources()
        }
    }

    /**
     * 删除置顶消息
     */
    fun delMsgTop(msgTop: MsgTopBean?) {
        ChatDao.mBoxStore.boxFor(MsgTopBean::class.java).run {
            try {
                remove(msgTop)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            closeThreadResources()
        }
    }


    /**
     * 修改置顶状态
     */
    fun updateMsgTop(msgTop: MsgTopBean?) {
        ChatDao.mBoxStore.boxFor(MsgTopBean::class.java).run {
            try {
                put(msgTop)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            closeThreadResources()
        }
    }


    /**
     * 查找置顶消息
     */
    fun queryMsgTopById(id: String): MsgTopBean? {
        ChatDao.mBoxStore.boxFor(MsgTopBean::class.java).run {
            val data = query().filter {
                it.id == id
            }.build().find().firstOrNull()
            closeThreadResources()
            return data
        }
    }

    /**
     * 查找单个群所有置顶消息
     */
    fun queryMsgTopByGroupId(groupId: String): MutableList<MsgTopBean>? {
        ChatDao.mBoxStore.boxFor(MsgTopBean::class.java).run {
            val data = query().filter {
                it.groupId == groupId
            }.build().find()
//            data?.let {
//                //根据置顶时间排序
//                it.sortBy { m -> m.topTime }
//            }
            closeThreadResources()
            return data
        }
    }
}