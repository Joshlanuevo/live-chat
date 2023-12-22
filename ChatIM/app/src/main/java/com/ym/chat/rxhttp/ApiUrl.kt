package com.ym.chat.rxhttp

import android.text.TextUtils
import com.ym.base.constant.EventKeys.BASE_HOSTURL
import com.ym.base.constant.EventKeys.WS_HOSTURL
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.BuildConfig
import com.ym.chat.utils.PlatformUtils


/**
 * 基类地址本地更换需要更换：
 * 1.baseUrlDebug和baseUrlRelease
 * 2.adUrl
 * Date:2020-10-6
 * Time:16:07
 */
open class ApiUrl {
    //<editor-fold defaultstate="collapsed" desc="基础域名信息">
    enum class HOST_TYPE {
        DEV,//开发环境
        TEST,//测试环境
        UAT,//预发环境
        RELEASE//生产环境
    }

    companion object {

        val currentType = HOST_TYPE.RELEASE

        val suffix = "image"


        //获取oss配置路径
        val getOssConfig get() = PlatformUtils.getOssUrl()

        /**
         * ws地址
         * 生产的默认配置从jenkins读取
         */
        var wsUrl = when (currentType) {
            HOST_TYPE.DEV -> "ws://chat-dev.youliaoim.com/message-ws"
            HOST_TYPE.RELEASE -> "wss://chatlc101.devtest88.com/message-ws"
            else -> "ws://im-chat-dev.gnit.vip/message-ws"
        }

        /**
         * API地址
         * 生产的默认配置从jenkins读取
         */
        var baseApiHost = when (currentType) {
            HOST_TYPE.DEV -> "https://chatlc101.devtest88.com/v1/"
            HOST_TYPE.RELEASE -> "https://chatlc101.devtest88.com/v1/"
            else -> "https://chatlc101.devtest88.com/v1/"
        }

        val baseApiUrl
            get() = if (!TextUtils.isEmpty(MMKVUtils.getString(BASE_HOSTURL + currentType))) {
                MMKVUtils.getString(BASE_HOSTURL + currentType)
            } else baseApiHost

        val websocketUrl
            get() = if (!TextUtils.isEmpty(MMKVUtils.getString(WS_HOSTURL + currentType))) {
                MMKVUtils.getString(WS_HOSTURL + currentType)
            } else wsUrl
    }
    //</editor-fold>

    /**
     * 用户相关
     */
    class User {
        companion object {
            //注册
            val register get() = "${baseApiUrl}member/memberInfo/register"

            //登录
            val login get() = "${baseApiUrl}member/memberInfo/login"

            //登出
            val loginOut get() = "${baseApiUrl}member/memberInfo/logout"

            //获取验证码
            val getCode get() = "${baseApiUrl}member/sms/send"

            //根据会员好获取会员信息
            val memberGetUserInfo get() = "${baseApiUrl}member/memberInfo/getMemberByMemberId"

            //会员信息
            val userInfo get() = "${baseApiUrl}member/memberInfo/getMemberByToken"

            //添加好友-查询
            val findFriend get() = "${baseApiUrl}member/memberInfo/getMemberByCodeOrMobile"

            //修改会员信息
            val editUserInfo get() = "${baseApiUrl}member/memberInfo/modify"

            //修改会员密码
            val editUserPaw get() = "${baseApiUrl}member/memberInfo/modifyPassword"

            //重置密码
            val forgetPassword get() = "${baseApiUrl}member/memberInfo/forgetPassword"

            //修改会员状态
            val modifyStatus get() = "${baseApiUrl}member/memberInfo/modifyStatus"

            //修改手机号码
            val modifyPhone get() = "${baseApiUrl}member/memberInfo/modifyMobile"

            //多端登录获取验证码
            val sendVerifyCode get() = "${baseApiUrl}member/memberInfo/sendDeviceVerifyCode"

            //校验验证码
            val checkVerifyCode get() = "${baseApiUrl}member/memberInfo/loginByVerifyCode"

            //修改友聊号
            val modifyUsername get() = "${baseApiUrl}member/memberInfo/modifyUsername"

            //获取是否支持手机号注册
            val isMobileRegister get() = "${baseApiUrl}member/memberInfo/getSystemRegister"

            //获取发现url
            val getDiscover get() = "${baseApiUrl}member/memberInfo/getDiscover"

            //获取好友通知信息
            val getFriendNotifyInfo get() = "${baseApiUrl}chat/applyInfo/queryFriendApplyInfoPages"
        }
    }


