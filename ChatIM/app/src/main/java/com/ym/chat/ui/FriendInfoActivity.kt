package com.ym.chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.view.MotionEvent
import android.widget.Switch
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.copyToClipboard
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.bean.FriendListBean
import com.ym.chat.databinding.ActivityFriendInfoBinding
import com.ym.chat.db.ChatDao
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.chat.R
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.ext.loadImg
import com.ym.chat.utils.ToastUtils
import com.ym.chat.utils.Utils
import com.ym.chat.viewmodel.FriendViewModel
import com.ym.chat.viewmodel.SetViewModel

/**
 * 好友资料信息
 */
class FriendInfoActivity : LoadingActivity() {
    private val bindView: ActivityFriendInfoBinding by binding()
    private val mViewModel = FriendViewModel()
    private val mSetViewModel = SetViewModel()
    private var isFriend = false//是否是好友
    private var friendId = ""
    private var inType = 0 //0 默认好友设置跳转过来  1 群成员界面 3 系统通知进入
    private var chatInfo: FriendListBean? = null
    private var isSwitchBlack = true//是否设置黑名单
    private var switchBlack = ""//黑名单参数
    private var isMakeDate = false//是否来源组装的数据

    companion object {
        val REMARK = "remark"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView() {
        bindView.ivBack.click {
            finish()
        }

        bindView.tvBtnChat.click {
            if (inType == 0) {
                this.finish()
            } else if (inType == 3) {
                if (isMakeDate) {
                    //申请添加好友
                    mViewModel.applyAddFriend(MMKVUtils.getUser()?.id ?: "", friendId)
                } else {
                    ChatActivity.start(this, friendId, 0)
                }
            } else {
                if (isFriend) {
                    ChatActivity.start(this, friendId, 0)
                } else {
                    //申请添加好友
                    mViewModel.applyAddFriend(MMKVUtils.getUser()?.id ?: "", friendId)
                }
            }
        }

        bindView.layoutRemark.click {
            //编辑用户信息
            startActivity(
                Intent(this@FriendInfoActivity, FriendRemakActivity::class.java).putExtra(
                    REMARK, chatInfo
                )
            )
        }

        bindView.tvBtnMute.click {
            //设置禁言/取消禁言
            var title = ""
            var content = ""
            if (chatInfo?.allowSpeak == "N") {
                title = getString(R.string.解除禁言)
                content = String.format(getString(R.string.解除禁言提示), chatInfo?.nickname)
            } else {
                title = getString(R.string.设置禁言)
                content = String.format(getString(R.string.设置禁言提示), chatInfo?.nickname)
            }
            HintDialog(
                title,
                content,
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        chatInfo?.let {
                            var muteType = if (it.allowSpeak == "N") "Y" else "N"
                            mViewModel.putGroupMemberMute(it.groupId, it.memberId, muteType)
                        }
                    }
                },
                iconId = R.drawable.ic_mine_header,
                headUrl = chatInfo?.headUrl,
                isShowHeader = true, isTitleTxt = true
            ).show(supportFragmentManager, "HintDialog")

        }

        bindView.tvBtnDel.pressEffectAlpha().click {
            HintDialog(
                getString(R.string.shanchuhaoyou),
                getString(R.string.您确定要删除此联系人吗),
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        //删除好友
                        chatInfo?.let {
                            mViewModel.deleteFriend(it.friendMemberId)
                        }
                    }
                },
                iconId = R.drawable.ic_mine_header,
                headUrl = chatInfo?.headUrl,
                isShowHeader = false, isTitleTxt = true
            ).show(supportFragmentManager, "HintDialog")
        }

        //设置好友 是否加入黑名单
        bindView.switchBlack.setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                if (v is Switch) {
                    var title = if (v.isChecked) getString(R.string.解除拉黑) else getString(R.string.laheihaoyou)
                    var content =
                        if (v.isChecked) String.format(
                            getString(R.string.解除拉黑提示),
                            chatInfo?.nickname
                        ) else  String.format(
                            getString(R.string.拉黑提示),
                            chatInfo?.nickname
                        )
                    HintDialog(
                        title,
                        content,
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                chatInfo?.id?.let {
                                    switchBlack = if (!v.isChecked) "Y" else "N"
                                    mViewModel.modifyFriendStatus(it, black = switchBlack)
                                    bindView.switchBlack.isChecked = !v.isChecked
                                }
                            }
                        },
                        iconId = R.drawable.ic_mine_header,
                        headUrl = chatInfo?.headUrl,
                        isShowHeader = false, isTitleTxt = true
                    ).show(supportFragmentManager, "HintDialog")
                }
            }
            true
        }
