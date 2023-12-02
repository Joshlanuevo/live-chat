package com.ym.chat.rxhttp

import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.SendGroupMsgBean
import rxhttp.RxHttp
import rxhttp.toOtherJson
import rxhttp.wrapper.cahce.CacheMode

object SendGroupMsgRepository : BaseRepository() {

    /**
     * 获取群发消息列表
     */
    suspend fun getSendGroupMsg(
        curPage: String,
        pageSize: String = "20"
    ): BaseBean<SendGroupMsgBean> {
        return RxHttp.get(ApiUrl.Chat.getSendGroupMsg)
            .add("curPage", curPage)
            .add("pageSize", pageSize)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<SendGroupMsgBean>>()
            .await()
    }

    /**
     * 获取群发消息列表
     */
    suspend fun delSendGroupMsg(): BaseBean<Boolean> {
        return RxHttp.putJson(ApiUrl.Chat.delSendGroupMsg)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<Boolean>>()
            .await()
    }
}