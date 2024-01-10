package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.chad.library.adapter.base.entity.node.BaseNode
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.enum.AskStatus
import com.ym.chat.enum.AskType
import com.ym.chat.rxhttp.FriendRepository
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.utils.PatternUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 好友相关
 *
 * 获取请求处理
 */
class FriendViewModel : BaseViewModel() {


    //页面显示数据
    val orginData = mutableListOf<BaseNode>()
    val showData = MutableLiveData<MutableList<BaseNode>>()

    //好友列表
    val friendListResult = MutableLiveData<LoadState<MutableList<FriendListBean>>>()

    //搜索结果
    val searchResult = MutableLiveData<LoadState<FriendInfoBean>>()

    //申请添加好友
    val applyAddFriendResult = MutableLiveData<LoadState<SimpleBean>>()

    //扫二维码添加好友
    val addFriendByEncode = MutableLiveData<LoadState<BaseBean<FriendQRCodeBean>>>()

    //删除好友
    val deleteFriend = MutableLiveData<LoadState<Boolean>>()

    //修改好友备注/设置消息免打扰/加入黑名单
    val modifyFriendStatus = MutableLiveData<LoadState<BaseBean<String>>>()

    //获取申请入好友  入群列表
    val askList = MutableLiveData<LoadState<BaseBean<AskFriendInfoBean>>>()

    //远程销毁好友消息
    val deleteRemoteFriendMessage = MutableLiveData<LoadState<SimpleBean>>()

    val putGroupMemberMuteLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()

    /**
     * 获取好友页面数据
     */
    fun getList() {
        requestLifeLaunch({

            orginData.clear()

            //我管理的群组
            val list = FriendGroupNode()
            list.title = ActivityUtils.getTopActivity().getString(R.string.我管理的群组)
            list.type = 1
            list.size = 0
            list.isExpanded = false
            list.childList = mutableListOf()
            orginData.add(list)

            //我加入的群组
            val list1 = FriendGroupNode()
            list1.title = ActivityUtils.getTopActivity().getString(R.string.我加入的群组)
            list1.type = 2
            list1.isExpanded = false
            list1.size = 0
            list1.childList = mutableListOf()
            orginData.add(list1)

            //好友列表数据
            val list2 = FriendGroupNode()
            list2.title = ActivityUtils.getTopActivity().getString(R.string.联系人)
            list2.type = 3
            list2.isExpanded = false
            orginData.add(list2)

            var groupList = ChatDao.getGroupDb().getGroupList()
            var userId = MMKVUtils.getUser()?.id ?: ""
            if (groupList != null && groupList.size > 0) {
                var localSortList = englishSortGroup(groupList)
                localSortList.forEach { g ->
                    if (g.ownerId == userId || g.roleType.uppercase() == "admin".uppercase()
                        || g.roleType.uppercase() == "owner".uppercase()
                    ) {
                        //我管理的群
                        list.childList.add(g)
                    } else {
                        //我创建的群
                        list1.childList.add(g)
                    }
                }
                list.size = list.childList.size
                list1.size = list1.childList.size
            }

            val result = ChatDao.getFriendDb().getFriendList()
            if (result != null && result.size > 0) {
                var localSortList = englishSort(result)
                list2.size = localSortList.size
                list2.childList = localSortList.toMutableList()
            }
            showData.value = orginData
        }, onError = {
            it.printStackTrace()
            //显示本地数据
            showLocalData()
        })
    }

