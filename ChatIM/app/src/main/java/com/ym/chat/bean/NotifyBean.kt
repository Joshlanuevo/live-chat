package com.ym.chat.bean

import com.ym.base.util.save.LoginData
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

/**
 * 系统通知 bean
 */
@Entity
data class NotifyBean(
    @Id
    var dbId: Long = 0,
    @Unique
    @Index
    val id: String,
    val chatType: String? = "",
    val title: String? = "",
    val content: String? = "",//副文本内容
    val contentBriefly: String? = "",//显示内容
    val contentTitle: String? = "",//内容标题
    val systemType: Int = 0,//必须是type = 0 的系统通知   systemType= 0 意见反馈  1系统通知
    val type: Int = 0,//0 系统通知  1 账号登录验证码  2 好友通知  3 群主验证好友通知
    val from: String? = "",
    val to: String? = "",
    var verifyType: Int = 0,//0 需要验证  1 已拒绝  2 已同意
    val createTime: Long = 0,
    val updateTime: Long = 0,
    val groupId: String? = "",
    val friendMemberId: String? = "",
    val friendMemberName: String? = "",
    val friendMemberHeadUrl: String? = "",
    val memberId: String? = "",
    val memberName: String? = "",
    var status: String? = "",
    var keepTime: String? = "",//保质时间
    var verfiyCode: String? = "",//验证码
    var deviceDescription: String? = "",//手机型号
    //消息已读状态，0未读，1已读
    var msgReadState: Int = 0
) {
    @Transient
    var selectType = 0// 0没有选择 1选择 2不能选择
    @Transient
    var isShowCheck = false//是否显示选择框
}


data class NotifyResultBean(
    var dbId: Long = 0,
    val id: String,
    val chatType: String? = "",
    val title: String? = "",
    val content: String? = "",
    val contentTitle: String? = "",//内容标题
    val type: String? = "",//Group Friend
    val from: String? = "",
    val to: String? = "",
    var verifyType: Int = 0,//0 需要验证  1 已拒绝  2 已同意
    val createTime: Long = 0,
    val updateTime: Long = 0,
    val groupId: String? = "",
    val friendMemberId: String? = "",
    val friendMemberName: String? = "",
    val memberId: String? = "",
    var status: String? = "",
    var keepTime: String? = "",//保质时间
    var verfiyCode: String? = "",//验证码
    var deviceDescription: String? = ""//手机型号
)

data class FriendNotifyBean(
    var records:MutableList<LoginData>?=null,
    var total:Int = 0,
    var size:Int = 0,
    var current:Int = 0,
    var pages:Int = 0,
    var searchCount:Boolean = true,
)