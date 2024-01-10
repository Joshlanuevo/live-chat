package com.ym.chat.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.enum.AskStatus
import com.ym.chat.enum.GroupMemberType
import com.ym.chat.rxhttp.GroupRepository
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.MsgType

/**
 * 群聊相关
 */
class ChatGroupViewModel : BaseViewModel() {
    val createGroupLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()
    val copyGroupLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()
    val deleteGroupLiveData = MutableLiveData<LoadState<BaseBean<String>>>()
    val deleteGroupMemberLiveData = MutableLiveData<LoadState<BaseBean<String>>>()
    val leaveGroupLiveData = MutableLiveData<LoadState<BaseBean<String>>>()
    val getMyAllGroupLiveData =
        MutableLiveData<LoadState<BaseBean<MutableList<NewGroupInfoBean>>>>()
    val getGroupMemberLiveData =
        MutableLiveData<LoadState<BaseBean<MutableList<GroupMemberBean>>>>()
    val putGroupLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()
    val putGroupMemberNoticeLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()
    val putGroupMemberMuteLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()
    val transferGroupLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()
    val setMemberRoleLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()

    //    val modifyMemberNicknameLiveData = MutableLiveData<LoadState<BaseBean<GroupInfoBean>>>()
    val addGroupLiveData = MutableLiveData<LoadState<SimpleBean>>()
    val setGroupMemberLiveData = MutableLiveData<LoadState<Boolean>>()
    val deleteRemoteGroupMessage = MutableLiveData<LoadState<SimpleBean>>()
    val putBatchModify = MutableLiveData<LoadState<MutableList<NotifyResultBean>>>()

