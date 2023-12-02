package com.ym.chat.rxhttp

import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.enum.AskStatus
import com.ym.chat.enum.AskType
import com.ym.chat.enum.GroupMemberType
import com.ym.chat.utils.ChatType
import rxhttp.RxHttp
import rxhttp.toOtherJson
import rxhttp.toStr
import rxhttp.wrapper.cahce.CacheMode

/**
 * 群聊
 * 相关的请求
 */
object GroupRepository : BaseRepository() {

    //创建群组
    suspend fun createGroup(
        name: String,
        memberIds: MutableList<MemberBean>
    ): BaseBean<GroupInfoBean> {
        return RxHttp.postJson(ApiUrl.Chat.createGroup)
            .addAll(getBaseParams().apply {
                put("ownerId", MMKVUtils.getUser()?.id ?: "")
                put("name", name)
                put("groupMemberList", memberIds)
            })
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupInfoBean>>()
            .await()
    }

    //申请加入群
    suspend fun applyAddGroup(groupId: String): SimpleBean {
        return RxHttp.postJson(ApiUrl.Chat.applyAddFriend)
//            .add("memberId", MMKVUtils.getUser()?.id ?: "")
            .add("groupId", groupId)
            .add("type", AskType.group)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    //邀请加入群
    suspend fun inviteAddGroup(
        groupId: String,
        friendMemberIdList: MutableList<String>,
        memberType: Int
    ): SimpleBean {
        return RxHttp.postJson(ApiUrl.Chat.addGroupMember)
            .add("friendMemberIdList", friendMemberIdList)
            .add("groupId", groupId)
            .add("type", if (memberType == 0) AskType.inviteGroup else AskType.group)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    //接到ws消息，获取管理员拉人入群列表
    suspend fun getAddMemberListToGroup(): String {
        return RxHttp.get(ApiUrl.Chat.addGroupMemberList)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toStr()
            .await()
    }


    //同意和拒绝入群申请
    suspend fun modifyGroup(groupId: String, memberId: String, status: AskStatus): SimpleBean {
        return RxHttp.putJson(ApiUrl.Chat.modifyFriend)
            .add("groupId", groupId)
            .add("id", memberId)
            .add("status", status)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    //复制群组信息
    suspend fun copyGroupInfo(): BaseBean<GroupInfoBean> {
        return RxHttp.putJson(ApiUrl.Chat.copyGroup)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupInfoBean>>()
            .await()
    }

    //转让组
    suspend fun transferGroup(groupId: String, memberId: String): BaseBean<GroupInfoBean> {
        return RxHttp.putJson(ApiUrl.Chat.transferGroup)
//            .add("id", MMKVUtils.getUser()?.id ?: "")
            .add("groupId", groupId)
            .add("memberId", memberId)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupInfoBean>>()
            .await()
    }

    //设置/取消群管理员
    suspend fun setMemberRole(
        groupId: String,
        memberId: String,
        role: GroupMemberType
    ): BaseBean<GroupInfoBean> {
        return RxHttp.putJson(ApiUrl.Chat.setMemberRole)
            .add("groupId", groupId)
            .add("memberId", memberId)
            .add("role", role)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupInfoBean>>()
            .await()
    }

    //远程销毁群消息
    suspend fun deleteRemoteGroupMessage(
        groupId: String,
        deleteMessageType: String = "Bilateral"
    ): SimpleBean {
        return RxHttp.deleteForm(ApiUrl.Chat.deleteRemoteGroupMessage)
            .add("groupId", groupId)
            .add("deleteMessageType", deleteMessageType)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    //删除群组
    suspend fun deleteGroup(groupId: String): BaseBean<String> {
        return RxHttp.deleteJson(ApiUrl.Chat.deleteGroup)
            .add("id", groupId)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

    //管理员删除群成员
    suspend fun deleteGroupMember(groupId: String, memberId: String): BaseBean<String> {
        return RxHttp.putJson(ApiUrl.Chat.deleteGroupMember)
            .add("groupId", groupId)
            .add("memberId", memberId)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

    //群成员退出群组
    suspend fun leaveGroup(groupId: String): BaseBean<String> {
        return RxHttp.putJson(ApiUrl.Chat.leaveGroup)
            .add("groupId", groupId)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

    //查询自己所有群
    suspend fun getMyAllGroup(): BaseBean<MutableList<NewGroupInfoBean>> {
        return RxHttp.get(ApiUrl.Chat.getNewMyAllGroup)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<MutableList<NewGroupInfoBean>>>()
            .await()
    }

//    //查询自己所有群(有群管理跟通知消息)
//    suspend fun getNewMyAllGroup(): BaseBean<MutableList<GroupInfoBean>> {
//        return RxHttp.get(ApiUrl.Chat.getNewMyAllGroup)
//            .addAllHeader(getBaseHeaders())
//            .setCacheMode(CacheMode.ONLY_NETWORK)
//            .toOtherJson<BaseBean<MutableList<GroupInfoBean>>>()
//            .await()
//    }

    //查询群成员列表
    suspend fun getGroupMemberList(groupId: String): BaseBean<MutableList<GroupMemberBean>> {
        return RxHttp.get(ApiUrl.Chat.getGroupMemberList)
            .add("groupId", groupId)
//            .add("operatorId", MMKVUtils.getUser()?.id ?: "")
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<MutableList<GroupMemberBean>>>()
            .await()
    }

    //批量入群审核
    suspend fun putBatchModify(batchModifyList: MutableList<BatchModify>): BaseBean<MutableList<NotifyResultBean>> {
        return RxHttp.putBody(ApiUrl.Chat.putBatchModify)
            .setBody(batchModifyList)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<MutableList<NotifyResultBean>>>()
            .await()
    }

    //关键字屏蔽
    suspend fun getKeyWord(): BaseBean<KeyWordBean> {
        return RxHttp.get(ApiUrl.Chat.getKeyWord)
            .addQuery("memberLevelId", MMKVUtils.getUser()?.memberLevelId)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<KeyWordBean>>()
            .await()
    }

    //发送群发消息
    suspend fun groupSendMsg(
        content: String,
        groupIdList: String,
        groupNameList: String,
        chatType: String
    ): BaseBean<GroupSendBean> {
        return RxHttp.postJson(ApiUrl.Chat.groupSendMsg)
            .add("content", content)
            .add("receiverId", groupIdList)
            .add("receiverName", groupNameList)
            .add("msgType", chatType)
            .add("sendTime", System.currentTimeMillis())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupSendBean>>()
            .await()
    }

    //修改群成员昵称
//    suspend fun modifyMemberNickname(groupId: String,memberId: String,nickname:String): BaseBean<GroupInfoBean> {
//        return RxHttp.putJson(ApiUrl.Chat.modifyMemberNickname)
//            .add("groupId", groupId)
//            .add("memberId", memberId)
//            .add("nickname", nickname)
//            .add("operatorId", MMKVUtils.getUser()?.id ?:"")
//            .setCacheMode(CacheMode.ONLY_NETWORK)
//            .toOtherJson<BaseBean<GroupInfoBean>>()
//            .await()
//    }

    /**
     * 修改群成员通知信息
     */
    suspend fun putGroupNotice(
        groupId: String, messageNotice: String
    ): BaseBean<GroupInfoBean> {
        return RxHttp.putJson(ApiUrl.Chat.modifyMemberInfo)
            .addAll(getBaseParams().apply {
                put("groupId", groupId)
                MMKVUtils.getUser()?.id?.let { put("memberId", it) }
                put("messageNotice", messageNotice)
            })
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupInfoBean>>()
            .await()
    }

    /**
     * 修改群成员 是否禁言
     * @allowSpeak 是否禁言:是 N,否 Y
     */
    suspend fun putGroupMute(
        groupId: String, memberId: String, allowSpeak: String
    ): BaseBean<GroupInfoBean> {
        return RxHttp.putJson(ApiUrl.Chat.modifyMemberInfo)
            .addAll(getBaseParams().apply {
                put("groupId", groupId)
                put("memberId", memberId)
                put("allowSpeak", allowSpeak)
            })
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupInfoBean>>()
            .await()
    }

    /**
     * 修改群信息
     * 一次只能修改一项目
     * @name 修改群名
     * @headUrl 修改头像
     * @leaveNoticeAdmin 否退群通知群管理:是 Y,否 N
     * @lookGroupInfo    是否允许成员查看群信息:是 Y,否 N
     *        还有很多后续添加......
     */
    suspend fun putGroupInfo(
        groupId: String, name: String? = null,
        notice: String? = null, headUrl: String? = null,
        allowSpeak: String? = null, messageNotice: String? = null, leaveNoticeAdmin: String? = null,
        lookGroupInfo: String? = null
    ): BaseBean<GroupInfoBean> {
        return RxHttp.putJson(ApiUrl.Chat.putGroupInfoList)
            .addAll(getBaseParams().apply {
                put("id", groupId)
                if (name?.isNotBlank() == true) {
                    put("name", name)
                }
                if (notice?.isNotBlank() == true) {
                    put("notice", notice)
                }
                if (headUrl?.isNotBlank() == true) {
                    put("headUrl", headUrl)
                }
                if (allowSpeak?.isNotBlank() == true) {
                    put("allowSpeak", allowSpeak)
                }
                if (messageNotice?.isNotBlank() == true) {
                    put("messageNotice", messageNotice)
                }
                if (leaveNoticeAdmin?.isNotBlank() == true) {
                    put("leaveNoticeAdmin", leaveNoticeAdmin)
                }
                if (lookGroupInfo?.isNotBlank() == true) {
                    put("lookGroupInfo", lookGroupInfo)
                }
            })
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<GroupInfoBean>>()
            .await()
    }


    //删除通知消息
    suspend fun deleteGroupNotify(notifyIdList: MutableList<String>): BaseBean<SimpleBean> {
        return RxHttp.deleteBody(ApiUrl.Chat.deleteNotify)
            .setBody(notifyIdList)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<SimpleBean>>()
            .await()
    }

    //删除多条系统通知
    suspend fun deleteMessageBatch(notifyIdList: MutableList<String>): BaseBean<SimpleBean> {
        return RxHttp.postBody(ApiUrl.Chat.deleteSystemMessageBatch)
            .setBody(notifyIdList)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<SimpleBean>>()
            .await()
    }

}