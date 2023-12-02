package com.ym.base.constant

/**
 * Author:yangcheng
 * Date:2020-10-6
 * Time:16:07
 */
class ConstBeanCode {
  companion object {
    const val SERVICE_NO_DATA = -500
    const val DATA_TRANSFORM_ERROR = -666

    //没有加入圈子，但是想操作圈子的异常
    const val NO_JOIN_CIRCLE = 100008 //msg:你尚未加入[651615]圈子!

    //其它地方登录
    const val SUCESS = 200 //登录失效，请重新登录
    const val LOGIN_INVALID = 5007 //登录失效，请重新登录
    const val LOGIN_OTHER = 5016 //msg:帐户在其它地方登录
    const val ACCOUNT_CLOSED = 1000010 //msg:已封号
    const val SENSITIVE_WORDS = 100009 //敏感词

    //const val ee =1000000 //没有报错,
    const val SEND_SMS_TOO_MORE = 1000001 //发送验证码过于频繁,
    const val SEND_SMS_FAILED = 1000002 //发送验证码失败,
    const val SMS_ERROR = 5003 //验证码错误,
    const val SEND_SMS_PHONE_HAVE = 1000003 //发送验证码手机已存在,
    const val REQUEST_PARAMS_NO_EMPTY = 1000004 //信息不能为空,
    const val ALREADY_REGIST_USER_NAME = 1000005 //用户名已被注册,
    const val LOOK_MOVE_COUNT_FINISH = 3000001 //观影次数用完
  }
}