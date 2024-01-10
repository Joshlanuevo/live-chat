package com.ym.chat.ui.fragment

import android.content.Intent
import android.view.Gravity
import coil.load
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.adapter.FriendAdapter
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupActionBean
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.databinding.FragmentContactBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.item.FriendGroupItem
import com.ym.chat.item.FriendUserItem
import com.ym.chat.popup.HomeAddPopupWindow
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.SearchFriendActivity
import com.ym.chat.utils.ImCache
import com.ym.chat.viewmodel.FriendViewModel

/**
 * 联系人Fragment
 */
class ContactFragment : LoadingFragment(R.layout.fragment_contact) {
    private val bindView: FragmentContactBinding by binding()
    private val mAdapter = FriendAdapter()
    private val mViewModel = FriendViewModel()

    override fun initView() {
//        val headerBinding = ViewHeaderFriendBinding.inflate(layoutInflater)
//        mAdapter.addHeaderView(headerBinding.root)
//        mAdapter.headerWithEmptyEnable = true
//        headerBinding.llAddFriend.click {
//            //添加好友
//            startActivity(Intent(activity, SearchFriendActivity::class.java))
//        }
        bindView.ivLogo.load(R.mipmap.ic_launcher_xy)
        mAdapter.addFullSpanNodeProvider(FriendGroupItem())
        mAdapter.addNodeProvider(FriendUserItem(onItemClickListener = {
            if (it is FriendListBean) {

                activity?.let { it1 ->
                    ChatActivity.start(it1, it.id, 0, chatName = it.name, chatHeader = it.headUrl)
                }
            } else if (it is GroupInfoBean) {
                activity?.let { it1 ->
                    ChatActivity.start(
                        it1,
                        it.id,
                        1,
                        chatName = it.name,
                        chatHeader = it.headUrl
                    )
                }
            }
        }))
        bindView.listFriend.adapter = mAdapter
        bindView.refreshLayout.setEnableLoadMore(false)
        //下拉刷新
        bindView.refreshLayout.setOnRefreshListener {
            ChatDao.syncFriendAndGroupToLocal()
        }

        bindView.tvSearch.click {
            startActivity(
                Intent(requireContext(), SearchFriendActivity::class.java)
                    .putExtra(SearchFriendActivity.SEARCHTYPE, 4)
            )
        }
        bindView.ivAdd.click {
            val popupWindow = HomeAddPopupWindow(requireContext())
            popupWindow.showPopup(bindView.ivAdd, 200, 4, Gravity.BOTTOM)
        }
    }

    override fun requestData() {
        showLoading()
        mViewModel.getList()
    }

    override fun observeCallBack() {
        mViewModel.showData.observe(this) { result ->
            bindView.refreshLayout.finishRefresh()
            hideLoading()
            mAdapter.setList(result)
        }
        /*刷新数据回调*/
        LiveEventBus.get(EventKeys.REFRESH_FRIEND_GROUP, Boolean::class.java).observe(this) {
            //刷新数据返回
            bindView.refreshLayout.finishRefresh()
            hideLoading()
            mViewModel.getList()
        }

        /*接收修改群名 群头像广播*/
        LiveEventBus.get(EventKeys.EDIT_GROUP_INFO, GroupInfoBean::class.java).observe(this) {
            //刷新数据操作，本地数据库已对，群昵称，头像，公告修改进行更新
            mViewModel.getList()
        }

        /*接收添加好友 创建群组广播，主动添加别人好友*/
        LiveEventBus.get(EventKeys.ADD_FRIEND, Boolean::class.java).observe(this) {
            //刷新数据操作
            if (ImCache.isUpdateNotifyMsg)
                ChatDao.syncFriendAndGroupToLocal(
                    isSyncFriend = true,
                    isSyncGroup = false,
                    isEventUpdateConver = false
                ) {
                    mViewModel.getList()
                }
        }

        /*后台解散群 广播*/
        LiveEventBus.get(EventKeys.DELETE_GROUP, String::class.java).observe(this) {
            //刷新数据操作
            if (ImCache.isUpdateNotifyMsg)
                ChatDao.syncFriendAndGroupToLocal(
                    isSyncFriend = false,
                    isEventUpdateConver = false
                ) {
                    mViewModel.getList()
                }
        }

        /*直接从本地数据库，刷新列表数据，被添加好友，设置/取消管理员，群头像更新，群昵称更新，退出群聊，被踢出群等ws事件，已更新本地数据库*/
        LiveEventBus.get(EventKeys.EVENT_REFRESH_CONTACT_LOCAL, Boolean::class.java).observe(this) {
            //刷新数据操作,本地数据库已更新
            mViewModel.getList()
        }

        /*群成员退出群 群组解散群 广播*/
        LiveEventBus.get(EventKeys.EXIT_GROUP, Boolean::class.java).observe(this) {
            //刷新数据操作，本地数据库已更新
            mViewModel.getList()
        }

        /**收到群设置 相关参数 的推送消息 广播*/
        LiveEventBus.get(EventKeys.GROUP_ACTION, GroupActionBean::class.java)
            .observe(this) { groupAction ->
                //刷新数据操作，自己被设置或取消管理员，本地数据库已更新
                if (ImCache.isUpdateNotifyMsg)
                    ChatDao.syncFriendAndGroupToLocal(
                        isSyncFriend = false
                    ) {
                        mViewModel.getList()
                    }
            }
    }
}