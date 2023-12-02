package com.ym.chat.rxhttp

import com.ym.chat.bean.AddEmojResultBean
import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.EmojListBean
import rxhttp.RxHttp
import rxhttp.toOtherJson
import rxhttp.wrapper.cahce.CacheMode

object MeExpressionRepository : BaseRepository() {
    /**
     * 获取gif表情列表
     */
    suspend fun getEmojList(
        curPage: String = "1",
        pageSize: String = "100"
    ): BaseBean<EmojListBean> {
        return RxHttp.get(ApiUrl.Chat.emojList)
            .add("curPage", curPage)
            .add("pageSize", pageSize)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<EmojListBean>>()
            .await()
    }

    /**
     * 新增gif表情
     */
    suspend fun addEmojGIf(
        url: String,
        thumbnail:String,
        width: Int,
        height: Int
    ): BaseBean<AddEmojResultBean> {
        return RxHttp.postJson(ApiUrl.Chat.addEmoj)
            .add("url", url)
            .add("thumbnail", thumbnail)
            .add("width", width)
            .add("height", height)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<AddEmojResultBean>>()
            .await()
    }

    /**
     * 删除gif表情
     */
    suspend fun delEmoj(ids: MutableList<String>): BaseBean<String> {
        return RxHttp.deleteBody(ApiUrl.Chat.delEmoj)
            .setBody(ids)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }
}