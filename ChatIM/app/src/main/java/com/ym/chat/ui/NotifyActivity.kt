package com.ym.chat.ui

import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.dylanc.viewbinding.binding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.NotifyBean
import com.ym.chat.databinding.ActivityNotifyBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.popup.NotifyPopupWindow
import com.ym.chat.ui.fragment.NotifyFragment
import com.ym.chat.viewmodel.NotifyViewModel


class NotifyActivity : LoadingActivity() {
    private val bindView: ActivityNotifyBinding by binding()
    private val mViewModel = NotifyViewModel()
    private var mFragments = mutableListOf<NotifyFragment>()
    private var mTitles = arrayOf("")
    private var isEdit = false //是否在编辑状态
    private var positionPager = 0 //显示pager页码
    private var isSelectAll = true //默认选中状态
    private var ivRedPoint: ImageView? = null

    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            toolbar.setBackgroundResource(R.color.white)
            tvTitle.text = getString(R.string.通知)
            ivAdd.visible()
            ivAdd.setImageResource(R.drawable.chat_right_more)
            ivAdd.click {
                showNotifyPopW(positionPager == 0)
            }
        }
        bindView.cbAllSelect.click {
            isSelectAll = !isSelectAll
            mFragments[positionPager].updateNotifyMsgList(positionPager, isEdit, isSelectAll)
        }

        bindView.tab.addOnTabSelectedListener(object : OnTabSelectedListener {
            //选中
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.customView == null) {
                    tab.customView =
                        LayoutInflater.from(this@NotifyActivity)
                            .inflate(R.layout.tab_title_view, null)
                }
                //设置点击后的字体大小
                var tvTitle = tab.customView?.findViewById<TextView>(R.id.tvTitle)
                ivRedPoint = tab.customView?.findViewById<ImageView>(R.id.ivRedPoint)
                tvTitle?.textSize = 18f
                tvTitle?.setTextColor(getColor(R.color.color_main))
                //设置点击的内容
                tvTitle?.text = tab.text
                //显示未读红点
                if (showUnreadRedView(tab.text.toString())) ivRedPoint?.visible() else ivRedPoint?.gone()
            }

            //未选中的
            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.customView == null) {
                    tab.customView =
                        LayoutInflater.from(this@NotifyActivity)
                            .inflate(R.layout.tab_title_view, null)
                }
                //设置点击后的字体大小
                var tvTitle = tab.customView?.findViewById<TextView>(R.id.tvTitle)
                var ivRedPoint = tab.customView?.findViewById<ImageView>(R.id.ivRedPoint)
                tvTitle?.textSize = 14f
                tvTitle?.setTextColor(getColor(R.color.color_gray_text))
                //设置点击的内容
                tvTitle?.text = tab.text
                //显示未读红点
                if (showUnreadRedView(tab.text.toString())) ivRedPoint?.visible() else ivRedPoint?.gone()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showNotifyPopW(isSystem: Boolean) {
        //系统通知不能多选
        var npw = NotifyPopupWindow(this@NotifyActivity) {
            if (it == 0) {
                //删除所有通知消息
                showDelDialog()
            } else if (it == 1) {
                isEdit = !isEdit
                //编辑消息
                showEditView(isEdit)
            }
        }
//        npw?.setSystemView(isSystem)
        npw?.showPopup(bindView.toolbar.ivAdd, 200, 4, Gravity.BOTTOM)
    }

    /**显示是否在编辑状态view*/
    private fun showEditView(edit: Boolean) {
        var data = when (positionPager) {
            0 -> {
                ChatDao.getNotifyDb().getNotifySystemMsg()
            }

            1 -> {
                ChatDao.getNotifyDb().getNotifyMsgList(2)
            }

            2 -> {
                ChatDao.getNotifyDb().getNotifyMsgList(3)
            }

            else -> null
        }
        if (data != null && data.size > 0) {
            if (edit) {
                bindView.cbAllSelect.visible()
                mFragments[positionPager].updateNotifyMsgList(positionPager, edit, isSelectAll)
            } else {
                bindView.cbAllSelect.gone()
                mFragments[positionPager].updateNotifyMsgList(positionPager, edit, isSelectAll)
            }
            bindView.cbAllSelect.isChecked = edit
        } else {
            bindView.cbAllSelect.gone()
        }
    }

    override fun requestData() {
        mTitles =
            arrayOf(getString(R.string.系统), getString(R.string.haoyou), getString(R.string.群组));
        for (i in mTitles.indices) {
            mFragments.add(NotifyFragment(i))
        }
        bindView.viewPager.adapter = NotifyFragmentStateAdapter(this, mFragments)
        var index = intent.getIntExtra("index", -1)
        if (index >= 0) {
            bindView.viewPager.setCurrentItem(1,true)
        }
        var tabLayoutMediator =
            TabLayoutMediator(bindView.tab, bindView.viewPager) { tab, position ->
                tab.text = mTitles[position]
            }
        tabLayoutMediator.attach()

        bindView.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                positionPager = position
                //编辑状态复原
                isEdit = false
                isSelectAll = true
                showEditView(isEdit)
                //是否显示右上角按钮
                showRightView()
            }
        })
        bindView.viewPager.offscreenPageLimit = 3

        //重置会话列表未读数
        ChatDao.getConversationDb().resetNotifyConverMsgCount()
        //校正群组消息处理状态为已读
        ChatDao.getNotifyDb().checkGroupData()
    }

    private fun showRightView() {
        var data: MutableList<NotifyBean> = when (positionPager) {
            0 -> {
                ChatDao.getNotifyDb().getNotifySystemMsg()
            }

            1 -> {
                ChatDao.getNotifyDb().getNotifyMsgList(2)
            }

            2 -> {
                ChatDao.getNotifyDb().getNotifyMsgList(3)
            }

            else -> mutableListOf()
        }
        if (data != null && data.size > 0) {
            bindView.toolbar.ivAdd.visible()
        } else {
            bindView.toolbar.ivAdd.gone()
        }
    }

    override fun onResume() {
        super.onResume()
        //显示未读红点
        if (showUnreadRedView(mTitles[positionPager])) ivRedPoint?.visible() else ivRedPoint?.gone()
    }

    private fun showUnreadRedView(title: String): Boolean {
        var data: MutableList<NotifyBean> = when (title) {
            getString(R.string.系统) -> {
                ChatDao.getNotifyDb().getNotifySystemMsg()
            }

            getString(R.string.haoyou) -> {
                ChatDao.getNotifyDb().getNotifyMsgList(2)
            }

            getString(R.string.群组) -> {
                ChatDao.getNotifyDb().getNotifyMsgList(3)
            }

            else -> mutableListOf()
        }
        if (data != null && data.size > 0) {
            data.forEach { if (it.msgReadState == 0) return true }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun observeCallBack() {
        //删除通知消息
        mViewModel.deleteNotify.observe(this) { result ->
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
                    delNotifyMsg()
                    "--------000-type=${positionPager}".logE()
                    mFragments[positionPager].updateNotifyMsgList(positionPager)

                    saveConversationMsg()
                }
            }
        }

        /*接收ws消息后  更新界面*/
        LiveEventBus.get(EventKeys.UPDATE_NOTIFY_MSG, Boolean::class.java).observe(this) {
            //更新消息界面
            mFragments.forEachIndexed { index, notifyFragment ->
//                "--------111-type=${positionPager}".logE()
                notifyFragment.updateNotifyMsgList(index)
            }
            //重置会话列表未读数
            ChatDao.getConversationDb().resetNotifyConverMsgCount()
        }

        /*接收全部同意通知消息  更新界面*/
        LiveEventBus.get(EventKeys.OPERATE_NOTIFY_MSG, Boolean::class.java).observe(this) {
            //更新消息界面
            isEdit = false
            isSelectAll = true
            //编辑消息
            showEditView(isEdit)
        }

        LiveEventBus.get(EventKeys.UPDATE_CONVER).observe(this) {
            //更新
            val current = bindView.viewPager.currentItem
            if (current == 0) {
                if (showUnreadRedView(getString(R.string.系统))) ivRedPoint?.visible() else ivRedPoint?.gone()
            }
        }
    }

    /**
     * 更新会话数据
     */
    private fun saveConversationMsg() {
        //更新会话消息界面数据
        var notifyList = ChatDao.getNotifyDb().getNotifyMsg()
        if (notifyList != null && notifyList.size > 0) {
            //还有其他通知消息
            //显示最后一条通知消息
            var notify = notifyList[notifyList.size - 1]
            ChatDao.getConversationDb()
                .updateNotifyLastMsg(
                    msgType = notify.type,
                    content = ChatDao.getNotifyDb().getTypeTitle(notify.chatType),
                    msgTime = notify.createTime,
                    isUpdateNotifyMsg = true
                )
        } else {
            //没有任何通知消息
            ChatDao.getConversationDb()
                .updateNotifyLastMsg(
                    msgType = 0,
                    content = "",
                    msgTime = System.currentTimeMillis(),
                    isUpdateNotifyMsg = true
                )
        }
    }

    /**清空不同类型的本地通知消息*/
    private fun delNotifyMsg() {
        when (positionPager) {
            0 -> {
                //清空系统通知
                ChatDao.getNotifyDb().delAllNotifyMsg()
            }

            1 -> {
                //清空好友请求通知
                ChatDao.getNotifyDb().delAllNotifyMsg(2)
            }

            2 -> {
                //清空群请求通知
                ChatDao.getNotifyDb().delAllNotifyMsg(3)
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="dialog 弹框">
    private fun showDelDialog() {
        var notifyList: MutableList<NotifyBean>? = null
        var strHint = when (positionPager) {
            0 -> {
                notifyList = ChatDao.getNotifyDb().getNotifySystemMsg()
                getString(R.string.是否清空所有系统通知消息)
            }

            1 -> {
                notifyList = ChatDao.getNotifyDb().getNotifyMsgList(2)
                getString(R.string.是否清空所有好友通知消息)
            }

            2 -> {
                notifyList = ChatDao.getNotifyDb().getNotifyMsgList(3)
                getString(R.string.是否清空所有群组通知消息)
            }

            else -> ""
        }
        if (notifyList != null && notifyList.size > 0) {
            HintDialog(
                getString(R.string.zhuyi),
                strHint,
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        var notifyIdList = mutableListOf<String>()
                        notifyList?.forEach {
                            notifyIdList.add(it.id)
                        }
                        if (positionPager == 0) {
                            //删除系统通知
                            mViewModel.deleteSystemNotify(notifyIdList)
                        } else {
                            //删除群验证通知
                            mViewModel.deleteGroupNotify(notifyIdList)
                        }
                    }
                },
                R.drawable.ic_dialog_top
            ).show(supportFragmentManager, "HintDialog")
        } else {
            getString(R.string.已没有通知消息清空).toast()
        }
    }

    private fun getNotifyMsg(): MutableList<NotifyBean> {
        var notifyList = mutableListOf<NotifyBean>()
        when (positionPager) {
            0 -> {
                //获取系统通知
                notifyList = ChatDao.getNotifyDb().getNotifySystemMsg()
            }

            1 -> {
                //获取好友请求通知
                notifyList = ChatDao.getNotifyDb().getNotifyMsgList(2)
            }

            2 -> {
                //获取群请求通知
                notifyList = ChatDao.getNotifyDb().getNotifyMsgList(3)
            }
        }
        return notifyList
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="init viewPagerAdapter">
    class NotifyFragmentStateAdapter(
        context: FragmentActivity,
        private val mFragments: MutableList<NotifyFragment>
    ) : FragmentStateAdapter(context) {
        override fun getItemCount(): Int {
            return mFragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }
    }
    //</editor-fold>
}