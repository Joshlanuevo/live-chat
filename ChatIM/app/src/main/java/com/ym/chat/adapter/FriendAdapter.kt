package com.ym.chat.adapter

import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.ym.chat.bean.FriendGroupNode
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.FriendUserNode
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.item.FriendGroupItem
import com.ym.chat.item.FriendUserItem

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/21 16:48
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 好友列表adapter
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class FriendAdapter : BaseNodeAdapter() {

//    init {
//        //初始化显示的item类型
//        addFullSpanNodeProvider(FriendGroupItem())
//        addNodeProvider(FriendUserItem())
//    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        val node = data[position]
        return when (node) {
            is FriendGroupNode -> 0
            is FriendUserNode -> 1
            is FriendListBean ->1
            is GroupInfoBean ->1
            else -> {
                -1
            }
        }
    }
}