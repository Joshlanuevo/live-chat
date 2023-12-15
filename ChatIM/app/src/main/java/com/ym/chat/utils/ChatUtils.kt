package com.ym.chat.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.*
import com.blankj.utilcode.util.Utils
import com.google.common.reflect.TypeToken
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.launchError
import com.ym.base.ext.logE
import com.ym.base.ext.toFile
import com.ym.base.ext.toast
import com.ym.base.rxhttp.RxHttpConfig
import com.ym.base.util.save.MMKVUtils
import com.ym.base.util.save.MMKVUtils.getUser
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.loadImgSquare
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.db.ChatDao
import com.ym.chat.db.ChatDao.getFriendDb
import com.ym.chat.db.ChatDao.getGroupDb
import com.ym.chat.item.chatlistener.ChatPopClickType
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.rxhttp.FriendRepository
import com.ym.chat.rxhttp.UserRepository
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.VideoPlayActivity
import com.ym.chat.utils.audio.AudioPlayManager
import com.ym.chat.utils.audio.IAudioPlayListener
import com.ym.chat.viewmodel.FriendViewModel
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener
import io.objectbox.exception.UniqueViolationException
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import org.json.JSONObject
import razerdp.basepopup.QuickPopupBuilder
import razerdp.basepopup.QuickPopupConfig
import razerdp.widget.QuickPopup
import rxhttp.RxHttp
import rxhttp.toFlow
import java.io.File
import java.util.*

/**
 * 聊天item播放
 */
object ChatUtils {

    var mLastPlayBean: ChatMessageBean? = null

    @Synchronized
    fun playAudio(
        bean: ChatMessageBean,
        position: Int,
        onStart: (() -> Unit),
        onStop: (() -> Unit),
        onComplete: (() -> Unit),
    ) {
        //停止旧的
        mLastPlayBean?.let { last ->
            if (last.isPlaying) {//正在播放
                AudioPlayManager.getInstance().stopPlay()
                if (last.id == bean.id) {//如果是点击的同一个id，则后续不需要处理
                    return
                }
            }
        }
        mLastPlayBean = bean
        //播放新的
        downLoadAudio(bean) { f ->
            if (f == null) {
                "下载语音失败，请重试".toast()
            } else {
                val uri = Uri.fromFile(f)
                AudioPlayManager.getInstance()
                    .startPlay(Utils.getApp(), uri, object : IAudioPlayListener {
                        override fun onStart(var1: Uri) {
                            onStart.invoke()
                        }

                        override fun onStop(var1: Uri) {
                            onStop.invoke()
                        }

                        override fun onComplete(var1: Uri) {
                            onComplete.invoke()
                        }
                    })
            }
        }
    }

