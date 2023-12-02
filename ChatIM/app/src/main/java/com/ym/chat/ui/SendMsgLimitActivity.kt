package com.ym.chat.ui

import android.widget.CompoundButton
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.*
import com.ym.chat.viewmodel.SetViewModel

/**
 * @version V1.0
 * @createAuthor
 *       ___         ___          ___
 *      /  /\       /  /\        /  /\           ___
 *     /  /::\     /  /:/       /  /::\         /__/|
 *    /  /:/\:\   /__/::\      /  /:/\:\    __  | |:|
 *   /  /:/~/::\  \__\/\:\    /  /:/~/::\  /__/\| |:|
 *  /__/:/ /:/\:\    \  \:\  /__/:/ /:/\:\ \  \:\_|:|
 *  \  \:\/:/__\/    \__\:\  \  \:\/:/__\/  \  \:::|
 *   \  \::/         /  /:/   \  \::/        \  \::|
 *    \  \:\        /__/:/     \  \:\         \  \:\
 *     \  \:\       \__\/       \  \:\         \  \:\
 *      \__\/                    \__\/          \__\/
 * @createDate  2021/02/05 14:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 发言频率限制 设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class SendMsgLimitActivity : LoadingActivity(), CompoundButton.OnCheckedChangeListener {
    private val bindView: ActivitySendMsgLimitBinding by binding()
    private val mViewModel = SetViewModel()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.fanyanpinlvxianzhi)
        }
        bindView.cb1.setOnCheckedChangeListener(this)
        bindView.cb2.setOnCheckedChangeListener(this)
        bindView.cb3.setOnCheckedChangeListener(this)
        bindView.cb4.setOnCheckedChangeListener(this)
    }

    override fun requestData() {
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
                }
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Checked处理">
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView) {
            bindView.cb1 -> showCheckedView(1, isChecked)
            bindView.cb2 -> showCheckedView(2, isChecked)
            bindView.cb3 -> showCheckedView(3, isChecked)
            bindView.cb4 -> showCheckedView(4, isChecked)
        }
    }

    private fun showCheckedView(i: Int, checked: Boolean) {
        if (checked) {
            when (i) {
                1 -> {
                    bindView.cb1.isEnabled = false//选中以后失去焦点
                    bindView.cb2.isEnabled = true
                    bindView.cb3.isEnabled = true
                    bindView.cb4.isEnabled = true
                    bindView.cb2.isChecked = false
                    bindView.cb3.isChecked = false
                    bindView.cb4.isChecked = false
                }
                2 -> {
                    bindView.cb1.isEnabled = true
                    bindView.cb2.isEnabled = false//选中以后失去焦点
                    bindView.cb3.isEnabled = true
                    bindView.cb4.isEnabled = true
                    bindView.cb1.isChecked = false
                    bindView.cb3.isChecked = false
                    bindView.cb4.isChecked = false
                }
                3 -> {
                    bindView.cb1.isEnabled = true
                    bindView.cb2.isEnabled = true
                    bindView.cb3.isEnabled = false//选中以后失去焦点
                    bindView.cb4.isEnabled = true
                    bindView.cb1.isChecked = false
                    bindView.cb2.isChecked = false
                    bindView.cb4.isChecked = false
                }
                4 -> {
                    bindView.cb1.isEnabled = true
                    bindView.cb2.isEnabled = true
                    bindView.cb3.isEnabled = true
                    bindView.cb4.isEnabled = false//选中以后失去焦点
                    bindView.cb1.isChecked = false
                    bindView.cb2.isChecked = false
                    bindView.cb3.isChecked = false
                }
            }
            //处理后台请求数据

        }
    }
    //</editor-fold>
}