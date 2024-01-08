package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.text.TextUtils
import android.view.Gravity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.dp2Px
import com.ym.base.ext.logD
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginData
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.SideBar
import com.ym.base.widget.ext.*
import com.ym.base.widget.sticky.StickyAnyAdapter
import com.ym.base.widget.sticky.StickyHeaderLinearLayoutManager
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.databinding.*
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.dialog.inputDialog
import com.ym.chat.enum.GroupMemberType
import com.ym.chat.item.DividerItem
import com.ym.chat.item.FriendContactItem
import com.ym.chat.item.TxtSimpleItem
import com.ym.chat.utils.PatternUtils
import com.ym.chat.viewmodel.ChatGroupViewModel
import com.ym.chat.viewmodel.FriendViewModel
import java.io.Serializable
import java.util.*

/**
 * 联系人
 * 0。创建群主 1。群发消息选择好友 2.群添加群成员 3。查看群所有成员
 */
class ContactActivity : LoadingActivity() {
    private val bindView: ActivityContactBinding by binding()
    private val mViewModel = FriendViewModel()
    private val mViewGroupModel = ChatGroupViewModel()
    private var mEmptyBind: LayoutEmptyBinding? = null

    private var isAllSelect = false//是否全选
    private var friendLists: MutableList<FriendListBean> = mutableListOf()
    private var friendAllLists = mutableListOf<Any>()
    private var searchFriendLists: MutableList<FriendListBean> = mutableListOf()
    private var searchFriendAllLists = mutableListOf<Any>()
    private var memberLists: MutableList<FriendListBean> = mutableListOf()//群成员列表
    private var inType = 0//0。创建群主 1。群发消息选择好友 2.群添加群成员 3。查看群所有成员
    private var groupId = ""//群组id
    private var removeIndex: Int = 0//移除成员索引
    private var memberType: Int = 2 //0群主 1管理员  2 成员
    private var maxMember = 200//群发消息最大选择人员上限

    //列表适配器
    private var mAdapter = object : StickyAnyAdapter() {
        override fun isStickyHeader(position: Int): Boolean {
            return this.data.size > position && this.data[position] is TxtSimpleBean
        }
    }

    companion object {
        val IN_TYPE = "in_type"
        val GROUP_ID = "group_id"
        val GROUP_MEMBER = "group_member"
        val MEMBERTYPE = "memberType"

        /**
         * @in_type，0。创建群主 1。群发消息选择好友 2.群添加群成员 3。查看群所有成员
         */
        fun start(
            context: Context,
            in_type: Int,
            groupId: String = "",
            memberType: Int = 0
        ) {
            val intent = Intent(context, ContactActivity::class.java)
            intent.putExtra(IN_TYPE, in_type)
            when (in_type) {
                2 -> {
                    intent.putExtra(GROUP_ID, groupId)
                    intent.putExtra(MEMBERTYPE, memberType)
                }
                3 -> {
                    intent.putExtra(GROUP_ID, groupId)
                    intent.putExtra(MEMBERTYPE, memberType)
                }
            }
            context.startActivity(intent)
        }
    }

    override fun initView() {
        getIntentData()

        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvSubtitle.click {
                isAllSelect = !isAllSelect
                setAllSelectState()
            }
        }

        bindView.tvComplete.click {
            when (inType) {
                0 -> {
                    val selectData = mAdapter.data.filter { it is FriendListBean && it.isSelect }
                    if (selectData.size > 100) {
                        "选择的人数不能大于100".toast()
                    } else {
                        showInputDialog()//创建群主，dialog
                    }
                }
                1 -> {
                    startChatActivity()//去ChatActivity发消息
                }
                2 -> {
                    val selectData = mAdapter.data.filter { it is FriendListBean && it.isSelect }
                    if (selectData.size > 100) {
                        "选择的人数不能大于100".toast()
                    } else {
                        inviteAddGroupMember(memberType)//调用添加好友接口
                    }
                }
            }
        }

        bindView.sideBar.setOnSelectListener(object : SideBar.OnSelectListener {
            override fun onSelect(s: String?) {
                if (!s.isNullOrBlank()) {
                    showSideBarView(s)
                }
            }

            override fun onMoveUp() = bindView.tvSelectBar.invisible()
        })

