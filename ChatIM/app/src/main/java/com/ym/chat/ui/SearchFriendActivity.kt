package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.ym.base.ext.dp2Px
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.databinding.*
import com.ym.chat.db.AccountDao
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.ext.setColorAndString
import com.ym.chat.item.*
import com.ym.chat.utils.ChatType
import com.ym.chat.viewmodel.FriendViewModel
import java.util.*

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 添加好友-搜索
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class SearchFriendActivity : LoadingActivity() {
    private val bindView: ActivityFriendSearchBinding by binding()
    private val mViewModel = FriendViewModel()
    private val mAdapter = BaseBinderAdapter()
    private var mSearchKeyWord: String? = ""
    private var searchType = 0//搜索类型，搜索好友(0) 搜索群聊消息(1) 搜索好友聊天消息(2) 搜索好友或者群组 收藏转发消息(3) 首页全局搜索 （4）
    private var idStr = ""  //好友 或者 群 id
    private var headUrl = ""//好友 获取 群 头像url
    private var name = ""//好友 获取 群 名字
    private var recordBean: RecordBean? = null //转发消息实体 只有搜索好友或者群组 收藏转发消息(3) 用到
    private var conversationList = mutableListOf<ConversationBean>()
    private var searchContentId: String = "" //搜索消息id  搜索群聊消息(1) 搜索好友聊天消息(2) 用到

    companion object {
        val SEARCHTYPE = "searchType"
        val SEARCHID = "searchId"
        val HEADURL = "headUrl"
        val NAME = "name"
        val SENDCONTENT = "sendContent"

        /**
         * 搜索类型，搜索好友(0) 搜索群聊消息(1)  搜索好友聊天消息(2)  搜索好友或者群组 收藏转发消息(3)  首页全局搜索 （4）
         *  搜索群聊消息(1)  必须要传群组id headUrl  name
         *  搜索好友聊天消息(2) 必须要传好友id headUrl  name
         *  搜索好友或者群组 收藏转发消息(3) 必传转发内容
         */
        fun start(context: Context, searchType: Int, recordBean: RecordBean) {
            val intent = Intent(context, SearchFriendActivity::class.java)
            intent.putExtra(SEARCHTYPE, searchType)
            intent.putExtra(SENDCONTENT, recordBean)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        bindView.viewBack.click { finish() }
        bindView.tvSearchOption.click {
            searchStrVerify()
        }
        bindView.etContent.addTextChangedListener {
            val result = it?.toString()?.trim()
            mSearchKeyWord = result
            if (result?.length!! > 0) assemblyData(result) else {
                mAdapter.setList(mutableListOf())
                if (searchType == 3) {
                    //转发消息
                    val lists = mutableListOf<Any>()
                    getConversationInfoTop5()
                    setConversationInfo(lists)
                    mAdapter.setList(lists)
                }
            }

            if (searchType == 0) {
                if (!TextUtils.isEmpty(result)) {
                    bindView.tvSearchOption.visible()
                    bindView.tvSearchOption.text = result?.let { it1 ->
                        "${getString(R.string.sousuo_title)}$result".setColorAndString(
                            colorId = getColor(R.color.color_main),
                            str = it1,
                            strFront = getString(R.string.sousuo_title)
                        )
                    }
                } else {
                    bindView.tvSearchOption.gone()
                    bindView.tvSearchOption.text = ""
                }
            }
        }
        setEditTextInputSpace(bindView.etContent)
    }

    /**
     * 对输入框字符验证
     */
    private fun searchStrVerify() {
        mSearchKeyWord?.let { str ->
            if (str.isNotEmpty())
                if (str.uppercase() == MMKVUtils.getUser()?.username?.uppercase() || str == AccountDao.getAccountMobile())
                    getString(R.string.查询的会员是本人).toast()
                else
                    mViewModel.searchFriend(str)
        }
    }

    /**
     * 禁止EditText输入空格和换行符
     *
     * @param editText EditText输入框
     */
    private fun setEditTextInputSpace(editText: EditText) {
        val filter: InputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source == " " || source.toString().contentEquals("\n")) {
                ""
            } else {
                null
            }
        }
        editText.filters = arrayOf<InputFilter>(filter)
    }

    /**
     * 组装数据
     */
    private fun assemblyData(mSearchKeyWord: String) {
        val lists = mutableListOf<Any>()
        when (searchType) {
            0 -> {
                //查询好友列表和群组列表 检索友聊号跟手机号
                getFriendGroup(lists, mSearchKeyWord)
            }
            1 -> {
                //查询所有群组聊天消息
                getGroupChatMsg(lists, mSearchKeyWord)
            }
            2 -> {
                //查询所有好友聊天消息
                getFriendChatMsg(lists, mSearchKeyWord)
            }
            3 -> {
                //组装 搜索到好友 或 自己的群组
                getFriendGroup(lists, mSearchKeyWord)
                //组装最近会话数据
                setConversationInfo(lists)
            }
            4 -> {
                //查询好友列表和群组列表 检索友聊号跟手机号
                getFriendGroup(lists, mSearchKeyWord)
                //查询所有聊天消息
                getChatMsg(lists, mSearchKeyWord)
            }
        }
        mAdapter.setList(lists)
    }


    /**
     * 组装所有聊天消息
     */
    private fun getChatMsg(lists: MutableList<Any>, mSearchKeyWord: String) {
        //查询所有群聊消息
        var contentList = ChatDao.getChatMsgDb().getAllMsgListBySearchContent(mSearchKeyWord)
        var isSearchResult = false
        if (contentList.size > 0) {
            var friendList = ChatDao.getFriendDb().getFriendList()
            var groupList = ChatDao.getGroupDb().getGroupList()
            contentList.forEach {
                if (it.chatType == ChatType.CHAT_TYPE_FRIEND) {
                    var friendId = if (MMKVUtils.getUser()?.id == it.from) it.to else it.from
                    var isFriend = false
                    friendList?.forEach { if (it.friendMemberId == friendId) isFriend = true }
                    if (isFriend) {
                        if (!isSearchResult)
                            addTitleView(lists)
                        isSearchResult = true
                        var friendInfo = ChatDao.getFriendDb().getFriendById(friendId)
                        var friend = FriendListBean()
                        friend.id = friendId
                        friend.friendMemberId = friendId
                        friend.name = friendInfo?.name ?: ""
                        friend.headUrl = friendInfo?.headUrl ?: ""
                        friend.searchToSendMemberId = MMKVUtils.getUser()?.id ?: ""
                        friend.searchContent = it.content
                        friend.searchContentId = it.dbId.toString()
                        friend.isSearch = true
                        friend.searchStr = mSearchKeyWord
                        lists.add(friend)
                        lists.add(DividerBean())
                    }
                } else {
                    var isGroup = false
                    groupList?.forEach { g -> if (g.id == it.groupId) isGroup = true }
                    if (isGroup) {
                        if (!isSearchResult)
                            addTitleView(lists)
                        isSearchResult = true
                        var groupInfo = ChatDao.getGroupDb().getGroupInfoById(it.groupId)
                        var group = GroupInfoBean()
                        group.id = groupInfo?.id ?: ""
                        group.name = groupInfo?.name ?: ""
                        group.headUrl = groupInfo?.headUrl ?: ""
                        group.searchContent = it.content
                        group.searchContentId = it.dbId.toString()
                        group.isSearch = true
                        group.searchStr = mSearchKeyWord
                        lists.add(group)
                        lists.add(DividerBean())
                    }
                }
            }
        }
    }

    private fun addTitleView(lists: MutableList<Any>) {
        lists.add(
            TxtSimpleBean(
                heightDp = 36f,
                txt = getString(R.string.聊天记录),
                txtColor = Color.parseColor("#BABABA"),
                bgColor = Color.parseColor("#EDEDED"),
                paddingStartPx = 14.dp2Px(),
                gravity = Gravity.CENTER_VERTICAL,
                textSizeSp = 14f,
            )
        )
    }

    /**
     * 组装群组列表数据
     */
    private fun getGroupChatMsg(lists: MutableList<Any>, mSearchKeyWord: String) {
        //查询所有群聊消息
        var contentList = ChatDao.getChatMsgDb().getMsgListByGroupId(idStr, mSearchKeyWord)
        "contentList==${contentList.size}".logD()
        if (contentList.size > 0) {
            lists.add(
                TxtSimpleBean(
                    heightDp = 36f,
                    txt = getString(R.string.群聊记录),
                    txtColor = Color.parseColor("#BABABA"),
                    bgColor = Color.parseColor("#EDEDED"),
                    paddingStartPx = 14.dp2Px(),
                    gravity = Gravity.CENTER_VERTICAL,
                    textSizeSp = 14f,
                )
            )
            contentList.forEach {
                var group = GroupInfoBean()
                group.id = idStr
                group.name = name
                group.headUrl = headUrl
                group.searchContent = it.content
                group.searchContentId = it.dbId.toString()
                group.isSearch = true
                group.searchStr = mSearchKeyWord
                lists.add(group)
                lists.add(DividerBean())
            }
        }
    }

    /**
     * 组装好友列表数据
     */
    private fun getFriendChatMsg(lists: MutableList<Any>, mSearchKeyWord: String) {
        var contentList = ChatDao.getChatMsgDb().getMsgListByTarget(idStr, mSearchKeyWord)
        if (contentList.size > 0) {
            lists.add(
                TxtSimpleBean(
                    heightDp = 36f,
                    txt = getString(R.string.聊天记录),
                    txtColor = Color.parseColor("#BABABA"),
                    bgColor = Color.parseColor("#EDEDED"),
                    paddingStartPx = 14.dp2Px(),
                    gravity = Gravity.CENTER_VERTICAL,
                    textSizeSp = 14f,
                )
            )
            contentList.forEach {
                var friend = FriendListBean()
                friend.id = idStr
                friend.friendMemberId = idStr
                friend.name = name
                friend.headUrl = headUrl
                friend.searchToSendMemberId = it.from
                friend.searchContent = it.content
                friend.searchContentId = it.dbId.toString()
                friend.isSearch = true
                friend.searchStr = mSearchKeyWord
                lists.add(friend)
                lists.add(DividerBean())
            }
        }
    }

    /**
     * 组装最近会话数据
     */
    private fun setConversationInfo(lists: MutableList<Any>) {
        lists.add(
            TxtSimpleBean(
                heightDp = 36f,
                txt = getString(R.string.最近聊天),
                txtColor = Color.parseColor("#BABABA"),
                bgColor = Color.parseColor("#EDEDED"),
                paddingStartPx = 14.dp2Px(),
                gravity = Gravity.CENTER_VERTICAL,
                textSizeSp = 14f,
            )
        )
        lists.addAll(conversationList)
    }

    /**
     * 组装 搜索到好友 或 自己的群组
     */
    private fun getFriendGroup(lists: MutableList<Any>, mSearchKeyWord: String) {
        //获取好友列表
        var friends = ChatDao.getFriendDb().getFriendList()
        "friend size=${friends.size}".logD()
        var isFriend = false//是否搜索到好友
        friends.forEach { f ->
            var remark = f.remark ?: ""
            if (remark.lowercase(
                    Locale.getDefault()
                ).contains(mSearchKeyWord.lowercase(Locale.getDefault())) || f.name.lowercase(
                    Locale.getDefault()
                ).contains(mSearchKeyWord.lowercase(Locale.getDefault())) || f.username.lowercase(
                    Locale.getDefault()
                ).contains(mSearchKeyWord.lowercase(Locale.getDefault())) || f.mobile.lowercase(
                    Locale.getDefault()
                ).contains(mSearchKeyWord.lowercase(Locale.getDefault()))
            ) {
                if (!isFriend) lists.add(
                    TxtSimpleBean(
                        heightDp = 36f,
                        txt = getString(R.string.已添加的好友),
                        txtColor = Color.parseColor("#BABABA"),
                        bgColor = Color.parseColor("#EDEDED"),
                        paddingStartPx = 14.dp2Px(),
                        gravity = Gravity.CENTER_VERTICAL,
                        textSizeSp = 14f,
                    )
                )//标题
                f.searchStr = mSearchKeyWord
                f.searchContentId = "-1"
                lists.add(f)//添加好友
                lists.add(DividerBean())//下划线
                isFriend = true
            }
        }
        //获取群组列表
        var groups = ChatDao.getGroupDb().getGroupList()
        var isGroup = false//是否搜索到群组
        groups.forEach { f ->
            if (f.name.lowercase(Locale.getDefault())
                    .contains(mSearchKeyWord.lowercase(Locale.getDefault()))
            ) {
                if (!isGroup) lists.add(
                    TxtSimpleBean(
                        heightDp = 36f,
                        txt = getString(R.string.已加入的群组),
                        txtColor = Color.parseColor("#BABABA"),
                        bgColor = Color.parseColor("#EDEDED"),
                        paddingStartPx = 14.dp2Px(),
                        gravity = Gravity.CENTER_VERTICAL,
                        textSizeSp = 14f,
                    )
                )//标题
                f.searchStr = mSearchKeyWord
                f.searchContentId = "-1"
                lists.add(f)//添加群组
                lists.add(DividerBean())//下划线
                isGroup = true
            }
        }
    }

    override fun requestData() {
        showSoftKeyboard()
        searchType = intent.getIntExtra(SEARCHTYPE, 0)
        when (searchType) {
            0 -> {
                bindView.etContent.hint = getString(R.string.搜索账号)
                bindView.etContent.imeOptions = EditorInfo.IME_ACTION_SEARCH
                bindView.etContent.isSingleLine = true
                bindView.etContent.setOnEditorActionListener(object :
                    TextView.OnEditorActionListener {
                    override fun onEditorAction(
                        v: TextView?,
                        actionId: Int,
                        event: KeyEvent?
                    ): Boolean {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            searchStrVerify()
                            return true
                        }
                        return false
                    }
                })
            }
            1 -> {
                idStr = intent.getStringExtra(SEARCHID).toString()
                headUrl = intent.getStringExtra(HEADURL).toString()
                name = intent.getStringExtra(NAME).toString()
                bindView.etContent.hint = getString(R.string.搜索群聊消息)
            }
            2 -> {
                idStr = intent.getStringExtra(SEARCHID).toString()
                headUrl = intent.getStringExtra(HEADURL).toString()
                name = intent.getStringExtra(NAME).toString()
                bindView.etContent.hint = getString(R.string.搜索好友聊天消息)
            }
            3 -> {
                bindView.etContent.hint = getString(R.string.搜索好友)
                recordBean = intent.getSerializableExtra(SENDCONTENT) as RecordBean
                val lists = mutableListOf<Any>()
                getConversationInfoTop5()
                setConversationInfo(lists)
                mAdapter.setList(lists)
            }
            4 -> bindView.etContent.hint = getString(R.string.sousuo)
        }
        mAdapter.addItemBinder(TxtSimpleItem())
        mAdapter.addItemBinder(SearchFriendItem(onItemClickListener = {
//            "跳转到私聊界面".toast()
            searchContentId = it.searchContentId
            if (searchType == 3) {
                showHintSendDialog(it.nickname, it.id, it.headUrl, 0)
            } else {
                var type = if (searchType == 4) {
                    //如果是全局搜索
                    if (searchContentId == "-1") 0 else 2
                } else {
                    getSendMsgType()
                }
                ChatActivity.start(this, it.id, 0, type, recordBean, searchContentId)
                searchTypeFinish()
            }
        }))
        mAdapter.addItemBinder(SearchGroupItem(onItemClickListener = {
//            "跳转到群聊界面".toast()
            searchContentId = it.searchContentId
            if (searchType == 3) {
                //判断这个群你是否被禁言
                var groupInfo = ChatDao.getGroupDb().getGroupInfoById(it.id)
//                "--------${groupInfo?.memberAllowSpeak}--------${groupInfo?.allowSpeak}---${groupInfo?.roleType}---${(groupInfo?.allowSpeak == "N" && groupInfo?.roleType == "Normal")}".logE()
                if (groupInfo?.memberAllowSpeak == "N" || (groupInfo?.allowSpeak == "N" && groupInfo?.roleType == "Normal")) {
                    getString(R.string.此群禁言状态).toast()
                } else {
                    showHintSendDialog(it.name, it.id, it.headUrl, 1)
                }
            } else {
                var type = if (searchType == 4) {
                    //如果是全局搜索
                    if (searchContentId == "-1") 0 else 2
                } else {
                    getSendMsgType()
                }
                ChatActivity.start(
                    this,
                    it.id,
                    1,
                    type,
                    recordBean,
                    searchContentId
                )
                searchTypeFinish()
                "contentList==${it.id}--${it.name}--${searchContentId}---${getSendMsgType()}".logD()
            }
        }))
        mAdapter.addItemBinder(HomeMsgItem(
            onItemClickListener = {
                when (it.type) {
                    0 -> {
                        //单聊
                        showHintSendDialog(it.name, it.chatId, it.img ?: "", 0)
                    }
                    1 -> {
                        //群聊
                        if (searchType == 3) {
                            //判断这个群你是否被禁言
                            var groupInfo = ChatDao.getGroupDb().getGroupInfoById(it.chatId)
                            if (groupInfo?.memberAllowSpeak == "N" || (groupInfo?.allowSpeak == "N" && groupInfo?.roleType == "Normal")) {
                                "此群禁言状态，不能转发消息".toast()
                            } else {
                                showHintSendDialog(it.name, it.chatId, it.img ?: "", 1)
                            }
                        } else {
                            showHintSendDialog(it.name, it.chatId, it.img ?: "", 1)
                        }
                    }
                }
            }
        )
        )
        mAdapter.addItemBinder(DividerItem())