    /**
     * 显示本地数据
     */
    private fun showLocalData() {

        orginData.clear()

        //我管理的群组
        val list = FriendGroupNode()
        list.title = ActivityUtils.getTopActivity().getString(R.string.我管理的群组) // "我管理的群组"
        list.type = 1
        list.size = 0
        list.isExpanded = false
        list.childList = mutableListOf()
        orginData.add(list)

        //我加入的群组
        val list1 = FriendGroupNode()
        list1.title = ActivityUtils.getTopActivity().getString(R.string.我加入的群组) // "我加入的群组"
        list1.type = 2
        list1.isExpanded = false
        list1.size = 0
        list1.childList = mutableListOf()
        orginData.add(list1)

        //好友列表数据
        val list2 = FriendGroupNode()
        list2.title = ActivityUtils.getTopActivity().getString(R.string.联系人) // "联系人"
        list2.type = 3
        list2.isExpanded = false
        orginData.add(list2)

        //显示本地数据
        var localList = ChatDao.getGroupDb().getGroupList()
        var localSortList = englishSortGroup(localList)
        localSortList.forEach { g ->
            if (g.ownerId == MMKVUtils.getUser()?.id ?: "") {
                //我管理的群
                list.childList.add(g)
            } else {
                //我创建的群
                list1.childList.add(g)
            }
        }
        list.size = list.childList.size
        list1.size = list1.childList.size

        //显示本地数据
        var localList1 = ChatDao.getFriendDb().getFriendList()
        var localSortList1 = englishSort(localList1)
        list2.size = localSortList1.size
        list2.childList = localSortList1.toMutableList()

        showData.value = orginData
    }

    /**
     * 中英文取首字母 按26字母排序
     * 其他数字 符合 放最后
     */
    private fun englishSort(list: MutableList<FriendListBean>): MutableList<FriendListBean> {
        val resultList = mutableListOf<FriendListBean>()
        if (list.isNotEmpty()) {
            list.sortBy { t -> t.fullChar }
            list.sortBy { t -> t.firstChar }
        }
        //非英文的放最后
        val otherList = list.filterNot { ff -> PatternUtils.isEnglish(ff.firstChar) }
        resultList.addAll(otherList)
        if (!otherList.isNullOrEmpty()) {
            resultList.sortBy { t -> t.createTimestamp }//按注册时间排序
        }
        //有英文或者汉字的名字
        val englishList = list.filter { ff -> PatternUtils.isEnglish(ff.firstChar) }
        resultList.addAll(0, englishList)
        return resultList
    }

    /**
     * 中英文取首字母 按26字母排序
     * 其他数字 符合 放最后
     */
    private fun englishSortGroup(list: MutableList<GroupInfoBean>): MutableList<GroupInfoBean> {
        val resultList = mutableListOf<GroupInfoBean>()
        if (list.isNotEmpty()) {
            list.sortBy { t -> t.fullChar }
            list.sortBy { t -> t.firstChar }
        }
        //非英文的放最后
        val otherList = list.filterNot { ff -> PatternUtils.isEnglish(ff.firstChar) }
        resultList.addAll(otherList)
        if (!otherList.isNullOrEmpty()) {
            resultList.sortBy { t -> t.createTimestamp }//按注册时间排序
        }
        //有英文或者汉字的名字
        val englishList = list.filter { ff -> PatternUtils.isEnglish(ff.firstChar) }
        resultList.addAll(0, englishList)
        return resultList
    }