        mAdapter.addItemBinder(FriendContactItem(inputType = inType, onCheckListener = { p, b ->
            selectFriendChanged(p, b)
        }, onClickListener = { p, b ->
            if (inType == 3) {
                startActivity(
                    Intent(
                        this,
                        FriendInfoActivity::class.java
                    ).putExtra(ChatActivity.CHAT_INFO, b as Serializable)
                        .putExtra(IN_TYPE, 1)
                )
            } else {
                selectFriendChanged(p, b)
            }
        }, onLongClickListener = { type, p, memberBean ->
            if (inType == 3 && memberBean.id != MMKVUtils.getUser()?.id) {
                when (type) {
                    0 -> {//设置/取消管理员
                        var title = ""
                        var content = ""
                        var isDeleteAdmin = false
                        if (memberBean.role.lowercase() == "normal") {
                            title = "设置管理员"
                            content = "您确定要设置 ${memberBean?.nickname} 为管理员吗？ "
                        } else {
                            title = "取消管理员"
                            content = "您确定要取消 ${memberBean?.nickname} 的管理员吗?"
                            isDeleteAdmin = true
                        }
                        HintDialog(
                            title,
                            content,
                            object : ConfirmDialogCallback {
                                override fun onItemClick() {
                                    memberBean.id?.let { it2 ->
                                        mViewGroupModel.setMemberRole(
                                            groupId,
                                            it2,
                                            if (isDeleteAdmin) GroupMemberType.normal else GroupMemberType.admin
                                        )
                                        setAndDelAdminId = it2
                                        isDelAdmin = isDeleteAdmin
                                    }
                                }
                            },
                            iconId = R.drawable.ic_mine_header,
                            headUrl = memberBean?.headUrl,
                            isShowHeader = true, isTitleTxt = true
                        ).show(supportFragmentManager, "HintDialog")
                    }
                    1 -> {//设置/取消禁言
                        var title = ""
                        var content = ""
                        if (memberBean?.allowSpeak == "N") {
                            title = "解除禁言"
                            content = "您确定要解除 ${memberBean?.nickname} 禁言吗？ "
                        } else {
                            title = "设置禁言"
                            content = "您确定要对 ${memberBean?.nickname} 设置禁言吗?"
                        }
                        HintDialog(
                            title,
                            content,
                            object : ConfirmDialogCallback {
                                override fun onItemClick() {
                                    isMute = memberBean.allowSpeak == "N"
                                    var muteType = if (memberBean.allowSpeak == "N") "Y" else "N"
                                    memberId = memberBean.memberId
                                    mViewModel.putGroupMemberMute(
                                        memberBean.groupId,
                                        memberBean.memberId,
                                        muteType
                                    )
                                }
                            },
                            iconId = R.drawable.ic_mine_header,
                            headUrl = memberBean?.headUrl,
                            isShowHeader = true, isTitleTxt = true
                        ).show(supportFragmentManager, "HintDialog")
                    }
                    2 -> {//移除成员
                        HintDialog(
                            "注意",
                            "是否将${memberBean.nickname}移出群组？",
                            object : ConfirmDialogCallback {
                                override fun onItemClick() {
                                    memberBean.id?.let { it1 ->
                                        friendLists.forEachIndexed { index, f ->
                                            if (f.id == memberBean.id) removeIndex = index
                                        }
                                        mViewGroupModel.deleteGroupMember(groupId, it1)
                                        "removeIndex=$removeIndex".logD()
                                    }
                                }
                            },
                            iconId = R.drawable.ic_mine_header,
                            headUrl = memberBean?.headUrl,
                            isShowHeader = true
                        ).show(supportFragmentManager, "HintDialog")
                    }
                }
            }
        })).addItemBinder(TxtSimpleItem()).addItemBinder(DividerItem())

        //设置适配器
        bindView.recycler.layoutManager =
            StickyHeaderLinearLayoutManager<StickyAnyAdapter>(this)
        bindView.recycler.adapter = mAdapter
        bindView.sideBar.gone()

