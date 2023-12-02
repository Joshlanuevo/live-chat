package com.ym.chat.popup

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.xu.xpopupwindow.XPopupWindow
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.ui.AddFriendActivity
import com.ym.chat.ui.ContactActivity
import com.ym.chat.ui.QRCodeActivity
import com.ym.chat.ui.SearchFriendActivity
import com.ym.chat.utils.MemberLevelType
import com.ym.chat.utils.MsgType

/**
 * 通知消息 右上方
 * popupWindow
 */
class NotifyPopupWindow(
    var ctx: Context,
    val onItemClickListener: ((type: Int) -> Unit)? = null
) :
    XPopupWindow(ctx) {
    private var isSystem: Boolean = false
    private var vGroupChat: View? = null
    private var tvNotifyEdit: View? = null
    override fun getLayoutId(): Int {
        return R.layout.popup_notify
    }

    override fun getLayoutParentNodeId(): Int {
        return R.id.parent
    }

    override fun initViews() {
        var tvNotifyDel = getPopupView().findViewById<TextView>(R.id.tvNotifyDel)
        tvNotifyDel.click {
            dismiss()
            //删除
            onItemClickListener?.invoke(0)
        }
//        if (!isSystem) {
//            vGroupChat = getPopupView().findViewById<View>(R.id.vGroupChat)
//            tvNotifyEdit = getPopupView().findViewById<TextView>(R.id.tvNotifyEdit)
//            vGroupChat?.visible()
//            tvNotifyEdit?.visible()
//            tvNotifyEdit?.click {
//                dismiss()
//                //编辑
//                onItemClickListener?.invoke(1)
//            }
//        }
    }

    fun setSystemView(isSystem: Boolean) {
        this.isSystem = isSystem
        if (isSystem) {
            vGroupChat?.gone()
            tvNotifyEdit?.gone()
        } else {
            vGroupChat?.visible()
            tvNotifyEdit?.visible()
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