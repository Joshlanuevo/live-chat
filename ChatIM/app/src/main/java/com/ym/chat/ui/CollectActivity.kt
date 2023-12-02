package com.ym.chat.ui

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import android.provider.Browser
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.GsonUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.ext.toFile
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.databinding.ActivityCollectBinding
import com.ym.chat.databinding.LayoutEmptyBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.item.CollectContentItem
import com.ym.chat.item.CollectTimeItem
import com.ym.chat.rxhttp.MeExpressionRepository
import com.ym.chat.update.UpdateCollectProcessTask
import com.ym.chat.update.UpdateProcessTask
import com.ym.chat.utils.*
import com.ym.chat.utils.audio.AudioPlayManager
import com.ym.chat.viewmodel.CollectModel
import com.ym.chat.viewmodel.MeExpressionViewModel
import com.ym.chat.widget.panel.ConversationInputPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.Executors


/**
 * 收藏界面
 */
class CollectActivity : LoadingActivity(),
    ConversationInputPanel.OnConversationInputPanelStateChangeListener,
    ConversationInputPanel.SendMsgListener, ConversationInputPanel.OnExtClickListener,
    ConversationInputPanel.OnDelGifClickListener,
    ConversationInputPanel.NoticeFriendListener, View.OnLayoutChangeListener {
    private val bindView: ActivityCollectBinding by binding()
    private val mViewModel = CollectModel()
    private val mAdapter = BaseBinderAdapter()
    private var curPage: Int = 1
    private val pageSize: Int = 500
    private var records: MutableList<RecordBean> = mutableListOf()
    private var isRefresh = false //是否在刷新 加载
    private var delCollectId = ""//删除某条消息id
    private var editCollectId = ""//编辑某条消息id
    private var editCollectContent = ""//编辑某条消息的内容

    private var isAutoScrollBottom = true//是否自动滑到底部
    private var isShowLastMsg = true //是否显示最后一条消息
    private var isFirstInit = true  //是否第一次初始化
    private val mEmojViewModel: MeExpressionViewModel by viewModels()//emoj表情数据获取
    private val MAX_GIF_NUM = 30//表情限制
    private var currentGifNum = 0//当前表情数量
    private var executorServiceSingle = Executors.newCachedThreadPool() //线程池

    override fun initView() {
        //设置信号栏颜色值
        window.statusBarColor = getColor(R.color.color_F8F8F8)
        //设置状态栏字体颜色
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        bindView.viewBack.click {
            finish()
        }

        initInputPanel()
        initRVCollect()

        bindView.rvCollect.addOnLayoutChangeListener(this)
        bindView.rvCollect.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val manager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                //最后一个显示
                val lastShowPosition = manager.findLastVisibleItemPosition()
                //第一个显示
                val firstShowPosition = manager.findFirstVisibleItemPosition()
                if (lastShowPosition + 1 == mAdapter.itemCount) {
                    isAutoScrollBottom = true
                    isShowLastMsg = true
                } else {
                    isShowLastMsg = false
                }
                if (dy < 0) {
                    //向上滑动来消息不需要滑动到底部
                    isAutoScrollBottom = false
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    override fun requestData() {
        //获取表情数据
        mEmojViewModel.getEmojList()
        mViewModel.getCollectList(curPage.toString(), pageSize.toString())
    }

    private fun addItemBinder() {
        mAdapter.addItemBinder(CollectTimeItem())
            .addItemBinder(
                CollectContentItem(
                    onItemDownClickListener = {
                        //下载文件
                        downloadFile(it)
                    },
                    onItemAddEClickListener = {
                        //添加表情
                        try {
                            if (currentGifNum >= MAX_GIF_NUM) {
                                String.format(getString(R.string.最多只能添加),"${MAX_GIF_NUM}").toast()
                            } else {
                                val jsonObject = JSONObject(it.content)
                                val photoUrl = jsonObject.optString("url")
                                val high = jsonObject.optLong("high")
                                val width = jsonObject.optLong("width")
                                GlobalScope.launch(Dispatchers.IO) {
                                    try {
                                        val result =
                                            MeExpressionRepository.addEmojGIf(
                                                photoUrl,
                                                photoUrl,
                                                width.toInt(),
                                                high.toInt()
                                            )
                                        if (result.code == 200) {
                                            getString(R.string.表情已添加成功).toast()
                                            mEmojViewModel.getEmojList()
                                        } else if (result.code == 40102) {
                                            getString(R.string.表情包数量超出限制).toast()
                                        } else {
                                            getString(R.string.GIF图片张).logE()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                        }
                    },
                    onItemSendClickListener = {
                        //转发
                        SearchFriendActivity.start(this, 3, it)
                    },
                    onItemCopyClickListener = {
                        //复制
                        val clipboard: ClipboardManager =
                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("simple text", it.content)
                        clipboard.setPrimaryClip(clip)
                        "收藏内容已复制".toast()
                    },
                    onItemEditClickListener = {
                        //编辑
                        bindView.inputPanelFrameLayout.setEditMessage(
                            it.content,
                            getMessageBean(it)
                        )
                    },
                    onItemDelClickListener = {
                        //远程销毁
                        showDelDialog(it)
                    },
                    onPlayAudioClickListener = { item, position ->
                        //点击播放音频
                        var chatMsgBean = setChatMsgBean(item)
                        playItemAudio(chatMsgBean, position)
                    },
                    onPlayVideoClickListener = { item, position ->
                        //点击播放视频
                        var chatMsgBean = setChatMsgBean(item)
                        ChatUtils.playVideo(this@CollectActivity, chatMsgBean)
                    },
                    onCollectItemListener = {
                        //重发消息
                        showSendDialog(it)
                    },
                    onPictureClickListener = {
                        //点击图片
                        var mutableList = mutableListOf<String>()
                        records.forEach { a ->
                            if (a is RecordBean) {
                                if (a.type.uppercase() == MsgType.MESSAGETYPE_PICTURE.uppercase()) {
                                    val imageMsg =
                                        GsonUtils.fromJson(a.content, ImageBean::class.java)
                                    mutableList.add(imageMsg.url)
                                }
                            }
                        }
                        val imageMsg = GsonUtils.fromJson(it.content, ImageBean::class.java)
                        PictureActivity.start(
                            this,
                            imageMsg.url,
                            2,
                            pictureCollectUrlList = mutableList
                        )
                    })
            )
    }


    /**
     * 删除弹框
     */
    private fun showDelDialog(recordBean: RecordBean) {
        HintDialog(
            getString(R.string.zhuyi),
            getString(R.string.确定要删除消息),
            object : ConfirmDialogCallback {
                override fun onItemClick() {
                    var collectIds = mutableListOf<String>()
                    delCollectId = recordBean.id
                    collectIds.add(delCollectId)
                    //删除
                    mViewModel.delCollectList(collectIds)
                }
            },
            R.drawable.ic_dialog_top
        ).show(supportFragmentManager, "HintDialog")
    }

    private fun getMessageBean(it: RecordBean): ChatMessageBean {
        return ChatMessageBean(
            chatType = it.type,
            content = it.content,
            id = it.id,
            from = it.memberId,
            to = it.memberId
        )
    }

    private fun downloadFile(item: RecordBean) {
        try {
            val fileMsg = GsonUtils.fromJson(item.content, FileMsgBean::class.java)
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val (isExit, id) = DownloadUtil.queryExist(this@CollectActivity, fileMsg.url)
            if (isExit) {
                id?.let {
                    val uri = downloadManager.getUriForDownloadedFile(it)
                    openFileInBrowser(this@CollectActivity, uri, fileMsg.url)
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
                request.setDescription(getString(R.string.downloading)) //添加在通知栏里显示的描述
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI) //设置下载的网络类型
                request.setVisibleInDownloadsUi(false) //是否显示下载 从Android Q开始会被忽略
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) //下载中与下载完成后都会在通知中显示| 另外可以选 DownloadManager.Request.VISIBILITY_VISIBLE 仅在下载中时显示在通知中,完成后会自动隐藏
                val downloadId = downloadManager.enqueue(request) //加入队列，会返回一个唯一下载id
                executorServiceSingle.submit(
                    UpdateCollectProcessTask(
                        downloadManager,
                        downloadId,
                        item
                    )
                )
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            executorServiceSingle.shutdownNow()
            if (null != receiver)
                unregisterReceiver(receiver)
        } catch (e: Exception) {
        }
    }

    /**
     * 重发消息
     */
    private fun showSendDialog(recordBean: RecordBean) {
        HintDialog(
            "",
            "是否重发该消息？",
            object : ConfirmDialogCallback {
                override fun onItemClick() {
                    //消息重发
                    mViewModel.reSendMsg(recordBean)
                }
            },
            R.drawable.ic_dialog_top
        ).show(supportFragmentManager, "HintDialog")
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRVCollect() {
        addItemBinder()
        bindView.rvCollect.adapter = mAdapter
        bindView.refreshLayout.setEnableLoadMore(false)
        //下拉刷新 就是加载
        bindView.refreshLayout.setOnRefreshListener {
            curPage++
            isRefresh = true
            mViewModel.getCollectList(curPage.toString(), pageSize.toString())
        }
        //加载更多
        bindView.refreshLayout.setOnLoadMoreListener() {
            curPage = 1
            isRefresh = true
            mViewModel.getCollectList(curPage.toString(), pageSize.toString())
        }
    }

    /**
     * recordBean 转化 ChatMessageBean
     */
    private fun setChatMsgBean(item: RecordBean): ChatMessageBean {
        return ChatMessageBean().apply {
            this.msgId = item.createTime
            this.content = item.content
            this.id = item.id
            this.to = item.memberId
            this.msgType = item.type
        }
    }

    override fun observeCallBack() {
        //获取收藏列表
        mViewModel.collectResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    if (!isRefresh)
                        showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    result.data?.data?.let { showView(it) }
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    bindView.refreshLayout.finishRefresh()
                    bindView.refreshLayout.finishLoadMore()
                    isRefresh = false
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                    setEmptyView()
                }
            }
        }

        //删除收藏列表
        mViewModel.delCollectResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.删除成功).toast()
                    delSuccessShowView()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //编辑收藏消息
        mViewModel.putCollectContent.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
//                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
//                    hideLoading()
                    editSuccessShowView()
                }
                is BaseViewModel.LoadState.Fail -> {
//                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //收藏回调
        mViewModel.sendCollectResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
//                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
//                    hideLoading()
                }
                is BaseViewModel.LoadState.Fail -> {
//                    hideLoading()
                    getString(R.string.收藏失败).toast()
                }
            }
        }

        //其他端更新了gif图片
        LiveEventBus.get(EventKeys.UPDATE_GIF_MSG, Boolean::class.java).observe(this) {
            mEmojViewModel.getEmojList()
        }

        //下载更新
        LiveEventBus.get(EventKeys.FILE_DOWNLOAD_PROCESS, RecordBean::class.java).observe(this) {
            mAdapter.notifyDataSetChanged()
        }

        mEmojViewModel.delEmojResult.observe(this) {
            //删除gif
            mEmojViewModel.getEmojList()
        }

        mEmojViewModel.emojListResult.observe(this) {
            //表情数据
            currentGifNum = it.size
            it.add(0, EmojListBean.EmojBean(isAddDefault = true))
            it?.forEach { e -> e.isDel = true }
            bindView.inputPanelFrameLayout.setGifData(it)
        }

        //主动发消息回调
        mViewModel.sendCollectMsgResult.observe(this) {
            records.add(it)
            mAdapter.addData(it)
            bindView.rvCollect.scrollToPosition(mAdapter.data.size - 1)
        }

        //媒体消息回调
        LiveEventBus.get(EventKeys.EVENT_SEND_MSG, RecordBean::class.java).observe(this) {
            records.add(it)
            mAdapter.addData(it)
            bindView.rvCollect.scrollToPosition(mAdapter.data.size - 1)
        }

        //媒体消息发送消息的回调
        LiveEventBus.get(EventKeys.EVENT_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
            .observe(this) {
                records.forEach { r ->
                    if (r is RecordBean) {
                        if (it.createTime == r.createTime) {
                            r.sendState = it.sendState
                            r.id = it.id
                            mAdapter.notifyDataSetChanged()
                            return@forEach
                        }
                    }
                }
            }

        //重发媒体消息
        LiveEventBus.get(EventKeys.EVENT_RESEND_SEND_MSG_CHANGED_STATE, RecordBean::class.java)
            .observe(this) {
                records.forEach { r ->
                    if (r is RecordBean) {
                        if (it.createTimeEnd == r.createTime) {
                            r.sendState = it.sendState
                            r.createTime = it.createTime
                            mAdapter.notifyDataSetChanged()
                            return@forEach
                        }
                    }
                }
            }
    }

    /**
     * 删除清空数据成功
     * 刷新数据
     */
    private fun delSuccessShowView() {
        var delIndex = -1
        records.forEachIndexed { index, r ->
            if (r.id == delCollectId) {
                delIndex = index
                return@forEachIndexed
            }
        }
        if (delIndex != -1) {
            records.removeAt(delIndex)
            assemblyData()
            //如果删除的是最后一条数据，发通知在会话栏 更新界面
            var size = records.size
            if (size > 0) {//删除后收藏信息不为空，
                if (size == delIndex) {//且刚才删除的是最后一条数据
                    //删除后保存最后一条数据
                    var msg = records[size - 1]
                    ChatDao.getConversationDb()
                        .updateCollectLastMsg(
                            msg.type,
                            msg.content,
                            TimeUtils.getCollectDateTimestamp(msg.createTime)
                        )
                }
            } else {
                //如果收藏数据都删除完
                ChatDao.getConversationDb()
                    .updateCollectLastMsg(
                        "text",
                        getString(R.string.欢迎使用),
                        System.currentTimeMillis()
                    )
            }
            //发广播给 会话界面 刷新
            LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
        }
    }

    /**
     * 编辑消息成功
     * 刷新数据
     */
    private fun editSuccessShowView() {
        var data = mAdapter.data
        data?.forEachIndexed { index, r ->
            if (r is RecordBean) {
                if (r.id == editCollectId) {
                    var dataR = data[index] as RecordBean
                    //刷新列表界面
                    dataR.content = editCollectContent
                    mAdapter.notifyItemChanged(index)
                    //更新会话消息数据
                    var msg = dataR
                    ChatDao.getConversationDb()
                        .updateCollectLastMsg(
                            msg.type,
                            msg.content,
                            TimeUtils.getCollectDateTimestamp(msg.createTime)
                        )
                    //发广播给 会话界面 刷新
                    LiveEventBus.get(EventKeys.UPDATE_CONVER).post(null)
                    editCollectId = ""
                    editCollectContent = ""
                    return@forEachIndexed
                }
            }
        }
    }

    /**
     * 获取到数据，显示界面
     */
    private fun showView(result: CollectBean) {
        bindView.refreshLayout.finishRefresh()
        bindView.refreshLayout.finishLoadMore()
        isRefresh = false
        result.let {
            if (curPage == 1) {
                records.clear()
            }
            var recordList = result.records ?: mutableListOf()
            recordList.reverse()//倒叙取值
            records.addAll(0, recordList)
            assemblyData()
            result.records?.size?.let {
                if (curPage == 1) {
                    bindView.rvCollect.scrollToPosition(mAdapter.itemCount - 1)
                } else {
                    bindView.rvCollect.scrollToPosition(recordList.size)
                }
            }
            if (curPage * pageSize > it.total) {
                bindView.refreshLayout.setEnableRefresh(false)
            } else {
                bindView.refreshLayout.setEnableRefresh(true)
            }
        }
        setEmptyView()
    }

    /**
     * 重新组装数据
     */
    private fun assemblyData() {
        var date = ""
        val collects = mutableListOf<Any>()
        records?.forEach {
            var dateStr = TimeUtils.getStringDateYYYYMMDD(it.createTime)
            if (dateStr != date) {
                collects.add(RecordDateBean(it.createTime))
            }
            collects.add(it)
            date = dateStr
        }
        //如果是第一页数据
        if (curPage == 1) {
            //获取正在发送的媒体消息
            var list = ChatDao.getCollectDb().getCollectMsg()
            if (list != null && list.size > 0) {
                collects.addAll(list)
                records.addAll(list)
            }
        }
        mAdapter.setList(collects)
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
                mAdapter.data.filterIsInstance<RecordBean>().let { list ->
                    list.firstOrNull { f -> f.createTime == bean.msgId }?.let { b ->
                        b.isPlaying = true
                        mAdapter.notifyItemChanged(position)
                    }
                }
            },
            onStop = {
                mAdapter.data.filterIsInstance<RecordBean>().let { list ->
                    list.firstOrNull { f -> f.createTime == bean.msgId }?.isPlaying = false
                    mAdapter.notifyItemChanged(position)
                }
            },
            onComplete = {
                mAdapter.data.filterIsInstance<RecordBean>().let { list ->
                    list.firstOrNull { f -> f.createTime == bean.msgId }?.isPlaying = false
                    mAdapter.notifyItemChanged(position)
                }
            })
    }


    @Synchronized
    fun playVideo(context: Context, bean: ChatMessageBean) {
        //停止旧的音频播放
        ChatUtils.mLastPlayBean?.let { last ->
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

    //<editor-fold defaultstate="collapsed" desc="设置空布局">
    private var mEmptyBind: LayoutEmptyBinding? = null
    private fun setEmptyView() {
        if (mEmptyBind == null)
            mEmptyBind = LayoutEmptyBinding.inflate(layoutInflater)
        mEmptyBind?.tvEmpty?.text = getString(R.string.meiyoushuju)
        mAdapter.setEmptyView(mEmptyBind!!.root)
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="输入框">
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


        var list = mutableListOf<EmojListBean.EmojBean>().apply {
            for (i in 0..20) {
                add(EmojListBean.EmojBean())
            }
        }
        list?.forEach { e -> e.isDel = true }
        bindView.inputPanelFrameLayout.setGifData(list)
    }

    override fun clickSendButton(str: String?) {
        //发送文字消息
        mViewModel.sendMsg(str ?: "")
    }

    /**发送gif图片消息*/
    override fun sendKeyboardImage(imgUrl: String?, with: Int, height: Int, isGifE: Boolean?) {
        HintDialog(
            "",
            getString(R.string.你确定要发送GIf表情图),
            isCanTouchOutsideSet = false,
            iconId = R.drawable.ic_dialog_top,
            callback = object : ConfirmDialogCallback {
                override fun onItemClick() {
                    mViewModel.sendImageGifMsg(imgUrl ?: "", with, height)
                    bindView.inputPanelFrameLayout.hideReplyView()
                }
            }
        ).show(supportFragmentManager, "HintDialog")
    }

    override fun editMsg(editMsg: ChatMessageBean?) {
        //处理发送编辑后的消息
        editMsg?.let {
            editCollectId = it.id
            editCollectContent = it.content
            var cmb = mutableListOf<String>()
            cmb.add(it.id)
            mViewModel.putCollectContent(it.content, cmb)
        }
    }

    override fun replyMsgListener(parentMsg: ChatMessageBean?) {
        //回复消息
    }

    override fun onNotifyListener(str: String?) {
    }

    override fun onCancenAt() {
    }

    override fun isTranslucentStatus(): Boolean {
        return false
    }

    override fun isAutoHideKeyBord(): Boolean {
        return false
    }

    override fun onSearchMem(keyword: String?) {
    }

    override fun onExtMenuClick(position: Int) {
        when (position) {
            0 -> {
                //选择相册
                ImageUtils.goSelImg(
                    this,
                    maxSelectNum = 1
                ) { localPath, w, h, time, listSize ->
                    //发送图片消息
                    mViewModel.sendImageMsg(localPath, w, h)
                }
            }
            1 -> {
                //拍照
                ImageUtils.goCamera(this) { localPath, w, h, time, listSize ->
                    //发送图片消息
                    mViewModel.sendImageMsg(localPath, w, h)
                }
            }
            2 -> {
                //发送视频
                ImageUtils.goRecordVideo(this) { localPath, w, h, time ->
                    //发送视频录制消息
                    mViewModel.sendVideoMsg(localPath, w, h)
                }
            }
            3 -> {
                //选择文件
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                this.startActivityForResult(intent, 1)
            }
        }
    }


    // 获取文件的真实路径
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //会自动去集合中找到对应的组件、请求码做返回
        if (resultCode === RESULT_OK) { //是否选择，没选择就不会继续
            val uri = data!!.data //得到uri，后面就是将uri转化成file的过程。
            var localPath = GetFilePathFromUri.getFileAbsolutePath(this@CollectActivity, uri)
            var fileSize = FileUtils.getSize(localPath)
            var fileSizeLong = FileUtils.getFileLength(localPath)
            "文件path=${localPath}".logD()
            "文件大小=${fileSize}".logD()
            "上传的文件过大---size M=${fileSizeLong / 1024f / 1024f}".logE()
            if (fileSizeLong / 1024f / 1024f < 150) {
                mViewModel.sendFileMsg(localPath)
            } else {
//                "上传的文件不超过150M".toast()
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
        }
    }

    override fun onInputPanelExpanded() {
    }

    override fun onInputPanelCollapsed() {
    }

    /**发送语音消息*/
    override fun onRecordSuccess(audioFile: String?, duration: Int) {
        mViewModel.sendAudioMsg(audioFile ?: "", duration)
    }


    /**删除一个gif*/
    override fun onDelGifClick(id: String) {
        HintDialog(
            getString(R.string.zhuyi),
            getString(R.string.quedingshanchu),
            isShowBtnCancel = false,
            iconId = R.drawable.ic_hint_delete,
            callback = object : ConfirmDialogCallback {
                override fun onItemClick() {
                    var gifIds = mutableListOf<String>()
                    gifIds.add(id)
                    mEmojViewModel.delEmoj(gifIds)
                }
            }
        ).show(supportFragmentManager, "HintDialog")
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        mAdapter.data.size.let {
            if (!isFirstInit && isAutoScrollBottom) {
                bindView.rvCollect.smoothScrollToPosition(it)
            }
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="关闭输入框">
    /**
     * 处理点击软键盘之外的空白处，隐藏软件盘
     *
     * @param ev
     * @return
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (checkMotionEventInView(ev, bindView.rvCollect)) {
                val imm =
                    getSystemService(androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                currentFocus?.let {
                    imm.hideSoftInputFromWindow(it.windowToken, 0)
                }
                bindView.inputPanelFrameLayout?.closeConversationInputPanel();
            }
            return super.dispatchTouchEvent(ev)
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)
    }

    private fun checkMotionEventInView(ev: MotionEvent, view: View): Boolean {
        var location = IntArray(2)
        view.getLocationInWindow(location)
        return ev.rawX > location[0] &&
                ev.rawX < location[0] + view.width &&
                ev.rawY > location[1] &&
                ev.rawY < location[1] + view.height
    }
    //</editor-fold>
}