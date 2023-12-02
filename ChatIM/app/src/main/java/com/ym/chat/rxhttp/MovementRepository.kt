package com.ym.chat.rxhttp

import com.ym.chat.bean.BaseBean
import com.ym.chat.bean.CollectBean
import rxhttp.RxHttp
import rxhttp.toOtherJson
import rxhttp.toStr
import rxhttp.wrapper.cahce.CacheMode

object MovementRepository : BaseRepository() {

    /**
     * 获取发现地址
     */
    suspend fun getFindUrl(): BaseBean<String> {
        return RxHttp.get(ApiUrl.User.getDiscover)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }
}