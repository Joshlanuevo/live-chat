package com.ym.chat.ui

import android.content.Intent
import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.databinding.ActivityChangeServiceidBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/17 14:46
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 换一个
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ChangeServiceIdActivity : BaseActivity() {
    private val bindView: ActivityChangeServiceidBinding by binding()
    override fun initView() {
        bindView.tvBack.click { finish() }
        bindView.llScan.click {
            val intent = Intent(this, QRCodeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
    }
}