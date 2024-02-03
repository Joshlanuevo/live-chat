package com.ym.chat.popup

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.xu.xpopupwindow.XPopupWindow
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.ui.ContactActivity
import com.ym.chat.ui.QRCodeActivity
import com.ym.chat.ui.SearchFriendActivity

/**
 * 首页右上方
 * popupWindow
 */
class HomeAddPopupWindow(var ctx: Context) : XPopupWindow(ctx) {
    override fun getLayoutId(): Int {
        return R.layout.popup_home_add
    }

    override fun getLayoutParentNodeId(): Int {
        return R.id.parent
    }

    override fun initViews() {
        var tvGroupChat = getPopupView().findViewById<TextView>(R.id.tvGroupChat)
        var vGroupChat = getPopupView().findViewById<View>(R.id.vGroupChat)
        vGroupChat.visible()
        if (MMKVUtils.isAdmin()) {
            var vGroupChat = getPopupView().findViewById<View>(R.id.vGroupChat)
            tvGroupChat.visible()
//            vGroupChat.visible()
        }
        tvGroupChat.click {
            dismiss()
            //实现发起群聊
            ContactActivity.start(ctx, 0)
        }
        var tvAddFriend = getPopupView().findViewById<TextView>(R.id.tvAddFriend)
        tvAddFriend.click {
            dismiss()
            //实现添加好友
            ctx.startActivity(Intent(ctx, SearchFriendActivity::class.java))
        }
        var tvQRCode = getPopupView().findViewById<TextView>(R.id.tvQRCode)
        tvQRCode.click {
            dismiss()
            //实现扫描二维码
            val intent = Intent(ctx, QRCodeActivity::class.java)
            ctx.startActivity(intent)
        }
    }

    override fun initData() {
    }

    override fun startAnim(view: View): Animator? {
        return null
    }

    override fun exitAnim(view: View): Animator? {
        return null
    }

    override fun animStyle(): Int {
        return -1
    }

}