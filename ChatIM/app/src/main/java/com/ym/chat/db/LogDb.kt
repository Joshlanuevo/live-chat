package com.ym.chat.db

import com.ym.chat.bean.LogBean

object LogDb {

    /**
     * 保存日志
     */
    fun saveLog(logBean: LogBean) {
        ChatDao.mBoxStore.boxFor(LogBean::class.java).run {
            try {
                put(logBean)
            } catch (e: Exception) {

            }
            closeThreadResources()
        }
    }

    /**
     * 查询日志
     */
    fun getLogList(): MutableList<LogBean> {
        return ChatDao.mBoxStore.boxFor(LogBean::class.java).run {
            val list = all
            closeThreadResources()
            list
        }
    }

    /**
     * 获取所有未上传的数据
     */
    fun getAllNoUP(): MutableList<LogBean> {
        return ChatDao.mBoxStore.boxFor(LogBean::class.java).run {
            val list = query().filter { it.upState != 1 && it.upState != 2 }.build().find()
            closeThreadResources()
            list
        }
    }

    /**
     * 更新状态
     */
    fun updateState(state: Int, id: Long) {
        return ChatDao.mBoxStore.boxFor(LogBean::class.java).run {
            try {
                put(get(id).apply {
                    upState = state
                })
            } catch (e: Exception) {

            }
        }
    }

    /**
     * 移除上传成功的
     */
    fun delAll(listData: MutableList<LogBean>) {
        return ChatDao.mBoxStore.boxFor(LogBean::class.java).run {
            try {
                remove(listData)
            } catch (e: Exception) {

            }
        }
    }
}