        //监听位置
        bindView.recycler.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) =
                    checkPosition2SideBar(recyclerView)
            })
        bindView.recycler.setPadding(
            0,
            0,
            0,
            52.dp2Px()
        )

        //执行搜索
        bindView.etSearch.addTextChangedListener {
            if (it != null) {
                //username name 中包含的字符   不区分大小写
                friendLists.filter { f ->
                    f.username.lowercase(Locale.getDefault())
                        .contains(it.toString().lowercase(Locale.getDefault())) ||
                            f.name.lowercase(Locale.getDefault())
                                .contains(it.toString().lowercase(Locale.getDefault()))
                }?.let {
                    searchFriendLists.clear()
                    searchFriendLists.addAll(it)
                    setDataHandle(searchFriendLists, searchFriendAllLists)
                    setViewShow()
                }
            }
        }
    }

    private fun selectFriendChanged(p: Int, b: FriendListBean) {
        var isSelect = false //是否选择了一个好友
        var selectNmb = 0
        friendAllLists.forEach { f ->
            if (f is FriendListBean && f.isSelect) {
                isSelect = true
                selectNmb++
            }
        }
        bindView.tvComplete.isEnabled = isSelect
    }

    //<editor-fold defaultstate="collapsed" desc="显示SideBar">
    var defaultDistance = 0f//相对sideBar顶部的间距
    var lineHeight = 0f//每行文字高度
    private fun showSideBarView(s: String) {
        val index = SideBar.characters.indexOf(s)
        if (defaultDistance == 0f) {
            val arr1 = IntArray(2)
            val arr2 = IntArray(2)
            bindView.tvSelectBar.getLocationOnScreen(arr1)
            bindView.sideBar.getLocationOnScreen(arr2)
            defaultDistance = arr2[1] - (arr1[1] + bindView.tvSelectBar.height / 2f)
            lineHeight =
                (bindView.sideBar.height - bindView.sideBar.paddingTop - bindView.sideBar.paddingBottom) * 1f / SideBar.characters.size
        }
        val distance =
            bindView.sideBar.paddingTop + lineHeight * index + lineHeight / 2f//相对控件顶部的间距
        bindView.tvSelectBar.translationY = defaultDistance + distance
        bindView.tvSelectBar.text = s
        bindView.tvSelectBar.visible()
        mAdapter.data.indexOfFirst { b -> (b as? TxtSimpleBean)?.txt == s }.let { i ->
            if (i >= 0) (bindView.recycler.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                i,
                0
            )
        }
    }
    //</editor-fold>

    /**
     * 邀请所选群成员进群
     */
    private fun inviteAddGroupMember(memberType: Int) {
        var memberLists = mutableListOf<String>()
        friendAllLists.forEachIndexed { index, any ->
            if (any is FriendListBean && any.isSelect) {
                any.id?.let { it1 -> memberLists.add(it1) }
            }
        }
        mViewGroupModel.inviteAddGroup(groupId, memberLists, memberType)
    }

    var isMute = false
    var memberId = ""

    /**
     * 此成员是否在禁言
     */
    private fun isMute(it: FriendListBean): Boolean {
        return it.allowSpeak == "N"
    }

    /**
     * 去chatActivity发送消息
     */
    private fun startChatActivity() {
        var friendIds: String = ""
        var friendNames: String = ""
        var ids = StringBuilder()
        var names = StringBuilder()
        friendAllLists.forEachIndexed { index, any ->
            if (any is FriendListBean && any.isSelect) {
                ids.append(any.id)
                ids.append(";")
                names.append(any.name)
                names.append(";")
            }
        }
        friendIds = if (ids.toString().contains(";")) ids.toString()
            .substring(0, ids.length - 1) else ""
        friendNames = if (names.toString().contains(";")) names.toString()
            .substring(0, names.length - 1) else ""
        ChatActivity.start(
            this,
            "",
            2,
            3,
            friendNames = friendNames,
            friendIds = friendIds
        )
        this.finish()
    }

    var setAndDelAdminId = ""//设置取消管理员id
    var isDelAdmin: Boolean = false //是否时候取消管理员

    private fun setAllSelectState() {
        if (isAllSelect) {
            bindView.toolbar.tvSubtitle.text = getString(R.string.quxiao) // "取消"
            bindView.tvComplete.isEnabled = true
            friendLists.forEachIndexed { index, friend ->
                friend.isSelect = true
            }
            setDataHandle(friendLists, friendAllLists)
        } else {
            bindView.toolbar.tvSubtitle.text = getString(R.string.全选) // "全选"
            bindView.tvComplete.isEnabled = false
            if (inType == 0 || inType == 2) {
                friendLists.forEachIndexed { index, friend ->
                    friend.isSelect = false
                }
            } else {
                friendLists.forEachIndexed { index, friend ->
                    if (index > 99) {
                        friend.isSelect = false
                    } else {
                        friend.isSelect = false
                    }
                }
            }
            setDataHandle(friendLists, friendAllLists)
        }
    }

    //<editor-fold defaultstate="collapsed" desc="跟数据类型显示界面">
    override fun requestData() {
        when (inType) {
            0 -> {
                //0。创建群主
                bindView.toolbar.tvSubtitle.text = getString(R.string.全选) // "全选"
                showCreateGroupAndSendGroupMsg()
            }
            1 -> {
                //1。群发消息选择好友
                bindView.toolbar.tvSubtitle.text = getString(R.string.全选) // "全选"
                showCreateGroupAndSendGroupMsg()
            }
            2 -> {
                //2.群添加群成员
                showAddGroupMember()
            }
            3 -> {
                //3。查看群所有成员
                groupId = intent.getStringExtra(GROUP_ID).toString()
                memberType = intent.getIntExtra(MEMBERTYPE, 0)
                bindView.toolbar.tvTitle.text = "全部群成员"
                bindView.toolbar.tvSubtitle.gone()
                bindView.flComplete.gone()
                showLookGroupMember()
            }
        }
    }

    private fun getIntentData(){
        intent?.let { intent ->
            inType = intent.getIntExtra(IN_TYPE, 0)
        }
    }
    /**
     * 查看群所有成员
     * 显示view
     */
    private fun showLookGroupMember() {
        var memberList = ChatDao.getGroupDb().getMemberByGroupId(groupId)
        var groupMemberList = mutableListOf<GroupMemberBean>()
        memberList.forEach { m ->
            var isRepeat = false
            groupMemberList.forEach { g ->
                if (m.id == g.id) {
                    isRepeat = true
                }
            }
            if (!isRepeat)
                groupMemberList.add(m)
        }
        var friendMembers = mutableListOf<FriendListBean>()
        memberList.forEachIndexed { index, m ->
            var friend = FriendListBean(dbId = index.toLong())
            friend.id = m.memberId ?: ""
            friend.friendMemberId = m.memberId ?: ""
            friend.name = m.nickname ?: ""
            friend.username = m.username ?: ""
            friend.headUrl = m.headUrl ?: ""
            friend.headText = m.headText ?: ""
            friend.displayHead = m.displayHead ?: ""
            friend.levelHeadUrl = m.levelHeadUrl ?: ""
            friend.role = m.role ?: ""
            friend.allowSpeak = m.allowSpeak ?: ""
            friend.groupId = m.groupId ?: ""
            friend.memberId = m.memberId ?: ""
            friend.role = m.role
            friend.isShowCheck = false //不显示右边的选择check按钮
//            "role====${friend.role}--${friend.id}--${friend.friendMemberId}".logD()
            friendMembers.add(friend)
        }
//        "=====memberList.size=${memberList.size}----friendMembers.size=${friendMembers.size}--friendAllLists.size=${friendAllLists.size}".logD()
        setDataHandle(friendMembers, friendAllLists)
        friendLists.clear()
        friendLists.addAll(friendMembers)
        setViewShow()
    }


    /**
     * 群添加群成员
     * 显示view
     */
    private fun showAddGroupMember() {
        groupId = intent.getStringExtra(GROUP_ID).toString()
        memberType = intent.getIntExtra(MEMBERTYPE, 0)
//        var mLists =
//            intent.getSerializableExtra(GROUP_MEMBER) as MutableList<FriendListBean>
//        memberLists.addAll(mLists)
        var memberList = ChatDao.getGroupDb().getMemberByGroupId(groupId)
        var friendMembers = mutableListOf<FriendListBean>()
        memberList?.forEachIndexed { index, m ->
            var friend = FriendListBean(dbId = index.toLong())
            friend.id = m.memberId ?: ""
            friend.friendMemberId = m.memberId ?: ""
            friend.name = m.nickname ?: ""
            friend.username = m.username ?: ""
            friend.headUrl = m.headUrl ?: ""
            friend.headText = m.headText ?: ""
            friend.displayHead = m.displayHead ?: ""
            friend.levelHeadUrl = m.levelHeadUrl ?: ""
            friend.role = m.role ?: ""
            friend.allowSpeak = m.allowSpeak ?: ""
            friend.groupId = m.groupId ?: ""
            friend.memberId = m.memberId ?: ""
//            "role====${friend.role}--${friend.id}--${friend.friendMemberId}".logD()
            friendMembers.add(friend)
        }
        memberLists.addAll(friendMembers)

        bindView.toolbar.tvTitle.text = getString(R.string.选择好友) // "选择好友"
        bindView.toolbar.tvSubtitle.text = getString(R.string.全选) // "全选"
//                    mViewModel.getFriendList()
        friendLists.clear()
        var friends = ChatDao.getFriendDb().getFriendList()
//        如果是添加群成员 不显示已添加的好友
        friends?.forEach { f ->
            var isHave = false//好友是否在成员列表中
            memberLists.forEach { m ->
                if (f.id == m.id) {
                    isHave = true
                }
            }
            if (!isHave) friendLists.add(f)
        }

        //显示右边的check按钮
        friendLists.forEach { f ->
            f.isShowCheck = true
        }

        setDataHandle(friendLists, friendAllLists)
        setViewShow()
    }

    /**
     * 0。创建群主 1。群发消息选择好友
     * 显示界面
     */
    private fun showCreateGroupAndSendGroupMsg() {
        bindView.toolbar.tvTitle.text = getString(R.string.选择好友) // "选择好友"
        var friends = ChatDao.getFriendDb().getFriendList()
//                    mViewModel.getFriendList()
        friendLists.clear()

        friendLists.addAll(friends ?: mutableListOf())
        //显示右边的check按钮
        friendLists.forEach { f ->
            f.isShowCheck = true
        }

        setDataHandle(friendLists, friendAllLists)
        setViewShow()
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="创建群聊">
    private var lastShowTime: Long = 0
    private fun showInputDialog() {
        if (System.currentTimeMillis() - lastShowTime < 500) return
        lastShowTime = System.currentTimeMillis()
        inputDialog(this, supportFragmentManager) {
            mCall = { name ->
                var members = mutableListOf<MemberBean>()
                members.add(MemberBean(MMKVUtils.getUser()?.id ?: ""))//添加自己
                friendAllLists.forEachIndexed { index, any ->
                    if (any is FriendListBean && any.isSelect) {
                        any.id?.let { MemberBean(it) }?.let {
                            members.add(it)
                        }
                    }
                }
                mViewGroupModel.createGroup(name, members)
                dismiss()
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="处理回调数据">
    override fun observeCallBack() {
        mViewModel.friendListResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    friendLists.clear()
                    if (inType == 2) {
                        //如果是添加群成员 不显示已添加的好友
                        result.data?.forEach { f ->
                            var isHave = false//好友是否在成员列表中
                            memberLists.forEach { m ->
                                if (f.id == m.id) {
                                    isHave = true
                                }
                            }
                            if (!isHave) friendLists.add(f)
                        }

                    } else {
                        friendLists.addAll(result.data ?: mutableListOf())
                    }

                    //显示右边的check按钮
                    friendLists.forEach { f ->
                        f.isShowCheck = true
                    }
                    //数据分组处理
                    setDataHandle(friendLists, friendAllLists)

                    setViewShow()

                    hideLoading()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        "获取数据失败".toast()
                    }
                }
            }
        }

        //创建群组
        mViewGroupModel.createGroupLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    //创建群组成功
                    hideLoading()
                    //保存到本地
                    ChatDao.getGroupDb().saveGroup(result.data?.data)
                    //跳转到聊天界面
                    result.data?.data?.id?.let {
                        ChatActivity.start(
                            this,
                            it,
                            1,
                            chatName = result.data?.data?.name ?: "",
                            chatHeader = result.data?.data?.headUrl ?: ""
                        )
                    }

                    //发送创建群广播
                    LiveEventBus.get(EventKeys.ADD_FRIEND, Boolean::class.java)
                        .post(true)//发广播给主页列表页面 更新界面
                    this.finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        "创建群组失败".toast()
                    }
                }
            }
        }

        //邀请群成员入群
        mViewGroupModel.addGroupLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
