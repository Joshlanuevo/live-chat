package com.ym.chat.utils

import com.blankj.utilcode.util.Utils
import com.ym.chat.R

object ServiceErrorCode {
    fun Int.toMsg(): String {
        if (this == 10023) {
            return getString(R.string.登录密码错误)
        } else if (this == 10000) {
            return getString(R.string.手机号已被注册)
        } else if (this == 10001) {
            return getString(R.string.密码不符合规则)
        } else if (this == 10002) {
            return getString(R.string.确认密码和密码不一致)
        } else if (this == 10003) {
            return getString(R.string.用户名或密码错误)
        } else if (this == 10004) {
            return getString(R.string.token验证失败)
        } else if (this == 10005) {
            return getString(R.string.会员不存在)
        } else if (this == 10008) {
            return getString(R.string.手机号码不合)
        } else if (this == 10009) {
            return getString(R.string.手机验证码已过期)
        } else if (this == 10010) {
            return getString(R.string.手机验证码错误)
        } else if (this == 10012) {
            return getString(R.string.发送短信失败)
        } else if (this == 10013) {
            return getString(R.string.邮箱不合法)
        } else if (this == 10018) {
            return getString(R.string.新密码和旧密码一致无需修改)
        } else if (this == 10019) {
            return getString(R.string.用户名不合法)
        } else if (this == 10020) {
            return getString(R.string.密码不能为空)
        } else if (this == 10033) {
            return getString(R.string.原密码输入错误)
        } else if (this == 10034) {
            return getString(R.string.密码错误次数)
        } else if (this == 10043) {
            return getString(R.string.该手机号设备已被拉入黑名单)
        } else if (this == 10050) {
            return getString(R.string.此用户已被封禁请先解除封禁再做修改)
        } else if (this == 10055) {
            return getString(R.string.该IP地址已被拉入黑名单)
        } else if (this == 10063) {
            return getString(R.string.同一IP注册会员次数上限)
        } else {
            return getString(R.string.请求服务器错误)
        }
    }

    fun getString(resId: Int): String {
        return ChatUtils.getString(resId);
    }
}