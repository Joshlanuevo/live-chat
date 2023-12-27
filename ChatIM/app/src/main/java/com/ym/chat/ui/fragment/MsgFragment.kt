package com.ym.chat.ui.fragment

import android.content.Intent
import android.view.Gravity
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.constant.EventKeys.UPDATE_CONVER
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseFragment
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.chat.BuildConfig
import com.ym.chat.R
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.DelConBean
import com.ym.chat.databinding.FragmentMsgBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.item.HomeMsgItem
import com.ym.chat.popup.HomeAddPopupWindow
import com.ym.chat.service.WebsocketWork
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.CollectActivity
import com.ym.chat.ui.NotifyActivity
import com.ym.chat.ui.SearchFriendActivity
import com.ym.chat.utils.ChatType
import com.ym.chat.viewmodel.ChatGroupViewModel
import com.ym.chat.viewmodel.FriendViewModel
import com.ym.chat.viewmodel.MsgViewModel

/**
 * 消息Fragment
 */
class MsgFragment : BaseFragment(R.layout.fragment_msg) {
    private val bindView: FragmentMsgBinding by binding()
    private val mAdapter = BaseBinderAdapter()
    private val mViewModel = MsgViewModel()
    private var mFriendViewModel = FriendViewModel()
    private var mChatGroupViewModel = ChatGroupViewModel()
    private lateinit var msgItem: HomeMsgItem
    override fun initView() {

        initItem()

        bindView.tvAppName.text = getString(R.string.app_name)
        bindView.listChat.adapter = mAdapter
        bindView.refreshHome.setEnableLoadMore(false)
        bindView.refreshHome.setOnRefreshListener {
            //获取会话列表
            mViewModel.getConverList()
        }

        bindView.tvSearch.click {
            startActivity(
                Intent(requireContext(), SearchFriendActivity::class.java)
                    .putExtra(SearchFriendActivity.SEARCHTYPE, 4)
            )
        }
        bindView.ivAdd.click {
            val popupWindow = HomeAddPopupWindow(requireContext())
            popupWindow.showPopup(bindView.ivAdd, 200, 4, Gravity.BOTTOM)
        }

    }

