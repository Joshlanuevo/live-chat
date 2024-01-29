package com.ym.chat.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.db.ChatDao
import com.ym.chat.service.ReplyService
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.SplashActivity
import com.ym.chat.utils.ImCache.notifycationMsg
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener
import me.leolin.shortcutbadger.ShortcutBadger

object NotificationUtils {
    private var notificationManager: NotificationManager? = null
    const val channelId = "22"

    /**
     * 显示通知栏
     */
    @SuppressLint("RestrictedApi")
    @Synchronized
    fun showNotification(context: Context, msg: ChatMessageBean) {
        if (msg.chatType != ChatType.CHAT_TYPE_FRIEND && msg.chatType != ChatType.CHAT_TYPE_GROUP) {
            //只显示好友消息、群消息
            return
        }

        var title = ""
        var content = ""
        var headUrl = ""
        var targetId = ""
        if (msg.chatType == ChatType.CHAT_TYPE_FRIEND) {
            ChatDao.getFriendDb().getFriendById(msg.from)?.let {
                //单聊
                title = it.name
//                    subTextStr = ""
                headUrl = it.headUrl
                targetId = it.id
                msg.from.let { sd ->
                    notifycationMsg[sd] =
                        (notifycationMsg[sd] ?: mutableListOf()).also { l -> l.add(msg) }
                }
            }
        } else if (msg.chatType == ChatType.CHAT_TYPE_GROUP) {
            //群聊
            ChatDao.getGroupDb().getGroupInfoById(msg.groupId)?.let {
                title = it.name
                targetId = it.id
                headUrl = it.headUrl
                msg.groupId.let { gid ->
                    notifycationMsg[gid] =
                        (notifycationMsg[gid]
                            ?: mutableListOf()).also { l -> l.add(msg) }
                }
            }
        }


        //前台
        if (AppUtils.isAppForeground()) {
            val ac = ActivityUtils.getTopActivity()
            if (ac is ChatActivity) {//处于聊天页
                if (targetId == ac.getChartId()) {
                    return
                } else {
                }
            }
        } else {
            initNotification(context, title, targetId, msg)
        }
    }

    private fun initNotification(
        context: Context,
        title: String,
        targetId: String,
        msg: ChatMessageBean
    ) {
        val chatPartner = Person.Builder()
            .setName(title)
            .setKey(targetId)
//                .setIcon(
//                    IconCompat.createFromIcon(
//                        Icon.createWithResource(
//                            Utils.getApp(),
//                            R.drawable.user_head_default
//                        )
//                    )
//                )
            .build()

        // 通知行为（点击后能进入应用界面）
        val intentClick = initClickIntent(msg)
        val pendingIntent = PendingIntent.getActivity(
            Utils.getApp(),
            0,
            intentClick,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Full-screen intent
        val fullScreenIntent = PendingIntent.getActivity(
            Utils.getApp(),
            0,
            intentClick,  // Use the same intent as the regular intent
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //通知栏移除
        val intent =
            Intent("com.ym.chat.broadcast.NOTIFICATION_REMOVE").putExtra("targetId", targetId)
        val removeIntent = PendingIntent.getBroadcast(
            Utils.getApp(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //添加消息
        val tempMsgList = notifycationMsg[targetId]
        val msgStyle = NotificationCompat.MessagingStyle(chatPartner)
            .setGroupConversation(true)
        tempMsgList?.forEach {
            msgStyle.addMessage(getMsgContent(context, it), it.createTime, chatPartner)
        }

        val count = ChatDao.getConversationDb().getConverunReadCount()

        var notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(msgStyle)
            .setDeleteIntent(removeIntent)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setNumber(count) // display the badge for unread messages
            .setPriority(PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenIntent, true)  // Set full-screen intent
            .setContentIntent(pendingIntent).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    adaptAndroidN(context, this, targetId, msg.chatType);
                }
            }
            .build()
        show(targetId.hashCode(), notification, channelId)
    }

    /**
     * 点击通知栏监听
     */
    private fun initClickIntent(msg: ChatMessageBean): Intent {
        return if (AppUtils.isAppRunning(AppUtils.getAppPackageName())) {
            Intent(Utils.getApp(), ChatActivity::class.java).also { intent ->
                if (msg.chatType == ChatType.CHAT_TYPE_FRIEND) {//单聊
                    intent.putExtra(ChatActivity.CHAT_ID, msg.from)
                    intent.putExtra(ChatActivity.CHAT_TYPE, 0)
                } else if (msg.chatType == ChatType.CHAT_TYPE_GROUP) {//群聊
                    intent.putExtra(ChatActivity.CHAT_ID, msg.groupId)
                    intent.putExtra(ChatActivity.CHAT_TYPE, 1)
                }
            }
        } else {
            Intent(Utils.getApp(), SplashActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    @Synchronized
    private fun show(id: Int, notification: Notification, channelId: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        initNotificationManager(Utils.getApp(), channelId)
        if (Build.BRAND.lowercase() == "xiaomi") {
//            BadgeUtils.setCount(0, Utils.getApp(), notification)
            ShortcutBadger.applyNotification(Utils.getApp(), notification, 0)
//            notificationManager?.notify(id, notification)
        } else {
//            notificationManager?.notify(id, notification)
            ShortcutBadger.applyCount(Utils.getApp(), 0)
//            BadgeUtils.setCount(0, Utils.getApp(), notification)
        }
        notificationManager?.notify(id, notification)
    }

    @Synchronized
    private fun initNotificationManager(context: Context, channelId: String) {
        synchronized(this) {
            if (notificationManager == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = context.getString(R.string.xiaoxi) // "消息"
                    val descriptionText = context.getString(R.string.消息描述) // "消息描述"
                    val importance = IMPORTANCE_HIGH
                    val channel = NotificationChannel(channelId, name, importance).apply {
                        description = descriptionText
                    }
                    channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    channel.setShowBadge(false)
                    // Register the channel with the system
                    notificationManager = Utils.getApp()
                        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager?.createNotificationChannel(channel)
                } else {
                }
            }
        }
    }

    private fun adaptAndroidN(
        context: Context,
        builder: NotificationCompat.Builder,
        toId: String,
        chatType: String
    ) {
        builder.setShowWhen(true)
        val replyLabel = context.getString(R.string.huifu) // "回复"
        val remoteInput: RemoteInput = RemoteInput.Builder("KEY_TEXT_REPLY")
            .setLabel(replyLabel)
            .build()
        val intent = Intent(context, ReplyService::class.java)
        intent.putExtra("toId", toId)
        intent.putExtra("chatType", chatType)
        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val action: NotificationCompat.Action =
            NotificationCompat.Action.Builder(R.drawable.ic_add, replyLabel, pendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build()
        builder.addAction(action)
    }

    /**
     * 根据targetId清空通知栏
     */
    fun clearNotification(targetId: String) {
        //移除通知栏消息数据
        ImCache.notifycationMsg[targetId]?.clear()
        notificationManager?.cancel(targetId!!.hashCode())
    }

    /**
     * 获取消息内容
     */
    fun getMsgContent(context: Context, msg: ChatMessageBean): CharSequence {
        //内容显示
        return when (msg.msgType) {
            MsgType.MESSAGETYPE_PICTURE -> "您收到了一张[图片]"
            MsgType.MESSAGETYPE_VOICE -> "您收到了一段[语音]"
            MsgType.MESSAGETYPE_VIDEO -> "您收到了一个[视频]"
            MsgType.MESSAGETYPE_FILE -> "您收到了一个[文件]"
            else -> AtUserHelper.parseAtUserLinkJx(
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
    }
}