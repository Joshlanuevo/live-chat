package com.ym.chat.rxhttp

import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.enum.AskStatus
import com.ym.chat.enum.AskType
import rxhttp.RxHttp
import rxhttp.toOtherJson
import rxhttp.wrapper.cahce.CacheMode

/**
 * 好友相关
 * 请求处理
 */
object FriendRepository : BaseRepository() {
    //好友列表
    suspend fun getFriendList(userId: String): BaseBean<MutableList<FriendListBean>> {
        return RxHttp.get(ApiUrl.Chat.friendList + "/$userId")
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<MutableList<FriendListBean>>>()
            .await()
    }

    //查找好友
    suspend fun searchFriend(codeOrMobile: String): FriendSearchBean {
        return RxHttp.get(ApiUrl.User.findFriend)
            .add("codeOrMobile", codeOrMobile)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<FriendSearchBean>()
            .await()
    }

    //申请添加好友
    suspend fun applyAddFriend(selfMemberId: String, friendMemberId: String): SimpleBean {
        return RxHttp.postJson(ApiUrl.Chat.applyAddFriend)
            .add("memberId", selfMemberId)
            .add("friendMemberId", friendMemberId)
            .add("type", AskType.friend)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    //扫二维 添加好友
    suspend fun addFriendByEncode(encode: String): BaseBean<FriendQRCodeBean> {
        return RxHttp.postJson(ApiUrl.Chat.addFriendByEncode)
            .add("encode", encode)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<FriendQRCodeBean>>()
            .await()
    }

    //删除好友
    suspend fun deleteFriend(friendMemberId: String): SimpleBean {
        return RxHttp.deleteJson(ApiUrl.Chat.deleteFriend)
            .add("friendMemberId", friendMemberId)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    //获取申请列表
    suspend fun getAskFriendList(type: AskType = AskType.friend): BaseBean<AskFriendInfoBean> {
        var listId = mutableListOf<String>()
        return RxHttp.get(ApiUrl.Chat.friendAskList)
//            .add("memberId", MMKVUtils.getUser()?.id ?:"")
//            .add("type", type)
            .add("applyIdList", listId.add(MMKVUtils.getUser()?.id ?: ""))
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<AskFriendInfoBean>>()
            .await()
    }

    //获取好友申请列表通知
    suspend fun getFriendNotifyInfo(): BaseBean<FriendNotifyBean> {
        return RxHttp.get(ApiUrl.User.getFriendNotifyInfo)
            .add("curPage", 1)
            .add("pageSize", 1000)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<FriendNotifyBean>>()
            .await()
    }


    //同意和拒绝好友申请
    suspend fun modifyFriend(status: AskStatus, friendMemberId: String, askId: String): SimpleBean {
        return RxHttp.putJson(ApiUrl.Chat.modifyFriend)
            .add("id", askId)
//            .add("memberId", MMKVUtils.getUser()?.id ?:"")
            .add("friendMemberId", friendMemberId)
            .add("status", status)
            .add("type", AskType.friend)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }


    //远程销毁好友消息
    suspend fun deleteRemoteFriendMessage(
        friendMemberId: String,
        deleteMessageType: String = "Bilateral"
    ): SimpleBean {
        return RxHttp.deleteJson(ApiUrl.Chat.deleteRemoteFriendMessage)
            .add("friendMemberId", friendMemberId)
            .add("deleteMessageType", deleteMessageType)
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<SimpleBean>()
            .await()
    }

    /**
     * 修改好友备注/设置消息免打扰/加入黑名单
     * 一次只能修改一项目
     * @friendMemberId 好友ID
     * @remark 好友备注 可以为空
     * @black 是否加入黑名单:是 Y,否 N
     * @messageNotice    是否消息免打扰:是 Y,否 N
     */
    suspend fun modifyFriendStatus(
        friendMemberId: String, remark: String? = null,
        black: String? = null, messageNotice: String? = null
    ): BaseBean<String> {
        return RxHttp.putJson(ApiUrl.Chat.modifyFriendStatus)
            .addAll(getBaseParams().apply {
//                MMKVUtils.getUser()?.id?.let { put("memberId", it) }
                put("friendMemberId", friendMemberId)
                when {
                    black?.isNotBlank() == true -> put("black", black)
                    messageNotice?.isNotBlank() == true -> put("messageNotice", messageNotice)
                    else -> remark?.let { put("remark", it) }
                }
            })
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

}