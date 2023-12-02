package com.ym.chat.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ym.base.widget.adapter.QuickNodeProviderVB
import com.ym.chat.R
import com.ym.chat.bean.FriendGroupNode
import com.ym.chat.databinding.ItemFriendGroupBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/21 16:50
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class FriendGroupItem : QuickNodeProviderVB<ItemFriendGroupBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemFriendGroupBinding.inflate(layoutInflater, parent, false)

    override val itemViewType: Int = 0

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        var view = helper.getViewBinding()
        item as FriendGroupNode
        if (item.isExpanded) {
            //展开
            view.ivArrow.setImageResource(R.drawable.ic_group_down)
        } else {
            //关闭
            view.ivArrow.setImageResource(R.drawable.ic_group_top)
        }
        when (item.type) {
            3 -> {
                view.ivIcon.setImageResource(R.drawable.ic_friend_icon)
            }
            2 -> {
                view.ivIcon.setImageResource(R.drawable.ic_group_icon_1)
            }
            else -> {
                view.ivIcon.setImageResource(R.drawable.ic_group_icon)
            }
        }
        view.tvGroupName.text = item.title + "(${item.size})"

    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        getAdapter()!!.expandOrCollapse(position)
    }
}