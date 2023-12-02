package com.ym.base.constant

import androidx.annotation.IntDef
import com.blankj.utilcode.constant.TimeConstants
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Author:yangcheng
 * Date:2020-10-8
 * Time:16:06
 */
class ConstTime {
  companion object {
    //短信验证码倒计时
    const val SMS_TIME_SECONDS = 60L

    //邮箱验证码倒计时
    const val EMAIL_TIME_SECONDS = 60L

    //定时上报活跃时间接口间隔时间
    const val UPLOAD_ACTIVE_TIME_SECONDS = 120L

    //网络请求超时时间
    const val NET_TIME_OUT = 30L

    //1分钟
    const val MIN_1 = 1L * TimeConstants.MIN

    //5分钟
    const val MIN_5 = 5L * TimeConstants.MIN

    //30分钟
    const val MIN_30 = 30L * TimeConstants.MIN

    //1小时
    const val HOUR_1 = 1L * TimeConstants.HOUR

    //1天
    const val DAY_1 = 1L * TimeConstants.DAY

    //1周
    const val WEEK_1 = 7L * TimeConstants.DAY

    /**
     * 毫秒与毫秒的倍数
     */
    const val MSEC = 1

    /**
     * 秒与毫秒的倍数
     */
    const val SEC = 1000

    /**
     * 分与毫秒的倍数
     */
    const val MIN = 60000

    /**
     * 时与毫秒的倍数
     */
    const val HOUR = 3600000

    /**
     * 天与毫秒的倍数
     */
    const val DAY = 86400000

    @IntDef(MSEC, SEC, MIN, HOUR, DAY)
    @Retention(RetentionPolicy.SOURCE)
    annotation class Unit
  }
}