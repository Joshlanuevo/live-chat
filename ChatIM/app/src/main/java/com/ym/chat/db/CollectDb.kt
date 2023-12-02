package com.ym.chat.db

import com.ym.chat.bean.RecordBean

/**
 * 收藏消息存储 db
 */
class CollectDb {

    /**
     * 增加收藏消息
     */
    fun addCollectMsg(msg: RecordBean?) {
        ChatDao.mBoxStore.boxFor(RecordBean::class.java).run {
            try {
                put(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            closeThreadResources()
        }
    }

    /**
     * 删除收藏消息
     */
    fun delCollectMsg(msg: RecordBean) {
        ChatDao.mBoxStore.boxFor(RecordBean::class.java).run {
            var list = query().filter { it.createTime == msg.createTime }.build().find()
            try {
                remove(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            closeThreadResources()
        }
    }


    /**
     * 修改收藏消息
     */
    fun updateCollectMsg(msg: RecordBean?) {
        ChatDao.mBoxStore.boxFor(RecordBean::class.java).run {
            try {
                put(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            closeThreadResources()
        }
    }

    /**
     * 获取本地收藏消息
     */
    fun getCollectMsg(): MutableList<RecordBean> {
        return ChatDao.mBoxStore.boxFor(RecordBean::class.java).run {
            var list = query().build().find()
            closeThreadResources()
            list
        }
    }
}