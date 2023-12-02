package com.ym.chat.ui

import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityGroupsendUtilsBinding
import com.ym.chat.databinding.FragmentNotifyLayoutEmptyBinding
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.item.SendGroupMsgItem
import com.ym.chat.popup.SelectPopWindow
import com.ym.chat.viewmodel.SendGroupMsgViewModel

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 群发助手
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class GroupSendUtilsActivity : LoadingActivity() {
    private val bindView: ActivityGroupsendUtilsBinding by binding()
    private val mViewModel = SendGroupMsgViewModel()
    private val mAdapter = BaseBinderAdapter()
    private var curPage: Int = 1
    private val pageSize: Int = 12
    private var isRefresh = false //是否在刷新 加载
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.群发助手)
            ivSearch.setImageResource(R.drawable.chat_right_more)
            ivSearch.pressEffectAlpha().click {
                //显示弹框
                showPopup()
            }
        }
        bindView.btnSend.click { ContactActivity.start(this, 1) }
        bindView.btnSendMsg.click { ContactActivity.start(this, 1) }
    }

    /**
     * 显示底部弹框
     */
    private fun showPopup() {
        SelectPopWindow(this)
            .apply {
                onItemClickListener = { data, position, view ->
                    dismiss()
                    HintDialog(
                        getString(R.string.qingkongjilu),
                        getString(R.string.您确定要清空所有记录吗),
                        callback = object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                mViewModel.delSendGroupMsg()
                            }
                        }, isShowHeader = false, isTitleTxt = true
                    ).show(supportFragmentManager, "HintDialog")

                }
            }.showPopupWindow()
    }

    override fun onResume() {
        super.onResume()
        mViewModel.getSendGroupMsg(curPage.toString(), pageSize.toString())
    }


    override fun requestData() {
        mAdapter.addItemBinder(SendGroupMsgItem(onItemClickListener = {
            //处理 重新再发一次群发消息
            it.receiverName?.let { it1 ->
                it.receiverId?.let { it2 ->
                    ChatActivity.start(
                        this,
                        "",
                        2,
                        3,
                        friendNames = it1,
                        friendIds = it2
                    )
                }
            }
        }))
        bindView.listSendGroupMsg.adapter = mAdapter
        bindView.refreshLayout.setEnableLoadMore(false)
        //下拉刷新
        bindView.refreshLayout.setOnRefreshListener {
            curPage = 1
            isRefresh = true
            mViewModel.getSendGroupMsg(curPage.toString(), pageSize.toString())
        }
        //加载更多
        bindView.refreshLayout.setOnLoadMoreListener() {
            curPage++
            isRefresh = true
            mViewModel.getSendGroupMsg(curPage.toString(), pageSize.toString())
        }
    }

    override fun observeCallBack() {
        //获取群发消息列表
        mViewModel.sendGroupMsgList.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    if (!isRefresh)
                        showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    bindView.refreshLayout.finishLoadMore()
                    bindView.refreshLayout.finishRefresh()
                    isRefresh = false
                    hideLoading()
                    //显示列表数据
                    if (result.data?.data?.records?.size!! > 0) {
                        bindView.llMsg.visible()
                        bindView.toolbar.ivSearch.visible()
                        bindView.llNotMsg.gone()
                        if (curPage == 1) {
                            //首次加载数据 或者 刷新数据
                            mAdapter.setList(result.data?.data?.records)
                        } else {
                            //加载更多
                            result.data?.data?.records?.let { mAdapter.addData(it) }
                        }
                        //根据数据 是否还需要显示加载更多
                        if (curPage * pageSize > result.data?.data?.total!!) {
                            bindView.refreshLayout.setEnableLoadMore(false)
                        } else {
                            bindView.refreshLayout.setEnableLoadMore(true)
                        }
                    } else {
                        //显示没有列表的数据
                        bindView.llNotMsg.visible()
                        bindView.toolbar.ivSearch.gone()
                        bindView.llMsg.gone()
                        bindView.refreshLayout.setEnableLoadMore(false)
                    }
                }
                is BaseViewModel.LoadState.Fail -> {
                    bindView.refreshLayout.finishLoadMore()
                    bindView.refreshLayout.finishRefresh()
                    isRefresh = false
                    hideLoading()
                    bindView.llNotMsg.visible()
                    bindView.toolbar.ivSearch.gone()
                    bindView.llMsg.gone()
                }
            }
        }

        //清空群发消息列表
        mViewModel.delSendGroupMsg.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.已清空群发消息).toast()
                    mAdapter.setList(mutableListOf())
                    bindView.llMsg.gone()
                    bindView.llNotMsg.visible()
                    bindView.toolbar.ivSearch.gone()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    bindView.llNotMsg.visible()
                    bindView.toolbar.ivSearch.gone()
                }
            }
        }
    }
}