    /**
     * 创建群组
     * @name  群名
     * @memberIds  群成员ID列表
     */
    fun createGroup(name: String, memberIds: MutableList<MemberBean>) {
        if (createGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.createGroup(name, memberIds)
            if (result.code == SUCCESS) {
                if (result?.data != null && !TextUtils.isEmpty(result.data.id)) {
                    //生成创建群组提示
//                    createGroupInfo(result.data.id, name)
                }

                //生成会话列表数据
                ChatDao.getConversationDb().saveGroupConversation(
                    result.data.id,
                    ChatUtils.getString(R.string.您创建了群组),
                    MsgType.MESSAGETYPE_TEXT
                )

                createGroupLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    createGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    createGroupLiveData.value = LoadState.Fail(exc = Exception(ChatUtils.getString(R.string.创建群组失败))) // "创建群组失败"
                }
            }
        }, onError = {
            createGroupLiveData.value = LoadState.Fail(exc = Exception(ChatUtils.getString(R.string.创建群组失败))) // "创建群组失败"
        }, onStart = {
            createGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     * 生成创建群组提示语
     */
    private fun createGroupInfo(groupIdStr: String, groupName: String) {
        //生成提示语
        val bean = ChatMessageBean().apply {
            to = ""
            from = ""
            sendState = 1
            groupId = groupIdStr
            chatType = ChatType.CHAT_TYPE_GROUP
            createTime = System.currentTimeMillis()
            msgType = MsgType.MESSAGETYPE_NOTICE
//            content = "您创建了群组"
            content = "您创建了群组「${groupName}」:\n" +
                    "群人数可达1000人\n" +
                    "群置顶消息可设置五则\n" +
                    "对话信息保存七日\n" +
                    "群管理员可禁言、踢人"
        }
        //1、保存消息到数据库
        ChatDao.getChatMsgDb().saveChatMsg(bean)
    }

    /**
     * 申请加入群组
     */
    fun applyAddGroup(groupId: String) {
        if (addGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.applyAddGroup(groupId)
            if (result.code == SUCCESS) {
                addGroupLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    addGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    addGroupLiveData.value = LoadState.Fail(exc = Exception("申请入群失败"))
                }
            }
        }, onError = {
            addGroupLiveData.value = LoadState.Fail(exc = Exception("申请入群失败"))
        }, onStart = {
            addGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     * 邀请加入群组
     */
    fun inviteAddGroup(groupId: String, friendMemberIdList: MutableList<String>, memberType: Int) {
        if (addGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.inviteAddGroup(groupId, friendMemberIdList, memberType)
            if (result.code == SUCCESS) {
                addGroupLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    addGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    addGroupLiveData.value = LoadState.Fail(exc = Exception("申请入群失败"))
                }
            }
        }, onError = {
            addGroupLiveData.value = LoadState.Fail(exc = Exception("申请入群失败"))
        }, onStart = {
            addGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     * 同意跟拒绝入群
     * @status 状态:待处理 Pending,已接受 Accepted,已拒绝 Refused
     */
    fun modifyGroup(groupId: String, memberId: String, status: AskStatus) {
        if (setGroupMemberLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.modifyGroup(groupId, memberId, status)
            if (result.code == SUCCESS) {
                setGroupMemberLiveData.value = LoadState.Success(true)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    setGroupMemberLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    setGroupMemberLiveData.value = LoadState.Fail(exc = Exception("操作失败"))
                }
                if (result.code == 30000) {
                    setGroupMemberLiveData.value = LoadState.Success(false)
                }
            }
        }, onError = {
            setGroupMemberLiveData.value = LoadState.Fail(exc = Exception("操作失败"))
        }, onStart = {
            setGroupMemberLiveData.value = LoadState.Loading()
        })
    }


    /**
     * 复制群组
     */
    fun copyGroup() {
        if (copyGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.copyGroupInfo()
            if (result.code == SUCCESS) {
                copyGroupLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    copyGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    copyGroupLiveData.value = LoadState.Fail(exc = Exception("复制群组信息失败"))
                }
            }
        }, onError = {
            copyGroupLiveData.value = LoadState.Fail(exc = Exception("复制群组信息失败"))
        }, onStart = {
            copyGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     *转让群
     */
    fun transferGroup(groupId: String, memberId: String) {
        if (transferGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.transferGroup(groupId, memberId)
            if (result.code == SUCCESS) {
                transferGroupLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    transferGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    transferGroupLiveData.value = LoadState.Fail(exc = Exception("转让群失败"))
                }
            }
        }, onError = {
            transferGroupLiveData.value = LoadState.Fail(exc = Exception("转让群失败"))
        }, onStart = {
            transferGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     *批量申请 同意拒绝入群
     */
    fun putBatchModify(batchModifyList: MutableList<BatchModify>) {
        if (putBatchModify.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.putBatchModify(batchModifyList)
            if (result.code == SUCCESS) {
                putBatchModify.value = LoadState.Success(result.data)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    "$errorInfo".toast()
                    putBatchModify.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    putBatchModify.value = LoadState.Fail(exc = Exception("操作失败"))
                }
            }
        }, onError = {
            putBatchModify.value = LoadState.Fail(exc = Exception("操作失败"))
        }, onStart = {
            putBatchModify.value = LoadState.Loading()
        })
    }

    /**
     *设置/取消群管理员
     */
    fun setMemberRole(groupId: String, memberId: String, role: GroupMemberType) {
        if (setMemberRoleLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.setMemberRole(groupId, memberId, role)
            if (result.code == SUCCESS) {
                setMemberRoleLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    setMemberRoleLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    setMemberRoleLiveData.value = LoadState.Fail(exc = Exception("操作失败"))
                }
            }
        }, onError = {
            setMemberRoleLiveData.value = LoadState.Fail(exc = Exception("操作失败"))
        }, onStart = {
            setMemberRoleLiveData.value = LoadState.Loading()
        })
    }

    /**
     *远程销毁消息
     */
    fun deleteRemoteGroupMessage(groupId: String, deleteMessageType: String = "Bilateral") {
        if (deleteRemoteGroupMessage.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.deleteRemoteGroupMessage(groupId, deleteMessageType)
            if (result.code == SUCCESS) {
                //重置会话列表未读数
                ChatDao.getConversationDb().resetConverMsgCount(groupId)
                //清除本地消息
                ChatDao.getChatMsgDb().delMsgListByGroupId(groupId)
                //更新本地会话数据
                ChatDao.getConversationDb().updateMsgByTargtId(groupId, ChatUtils.getString(R.string.yuanchengxiaoxiyibeixiaohui)) // "消息已被远程销毁"
                deleteRemoteGroupMessage.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    deleteRemoteGroupMessage.value = LoadState.Fail(exc = Exception(errorInfo))
                    errorInfo.toast()
                } else {
                    deleteRemoteGroupMessage.value = LoadState.Fail(exc = Exception("操作失败"))
                }
            }
        }, onError = {
            deleteRemoteGroupMessage.value = LoadState.Fail(exc = Exception("操作失败"))
        }, onStart = {
            deleteRemoteGroupMessage.value = LoadState.Loading()
        })
    }