    private val mFileDir = PathConfig.TEMP_CACHE_DIR
    private var mDisposable: Disposable? = null
    private fun downLoadAudio(bean: ChatMessageBean, suc: (file: File?) -> Unit) {
        val downLoadName = EncryptUtils.encryptMD5ToString(bean.mAudioUrl)
        val sucName = "$downLoadName.amr"
        if (File(mFileDir, sucName).exists()) {//已经下载好了的
            suc.invoke(File(mFileDir, sucName))
            return
        } else {//执行下载
            //RxHttp 下载
            val tempFile = File(mFileDir, downLoadName)
            val downSize = if (tempFile.exists()) tempFile.length() else 0L
            launchError {
                RxHttp.get(bean.mAudioUrl)
                    .setOkClient(RxHttpConfig.getOkHttpClient().build()) //不要加log打印，否则文件太大要OOM
                    .setRangeHeader(downSize) //设置开始下载位置，结束位置默认为文件末尾,如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                    .toFlow(tempFile.path) { progress ->
                        //下载进度回调,0-100，仅在进度有更新时才会回调
                        bean.audioDownLoadProgress = progress.progress //当前进度 0-100
                    }.catch {

                    }.collect {
                        bean.audioDownLoadProgress = 100
                        //下载成功，处理相关逻辑
                        FileUtils.rename(tempFile, sucName)
                        suc.invoke(File(mFileDir, sucName))
                    }
            }
        }
    }

    @Synchronized
    fun playVideo(context: Context, bean: ChatMessageBean) {
        //停止旧的音频播放
        mLastPlayBean?.let { last ->
            if (last.isPlaying) {//正在播放
                AudioPlayManager.getInstance().stopPlay()
                if (last.msgId == bean.msgId) {//如果是点击的同一个id，则后续不需要处理
                    return
                }
            }
        }
        var url = ""
        if (!TextUtils.isEmpty(bean.videoInfo?.url)) {
            url = bean.videoInfo?.url ?: ""
        } else if (!TextUtils.isEmpty(bean.localPath)) {
            url = bean.localPath.toFile()?.path ?: ""
        }
        VideoPlayActivity.startActivity(context, url)
    }

    @SuppressLint("StaticFieldLeak")
    private var mMorePopUpWindow: QuickPopup? = null

    /**
     * 显示回复消息
     */
    fun showRelyMsg(
        context: Context,
        msg: ChatMessageBean,
        ivReplyPreview: ImageView,
        tvReplyName: TextView,
        tvReplyContent: TextView
    ) {

        //显示昵称
        if (msg.dir == 1) {
            //自己发的消息
            tvReplyName.text = getUser()?.name
        } else {
            //收到的消息
            if (msg.chatType == ChatType.CHAT_TYPE_GROUP) {
                val memberBean = getGroupDb().getMemberInGroup(msg.from, msg.groupId)
                if (memberBean != null) {
                    tvReplyName.text = memberBean.name
                }
            } else if (msg.chatType == ChatType.CHAT_TYPE_FRIEND) {
                val friend = getFriendDb().getFriendById(msg.from)
                if (friend != null) {
                    tvReplyName.text = friend.name
                }
            }
        }

        when (msg.msgType) {
            MsgType.MESSAGETYPE_PICTURE -> {

                //图片消息
                ivReplyPreview.setVisibility(View.VISIBLE)
                val (_, _, url) = GsonUtils.fromJson(msg.content, ImageBean::class.java)
                //                tvReplyName.setText(msg.getN());
//                tvReplyContent.setText(msg.getT());
                ivReplyPreview.loadImgSquare(url)
                tvReplyContent.text = getString(R.string.tupian)
            }

            MsgType.MESSAGETYPE_VOICE -> {

                //语音消息
                ivReplyPreview.setVisibility(View.GONE)
                tvReplyContent.text = getString(R.string.语音消息)
            }

            MsgType.MESSAGETYPE_VIDEO -> {

                //视频消息
                ivReplyPreview.setVisibility(View.VISIBLE)
                val imageMsg = GsonUtils.fromJson(msg.content, VideoMsgBean::class.java)
                ivReplyPreview.loadImgSquare(imageMsg.coverUrl)
                tvReplyContent.text = getString(R.string.视频)
            }

            MsgType.MESSAGETYPE_REPLY -> {

                //回复消息
                ivReplyPreview.setVisibility(View.GONE)
                tvReplyContent.text = getString(R.string.回复消息)
            }

            MsgType.MESSAGETYPE_TEXT, MsgType.MESSAGETYPE_AT -> {

                //普通文字消息
                ivReplyPreview.setVisibility(View.GONE)
                tvReplyContent.text = AtUserHelper.parseAtUserLinkJx(
                    msg.content,
                    ContextCompat.getColor(context, R.color.color_at),
                    object : AtUserLinkOnClickListener {
                        override fun ulrLinkClick(str: String?) {
                        }

                        override fun atUserClick(str: String?) {
                        }

                        override fun phoneClick(str: String?) {
                        }
                    })
            }

            MsgType.MESSAGETYPE_FILE -> {

                //文件消息
                val fileMsg = GsonUtils.fromJson(msg.content, FileMsgBean::class.java)
                ivReplyPreview.setVisibility(View.GONE)
                tvReplyContent.text = fileMsg.name
            }
        }
    }

    /**
     * 显示更多菜单
     */
    fun showPopupWindow(
        context: Context,
        view: View,
        data: ChatMessageBean,
        position: Int,
        onClickListener: OnChatItemListener?,
        gravityType: Int = 0,
        offsetX: Float = -20.0f,
        offsetY: Int = 0
    ) {
        mMorePopUpWindow = QuickPopupBuilder.with(context)
            .contentView(R.layout.dialog_chat_more)
            .config(
                QuickPopupConfig()
                    .offsetX(SizeUtils.dp2px(offsetX))
                    .offsetY(offsetY)
                    .gravity(gravityType)
                    .backgroundColor(Color.TRANSPARENT)
                    .withClick(R.id.llSc) {
                        //收藏
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.Collect
                        )
                        mMorePopUpWindow?.dismiss()
                    }.withClick(R.id.llXh) {
                        //远程销毁
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.Destory
                        )
                        mMorePopUpWindow?.dismiss()
                    }.withClick(R.id.llZf) {
                        //转发
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.Forward
                        )
                        mMorePopUpWindow?.dismiss()
                    }.withClick(R.id.llBj) {
                        //编辑
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.Edit
                        )
                        mMorePopUpWindow?.dismiss()
                    }.withClick(R.id.llCopy) {
                        //复制
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.Copy
                        )
                        mMorePopUpWindow?.dismiss()
                    }.withClick(R.id.llHf) {
                        //回复
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.Reply
                        )
                        mMorePopUpWindow?.dismiss()
                    }.withClick(R.id.llZd) {
                        //置顶
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.Top
                        )
                        mMorePopUpWindow?.dismiss()
                    }.withClick(R.id.llAddPhiz) {
                        //添加表情
                        onClickListener?.onPopMenuClickListener(
                            data,
                            position,
                            ChatPopClickType.AddPhiz
                        )
                        mMorePopUpWindow?.dismiss()
                    }
            ).build()
        mMorePopUpWindow?.showPopupWindow(view)

        //只有文本消息，@消息才能编辑
