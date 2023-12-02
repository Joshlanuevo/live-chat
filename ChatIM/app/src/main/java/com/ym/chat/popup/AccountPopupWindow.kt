package com.ym.chat.popup

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseBinderAdapter
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.xu.xpopupwindow.XPopupWindow
import com.ym.base.ext.logD
import com.ym.base.widget.BaseVBHolder
import com.ym.base.widget.BaseVBQuickAdapter
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.AccountBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.databinding.ItemAccountListBinding
import com.ym.chat.databinding.ItemLoginAccountBinding
import com.ym.chat.databinding.PopupAccountListBinding
import com.ym.chat.ext.roundLoad
import com.ym.chat.item.CollectContentItem
import com.ym.chat.item.CollectTimeItem
import com.ym.chat.ui.AddFriendActivity
import com.ym.chat.ui.ContactActivity
import com.ym.chat.ui.QRCodeActivity
import com.ym.chat.ui.SearchFriendActivity

/**
 * 登录 选择用户手机号码 下拉框
 * popupWindow
 */
class AccountPopupWindow(
    ctx: Context,
    var onItemClickListener: ((memberBean: AccountBean) -> Unit)? = null
) : XPopupWindow(ctx) {

    var accounts: MutableList<AccountBean>? = null
    var rvAccount: RecyclerView? = null

    fun setData(accounts: MutableList<AccountBean>) {
        this.accounts = accounts
        val mAdapter = AccountAdapter()
        rvAccount?.adapter = mAdapter
        mAdapter.setList(accounts)
    }

    override fun getLayoutId(): Int {
        return R.layout.popup_account_list
    }

    override fun getLayoutParentNodeId(): Int {
        return R.id.parent
    }

    override fun initViews() {
        rvAccount = getPopupView().findViewById<RecyclerView>(R.id.rv_account)
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

    inner class AccountAdapter :
        BaseVBQuickAdapter<AccountBean, ItemLoginAccountBinding>() {
        override fun convert(
            holder: BaseVBHolder<ItemLoginAccountBinding>,
            item: AccountBean
        ) {
            holder.viewBinding.apply {
                holder.viewBinding.tvPhoneNmb.text =
                    if (item.accountType == 0) item.mobile else item.showUsername()
                holder.viewBinding.root.pressEffectAlpha().click {
                    onItemClickListener?.invoke(item)
                }
            }
        }
    }

}