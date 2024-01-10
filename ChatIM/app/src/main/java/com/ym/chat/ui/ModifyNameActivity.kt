package com.ym.chat.ui

import android.text.TextUtils
import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.toast
import com.ym.base.ext.trimText
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.invisible
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityModifyInfoBinding
import com.ym.chat.db.AccountDao
import com.ym.chat.viewmodel.ModifyNameViewModel

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 修改姓名、性别、设置签名
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ModifyNameActivity : LoadingActivity() {
    private val bindView: ActivityModifyInfoBinding by binding()
    private val mViewModel: ModifyNameViewModel = ModifyNameViewModel()

    private var name: String = ""
    private var gender = "male"
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
            name = bindView.etName.text.toString().replace("\\s".toRegex(), "")
            if (name.isNullOrBlank()) {
                getString(R.string.姓名不可为空).toast()
                return@click
            }
//            var sign = bindView.etSigin.text.toString()

            gender = when {
                bindView.rbm.isChecked -> {
                    "Male"
                }
                bindView.rbf.isChecked -> {
                    "Female"
                }
                else -> {
                    "Other"
                }
            }
            mViewModel.editUserInfo(name = name, gender = gender)
        }
    }

    override fun requestData() {
        var userInfo = MMKVUtils.getUser()
        userInfo.apply {
            bindView.etName.setText(this?.name)
            if ("Female".lowercase() == this?.gender?.lowercase()) {
                bindView.rbf.isChecked = true
            } else if ("Male".lowercase() == this?.gender?.lowercase()) {
                bindView.rbm.isChecked = true
            } else {
                bindView.rbNo.isChecked = true
            }

        }
    }

    override fun observeCallBack() {
        //编辑用户信息
        mViewModel.editUserInfoLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    MMKVUtils.getUser()?.name = name
                    MMKVUtils.getUser()?.gender = gender
                    LiveEventBus.get(EventKeys.EDIT_USER_NAME, String::class.java).post(name)
                    MMKVUtils.getUser()?.let {
                        AccountDao.saveAccount(
                            it,
                            mobile = AccountDao.getAccountMobile(),
                            password = AccountDao.getAccountPwd()
                        )
                    } //把修改的信息保存到本地账号
                    this.finish()
                    getString(R.string.编辑用户信息成功).toast() // "编辑用户信息成功"
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