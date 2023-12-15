package com.ym.chat.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import android.provider.Browser
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.constant.EventKeys.GROUP_ACTION
import com.ym.base.constant.EventKeys.MSG_NEW
import com.ym.base.ext.*
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.BaseBinderAdapterPro
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.databinding.ActivityChatBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.ConfirmSelectDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.dialog.SelectHintDialog
import com.ym.chat.ext.loadImg
import com.ym.chat.item.*
import com.ym.chat.item.chatlistener.ChatPopClickType
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.rxhttp.MeExpressionRepository
import com.ym.chat.service.WebsocketWork
import com.ym.chat.update.UpdateProcessTask
import com.ym.chat.utils.*
import com.ym.chat.utils.ChatType.CHAT_TYPE_GROUP_SEND
import com.ym.chat.utils.MsgType.ALLOWSPEAK
import com.ym.chat.utils.MsgType.CancelMemberRole
import com.ym.chat.utils.MsgType.GroupModifyName
import com.ym.chat.utils.MsgType.MESSAGETYPE_AT
import com.ym.chat.utils.MsgType.MESSAGETYPE_TIME
import com.ym.chat.utils.MsgType.SetMemberRole
import com.ym.chat.utils.StringExt.isAtMsg
import com.ym.chat.utils.audio.AudioPlayManager
import com.ym.chat.viewmodel.ChatGroupViewModel
import com.ym.chat.viewmodel.ChatViewModel
import com.ym.chat.viewmodel.MeExpressionViewModel
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.panel.ConversationInputPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.minetsh.imaging.IMGEditActivity
import org.json.JSONObject
import java.io.File
import java.io.Serializable
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs


/**
 * @version V1.0
 * @createAuthor
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 聊天界面
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ChatActivity : LoadingActivity(),
    ConversationInputPanel.OnConversationInputPanelStateChangeListener,
    ConversationInputPanel.SendMsgListener, ConversationInputPanel.OnExtClickListener,
    ConversationInputPanel.OnDelGifClickListener,
    ConversationInputPanel.NoticeFriendListener, View.OnLayoutChangeListener {

    private val REQ_IMAGE_EDIT = 100
    private val REQ_FILE_SELECT = 101

    private val mViewModel = ChatViewModel()
    private val mViewGroupModel = ChatGroupViewModel()
    private val mEmojViewModel: MeExpressionViewModel by viewModels()//emoj表情数据获取
    private val bindView: ActivityChatBinding by binding()
    private val mAdapter = BaseBinderAdapterPro()
    private val mAtAdapter = BaseBinderAdapterPro()
    private var mChatType = 0//当前聊天窗口类型，单聊(0)或者群聊(1) 群发消息（2）
    private var mTargetId = ""//聊天ID
    private var chatInfo: FriendListBean? = null//好友信息
    private var groupInfo: GroupInfoBean? = null//群信息
    private var isAutoScrollBottom = true//是否自动滑到底部
    private var isShowLastMsg = true //是否显示最后一条消息
    private var isFirstInit = true  //是否第一次初始化
    private var isMute = false //群设置禁言 是否禁言状态，true为禁言，只针对群有效
    private var isMemberMute = false //个人禁言 是否禁言状态，true为禁言，只针对群个人有效
    private var isSelectMode = false//是否处于多选模式
    private var unReadCount = 0   //未读消息条数
    private var sendMsgType = 0 //0 默认发消息  1 转发消息 2 搜索历史记录
    private var recordBean = RecordBean() //转发消息实体
    private var searchContentId: String = "" //搜索消息id
    private var friendIds = "" //群发消息 群发成员id 字符串
    private var friendNames = ""//群发消息 群发成员name 字符串
    private var inLineNum = 1 // 群在线人数
    private var MAXSTRINGLENGTH = 500//每条文本消息最大发送长度
    private val MAX_GIF_NUM = 30//表情限制
    private var currentGifNum = 0//当前表情数量
    private var firstShowPosition: Int = 0//第一个完整显示item 的序列号
    private var isShowTopDefaultView = false //是否显示置顶消息填充的View
    private var executorServiceSingle = Executors.newCachedThreadPool() //线程池
    private var mImageFile: File? = null
    private var loadDirection = ""

    companion object {

        const val CHAT_INFO = "chatInfo"
        const val GROUP_INFO = "groupInfo"
        const val CHAT_ID = "chatId"
        const val CHAT_TYPE = "chatType"
        const val SEND_MSG_TYPE = "sendMsgType"
        const val SEND_RECORD_BEAN = "sendRecordBean"
        const val SEARCH_CONTENT_ID = "searchContentId"
        const val FRIEND_id_LIST = "friend_id_list"
        const val FRIEND_NAME_LIST = "friend_name_list"
        const val GET_HISTORY_PAGESIZE = 1000
        const val TONAME = "toName"
        const val TOHEADER = "toHeader"

        val owerAndManagerList = mutableListOf<String>()//群管理员或群主数据
        val groupMemberList = mutableListOf<GroupMemberBean>()//所有群成员
        val msgTopList = mutableListOf<MsgTopBean>()//置顶消息

        /**
         * 当前聊天窗口类型@chatType，单聊(0)、群聊(1)、群发消息(2)、转发消息(3)
         * @sendMsgType 0 默认发消息  1 转发消息 2 搜索历史记录 3 群发消息
         * @sendRecordBean  转发消息的实例
         */
        fun start(
            context: Context, chatId: String, chatType: Int, sendMsgType: Int = 0,
            sendRecordBean: RecordBean? = null, searchContentId: String = "",
            friendNames: String = "", friendIds: String = ""
        ) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(CHAT_TYPE, chatType)
            intent.putExtra(CHAT_ID, chatId)
            intent.putExtra(SEND_MSG_TYPE, sendMsgType)
            intent.putExtra(SEND_RECORD_BEAN, sendRecordBean)
            intent.putExtra(SEARCH_CONTENT_ID, searchContentId)
            intent.putExtra(FRIEND_id_LIST, friendIds)
            intent.putExtra(FRIEND_NAME_LIST, friendNames)
            context.startActivity(intent)
        }

        /**
         * 开启聊天页面
         */
        fun start(
            context: Context,
            chatId: String,
            chatType: Int,
            chatName: String,
            chatHeader: String
        ) {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(CHAT_TYPE, chatType)
            intent.putExtra(CHAT_ID, chatId)
            intent.putExtra(TONAME, chatName)
            intent.putExtra(TOHEADER, chatHeader)
            intent.putExtra(SEND_MSG_TYPE, 0)
            context.startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        if (intent != null) {
            mChatType = 0//当前聊天窗口类型，单聊(0)或者群聊(1) 群发消息（2）
            mTargetId = ""//聊天ID
            chatInfo = null//好友信息
            groupInfo = null//群信息
            isAutoScrollBottom = true//是否自动滑到底部
            isShowLastMsg = true //是否显示最后一条消息
            isFirstInit = true  //是否第一次初始化
            isMute = false //是否禁言状态，true为禁言，只针对群有效
            isSelectMode = false//是否处于多选模式
            unReadCount = 0   //未读消息条数
            sendMsgType = 0 //0 默认发消息  1 转发消息 2 搜索历史记录
            recordBean = RecordBean() //转发消息实体
            searchContentId = "" //搜索消息id
            friendIds = "" //群发消息 群发成员id 字符串
            friendNames = ""//群发消息 群发成员name 字符串
        }
        bindView?.layoutTop?.root?.gone()//初始化置顶界面
        requestData()
    }

