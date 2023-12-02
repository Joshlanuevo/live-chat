package com.ym.chat.bean

import com.ym.base.widget.SideBar
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class AccountBean(
    @Id
    var idInDb: Long = 0,
    val id: String = "",
    //友聊账号
    val username: String = "",
    //昵称
    var name: String = "",
    val code: String = "",
    val memberLevelId: String = "",
    val gender: String = "",
    val mobile: String = "",
    val email: String = "",
    val address: String = "",
    var headUrl: String = "",
    val sign: String = "",
    val status: String = "",
    val onlineStatus: String = "",
    val recommendCode: String = "",
    val registerType: String = "",
    val remark: String = "",
    val createTime: String = "",
    val updateTime: String = "",
    var accountType: Int = 0,//输入账号类型，0：手机号，1：友聊账号
    var displayHead: String = "",//是 Y,否 N
    var headText: String = "",//层级头像标识文字
    var levelHeadUrl: String = "",//层级头像标识图片
    val password: String = "",
) {
    var isEdit = false //是否在编辑状态
    var isSelect = false //是否是当前登录账号
    var firstChar: String = ""
        get() {
            return SideBar.characters[0]
        }

    fun showUsername(): String {
        return if (username.isNullOrBlank()) code else username
    }
}