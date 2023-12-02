package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.databinding.ItemSearchFriendGroupBinding
import com.ym.chat.databinding.ItemSearchGroupBinding
import com.ym.chat.databinding.ItemSearchMessageBinding
import com.ym.chat.ext.roundLoad
import com.ym.chat.ext.setColorAndString
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener

/**
 * 搜索群组
 */
class SearchGroupItem(val onItemClickListener: ((memberBean: GroupInfoBean) -> Unit)? = null) :
    QuickViewBindingItemBinder<GroupInfoBean, ItemSearchGroupBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemSearchGroupBinding.inflate(layoutInflater, parent, false)


    override fun convert(
        holder: BinderVBHolder<ItemSearchGroupBinding>,
        data: GroupInfoBean
    ) {
        holder.viewBinding.let {
            val strContent = AtUserHelper.parseAtUserLinkJx(data.searchContent,
                ContextCompat.getColor(context, R.color.color_at),object :
                    AtUserLinkOnClickListener{
                    override fun ulrLinkClick(str: String?) {
                    }

                    override fun atUserClick(str: String?) {
                    }

                    override fun phoneClick(str: String?) {
                    }
                }).toString()
            it.layoutHeader.ivHeader.roundLoad(data.headUrl, R.drawable.ic_mine_header_group)
            if (data.isSearch) {
                it.tvMsg.visible()
                it.tvMsg.text = strContent.setColorAndString(
                    colorId = context.getColor(R.color.color_main),
                    str = data.searchStr
                )
                it.tvName.text = data.name
            } else {
                it.tvMsg.gone()
                it.tvName.text = data.name.setColorAndString(
                    colorId = context.getColor(R.color.color_main),
                    str = data.searchStr
                )
                it.tvMsg.text = ""
            }
            it.root.click { onItemClickListener?.invoke(data) }
        }
    }
}