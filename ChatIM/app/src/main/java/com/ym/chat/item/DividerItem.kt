package com.ym.chat.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.bean.DividerBean
import com.ym.chat.databinding.ItemDividerBinding


class DividerItem : QuickViewBindingItemBinder<DividerBean, ItemDividerBinding>() {
    //<editor-fold defaultstate="collapsed" desc="XML">
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemDividerBinding.inflate(layoutInflater, parent, false)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="数据填充">
    override fun convert(holder: BinderVBHolder<ItemDividerBinding>, data: DividerBean) {
        holder.itemView.layoutParams?.height = data.heightPx
        (holder.itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.let { p ->
            p.marginStart = data.marginStart
            p.marginEnd = data.marginEnd
        }
        holder.itemView.setBackgroundColor(data.bgColor)
        if (data.isShow) {
            holder.itemView.visible()
        } else {
            holder.itemView.gone()
        }

    }
    //</editor-fold>
}