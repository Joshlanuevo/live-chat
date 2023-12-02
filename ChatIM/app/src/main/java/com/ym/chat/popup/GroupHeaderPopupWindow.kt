package com.ym.chat.popup

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.Utils
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
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.MemberLevelType
import com.ym.chat.utils.MsgType

/**
 * 群设置 长按头像
 * popupWindow
 */
class GroupHeaderPopupWindow(
    val ctx: Context,
    val onItemClickListener: ((type: Int) -> Unit)? = null
) :
    XPopupWindow(ctx) {
    private var tvSetAdmin: TextView? = null
    private var tvRemoveMember: TextView? = null
    private var tvSetMute: TextView? = null
    private var vSetAdmin: View? = null
    override fun getLayoutId(): Int {
        return R.layout.popup_group_header
    }

    override fun getLayoutParentNodeId(): Int {
        return R.id.parent
    }

    override fun initViews() {
        tvSetAdmin = getPopupView().findViewById<TextView>(R.id.tvSetAdmin)
        vSetAdmin = getPopupView().findViewById<View>(R.id.vSetAdmin)
        tvRemoveMember = getPopupView().findViewById<TextView>(R.id.tvRemoveMember)
        tvSetMute = getPopupView().findViewById<TextView>(R.id.tvSetMute)
        tvSetAdmin?.click {
            dismiss()
            onItemClickListener?.invoke(0)
        }
        tvSetMute?.click {
            dismiss()
            onItemClickListener?.invoke(1)
        }
        tvRemoveMember?.click {
            dismiss()
            onItemClickListener?.invoke(2)
        }
    }

    fun setSettingAdmin(isAdmin: Boolean) {
        if(isAdmin) {//设置成管理员
            tvSetAdmin?.text = ChatUtils.getString(R.string.取消管理员)
        }else{
            tvSetAdmin?.text =  ChatUtils.getString(R.string.jingshengweiguanliyuan)
        }
    }

    fun setSettingMute(isMuteState: Boolean) {
        if(isMuteState) {//限制是禁言状态
            tvSetMute?.text = ChatUtils.getString(R.string.取消禁言)
        }else{
            tvSetMute?.text =  ChatUtils.getString(R.string.jinyan)
        }
    }

    fun setShowView(isShowAll: Boolean) {
        if(isShowAll) {//限制是禁言状态
            tvSetAdmin?.visible()
            vSetAdmin?.visible()
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