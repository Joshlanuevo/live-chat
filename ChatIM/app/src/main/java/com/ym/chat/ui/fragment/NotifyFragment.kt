package com.ym.chat.ui.fragment

import android.os.Build
import androidx.annotation.RequiresApi
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.BatchModify
import com.ym.chat.bean.NotifyBean
import com.ym.chat.databinding.FragmentNotifyBinding
import com.ym.chat.databinding.FragmentNotifyLayoutEmptyBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.enum.AskStatus
import com.ym.chat.item.NotifyItem
import com.ym.chat.service.WebsocketWork
import com.ym.chat.ui.WebActivity
import com.ym.chat.viewmodel.ChatGroupViewModel
import com.ym.chat.viewmodel.NotifyViewModel

/**
 * 通知Fragment
 */
class NotifyFragment(private val notifyType: Int) : LoadingFragment(R.layout.fragment_notify) {
    private val bindView: FragmentNotifyBinding by binding()
    private val mAdapter = BaseBinderAdapter()
    private val mViewModel = ChatGroupViewModel()
    private val mNotifyViewModel = NotifyViewModel()
    private var page = 1//获取的页数
    private var pageNum = 10//一页获取多少条数据
    private var operateNotifyBean: NotifyBean? = null//操作同意 拒绝 的notifyBean
    private var mNotifyList = mutableListOf<NotifyBean>()//显示列表的数据

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        initAdapter()
        bindView.refreshNotify.setEnableLoadMore(false)
        bindView.refreshNotify.setOnRefreshListener {
            //刷新全部通知
            requestData()
            bindView.refreshNotify.finishRefresh()
        }
        bindView.refreshNotify.setOnLoadMoreListener() {
            //加载更多通知
            page++
        }
        bindView.btnAgree.click {
            //全部同意
            var notifyList = getBatchModifyList(0)
            if (notifyList != null && notifyList.size > 0) {
                mViewModel.putBatchModify(notifyList)
            } else {
                getString(R.string.请选择需要操作的通知).toast()
            }
            val allList = mutableListOf<String>()
            mNotifyList.forEach {
                ChatDao.getNotifyDb().updateRead(it.id, 1)
                allList.add(it.id)
            }
            WebsocketWork.WS.sendSysNotifyReadState(allList)
        }
        bindView.btnRefuse.click {
            //全部拒绝
            var notifyList = getBatchModifyList(1)
            if (notifyList != null && notifyList.size > 0) {
                mViewModel.putBatchModify(notifyList)
            } else {
                getString(R.string.请选择需要操作的通知).toast()
            }
            val allList = mutableListOf<String>()
            mNotifyList.forEach {
                ChatDao.getNotifyDb().updateRead(it.id, 1)
                allList.add(it.id)
            }
            WebsocketWork.WS.sendSysNotifyReadState(allList)
        }
    }

    /**获取选择了的 同意 或者 拒绝的数据*/
    private fun getBatchModifyList(i: Int): MutableList<BatchModify> {
        var mList = mutableListOf<BatchModify>()
        mNotifyList.forEach {
            if (it.selectType == 1) {
                mList.add(
                    BatchModify(
                        it.groupId ?: "",
                        it.id,
                        if (i == 0) "Accepted" else "Refused"
                    )
                )
            }
        }
        return mList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAdapter() {
        mAdapter.addItemBinder(NotifyItem(onClickListener = { type, bean ->
            when (type) {
                0 -> {
                    //同意
                    bean.verifyType = 1
                    bean.status = "Accepted"
                    operateNotifyBean = bean
                    bean.groupId?.let {
                        bean.id?.let { it1 ->
                            mViewModel.modifyGroup(
                                it,
                                it1, AskStatus.Accepted
                            )
                        }
                    }
                }
                1 -> {
                    //拒绝
                    bean.verifyType = 2
                    bean.status = "Refused"
                    operateNotifyBean = bean
                    ChatDao.getNotifyDb().updateRead(bean.id, 1)
                    WebsocketWork.WS.sendSysNotifyReadState(mutableListOf<String>().apply {
                        add(operateNotifyBean?.id ?: "")
                    })
                    bean.groupId?.let {
                        bean.id?.let { it1 ->
                            mViewModel.modifyGroup(
                                it,
                                it1, AskStatus.Refused
                            )
                        }
                    }
                }
                2 -> {
                    //删除一条通知消息
                    showDelDialog(bean)
                }
                3 -> {
                    //选中 取消
                    mNotifyList.forEachIndexed { index, it ->
                        if (it.id == bean.id) {
                            it.selectType = bean.selectType
                            mAdapter.notifyItemChanged(index)
                        }
                    }
                }
                4 -> {
                    //点击item
                    activity?.let {
                        bean.content?.let { it1 ->

                            ChatDao.getNotifyDb().updateRead(bean.id, 1)
                            WebsocketWork.WS.sendSysNotifyReadState(mutableListOf<String>().apply {
                                add(bean.id)
                            })

                            WebActivity.start(
                                it,
                                it1,
                                getString(R.string.xiangqing),
                                1
                            )

                            updateNotifyMsgList(notifyType)
                        }
                    }
                }
            }
        }))
        bindView.listNotify.adapter = mAdapter
        setEmptyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun requestData() {
        updateNotifyMsgList(notifyType)
        if (notifyType == 1) {
            ChatDao.getNotifyDb().updateNotifyRead(2, 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun observeCallBack() {
        //获取好友申请列表
        mNotifyViewModel.getFriendNotifyInfo.observe(this) {
            mNotifyList.clear()
            it.data?.let { it1 ->
                mNotifyList.addAll(it1.reversed())
            }
            mAdapter.setList(mNotifyList)
        }
        mNotifyViewModel.getSystemMsg.observe(this) {
            mNotifyList.clear()
            LiveEventBus.get(EventKeys.UPDATE_COUNT).post("")
            //显示系统消息
            var data = ChatDao.getNotifyDb().getNotifySystemMsg().sortedBy { it.createTime }
                .asReversed()
            data?.let { mNotifyList.addAll(data) }
            mAdapter.setList(mNotifyList)
        }
        mViewModel.setGroupMemberLiveData.observe(this) { result ->
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
                    //存取数据 并更新界面
                    if (result.data != true) {
                        //说明成员已在群
                        operateNotifyBean?.verifyType = 3
                    }
                    //设置为已读
                    ChatDao.getNotifyDb().updateRead(operateNotifyBean?.id ?: "", 1)
                    WebsocketWork.WS.sendSysNotifyReadState(mutableListOf<String>().apply {
                        add(operateNotifyBean?.id ?: "")
                    })
                    operateNotifyBean?.let { ChatDao.getNotifyDb().saveNotifyMsg(it) }
                }
            }
        }

        mViewModel.putBatchModify.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.设置成功).toast()
                    //保存状态保存到本地，本更新界面
                    result.data?.forEach {
                        mNotifyList.forEach { n ->
                            if (it.id == n.id) {
                                n.status = it.status
                                n.verifyType = ChatDao.getNotifyDb().getVerifyType(n.status ?: "")
                                ChatDao.getNotifyDb().updateNotifyMsg(n)
                            }
                        }
                    }
                    LiveEventBus.get(EventKeys.OPERATE_NOTIFY_MSG).post(true)
                }
            }
        }

        //删除一条通知消息
        mNotifyViewModel.deleteNotify.observe(this) { result ->
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
                    notifyBean?.let {
                        ChatDao.getNotifyDb().delNotifyMsgById(it.id)
                        mAdapter.data.forEach { a ->
                            if (a is NotifyBean) {
                                if (a.id == it.id) {
                                    mAdapter.data.remove(a)
                                    //刷新界面
                                    mAdapter.notifyDataSetChanged()
                                    //更新会话界面消息
                                    saveConversationMsg()
                                    return@forEach
                                }
                            }
                        }
//                        //发广播通知主页更新
//                        LiveEventBus.get(EventKeys.DELETE_NOTIFY_MSG, String::class.java)
//                            .post(notifyBean?.id)
                    }
                }
            }
        }

        /*接收删除一条好友通知消息*/
        LiveEventBus.get(EventKeys.DELETE_NOTIFY_MSG, String::class.java).observe(this) {
            //刷新界面
            requestData()
            //更新会话界面消息
            saveConversationMsg()
        }

        LiveEventBus.get(EventKeys.UPDATE_NOTIFY).observe(this) {
            //更新
            requestData()
        }
    }

    /**
     * 保存到回话信息
     */
    private fun saveConversationMsg() {
        var notifyList = ChatDao.getNotifyDb().getNotifyMsg()
        //如果删除的是最新的一条,需更新会话消息界面数据
        if (notifyList?.isNotEmpty() == true) {
            //说明删除的是最新一条，但不是最后一条
            //更新会话消息界面数据
            notifyList.last().also { n ->
                ChatDao.getConversationDb()
                    .updateNotifyLastMsg(
                        msgType = n.type,
                        content = ChatDao.getNotifyDb()
                            .getTypeTitle(n.chatType),
                        msgTime = n.createTime,
                        isUpdateNotifyMsg = true
                    )
            }
        } else {
            //说明最新一条就是最后一条
            //更新会话消息界面数据
            ChatDao.getConversationDb()
                .updateNotifyLastMsg(
                    msgType = 0,
                    content = "",
                    msgTime = System.currentTimeMillis(),
                    isUpdateNotifyMsg = true
                )
        }
    }


    /**获取数据显示界面*/
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotifyMsgList(type: Int) {
        mNotifyList.clear()
        when (type) {
            0 -> {
                mNotifyViewModel.getSysNotice()
            }
            1 -> {
                //显示好友申请通知
                mNotifyViewModel.getFriendNotifyInfo()
            }
            2 -> {
                //显示群申请通知
                setNotifyView(3)
            }
        }
    }

    /**是否显示编辑 多选数据*/
    fun updateNotifyMsgList(positionPager: Int, isEdit: Boolean, isSelectAll: Boolean) {
        when (positionPager) {
            1 -> {
                //显示好友申请通知
                showSelectView(isEdit, isSelectAll)
            }
            2 -> {
                //显示群申请通知
                showSelectView(isEdit, isSelectAll)
            }
        }
        if (isEdit) {//编辑状态不能刷新数据
            bindView.refreshNotify.setEnableRefresh(false)
        } else {
            bindView.refreshNotify.setEnableRefresh(true)
        }
    }

    /**显示编辑界面*/
    private fun showSelectView(isEdit: Boolean, isSelectAll: Boolean) {
        mNotifyList.forEach {
            if (isEdit) {
                bindView.llVerify.visible()
                it.isShowCheck = true
            } else {
                bindView.llVerify.gone()
                it.isShowCheck = false
            }
            if (isSelectAll) {
                it.selectType = if (it.verifyType == 0) 1 else 2
            } else {
                it.selectType = if (it.verifyType == 0) 0 else 2
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    private fun setNotifyView(type: Int) {
        var data = ChatDao.getNotifyDb().getNotifyMsgList(type).sortedBy { it.createTime }
            .asReversed()
        data?.let { mNotifyList.addAll(data) }
        mAdapter.setList(mNotifyList)
    }

    var notifyBean: NotifyBean? = null

    //<editor-fold defaultstate="collapsed" desc="dialog 弹框">
    private fun showDelDialog(bean: NotifyBean) {
        HintDialog(
            getString(R.string.zhuyi),
            getString(R.string.是否确认删除此系统通知消息),
            object : ConfirmDialogCallback {
                override fun onItemClick() {
                    notifyBean = bean
                    if (notifyBean?.type == 0 || notifyBean?.type == 1) {
                        //删除系统通知
                        mNotifyViewModel.deleteSystemNotify(arrayListOf(bean.id))
                    } else {
                        //删除群申请通知
                        mNotifyViewModel.deleteGroupNotify(arrayListOf(bean.id))
                    }
                }
            },
            R.drawable.ic_dialog_top
        ).show(childFragmentManager, "HintDialog")
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="设置空布局">
    private var mEmptyBind: FragmentNotifyLayoutEmptyBinding? = null
    private fun setEmptyView() {
        if (mEmptyBind == null)
            mEmptyBind = FragmentNotifyLayoutEmptyBinding.inflate(layoutInflater)
        mAdapter.setEmptyView(mEmptyBind!!.root)
    }
    //</editor-fold>
}