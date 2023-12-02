package com.ym.chat.utils

import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.bean.RecordBean

/**
 * @version V1.0
 * @createAuthor
 *       ___         ___          ___
 *      /  /\       /  /\        /  /\           ___
 *     /  /::\     /  /:/       /  /::\         /__/|
 *    /  /:/\:\   /__/::\      /  /:/\:\    __  | |:|
 *   /  /:/~/::\  \__\/\:\    /  /:/~/::\  /__/\| |:|
 *  /__/:/ /:/\:\    \  \:\  /__/:/ /:/\:\ \  \:\_|:|
 *  \  \:\/:/__\/    \__\:\  \  \:\/:/__\/  \  \:::|
 *   \  \::/         /  /:/   \  \::/        \  \::|
 *    \  \:\        /__/:/     \  \:\         \  \:\
 *     \  \:\       \__\/       \  \:\         \  \:\
 *      \__\/                    \__\/          \__\/
 * @createDate  2022.03.25 2:26 下午
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
object ChatMsgUtils {

    fun groupMemberCopyFriendListBean(groupMember: GroupMemberBean): FriendListBean {
        return FriendListBean(
            headUrl = groupMember.headUrl ?: "",
            name = groupMember.name ?: "",
            mobile = groupMember.mobile ?: "",
            username = groupMember.username ?: "",
            friendMemberId = groupMember.id ?: "",
            remark = groupMember.nickRemark ?: "",
            levelHeadUrl = groupMember.levelHeadUrl ?: "",
            displayHead = groupMember.displayHead ?: "",
            allowSpeak = groupMember.allowSpeak ?:"",
            groupId = groupMember.groupId ?:"",
            memberId = groupMember.memberId ?:"",
            role = groupMember.role ?:""
        )
    }

    fun chatMessageBeanCopyRecordBean(data: ChatMessageBean): RecordBean {
        return RecordBean(
            0,
            data.content,
            data.createTime.toString(),
            "", "", "",
            data.id, "", data.msgType, "", ""
        )
    }

}