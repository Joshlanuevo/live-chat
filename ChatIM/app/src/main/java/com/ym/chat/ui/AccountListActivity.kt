package com.ym.chat.ui


import android.content.Intent
import android.text.TextUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.ym.base.ext.mContext
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.AccountBean
import com.ym.chat.databinding.ActivityAccountListBinding
import com.ym.chat.db.AccountDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.item.AccountItem
import com.ym.chat.viewmodel.SetViewModel
import rxhttp.RxHttpPlugins

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 切换账号
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class AccountListActivity : LoadingActivity() {
    private val bindView: ActivityAccountListBinding by binding()
    private val mViewModel: SetViewModel = SetViewModel()
    private val mAdapter = BaseBinderAdapter()
    private var accList = mutableListOf<AccountBean>()
    private var isEdit = false //是否在编辑状态
    private var changedType = 1 //切换类型 1指定账号切换 2 新账号切换
    private var mobile = ""
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.设置)
            tvSubtitle.text = getString(R.string.bianji)
            tvSubtitle.visible()
            tvSubtitle.click {
                isEdit = !isEdit
                setEditText(isEdit)
                accList.forEachIndexed { index, loginData ->
                    loginData.isEdit = isEdit
                }
                mAdapter.setList(accList)
            }
        }
        bindView.tvChangeAccount.click {
            HintDialog(
                getString(R.string.切换并退出当前账号),
                getString(R.string.您确定要退出当前帐号吗),
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        changedType = 2
                        mViewModel.loginOut()
                    }
                }, headUrl = MMKVUtils.getUser()?.headUrl, isShowHeader = true, isTitleTxt = true
            ).show(supportFragmentManager, "HintDialog")
        }
    }

    private fun setEditText(edit: Boolean) {
        bindView.toolbar.tvSubtitle.text = if (edit) "${getString(R.string.完成)}" else "${getString(R.string.bianji)}"
    }

    override fun requestData() {
        mAdapter.addItemBinder(AccountItem(onDelItemClickListener = {
            HintDialog(
                getString(R.string.删除账号),
                getString(R.string.shanchutishi),
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        AccountDao.deleteAccount(it.idInDb)
                        accList.remove(it)
                        mAdapter.setList(accList)
                        if (accList.size > 5) {
                            //限制展示高度
                            bindView.rvAccount.layoutParams.height = SizeUtils.dp2px(5 * 66.0f)
                        } else {
                            bindView.rvAccount.layoutParams.height = SizeUtils.dp2px(accList.size * 66.0f)
                        }
                    }
                }, headUrl = it?.headUrl, isShowHeader = true, isTitleTxt = true
            ).show(supportFragmentManager, "HintDialog")
        }, onItemClickListener = {
            HintDialog(
                getString(R.string.切换账号),
                "${String.format(getString(R.string.qiehuanzhanghao), it.name)}",
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        changedType = 1
                        mobile = it.id
                        mViewModel.loginOut()
                    }
                }, headUrl = it?.headUrl, isShowHeader = true, isTitleTxt = true
            ).show(supportFragmentManager, "HintDialog")
        }))
        bindView.rvAccount.adapter = mAdapter
        val accounts = AccountDao.getAccounts()
        accounts.forEach { a ->
            a.isSelect = a.id == MMKVUtils.getUser()?.id
        }
        accounts.reverse()//倒叙排列
        accList.addAll(accounts)
        if (accList.size > 5) {
            //限制展示高度
            bindView.rvAccount.layoutParams.height = SizeUtils.dp2px(5 * 66.0f)
        }
        mAdapter.setList(accList)
    }

    override fun observeCallBack() {
        //退出登录
        mViewModel.loginOutLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //退出登录
                    RxHttpPlugins.cancelAll()
                    MMKVUtils.clearUserInfo()
                    ActivityUtils.finishAllActivities()
                    mContext.startActivity(Intent(mContext, LoginActivity::class.java)
                        .apply {
                            putExtra(LoginActivity.IN_TYPE, changedType)
                            putExtra(LoginActivity.MOBILE, mobile)
                        })
                    getString(R.string.已退出现有账号).toast()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.退出异常).toast()
                    }
                }
            }
        }
    }
}