    private fun initItem() {
        msgItem = HomeMsgItem(
            onItemClickListener = {
                when (it.type) {
                    0 -> {
                        //单聊
                        ChatActivity.start(requireContext(), it.chatId, 0)
//                            "单聊会话数据".toast()
                    }
                    1 -> {
                        //群聊
                        ChatActivity.start(requireContext(), it.chatId, 1)
                    }
                    2 -> {
                        //系统默认
                        when (it.sysType) {
                            //我的收藏
                            1 -> startActivity(Intent(activity, CollectActivity::class.java))
                            2 ->{
                                //系统通知
                                startActivity(Intent(activity, NotifyActivity::class.java).apply {
                                    if(it.lastMsg==getString(R.string.haoyoutongzhi)){
                                        putExtra("index",1)
                                    }
                                })
                            }
                        }
                    }
                }
            },
            onClickDeleteConver = { bean, position ->
                showHintDialog(0, bean, getString(R.string.yichuduihua), String.format(getString(R.string.您确定要移除与),bean.name))
            },
            onClickOnTop = { bean, position ->
                //置顶
                if (bean.isTop) {
                    /**取消置顶*/
                    val delConBean = DelConBean().apply {
                        cmd = 49
                        memberId = MMKVUtils.getUser()?.id ?: ""
                        operationType = "DelTop"
                    }
                    if (bean.type == 0) {
                        delConBean.friendMemberId = bean.chatId
                        delConBean.type = ChatType.CHAT_TYPE_FRIEND
                    } else if (bean.type == 1) {
                        delConBean.groupId = bean.chatId
                        delConBean.type = ChatType.CHAT_TYPE_GROUP
                    }
                    ChatDao.getConversationDb().cancelConversationMsgByTargetId(bean.chatId)
                    WebsocketWork.WS.updateConver(delConBean)
                } else {
                    /**添加置顶*/
                    val size = ChatDao.getConversationDb().getConverTopMsgCount()
                    if (size < 10) {
                        val delConBean = DelConBean().apply {
                            cmd = 49
                            memberId = MMKVUtils.getUser()?.id ?: ""
                            operationType = "AddTop"
                        }
                        if (bean.type == 0) {
                            delConBean.friendMemberId = bean.chatId
                            delConBean.type = ChatType.CHAT_TYPE_FRIEND
                        } else if (bean.type == 1) {
                            delConBean.groupId = bean.chatId
                            delConBean.type = ChatType.CHAT_TYPE_GROUP
                        }
                        ChatDao.getConversationDb().updateConversationMsgByTargetId(bean.chatId, "")
                        WebsocketWork.WS.updateConver(delConBean)
                    } else {
                        "置顶最大只能设置10条".toast()
                    }
                }
            },
            onClickDelMsg = { bean, position ->
                //清除消息
                showHintDialog(1, bean, getString(R.string.qingkongxiaoxi), String.format(getString(R.string.清空提示),getFriendOrGroupName(bean)))
            },
            onClickNotNotify = { bean, position ->
                var isMessageNotice = if (bean.type == 0) {
                    //私聊会话
                    ChatDao.getFriendDb().getFriendById(bean.chatId)?.messageNotice == "N"
                } else {
                    //群聊会话
                    ChatDao.getGroupDb().isMessageNotice(bean.chatId)
                }
                //静音
                if (isMessageNotice) {
                    showHintDialog(
                        2,
                        bean,
                        getString(R.string.kaiqitongzhi),
                        String.format(getString(R.string.开启对话提示),getFriendOrGroupName(bean)),
                        isMessageNotice
                    )
                } else {
                    showHintDialog(
                        2,
                        bean,
                        getString(R.string.jinyinxiaoxi),
                        String.format(getString(R.string.静音提示),getFriendOrGroupName(bean)),
                        isMessageNotice
                    )
                }
            },
            onClickNotRead = { bean, position ->
                if (bean.msgCount > 0) {
                    //标记已读
                    showHintDialog(
                        3,
                        bean,
                        getString(R.string.标记已读),
                        String.format(getString(R.string.标记已读提示),getFriendOrGroupName(bean)),
                        isRead = true
                    )
                } else {
                    if (bean.isRead)//标记未读
                        showHintDialog(
                            3,
                            bean,
                            getString(R.string.biaojiweidu),
                            String.format(getString(R.string.标记未读提示),getFriendOrGroupName(bean)),
                            isRead = false
                        )
                    else//标记已读
                        showHintDialog(
                            3,
                            bean,
                            getString(R.string.标记已读),
                            String.format(getString(R.string.标记已读提示),getFriendOrGroupName(bean)),
                            isRead = true
                        )
                }
            },
        )
        msgItem.isConverList = true
        mAdapter.addItemBinder(msgItem)
    }


