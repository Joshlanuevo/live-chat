package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.ym.chat.ext.roundLoad
import com.ym.base.widget.adapter.QuickNodeProviderVB
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.pressEffectBgColor
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.databinding.ItemFriendUserBinding
import com.ym.chat.ext.loadImg
import com.ym.chat.utils.Utils

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
class FriendUserItem(private val onItemClickListener: ((any: Any) -> Unit)? = null) :
    QuickNodeProviderVB<ItemFriendUserBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemFriendUserBinding.inflate(layoutInflater, parent, false)

    override val itemViewType: Int = 1

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        var view = helper.getViewBinding()
        view.layoutHeader.ivHeaderMark.gone()
        view.ivSystemNotify.gone()
        when (item) {
            is FriendListBean -> {
                view.layoutHeader.ivHeader.loadImg(item)
                view.tvName.text = item.nickname
                if(item.memberLevelCode == "System"){
                    view.ivSystemNotify.visible()
                }
                view.root.click { onItemClickListener?.invoke(item) }
                Utils.showDaShenImageView(
                    view.layoutHeader.ivHeaderMark, item?.displayHead == "Y", item?.levelHeadUrl
                )
            }
            is GroupInfoBean -> {
                view.layoutHeader.ivHeader.loadImg(
                    item.headUrl,
                    item.name,
                    R.drawable.ic_mine_header_group,
                    true
                )
                view.tvName.text = item.name
                view.root.click { onItemClickListener?.invoke(item) }
            }
        }
    }
}