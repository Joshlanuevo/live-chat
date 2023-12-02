package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.databinding.*
import java.util.*

/**
 * 群发消息
 */
class SendGroupMessageActivity : BaseActivity() {
    private val bindView: ActivitySendGroupMessageBinding by binding()

    private var friendIds: String = ""

    companion object {
        const val FRIEND_id_LIST = "friend_id_list"
        const val FRIEND_NAME_LIST = "friend_name_list"
        fun start(context: Context, friendNames: String, friendIds: String) {
            val intent = Intent(context, SendGroupMessageActivity::class.java)
            intent.putExtra(FRIEND_id_LIST, friendIds)
            intent.putExtra(FRIEND_NAME_LIST, friendNames)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = getString(R.string.群发消息)
        }
    }

    override fun requestData() {
        intent?.let { intent ->
            var friendNames = intent.getStringExtra(FRIEND_NAME_LIST) ?:""
            friendIds = intent.getStringExtra(FRIEND_id_LIST) ?:""
            bindView.tvFriendName.text=friendNames
        }
        //弹出软键盘
        inPutSoftKeyboard()
    }

    private fun inPutSoftKeyboard() {
        bindView.etContent.isFocusable = true
        bindView.etContent.isFocusableInTouchMode = true
        bindView.etContent.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
//        var timer =Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                var manager =bindView.etContent.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                manager.showSoftInput(bindView.etContent,0)
//                "执行弹出软键盘".toast()
//            }
//        },998)
    }


    override fun observeCallBack() {
    }
}