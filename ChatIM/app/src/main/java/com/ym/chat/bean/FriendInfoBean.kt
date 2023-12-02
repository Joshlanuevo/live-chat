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
class FriendInfoBean(
    val id: String,

    //友聊账号
    val username: String,

    //昵称
    val name: String,
    val password: String,
    val memberLevelId: String,
    val gender: String,
    val mobile: String,
    val email: String,
    val address: String,
    val headUrl: String,
    val sign: String,
    val status: String,
    val onlineStatus: String,
    val registerType: String,
    val remark: String,
    val createTime: String,
    val updateTime: String,
) : Serializable