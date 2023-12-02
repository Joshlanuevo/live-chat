package com.ym.chat.ui

import com.dylanc.viewbinding.binding
import com.ym.base.ext.logD
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseActivity
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.chat.BuildConfig
import com.ym.chat.R
import com.ym.chat.bean.UpdateResponse
import com.ym.chat.bean.VersionBean
import com.ym.chat.databinding.*
import com.ym.chat.dialog.updateDialog
import com.ym.chat.ext.roundLoad
import com.ym.chat.utils.AppManagerUtils
import com.ym.chat.utils.Utils
import com.ym.chat.viewmodel.SetViewModel

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 关于友聊
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class AboutJxActivity : LoadingActivity() {
    private val bindView: ActivityAboutJixinBinding by binding()
    private val mViewModel = SetViewModel()
    private var version: String? = null
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = "${getString(R.string.关于)}${getString(R.string.app_name)}"
        }
        bindView.linVersion.click {
            mViewModel.getAppVersion()
        }
    }

    override fun requestData() {
        version = AppManagerUtils.getVersionName(this)
        bindView.tv2.text = "Chat $version"
        bindView.tv3.text = "${getString(R.string.version)} $version"
        bindView.tv1.text = getString(R.string.app_name)
    }

    override fun observeCallBack() {
        mViewModel.appVersion.observe(this) { result ->
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
                    if (version?.isNotBlank() == true && result?.data?.versionNo?.isNotBlank() == true) {
                        if (Utils.isNewAppVersion(version!!, result?.data?.versionNo!!)) {
                            result.data?.let { Utils.showUpdateDialog(it, supportFragmentManager) }
                        } else {
                            getString(R.string.已经是最新版本).toast()
                        }
                    }
                }
            }
        }
    }
}