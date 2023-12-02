package com.ym.chat.rxhttp

import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.CollectBean
import com.ym.chat.bean.SimpleBean
import rxhttp.RxHttp
import rxhttp.toOtherJson
import rxhttp.wrapper.cahce.CacheMode

object CollectRepository : BaseRepository() {
    /**
     * 获取收藏列表
     */
    suspend fun getCollectList(curPage: String, pageSize: String = "20"): BaseBean<CollectBean> {
        return RxHttp.get(ApiUrl.Chat.collectList)
            .add("curPage", curPage)
            .add("pageSize", pageSize)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<CollectBean>>()
            .await()
    }

    /**
     * 批量删除 收藏
     */
    suspend fun delCollectList(favoriteIdList: MutableList<String>): BaseBean<CollectBean> {
        return RxHttp.deleteBody(ApiUrl.Chat.delCollect)
            .setBody(favoriteIdList)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<CollectBean>>()
            .await()
    }

    /**
     * 批量编辑 收藏
     */
    suspend fun putCollectContent(content:String,favoriteIdList: MutableList<String>): BaseBean<SimpleBean> {
        return RxHttp.putJson(ApiUrl.Chat.putCollectContent)
            .add("content", content)
            .add("favoriteIdList", favoriteIdList)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<SimpleBean>>()
            .await()
    }
}