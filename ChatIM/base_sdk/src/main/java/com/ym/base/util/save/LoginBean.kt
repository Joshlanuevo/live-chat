package com.ym.base.util.save

import com.ym.base.widget.SideBar


/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/19 14:23
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
data class LoginBean(
    val info: String,
    val code: Int,
    val data: LoginData,
)

data class LoginData(
    val id: String,
    //友聊账号
    var username: String,
    //昵称
    var name: String,
    val password: String,
    val code: String,
    val memberLevelId: String,
    val memberLevelCode: String,
    val friendMemberId: String,
    val memberId: String,
    var gender: String,
    var mobile: String,
    val email: String,
    val address: String,
    var headUrl: String,
    val sign: String,
    val status: String,
    val onlineStatus: String,
    val recommendCode:String,
    val registerType: String,
    val remark: String,
    val black: String,
    val createTime: String,
    val updateTime: String,
    var displayHead: String = "",//是 Y,否 N
    var headText: String = "",//层级头像标识文字
    var levelHeadUrl: String = "",//层级头像标识图片
    var deviceDescription: String = ""//设备描述

) {
    var isEdit = false //是否在编辑状态
    var firstChar: String = ""
        get() {
            return SideBar.characters[0]
//            val PinyinUtils.getPinyinFirstLetter(showName())?.uppercase() ?: "#"
//            return if (PatternUtils.isEnglish(char)) char else "#"
        }

    fun showUserName(): String? {
        return if (username.isNullOrBlank()) code else username
    }
    fun showNickName(): String? {
        return if (remark.isNullOrBlank()) name else remark
    }
}