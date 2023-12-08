package com.ym.chat.bean

import com.blankj.utilcode.util.GsonUtils
import com.ym.base.widget.adapter.BaseBinderAdapterPro
import com.ym.base.widget.adapter.BaseItemBinderPro
import com.ym.chat.item.*
import com.ym.chat.utils.MsgType.MESSAGETYPE_AT
import com.ym.chat.utils.MsgType.MESSAGETYPE_CONTACT
import com.ym.chat.utils.MsgType.MESSAGETYPE_FILE
import com.ym.chat.utils.MsgType.MESSAGETYPE_NOTICE
import com.ym.chat.utils.MsgType.MESSAGETYPE_PICTURE
import com.ym.chat.utils.MsgType.MESSAGETYPE_TEXT
import com.ym.chat.utils.MsgType.MESSAGETYPE_TIME
import com.ym.chat.utils.MsgType.MESSAGETYPE_UNREAD
import com.ym.chat.utils.MsgType.MESSAGETYPE_VIDEO
import com.ym.chat.utils.MsgType.MESSAGETYPE_VOICE
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique

/**
 * 聊天记录
 */
@Entity
data class ChatMessageBean(
    @Id
    var dbId: Long = 0,
    var command: Int = 0,
    var chatType: String = "",
    var cmd: Int = 0,
    var content: String = "",
    @Index
    var createTime: Long = 0,
    var from: String = "",
    var groupId: String = "",
    @Index
    @Unique
    var id: String = "",
    var fromName: String = "",//发送人名字
    var msgType: String = "",
    var serverReceiveTime: String = "",
    var fromHead: String = "",//发送人头像
    //对方ID
    var to: String = "",
    //修改消息id
    var parentMessageId: String = "",
    //操作类型
    var operationType: String = "",
    //修改消息editId
    var editId: String = ""

) : BaseBinderAdapterPro.Types {

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


    //远程文件地址（只对发送方有效）
    var servicePath: String = ""

    //媒体消息使用，媒体文件是否上传成功
    var isUpload = false

    //消息客户端的uuid
    @Unique
    var uuid: String = ""

    //当前模式，true为编辑模式，false为正常模式
    @Transient
    var isEditMode: Boolean = false

    @Transient
    var isSel: Boolean = false

    @Transient
    var replayParentMsg: ChatMessageBean? = null

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
                MESSAGETYPE_FILE -> {
                    //文件消息
                    return if (dir == 0) {
                        //文本消息-收到
                        ChatFileLeft::class.java
                    } else {
                        //文本消息-发送
                        ChatFileRight::class.java
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
                MESSAGETYPE_UNREAD -> {
                    //消息未读
                    return ChatUnReadItem::class.java
                }
                MESSAGETYPE_CONTACT -> {
                    //名片消息
                    return if (dir == 0) {
                        ChatContactCardLeft::class.java
                    } else {
                        ChatContactCardRight::class.java
                    }
                }
                else -> {
                    //未知消息
                    return if (dir == 0) {
                        ChatUndefinedLeft::class.java
                    } else {
                        ChatUndefinedRight::class.java
                    }
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

    @Transient
    var isPlaying: Boolean = false

    //是否高亮
    @Transient
    var isHighlight: Boolean = false

    @Transient
    var audioDownLoadProgress = 0

    @Transient
    var fileUploadProgress = 0 //文件上传进度

    @Transient
    var downloadProcess: Float = 0f//下载进度
}