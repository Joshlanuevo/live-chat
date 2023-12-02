package com.ym.chat.ui

import android.content.Intent
import com.dylanc.viewbinding.binding
import com.ym.base.mvvm.BaseActivity
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.ActivityAddFriendBinding
import com.ym.chat.databinding.ActivityModifyInfoBinding
import com.ym.chat.databinding.ActivityPersonalBinding
import com.ym.chat.databinding.ActivityShareCodeBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 添加好友
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class AddFriendActivity : BaseActivity() {
    private val bindView: ActivityAddFriendBinding by binding()
    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = "${getString(R.string.add_haoyou)}"
        }
        bindView.tvSearch.click {
            startActivity(Intent(this, SearchFriendActivity::class.java))
        }
    }

    override fun requestData() {
        bindView.tvUsername.text = "${getString(R.string.我的账号)}：${MMKVUtils.getUser()?.username ?: ""}"
    }

    override fun observeCallBack() {
    }
}