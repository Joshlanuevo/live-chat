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
 * 长按头像
 * popupWindow
 */
class ChatHeaderPopupWindow(
    val ctx: Context,
    val onItemClickListener: ((type: Int) -> Unit)? = null
) :
    XPopupWindow(ctx) {
    private var tvATa: TextView? = null
    private var tvDelGroup: TextView? = null
    private var tvMute: TextView? = null
    private var vATa: View? = null
    private var vDelGroup: View? = null
    override fun getLayoutId(): Int {
        return R.layout.popup_chat_header
    }

    override fun getLayoutParentNodeId(): Int {
        return R.id.parent
    }

    override fun initViews() {
        tvATa = getPopupView().findViewById<TextView>(R.id.tvATa)
        vATa = getPopupView().findViewById<View>(R.id.vATa)
        tvDelGroup = getPopupView().findViewById<TextView>(R.id.tvDelGroup)
        vDelGroup = getPopupView().findViewById<View>(R.id.vDelGroup)
        tvMute = getPopupView().findViewById<TextView>(R.id.tvMute)
        tvATa?.click {
            dismiss()
            //@人
            onItemClickListener?.invoke(0)
        }
    }

    fun setShowDelGroup(isShowViewDelGroup: Boolean) {
        if(isShowViewDelGroup) {
            vATa?.visible()
            tvDelGroup?.visible()
            tvDelGroup?.click {
                dismiss()
                //移除群聊
                onItemClickListener?.invoke(1)
            }
        }
    }
    fun setShowMute(isShowViewMute: Boolean) {
        if(isShowViewMute) {
            vDelGroup?.visible()
            tvMute?.visible()
            tvMute?.click {
                dismiss()
                //禁言
                onItemClickListener?.invoke(2)
            }
        }
    }
    fun setStateMute(isMuteState: Boolean) {
        if(isMuteState) {//限制是禁言状态
            tvMute?.text = ChatUtils.getString(R.string.取消禁言)
        }else{
            tvMute?.text = ChatUtils.getString(R.string.jinyan)
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