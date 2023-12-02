package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.databinding.ItemSearchFriendGroupBinding
import com.ym.chat.ext.loadImg
import com.ym.chat.ext.setColorAndString
import com.ym.chat.utils.Utils

/**
 * 搜索好友
 */
class SearchFriendItem(val onItemClickListener: ((memberBean: FriendListBean) -> Unit)? = null) :
    QuickViewBindingItemBinder<FriendListBean, ItemSearchFriendGroupBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemSearchFriendGroupBinding.inflate(layoutInflater, parent, false)


    override fun convert(
        holder: BinderVBHolder<ItemSearchFriendGroupBinding>,
        data: FriendListBean
    ) {
        holder.viewBinding.let {
            it.layoutHeader.ivHeader.loadImg(data)
            Utils.showDaShenImageView(
                it.layoutHeader.ivHeaderMark, data?.displayHead == "Y", data?.levelHeadUrl
            )
            it.tvName.text = data.nickname.setColorAndString(
                colorId = context.getColor(R.color.color_main),
                str = data.searchStr
            )
//            var name = if (data.id == data.searchToSendMemberId) "${data.name}:" else "我:"
            var name = ""
            it.tvMsg.text = if (data.isSearch)
                "$name${data.searchContent}".setColorAndString(
                    colorId = context.getColor(R.color.color_main),
                    str = data.searchStr,
                    strFront = name
                )
                else
                "${context.getString(R.string.zhanghao)}：${data.username}".setColorAndString(
                    colorId = context.getColor(R.color.color_main),
                    str = data.searchStr,
                    strFront = "${context.getString(R.string.zhanghao)}："
                )
            it.root.click { onItemClickListener?.invoke(data) }
        }
    }
}