//        mAdapter.setEmptyView(LayoutEmptyBinding.inflate(layoutInflater).root)

        bindView.rvList.adapter = mAdapter
        setEmptyView()
    }

    //<editor-fold defaultstate="collapsed" desc="设置空布局">
    private var mEmptyBind: FragmentNotifyLayoutEmptyBinding? = null
    private fun setEmptyView() {
        if (mEmptyBind == null)
            mEmptyBind = FragmentNotifyLayoutEmptyBinding.inflate(layoutInflater)
        mEmptyBind?.tvEmpty?.text = getString(R.string.暂无搜索内容)
        mAdapter.setEmptyView(mEmptyBind!!.root)
    }
    //</editor-fold>

    /**
     * 弹出软键盘
     */
    private fun showSoftKeyboard() {
        bindView.etContent.isFocusable = true
        bindView.etContent.isFocusableInTouchMode = true
        bindView.etContent.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /**
     * 获取最近会话信息 前5条记录
     */
    private fun getConversationInfoTop5() {
        conversationList.clear()
        var cList = ChatDao.getConversationDb().getConversationNotSystemList()
        var size = cList.size
        if (size > 5) size = 5
        conversationList.addAll(cList.slice(0 until size))
    }

    /**
     * 根据搜索类型 是否关闭activity
     */
    private fun searchTypeFinish() {
        if (searchType == 3) finish()
    }

    /**
     * 是否是转发消息
     */
    private fun getSendMsgType(): Int {
        return when (searchType) {
            3 -> 1
            1, 2, 4 -> 2
            else -> 0
        }
    }

    override fun observeCallBack() {
        //搜索结果
        mViewModel.searchResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    result.exc?.message.toast()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (result.data == null) {
                        getString(R.string.该用户不存在).toast()
                    } else {
                        SearchFriendResultActivity.start(this, result.data!!)
                    }
                }
            }
        }
    }

    /**
     *转发消息时候的提示
     */
    private fun showHintSendDialog(name: String, chatId: String, headUrl: String?, chatYType: Int) {
        HintDialog(
            getString(R.string.转发消息),
            String.format(getString(R.string.你需要转发给),name),
            headUrl = headUrl,
            callback = object : ConfirmDialogCallback {
                override fun onItemClick() {
                    ChatActivity.start(
                        this@SearchFriendActivity,
                        chatId,
                        chatYType,
                        getSendMsgType(),
                        recordBean
                    )
                    searchTypeFinish()
                }
            },
            iconId = if (chatYType == 0) R.drawable.ic_mine_header else R.drawable.ic_mine_header_group,
            isShowHeader = true, isTitleTxt = true
        ).show(supportFragmentManager, "HintDialog")
    }
}