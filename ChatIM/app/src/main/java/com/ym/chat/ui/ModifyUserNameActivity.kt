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
import com.ym.base.widget.ext.invisible
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityModifyUsernameBinding
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.utils.PatternUtils
import com.ym.chat.viewmodel.ModifyNameViewModel

/**
 * @description 修改友聊好 username
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ModifyUserNameActivity : LoadingActivity() {
    private val bindView: ActivityModifyUsernameBinding by binding()
    private val mViewModel: ModifyNameViewModel = ModifyNameViewModel()
    private var username = ""

    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.修改账号)
        }

        bindView.etName.doAfterTextChanged {
            if (it.isNullOrEmpty()) {
                bindView.ivClear.invisible()
            } else {
                bindView.ivClear.visible()
                if (it.toString().contains("\\s".toRegex())) {
                    val content = it.toString().replace("\\s".toRegex(), "")
                    bindView.etName.setText(content)
                    bindView.etName.setSelection(content.length)
                }
            }
        }

        bindView.ivClear.setOnClickListener {
            bindView.etName.setText("")
        }

        bindView.btnSave.click {
            username = bindView.etName.text.toString().trim()
            if (username.isNotEmpty()) {
                if (!PatternUtils.isUsername(username)) {
                    getString(R.string.您输入的友聊号有误).toast()
                    return@click
                }
                var content = if (MMKVUtils.isAdmin()) getString(R.string.确定要修改友聊号) else getString(R.string.友聊号只能修改一次)
                HintDialog(
                    getString(R.string.zhuyi),
                    content,
                    isShowBtnCancel = true,
                    iconId = R.drawable.ic_dialog_top,
                    callback = object : ConfirmDialogCallback {
                        override fun onItemClick() {
                            mViewModel.editUsername(username)
                        }
                    }, isSettingTitleColor = true
                ).show(supportFragmentManager, "HintDialog")
            } else {
                getString(R.string.友聊号不能为空2).toast()
            }
        }
    }

    override fun requestData() {
        var userInfo = MMKVUtils.getUser()
        userInfo?.apply {
            var username = this.username
            if (username.isEmpty()) {
                username = this.code
            }
            bindView.etName.setText(username)
        }
    }

    override fun observeCallBack() {
        //编辑用户信息
        mViewModel.modifyUsername.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    MMKVUtils.getUser()?.username = username
                    MMKVUtils.putUser(MMKVUtils.getUser())
                    LiveEventBus.get(EventKeys.EDIT_USER_NAME, String::class.java).post("")
                    this.finish()
                    getString(R.string.编辑用户信息成功).toast()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.编辑用户信息失败).toast()
                    }
                }
            }
        }
    }
}