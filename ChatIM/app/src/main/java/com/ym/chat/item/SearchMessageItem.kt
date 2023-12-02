package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.widget.ext.click
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.databinding.ItemSearchMessageBinding
import com.ym.chat.ext.loadImg

/**
 * 搜索群历史记录
 */
class SearchMessageItem(val onItemClickListener: ((memberBean: GroupMemberBean) -> Unit)? = null) :
    QuickViewBindingItemBinder<GroupMemberBean, ItemSearchMessageBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemSearchMessageBinding.inflate(layoutInflater, parent, false)


    override fun convert(holder: BinderVBHolder<ItemSearchMessageBinding>, data: GroupMemberBean) {
        holder.viewBinding.let {
            it.layoutHeader.ivHeader.loadImg(data)
            it.tvName.text = data.nickname
            it.root.click { onItemClickListener?.invoke(data) }
        }
    }
}