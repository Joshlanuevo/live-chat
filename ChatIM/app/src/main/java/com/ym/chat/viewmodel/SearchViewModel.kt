package com.ym.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.rxLifeScope
import com.ym.base.mvvm.BaseViewModel
import com.ym.chat.bean.FriendInfoBean
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.rxhttp.UserRepository


class SearchViewModel : BaseViewModel() {
    //<editor-fold defaultstate="collapsed" desc="搜索指定的用户">
    val friendLiveData = MutableLiveData<LoadState<FriendInfoBean>>()
    fun searchFriend(account: String) {
        if (friendLiveData.value is LoadState.Loading) return
//        rxLifeScope.launch({
//            val result = UserRepository.searchFriend(account)
//            friendLiveData.value = LoadState.Success(data = result.data)
//        }, { e ->
//            friendLiveData.value = LoadState.Fail(exc = e)
//        }, {
//            friendLiveData.value = LoadState.Loading()
//        })
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="搜索指定群，获取信息">
    val getGroupInfo: MutableLiveData<LoadState<GroupInfoBean>> = MutableLiveData()


    fun getGroupInfo(isLoading: Boolean = true, gId: String) {
        if (getGroupInfo.value is LoadState.Loading) return
//        requestLifeLaunch({
//            val result = GroupChatSettingRepository.getGroupInfo(gId)
//            getGroupInfo.value = LoadState.Success(result, isRefresh = isLoading)
//        }, { e ->
//            getGroupInfo.value = LoadState.Fail(e, isLoading)
//            e.printStackTrace()
//            //e.message.toast()
//        }, { //开始回调，可以开启等待弹窗
//            getGroupInfo.value = LoadState.Loading(isLoading)
//        }, { //结束回调，可以销毁等待弹窗
//        })
    }
    //</editor-fold>

}