//    /**非管理员账号，不可以截屏**/
//    override fun canScreenCapture(): Boolean {
//        return !(!MMKVUtils.isAdmin() && mChatType == 1)
//    }

    override fun initView() {

        //设置信号栏颜色值
        window.statusBarColor = getColor(R.color.white)
        //设置状态栏字体颜色
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR


        bindView.viewBack.click {
            finish()
        }
        bindView.ivMore.click {
            when (mChatType) {
                0 -> {
                    //单聊设置
                    chatInfo?.let {
                        startActivity(
                            Intent(this, UserChatSetActivity::class.java).putExtra(
                                CHAT_INFO,
                                chatInfo as Serializable
                            )
                        )
                    }
                }

                1 -> {
                    groupInfo?.let { it1 ->
                        ChatUtils.showSettingMorePopupWindow(
                            this,
                            bindView.ivMore,
                            it1,
                            0,
                            onClickSettingMore = { data, type ->
                                when (type) {
                                    0 -> {//查看简介
                                        //群聊设置
                                        groupInfo?.let {
                                            startActivity(
                                                Intent(
                                                    this,
                                                    GroupChatSetActivity::class.java
                                                ).putExtra(
                                                    GROUP_INFO,
                                                    groupInfo as Serializable
                                                )
                                            )
                                        }
                                    }

                                    1 -> {//开启/关闭静音
                                        var messageNotice =
                                            if (groupInfo?.messageNotice == "N") "Y" else "N"
                                        mViewGroupModel.putGroupMemberNotice(
                                            groupInfo?.id ?: "",
                                            messageNotice = messageNotice
                                        )
                                    }

                                    2 -> {//清空历史消息
                                        if (data.roleType.lowercase() != "normal") {
                                            HintDialog(
                                                getString(R.string.删除历史消息),
                                                String.format(
                                                    getString(R.string.删除提示),
                                                    data.name
                                                ),
                                                object : ConfirmDialogCallback {
                                                    override fun onItemClick() {
                                                        groupInfo?.id?.let { it1 ->
                                                            mViewGroupModel.deleteRemoteGroupMessage(
                                                                it1
                                                            )
                                                        }
                                                    }
                                                },
                                                iconId = R.drawable.ic_mine_header_group,
                                                headUrl = groupInfo?.headUrl,
                                                isShowHeader = true, isTitleTxt = true
                                            ).show(supportFragmentManager, "HintDialog")
                                        }
                                    }

                                    3 -> {//退出/解散群组
                                        var title = ""
                                        var content = ""
                                        if (data.roleType.lowercase() == "owner") {
                                            title = getString(R.string.解散群聊)
                                            content = String.format(
                                                getString(R.string.确定解散提示),
                                                data.name
                                            )
                                        } else {
                                            title = "退出群聊"
                                            content = String.format(
                                                getString(R.string.确定离开提示),
                                                data.name
                                            )
                                        }
                                        HintDialog(
                                            title,
                                            content,
                                            object : ConfirmDialogCallback {
                                                override fun onItemClick() {
                                                    groupInfo?.id?.let { it1 ->
                                                        if (data.roleType.lowercase() == "owner") {
                                                            //群主解散群
                                                            mViewGroupModel.deleteGroup(it1)
                                                        } else {
                                                            //群成员退出群
                                                            mViewGroupModel.leaveGroup(it1)
                                                        }
                                                    }
                                                }
                                            },
                                            iconId = R.drawable.ic_mine_header_group,
                                            headUrl = groupInfo?.headUrl,
                                            isShowHeader = true, isTitleTxt = true
                                        ).show(supportFragmentManager, "HintDialog")
                                    }
                                }
                            })
                    }
                }
            }
        }
        bindView.layoutName.click {
            when (mChatType) {
                0 -> {
                    //单聊设置
                    chatInfo?.let {
                        startActivity(
                            Intent(this, UserChatSetActivity::class.java).putExtra(
                                CHAT_INFO,
                                chatInfo as Serializable
                            )
                        )
                    }
                }

                1 -> {
                    //群聊设置
                    groupInfo?.let {
                        startActivity(
                            Intent(this, GroupChatSetActivity::class.java).putExtra(
                                GROUP_INFO,
                                groupInfo as Serializable
                            )
                        )
                    }
                }
            }
        }

        initAdapter()
        initInputPanel()
        initAtRecyclerView()

        bindView.listChat.addOnLayoutChangeListener(this)
        bindView.listChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                //最后一个显示
                val lastShowPosition = manager.findLastVisibleItemPosition()
                //第一个显示
                firstShowPosition = manager.findFirstVisibleItemPosition()
                if (mChatType == 1 && firstShowPosition < 2) {
                    isShowTopDefaultView = true
                    if (msgTopList.size > 0) {
                        bindView.viewDefault.visible()
                    } else {
                        bindView.viewDefault.gone()
                    }
                }
                if (lastShowPosition + 1 == mAdapter.itemCount) {
                    isAutoScrollBottom = true
                    isShowLastMsg = true
                    unReadCount = 0
                } else {
                    isShowLastMsg = false
                }
                if (lastShowPosition + 2 >= mAdapter.itemCount) {
                    mUnReadCount = 0
                    bindView.ivSetBottom.gone()
                    bindView.tvUnReadCount.gone()
                    //设置消息已读
//                    ChatDao.getChatMsgDb().setMsgRead(getChatType(), mTargetId)
                } else {
                    bindView.ivSetBottom.visible()

                    //显示消息未读
//                    showUnReadCount()
                }
                if (dy < 0) {
                    //向上滑动来消息不需要滑动到底部
                    isAutoScrollBottom = false
                }
//                "---dx=${dx}---dy=${dy}---isAutoScrollBottom=${isAutoScrollBottom}---lastShowPosition=${lastShowPosition}---firstShowPosition=${firstShowPosition}".logE()
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        //点击置底按钮
        bindView.ivSetBottom.click {
            //滚动到底部
            bindView.listChat.scrollToPosition(mAdapter.data.size - 1)
            bindView.tvUnReadCount.gone()
            ChatDao.getChatMsgDb().setMsgRead(getChatType(), mTargetId)
        }

        bindView.refresh.setOnRefreshListener {
            loadDirection = "Up"
            mAdapter.data.firstOrNull {
                it is ChatMessageBean && (it.msgType != MsgType.MESSAGETYPE_NOTICE || it.msgType != MsgType.MESSAGETYPE_TIME)
            }?.let {
                it as ChatMessageBean
                mViewModel.getLoadMoreMsg(getChatType(), it.id, loadDirection, mTargetId)
            }
        }
        bindView.refresh.setOnLoadMoreListener {
            loadDirection = "Down"
            mAdapter.data.lastOrNull {
                it is ChatMessageBean && (it.msgType != MsgType.MESSAGETYPE_NOTICE || it.msgType != MsgType.MESSAGETYPE_TIME)
            }?.let {
                it as ChatMessageBean
                mViewModel.getLoadMoreMsg(getChatType(), it.id, loadDirection, mTargetId)
            }
        }
    }

    private var mUnReadCount = 0

    /**
     * 显示消息未读数量
     */
    private fun showUnReadCount() {
        if (bindView.ivSetBottom.visibility == View.VISIBLE) {
            if (getChatType() == ChatType.CHAT_TYPE_FRIEND) {
                val count = ChatDao.getChatMsgDb().getUnReadCount(mTargetId)
                if (mUnReadCount > 0) {
                    bindView.tvUnReadCount.visible()
                    bindView.tvUnReadCount.text = "$mUnReadCount"
                } else {
                    bindView.tvUnReadCount.gone()
                }
            } else if (getChatType() == ChatType.CHAT_TYPE_GROUP) {
                val count = ChatDao.getChatMsgDb().getUnReadCountByGroup(mTargetId)
                if (mUnReadCount > 0) {
                    bindView.tvUnReadCount.visible()
                    bindView.tvUnReadCount.text = "$mUnReadCount"
                } else {
                    bindView.tvUnReadCount.gone()
                }
            }
        }
    }


    /**
     * 初始化输入面板
     */
    private fun initInputPanel() {
        bindView.inputPanelFrameLayout.init(this, bindView.root)
        bindView.inputPanelFrameLayout.setOnConversationInputPanelStateChangeListener(this)
        bindView.inputPanelFrameLayout.setSendMsgListener(this)
        bindView.inputPanelFrameLayout.setExtClickListener(this)
        bindView.inputPanelFrameLayout.setDelGifClickListener(this)
        bindView.inputPanelFrameLayout.setNoticeFriendListener(this)

        val list = mutableListOf<EmojListBean.EmojBean>().apply {
            for (i in 0..20) {
                add(EmojListBean.EmojBean())
            }
        }
        list?.forEach { it.isDel = true }
        bindView.inputPanelFrameLayout.setGifData(list)
    }

    /**
     * 初始化adapter
     */
    private fun initAdapter() {

        bindView.listChat.adapter = mAdapter.apply {

            //时间线
            addItemBinderMany(ChatTimeItem())
            //消息未读
            addItemBinderMany(ChatUnReadItem())

            //文字消息
            addItemBinderMany(ChatTextLeft(onChatItemListener))
            addItemBinderMany(ChatTextRight(onChatItemListener))

            //图片消息
            addItemBinderMany(ChatImageLeft(onChatItemListener))
            addItemBinderMany(ChatImageRight(onChatItemListener))

            //未知消息
            addItemBinderMany(ChatUndefinedLeft(onChatItemListener))
            addItemBinderMany(ChatUndefinedRight(onChatItemListener))

            //文件消息
            addItemBinderMany(ChatFileLeft(onChatItemListener, onClickListener = { item, position ->
                downloadFile(item)
            }))
            addItemBinderMany(
                ChatFileRight(
                    onChatItemListener,
                    onClickListener = { item, position ->
                        downloadFile(item)
                    })
            )

            //语音消息
            addItemBinderMany(
                ChatAudioLeft(onChatItemListener, onPlayClickListener = { item, position ->
                    playItemAudio(item, position)
                })
            )
            addItemBinderMany(
                ChatAudioRight(
                    onChatItemListener,
                    onPlayClickListener = { item, position ->
                        playItemAudio(item, position)
                    })
            )

            //视频消息
            addItemBinderMany(
                ChatVideoLeft(
                    onChatItemListener,
                    onPlayClickListener = { item ->
                        ChatUtils.playVideo(this@ChatActivity, item)
                    })
            )
            addItemBinderMany(ChatVideoRight(onChatItemListener, onPlayClickListener = {
                ChatUtils.playVideo(this@ChatActivity, it)
            }))

            //名片消息
            addItemBinderMany(ChatContactCardLeft(onChatItemListener) { contactBean, _ ->
                clickContactCard(contactBean)
            })
            addItemBinderMany(ChatContactCardRight(onChatItemListener) { contactBean, _ ->
                clickContactCard(contactBean)
            })
        }
    }

    /**
     * 点击分享名片
     */
    private fun clickContactCard(contactBean: ContactCardMsgBean) {
        try {
            val localData = ChatDao.getFriendDb().getFriendById(contactBean.shareMemberId)
            if (localData != null) {
                //本地数据库有
                start(
                    this,
                    contactBean.shareMemberId,
                    0,
                    chatName = contactBean.shareMemberName,
                    chatHeader = contactBean.shareMemberHeadUrl
                )
            } else {
                //本地数据库没有
                val intent = Intent(this, FriendInfoActivity::class.java)
                intent.putExtra(ContactActivity.IN_TYPE, 3)
                intent.putExtra("isMakeDate", true)
                val friend = FriendListBean(
                    -1,
                    name = contactBean.shareMemberName,
                ).apply {
                    this.headUrl = contactBean.shareMemberHeadUrl
                }
                friend.id = contactBean.shareMemberId
                friend.friendMemberId = contactBean.shareMemberId
                startActivity(
                    intent.putExtra(ChatActivity.CHAT_INFO, friend)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadFile(item: ChatMessageBean) {
        try {
            val fileMsg = GsonUtils.fromJson(item.content, FileMsgBean::class.java)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val (isExit, id) = DownloadUtil.queryExist(this@ChatActivity, fileMsg.url)
            if (isExit) {
                id?.let {
                    val uri = downloadManager.getUriForDownloadedFile(it)
                    openFileInBrowser(this@ChatActivity, uri, fileMsg.url)
                }
            } else {
                getString(R.string.文件已开始下载).toast()
                val request =
                    DownloadManager.Request(Uri.parse(fileMsg.url)) //添加下载文件的网络路径

                //注册广播接收器
                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                registerReceiver(receiver, filter)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileMsg.name
                ) //添加保存文件路径与名称
                request.setTitle(fileMsg.name) //添加在通知栏里显示的标题
                request.setDescription("下载中") //添加在通知栏里显示的描述
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI) //设置下载的网络类型
                request.setVisibleInDownloadsUi(false) //是否显示下载 从Android Q开始会被忽略
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //下载中与下载完成后都会在通知中显示| 另外可以选 DownloadManager.Request.VISIBILITY_VISIBLE 仅在下载中时显示在通知中,完成后会自动隐藏
                val downloadId = downloadManager.enqueue(request) //加入队列，会返回一个唯一下载id
                executorServiceSingle.submit(UpdateProcessTask(downloadManager, downloadId, item))
            }

        } catch (e: Exception) {
            getString(R.string.文件下载异常).toast()
        }
    }

    /**
     * 广播接受器, 下载完成监听器
     */
    var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                //获取当前完成任务的ID
                val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                getString(R.string.文件已下载完成).toast()
//                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//                val uri = downloadManager.getUriForDownloadedFile(reference)
//                openFileInBrowser(this@ChatActivity, uri)
                mAdapter.notifyDataSetChanged()
            }
            if (action == DownloadManager.ACTION_NOTIFICATION_CLICKED) {
                //广播被点击了
            }
        }
    }

    private fun openFileInBrowser(context: Context, uri: Uri, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
            intent.setDataAndType(uri, DownloadUtil.getMimeType(url))
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "Chooser"))
        } catch (e: ActivityNotFoundException) {
            getString(R.string.手机中没有支持此文件的App).toast()
        }
    }


    /**初始化at选择列表的adapter**/
    private fun initAtRecyclerView() {
        val behavior = BottomSheetBehavior.from(bindView.atSelectList)
        behavior.peekHeight = SizeUtils.dp2px(120f)//dialog的高度
        bindView.atSelectList.adapter = mAtAdapter.apply {
            addItemBinder(ChatAtSelectItem { data, position ->
                bindView.coordinator.gone()
                bindView.inputPanelFrameLayout.appendAtText(data.name + " ", data.id)
            })
        }
    }

    private var toName: String = ""
    private var toHeadler: String = ""

    override fun requestData() {
        //获取表情数据
        mEmojViewModel.getEmojList()
        //获取关键字屏蔽
        mViewModel.getKeyWord()

        intent?.let { intent ->
            mChatType = intent.getIntExtra(CHAT_TYPE, -1)
            sendMsgType = intent.getIntExtra(SEND_MSG_TYPE, 0)
            toName = intent.getStringExtra(TONAME) ?: ""
            toHeadler = intent.getStringExtra(TOHEADER) ?: ""
            mTargetId = intent.getStringExtra(CHAT_ID).toString()
            //清空通知栏，该聊天数据
            NotificationUtils.clearNotification(mTargetId)
            setDraftData()
            mViewModel.updateConver(mTargetId, getChatType())
            when (mChatType) {
                0 -> {
                    chatInfo = ChatDao.getFriendDb().getFriendById(mTargetId)
                    if (chatInfo == null) {
                        val f = FriendListBean()
                        f.id = mTargetId
                        f.name = "用户 $mTargetId"
                        chatInfo = f
                        ChatDao.getFriendDb().saveFriend(chatInfo)
                        ChatDao.syncFriendAndGroupToLocal()
                    }
                    bindView.tvTitle.text = chatInfo?.nickname
                    bindView.tvTitleHint.visible()
                    bindView.tvTitleHint.text = getString(R.string.jiamichat)
                    bindView.layoutHeader.ivHeader.loadImg(chatInfo)
                    Utils.showDaShenImageView(bindView.layoutHeader.ivHeaderMark, chatInfo)
                    getTypeInfo()

                    if (chatInfo == null) {
                        //没有找到好友数据
                        bindView.inputPanelFrameLayout.gone()
                    }

                    //重置会话列表未读数
                    ChatDao.getConversationDb().resetConverMsgCount(mTargetId)
                    //设置消息已读
                    ChatDao.getChatMsgDb().setMsgRead(getChatType(), mTargetId)
                    //获取聊天消息
                    showLoading()
                    mViewModel.getMsgList(mTargetId, mChatType)
                    startTimer()
                    bindView.layoutTop.root.gone()
                    bindView.inputPanelFrameLayout.closeMute()
                }

                1 -> {
                    groupInfo = ChatDao.getGroupDb().getGroupInfoById(mTargetId)
                    bindView.tvTitle.text = groupInfo?.name
                    isMute = groupInfo?.allowSpeak == "N"
                    bindView.tvTitleHint.visible()
                    bindView.layoutHeader.ivHeader.loadImg(
                        groupInfo?.headUrl, groupInfo?.name, R.drawable.ic_mine_header_group, true
                    )
//                    if (!MMKVUtils.isAdmin())
//                        bindView.tvGroupNotifyMsg.visible()
                    getTypeInfo()

                    if (groupInfo == null) {
                        //没有找到好友数据
                        bindView.inputPanelFrameLayout.gone()
                    }

                    //获取群主管理员id列表
                    mViewModel.getManagerList(mTargetId)

                    //重置会话列表未读数
                    ChatDao.getConversationDb().resetConverMsgCount(mTargetId)
                    //设置消息已读
                    ChatDao.getChatMsgDb().setMsgRead(getChatType(), mTargetId)
                    //获取聊天消息
                    showLoading()
                    mViewModel.getMsgList(mTargetId, mChatType)
                    startTimer()

//                    showTopMsgView()//显示本地缓存置顶消息
                    mViewModel.getTopInfoList(mTargetId)//从云端获取最新
                }

                2 -> {//群发消息
                    bindView.ivMore.gone()
//                    bindView.listChat.invalidate()
                    bindView.llGroupSendMsg.visible()
                    friendNames =
                        intent.getStringExtra(SendGroupMessageActivity.FRIEND_NAME_LIST) ?: ""
                    friendIds = intent.getStringExtra(SendGroupMessageActivity.FRIEND_id_LIST) ?: ""
                    bindView.tvFriendName.text = friendNames.replace(";", ",")
                    mViewModel.friendIds = friendIds
                    mViewModel.friendNames = friendNames
                    bindView.layoutTop.root.gone()
                }
            }
        }
    }

    private fun setDraftData() {
        var draftData = ChatDao.getDraftDb().queryDraftByChatId(mTargetId)
        if (draftData == null) {
            draftData = DraftBean().apply {
                chatId = mTargetId
            }
        }
        bindView.inputPanelFrameLayout.setDraft(draftData)
    }

    private fun getTypeInfo() {
        when (sendMsgType) {
            1 -> recordBean = intent.getSerializableExtra(SEND_RECORD_BEAN) as RecordBean //转发好友消息
            2 -> searchContentId = intent.getStringExtra(SEARCH_CONTENT_ID).toString() //搜索指定内容id
        }
    }

    /**
     * 跳转到指定消息并且闪烁
     */
    var positionHighlight = 0
    private fun scrollToItem(position: Int) {

        if (mAdapterTop?.data?.size!! > 0) bindView.viewDefault.visible()
        bindView.listChat.scrollToPosition(position)

        val item = mAdapter.data[position]
        if (item is ChatMessageBean) {
//            "----------positionHighlight=${positionHighlight}--position=${position}---${item.isHighlight}---${(positionHighlight == position && item.isHighlight)}".logE()
            if (!(positionHighlight == position && item.isHighlight)) {//如果这个item在做高亮动画，不再重新高亮
                item.isHighlight = true
                positionHighlight = position
                mAdapter.notifyItemChanged(position)
            }
        }
    }

    override fun observeCallBack() {
        /*ws是否在线人数 广播*/
        LiveEventBus.get(EventKeys.GET_FRIEND_GROUP_LINE_MSG, String::class.java).observe(this) {
            //刷新数据操作
            if (!TextUtils.isEmpty(it)) {
                val jsonObject = JSONObject(it)
                jsonObject.optJSONObject("data")?.let { data ->
                    val groupId = data.optString("groupId")
                    val memberId = data.optString("memberId")
                    val onlineCount = data.optInt("onlineCount")
                    when (mChatType) {
                        0 -> {
                            if (mTargetId == memberId) {
                                if (onlineCount == 1) {
                                    bindView.tvTitleHint.text = getString(R.string.在线加密聊天)
                                } else {
                                    bindView.tvTitleHint.text = getString(R.string.离线加密聊天)
                                }
                            }
                        }

                        1 -> {
                            if (mTargetId == groupId) {
                                inLineNum = onlineCount
                                bindView.tvTitleHint.text =
                                    "${getString(R.string.加密聊天)} (${groupMemberList.size ?: 1})"//显示在线人数/总人数
                            }
                        }
                    }
                }
            }
        }
        /*ws被别人踢出群 广播*/
        LiveEventBus.get(EventKeys.DEL_MEMBER_GROUP, String::class.java).observe(this) {
            //刷新数据操作
            if (!TextUtils.isEmpty(it)) {
                val jsonObject = JSONObject(it)
                jsonObject.optJSONObject("data")?.let { data ->
                    val groupId = data.optString("groupId")
                    val operatorId = data.optString("operatorId")//操作人ID
                    val name = data.optString("operatorName")//操作人
                    val memberId = data.optString("memberId")//被移除人id
                    if (groupId == groupInfo?.id && memberId == MMKVUtils.getUser()?.id)
                        showHintDialog(name)
                    //处理多端同步
                    if (operatorId == MMKVUtils.getUser()?.id) {
                        //重新获取群成员数据
                        mViewModel.getManagerList(mTargetId)
                    }
                }
            }
        }

        /*ws后台群解散 广播*/
        LiveEventBus.get(EventKeys.SYSTEM_DEL_GROUP, String::class.java).observe(this) {
            //刷新数据操作
            if (it == mTargetId) {
                showHintDialog("")
            }
        }

        /*接收到群消息置顶广播*/
        LiveEventBus.get(EventKeys.MSG_TOP_WS, Boolean::class.java).observe(this) {
            if (mChatType == 1)
                mViewModel.getTopInfoList(mTargetId)
        }

        /*接收到清空群消息或者好友聊天消息 广播*/
        LiveEventBus.get(EventKeys.CLEAR_GROUP_MSG, Boolean::class.java).observe(this) {
            mAdapter.setList(mutableListOf())
        }

        /*接收修改好友是否打开通知 广播*/
        LiveEventBus.get(EventKeys.EDIT_USER_MESSAGE_NOTICE, String::class.java).observe(this) {
            chatInfo?.messageNotice = it
        }

        /*群成员退出群 群组解散群 广播*/
        LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java).observe(this) {
            //退出群聊
            this.finish()
        }

        LiveEventBus.get(MSG_NEW, ChatMessageBean::class.java).observe(this) { msg ->
            //收到消息
            if (msg.chatType == ChatType.CHAT_TYPE_FRIEND && if (msg.dir == 0) msg.from == chatInfo?.id else msg.to == chatInfo?.id) {
                //单聊消息
                processMsg(msg)
            } else if (msg.chatType == ChatType.CHAT_TYPE_GROUP && msg.groupId == groupInfo?.id) {
                //群组消息
                processMsg(msg)
            } else {
                //其他消息
            }

            //重置会话列表未读数
            ChatDao.getConversationDb().resetConverMsgCount(mTargetId)

            if (bindView.ivSetBottom.visibility == View.VISIBLE) {
                mUnReadCount++
            }

            //显示消息未读
            showUnReadCount()
        }
        /*接收修改群名 群头像 是否禁言广播*/
        LiveEventBus.get(EventKeys.EDIT_GROUP_INFO, GroupInfoBean::class.java).observe(this) {
            it?.let {
                try {
                    bindView.tvTitle.text = it.name
                    groupInfo?.headUrl = it.headUrl
                    groupInfo?.allowSpeak = it.allowSpeak
                    "headUrl==${it.headUrl}---${it.allowSpeak}".logD()
                    bindView.layoutHeader.ivHeader.loadImg(
                        groupInfo?.headUrl, groupInfo?.name, R.drawable.ic_mine_header_group, true
                    )
                } catch (e: Exception) {

                }
            }
        }

        //更新好友备注
        LiveEventBus.get(
            EventKeys.EDIT_FRIEND_REMARK_NOTICE,
            FriendListBean::class.java
        ).observe(this) {
            if (mChatType == 0) {
                bindView.tvTitle.text = it.nickname
                chatInfo?.remark = it.remark
            }
        }

        //获取最新置顶消息
        mViewModel.getTopMsgList.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }

                is BaseViewModel.LoadState.Success -> {
                    msgTopList.clear()
                    val list = result.data?.data
                    list?.let { msgTopList.addAll(it) }
                    showTopMsgView()
                    mAdapter.notifyDataSetChanged()
                    hideLoading()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }

        //增加置顶消息
        mViewModel.addTopMsg.observe(this) {
            when (it) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    mViewModel.getTopInfoList(mTargetId)
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }

        //删除置顶消息
        mViewModel.delTopMsg.observe(this) {
            when (it) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    mViewModel.getTopInfoList(mTargetId)
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }

        //主动发消息回调
        mViewModel.sendMsgResult.observe(this) {
            processMsg(it)
            bindView.listChat.scrollToPosition(mAdapter.data.size - 1)
        }

        //收到群发发消息成功
        mViewModel.groupSendMsgResult.observe(this) {
            when (it) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.群发消息已发送成功).toast()
                    finish()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }

        //聊天历史消息
        mViewModel.msgList.observe(this) {
            hideLoading()
            isFirstInit = false
            mAdapter.setList(it)

            when (sendMsgType) {
                1 -> {
                    bindView.listChat.scrollToPosition(it.size - 1)
                    sendForwardMsg()
                } //如果是转发消息 执行发消息
                2 -> scrollToPosition(it) //如果是搜索消息，根据消息ID，跳转到指定条目
                else -> {
                    val postion = mViewModel.firstUnReadMsg?.value ?: -1
                    bindView.refresh.setEnableLoadMore(false)
                    if (postion != -1) {
                        isAutoScrollBottom = false
                        bindView.listChat.scrollToPosition(postion)
                    } else {
                        isAutoScrollBottom = false
                        bindView.listChat.scrollToPosition(it.size - 1)
                    }
                }
            }
        }

        //收藏回调
        mViewModel.collectResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.收藏成功).toast()
                    collectMsg?.let { msg ->
                        ChatDao.getConversationDb()
                            .updateCollectLastMsg(msg.msgType, msg.content, msg.createTime)
                        collectMsg = null
                    }
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    getString(R.string.收藏失败).toast()
                }
            }
        }

        //管理员踢人
        mViewGroupModel.deleteGroupMemberLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //重新获取群成员数据
                    mViewModel.getManagerList(mTargetId)
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //群设置 群成员消息免打扰
        mViewGroupModel.putGroupMemberNoticeLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }

                is BaseViewModel.LoadState.Success -> {
                    groupInfo?.messageNotice = if (groupInfo?.messageNotice == "N") "Y" else "N"
                }

                is BaseViewModel.LoadState.Fail -> {
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //群成员退出群
        mViewGroupModel.leaveGroupLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    this.finish()
                    LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java)
                        .post(true)
                    getString(R.string.退群成功).toast()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //群主解散群
        mViewGroupModel.deleteGroupLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    this.finish()
                    LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java)
                        .post(true)
                    getString(R.string.群已解散).toast()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }


        //远程销毁群消息
        mViewGroupModel.deleteRemoteGroupMessage.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.群消息已全部销毁).toast()
                    //清空本地聊天数据操作
                    groupInfo?.id?.let {
                        ChatDao.getChatMsgDb().delMsgListByGroupId(it)
                        LiveEventBus.get(EventKeys.CLEAR_GROUP_MSG, Boolean::class.java)
                            .post(true)
                    }
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }

        //群设置 群成员禁言
        mViewGroupModel.putGroupMemberMuteLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //刷新数据操作
                    if (mChatType == 1) {
                        //重新获取群成员数据
                        mViewModel.getManagerList(mTargetId)
                    }
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        mEmojViewModel.delEmojResult.observe(this) {
            //删除gif
            mEmojViewModel.getEmojList()
        }

        //媒体消息回调
        LiveEventBus.get(EventKeys.EVENT_SEND_MSG, ChatMessageBean::class.java).observe(this) {
            processMsg(it)
            bindView.listChat.scrollToPosition(mAdapter.data.size - 1)
        }

        //后台更新了敏感词
        LiveEventBus.get(EventKeys.UPDATE_SENSITIVE_WORD_MSG, Boolean::class.java).observe(this) {
            mViewModel.getKeyWord()
        }

        //其他端更新了gif图片
        LiveEventBus.get(EventKeys.UPDATE_GIF_MSG, Boolean::class.java).observe(this) {
            mEmojViewModel.getEmojList()
        }

        //消息发送状态更新
        LiveEventBus.get(EventKeys.SEND_STATE_UPDATE, ChatMessageBean::class.java)
            .observe(this) { msg ->
                mAdapter.data.indexOfFirst {
                    it is ChatMessageBean && it.dbId == msg.dbId
                }.let { index ->
                    mAdapter.setData(index, msg)

                }
            }

        //消息已读状态
        LiveEventBus.get(EventKeys.MSG_READ_EVENT, ChatMessageBean::class.java)
            .observe(this) { msg ->
                mAdapter.data.indexOfFirst {
                    it is ChatMessageBean && it.dbId == msg.dbId
                }.let { index ->
                    if (index >= 0) {
                        (mAdapter.data.get(index) as ChatMessageBean).msgReadState = 1
                        mAdapter.notifyItemChanged(index)
                    }
                }
            }

        //消息编辑
        LiveEventBus.get(EventKeys.MSG_EDIT, ChatMessageBean::class.java)
            .observe(this) { msg ->
                mAdapter.data.indexOfFirst {
                    it is ChatMessageBean && it.dbId == msg.dbId
                }.let { index ->
                    if (index >= 0) {
                        val bean = mAdapter.data[index] as ChatMessageBean
                        bean.content = msg.content
                        bean.editId = msg.editId
                        bean.operationType = msg.operationType
//                        mAdapter.notifyItemChanged(index)
                        mAdapter.notifyDataSetChanged()
                    }
                }
            }

        //远程销毁单条消息
        LiveEventBus.get(EventKeys.DEL_MSG_ONE, String::class.java).observe(this) { serviceId ->
            mAdapter.data.filter {
                it is ChatMessageBean && (it.id == serviceId || it.editId == serviceId)
            }.let { result ->
                if (result.isNotEmpty()) {
                    val temp = result[0]
                    mAdapter.data.remove(temp)
                    clearDataHeader()
                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        //远程销毁所有消息
        LiveEventBus.get(EventKeys.DEL_MSG_ALL, String::class.java).observe(this) { chatId ->
            if (chatId == mTargetId) {
//                finish()
                mAdapter.data.clear()
                mAdapter.notifyDataSetChanged()
            }
        }

        //群聊相关
        LiveEventBus.get(GROUP_ACTION, GroupActionBean::class.java).observe(this) { groupAction ->
            processGroupAction(groupAction)
        }

        //群主转让
        LiveEventBus.get(EventKeys.TRANSFER_GROUP, String::class.java).observe(this) { groupId ->
            if (groupId == mTargetId) {
                //获取群主管理员id列表
                mViewModel.getManagerList(mTargetId)
            }
        }

        //群管理员和群主数据
        mViewModel.groupMemberList.observe(this) {
            showMemberTypeView(it)

            if (mAdapter.data.size == 0) {
                val ownerBean = it.firstOrNull { it.role.lowercase() == "owner" }
                if (ownerBean?.id == MMKVUtils.getUser()?.id) {
                    mAdapter.setEmptyView(R.layout.view_group_empty)
                }
            }
        }
        mEmojViewModel.emojListResult.observe(this) {
            //表情数据
            currentGifNum = it.size
            it.add(0, EmojListBean.EmojBean(isAddDefault = true))
            it?.forEach { e -> e.isDel = true }
            bindView.inputPanelFrameLayout.setGifData(it)
        }

        mViewModel.loadMoreMsgList.observe(this) {
            hideLoading()
            bindView.refresh.finishRefresh()
            bindView.refresh.finishLoadMore()
            if (loadDirection == "Down") {
                mAdapter.addData(mViewModel.generateDateHeaders(it, false))
                if (it.size >= GET_HISTORY_PAGESIZE) {
                    bindView.refresh.setEnableLoadMore(true)
                } else {
                    bindView.refresh.setEnableLoadMore(false)
                }
            } else {
                mAdapter.addData(0, mViewModel.generateDateHeaders(it, false))
                if (it.size >= GET_HISTORY_PAGESIZE) {
                    bindView.refresh.setEnableRefresh(true)
                } else {
                    bindView.refresh.setEnableRefresh(false)
                }
            }
        }

        //更新数据
        LiveEventBus.get(EventKeys.UPDATE_GIF).observe(this) {
            mEmojViewModel.getEmojList()
        }

        /*接收到 设置了成员禁言 广播*/
        LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP, FriendListBean::class.java)
            .observe(this) { f ->
                if (mChatType == 1) {
                    //刷新数据
                    groupMemberList?.forEach {
                        if (it.memberId == f.id) {
                            it.allowSpeak = f.allowSpeak
                            return@forEach
                        }
                    }
                }
            }

        /*接收到 设置了成员禁言 广播*/
        LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP_1, FriendListBean::class.java)
            .observe(this) { f ->
                if (mChatType == 1) {
                    //刷新数据
                    groupMemberList?.forEach {
                        if (it.memberId == f.id) {
                            it.allowSpeak = f.allowSpeak
                            return@forEach
                        }
                    }
                }
            }

        /*接收到 设置了成员禁言 广播*/
        LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP_2, Boolean::class.java).observe(this) {
            if (mChatType == 1) {
                //刷新数据
                mViewModel.getManagerList(mTargetId)
            }
        }

        LiveEventBus.get(EventKeys.FILE_DOWNLOAD_PROCESS, ChatMessageBean::class.java)
            .observe(this) {
                mAdapter.notifyDataSetChanged()
            }

        /**ws 收到已被删除好友*/
        LiveEventBus.get(EventKeys.DEL_FRIEND_ACTION, String::class.java)
            .observe(this) {
                var friend = ChatDao.getFriendDb().getFriendById(it)
                var content = ""
                if (chatInfo?.id == it) {
                    content = String.format(getString(R.string.shanchutishi1),friend?.nickname)
                } else {
                    //操作人是自己
                    content = String.format(getString(R.string.shanchutishi2),friend?.nickname)
                }
                if (mChatType == 0) {
                    //被删除好友
                    HintDialog(
                        getString(R.string.zhuyi),
                        content,
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                finish()
                            }
                        },
                        R.drawable.ic_mine_header,
                        headUrl = friend?.headUrl ?: "",
                        isShowBtnCancel = false,
                        isCanTouchOutsideSet = false,
                        isShowHeader = true
                    ).show(supportFragmentManager, "HintDialog")
                }
            }
    }

    /**
     * 根据群成员显示 成员昵称 管理员 是否禁言等等
     */
    @SuppressLint("SetTextI18n")
    private fun showMemberTypeView(memberList: MutableList<GroupMemberBean>?) {
        mAdapter.notifyDataSetChanged()//刷新数据显示群成员头像 昵称
        groupMemberList.clear()
        bindView.tvTitleHint.text = "${getString(R.string.jiamichat)} (${memberList?.size ?: 1})"//显示在线人数
        memberList?.let { groupMemberList.addAll(it) }

        val tempList = mutableListOf<String>()
        memberList?.filter { g -> g.role.lowercase() != "normal" }
            ?.forEach { g -> tempList.add(g.id) }

        memberList?.forEach { g ->
            if (g.memberId == MMKVUtils.getUser()?.id) {
                groupInfo?.roleType = g.role
            }
        }

        //页面缓存
        owerAndManagerList.clear()
        owerAndManagerList.addAll(tempList)

        isMemberMute = isMemberMute(memberList)

        showMeteView(isMemberMute)
    }

    /**
     * 显示是否禁言view
     */
    private fun showMeteView(isMemberMute: Boolean) {
        if (isMute) {//群设置禁言
            //开启了群禁言
            if (owerAndManagerList != null && owerAndManagerList.size > 0) {
                if (!owerAndManagerList.contains(MMKVUtils.getUser()?.id)) {
                    //当前用户非群主或者管理员
                    var text =
                        if (isMemberMute) getString(R.string.你已被禁言) else getString(R.string.benqunjinyan)
                    bindView.inputPanelFrameLayout.setMute(text, null)
                } else {
                    //当前用户是管理员
                    if (isMemberMute) {
                        bindView.inputPanelFrameLayout.setMute(getString(R.string.你已被禁言), null)
                    } else {
                        //管理员或者群主，不会禁言
                        bindView.inputPanelFrameLayout.closeMute()
                    }
                }
            } else {
                //没有拿回群管理员数据，本地数据里面找
                if (groupInfo?.roleType == "Normal")
                    bindView.inputPanelFrameLayout.setMute(
                        getString(R.string.benqunjinyan),
                        null
                    )
            }
        } else {
            if (isMemberMute) {
                //给单个人禁言
                bindView.inputPanelFrameLayout.setMute(getString(R.string.你已被禁言), null)
            } else {
                //关闭群禁言
                bindView.inputPanelFrameLayout.closeMute()
            }
        }
    }

    /** 判断 对单个用户被禁言*/
    private fun isMemberMute(memberList: MutableList<GroupMemberBean>?): Boolean {
        val userId = MMKVUtils.getUser()?.id
        memberList?.forEach { m ->
            if (userId == m.memberId) {
                return m.allowSpeak == "N"
                return@forEach
            }
        }
        return groupInfo?.memberAllowSpeak == "N"
    }

    /**
     * 显示指定条目的数据
     */
    private fun scrollToPosition(chatMsgList: MutableList<ChatMessageBean>) {
        chatMsgList.forEachIndexed { i, c ->
            if (c.dbId.toString() == searchContentId) {
                isAutoScrollBottom = false
                bindView.listChat.scrollToPosition(i)
                return@forEachIndexed
            }
        }
    }

    /**
     * 显示指定条目的数据
     */
    private fun scrollToPosition(chatMsgList: MutableList<Any>, id: String) {
        chatMsgList.forEachIndexed { i, c ->
            if (c is ChatMessageBean && (c.id == id || c.editId == id)) {
                isAutoScrollBottom = false
                scrollToItem(i)
//                bindView.listChat.scrollToPosition(i)
                return@forEachIndexed
            }
        }
    }

    /**
     * 处理消息显示,生成时间提示
     */
    private fun processMsg(msg: ChatMessageBean) {
        if (msg.msgType == MsgType.MESSAGETYPE_NOTICE) {
            mAdapter.addData(msg)
        } else {
            try {
                val lastMsg =
                    mAdapter.data.last { it is ChatMessageBean && (it.msgType != MESSAGETYPE_TIME || it.msgType != MsgType.MESSAGETYPE_NOTICE) }
                lastMsg as ChatMessageBean

                //生成时间提示
                mViewModel.createDateMsg(msg, lastMsg)?.let {
                    mAdapter.addData(it)
                }
                mAdapter.addData(msg)
            } catch (e: NoSuchElementException) {
                //没有找到数据
                mViewModel.createDateMsg(msg, null)?.let {
                    mAdapter.addData(it)
                }
                mAdapter.addData(msg)
            }
        }
    }

    /**清理时间提示**/
    private fun clearDataHeader() {
        val deleteData = mutableListOf<Int>()
        for (i in (mAdapter.data.size - 1) downTo 0) {
            val msg = mAdapter.data[i]
            if (msg is ChatMessageBean && msg.msgType == MESSAGETYPE_TIME) {
                if (i == mAdapter.data.size - 1) {
                    deleteData.add(i)
                } else {
                    if (i > 0) {
                        val next = mAdapter.data[i - 1]
                        if (next is ChatMessageBean && next.msgType == MESSAGETYPE_TIME) {
                            deleteData.add(i - 1)
                        }
                    }
                }
            }
        }

        for (i in deleteData) {
            mAdapter.data.removeAt(i)
            mAdapter.notifyItemRemoved(i)
        }
    }

    /**发送转发消息***/
    private fun sendForwardMsg() {
        recordBean.let { r ->
            mViewModel.forwardMsg(r, getToId(), getChatType())
        }
    }

    override fun onInputPanelExpanded() {
    }

    override fun onInputPanelCollapsed() {
    }

    override fun onRecordSuccess(audioFile: String?, duration: Int) {
        //录音成功
        if (!TextUtils.isEmpty(audioFile)) {
            if (getChatType() == CHAT_TYPE_GROUP_SEND) {
                showLoading(getString(R.string.发送语音中))
            }

            var parentId = ""
            if (mWaitReplyMsg != null) {
                parentId = mWaitReplyMsg?.id ?: ""
            }

            mViewModel.sendAudioMsg(audioFile ?: "", duration, getToId(), getChatType(), parentId)
            bindView.inputPanelFrameLayout.hideReplyView()
        }
    }

    override fun clickSendButton(str: String?) {
        str?.let {
            if (str.length > MAXSTRINGLENGTH) {
                val size = if (str.length % MAXSTRINGLENGTH == 0) {
                    str.length / MAXSTRINGLENGTH
                } else {
                    str.length / MAXSTRINGLENGTH + 1
                }
                for (i in 0 until size) {
                    val strSub = if (i == size - 1) {
                        str.subSequence(MAXSTRINGLENGTH * i, str.length)
                            .toString()
                    } else {
                        str.subSequence(MAXSTRINGLENGTH * i, MAXSTRINGLENGTH * (i + 1))
                            .toString()
                    }

                    var parentId = ""
                    if (mWaitReplyMsg != null) {
                        parentId = mWaitReplyMsg?.id ?: ""
                    }

                    //发送文本消息
                    mViewModel.sendMsg(strSub, getToId(), getChatType(), parentId)
                }
            } else {
                var parentId = ""
                if (mWaitReplyMsg != null) {
                    parentId = mWaitReplyMsg?.id ?: ""
                }

//                for (i in 0..100) {
//                    //发送文本消息
//                    mViewModel.sendMsg("$i", getToId(), getChatType(), parentId)
//                }

                //发送文本消息
                mViewModel.sendMsg(str, getToId(), getChatType(), parentId)
            }
        }
    }

    /**发送gif图片消息*/
    override fun sendKeyboardImage(imgUrl: String?, width: Int, height: Int, isGifE: Boolean) {
        if (isGifE) {
            /**如果是发送gif表情图，必须弹框提示，限制发送太快*/
            HintDialog(
                getString(R.string.fasong),
                getString(R.string.你确定要发送GIf表情图),
                isCanTouchOutsideSet = false,
                iconId = R.drawable.ic_mine_header_group,
                callback = object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        var parentId = ""
                        if (mWaitReplyMsg != null) {
                            parentId = mWaitReplyMsg?.id ?: ""
                        }
                        mViewModel.sendImageUrlMsg(
                            imgUrl ?: "",
                            getToId(),
                            getChatType(),
                            width,
                            height,
                            parentId
                        )
                        bindView.inputPanelFrameLayout.hideReplyView()
                    }
                }
            ).show(supportFragmentManager, "HintDialog")
        } else {
            //直接发送输入法的图片
            var parentId = ""
            if (mWaitReplyMsg != null) {
                parentId = mWaitReplyMsg?.id ?: ""
            }
            mViewModel.sendImageUrlMsg(
                imgUrl ?: "",
                getToId(),
                getChatType(),
                width,
                height,
                parentId
            )
            bindView.inputPanelFrameLayout.hideReplyView()
        }
    }

    override fun editMsg(msg: ChatMessageBean) {
        msg.msgType = if (msg.content.isAtMsg()) {
            MESSAGETYPE_AT
        } else {
            MsgType.MESSAGETYPE_TEXT
        }
        mViewModel.modifyMsg(msg)
    }

    //待回复消息
    private var mWaitReplyMsg: ChatMessageBean? = null
    override fun replyMsgListener(parentMsg: ChatMessageBean?) {
        //回复消息
        mWaitReplyMsg = parentMsg
    }

    override fun onExtMenuClick(position: Int) {
        when (position) {
            0 -> {
                //选择相册
                ImageUtils.goSelImg(
                    this,
                    maxSelectNum = if (mChatType == 2) 1 else 20
                ) { localPath, w, h, time, listSize ->
                    if (listSize == 1 && !PatternUtils.isImageUrlGifMatcher(localPath)) {

                        try {
                            mImageFile =
                                File(
                                    cacheDir,
                                    localPath.substring(localPath.indexOfLast { it == '/' } + 1)
                                )

                            startActivityForResult(
                                Intent(this, IMGEditActivity::class.java)
                                    .putExtra(
                                        IMGEditActivity.EXTRA_IMAGE_URI,
                                        Uri.fromFile(File(localPath))
                                    )
                                    .putExtra(
                                        IMGEditActivity.EXTRA_IMAGE_SAVE_PATH,
                                        mImageFile?.absolutePath
                                    ),
                                REQ_IMAGE_EDIT
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        //发送图片消息
                        if (getChatType() == CHAT_TYPE_GROUP_SEND) {
                            showLoading(getString(R.string.发送图片中))
                        }

                        var parentId = ""
                        if (mWaitReplyMsg != null) {
                            parentId = mWaitReplyMsg?.id ?: ""
                        }
                        mViewModel.sendImageMsg(localPath, getToId(), getChatType(), w, h, parentId)
                        bindView.inputPanelFrameLayout.hideReplyView()
                    }
                }
            }

            1 -> {
                //拍照
                ImageUtils.goCamera(this) { localPath, w, h, time, listSize ->

                    if (listSize == 1 && !PatternUtils.isImageUrlGifMatcher(localPath)) {
                        try {
                            mImageFile =
                                File(
                                    cacheDir,
                                    localPath.substring(localPath.indexOfLast { it == '/' } + 1)
                                )
                            startActivityForResult(
                                Intent(this, IMGEditActivity::class.java)
                                    .putExtra(
                                        IMGEditActivity.EXTRA_IMAGE_URI,
                                        Uri.fromFile(File(localPath))
                                    )
                                    .putExtra(
                                        IMGEditActivity.EXTRA_IMAGE_SAVE_PATH,
                                        mImageFile?.absolutePath
                                    ),
                                REQ_IMAGE_EDIT
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        if (getChatType() == CHAT_TYPE_GROUP_SEND) {
                            showLoading("发送图片中...")
                        }
                        var parentId = ""
                        if (mWaitReplyMsg != null) {
                            parentId = mWaitReplyMsg?.id ?: ""
                        }

                        mViewModel.sendImageMsg(localPath, getToId(), getChatType(), w, h, parentId)
                        bindView.inputPanelFrameLayout.hideReplyView()
                    }

                }
            }

            2 -> {
                //发送视频
                ImageUtils.goRecordVideo(this) { localPath, w, h, time ->
                    //发送视频录制消息
                    if (getChatType() == CHAT_TYPE_GROUP_SEND) {
                        showLoading(getString(R.string.发送视频中))
                    }
                    var parentId = ""
                    if (mWaitReplyMsg != null) {
                        parentId = mWaitReplyMsg?.id ?: ""
                    }

                    mViewModel.sendVideoMsg(localPath, getToId(), getChatType(), w, h, parentId)
                    bindView.inputPanelFrameLayout.hideReplyView()
                }
            }

            3 -> {
                //选择文件
                if (mChatType == 2) {
                    getString(R.string.暂不支持群发消息).toast()
                } else {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "*/*"
                    this.startActivityForResult(intent, REQ_FILE_SELECT)
                }
            }
        }
    }

    // 获取文件的真实路径
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //会自动去集合中找到对应的组件、请求码做返回
        if (resultCode == RESULT_OK) { //是否选择，没选择就不会继续
            if (requestCode == REQ_FILE_SELECT) {
                val uri = data!!.data //得到uri，后面就是将uri转化成file的过程。
                var localPath = GetFilePathFromUri.getFileAbsolutePath(this@ChatActivity, uri)
                var fileSize = FileUtils.getSize(localPath)
                var fileSizeFloat = FileUtils.getFileLength(localPath)
                "文件path=${localPath}".logD()
                "文件大小=${fileSize}".logD()
                "上传的文件过大---size M=${fileSizeFloat / 1024f / 1024f}".logE()

                if (fileSizeFloat / 1024f / 1024f < 150) {
                    var parentId = ""
                    if (mWaitReplyMsg != null) {
                        parentId = mWaitReplyMsg?.id ?: ""
                    }
                    mViewModel.sendFileMsg(localPath, fileSize, getToId(), getChatType(), parentId)
                    bindView.inputPanelFrameLayout.hideReplyView()
                } else {
                    "上传的文件过大---size=${fileSizeFloat}".logE()
                    HintDialog(
                        getString(R.string.zhuyi),
                        getString(R.string.wenjianguodatishi),
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                            }
                        },
                        R.drawable.ic_dialog_top
                    ).show(supportFragmentManager, "HintDialog")
                }

            } else if (requestCode == REQ_IMAGE_EDIT) {
                var width = 0
                var height = 0
                if (data != null) {
                    width = data.getIntExtra("imgWidth", 0)
                    height = data.getIntExtra("imgHeight", 0)
                }
                //发送图片消息
                if (getChatType() == CHAT_TYPE_GROUP_SEND) {
                    showLoading("发送图片中...")
                }
                var parentId = ""
                if (mWaitReplyMsg != null) {
                    parentId = mWaitReplyMsg?.id ?: ""
                }
                if (mImageFile != null) {
                    mViewModel.sendImageMsg(
                        mImageFile!!.absolutePath,
                        getToId(),
                        getChatType(),
                        width,
                        height,
                        parentId
                    )
                    bindView.inputPanelFrameLayout.hideReplyView()
                }
            }

        }
    }

    /**
     * 获取toId
     */
    private fun getToId(): String {
        return when (mChatType) {
            //单聊
            0 -> chatInfo?.id ?: ""
            //群聊
            1 -> groupInfo?.id ?: ""
            else -> ""
        }
    }

    /**
     * 获取聊天类型
     */
    private fun getChatType(): String {
        return when (mChatType) {
            //单聊
            0 -> ChatType.CHAT_TYPE_FRIEND
            //群聊
            1 -> ChatType.CHAT_TYPE_GROUP
            //群发消息
            2 -> CHAT_TYPE_GROUP_SEND
            else -> ""
        }
    }

    /**
     * @功能
     */
    override fun onNotifyListener(str: String?) {
        if (mChatType == 1) {//群聊才有@
            isAutoHideKeyWord = false
            bindView.coordinator.visible()

            val atMsg = ChatDao.getGroupDb().getMemberByGroupId(mTargetId).apply {
                add(0, GroupMemberBean(name = getString(R.string.所有人), memberId = "0000000000000000000"))
                this.forEach { it.atStr = str ?: "" }
            }
            //测试数据
            mAtAdapter.setList(atMsg)
        }
    }

    /**
     * 关闭@功能
     */
    override fun onCancenAt() {
        isAutoHideKeyWord = true
        bindView.coordinator.gone()
    }

    /**
     * 搜索过滤数据
     */
    override fun onSearchMem(keyWord: String) {
        isAutoHideKeyWord = false
        bindView.coordinator.visible()
        val list = ChatUtils.searchAtMem(
            ChatDao.getGroupDb().getMemberByGroupId(mTargetId),
            keyWord.substring(1, keyWord.length)
        )
        list?.forEach { it.atStr = keyWord ?: "" }
        mAtAdapter.setList(list)
    }

    private var isAutoHideKeyWord = true//是否消息

    /**
     * 处理点击软键盘之外的空白处，隐藏软件盘
     *
     * @param ev
     * @return
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (isAutoHideKeyWord && checkMotionEventInView(ev, bindView.listChat)) {
                val imm =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                currentFocus?.let {
                    imm.hideSoftInputFromWindow(it.windowToken, 0)
                }
                bindView.inputPanelFrameLayout.closeConversationInputPanel()
            }
            return super.dispatchTouchEvent(ev)
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)

//        return super.dispatchTouchEvent(ev);
    }

    private fun checkMotionEventInView(ev: MotionEvent, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        return ev.rawX > location[0] &&
                ev.rawX < location[0] + view.width &&
                ev.rawY > location[1] &&
                ev.rawY < location[1] + view.height
    }

    override fun isTranslucentStatus(): Boolean {
        return false
    }

    override fun isAutoHideKeyBord(): Boolean {
        return false
    }

    private var collectMsg: ChatMessageBean? = null

    /**
     * 聊天item事件
     */
    private val onChatItemListener = object : OnChatItemListener {
        override fun onItemClick(bean: ChatMessageBean, position: Int) {
        }

        override fun onItemHeaderClick(data: ChatMessageBean) {
            //点击头像
            if (groupInfo?.roleType?.uppercase() == "admin".uppercase()
                || groupInfo?.roleType?.uppercase() == "owner".uppercase()
            ) {
                val groupMember = ChatDao.getGroupDb().getMemberInGroup(data.from, mTargetId)
                val chatInfo = groupMember?.let { ChatMsgUtils.groupMemberCopyFriendListBean(it) }
                if (chatInfo != null)
                    startActivity(
                        Intent(mActivity, FriendInfoActivity::class.java)
                            .putExtra(CHAT_INFO, chatInfo as Serializable)
                            .putExtra(ContactActivity.IN_TYPE, 1)
                    )
            }
        }

        override fun onItemHeaderLongClick(type: Int, data: ChatMessageBean) {
            when (type) {
                0 -> {
                    //长按@好友
                    ChatDao.getGroupDb().getMemberById(data.from)?.let {
                        bindView.inputPanelFrameLayout.longClickappendAtText(
                            it.nickname + " ",
                            it.id
                        )
                    }
                }

                1 -> {
                    var member = ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)
                    var headUrl = member?.headUrl ?: ""
                    var name = member?.nickname ?: getString(R.string.此人)
                    //踢出群聊
                    HintDialog(
                        getString(R.string.yichuqunliao),
                        String.format(getString(R.string.yichutishi),name),
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                //踢出群聊
                                mViewGroupModel.deleteGroupMember(data.groupId, data.from)
                            }
                        },
                        R.drawable.ic_mine_header,
                        headUrl = headUrl,
                        isShowHeader = true, isTitleTxt = true
                    ).show(supportFragmentManager, "HintDialog")
                }

                2 -> {
                    //禁言
                    var member = ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)
                    var headUrl = member?.headUrl ?: ""
                    var name = member?.nickname ?: "此人"
                    var isMute = member?.allowSpeak == "N"
                    HintDialog(
                        if (isMute) getString(R.string.取消禁言) else getString(R.string.设置禁言),
                        if (isMute) String.format(getString(R.string.quxiaojinyan),name) else String.format(getString(R.string.shezhijinyan),name),
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                //设置禁言
                                var strMute = if (isMute) "Y" else "N"
                                mViewGroupModel.putGroupMemberMute(data.groupId, data.from, strMute)
                            }
                        },
                        R.drawable.ic_mine_header,
                        headUrl = headUrl,
                        isShowHeader = true, isTitleTxt = true
                    ).show(supportFragmentManager, "HintDialog")
                }
            }
        }

        override fun clickReplyMsg(data: ChatMessageBean) {
            //点击回复消息跳转
            scrollToPosition(mAdapter.data, data.id)
        }

        override fun readCallBack(data: ChatMessageBean) {
            //消息已读回执(音频消息，点击之后才是已读)
            if (data.msgReadState == 0) {
                mViewModel.sendReadState(data.id, mTargetId, getChatType(), data.from)
                data.msgReadState = 1
            }
        }

        override fun reSendMsg(data: ChatMessageBean) {
            //重发消息
            HintDialog(
                getString(R.string.重发消息),
                getString(R.string.是否重发该消息),
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        //消息重发
                        mViewModel.reSendMsg(data)
                    }
                }
            ).show(supportFragmentManager, "HintDialog")
        }

        /**
         * 长按弹窗
         */
        override fun onPopMenuClickListener(
            data: ChatMessageBean,
            position: Int,
            type: ChatPopClickType
        ) {
            when (type) {
                ChatPopClickType.AddPhiz -> {
                    //添加表情
                    try {
                        if (currentGifNum >= MAX_GIF_NUM) {
                            String.format(getString(R.string.最多只能添加),"${MAX_GIF_NUM}").toast()
                            return
                        }
                        val imageMsg = GsonUtils.fromJson(data.content, ImageBean::class.java)
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val result =
                                    MeExpressionRepository.addEmojGIf(
                                        imageMsg.url,
                                        imageMsg.url,
                                        imageMsg.width,
                                        imageMsg.height
                                    )
                                if (result.code == 200) {
                                    getString(R.string.表情已添加成功).toast()
                                    mEmojViewModel.getEmojList()
                                } else if (result.code == 40102) {
                                    getString(R.string.表情包数量超出限制).toast()
                                } else {
                                    "GIF图片张,上传失败！".logE()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                    }
                }

                ChatPopClickType.Reply -> {
                    //回复
                    if (!bindView.inputPanelFrameLayout.isMute()) {
                        bindView.inputPanelFrameLayout.showReply(data)
                    }
                }

                ChatPopClickType.Top -> {
                    //置顶
                    setTopDialog(data)
                }

                ChatPopClickType.Copy -> {
                    //复制
                    val clipboard: ClipboardManager =
                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("simple text", data.content)
                    clipboard.setPrimaryClip(clip)
                    getString(R.string.消息内容已复制).toast()
                }

                ChatPopClickType.Forward -> {
                    //转发
                    val recordBean = ChatMsgUtils.chatMessageBeanCopyRecordBean(data)
                    SearchFriendActivity.start(mActivity, 3, recordBean)
                }

                ChatPopClickType.Collect -> {
                    //收藏
                    collectMsg = data
                    mViewModel.collect(data.content, data.msgType)
                }

                ChatPopClickType.Edit -> {
                    //编辑
                    if (!bindView.inputPanelFrameLayout.isMute()) {
                        if (data.msgType == MsgType.MESSAGETYPE_TEXT) {
                            //只有文字消息，@消息才能编辑
                            bindView.inputPanelFrameLayout.setEditMessage(data.content, data)
                        } else if (data.msgType == MESSAGETYPE_AT) {
                            //只有文字消息，@消息才能编辑
                            bindView.inputPanelFrameLayout.setEditMessage(
                                AtUserHelper.parseAtUserLinkJxEditText(
                                    data.content,
                                    ContextCompat.getColor(mActivity, R.color.color_at)
                                ), data
                            )
                        }
                    }
                }

                ChatPopClickType.Destory -> {
                    //删除消息
                    showDelDialog(data)
                }
            }
        }
    }


    /**
     * 删除弹框
     *
     * 消息删除方式:双向删除 Bilateral, 单向删除 Unilateral
     */
    private fun showDelDialog(chatMessageBean: ChatMessageBean) {
        var headUrl = ""
        var isAdmin = false
        if (chatMessageBean.chatType == ChatType.CHAT_TYPE_FRIEND) {
            headUrl = ChatDao.getFriendDb().getFriendById(chatMessageBean.from)?.headUrl ?: ""
            isAdmin = MMKVUtils.isAdmin()
        } else {
            headUrl = ChatDao.getGroupDb()
                .getMemberInGroup(chatMessageBean.from, chatMessageBean.groupId)?.headUrl ?: ""
            var groupInfo =
                chatMessageBean?.groupId?.let { ChatDao.getGroupDb().getGroupInfoById(it) }
            isAdmin = groupInfo?.roleType?.lowercase() != "normal"
        }

        SelectHintDialog(
            getString(R.string.zhuyi),
            getString(R.string.quedingxiaohui),
            object : ConfirmSelectDialogCallback {
                override fun onItemClick(isSelect: Boolean) {
                    var delMsgType = if (isSelect) {
                        //单向删除 Unilateral
                        "Unilateral"
                    } else {
                        //双向删除 Bilateral
                        "Bilateral"
                    }
                    deleteMsg(chatMessageBean, delMsgType)
                }
            },
            R.drawable.ic_mine_header,
            headUrl = headUrl,
            isShowLLSelectView = true,
            isShowHeader = true,
            isAdmin = true
        ).show(supportFragmentManager, "HintDialog")
    }

    private fun deleteMsg(data: ChatMessageBean, delMsgType: String) {
        ChatDao.getChatMsgDb().delMessage(data.dbId) {
            //删除回调
            mAdapter.data.remove(data)
            clearDataHeader()
        }
        mAdapter.notifyDataSetChanged()

        if (data.chatType == ChatType.CHAT_TYPE_FRIEND) {
            //删除好友消息
            if ("Modify" == data.operationType) {
                Log.e("Modify", data.id + "   " + data.editId)
                mViewModel.deleteMessage(
                    data.editId,
                    data.chatType,
                    "",
                    data.from,
                    data.to,
                    delMsgType
                )
            } else {
                mViewModel.deleteMessage(data.id, data.chatType, "", data.from, data.to, delMsgType)
            }
        } else if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
            //删除群聊消息
            if ("Modify" == data.operationType) {
                Log.e("Modify 群", data.id + "   " + data.editId)

                mViewModel.deleteMessage(
                    data.editId,
                    data.chatType,
                    data.groupId,
                    MMKVUtils.getUser()?.id ?: "",
                    "",
                    delMsgType
                )
            } else {
                mViewModel.deleteMessage(
                    data.id,
                    data.chatType,
                    data.groupId,
                    MMKVUtils.getUser()?.id ?: "",
                    "",
                    delMsgType
                )
            }
        }
    }

    /**
     * 播放音频
     */
    @Synchronized
    private fun playItemAudio(bean: ChatMessageBean, position: Int) {
        ChatUtils.playAudio(
            bean,
            position,
            onStart = {
                mAdapter.data.filterIsInstance<ChatMessageBean>().let { list ->
                    list.firstOrNull { f -> f.dbId == bean.dbId }?.let { b ->
                        b.isPlaying = true
                        if (b.dir == 0) {
                            //音频消息，发送已读回执
                            b.msgReadState = 1
                            if (mChatType == 0) {
                                mViewModel.sendReadState(b.id, mTargetId, getChatType(), b.from)
                            } else {
                                //更新数据库状态
                                ChatDao.getChatMsgDb().updateMsgReadState(1, b.id)
                            }
                        }
                        mAdapter.notifyItemChanged(position)
                    }
                }
            },
            onStop = {
                mAdapter.data.filterIsInstance<ChatMessageBean>().let { list ->
                    list.firstOrNull { f -> f.dbId == bean.dbId }?.isPlaying = false
                    mAdapter.notifyItemChanged(position)
                }
            },
            onComplete = {
                mAdapter.data.filterIsInstance<ChatMessageBean>().let { list ->
                    list.firstOrNull { f -> f.dbId == bean.dbId }?.isPlaying = false
                    mAdapter.notifyItemChanged(position)
                }
            })
    }

    override fun onLayoutChange(
        p0: View?, p1: Int, p2: Int, p3: Int, p4: Int,
        p5: Int, p6: Int, p7: Int, p8: Int
    ) {
        mAdapter.data.size.let {
//            "---p1=${p1}---p2=${p2}---p3=${p3}---p4=${p4}--${isFirstInit}---${isSelectMode}--${isAutoScrollBottom}".logD()
            if (!isFirstInit && !isSelectMode && isAutoScrollBottom) {
                bindView.listChat.smoothScrollToPosition(it)
            }
        }
    }

    /***处理群事件**/
    private fun processGroupAction(groupAction: GroupActionBean) {
        if (groupAction.groupId == mTargetId) {
            //只处理当前打开群组的事件
            when (groupAction.messageType) {
                ALLOWSPEAK -> {
                    //禁言
                    isMute = groupAction.setValue == "N"
                    showMeteView(isMemberMute)
                }

                SetMemberRole -> {
                    //设置群管理员,新增群主或管理员到本地数据
                    owerAndManagerList.add(groupAction.setValue)
                    if (groupAction.setValue == MMKVUtils.getUser()?.id) {
                        //当前用户角色变成了管理员
                        if (isMemberMute) {//现在是单个禁言状态
                            bindView.inputPanelFrameLayout.setMute(getString(R.string.你已被禁言), null)
                        } else {
                            //其他状态都不禁言
                            bindView.inputPanelFrameLayout.closeMute()
                        }
                        //管理员可以查看群成员资料
                        groupInfo?.roleType = "Admin"
                    }

                    //获取群主管理员id列表 主要是为了刷新头像上的管理员图标
                    mViewModel.getManagerList(mTargetId)
                }

                CancelMemberRole -> {
                    //取消群管理员,移除群主或管理员到本地数据
                    if (owerAndManagerList.contains(groupAction.setValue)) {
                        owerAndManagerList.remove(groupAction.setValue)
                    }
                    if (groupAction.setValue == MMKVUtils.getUser()?.id) {
                        //当前用户角色被取消管理员，如果是禁言状态
                        if (isMute) {
                            bindView.inputPanelFrameLayout.setMute(
                                getString(R.string.benqunjinyanzhong),
                                null
                            )
                        }
                        //不是管理员时 不可以查看群成员资料
                        groupInfo?.roleType = "Normal"
                    }

                    //获取群主管理员id列表 主要是为了刷新头像上的管理员图标
                    mViewModel.getManagerList(mTargetId)
                }

                GroupModifyName -> {
                    //修改群名称
                    bindView.tvTitle.text = groupAction.setValue
                    groupInfo?.name = groupAction.setValue
                }

                MsgType.NOTICE -> {
                    //修改群公告
                    groupInfo?.notice = groupAction.setValue
                }

                MsgType.HEADERURL -> {
                    //修改群头像
                    groupInfo?.headUrl = groupAction.setValue
                }

                MsgType.MemberAllowSpeak -> {
                    if (groupAction.targetId == MMKVUtils.getUser()?.id) {
                        //如果被操作的是自己
                        //被群主或者管理员禁言
                        if (groupAction.setValue == "N") {
                            //被禁言
                            isMemberMute = true
                        } else if (groupAction.setValue == "Y") {
                            //被允许发言
                            isMemberMute = false
                        }
                        showMeteView(isMemberMute)
                        groupMemberList.forEach {
                            if (it.memberId == groupAction.targetId) {
                                it.allowSpeak = groupAction.setValue
                            }
                        }
                        //保存到群成员缓存数据
                        ChatDao.getGroupDb().updateGroupMemberMute(
                            groupAction.groupId,
                            groupAction.targetId,
                            groupAction.setValue
                        )
                    }
                    if (groupAction.operatorId == MMKVUtils.getUser()?.id) {
                        //如果操作人是自己，处理多端同步
                        //保存到群成员缓存数据
                        ChatDao.getGroupDb().updateGroupMemberMute(
                            groupAction.groupId,
                            groupAction.targetId,
                            groupAction.setValue
                        )
                    }
                }
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="设置一个定时器 5秒发送一次">
    private var timer = Timer()
    private var timerTask: TimerTask? = null

    /**启动计时器 每5秒执行一次*/
    private fun startTimer() {
        timerTask = object : TimerTask() {
            override fun run() {
                when (mChatType) {
                    0 -> {
                        //获取在线状态
                        WebsocketWork.WS.sendFriendInLine(mTargetId)
                    }

                    1 -> {
                        //获取在线人数
//                        WebsocketWork.WS.sendGroupInLineNum(mTargetId)
                    }
                }
            }
        }
        timer.schedule(timerTask, 10, 60 * 1000)
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="置顶消息">
    private var index = 0
    private var mAdapterTop = BaseBinderAdapter()
    private fun showTopMsgView() {
        if (msgTopList.size > 0) {
            mAdapterTop.addItemBinder(
                MsgTopItem(
                    supportFragmentManager,
                    onItemClickListener = {
                        //滑动到指定行
                        scrollToPosition(mAdapter.data, it.messageId)
                        //收起rv界面
                        bindView.layoutTop.llRvTop.gone()
                        //显示layout
                        showLayoutTop(it)
                    },
                    onDelItemClickListener = {
//                        "-----------${groupInfo?.roleType}".logE()
                        if (groupInfo?.roleType?.lowercase() != "normal") {
                            showDelDialog(it)
                        }
                    })
            )
            bindView.layoutTop.rvTop.adapter = mAdapterTop
            bindView.layoutTop.ivTopOnUp.click {
                if (bindView.layoutTop.llTop.isVisible) {
                    bindView.layoutTop.llTop.gone()
                    bindView.layoutTop.llRvTop.visible()
                } else {
                    bindView.layoutTop.llRvTop.gone()
                    bindView.layoutTop.llTop.visible()
                }
            }
            setList(msgTopList)
            initLayout()
            if (isShowTopDefaultView) {
                bindView.viewDefault.visible()
            }
        } else {
            bindView.layoutTop.root.gone()
            bindView.viewDefault.gone()
        }
    }


    /**
     * 删除置顶消息
     */
    private fun showDelDialog(data: MsgTopBean) {
        HintDialog(
            groupInfo?.name ?: "",
            "您确定要删除群组中这则置顶消息吗？",
            object : ConfirmDialogCallback {
                override fun onItemClick() {
                    //只有群主管理员才能删除置顶消息
                    val topMsg = mutableListOf<String>()
                    topMsg.add(data.id)
                    //删除一条置顶消息
                    mViewModel.delTopInfoList(topMsg)
                }
            },
            R.drawable.ic_mine_header_group,
            headUrl = groupInfo?.headUrl,
            isShowHeader = true
        ).show(supportFragmentManager, "HintDialog")
    }

    /**
     * 设置 置顶消息
     */
    private fun setTopDialog(data: ChatMessageBean) {
        HintDialog(
            groupInfo?.name ?: "",
            getString(R.string.您想要在群组中置顶这则消息吗),
            object : ConfirmDialogCallback {
                override fun onItemClick() {
                    msgTopList.let {
                        if (msgTopList.size >= 5) {
                            getString(R.string.最多只能置顶5条消息).toast()
                        } else {
                            var id = data.id
                            if ("Modify" == data.operationType) {
                                id = data.editId//如果是编辑消息，就传编辑id
                            }
                            mViewModel.addTopInfoList(id, data.groupId)
                        }
                    }
                }
            },
            R.drawable.ic_mine_header_group,
            headUrl = groupInfo?.headUrl,
            isShowHeader = true
        ).show(supportFragmentManager, "HintDialog")
    }


    private fun showLayoutTop(m: MsgTopBean) {
        bindView.layoutTop.llTop.visible()
        msgTopList.forEachIndexed { i, msgTopBean ->
            if (msgTopBean.id == m.id) {
                index = i
                return@forEachIndexed
            }
        }
        TopUtil.showMsgContent(this@ChatActivity, bindView.layoutTop, msgTopList[index], index)
        TopUtil.showItemPoint(bindView.layoutTop, msgTopList.size, index)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initLayout() {
        index = msgTopList.size - 1
        bindView.layoutTop.root.visible()
//        bindView.viewDefault.visible()
        TopUtil.showItemPoint(bindView.layoutTop, msgTopList.size, index)
        TopUtil.showMsgContent(this@ChatActivity, bindView.layoutTop, msgTopList[index], index)
        bindView.layoutTop.llTop.click {
            if (msgTopList.size > (index + 1)) {
                index++
            } else {
                index = 0
            }
            TopUtil.showMsgContent(this@ChatActivity, bindView.layoutTop, msgTopList[index], index)
            TopUtil.showItemPoint(bindView.layoutTop, msgTopList.size, index)
            scrollToPosition(mAdapter.data, msgTopList[index].messageId)
        }
        bindView.layoutTop.ivTopOnDown.click {
            if (bindView.layoutTop.llTop.isVisible) {
                bindView.layoutTop.llTop.gone()
                bindView.layoutTop.llRvTop.visible()
            } else {
                bindView.layoutTop.llRvTop.gone()
                bindView.layoutTop.llTop.visible()
            }
        }

        /** 记录按下的坐标点（起始点）**/
        var mPosY = 0f
        var mPosYll = 0f

        /** 记录移动后抬起坐标点（终点）**/
        var mCurPosY = 0f
        var mCurPosYll = 0f
        bindView.layoutTop.llTop.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mPosYll = event.y
//                        "--------DOWN-mCurPosYll=${mCurPosYll}---mPosYll=${mPosYll}----${mCurPosYll - mPosYll}".logE()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        mCurPosYll = event.y
//                        "--------MOVE-mCurPosYll=${mCurPosYll}---mPosYll=${mPosYll}----${mCurPosYll - mPosYll}".logD()
                    }

                    MotionEvent.ACTION_UP -> {
//                        "--------UP-mCurPosYll=${mCurPosYll}---mPosYll=${mPosYll}----${mCurPosYll - mPosYll}".logE()
                        if (mCurPosYll != -1f && mCurPosYll - mPosYll > 0
                            && (abs(mCurPosYll - mPosYll) > 50)
                        ) {
                            //向下滑動
                            if (bindView.layoutTop.llTop.isVisible) {
                                bindView.layoutTop.llTop.gone()
                                bindView.layoutTop.llRvTop.visible()
                            } else {
                                bindView.layoutTop.llRvTop.gone()
                                bindView.layoutTop.llTop.visible()
                            }
                            mCurPosYll = -1f
                            return true
                        }
                    }
                }
                return false
            }
        })

        bindView.layoutTop.rvTop.setOnTouchListener(object : View.OnTouchListener {
            var isConsumption = false
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mPosY = event.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (!isConsumption) {
                            mPosY = event.y
                            isConsumption = true
                        }
                        mCurPosY = event.y
                    }

                    MotionEvent.ACTION_UP -> {
                        if (mCurPosY - mPosY < -50) {
                            //向上滑動
                            if (bindView.layoutTop.llTop.isVisible) {
                                bindView.layoutTop.llTop.gone()
                                bindView.layoutTop.llRvTop.visible()
                            } else {
                                bindView.layoutTop.llRvTop.gone()
                                bindView.layoutTop.llTop.visible()
                            }
                            return true
                        } else if (mCurPosY - mPosY > 50) {
                            return true
                        } else {
                            isConsumption = false
//                            "--------UP-return false".logE()
                        }
                    }
                }
                return false
            }
        })
    }

    private fun setList(topMsgList: MutableList<MsgTopBean>) {
        topMsgList.forEachIndexed { index, msgTopBean ->
            msgTopBean.index = index + 1
        }
        mAdapterTop.setList(topMsgList)
    }
    //</editor-fold>

    fun getChartId() = mTargetId

    override fun onDestroy() {
        bindView.inputPanelFrameLayout.onDestroy()
        super.onDestroy()
        AudioPlayManager.getInstance().stopPlay()
        timer?.cancel()
        timerTask?.cancel()
        timerTask = null
        executorServiceSingle?.shutdownNow()
        try {
            if (null != receiver)
                unregisterReceiver(receiver)
        } catch (e: Exception) {
        }
    }

    private var isNeedRequestData = false
    override fun onResume() {
        super.onResume()
        if (!isNeedRequestData) {
            isNeedRequestData = true
        } else {
            requestData()
        }
    }
    override fun onRestart() {
        super.onRestart()
        mAdapter?.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        bindView.inputPanelFrameLayout.onActivityPause()
    }

    /**删除一个gif*/
    override fun onDelGifClick(id: String) {
        HintDialog(
            getString(R.string.zhuyi),
            getString(R.string.quedingshanchu),
            isShowBtnCancel = false,
            iconId = R.drawable.ic_mine_header_group,
            callback = object : ConfirmDialogCallback {
                override fun onItemClick() {
                    var gifIds = mutableListOf<String>()
                    gifIds.add(id)
                    mEmojViewModel.delEmoj(gifIds)
                }
            }
        ).show(supportFragmentManager, "HintDialog")
    }
}
