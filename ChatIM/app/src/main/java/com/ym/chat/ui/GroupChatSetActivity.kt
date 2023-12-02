package com.ym.chat.ui

import android.content.Intent
import android.text.TextUtils
import android.view.Gravity
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.copyToClipboard
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
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.dialog.HintThreeDialog
import com.ym.chat.dialog.inputDialog
import com.ym.chat.enum.GroupMemberType
import com.ym.chat.ext.roundLoad
import com.ym.chat.item.GroupMemberItem
import com.ym.chat.popup.ChatHeaderPopupWindow
import com.ym.chat.popup.GroupHeaderPopupWindow
import com.ym.chat.popup.SelectPhotoPopWindow
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ImageUtils.goCamera
import com.ym.chat.utils.ImageUtils.goSelImg
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.ToastUtils
import com.ym.chat.viewmodel.ChatGroupViewModel
import com.ym.chat.viewmodel.ChatViewModel
import org.json.JSONObject
import java.io.Serializable
import java.util.*

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 群聊设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class GroupChatSetActivity : LoadingActivity() {
    private val bindView: ActivityGroupChatsetBinding by binding()
    private var mViewModel = ChatGroupViewModel()
    private val mAdapter = BaseBinderAdapter()
    private var groupInfo: GroupInfoBean? = null
    private var members = mutableListOf<GroupMemberBean>()
    private var memberAll = mutableListOf<GroupMemberBean>()
    private var allowSpeak = "N"//开启禁言
    private var messageNotice = "N"
    private var headUrl = ""
    private var removeIndex: Int = 0//移除成员索引
    private var memberType: Int = 2 //0群主 1管理员  2 成员
    private var changedType: Int = -1 //我正在修改类型 0头像 1禁言 2免打扰
    private var isSwitchMute = true//设置免打扰后是否调用接口
    private var isSwitchMessage = true//设置是否通知后是否调用接口
    private var groupName = ""//群名
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.群设置)
        }
        //设置群头像
        bindView.ivGroupHead.click {
            showSelectPhotoPopWindow()
        }
        //设置群名
        bindView.llGroupName.click {
            showInputDialog()
        }
        bindView.tvGroupName.click {
            showInputDialog()
        }
        //长按复制
        bindView.tvGroupName.setOnLongClickListener {
            bindView.tvGroupName.text.toString().trim().copyToClipboard()
            ToastUtils.showToastWithImg(this, getString(R.string.已复制), R.drawable.ic_dialog_success)
            true
        }
        //跳转到查看群列表
        bindView.llMembers.click {
            startLookContactActivity()
        }
        //跳转到搜索界面
        bindView.llSearch.click {
            startSearchFriendActivity()
        }

        //消息免打扰
        bindView.switchMessage.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isSwitchMessage) {//第一次敷值不处理
                groupInfo?.id?.let {
                    messageNotice = if (isChecked) "N" else "Y"
                    mViewModel.putGroupMemberNotice(it, messageNotice = messageNotice)
                }
            } else {
                isSwitchMessage = true
            }
        }
        //是否禁言
        bindView.switchMute.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isSwitchMute) {//第一次敷值不处理
                groupInfo?.id?.let {
                    allowSpeak = if (isChecked) "N" else "Y"
                    mViewModel.putGroupInfo(it, allowSpeak = allowSpeak)
                    changedType = 1
                }
            } else {
                isSwitchMute = true
            }
        }
        //跳转到公告界面
        bindView.llNotice.click {
            groupInfo?.role = memberType
            startActivity(
                Intent(this, AnnouncementActivity::class.java)
                    .putExtra(AnnouncementActivity.INTYPE, 0)
                    .putExtra(AnnouncementActivity.GROUPINFO, groupInfo)
            )
        }
        //远程销毁群消息
        bindView.btnDestroy.click {
            //远程销毁群消息
            showDelMsgHintDialog()
        }

        //发言频率限制
        bindView.llSendMsgLimit.click {
            startActivity(Intent(this, SendMsgLimitActivity::class.java))
        }

        //解散 退出群组
        bindView.btnLogout.click {
            var title = ""
            var content = ""
            if (memberType == 0) {
                title = getString(R.string.解散群聊)
                content = String.format(getString(R.string.确定解散提示),groupName)
            } else {
                title = getString(R.string.tuichuqunliao)
                content = String.format(getString(R.string.确定离开提示),groupName)
            }
            HintDialog(
                title,
                content,
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        groupInfo?.id?.let { it1 ->
                            if (memberType == 0) {
                                //群主解散群
                                mViewModel.deleteGroup(it1)
                            } else {
                                //群成员退出群
                                mViewModel.leaveGroup(it1)
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


    override fun requestData() {
        intent?.let { intent ->
            groupInfo = intent.getSerializableExtra(ChatActivity.GROUP_INFO) as GroupInfoBean
            groupName = groupInfo?.name ?: ""
            bindView.tvGroupName.text = groupName
            bindView.ivGroupHead.roundLoad(groupInfo?.headUrl, R.drawable.ic_mine_header_group)
            bindView.tvNotice.text = groupInfo?.notice
        }
        groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }

        mAdapter.addItemBinder(GroupMemberItem(onItemClickListener = {
            if (it.id == "10086") {
                //跳转到添加群友
                startContactActivity()
//                if (memberType != 2) {
//                    startContactActivity()
//                }
            } else {
                startFriendInfoActivity(it)
//                if (memberType != 2) {
//                    startFriendInfoActivity(it)
//                }
            }
        }, onLongClickListener = { type, position, memberBean ->
            when (type) {
                0 -> {//设置/取消管理员
                    var title = ""
                    var content = ""
                    var isDeleteAdmin = false
                    if (memberBean?.role?.lowercase() == "normal") {
                        title = getString(R.string.设置管理员)
                        content = String.format(getString(R.string.设置管理员提示),memberBean?.nickname)
                    } else {
                        title = getString(R.string.取消管理员)
                        content = String.format(getString(R.string.取消管理员提示),memberBean?.nickname)
                        isDeleteAdmin = true
                    }
                    HintDialog(
                        title,
                        content,
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                groupInfo?.id?.let { it2 ->
                                    mViewModel.setMemberRole(
                                        it2,
                                        memberBean.memberId,
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
                        title = getString(R.string.解除禁言)
                        content = String.format(getString(R.string.解除禁言提示),memberBean?.nickname)
                    } else {
                        title = getString(R.string.设置禁言)
                        content =String.format(getString(R.string.设置禁言提示),memberBean?.nickname)
                    }
                    HintDialog(
                        title,
                        content,
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                var muteType = if (memberBean.allowSpeak == "N") "Y" else "N"
                                memberBean.allowSpeak = muteType
                                groupMemberBean = memberBean
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
                        getString(R.string.zhuyi),
                        String.format(getString(R.string.移出群组提示),memberBean?.nickname),
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                groupInfo?.id?.let { it1 ->
                                    removeIndex = position
                                    mViewModel.deleteGroupMember(it1, memberBean.memberId)
                                }
                            }
                        },
                        iconId = R.drawable.ic_mine_header,
                        headUrl = memberBean?.headUrl,
                        isShowHeader = true
                    ).show(supportFragmentManager, "HintDialog")
                }
            }
        }))
        bindView.rvMember.adapter = mAdapter

        var memberList = groupInfo?.id?.let {
            ChatDao.getGroupDb().getMemberByGroupId(it)
        }
        memberList?.let {
            showView(false, it)
            setMemberList(it)
        }
    }

    //<editor-fold defaultstate="collapsed" desc="修改群名称">
    private var lastShowTime: Long = 0
    private fun showInputDialog() {
        if (System.currentTimeMillis() - lastShowTime < 500) return
        lastShowTime = System.currentTimeMillis()
        inputDialog(this, supportFragmentManager) {
            mCall = { name ->
                groupName = name
                mViewModel.putGroupInfo(groupInfo?.id ?: "", name = name)
                dismiss()
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="处理函数">
    /**
     * 展示远程销毁 dialog
     */
    private fun showDelMsgHintDialog() {
        HintDialog(
            getString(R.string.远程销毁),
            String.format(getString(R.string.远程销毁提示),groupName),
            object : ConfirmDialogCallback {
                override fun onItemClick() {
                    groupInfo?.id?.let { it1 ->
                        mViewModel.deleteRemoteGroupMessage(it1)
                    }
                }
            },
            iconId = R.drawable.ic_mine_header_group,
            headUrl = groupInfo?.headUrl,
            isShowHeader = true, isTitleTxt = true
        ).show(supportFragmentManager, "HintDialog")
    }

    /**
     * 展示选择照片 或 拍照 popupWindow
     */
    private fun showSelectPhotoPopWindow() {
        SelectPhotoPopWindow(this)
            .apply {
                onItemClickListener = { data, position, view ->
                    dismiss()
                    when (position) {
                        0 -> goCamera(
                            activity,
                            true,
                            onResultCallBack = { localPath: String, w: Int, h: Int, time: Long, listSize: Int ->
                                updateImageFileGoogle(localPath)
                            })
                        1 -> goSelImg(
                            activity,
                            1,
                            true,
                            isGif = false,
                            onResultCallBack = { localPath: String, w: Int, h: Int, time: Long, listSize: Int ->
                                updateImageFileGoogle(localPath)
                            })
                    }
                }
            }.showPopupWindow()
    }

    /**
     *查看群成员
     */
    private fun startLookContactActivity() {
        groupInfo?.id?.let { it1 ->
            ContactActivity.start(
                this, 3,
                it1, memberType = memberType
            )
        }
    }

    private fun startSearchFriendActivity() {
        startActivity(
            Intent(
                this,
                SearchFriendActivity::class.java
            ).putExtra(SearchFriendActivity.SEARCHTYPE, 1)
                .putExtra(SearchFriendActivity.SEARCHID, groupInfo?.id ?: "")
                .putExtra(SearchFriendActivity.HEADURL, groupInfo?.headUrl ?: "")
                .putExtra(SearchFriendActivity.NAME, groupName ?: "")
        )
    }

    /**
     * 上传图片文件到谷歌云
     */
    private fun updateImageFileGoogle(localPath: String) {
        //1、上传图片
        ChatViewModel().uploadFile(localPath, "Picture", progress = {}, success = { result ->
            var headUrl = result.data.filePath
            bindView.ivGroupHead.roundLoad(headUrl, R.drawable.ic_mine_header_group)
            //提交群头像url 到群
            groupInfo?.id?.let {
                mViewModel.putGroupInfo(it, headUrl = headUrl)
                this.headUrl = headUrl
                changedType = 0
            }
        }, error = {
            getString(R.string.图片上传失败).toast()
        })
    }

    /**
     * 1。如果长按是自己的头像不坐处理
     * 2。如果是管理员 不能编辑 群主 其他 管理员  只能编辑成员的信息
     * 3。管理员不能设置成员为管理员
     */
    private fun showEditHintDialog(it: GroupMemberBean) {
        "role==${it.role}".logD()
        if (it.id != MMKVUtils.getUser()?.id) {
            when (memberType) {
                0 -> {//群主操作
                    if (it.role?.lowercase(Locale.getDefault()) == "normal".lowercase(
                            Locale.getDefault()
                        )
                    ) {
                        //设置管理员的dialog
                        setAdminDialog(it, isMute = isMute(it))
                    } else {
                        if (it.role?.lowercase(Locale.getDefault()) == "admin".lowercase(
                                Locale.getDefault()
                            )
                        ) {
                            //取消管理员的dialog
                            setAdminDialog(it, isMute = isMute(it), isDelAdmin = true)
                        }
                    }
                }
                1 -> {//管理员的操作
                    if (it.role?.lowercase(Locale.getDefault()) == "normal".lowercase(
                            Locale.getDefault()
                        )
                    ) {
                        //只能踢人
                        setAdminDialog(it, isMute = isMute(it), isShowFirstBtn = false)
                    }
                }
            }
        }
    }

    /**
     * 此成员是否在禁言
     */
    private fun isMute(it: GroupMemberBean): Boolean {
        return it.allowSpeak == "N"
    }

    /**
     * 跳转到查看群成员资料
     */
    private fun startFriendInfoActivity(it: GroupMemberBean) {
        var friend = FriendListBean(dbId = it.id.toLong())
        friend.friendMemberId = it.id
        friend.name = it.name ?: ""
        friend.username = it.username ?: ""
        friend.remark = it.nickRemark ?: ""
        friend.headUrl = it.headUrl ?: ""
        friend.headText = it.headText ?: ""
        friend.displayHead = it.displayHead ?: ""
        friend.levelHeadUrl = it.levelHeadUrl ?: ""
        friend.allowSpeak = it.allowSpeak ?: ""
        friend.groupId = it.groupId ?: ""
        friend.memberId = it.memberId ?: ""
        friend.role = it.role ?: ""
        friend.isShowCheck = false //不显示右边的选择check按钮
        startActivity(
            Intent(
                this,
                FriendInfoActivity::class.java
            ).putExtra(ChatActivity.CHAT_INFO, friend as Serializable)
                .putExtra(ContactActivity.IN_TYPE, 1)
        )
    }

    /**
     * 跳转到添加好友
     */
    private fun startContactActivity() {
        groupInfo?.id?.let { it1 ->
            ContactActivity.start(
                this,
                2,
                groupId = it1,
                memberType = memberType
            )
        }
    }


    var setAndDelAdminId = ""//设置取消管理员id
    var isDelAdmin: Boolean = false //是否时候取消管理员
    var groupMemberBean: GroupMemberBean? = null//记录修改禁言

    /**
     * @it 好友成员数据
     * @isDelAdmin  是否取消管理员
     */
    private fun setAdminDialog(
        it: GroupMemberBean,
        isMute: Boolean = true,
        isDelAdmin: Boolean = false,
        position: Int = 0,
        isShowFirstBtn: Boolean = true
    ) {
        HintThreeDialog(
            btnTitle = if (isMute) getString(R.string.取消禁言) else getString(R.string.jinyan),
            btnOneTitle = if (isDelAdmin) getString(R.string.取消管理员) else "",
            isShowFirstBtn = isShowFirstBtn,
            onClickListenerBtn = {
                //设置禁言/取消禁言
                var muteType = if (isMute) "Y" else "N"
                it.allowSpeak = muteType
                groupMemberBean = it
                mViewModel.putGroupMemberMute(it.groupId, it.memberId, muteType)
            },
            onClickListenerBtn1 = {
                groupInfo?.id?.let { it2 ->
                    mViewModel.setMemberRole(
                        it2,
                        it.memberId,
                        if (isDelAdmin) GroupMemberType.normal else GroupMemberType.admin
                    )
                    setAndDelAdminId = it2
                    this.isDelAdmin = isDelAdmin
                }
            },
            onClickListenerBtn2 = {
                HintDialog(
                    getString(R.string.zhuyi),
                    String.format(getString(R.string.移出群组提示),it.nickname),
                    object : ConfirmDialogCallback {
                        override fun onItemClick() {
                            groupInfo?.id?.let { it1 ->
                                removeIndex = position
                                mViewModel.deleteGroupMember(it1, it.memberId)
                            }
                        }
                    },
                    iconId = R.drawable.ic_mine_header,
                    headUrl = it?.headUrl,
                    isShowHeader = true
                ).show(supportFragmentManager, "HintDialog")
            }
        ).show(supportFragmentManager, "HintThreeDialog")
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="数据请求回调处理">
    override fun observeCallBack() {
        //获取群成员列表
        mViewModel.getGroupMemberLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
//                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    showView(true, result?.data?.data)
                    setMemberList(result?.data?.data)
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
        mViewModel.deleteRemoteGroupMessage.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.群消息已全部销毁).toast()
                    //清空本地聊天数据操作
                    groupInfo?.id?.let {
                        clearGroupMsg(it)
                        LiveEventBus.get(EventKeys.CLEAR_GROUP_MSG, Boolean::class.java)
                            .post(true)
                    }
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
//                    if (!TextUtils.isEmpty(result.exc?.message)) {
//                        result.exc?.message.toast()
//                    }
                }
            }
        }

        //设置取消管理员
        mViewModel.setMemberRoleLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //刷新数据操作
                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
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
        mViewModel.deleteGroupMemberLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (members.size > removeIndex) {
                        members.removeAt(removeIndex)
                        mAdapter.setList(members)
                    }
                    //刷新数据操作
                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //群成员退出群
        mViewModel.leaveGroupLiveData.observe(this) { result ->
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
        mViewModel.deleteGroupLiveData.observe(this) { result ->
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

        //群设置
        mViewModel.putGroupLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }
                is BaseViewModel.LoadState.Success -> {
                    //"设置成功".toast()

                    when (changedType) {
                        0 -> {
                            groupInfo?.headUrl = headUrl
                            ChatDao.getGroupDb().updateIconById(groupInfo?.id ?: "", headUrl)
                            "headUrl==${headUrl}".logD()
                        }
                        1 -> {
                            groupInfo?.allowSpeak = allowSpeak
                            ChatDao.getGroupDb().updateGroupMute(groupInfo?.id ?: "", allowSpeak)
                            "allowSpeak==${allowSpeak}".logD()
                        }
                        2 -> {
                            members.forEach { f ->
                                if (groupInfo?.id == f.id) {
                                    f.messageNotice = messageNotice
                                }
                            }
                        }
                    }
                }
                is BaseViewModel.LoadState.Fail -> {
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //群设置 群成员消息免打扰
        mViewModel.putGroupMemberNoticeLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }
                is BaseViewModel.LoadState.Success -> {
                    members.forEach { f ->
                        if (groupInfo?.id == f.id) {
                            f.messageNotice = messageNotice
                        }
                    }
                }
                is BaseViewModel.LoadState.Fail -> {
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
//                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
                    mAdapter.data?.forEach {
                        if (groupMemberBean != null)
                            if (it is FriendListBean) {
                                if (it.memberId == groupMemberBean?.id) {
                                    it.allowSpeak = groupMemberBean?.allowSpeak ?: ""
                                    groupMemberBean = null
                                    return@forEach
                                }
                            }
                    }
                    LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP_2, Boolean::class.java)
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

        //群设置修改群名称
        mViewModel.putGroupLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.设置成功).toast()
                    groupInfo?.name = groupName
                    bindView.tvGroupName.text = groupName
                    ChatDao.getGroupDb().updateNameById(groupInfo?.id ?: "", groupName)
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    groupName = groupInfo?.name ?: ""
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }

        //群主转让
        LiveEventBus.get(EventKeys.TRANSFER_GROUP, String::class.java).observe(this) { groupId ->
            if (groupId == groupInfo?.id) {
                //刷新数据操作
                groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
            }
        }

        /*ws后台群解散 广播*/
        LiveEventBus.get(EventKeys.SYSTEM_DEL_GROUP, String::class.java).observe(this) {
            //刷新数据操作
            if (it == groupInfo?.id) {
                showHintDialog("")
                LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java)
                    .post(true)
            }
        }

        /*管理员踢人 广播*/
        LiveEventBus.get(EventKeys.ADMIN_DEL_MEMBER_GROUP, Boolean::class.java).observe(this) {
            //刷新数据操作
            groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
        }

        /*管理员踢人 广播*/
        LiveEventBus.get(EventKeys.DEL_MEMBER_GROUP, String::class.java).observe(this) {
            //刷新数据操作
            groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
        }

        /*添加群成员 广播*/
        LiveEventBus.get(EventKeys.GROUP_ADD_MEMBER, String::class.java).observe(this) {
            //刷新数据操作
            groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
        }

        /*管理员设置群成员禁言 广播*/
        LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP_1, FriendListBean::class.java)
            .observe(this) { f ->
                //刷新数据操作
                mAdapter.data?.forEach {
                    if (it is GroupMemberBean) {
                        if (it.memberId == f.id) {
                            it.allowSpeak = f.allowSpeak
                            return@forEach
                        }
                    }
                }
            }

        /*管理员设置群成员禁言 广播*/
        LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP, FriendListBean::class.java)
            .observe(this) { f ->
                //刷新数据操作
                mAdapter.data?.forEach {
                    if (it is GroupMemberBean) {
                        if (it.memberId == f.id) {
                            it.allowSpeak = f.allowSpeak
                            return@forEach
                        }
                    }
                }
            }

        /*接收修改公告 和 群名 广播*/
        LiveEventBus.get(EventKeys.EDIT_GROUP_INFO, GroupInfoBean::class.java).observe(this) {
            groupInfo = it
            bindView.tvGroupName.text = it.name
            bindView.tvNotice.text = it.notice
        }


        /*接收添加群成员 设置取消管理员 广播*/
        LiveEventBus.get(EventKeys.ADD_GROUP_MEMBER, Boolean::class.java).observe(this) {
            //刷新数据操作
            groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
        }

        /*ws被别人踢出群 广播*/
        LiveEventBus.get(EventKeys.DEL_MEMBER_GROUP, String::class.java).observe(this) {
            //刷新数据操作
            if (!TextUtils.isEmpty(it)) {
                val jsonObject = JSONObject(it)
                val data = jsonObject.optJSONObject("data")
                val groupId = data.optString("groupId")
                val name = data.optString("name")//被移除人名字
                val memberId = data.optString("memberId")//被移除人id
                if (groupId == groupInfo?.id && memberId == MMKVUtils.getUser()?.id) {
//                    该事件，公共部分已发
                    LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java)
                        .post(true)
                    showHintDialog(name)
                }
            }
        }

        /*群主设置信息 其他成员收到广播*/
        LiveEventBus.get(EventKeys.GROUP_ACTION, GroupActionBean::class.java)
            .observe(this) { groupAction ->
                processGroupAction(groupAction)
            }

        LiveEventBus.get(EventKeys.EDIT_FRIEND_REMARK_NOTICE, FriendListBean::class.java)
            .observe(this) {
                //刷新群列表数据
                mAdapter.notifyDataSetChanged()
            }
    }

    /**
     * 组装数据 群组跟群管理按顺序显示
     */
    private fun setMemberList(memberList: MutableList<GroupMemberBean>?) {
        members.clear()
        memberAll.clear()

        //处理显示好友列表
        var size = memberList?.size ?: 0
        bindView.tvMemberNub.text = "${getString(R.string.全部群成员)} ($size/2000)"

        var memberAdd = GroupMemberBean(
            headUrl = R.drawable.ic_group_add_member.toString(),
            memberId = "10086",
            name = getString(R.string.添加成员)
        )
        //首先添加群主
        memberList?.forEachIndexed { index, g ->
            if (g.role.lowercase(Locale.getDefault()) == "owner".lowercase(Locale.getDefault())) {
                members.add(g)
            }
        }
        //再添加管理
        memberList?.forEachIndexed { index, g ->
            if (g.role.lowercase(Locale.getDefault()) == "admin".lowercase(Locale.getDefault())) {
                if (members.size <= 3) members.add(g)
            }
        }
        //如果还不够三个成员，再添加成员
        if (members.size <= 3)
            memberList?.forEachIndexed { index, g ->
                if (g.role.lowercase(Locale.getDefault()) == "normal".lowercase(
                        Locale.getDefault()
                    )
                ) {
                    if (members.size <= 3) members.add(g)
                }
            }

        memberList?.let { memberAll.addAll(it) }
        if (memberType != 2) {
            if (members.size >= 4) {
                members.removeAt(members.size - 1)
            }
            members.add(memberAdd)
        }
        mAdapter.setList(members)
    }
    //</editor-fold>

    /**
     * 清空这个群
     * 本地缓存聊天消息
     */
    private fun clearGroupMsg(groupId: String) {
        ChatDao.getChatMsgDb().delMsgListByGroupId(groupId)
    }

    //<editor-fold defaultstate="collapsed" desc="根据数据类型 显示view">
    /**
     * 界面根据权限显示
     * @memberType: 0群主 1管理员  2 成员
     */
    private fun showView(isShowSwitch: Boolean, memberList: MutableList<GroupMemberBean>?) {
        var memberBean: GroupMemberBean? = null
        var name = ""
        memberList?.forEachIndexed { index, g ->
            var id = MMKVUtils.getUser()?.id ?: ""
            if (id == g.memberId) {
                memberBean = g
                //  普通成员 normal,管理员 admin,群主 owner
                when (g.role.lowercase(Locale.getDefault())) {
                    "owner".lowercase(Locale.getDefault()) -> memberType = 0
                    "admin".lowercase(Locale.getDefault()) -> memberType = 1
                    "normal".lowercase(Locale.getDefault()) -> memberType = 2
                }
            }
            if (g.role.lowercase(Locale.getDefault()) == "owner".lowercase(Locale.getDefault())) {
                name = g.nickname
            }
        }

        "memberType== $memberType".logD()
        if (memberType == 2) {
            bindView.llGroupName.isEnabled = false
            bindView.ivGroupHead.isEnabled = false
            bindView.tvGroupName.isEnabled = false
            bindView.llMembers.visible()
            bindView.rvMember.visible()
            bindView.btnLogout.visible()
            bindView.ivGroupNameKey.visible()
            bindView.llMute.gone()
            bindView.vMute.gone()
            bindView.btnDestroy.gone()
            bindView.vSendMsgLimit.gone()
        } else {
            if (isShowSwitch) {
                allowSpeak = groupInfo?.allowSpeak ?: "Y"
                //默认不禁言关闭状态
                if (allowSpeak == "N") {
                    isSwitchMute = false
                }
                bindView.switchMute.visible()
                bindView.switchMute.isChecked = (allowSpeak == "N")//是否禁言
            } else {
                bindView.switchMute.gone()
            }
            bindView.llGroupName.isEnabled = true
            bindView.ivGroupHead.isEnabled = true
            bindView.tvGroupName.isEnabled = true
            bindView.btnLogout.visible()
            bindView.ivGroupNameKey.visible()
            bindView.llMembers.visible()
            bindView.rvMember.visible()
            bindView.llMute.visible()
            bindView.vMute.visible()
            bindView.btnDestroy.visible()
            bindView.vSendMsgLimit.visible()
            if (memberType == 0) {
                //我是群主
                bindView.btnLogout.text = getString(R.string.解散群组)
            }
        }
        if (memberType == 0) name = getString(R.string.wo)
        bindView.tvGroupNickname.text = name

        if (isShowSwitch) {
            messageNotice = memberBean?.messageNotice ?: "Y"
            "messageNotice== $messageNotice".logD()
            //默认消息不免打扰 关闭状态
            if (messageNotice == "N") {
                isSwitchMessage = false
            }
            bindView.switchMessage.visible()
            bindView.switchMessage.isChecked = (messageNotice == "N")//是否消息免打扰
        } else {
            bindView.switchMessage.gone()
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="处理群事件">
    /**
     * 处理群事件
     */
    private fun processGroupAction(groupAction: GroupActionBean) {
        if (groupAction.groupId == groupInfo?.id) {
            //只处理当前打开群组的事件
            when (groupAction.messageType) {
                MsgType.ALLOWSPEAK -> {
                    //禁言
                    groupInfo?.allowSpeak = groupAction.setValue
                    allowSpeak = groupInfo?.allowSpeak.toString()
                    var isChecked = bindView.switchMute.isChecked//获取当前按钮的状态
                    if (isChecked) {
                        if (groupAction.setValue == "Y") {
                            isSwitchMute = false
                            bindView.switchMute.isChecked = (groupAction.setValue == "N")
                        }
                    } else {
                        if (groupAction.setValue == "N") {
                            isSwitchMute = false
                            bindView.switchMute.isChecked = (groupAction.setValue == "N")
                        }
                    }
                }
                MsgType.SetMemberRole -> {
                    //设置群管理员 刷新群成员数据
                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
                }
                MsgType.CancelMemberRole -> {
                    //取消群管理员 刷新群成员数据
                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
                }
                MsgType.GroupModifyName -> {
                    //修改群名称
                    bindView.tvGroupName.text = groupAction.setValue
                    groupInfo?.name = groupAction.setValue
                }
                MsgType.NOTICE -> {
                    //修改群公告
                    groupInfo?.notice = groupAction.setValue
                    bindView.tvNotice.text = groupAction.setValue
                }
                MsgType.HEADERURL -> {
                    //修改群头像
                    groupInfo?.headUrl = groupAction.setValue
                    bindView.ivGroupHead.roundLoad(
                        groupInfo?.headUrl,
                        R.drawable.ic_mine_header_group
                    )
                }
                MsgType.MemberAllowSpeak -> {
                    //设置了单个用户禁言 刷新数据
                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
                }
                ChatType.DeleteGroupMember -> {
                    //删除了一个群成员
                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
                }
                else ->{
                    groupInfo?.id?.let { mViewModel.getGroupMemberList(it) }
                }
            }
        }
    }
    //</editor-fold>
}