package com.ym.chat.bean

import com.blankj.utilcode.util.GsonUtils
import com.ym.base.widget.adapter.BaseBinderAdapterPro
import com.ym.base.widget.adapter.BaseItemBinderPro
import com.ym.chat.item.*
import com.ym.chat.utils.MsgType.MESSAGETYPE_AT
import com.ym.chat.utils.MsgType.MESSAGETYPE_NOTICE
import com.ym.chat.utils.MsgType.MESSAGETYPE_PICTURE
import com.ym.chat.utils.MsgType.MESSAGETYPE_TEXT
import com.ym.chat.utils.MsgType.MESSAGETYPE_TIME
import com.ym.chat.utils.MsgType.MESSAGETYPE_VIDEO
import com.ym.chat.utils.MsgType.MESSAGETYPE_VOICE
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

/**
 * 聊天记录 置顶消息
 */
@Entity
data class MsgTopBean(
    @Id
    var dbId: Long = 0,
    var command: Int = 0,
    var chatType: String = "",
    var cmd: Int = 0,
    var content: String = "",
    var from: String = "",
    var groupId: String = "",
    @Index
    @Unique
    var id: String = "",

    var friendMemberId: String? = "",
    var contentType: String = "",
    var msgType: String = "",
    var serverReceiveTime: String = "",
    //对方ID
    var to: String = "",
    //置顶消息id
    var messageId: String = "",
    //操作类型
    var operationType: String = "",
    //修改消息editId
    var editId: String = ""

) : BaseBinderAdapterPro.Types {

    //置顶
    var isTop: Boolean = false

    //置顶时间
    var topTime: Long = 0

    //本地地址，媒体消息，未发送显示
    var localPath: String = ""

    //消息已读状态，0未读，1已读
    var msgReadState: Int = 0

    //消息id
    var msgId: String = ""

    //消息方向，0：收到，1：发送
    var dir: Int = 0

    //消息发送状态（只对发送方有效），0：发送中，1：发送成功（已发），2：发送失败
    var sendState: Int = -1


    @Transient
    var index: Int = 1//置顶消息显示第几条


    @Transient
    override var clazz: Class<out BaseItemBinderPro<*, *>>? = null
        get() {
            return when (msgType) {
                MESSAGETYPE_TEXT -> {
                    return if (dir == 0) {
                        //文本消息-收到
                        ChatTextLeft::class.java
                    } else {
                        //文本消息-发送
                        ChatTextRight::class.java
                    }
                }
                MESSAGETYPE_PICTURE -> {
                    //图片消息
                    return if (dir == 0) {
                        //文本消息-收到
                        ChatImageLeft::class.java
                    } else {
                        //文本消息-发送
                        ChatImageRight::class.java
                    }
                }
                MESSAGETYPE_VOICE -> {
                    //音频消息
                    return if (dir == 0) {
                        //音频消息-收到
                        ChatAudioLeft::class.java
                    } else {
                        //音频消息-发送
                        ChatAudioRight::class.java
                    }
                }
                MESSAGETYPE_VIDEO -> {
                    //视频消息
                    return if (dir == 0) {
                        //视频消息-收到
                        ChatVideoLeft::class.java
                    } else {
                        //视频消息-发送
                        ChatVideoRight::class.java
                    }
                }
                MESSAGETYPE_TIME -> {
                    //时间线
                    return ChatTimeItem::class.java
                }
                MESSAGETYPE_NOTICE -> {
                    //通知类型
                    return ChatTimeItem::class.java
                }
                MESSAGETYPE_AT -> {
                    //@消息类型
                    return if (dir == 0) {
                        ChatTextLeft::class.java
                    } else {
                        ChatTextRight::class.java
                    }
                }
                else -> {
                    return ChatTextLeft::class.java
                }
            }
        }

    @Transient
    var mAudioUrl: String = ""
        get() {
            if (field.isNullOrBlank()) {
                try {
                    field = GsonUtils.fromJson(content ?: "{}", AudioMsgBean::class.java).url
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return field
        }

    @Transient
    var mAudioTime: Int = 0
        get() {
            try {
                field = GsonUtils.fromJson(content ?: "{}", AudioMsgBean::class.java).time
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return field
        }

    @Transient
    var videoInfo: VideoMsgBean? = null
        get() {
            if (field == null) {
                try {
                    field = GsonUtils.fromJson(content ?: "{}", VideoMsgBean::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return field
        }

}

data class TopInfo(
    var id: String? = null,
    var memberId: String? = null,
    var friendMemberId: String? = null,
    var groupId: String? = null,
    var messageId: String? = null,
    var operatorId: String? = null,
    var type: String? = null,
    var createTime: String? = null,
    var delIdList: String? = null,
    var content: String? = null
)