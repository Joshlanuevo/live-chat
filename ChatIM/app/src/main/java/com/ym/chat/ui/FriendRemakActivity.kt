package com.ym.chat.ui

import android.text.TextUtils
import androidx.core.widget.doAfterTextChanged
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.base.widget.ext.invisible
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.databinding.ActivityFriendRemarkBinding
import com.ym.chat.db.AccountDao
import com.ym.chat.db.ChatDao
import com.ym.chat.viewmodel.FriendRemarkViewModel
import com.ym.chat.viewmodel.FriendViewModel

/**
 * 好友资料信息
 */
class FriendRemakActivity : LoadingActivity() {
    private val bindView: ActivityFriendRemarkBinding by binding()
    private val mViewModel: FriendViewModel = FriendViewModel()

    private var name: String = ""
    private var chatInfo: FriendListBean? = null

    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.bianji)
        }

        bindView.etName.doAfterTextChanged {
            if (it.isNullOrEmpty()) {
                bindView.ivClear.invisible()
            } else {
                bindView.ivClear.visible()
            }
        }

        bindView.ivClear.setOnClickListener {
            bindView.etName.setText("")
        }

        bindView.btnSave.click {
            name = bindView.etName.text.toString().trim()
//            if (name.isNullOrBlank()) {
//                "备注不可为空".toast()
//                return@click
//            }
            chatInfo?.id?.let {
                mViewModel.modifyFriendStatus(it, remark = name)
            }
        }
    }

    override fun requestData() {
        chatInfo = intent.getSerializableExtra(FriendInfoActivity.REMARK) as FriendListBean
        chatInfo?.apply {
            if (this.remark.isNullOrBlank()) {
                bindView.etName.hint = getString(R.string.qingshuru)
            } else {
                bindView.etName.setText(this.remark)
            }

        }
    }

    override fun observeCallBack() {
        //编辑用户信息
        mViewModel.modifyFriendStatus.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.备注修改成功).toast()
                    chatInfo?.let {
                        it.remark = name
                        ChatDao.getFriendDb().updateRemark(it.id, name)
                    }
                    //发通知更新好友备注
                    LiveEventBus.get(
                        EventKeys.EDIT_FRIEND_REMARK_NOTICE,
                        FriendListBean::class.java
                    ).post(chatInfo)
                    finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.备注修改失败).toast()
                    }
                }
            }
        }

    }
}
