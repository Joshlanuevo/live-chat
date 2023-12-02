package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.bean.AccountBean
import com.ym.chat.databinding.ItemAccountListBinding
import com.ym.chat.ext.loadImg
import com.ym.chat.utils.Utils

/**
 * 登录过用户的数据
 */
class AccountItem(val onItemClickListener: ((memberBean: AccountBean) -> Unit)? = null,
                  val onDelItemClickListener: ((memberBean: AccountBean) -> Unit)? = null):
    QuickViewBindingItemBinder<AccountBean, ItemAccountListBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemAccountListBinding.inflate(layoutInflater, parent, false)


    override fun convert(holder: BinderVBHolder<ItemAccountListBinding>, data: AccountBean) {
        holder.viewBinding.let {
            if (data.isEdit) {
                it.ivSelect.visible()
            } else {
                it.root.click { onItemClickListener?.invoke(data) }
                it.ivSelect.gone()
            }
            if (data.isSelect) {
                it.ivAccCurrentUser.visible()
            } else {
                it.ivAccCurrentUser.gone()
            }
            Utils.showDaShenImageView(
                it.layoutHeader.ivHeaderMark, data?.displayHead == "Y", data?.levelHeadUrl
            )
            it.layoutHeader.ivHeader.loadImg(data.headUrl,data.remark,data.name,data.showUsername())
            it.tvName.text = data.name
            it.tvPhone.text = data.username

            it.ivSelect.click { //处理删除操作
                onDelItemClickListener?.invoke(data)
            }
        }
    }
}