    /**
     * 获取好友列表
     */
    fun getFriendList(isFriendInfoView: Boolean = false, friendMemberId: String = "") {
        if (friendListResult.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = FriendRepository.getFriendList(MMKVUtils.getUser()?.id ?: "")
            if (result.code == SUCCESS) {
                withContext(Dispatchers.IO) {
                    //持久化到数据库
                    ChatDao.getFriendDb().saveFriendList(result.data) {
                        LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                            .post(true)
                        if (isFriendInfoView) {
                            LiveEventBus.get(EventKeys.EDIT_FRIEND_NOTICE, String::class.java)
                                .post(friendMemberId)
                            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
                        }
                    }
                }.let {
                    friendListResult.value = LoadState.Success(result.data)
                }
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    friendListResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    friendListResult.value = LoadState.Fail(exc = Exception("查询失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            friendListResult.value = LoadState.Fail(exc = Exception("查询失败"))
        })

    }

    /**
     * 搜索好友
     */
    fun searchFriend(keyWord: String) {
        requestLifeLaunch({
            val result = FriendRepository.searchFriend(keyWord)
            if (result.code == SUCCESS) {
                searchResult.value = LoadState.Success(result.data)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    searchResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    searchResult.value = LoadState.Fail(exc = Exception("查询失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            searchResult.value = LoadState.Fail(exc = Exception("查询失败"))
        }, onStart = {
            searchResult.value = LoadState.Loading()
        })
    }

    /**
     * 添加好友
     */
    fun applyAddFriend(selfMemberId: String, friendMemberId: String) {
        requestLifeLaunch({
            val result = FriendRepository.applyAddFriend(selfMemberId, friendMemberId);
            if (result.code == SUCCESS) {
                applyAddFriendResult.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    applyAddFriendResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    applyAddFriendResult.value = LoadState.Fail(exc = Exception("添加失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            applyAddFriendResult.value = LoadState.Fail(exc = Exception("添加失败"))
        }, onStart = {
            applyAddFriendResult.value = LoadState.Loading()
        })
    }

    /**
     * 扫二维码 直接添加好友
     */
    fun addFriendByEncode(encode: String) {
        requestLifeLaunch({
            val result = FriendRepository.addFriendByEncode(encode)
            if (result.code == SUCCESS) {
                addFriendByEncode.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    addFriendByEncode.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    addFriendByEncode.value = LoadState.Fail(exc = Exception("添加失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            addFriendByEncode.value = LoadState.Fail(exc = Exception("添加失败"))
        }, onStart = {
            addFriendByEncode.value = LoadState.Loading()
        })
    }

    /**
     * 获取申请列表
     */
    fun askFriendList(type: AskType = AskType.friend) {
        requestLifeLaunch({
            val result = FriendRepository.getAskFriendList(type)
            if (result.code == SUCCESS) {
                askList.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    askList.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    askList.value = LoadState.Fail(exc = Exception("获取数据异常"))
                }
            }
        }, onError = {
            it.printStackTrace()
            askList.value = LoadState.Fail(exc = Exception("获取数据异常"))
        }, onStart = {
            askList.value = LoadState.Loading()
        })
    }

    /**
     * 删除好友
     */
    fun deleteFriend(friendMemberId: String) {
        requestLifeLaunch({
            val result = FriendRepository.deleteFriend(friendMemberId);
            if (result.code == SUCCESS) {
                deleteFriend.value = LoadState.Success(true)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    deleteFriend.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    deleteFriend.value = LoadState.Fail(exc = Exception("删除好友失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            deleteFriend.value = LoadState.Fail(exc = Exception("删除好友失败"))
        }, onStart = {
            deleteFriend.value = LoadState.Loading()
        })
    }

    /**
     * 同意和拒绝好友申请
     * @status 状态:待处理 pending,已接受 accepted,已拒绝 refused
     */
    fun modifyFriend(status: AskStatus, friendMemberId: String, askId: String) {
        requestLifeLaunch({
            val result = FriendRepository.modifyFriend(status, friendMemberId, askId)
            if (result.code == SUCCESS) {
                applyAddFriendResult.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    applyAddFriendResult.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    applyAddFriendResult.value = LoadState.Fail(exc = Exception("操作失败"))
                }
            }
        }, onError = {
            it.printStackTrace()
            applyAddFriendResult.value = LoadState.Fail(exc = Exception("操作失败"))
        }, onStart = {
            applyAddFriendResult.value = LoadState.Loading()
        })
    }


    /**
     *修改群成员 是否禁言
     * @mute 是否禁言:是 N,否 Y
     */
    fun putGroupMemberMute(groupId: String, memberId: String, mute: String) {
        if (putGroupMemberMuteLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.putGroupMute(groupId, memberId, mute)
            if (result.code == SUCCESS) {
                putGroupMemberMuteLiveData.value = LoadState.Success(result)
                //添加到缓存
                ChatDao.getGroupDb().updateGroupMemberMute(groupId, memberId, mute)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    putGroupMemberMuteLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    putGroupMemberMuteLiveData.value = LoadState.Fail(exc = Exception("编辑禁言信息失败"))
                }
            }
        }, onError = {
            putGroupMemberMuteLiveData.value = LoadState.Fail(exc = Exception("编辑禁言信息失败"))
        }, onStart = {
            putGroupMemberMuteLiveData.value = LoadState.Loading()
        })
    }


    /**
     *远程销毁好友消息
     */
    fun deleteRemoteFriendMessage(friendMemberId: String, deleteMessageType: String = "Bilateral") {
        if (deleteRemoteFriendMessage.value is LoadState.Loading) return
        requestLifeLaunch({
            val result =
                FriendRepository.deleteRemoteFriendMessage(friendMemberId, deleteMessageType)
            if (result.code == SUCCESS) {
                deleteRemoteFriendMessage.value = LoadState.Success(result)
                //更新本地会话数据
                ChatDao.getConversationDb().updateMsgByTargtId(friendMemberId, "消息已被远程销毁")
                //重置会话列表未读数
                ChatDao.getConversationDb().resetConverMsgCount(friendMemberId)
                //清除本地消息
                ChatDao.getChatMsgDb().delMsgListByFriendId(friendMemberId)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    deleteRemoteFriendMessage.value = LoadState.Fail(exc = Exception(errorInfo))
                    errorInfo.toast()
                } else {
                    deleteRemoteFriendMessage.value = LoadState.Fail(exc = Exception("操作失败"))
                }
            }
        }, onError = {
            deleteRemoteFriendMessage.value = LoadState.Fail(exc = Exception("操作失败"))
        }, onStart = {
            deleteRemoteFriendMessage.value = LoadState.Loading()
        })
    }

    /**
     * 修改好友备注/设置消息免打扰/加入黑名单
     * 一次只能修改一个参数
     * @friendMemberId 好友ID  必传
     * @remark 好友备注 可以为空
     * @black 是否加入黑名单:是 Y,否 N
     * @messageNotice    是否消息免打扰:是 N,否 Y
     */
    fun modifyFriendStatus(
        friendMemberId: String, remark: String? = null,
        black: String? = null, messageNotice: String? = null
    ) {
        if (modifyFriendStatus.value is LoadState.Loading) return
        requestLifeLaunch({
            var result: BaseBean<String> = when {
                messageNotice?.isNotBlank() == true -> {
                    FriendRepository.modifyFriendStatus(
                        friendMemberId = friendMemberId,
                        messageNotice = messageNotice
                    )
                }
                black?.isNotBlank() == true -> {
                    FriendRepository.modifyFriendStatus(
                        friendMemberId = friendMemberId,
                        black = black
                    )
                }
                else -> {
                    FriendRepository.modifyFriendStatus(
                        friendMemberId = friendMemberId,
                        remark = remark
                    )
                }
            }
            if (result?.code == SUCCESS) {
                modifyFriendStatus.value = LoadState.Success(result)
                if (messageNotice?.isNotBlank() == true) {
                    //更新了好友免打扰状态
                    ChatDao.getFriendDb().updateMessageNotice(friendMemberId, messageNotice)
                    //更新会话信息 群免打扰状态
                    ChatDao.getConversationDb()
                        .updateConversationMsgMuteByTargetId(friendMemberId, messageNotice == "N")
                }
            } else {
                val errorInfo = result?.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    modifyFriendStatus.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    modifyFriendStatus.value = LoadState.Fail(exc = Exception("修改失败"))
                }
            }
        }, onError = {
            modifyFriendStatus.value = LoadState.Fail(exc = Exception("修改失败"))
        }, onStart = {
            modifyFriendStatus.value = LoadState.Loading()
        })
    }
}