//        if ((data.msgType == MsgType.MESSAGETYPE_AT || data.msgType == MsgType.MESSAGETYPE_TEXT) && data.dir == 1) {
//            mMorePopUpWindow?.findViewById<LinearLayout>(R.id.llBj)?.visible()
//        } else {
//            mMorePopUpWindow?.findViewById<LinearLayout>(R.id.llBj)?.gone()
//        }

        //只有文本消息，@消息 文本消息，成员权限 群主 管理员 才能复制
        var vCopy = mMorePopUpWindow?.findViewById<LinearLayout>(R.id.llCopy)
        if ((data.msgType == MsgType.MESSAGETYPE_AT || data.msgType == MsgType.MESSAGETYPE_TEXT)) {

            //我现在是群主或者管理员
            vCopy?.visible()

//            /**只有管理员 群主 才有复制功能*/
//            var memberUserGroup =
//                MMKVUtils.getUser()?.id?.let {
//                    ChatDao.getGroupDb().getMemberInGroup(it, data.groupId)
//                }
//            memberUserGroup.let {
//                if (it?.role == "Normal") {//我现在是普通成员
//                    //不能复制群主跟管理员的消息
//                    vCopy?.gone()
//                } else {
//                    //我现在是群主或者管理员
//                    vCopy?.visible()
//                }
//            }
        } else {
            vCopy?.gone()
        }

        var vXh = mMorePopUpWindow?.findViewById<LinearLayout>(R.id.llXh)
        var vZd = mMorePopUpWindow?.findViewById<LinearLayout>(R.id.llZd)
        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
            //群聊，只有群主或者管理员才显示远程销毁
            val isManager = ChatDao.getGroupDb()
                .getGroupInfoById(data.groupId)?.roleType?.lowercase() != "Normal".lowercase()
            if (isManager) {
                vZd?.visible()
            } else {
                vZd?.gone()
            }
        }

        if (data.dir == 1) {
            //可以删除自己的消息
            vXh?.visible()
        } else {
            vXh?.gone()
        }

        var vAddPhiz = mMorePopUpWindow?.findViewById<LinearLayout>(R.id.llAddPhiz)
        if (data.msgType == MsgType.MESSAGETYPE_PICTURE) {
            try {
                vAddPhiz?.visible()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 检索数据
     */
    fun searchAtMem(
        datas: MutableList<GroupMemberBean>,
        keyWord: String
    ): List<GroupMemberBean> {
        return datas.filter { f ->
            var username = f?.username ?: ""
            var name = f?.name ?: ""
            username.lowercase(Locale.getDefault())
                .contains(keyWord.lowercase(Locale.getDefault())) || name.lowercase(
                Locale.getDefault()
            ).contains(keyWord.lowercase(Locale.getDefault()))
        }
    }

    /**
     * 解密
     */
    private fun decodeContent(key: String?): String {
        return String(
            AesUtils.decode(
                MMKVUtils.getUser()?.id,
                MMKVUtils.getUser()?.code,
                key?.toByteArray()
            )
        )
    }

    /**
     * 处理消息
     */
    fun processMsg(message: String, isHistoryMsg: Boolean = false) {
        if (!TextUtils.isEmpty(message)) {
            var jsonObject: JSONObject? = null
            try {
                jsonObject = JSONObject(message)
            } catch (e: Exception) {
                "消息格式错误${message}".logE()
                e.printStackTrace()
            }
            jsonObject?.let {
                val command = jsonObject.optInt("command")
                try {
                    when (command) {
                        CommandType.CHAT, CommandType.CHAT_REPLY -> {
                            //聊天消息
                            val data = jsonObject.optString("data")
                            val chatMsg = GsonUtils.fromJson(data, ChatMessageBean::class.java)
                            //给自己发的消息
                            if (chatMsg.from == MMKVUtils.getUser()?.id) {
                                val toId =
                                    if (chatMsg.chatType == ChatType.CHAT_TYPE_FRIEND) chatMsg.to else chatMsg.groupId
                                if (chatMsg.from == chatMsg.to) {
//                        {"command":11,"createTime":1643004345060,"data":{"chatType":"Friend","content":"不会","createTime":1643004345060,"from":"2881308859587452928","id":"2898304818517590016","msgType":"Text","to":"2881308859587452928"}}
                                    //群发的消息，不需要处理
                                    return
                                }

                                saveMsg(chatMsg, isSelfMsg = true)
                            } else {
                                saveMsg(chatMsg)
                            }
                        }

                        CommandType.MSG_EDIT -> {
                            //聊天消息
                            val data = jsonObject.optString("data")
                            val chatMsg = GsonUtils.fromJson(data, ChatMessageBean::class.java)
                            if (!chatMsg.parentMessageId.isNullOrEmpty()) {
                                ChatDao.getChatMsgDb()
                                    .queryMsgByIdBeforeDate(chatMsg.parentMessageId)
                                    .let {
                                        if (it != null) {
                                            it.content = chatMsg.content
                                            it.editId = chatMsg.id
                                            it.msgType = chatMsg.msgType
                                            it.operationType = chatMsg.operationType
                                            saveEditMsg(it, chatMsg.from == MMKVUtils.getUser()?.id)
                                        }
                                    }
                            }


                        }
//                    CommandType.OTHER_LOGIN -> {
//                        //账号被踢
//                        val jsonData = jsonObject.optJSONObject("data")
//                        val type = jsonData.optString("type")
//                        MMKVUtils.clearUserInfo()
//                        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
//                        ImCache.KillOutType = type//记录被踢原因
//                    }
                        CommandType.OFFLINE_MSG -> {
                            //离线消息
                            val data = jsonObject.optJSONObject("data")
                            //好友离线消息
                            val friendMsg = data.optJSONObject("friends")
                            //群聊离线消息
                            val groupsMsg = data.optJSONObject("groups")

                            try {
                                //好友离线消息
                                val names = friendMsg.names()
                                if (names != null && names.length() > 0) {
                                    for (index in 0 until names.length()) {
                                        val f = names.getString(index)
                                        val msgListStr = friendMsg.optString(f)
                                        val type =
                                            object :
                                                TypeToken<MutableList<ChatMessageBean>>() {}.type
                                        val msgList =
                                            GsonUtils.fromJson<MutableList<ChatMessageBean>>(
                                                msgListStr,
                                                type
                                            )
                                        msgList.forEach { chatMsg ->
                                            saveMsg(chatMsg)
                                        }
                                    }
                                }

                                //群离线消息
                                val groupNames = groupsMsg.names()
                                if (groupNames != null && groupNames.length() > 0) {
                                    for (index in 0 until groupNames.length()) {
                                        val f = groupNames.getString(index)
                                        val msgListStr = groupsMsg.optString(f)
                                        val type =
                                            object :
                                                TypeToken<MutableList<ChatMessageBean>>() {}.type
                                        val msgList =
                                            GsonUtils.fromJson<MutableList<ChatMessageBean>>(
                                                msgListStr,
                                                type
                                            )
                                        msgList.forEach { chatMsg ->
                                            saveMsg(chatMsg)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                "数据解析异常=${jsonObject.toString()}".logE()
                            }
                        }

                        CommandType.JOIN_GROUP -> {
                            //新建入群
                            if (ImCache.isUpdateNotifyMsg)
                                ChatDao.syncFriendAndGroupToLocal(
                                    isSyncFriend = false,
                                    isSyncGroup = true,
                                    isEventUpdateConver = true
                                )
                            val jsonData = jsonObject.optJSONObject("data")
                            val createTime = jsonObject.optLong("createTime")
                            val id = jsonObject.optString("id")
                            val createTime11 = jsonData.optLong("createTime")
                            val groupIdStr = jsonData.optString("groupId")
                            val userId = jsonData.optString("memberId")
                            val nick = jsonData.optString("name")
                            var groupName = ""
                            if (!jsonData.isNull("groupName"))
                                groupName = jsonData.optString("groupName")

                            "-----${createTime}---${createTime11}-----\n${jsonData.toString()}".logE()
                            //生成提示语
                            if (userId != MMKVUtils.getUser()?.id) {
                                //非自己
                                //生成会话列表数据
                                ChatDao.getConversationDb().saveGroupConversation(
                                    groupIdStr,
                                    "${nick}创建「${groupName}」群",
                                    MsgType.MESSAGETYPE_TEXT
                                )
//                                createGroupNotice(groupIdStr, "您被${nick}邀请入群", id, createTime)
                            } else {
                                var groupInfo = ChatDao.getGroupDb().getGroupInfoById(groupIdStr)
                                if (groupInfo != null) {
                                    //说明这个群是移动端操作的，不需要处理
                                } else {
                                    //说明这个群是pc操作的
                                }
                            }
                        }

                        CommandType.DEL_ALL_MSG -> {
                            //远程销毁所有消息（好友）
                            val jsonData = jsonObject.optJSONObject("data")
                            val chatType = jsonData.optString("chatType")
                            if (chatType == ChatType.CHAT_TYPE_FRIEND) {
                                val from = jsonData.optString("from")
                                val to = jsonData.optString("to")

                                var delTarget = ""
                                if (from == MMKVUtils.getUser()?.id) {
                                    //消息是自己发送给自己的
                                    delTarget = to
                                } else {
                                    delTarget = from
                                }
                                //删除本地所有聊天数据
                                ChatDao.getChatMsgDb().delMsgListByFriendId(delTarget)
                                //更新本地会话数据
                                ChatDao.getConversationDb()
                                    .updateMsgByTargtId(
                                        delTarget,
                                        getString(R.string.yuanchengxiaoxiyibeixiaohui)
                                    )

                                LiveEventBus.get(EventKeys.DEL_MSG_ALL, String::class.java)
                                    .post(delTarget)
                            }
                        }

                        CommandType.DELETE_MSG -> {
                            //远程销毁单条消息
                            val jsonData = jsonObject.optJSONObject("data")
                            val id = jsonData.optString("id")
                            ChatDao.getChatMsgDb().delMessageByServiceId(id)

                            LiveEventBus.get(EventKeys.DEL_MSG_ONE, String::class.java)
                                .post(id)
                        }
//                        CommandType.NOTIFICATION_COUNT -> {
//                            //系统通知消息已读回执
//                            val data = jsonObject.optJSONObject("data")
//                            if (data != null) {
//                                val chatType = data.optString("chatType")
//                                if (chatType == "SystemNotify") {
//                                    //系统通知消息已读
//                                    val messageIdList = data.optJSONArray("messageIdList")
//                                    if (messageIdList != null && messageIdList.length() > 0) {
//                                        for (i in 0 until messageIdList.length()) {
//                                            val msgId = messageIdList.optString(i)
//                                            ChatDao.getNotifyDb().updateRead(msgId, 1)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        CommandType.SYSTEM_MSG -> {
//                            //系统通知
//                            ChatDao.getNotifyDb().saveSystemNotifyMsgByJson(jsonObject, 1)
//                        }
//                        CommandType.DEVICE_LOGIN_VERIFICATION -> {
//                            //新设备验证码
//                            ChatDao.getNotifyDb().saveNotifyMsgByJson(jsonObject)
//                        }
//                        CommandType.SYSTEM_FEEDBACK_MSG -> {
//                            //意见反馈系统通知
//                            ChatDao.getNotifyDb().saveSystemNotifyMsgByJson(jsonObject)
//                        }

                        CommandType.DELETE_NOTIFY_MSG -> {
                            //多端同步删除系统通知消息
                            try {
//                        {"command":44,"createTime":1650881923733,"data":{"messageIdList":["3025982856533532673"]},"id":"3033640594243854336"}
                                val jsonData = jsonObject.optJSONObject("data")
                                val messageIdList = jsonData.optJSONArray("messageIdList")
                                if (messageIdList != null && messageIdList.length() > 0) {
                                    for (index in 0 until messageIdList.length()) {
                                        var notifyId = messageIdList[index]
                                        if (notifyId is String)
                                            ChatDao.getNotifyDb().delNotifyMsgById(notifyId)
                                    }
                                    //发广播通知主页更新
                                    LiveEventBus.get(
                                        EventKeys.DELETE_NOTIFY_MSG,
                                        String::class.java
                                    )
                                        .post("")
                                }
                            } catch (e: Exception) {
                            }
                        }

                        CommandType.NOTIFICATION_DEL_CHAT_MSG -> {
                            //删除会话ws推送
//                            try {
//                                val jsonData = jsonObject.optJSONObject("data")
//                                val memberId = jsonData.optString("memberId")
//                                val friendMemberId = jsonData.optString("friendMemberId")
//                                val groupId = jsonData.optString("groupId")
//                                when (jsonData.optString("type")) {
//                                    "Friend" -> {
//                                        ChatDao.getConversationDb()
//                                            .delConverByTargtId(friendMemberId)
//                                    }
//                                    "Group" -> {
//                                        ChatDao.getConversationDb().delConverByTargtId(groupId)
//                                    }
//                                }
//                            } catch (e: Exception) {
//                            }
                        }
                    }
                } catch (e: Exception) {
                    "数据解析异常=${jsonObject.toString()}".logE()
                }
            }
        }
    }

    fun getString(resId: Int): String {
        return ActivityUtils.getTopActivity().getString(resId)
    }

    /**
     * 处理群事件
     */
    fun processGroupEvent(
        groupAction: GroupActionBean,
        msgId: String,
        msgCreateTime: Long
    ) {
        when (groupAction.messageType) {
            MsgType.TransferGroup -> {
                //群主转让群
                try {
                    //生成提示语
                    val content =
                        if (groupAction.setValue == MMKVUtils.getUser()?.id) {
                            //转给的群主是自己
                            ChatUtils.getString(R.string.群主已转让给我)
                        } else {
                            //非自己的提示
                            String.format(
                                ChatUtils.getString(R.string.群主已转让给我),
                                groupAction.targetName
                            )
                        }
                    ChatUtils.createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)

                    //发广播群主转让
                    LiveEventBus.get(EventKeys.TRANSFER_GROUP, String::class.java)
                        .post(groupAction.groupId)
                    //发广播更新群列表
                    LiveEventBus.get(EventKeys.DELETE_GROUP, String::class.java)
                        .post(groupAction.groupId)
                } catch (e: Exception) {
                }
            }

            MsgType.HeaderUrl -> {
                //修改了群头像
                ChatDao.getGroupDb().updateIconById(groupAction.groupId, groupAction.setValue)
                var operatorName = groupAction.operatorName
                if (operatorName.isNullOrEmpty()) {
                    operatorName = ChatUtils.getString(R.string.系统通知1)
                }

                //生成提示
                val content = if (groupAction.operatorId == MMKVUtils.getUser()?.id) {
                    //管理员自己
                    ChatUtils.getString(R.string.您修改了群头像)
                } else {
                    String.format(ChatUtils.getString(R.string.修改了群头像), operatorName)
                }
                createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
            }

            MsgType.GroupModifyName -> {
                //修改了群名称，需要同步会话列表&好友列表数据
                ChatDao.getGroupDb()
                    .updateNameById(groupAction.groupId, groupAction.setValue)

                var operatorName = groupAction.operatorName
                if (operatorName.isNullOrEmpty()) {
                    operatorName = ChatUtils.getString(R.string.系统通知1)
                }

                //生成提示
                val content = if (groupAction.operatorId == MMKVUtils.getUser()?.id) {
                    //管理员自己
                    String.format(
                        ChatUtils.getString(R.string.您修改了群名称为),
                        groupAction.setValue
                    )
                } else {
                    //吃瓜群众，跟我没啥关系
                    "${operatorName}修改了群名称为\"${groupAction.setValue}\""
                }
                createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
            }

            MsgType.ALLOWSPEAK -> {
                //更新本地群组的禁言状态
                ChatDao.getGroupDb()
                    .updateGroupMute(groupAction.groupId, groupAction.setValue)

                var operatorName = groupAction.operatorName
                if (operatorName.isNullOrEmpty()) {
                    operatorName = ChatUtils.getString(R.string.系统通知1)
                }

                val isMute = groupAction.setValue == "N"
                if (isMute) {
                    val content = if (groupAction.operatorId == MMKVUtils.getUser()?.id) {
                        //管理员自己
                        "您开启了本群禁言"
                    } else {
                        //吃瓜群众，跟我没啥关系
                        String.format(getString(R.string.开启了本群禁言), operatorName)
                    }
//                    createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
                } else {
                    val content = if (groupAction.operatorId == MMKVUtils.getUser()?.id) {
                        //管理员自己
                        getString(R.string.您取消了本群禁言)
                    } else {
                        //吃瓜群众，跟我没啥关系
                        String.format(getString(R.string.取消了本群禁言设置), operatorName)
                    }
//                    createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
                }
            }

            MsgType.SetMemberRole -> {
                var operatorName = groupAction.operatorName
                if (operatorName.isNullOrEmpty()) {//系统后台设置
                    val content = if (groupAction.setValue == MMKVUtils.getUser()?.id) {
                        getString(R.string.您被设为了管理员)
                    } else {
                        String.format(getString(R.string.被设为了管理员), groupAction.targetName)
                    }
//                    createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
                } else {//前端app或者pc端设置
                    //设置管理员，生成提示
                    val content = if (groupAction?.operatorId == MMKVUtils.getUser()?.id) {
                        //管理员自己
                        "您将${groupAction.targetName}设为了管理员"
                    } else if (groupAction.setValue == MMKVUtils.getUser()?.id) {
                        //被设置为管理员的我
                        if (ImCache.isUpdateNotifyMsg)
                            ChatDao.syncFriendAndGroupToLocal(
                                isSyncFriend = false,
                                isSyncGroup = true,
                                isEventUpdateConver = false
                            )
                        String.format(getString(R.string.您被设为了管理员1), operatorName)
                    } else {
                        //吃瓜群众，跟我没啥关系
                        String.format(
                            getString(R.string.设为了管理),
                            groupAction.targetName,
                            operatorName
                        )
                    }
//                    createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
                }
            }

            MsgType.CancelMemberRole -> {
                var operatorName = groupAction.operatorName
                if (operatorName.isNullOrEmpty()) {//系统后台设置
                    val content = if (groupAction.setValue == MMKVUtils.getUser()?.id) {
                        getString(R.string.您被移除了管理员权限)
                    } else {
                        String.format(
                            getString(R.string.被移除了管理员权限),
                            groupAction.targetName
                        )
                    }
//                    createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
                } else {//前端app或者pc端设置
                    //取消管理员
                    val content = if (groupAction.operatorId == MMKVUtils.getUser()?.id) {
                        //管理员自己
                        String.format(
                            getString(R.string.被您取消了管理员权限),
                            groupAction.targetName
                        )
                    } else if (groupAction.setValue == MMKVUtils.getUser()?.id) {
                        //被取消管理员的是自己
                        if (ImCache.isUpdateNotifyMsg)
                            ChatDao.syncFriendAndGroupToLocal(
                                isSyncFriend = false,
                                isSyncGroup = true,
                                isEventUpdateConver = false
                            )
                        String.format(getString(R.string.移除了管理员权限), operatorName)
                    } else {
                        //吃瓜群众，跟我没啥关系
                        String.format(
                            getString(R.string.被移除了管理员权限1),
                            groupAction.targetName
                        )
                    }
//                    createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
                }
            }

            MsgType.Notice -> {
                //修改群公告
                ChatDao.getGroupDb()
                    .updateNoticeById(groupAction.groupId, groupAction.setValue)

                var operatorName = groupAction.operatorName
                if (operatorName.isNullOrEmpty()) {
                    operatorName = getString(R.string.系统通知1)
                }

                //生成提示
                val content = if (groupAction.operatorId == MMKVUtils.getUser()?.id) {
                    //管理员自己
                    "${getString(R.string.您设置了群公告)}\"${groupAction.setValue}\""
                } else {
                    //吃瓜群众，跟我没啥关系
                    "${operatorName}${getString(R.string.设置了群公告为)}\"${groupAction.setValue}\""
                }
                createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
            }

            MsgType.MemberAllowSpeak -> {
                var operatorName = groupAction.operatorName
                if (operatorName.isNullOrEmpty()) {
                    operatorName = getString(R.string.系统通知1)
                }

                if (groupAction.operatorId != MMKVUtils.getUser()?.id) {
                    //如果操作人不是自己才处理
                    ChatDao.getGroupDb().updateGroupMemberMute(
                        groupAction.groupId,
                        groupAction.targetId ?: "",
                        groupAction.setValue
                    )
                }
                //生成提示
                val content = if (groupAction?.operatorId == MMKVUtils.getUser()?.id) {
                    //管理员自己
                    if (groupAction.setValue == "N") {
                        "您把\"${groupAction.targetName ?: ""}\"设置了禁言"
                    } else {
                        "您把\"${groupAction.targetName ?: ""}\"取消了禁言"
                    }
                } else {
                    if (groupAction.setValue == "N") {
                        "\"${operatorName}\"把\"${groupAction.targetName ?: ""}\"设置了禁言"
                    } else {
                        "\"${operatorName}\"把\"${groupAction.targetName ?: ""}\"取消了禁言"
                    }
                }
//                createGroupNotice(groupAction.groupId, content, msgId, msgCreateTime)
            }
        }
    }


    /**
     * 获取好友申请通知
     */
    fun getFriendNotifyList() {
        GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            try {
                val result = FriendRepository.getFriendNotifyInfo()
                if (result.code == 200) {
                    result.data?.records?.forEach {
                        val msgReadState = if (it.status == "Pending") {
                            0
                        } else {
                            1
                        }

                        ChatDao.getNotifyDb()
                            .saveNotifyFriendMsg(
                                it.id,
                                it.friendMemberId,
                                it.memberId,
                                it.status,
                                msgReadState,
                                it.name,
                                it.headUrl, time = it.createTime.toLong()
                            )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 生成提示信息
     */
    fun createGroupNotice(
        groupid: String,
        contentStr: String,
        msgId: String,
        msgCreateTime: Long
    ) {
        val bean = ChatMessageBean().apply {
            to = ""
            from = ""
            sendState = 1
            groupId = groupid
            chatType = ChatType.CHAT_TYPE_GROUP
            msgType = MsgType.MESSAGETYPE_NOTICE
            createTime = msgCreateTime
            content = contentStr
            id = msgId
        }
        try {
            ChatDao.getChatMsgDb().saveChatMsg(bean)
            LiveEventBus.get(EventKeys.MSG_NEW, ChatMessageBean::class.java).post(bean)
        } catch (e: Exception) {
            e.printStackTrace()
            "重复消息:${contentStr}".logE()
        }
    }

    /**
     * 保存消息
     */
    fun saveMsg(
        chatMsg: ChatMessageBean,
        isSentEvent: Boolean = true,
        isSelfMsg: Boolean = false//是否自己的消息
    ) {

        if (isSelfMsg) {
            chatMsg.dir = 1
            chatMsg.sendState = 1
        }

        //1、保存消息到数据库
        try {
            ChatDao.getChatMsgDb().saveChatMsg(chatMsg)

            //2、生成或更新会话表
            if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
                ChatDao.getConversationDb()
                    .saveGroupConversation(
                        chatMsg.groupId,
                        chatMsg.content,
                        chatMsg.msgType,
                        fromId = if (isSelfMsg) MMKVUtils.getUser()?.id ?: "" else chatMsg.from,
                        dir = if (isSelfMsg) 1 else 0,
                        msgTime = chatMsg.createTime
                    )
            } else {
                ChatDao.getConversationDb()
                    .saveFriendConversation(
                        if (isSelfMsg) chatMsg.to else chatMsg.from,
                        chatMsg.content,
                        chatMsg.msgType,
                        if (isSelfMsg) 1 else 0,
                        msgTime = chatMsg.createTime
                    )
            }

            if (isSentEvent && ImCache.isUpdateNotifyMsg) {
                LiveEventBus.get(EventKeys.MSG_NEW, ChatMessageBean::class.java).post(chatMsg)
            }

            playAudio(chatMsg, isSelfMsg)

        } catch (e: UniqueViolationException) {
            "重复消息:${chatMsg}".logE()
        }
    }

    /**
     * 保存编辑消息
     */
    fun saveEditMsg(
        chatMsg: ChatMessageBean,
        isSelfMsg: Boolean
    ) {
        //1、保存消息到数据库
        try {
            ChatDao.getChatMsgDb().saveChatMsg(chatMsg)
            LiveEventBus.get(EventKeys.MSG_EDIT, ChatMessageBean::class.java).post(chatMsg)
            if (chatMsg.dbId == ChatDao.getChatMsgDb().queryMsgMaxId()) {//最后一条消息刷新会话
                //2、生成或更新会话表
                if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
                    ChatDao.getConversationDb()
                        .saveGroupConversation(
                            chatMsg.groupId,
                            chatMsg.content,
                            chatMsg.msgType,
                            if (isSelfMsg) 1 else 0,
                            true
                        )
                } else {
                    ChatDao.getConversationDb()
                        .saveFriendConversation(
                            if (isSelfMsg) chatMsg.to else chatMsg.from,
                            chatMsg.content,
                            chatMsg.msgType,
                            if (isSelfMsg) 1 else 0,
                            true
                        )
                }
            }
            playAudio(chatMsg, isSelfMsg)
        } catch (e: UniqueViolationException) {
            "重复消息:${chatMsg}".logE()
        }
    }

    /**
     * 播放Mp3
     */
    private fun playAudio(
        chatMsg: ChatMessageBean,
        isSelfMsg: Boolean = false
    ) {
        if (!isSelfMsg) {

//            if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP || chatMsg.chatType == ChatType.CHAT_TYPE_FRIEND) {
//                Mp3Player.playMusic()
//                NotificationUtils.showNotification(Utils.getApp(), chatMsg)
//            } else {
//                Mp3Player.playMusic()
//            }

            if (chatMsg.chatType == ChatType.CHAT_TYPE_GROUP) {
                val noticeState = ChatDao.getGroupDb()
                    .isMessageNotice(chatMsg.groupId)
                if (!noticeState) {
                    //没有设置免打扰，需要播放声音
                    if (AppUtils.isAppForeground() && ImCache.isUpdateNotifyMsg) {
                        Mp3Player.playMusic()
                    }
                    NotificationUtils.showNotification(Utils.getApp(), chatMsg)
                }
            } else if (chatMsg.chatType == ChatType.CHAT_TYPE_FRIEND) {
                if (ChatDao.getFriendDb().isMessageNotice(chatMsg.from)) {
                    //没有设置免打扰，需要播放声音
                    if (AppUtils.isAppForeground() && ImCache.isUpdateNotifyMsg) {
                        Mp3Player.playMusic()
                    }
                    NotificationUtils.showNotification(Utils.getApp(), chatMsg)
                }
            } else {
                Mp3Player.playMusic()
            }
        }
    }

    /**
     * 获取用户信息
     * ws多端同步
     */
    fun getUserInfo(isWsSync: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                UserRepository.getUserInfo(isWsSync)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 高亮背景
     */
    fun highlightBackground(view: View, onFinish: (() -> Unit)) {
        val color = Color.parseColor("#6C96C7")
        GlobalScope.launch(Dispatchers.IO) {
            for (i in 255 downTo 0) {
                withContext(Dispatchers.Main) {
                    view.setBackgroundColor(ColorUtils.setAlphaComponent(color, i))
                }
                delay(7)
            }

            onFinish.invoke()
        }
    }

    fun showTopMsg(ivTop: ImageView, msgId: String) {
        if (ChatActivity.msgTopList != null && ChatActivity.msgTopList.size > 0) {
            var isTopMsg = false
            ChatActivity.msgTopList.forEach {
                if (it.messageId == msgId) {
                    isTopMsg = true
                    return@forEach
                }
            }
            if (isTopMsg) {
                ivTop.visible()
            } else {
                ivTop.gone()
            }
        } else {
            ivTop.gone()
        }
    }


    /**
     * 显示右上角显示popup
     */
    private var mPopUpWindow: QuickPopup? = null
    fun showSettingMorePopupWindow(
        context: Context,
        view: View,
        data: GroupInfoBean,
        gravityType: Int = 0, onClickSettingMore: ((data: GroupInfoBean, type: Int) -> Unit)? = null
    ) {
        mPopUpWindow = QuickPopupBuilder.with(context)
            .contentView(R.layout.popup_chat_setting_more)
            .config(
                QuickPopupConfig()
                    .offsetX(SizeUtils.dp2px(-140.0f))
                    .offsetY(SizeUtils.dp2px(-8.0f))
                    .gravity(gravityType)
                    .backgroundColor(Color.TRANSPARENT)
                    .withClick(R.id.llCkjj) {
                        //查看简介
                        onClickSettingMore?.invoke(data, 0)
                        mPopUpWindow?.dismiss()
                    }.withClick(R.id.llNotNotify) {
                        //静音
                        onClickSettingMore?.invoke(data, 1)
                        mPopUpWindow?.dismiss()
                    }.withClick(R.id.llDelMsg) {
                        //清空历史记录
                        onClickSettingMore?.invoke(data, 2)
                        mPopUpWindow?.dismiss()
                    }.withClick(R.id.llDel) {
                        //退出/删除群聊
                        onClickSettingMore?.invoke(data, 3)
                        mPopUpWindow?.dismiss()
                    }).build()
        mPopUpWindow?.showPopupWindow(view)
        val tvDel = mPopUpWindow?.findViewById<TextView>(R.id.tvDel)
        val tvNotNotify = mPopUpWindow?.findViewById<TextView>(R.id.tvNotNotify)
        val ivJy = mPopUpWindow?.findViewById<ImageView>(R.id.ivJy)
        val llDelMsg = mPopUpWindow?.findViewById<LinearLayout>(R.id.llDelMsg)
        val vDelMsg = mPopUpWindow?.findViewById<View>(R.id.vDelMsg)
        var isMessageNotice = data.messageNotice == "N"
        if (isMessageNotice) {
            tvNotNotify?.text = getString(R.string.quxiaojinyin)
            ivJy?.setImageResource(R.drawable.ic_msg_jy_1)
        }
        if (data.roleType.lowercase() == "owner") {
            tvDel?.text = getString(R.string.解散群组)
        }
        if (data.roleType.lowercase() != "normal") {
            llDelMsg?.visible()
            vDelMsg?.visible()
        }
    }
}