//    /**
//     *设置群成员昵称
//     */
//    fun modifyMemberNickname(groupId: String,memberId: String,nickname:String) {
//        if (modifyMemberNicknameLiveData.value is LoadState.Loading) return
//        requestLifeLaunch({
//            val result = GroupRepository.modifyMemberNickname(groupId,memberId,nickname)
//            if (result.code == SUCCESS) {
//                modifyMemberNicknameLiveData.value = LoadState.Success(result)
//            } else {
//                val errorInfo = result.info
//                if (!TextUtils.isEmpty(errorInfo)) {
//                    modifyMemberNicknameLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
//                } else {
//                    modifyMemberNicknameLiveData.value = LoadState.Fail(exc = Exception("设置群成员昵称失败"))
//                }
//            }
//        }, onError = {
//            modifyMemberNicknameLiveData.value = LoadState.Fail(exc = Exception("设置群成员昵称失败"))
//        }, onStart = {
//            modifyMemberNicknameLiveData.value = LoadState.Loading()
//        })
//    }


    /**
     *删除解散群组
     */
    fun deleteGroup(groupId: String) {
        if (deleteGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.deleteGroup(groupId)
            if (result.code == SUCCESS) {
                deleteGroupLiveData.value = LoadState.Success(result)
                //删除本地群数据
                ChatDao.getGroupDb().deleteGroup(groupId)
                LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                    .post(false)
                //删除会话列表数据
                ChatDao.getConversationDb().delConverByTargtId(groupId)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    deleteGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    deleteGroupLiveData.value = LoadState.Fail(exc = Exception("删除群组失败"))
                }
            }
        }, onError = {
            deleteGroupLiveData.value = LoadState.Fail(exc = Exception("删除群组失败"))
        }, onStart = {
            deleteGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     *删除群成员
     */
    fun deleteGroupMember(groupId: String, memberId: String) {
        if (deleteGroupMemberLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.deleteGroupMember(groupId, memberId)
            if (result.code == SUCCESS) {
                deleteGroupMemberLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    deleteGroupMemberLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    deleteGroupMemberLiveData.value = LoadState.Fail(exc = Exception("删除群成员失败"))
                }
            }
        }, onError = {
            deleteGroupMemberLiveData.value = LoadState.Fail(exc = Exception("删除群成员失败"))
        }, onStart = {
            deleteGroupMemberLiveData.value = LoadState.Loading()
        })
    }

    /**
     *群成员退群
     */
    fun leaveGroup(groupId: String) {
        if (leaveGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.leaveGroup(groupId)
            if (result.code == SUCCESS) {
                //删除本地群数据
                ChatDao.getGroupDb().deleteGroup(groupId)
                LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java)
                    .post(false)
                leaveGroupLiveData.value = LoadState.Success(result)
                //删除会话列表数据
                ChatDao.getConversationDb().delConverByTargtId(groupId)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    leaveGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    leaveGroupLiveData.value = LoadState.Fail(exc = Exception("退群失败"))
                }
            }
        }, onError = {
            leaveGroupLiveData.value = LoadState.Fail(exc = Exception("退群失败"))
        }, onStart = {
            leaveGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     *查询自己所有群
     */
    fun getMyAllGroup() {
        if (getMyAllGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.getMyAllGroup()
            if (result.code == SUCCESS) {
                getMyAllGroupLiveData.value = LoadState.Success(result)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    getMyAllGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    getMyAllGroupLiveData.value = LoadState.Fail(exc = Exception("获取群信息失败"))
                }
            }
        }, onError = {
            getMyAllGroupLiveData.value = LoadState.Fail(exc = Exception("获取群信息失败"))
        }, onStart = {
            getMyAllGroupLiveData.value = LoadState.Loading()
        })
    }

    /**
     *查询群成员列表
     */
    fun getGroupMemberList(groupId: String) {
        if (getGroupMemberLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.getGroupMemberList(groupId)
            if (result.code == SUCCESS) {
                getGroupMemberLiveData.value = LoadState.Success(result)
                //存储群成员到本地数据库
                ChatDao.getGroupDb().saveGroupMemberList(result.data, groupId)
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    getGroupMemberLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    getGroupMemberLiveData.value = LoadState.Success(
                        BaseBean(
                            data = ChatDao.getGroupDb().getMemberByGroupId(groupId)
                        )
                    )
                }
            }
        }, onError = {
            getGroupMemberLiveData.value =
                LoadState.Success(BaseBean(data = ChatDao.getGroupDb().getMemberByGroupId(groupId)))
        }, onStart = {
            getGroupMemberLiveData.value = LoadState.Loading()
        })
    }

    /**
     *修改群成员通知
     */
    fun putGroupMemberNotice(groupId: String, messageNotice: String) {
        if (putGroupMemberNoticeLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            val result = GroupRepository.putGroupNotice(groupId, messageNotice)
            if (result.code == SUCCESS) {
                putGroupMemberNoticeLiveData.value = LoadState.Success(result)
                //更新免打扰状态
                ChatDao.getGroupDb().updateMessageNotice(groupId, messageNotice)
                //更新会话信息 好友免打扰状态
                ChatDao.getConversationDb()
                    .updateConversationMsgMuteByTargetId(groupId, messageNotice == "N")
            } else {
                val errorInfo = result.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    putGroupMemberNoticeLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    putGroupMemberNoticeLiveData.value = LoadState.Fail(exc = Exception("编辑通知信息失败"))
                }
            }
        }, onError = {
            putGroupMemberNoticeLiveData.value = LoadState.Fail(exc = Exception("编辑通知信息失败"))
        }, onStart = {
            putGroupMemberNoticeLiveData.value = LoadState.Loading()
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
     * 修改群成员信息
     * 一次只能修改一项目
     * @name 修改群名
     * @headUrl 修改头像
     * @leaveNoticeAdmin 否退群通知群管理:是 Y,否 N
     * @lookGroupInfo    是否允许成员查看群信息:是 Y,否 N
     *        还有很多后续添加......
     */
    fun putGroupInfo(
        groupId: String,
        name: String? = null,
        notice: String? = null,
        headUrl: String? = null,
        allowSpeak: String? = null,
        messageNotice: String? = null,
        leaveNoticeAdmin: String? = null,
        lookGroupInfo: String? = null
    ) {
        if (putGroupLiveData.value is LoadState.Loading) return
        requestLifeLaunch({
            var result: BaseBean<GroupInfoBean> = when {
                name?.isNotBlank() == true -> {
                    GroupRepository.putGroupInfo(groupId, name = name)
                }
                notice?.isNotBlank() == true -> {
                    GroupRepository.putGroupInfo(groupId, notice = notice)
                }
                headUrl?.isNotBlank() == true -> {
                    GroupRepository.putGroupInfo(groupId, headUrl = headUrl)
                }
                allowSpeak?.isNotBlank() == true -> {
                    GroupRepository.putGroupInfo(groupId, allowSpeak = allowSpeak)
                }
                messageNotice?.isNotBlank() == true -> {
                    GroupRepository.putGroupInfo(groupId, messageNotice = messageNotice)
                }
                leaveNoticeAdmin?.isNotBlank() == true -> {
                    GroupRepository.putGroupInfo(groupId, leaveNoticeAdmin = leaveNoticeAdmin)
                }
                else -> {
                    GroupRepository.putGroupInfo(groupId, lookGroupInfo = lookGroupInfo)
                }
            }
            if (result?.code == SUCCESS) {
                putGroupLiveData.value = LoadState.Success(result)
                if (headUrl?.isNotBlank() == true) {
                    //修改了群头像
                    ChatDao.getGroupDb().updateIconById(groupId, headUrl)
                }
            } else {
                val errorInfo = result?.info
                if (!TextUtils.isEmpty(errorInfo)) {
                    putGroupLiveData.value = LoadState.Fail(exc = Exception(errorInfo))
                } else {
                    putGroupLiveData.value = LoadState.Fail(exc = Exception("修改群信息失败"))
                }
            }
        }, onError = {
            putGroupLiveData.value = LoadState.Fail(exc = Exception("修改群信息失败"))
        }, onStart = {
            putGroupLiveData.value = LoadState.Loading()
        })
    }


}