    private fun showHintDialog(
        type: Int,
        bean: ConversationBean,
        title: String,
        content: String,
        isMessageNotice: Boolean = false, //只有设置静音才有效
        isRead: Boolean = false, //只有标记已读未读才有效
    ) {
        var headUrl = getHeadUrl(bean)
        var iconId = getHeadId(bean)
        val hintDialog = HintDialog(
            title,
            content,
            headUrl = headUrl,
            isShowBtnCancel = true,
            isCanTouchOutsideSet = false,
            isShowHeader = true,
            isTitleTxt = true,
            iconId = iconId,
            callback = object : ConfirmDialogCallback {
                override fun onItemClick() {
                    when (type) {
                        0 -> {
                            //删除
                            ChatDao.getConversationDb().delConver(bean.idInDb)
                            val delConBean = DelConBean().apply {
                                cmd = 49
                                memberId = MMKVUtils.getUser()?.id ?: ""
                                operationType = "Del"
                            }
                            if (bean.type == 0) {
                                delConBean.friendMemberId = bean.chatId
                                delConBean.type = ChatType.CHAT_TYPE_FRIEND
                            } else if (bean.type == 1) {
                                delConBean.groupId = bean.chatId
                                delConBean.type = ChatType.CHAT_TYPE_GROUP
                            }
                            WebsocketWork.WS.updateConver(delConBean)
                        }
                        1 -> {
                            //清除消息
                            if (bean.type == 0) {
                                //私聊会话
                                mFriendViewModel.deleteRemoteFriendMessage(
                                    bean.chatId,
                                    "Unilateral"
                                )
                            } else {
                                //群聊会话
                                mChatGroupViewModel.deleteRemoteGroupMessage(
                                    bean.chatId,
                                    "Unilateral"
                                )
                            }
                        }
                        2 -> {
                            //禁音
                            var messageNotice = if (isMessageNotice) "Y" else "N"
                            if (bean.type == 0) {
                                //私聊会话
                                mFriendViewModel.modifyFriendStatus(
                                    bean.chatId,
                                    messageNotice = messageNotice
                                )
                            } else {
                                //群聊会话
                                mChatGroupViewModel.putGroupMemberNotice(
                                    bean.chatId,
                                    messageNotice = messageNotice
                                )
                            }
                        }
                        3 -> {
                            //标记已读/未读
                            var signType = ""//是否已读/未读
                            //标记已读
                            if (isRead) {
                                //现在已读
                                ChatDao.getChatMsgDb()
                                    .setMsgRead(getChatType(bean.type), bean.chatId)
                                ChatDao.getConversationDb().setConversationRead(bean.chatId, true)
                                signType = "read"
                                ChatDao.getConversationDb().resetConverMsgCount(bean.chatId)
                            } else {
                                //现在未读
                                ChatDao.getConversationDb().setConversationRead(bean.chatId, false)
                                signType = "unread"
                            }

                            if (bean.type == 0) {
                                //私聊消息
                                WebsocketWork.WS.sendReadConState(
                                    bean.chatId,
                                    "",
                                    "Friend",
                                    signType
                                )
                            } else {
                                //群聊消息
                                WebsocketWork.WS.sendReadConState(
                                    "",
                                    bean.chatId,
                                    "Group",
                                    signType
                                )
                            }
                        }
                    }
                }
            }
        )
        hintDialog?.show(parentFragmentManager, "HintDialog")
    }

    /**
     * 获取本地默认头像Id
     */
    private fun getHeadId(bean: ConversationBean): Int {
        return if (bean.type == 0) {
            R.drawable.ic_mine_header
        } else {
            R.drawable.ic_mine_header_group
        }
    }

    /**
     * 获取头像Url
     */
    private fun getHeadUrl(bean: ConversationBean): String {
        return if (bean.type == 0) {
            ChatDao.getFriendDb().getFriendById(bean.chatId)?.headUrl ?: ""
        } else {
            ChatDao.getGroupDb().getGroupInfoById(bean.chatId)?.headUrl ?: ""
        }
    }

    /**
     * 获取好友或者群 的名名称
     */
    private fun getFriendOrGroupName(bean: ConversationBean): String {
        return if (bean.type == 0) {
            ChatDao.getFriendDb().getFriendById(bean.chatId)?.nickname ?: ""
        } else {
            ChatDao.getGroupDb().getGroupInfoById(bean.chatId)?.name + getString(R.string.群)
        }
    }


    override fun requestData() {
        //获取会话列表
        mViewModel.getConverList()
    }

    override fun observeCallBack() {
        mViewModel.conversationResult.observe(this) {
            bindView.refreshHome.finishRefresh()
            mAdapter.setList(it)
        }

//        /*接收到群消息置顶广播*/
//        LiveEventBus.get(EventKeys.MSG_TOP_WS, Boolean::class.java).observe(this) {
//            //获取置顶消息列表
////            mViewModel.getTopInfoList()
//        }

        //会话列表数据更新
        LiveEventBus.get(UPDATE_CONVER, ConversationBean::class.java).observe(this) { conver ->
            bindView.root.postDelayed({
                mViewModel.getConverList()
            }, 500)
        }

        //系统通知删除事件
        LiveEventBus.get(EventKeys.DELETE_NOTIFY_MSG, String::class.java).observe(this) {
            mViewModel.getConverList()
        }
    }

    /**
     * 获取聊天类型
     */
    private fun getChatType(mChatType: Int): String {
        return when (mChatType) {
            //单聊
            0 -> ChatType.CHAT_TYPE_FRIEND
            //群聊
            1 -> ChatType.CHAT_TYPE_GROUP
            //群发消息
            2 -> ChatType.CHAT_TYPE_GROUP_SEND
            else -> ""
        }
    }
}