//                    "邀请群成员成功".toast()
                    LiveEventBus.get(EventKeys.ADD_GROUP_MEMBER, Boolean::class.java)
                        .post(true)//发关闭给群设置界面更新数据
                    this.finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        "邀请失败".toast()
                    }
                }
            }
        }


        //设置取消管理员
        mViewGroupModel.setMemberRoleLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (isDelAdmin) "取消管理员成功".toast() else "设置管理员成功".toast()
                    friendLists?.forEach { f ->
                        if (f.id == setAndDelAdminId) {
                            f.role = if (isDelAdmin) "normal" else "admin"
                        }
                    }
                    //更新列表
                    setDataHandle(friendLists, friendAllLists)
                    setViewShow()

                    //发广播给群设置界面更新数据
                    LiveEventBus.get(EventKeys.ADD_GROUP_MEMBER, Boolean::class.java)
                        .post(true)
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
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

                    LiveEventBus.get(EventKeys.ADMIN_DEL_MEMBER_GROUP, Boolean::class.java)
                        .post(true)

                    friendLists.removeAt(removeIndex)

                    //数据分组处理
                    setDataHandle(friendLists, friendAllLists)

                    setViewShow()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }


        //群设置 群成员禁言
        mViewModel.putGroupMemberMuteLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //刷新数据操作
                    mAdapter.data?.forEach {
                        if (it is FriendListBean) {
                            if (it.memberId == memberId) {
                                it.allowSpeak = if (isMute) "Y" else "N"
                                LiveEventBus.get(
                                    EventKeys.ADMIN_EDIT_MEMBER_GROUP_1,
                                    FriendListBean::class.java
                                ).post(it)
                            }
                        }
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

        /*接收到 设置了成员禁言 广播*/
        LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP, FriendListBean::class.java)
            .observe(this) { f ->
                mAdapter.data?.forEach {
                    if (it is FriendListBean) {
                        if (it.memberId == f.id) {
                            it.allowSpeak = f.allowSpeak
                        }
                    }
                }
            }


        /*群主设置信息 其他成员收到广播*/
        LiveEventBus.get(EventKeys.GROUP_ACTION, GroupActionBean::class.java)
            .observe(this) { groupAction ->
                if (inType == 3)
                    processGroupAction(groupAction)
            }

        /*管理员踢人 广播*/
        LiveEventBus.get(EventKeys.DEL_MEMBER_GROUP, String::class.java).observe(this) {
            //刷新数据操作
            if (inType == 3)
                Handler().postDelayed(Runnable {
                    showLookGroupMember()
                }, 1 * 1000) //延迟1秒执行
        }

        /*添加群成员 广播*/
        LiveEventBus.get(EventKeys.GROUP_ADD_MEMBER, String::class.java).observe(this) {
            //刷新数据操作
            if (inType == 3)
                Handler().postDelayed(Runnable {
                    showLookGroupMember()
                }, 1 * 1000) //延迟1秒执行
        }
    }
    //</editor-fold>

    /**
     * 是否显示 右侧选择 bar
     *  标题  右侧 全选按钮
     */
    private fun setViewShow() {
        if (mAdapter.data.isNotEmpty()) {
            if (inType != 3) {
                bindView.toolbar.tvSubtitle.visible()
            }
        }

        bindView.sideBar.visibleGone(mAdapter.data.isNotEmpty())
        if (mEmptyBind == null && bindView.etSearch.isVisible) {
            mEmptyBind = LayoutEmptyBinding.inflate(layoutInflater)
            if (bindView.root.height == 0) bindView.root.post {
                setEmptyView()
            } else setEmptyView()
        }
    }

    /**
     * 数据处理
     */
    private fun setDataHandle(fList: MutableList<FriendListBean>, fAllList: MutableList<Any>) {
        var listData = mutableListOf<Any>()
        var list = mutableListOf<FriendListBean>()
        if (inType == 3) {
            //如果是群成员信息 必须把群主放第一  管理员放第二  其他成员列表显示
            fList.forEach {
                if (it.role?.lowercase(Locale.getDefault()) == "owner".lowercase(
                        Locale.getDefault()
                    )
                ) {
                    //如果是群主
                    listData.add(setTxtTitle("群主"))
                    it.showLine = false
                    listData.add(it)
                }
            }
            var isAdmin = false//是否有管理员
            fList.forEach {
                if (it.role?.lowercase(Locale.getDefault()) == "admin".lowercase(
                        Locale.getDefault()
                    )
                ) {
                    //如果是管理员
                    if (!isAdmin) {
                        listData.add(setTxtTitle("管理员"))
                        isAdmin = true
                    }
                    it.showLine = true
                    listData.add(it)
                }
            }

            fList.filter { f ->
                f.role?.lowercase(Locale.getDefault()) == "normal".lowercase(
                    Locale.getDefault()
                )
            }?.let { list.addAll(it) }

        } else {
            list.addAll(fList)
        }

        if (list.isNotEmpty()) {
            list.sortBy { t -> t.fullChar }
            list.sortBy { t -> t.firstChar }
        }
        val resultList = mutableListOf<FriendListBean>()
        //非英文的放最后
        val otherList = list.filterNot { ff -> PatternUtils.isEnglish(ff.firstChar) }
        resultList.addAll(otherList)
        if (!otherList.isNullOrEmpty())
            resultList.sortBy { t -> t.createTimestamp }//按注册时间排序
        //有英文或者汉字的名字
        val englishList = list.filter { ff -> PatternUtils.isEnglish(ff.firstChar) }
        resultList.addAll(0, englishList)

        var char = ""
        resultList.forEachIndexed { index, b ->
            //非英文的统一放到#
            val firstChar = b.firstChar.uppercase()
            //分组
            if (firstChar != char) {
                char = firstChar
                if (list.size > 0) list[list.size - 1].showLine = false//每个组最后一个不显示分割线
                listData.add(setTxtTitle(char))
            }
            b.showLine = (list.size - 1) != index //不显示短线
            listData.add(b)
            if (list.size - 1 == index) listData.add(DividerBean()) //显示长线
        }
        fAllList.clear()
        fAllList.addAll(listData)
        mAdapter.setList(listData)
    }

    private fun setTxtTitle(s: String): TxtSimpleBean {
        return TxtSimpleBean(
            heightDp = 36f,
            txt = s,
            txtColor = Color.parseColor("#BABABA"),
            bgColor = Color.parseColor("#EDEDED"),
            paddingStartPx = 14.dp2Px(),
            gravity = Gravity.CENTER_VERTICAL,
            textSizeSp = 14f,
        )
    }

    //<editor-fold defaultstate="collapsed" desc="设置空布局">
    private fun setEmptyView() {
        mEmptyBind?.let { vb ->
            if (inType == 2) {
                vb.tvEmpty.text = "没有能入群的好友喔"
            }
            mAdapter.setEmptyView(vb.root)
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="滚动到对应位置显示">
    //列表第一个显示对于的位置
    private var mFirstPosition: Int = -1
    private fun checkPosition2SideBar(recyclerView: RecyclerView) {
        val p = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (p >= 0 && mFirstPosition != p && mAdapter.data.size > p) {
            mFirstPosition = p
            val data = mAdapter.data[p]
            if (data is TxtSimpleBean) {
                bindView.sideBar.updateSelectChar(data.txt)
            } else if (data is LoginData) {
                bindView.sideBar.updateSelectChar(data.firstChar)
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="处理群事件">
    /**
     * 处理群事件
     */
    private fun processGroupAction(groupAction: GroupActionBean) {
        if (groupAction.groupId == groupId) {
            //只处理当前打开群组的事件
            Handler().postDelayed(Runnable {
                showLookGroupMember()
            }, 1 * 1000) //延迟1秒执行

        }
    }
    //</editor-fold>
}