package com.ym.chat.ui

import android.content.Intent
import android.text.TextUtils
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.databinding.*
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.ext.loadImg
import com.ym.chat.viewmodel.FriendViewModel
import java.io.Serializable

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 单聊设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class UserChatSetActivity : LoadingActivity() {
    private val bindView: ActivityUserChatBinding by binding()
    private var mViewModel = FriendViewModel()
    private var chatInfo: FriendListBean? = null
    private var messageNotice: String? = "Y"
    private var isSwitchMessage = true//设置是否通知后是否调用接口
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.liaotianshezhi)
        }
        bindView.linFriend.click {
            startActivity(
                Intent(
                    this,
                    FriendInfoActivity::class.java
                ).putExtra(ChatActivity.CHAT_INFO, chatInfo as Serializable)
            )
            this.finish()
        }
        //设置好友消息免打扰
        bindView.switchMessage.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isSwitchMessage) {//第一次敷值不处理
                chatInfo?.id?.let {
                    messageNotice = if (isChecked) "N" else "Y"
                    mViewModel.modifyFriendStatus(it, messageNotice = messageNotice)
                }
            } else {
                isSwitchMessage = true
            }
        }
        val isManager = MMKVUtils.isAdmin()
        if (isManager) {
            bindView.btnDelMsg.visible()
            //远程销毁好友消息
            bindView.btnDelMsg.click {
                chatInfo?.id?.let { it1 ->
                    HintDialog(
                        getString(R.string.远程销毁),
                        String.format(getString(R.string.远程销毁提示),chatInfo?.nickname),
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                mViewModel.deleteRemoteFriendMessage(
                                    it1
                                )
                            }
                        },
                        R.drawable.ic_mine_header, headUrl = chatInfo?.headUrl, isShowHeader = true, isTitleTxt = true
                    ).show(supportFragmentManager, "HintDialog")
                }
            }

//            /**这一版不需要删除功能*/
//            bindView.btnDelFriend.visible()
//            //删除好友
//            bindView.btnDelFriend.click {
//                chatInfo?.id?.let { it1 -> mViewModel.deleteFriend(it1) }
//            }
        }


        //跳转到搜索界面
        bindView.tvBtnSearch.click {
            startActivity(
                Intent(
                    this,
                    SearchFriendActivity::class.java
                ).putExtra(SearchFriendActivity.SEARCHTYPE, 2)
                    .putExtra(SearchFriendActivity.SEARCHID, chatInfo?.id ?: "")
                    .putExtra(SearchFriendActivity.HEADURL, chatInfo?.headUrl ?: "")
                    .putExtra(SearchFriendActivity.NAME, chatInfo?.nickname ?: "")
            )
        }
    }

    override fun requestData() {
        intent?.let { intent ->
            chatInfo = intent.getSerializableExtra(ChatActivity.CHAT_INFO) as FriendListBean
            bindView.tvName.text = chatInfo?.nickname
            bindView.layoutHeader.ivHeader.loadImg(chatInfo)
            messageNotice = chatInfo?.messageNotice ?: "Y"
            //默认消息不免打扰 关闭状态
            if (messageNotice == "N") {
                isSwitchMessage = false
            }
            bindView.switchMessage.isChecked = (messageNotice == "N")
        }
    }

    override fun observeCallBack() {
        //设置用户免打扰
        mViewModel.modifyFriendStatus.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                }
                is BaseViewModel.LoadState.Success -> {
                    //发通知更新好友设置
                    LiveEventBus.get(EventKeys.EDIT_USER_MESSAGE_NOTICE, String::class.java)
                        .post(messageNotice)
                }
                is BaseViewModel.LoadState.Fail -> {
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.设置失败).toast() // "设置失败"
                    }
                }
            }
        }


        //远程销毁好友消息
        mViewModel.deleteRemoteFriendMessage.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    "好友消息已全部销毁".toast()
                    chatInfo?.id?.let {
                        ChatDao.getChatMsgDb().delMsgListByFriendId(it)
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

        //删除好友
        mViewModel.deleteFriend.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    "好友已删除".toast()
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
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }


        /**ws 收到已被删除好友*/
        LiveEventBus.get(EventKeys.DEL_FRIEND_ACTION, String::class.java)
            .observe(this) {
                var friend = ChatDao.getFriendDb().getFriendById(it)
                var content = ""
                if (chatInfo?.id == it) {
                    content = "你已被${friend?.nickname}删除好友关系"
                } else {
                    //操作人是自己
                    content = "你已删除${friend?.nickname}的好友关系"
                }
                //被删除好友
                HintDialog(
                    "注意",
                    content,
                    object : ConfirmDialogCallback {
                        override fun onItemClick() {
                            finish()
                            LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java)
                                .post(true)
                        }
                    },
                    isShowBtnCancel = false,
                    isCanTouchOutsideSet = false, iconId = R.drawable.ic_mine_header, headUrl = chatInfo?.headUrl, isShowHeader = true
                ).show(supportFragmentManager, "HintDialog")
            }

    }
}