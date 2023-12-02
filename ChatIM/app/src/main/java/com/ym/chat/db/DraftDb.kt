package com.ym.chat.db

import com.ym.chat.bean.DraftBean
import io.objectbox.exception.UniqueViolationException

class DraftDb {
    /**
     * 增加草稿
     */
    fun addDraft(data: DraftBean?) {
        try {
            ChatDao.mBoxStore.boxFor(DraftBean::class.java).run {
                try {
                    put(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                closeThreadResources()
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 删除草稿
     */
//    fun deleteDraft(data: DraftBean?) {
//        try {
//            ChatDao.mBoxStore?.boxFor(DraftBean::class.java).run {
//                try {
//                    remove(data)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                closeThreadResources()
//            }
//        } catch (e: Exception) {
//        }
//    }
    fun deleteDraft(data: DraftBean?) {
        try {
            val draftBox = ChatDao.mBoxStore?.boxFor(DraftBean::class.java)
            draftBox?.run {
                try {
                    data?.let {
                        remove(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    closeThreadResources()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 修改草稿
     */
//    fun updateDraft(data: DraftBean?) {
//        try {
//            ChatDao.mBoxStore?.boxFor(DraftBean::class.java).run {
//                try {
//                    put(data)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                closeThreadResources()
//            }
//        } catch (e: Exception) {
//        }
//    }
    fun updateDraft(data: DraftBean?) {
        try {
            val draftBox = ChatDao.mBoxStore?.boxFor(DraftBean::class.java)
            draftBox?.run {
                try {
                    data?.let {
                        put(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    closeThreadResources()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 查找草稿
     */
//    fun queryDraftByChatId(chatId: String): DraftBean? {
//        try {
//            ChatDao.mBoxStore?.boxFor(DraftBean::class.java).run {
//                val data = query().filter {
//                    it.chatId == chatId
//                }.build().find().firstOrNull()
//                closeThreadResources()
//                return data
//            }
//        } catch (e: Exception) {
//            return null
//        }
//    }
    fun queryDraftByChatId(chatId: String): DraftBean? {
        try {
            val draftBox = ChatDao.mBoxStore?.boxFor(DraftBean::class.java)
            draftBox?.run {
                val data = query().filter {
                    it.chatId == chatId
                }.build().find().firstOrNull()
                closeThreadResources()
                return data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


}