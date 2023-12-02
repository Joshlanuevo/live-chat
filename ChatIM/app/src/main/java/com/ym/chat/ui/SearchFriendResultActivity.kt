package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.chat.R
import com.ym.chat.bean.FriendInfoBean
import com.ym.chat.databinding.*
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.viewmodel.FriendViewModel

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 添加好友-搜索结果
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class SearchFriendResultActivity : LoadingActivity() {
    private val bindView: ActivityFriendSearchResultBinding by binding()
    private val mViewModel = FriendViewModel()
    private var mFriendInfo: FriendInfoBean? = null
    private var isFriend = false//是否是已添加的好友

    companion object {
        fun start(context: Context, friendInfo: FriendInfoBean) {
            var bundle = Bundle()
            bundle.putSerializable("friendInfo", friendInfo)

            var intent = Intent(context, SearchFriendResultActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        bindView.toolbar.tvTitle.text = getString(R.string.add_haoyou)
        intent?.extras?.getSerializable("friendInfo")?.let { friendInfo ->
            friendInfo as FriendInfoBean
            mFriendInfo = friendInfo
            bindView.tvNickname.text = friendInfo.name
            bindView.tvName.text = "${getString(R.string.zhanghao)}:${friendInfo.username}"
            bindView.layoutHeader.ivHeader.loadImg(friendInfo)
            if (friendInfo.id == MMKVUtils.getUser()?.id) {
                bindView.btnAddFriend.gone()
            } else {
                isFriend = mFriendInfo?.id?.let { ChatDao.getFriendDb().isFriendById(it) } == true
                if (isFriend) {
                    bindView.btnAddFriend.text = getString(R.string.liaotian)
                }
            }
        }

        bindView.toolbar.viewBack.click { finish() }
        bindView.btnAddFriend.click {
            if (isFriend) {
                //去聊天
                mFriendInfo?.id?.let { it1 -> ChatActivity.start(this, it1, 0)
                this.finish()}
            } else {
                //申请添加好友
                mViewModel.applyAddFriend(MMKVUtils.getUser()?.id ?: "", mFriendInfo?.id ?: "")
            }
        }
    }

    override fun requestData() {

    }

    override fun observeCallBack() {
        //处理添加好友操作
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
                    getString(R.string.添加好友成功).toast()
                    //单聊
                    mFriendInfo?.id?.let { ChatActivity.start(this, it, 0) }
                    finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    result.exc?.message.toast()
                }
            }
        }
    }

    /**
     * 获取所有好友数据 保存到本地
     */
    private fun getFriendListSaveAddFriend() {
        mViewModel.getFriendList()
    }
}