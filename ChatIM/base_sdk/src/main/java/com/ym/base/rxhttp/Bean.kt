package com.ym.base.rxhttp

import java.io.Serializable

open class BaseBean<T>(
    var code: Int? = null,
    var message: String? = null,
    var data: T? = null,
) : Serializable

open class BaseListBean<T>(
    var code: Int? = null,
    var message: String? = null,
    var data: MutableList<T>? = null,
) : Serializable

open class BaseDataList<T>(
    code: Int? = null,
    message: String? = null,
    data: BaseBeanData<T>? = null
) : BaseBean<BaseBeanData<T>>(code, message, data), Serializable

open class BaseBeanData<T>(
    val countId: String? = null,        /// "",
    val current: Int = 0,   /// 0,
    val size: Int = 0,   /// 0,
    val total: Int = 0,   /// 0,
    val pages: Int = 0,   /// 0,
    val hitCount: Boolean? = null,          /// true,
    val maxLimit: Int? = null,      /// 0,
    val optimizeCountSql: Boolean? = null,          /// true,
    val searchCount: Boolean? = null,          /// true,
    var records: MutableList<T>? = null,
) : Serializable


data class UserBean(
    val balance: Float? = 0f,            // 54,余额
    val collectNum: Long? = -1,            // 54,喜欢数?收集数?关注数
    var describes: String? = null,            // null,个人简介
    val earnings: Float? = 0f,            // 54,收益
    val fansNum: Long? = 0,            // 0,粉丝数
    var headimg: String? = null,            // null,头像url
    var homeBackUrl: String? = null,            // null,头像背景url
    val inviteCode: String? = null,            // "ydNulfL3"邀请码
    val leaderCode: Boolean = false,            // "ydNulfL3"邀请码
    val inviteNum: Long? = 0,            // 0,邀请数
    val mobile: String? = null,            //绑定的手机
    val moreUrl: String? = null,            //    ???
    var nickname: String? = null,            // "rJM7vkMp",昵称
    var snsUrl: String? = null,            // "rJM7vkMp",  ???
    val userId: Long? = -1,            // 54,
    var version: String? = null,            // "1.0.0",
    var isAttestation: Int? = null ,            // 是否认证(约啪认证),
) : Serializable