package com.ym.chat.utils

import com.ym.chat.bean.*

/**
 * 缓存数据
 */
object ImCache {
    //群成员数据
    val groupMemberList: MutableList<GroupMemberBean> = mutableListOf()

    //好友数据
    val friendList: MutableList<FriendListBean> = mutableListOf()

    //群组数据
    val groupList: MutableList<GroupInfoBean> = mutableListOf()


    //消息
    val notifycationMsg = hashMapOf<String, MutableList<ChatMessageBean>>()

    //被踢原因
    var KillOutType: String = ""

    //是否需要更新通知消息
    var isUpdateNotifyMsg: Boolean = true

    //at了自己的消息数据
    var atConverMsgList: MutableList<AtMessageInfoBean> = mutableListOf()
}