    //<editor-fold defaultstate="collapsed" desc="聊天相关">
    /**
     * 聊天相关
     */
    class Chat {
        companion object {
            //查询好友列表
            val friendList get() = "${baseApiUrl}chat/friendInfo/listFriendInfoMessage"

            //查询好友申请列表
            val friendAskList get() = "${baseApiUrl}chat/applyInfo/listByIds"

            //申请添加好友
            val applyAddFriend get() = "${baseApiUrl}chat/applyInfo/build"

            //删除好友
            val deleteFriend get() = "${baseApiUrl}chat/friendInfo/delete"

            //同意或拒绝请求
            val modifyFriend get() = "${baseApiUrl}chat/applyInfo/modify"

            //修改好友备注/设置消息免打扰/加入黑名单
            val modifyFriendStatus get() = "${baseApiUrl}chat/friendInfo/modify"

            //创建群组
            val createGroup get() = "${baseApiUrl}chat/groupInfo/build"

            //获取群组信息
            val getGroupInfoByGroupId get() = "${baseApiUrl}chat/groupInfo/getGroupInfoByGroupId"

            //复制群组信息
            val copyGroup get() = "${baseApiUrl}chat/groupInfo/copy"

            //转让群
            val transferGroup get() = "${baseApiUrl}chat/groupInfo/transfer"

            //删除 解散群组
            val deleteGroup get() = "${baseApiUrl}chat/groupInfo/delete"

            //删除群通知消息
            val deleteNotify get() = "${baseApiUrl}chat/applyInfo/delete"

            //管理员删除群成员
            val deleteGroupMember get() = "${baseApiUrl}chat/groupInfo/deleteGroupMember"

            //群成员退群
            val leaveGroup get() = "${baseApiUrl}chat/groupInfo/leave"

            //添加群成员
            val addGroupMember get() = "${baseApiUrl}chat/applyInfo/batchMemberToGroup"

            //收到ws管理员拉取群成员入群 调用http接口
            val addGroupMemberList get() = "${baseApiUrl}chat/applyInfo/listGroupApplyInfo"

            //查询自己所有群
            val getMyAllGroup get() = "${baseApiUrl}chat/groupInfo/listMemberGroup"

            //查询自己所有群(有群管理跟通知消息)
            val getNewMyAllGroup get() = "${baseApiUrl}chat/groupInfo/listMemberInGroup"

            //查询群成员列表
            val getGroupMemberList get() = "${baseApiUrl}chat/groupInfo/listGroupMember"

            //设置群信息
            val putGroupInfoList get() = "${baseApiUrl}chat/groupInfo/modify"

            //设置/取消群管理员
            val setMemberRole get() = "${baseApiUrl}chat/groupInfo/setMemberRole"

            //修改群成员信息
            val modifyMemberInfo get() = "${baseApiUrl}chat/groupInfo/modifyMemberNickname"

            //远程销毁群的消息
            val deleteRemoteGroupMessage get() = "${baseApiUrl}chat/groupInfo/deleteRemoteGroupMessage"

            //远程销毁好友消息
            val deleteRemoteFriendMessage get() = "${baseApiUrl}chat/friendInfo/deleteRemoteFriendMessage"

            //上传文件
            val uploadFile get() = "${baseApiUrl}sso/im/upload/v1"

            //收藏
            val collect get() = "${baseApiUrl}chat/favoriteInfo/build"

            //收藏列表
            val collectList get() = "${baseApiUrl}chat/favoriteInfo/list"

            //删除收藏
            val delCollect get() = "${baseApiUrl}chat/favoriteInfo/delete"

            //获取群发消息列表
            val getSendGroupMsg get() = "${baseApiUrl}chat/batchSend/list"

            //清空群发消息
            val delSendGroupMsg get() = "${baseApiUrl}chat/batchSend/clear"

            //发送群发消息
            val groupSendMsg get() = "${baseApiUrl}chat/batchSend/build"

            //删除单条消息
            val deleteMessage get() = "${baseApiUrl}message/deleteMessage"

            //删除多条消息
            val deleteSystemMessageBatch get() = "${baseApiUrl}message/deleteSystemMessageBatch"

            //生成邀请链接秘文
            val getRefereeLink get() = "${baseApiUrl}chat/friendInfo/getRefereeLink"

            //根据秘文添加好友，适用于扫二维码添加好友
//            val addFriendByEncode get() = "${baseApiUrl}chat/friendInfo/addFriendByEncode"
            val addFriendByEncode get() = "${baseApiUrl}chat/applyInfo/addFriendByEncode"

            //发送消息
            val sendMsg get() = "${baseApiUrl}message/send"

            //编辑消息
            val modifyMsg get() = "${baseApiUrl}message/modify"

            //消息收到回执，content里面的ID
            val messageAck get() = "${baseApiUrl}message/messagePush/realtime/messageAck"

            //获取实时消息
            val realtimeMsgList get() = "${baseApiUrl}message/messagePush/realtime/list"

            //登录成功后 获取历史消息 最后一条消息ID
            val historyMsgList get() = "${baseApiUrl}message/messagePush/history/list"

            //获取系统消息
            val hisNoticeMsg get() = "${baseApiUrl}message/messagePush/history/systemNotify/list"

            //定向回复消息
            val messageReply get() = "${baseApiUrl}message/reply"

            //获取群置顶消息
            val getTopInfo get() = "${baseApiUrl}chat/topInfo/list"

            //删除群置顶消息
            val delTopInfo get() = "${baseApiUrl}chat/topInfo/delete"

            //添加群置顶消息
            val addTopInfo get() = "${baseApiUrl}chat/topInfo/build"

            //表情包列表
            val emojList get() = "${baseApiUrl}chat/emojInfo/list"

            //添加表情
            val addEmoj get() = "${baseApiUrl}chat/emojInfo/build"

            //删除gif
            val delEmoj get() = "${baseApiUrl}chat/emojInfo/delete"

            //批量入群审核
            val putBatchModify get() = "${baseApiUrl}chat/applyInfo/batchModify"

            //关键字屏蔽
            val getKeyWord get() = "${baseApiUrl}member/sensitiveWord/getSensitiveWordByMemberLevelId"

            //获取配置
            val getSystemInfo get() = "${baseApiUrl}member/memberInfo/getSystemInfo"

            //批量编辑收藏消息
            val putCollectContent get() = "${baseApiUrl}chat/favoriteInfo/modify"

            //分享联系人
            val shareContact get() = "${baseApiUrl}message/shareContact"

            //会话列表接口
            val sessionList get() = "${baseApiUrl}message/sessionInfo/list"

            //从服务端调用消息
            val getMsgFromService get() = "${baseApiUrl}message/messagePush/history/list/page"

            //查询@自己消息数据
            val getAtMsgList get() = "${baseApiUrl}message/messagePush/at/list"

            //上报@消息已读
            val ackAtMessage get() = "${baseApiUrl}message/messagePush/at/ack/messageIds"

            //根据ID获取消息
            val getMessageByIds get() = "${baseApiUrl}message/messagePush/getMessageByIds"

            //新增会话
            val sessionInfoAdd get() = "${baseApiUrl}message/sessionInfo/add"
        }
    }
    //</editor-fold>

    class Version {
        companion object {
            //获取app版本信息
            val getAppVersion get() = "${baseApiUrl}member/appVersion/load/appVersion"

            //建议反馈
            val sendFeedBack get() = "${baseApiUrl}member/feedback/build"

        }
    }
}