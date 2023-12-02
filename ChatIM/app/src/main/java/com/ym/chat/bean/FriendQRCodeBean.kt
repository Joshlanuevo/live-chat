package com.ym.chat.bean

import java.io.Serializable

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/29 14:08
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class FriendQRCodeBean(
    val id: String,
    val memberId: String,
    val friendMemberId: String? = "",
    val type: String,
    val friendRelationFlag: Boolean,
) : Serializable