//        //设置好友 是否加入黑名单
//        bindView.switchBlack.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isSwitchBlack) {//第一次敷值不处理
//                chatInfo?.id?.let {
//                    switchBlack = if (isChecked) "Y" else "N"
//                    mViewModel.modifyFriendStatus(it, black = switchBlack)
//                }
//            } else {
//                isSwitchBlack = true
//            }
//        }

        bindView.tvUsername.setOnLongClickListener {
            if (inType != 0) {
                bindView.tvUsername.text.toString().trim().copyToClipboard()
                ToastUtils.showToastWithImg(
                    this@FriendInfoActivity,
                    "已复制",
                    R.drawable.ic_dialog_success
                )
            }
            true
        }
        bindView.tvNickName.setOnLongClickListener {
            bindView.tvNickName.text.toString().trim().copyToClipboard()
            ToastUtils.showToastWithImg(
                this@FriendInfoActivity,
                getString(R.string.已复制),
                R.drawable.ic_dialog_success
            )
            true
        }
    }

    override fun requestData() {
        intent?.let { intent ->
            inType = intent.getIntExtra(ContactActivity.IN_TYPE, 0) //0 默认好友设置跳转过来  1 其他界面

            chatInfo = intent.getSerializableExtra(ChatActivity.CHAT_INFO) as FriendListBean
            isMakeDate = intent.getBooleanExtra("isMakeDate", false)

            bindView.tvNickName.text = chatInfo?.nickname
            bindView.layoutHeader.ivHeader.loadImg(chatInfo)
            bindView.tvUsername.text = chatInfo?.username
            bindView.tvPhone.text = chatInfo?.mobile
            bindView.tvGender.text = when (chatInfo?.gender?.lowercase()) {
                "male".lowercase() -> {
                    getString(R.string.nan)
                }

                "female".lowercase() -> {
                    getString(R.string.nv)
                }

                else -> {
                    getString(R.string.buxiangtoulu)
                }
            }
            friendId = chatInfo?.id.toString()
            mSetViewModel.memberIdGetUserInfo(friendId)
            Utils.showDaShenImageView(
                bindView.layoutHeader.ivHeaderMark,
                chatInfo?.displayHead == "Y",
                chatInfo?.levelHeadUrl
            )

            var friend = chatInfo?.id?.let { ChatDao.getFriendDb().getFriendById(it) }
            switchBlack = friend?.black ?: "N"
            //默认不加入黑名单关闭状态
            if (switchBlack == "Y") {
                isSwitchBlack = false
                bindView.switchBlack.isChecked = true
            }

            when (inType) {
                0, 3 -> {
                    bindView.tvTitle.text = getString(R.string.好友资料)
                    bindView.layoutRemark.visible()
                    //备注
                    if (chatInfo?.remark.isNullOrBlank()) {
                        bindView.tvRemark.text = getString(R.string.qingshurubeizhu)
                    } else {
                        bindView.tvRemark.text = chatInfo?.remark
                    }

                    if (MMKVUtils.isAdmin()) {
                        bindView.llPhone.visible()
                        bindView.vPhone.visible()

                        bindView.tvBtnDel.visible()

                        bindView.vBlack.visible()
                        bindView.llBlack.visible()
                        bindView.switchBlack.visible()
                    }

                    if (isMakeDate) {
                        //组装的数据，不是自己的好友
                        bindView.tvBtnDel.gone()
                        bindView.tvBtnChat.text = getString(R.string.加好友)
                        bindView.tvUsername.text = "****"
                    }
                }

                1 -> {
                    if (friendId == MMKVUtils.getUser()?.id) {
                        //如果查看的是自己的资料
                        bindView.tvTitle.text = getString(R.string.我的资料)
                        bindView.llGoChat.gone()
                        bindView.layoutRemark.gone()
                        bindView.switchBlack.gone()
                        bindView.vBlack.gone()
                        bindView.llBlack.gone()
                        bindView.tvPhone.text = MMKVUtils.getUser()?.mobile
                    } else {
                        //如果查看是好友的资料
                        showFriendView()
                        /**获取自己 在群的成员信息*/
                        var groupMemberUser =
                            MMKVUtils.getUser()?.id?.let { it1 ->
                                chatInfo?.groupId?.let {
                                    ChatDao.getGroupDb().getMemberInGroup(
                                        it1, it
                                    )
                                }
                            }
                        groupMemberUser?.let {
                            when (it.role.lowercase()) {
                                "Owner".lowercase() -> {
                                    //我是群主 除了自己其他人都禁言
                                    bindView.tvBtnMute.visible()
                                }

                                "admin".lowercase() -> {
                                    //我是管理员
                                    if (chatInfo?.role?.lowercase() == "Normal".lowercase())
                                        bindView.tvBtnMute.visible()
                                }
                            }
                        }

                        if (chatInfo?.allowSpeak == "N") {
                            bindView.tvBtnMute.text = getString(R.string.取消禁言)
                        }

                        bindView.tvUsername.text = "****"
                    }
                }

            }
        }
    }

    private fun showFriendView() {
        bindView.tvTitle.text = getString(R.string.成员资料)
        //需判断此成员是否是自己的好友
        var friends = ChatDao.getFriendDb().getFriendList()
        friends.forEach { f ->
            if (f.id == friendId) {
                isFriend = true
            }
        }
        if (!isFriend) {
            bindView.tvBtnChat.text = getString(R.string.加好友)
            bindView.layoutRemark.gone()
        } else {
            bindView.layoutRemark.visible()
            //备注
            if (chatInfo?.remark.isNullOrBlank()) {
                bindView.tvRemark.text = getString(R.string.qingshurubeizhu)
            } else {
                bindView.tvRemark.text = chatInfo?.remark
            }
        }
    }

    /**
     * 获取所有好友数据 保存到本地
     */
    private fun getFriendListSaveAddFriend() {
        mViewModel.getFriendList()
    }

    override fun observeCallBack() {
        //删除好友
        mViewModel.deleteFriend.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }

                is BaseViewModel.LoadState.Success -> {
                    chatInfo?.id?.let {
                        //清除成员列表 成员
                        ChatDao.getFriendDb().delFriendById(it)
                        //清除会话历史记录
                        ChatDao.getChatMsgDb().delMsgListByFriendId(it)
                        //清除主页新消息窗口
                        ChatDao.getConversationDb().delConverByTargtId(it)
                        //发广播通知 退出聊天界面
                        LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java)
                            .post(true)
                        //直接退出界面
                        this.finish()
                    }
                }

                is BaseViewModel.LoadState.Fail -> {
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.设置失败).toast()
                    }
                }
            }
        }

        //设置用户黑名单
        mViewModel.modifyFriendStatus.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }

                is BaseViewModel.LoadState.Success -> {
                }

                is BaseViewModel.LoadState.Fail -> {
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.设置失败).toast()
                    }
                }
            }
        }

        mSetViewModel.memberIdGetUserInfo.observe(this) { result ->
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
                    var chatInfo = result.data?.data
                    bindView.tvNickName.text = chatInfo?.showNickName()
                    bindView.layoutHeader.ivHeader.loadImg(chatInfo)
                    bindView.tvUsername.text = chatInfo?.username
                    bindView.tvPhone.text = chatInfo?.mobile
//                    if (switchBlack != chatInfo?.black) {
//                        isSwitchBlack = false
//                        bindView.switchBlack.isChecked = chatInfo?.black == "Y"
//                        switchBlack = chatInfo?.black ?:"N"
//                    }
                    bindView.tvGender.text = when (chatInfo?.gender?.lowercase()) {
                        "male".lowercase() -> {
                            getString(R.string.nan)
                        }

                        "female".lowercase() -> {
                            getString(R.string.nv)
                        }

                        else -> {
                            getString(R.string.baomi)
                        }
                    }
                    Utils.showDaShenImageView(
                        bindView.layoutHeader.ivHeaderMark,
                        chatInfo?.displayHead == "Y",
                        chatInfo?.levelHeadUrl
                    )
                }
            }
        }

        mViewModel.applyAddFriendResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    result.exc?.message.toast()
                }

                is BaseViewModel.LoadState.Success -> {
                    //这里要把好友信息保存到本地
                    getFriendListSaveAddFriend()
//                    "添加好友成功".toast()
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
                    if (chatInfo?.allowSpeak == "N") {
                        chatInfo?.allowSpeak = "Y"
                        bindView.tvBtnMute.text = getString(R.string.jinyan)
                    } else {
                        chatInfo?.allowSpeak = "N"
                        bindView.tvBtnMute.text = getString(R.string.取消禁言)
                    }

                    LiveEventBus.get(EventKeys.ADMIN_EDIT_MEMBER_GROUP, FriendListBean::class.java)
                        .post(chatInfo)
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }


        //获取所有好友保存到本地
        mViewModel.friendListResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //单聊
                    ChatActivity.start(this, friendId, 0)
                    finish()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    result.exc?.message.toast()
                }
            }
        }

        /**接收ws多端同步 修改好友信息 广播*/
        LiveEventBus.get(EventKeys.EDIT_FRIEND_NOTICE, String::class.java).observe(this) {
            var friend = ChatDao.getFriendDb().getFriendById(it)
            if (switchBlack != friend?.black) {
                isSwitchBlack = false
                bindView.switchBlack.isChecked = friend?.black == "Y"
                if (friend?.black != null)
                    switchBlack = friend?.black ?: ""
            }
        }

        /*接收修改名字 广播*/
        LiveEventBus.get(EventKeys.EDIT_USER_NAME, String::class.java).observe(this) {
            bindView.tvNickName.text = it
        }

        LiveEventBus.get(EventKeys.EDIT_FRIEND_REMARK_NOTICE, FriendListBean::class.java)
            .observe(this) {
                chatInfo = it
                bindView.tvNickName.text = chatInfo?.remark
                bindView.tvRemark.text = chatInfo?.remark
            }
    }
}