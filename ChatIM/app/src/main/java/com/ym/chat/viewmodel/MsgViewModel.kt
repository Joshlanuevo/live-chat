package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.MsgTopBean
import com.ym.chat.bean.TopInfo
import com.ym.chat.db.ChatDao
import com.ym.chat.rxhttp.ChatRepository
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.MsgType
import org.json.JSONObject

/**
 * 会话列表Viewmodel
 */
class MsgViewModel : BaseViewModel() {

    //会话列表数据回调
    val conversationResult = MutableLiveData<MutableList<ConversationBean>>()

    //获取群置顶消息
    val getTopMsgList = MutableLiveData<LoadState<BaseBean<MutableList<MsgTopBean>>>>()

    //删除置顶消息
    val delTopMsg = MutableLiveData<LoadState<BaseBean<String>>>()

    //增加置顶消息
    val addTopMsg = MutableLiveData<LoadState<BaseBean<TopInfo>>>()

    /**
     * 从服务端拉去会话列表数据
     */
    fun getConverListFromService() {

    }

    /**
     * 获取会话列表
     */
    fun getConverList() {
        requestLifeLaunch({
            conversationResult.value = ChatDao.getConversationDb().getConversationList()
        }, {
            it.printStackTrace()
            conversationResult.value = mutableListOf()
        })
    }

    /**
     * 获取置顶消息
     */
    fun getTopInfoList() {
        requestLifeLaunch({
            val result = ChatRepository.getTopInfo()
            if (result.code == SUCCESS) {
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
//                                c.topTime = msgTopBean.createTime
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
                getConverList()//刷新数据
            }
        }, onError = {
            it.printStackTrace()
        }, onStart = {
        })
    }

    /**
     * 取消置顶消息
     */
    fun delTopInfoList(
        msgId: String,
        topMsgIdList: MutableList<String>,
        isDelTopMsg: Boolean = false//是否是删除置顶会话消息
    ) {
        requestLifeLaunch({
            val result = ChatRepository.delTopInfo(topMsgIdList)
            if (result.code == SUCCESS) {
                delTopMsg.value = LoadState.Success(result)
                //保存到本地
                ChatDao.getConversationDb().cancelConversationMsgByTargetId(msgId)
                getConverList()//刷新数据

                //如果是删除会话消息,处理删除操作
                if (isDelTopMsg) {
                    var c = ChatDao.getConversationDb().getCollectLastMsgByChatId(msgId)
                    c?.let {
                        //删除
                        ChatDao.getConversationDb().delConverByTargtId(msgId)
                    }
                }
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    delTopMsg.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    delTopMsg.value = LoadState.Fail(exc = Exception("取消置顶消息失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            delTopMsg.value = LoadState.Fail(exc = Exception("取消置顶消息失败"))
        }, onStart = {
            delTopMsg.value = LoadState.Loading()
        })
    }

    /**
     * 增加置顶消息
     */
    fun addTopInfoList(friendMemberId: String? = null, groupId: String? = null) {
        requestLifeLaunch({
            val result = ChatRepository.addTopInfoSession(friendMemberId, groupId)
            if (result.code == SUCCESS) {
                addTopMsg.value = LoadState.Success(result)
                var id = friendMemberId
                if (id.isNullOrBlank()) {
                    id = groupId
                }
                id?.let {
                    result.data?.let { t ->
                        if (id == groupId || id == friendMemberId) {
                            //保存包本地
                            t.id?.let { it1 ->
                                ChatDao.getConversationDb().updateConversationMsgByTargetId(
                                    id,
                                    it1
                                )
                            }
                            getConverList()//刷新数据
                        }
                    }
                }
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    "$errorInfo".toast()
                    addTopMsg.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    addTopMsg.value = LoadState.Fail(exc = Exception("添加置顶消息失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            addTopMsg.value = LoadState.Fail(exc = Exception("添加置顶消息失败"))
        }, onStart = {
            addTopMsg.value = LoadState.Loading()
        })
    }
}