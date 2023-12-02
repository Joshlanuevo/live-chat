package com.ym.chat.bean

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/24 15:24
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 好友列表
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
data class FriendSearchBean(
    var info: String,
    var code: Int,
    val data: FriendInfoBean,
)