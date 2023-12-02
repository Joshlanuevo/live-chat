package com.ym.chat.item

import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.dp2Px
import com.ym.chat.bean.TxtSimpleBean
import com.ym.chat.databinding.ItemTxtSimpleBinding


class TxtSimpleItem : QuickViewBindingItemBinder<TxtSimpleBean, ItemTxtSimpleBinding>() {
    //<editor-fold defaultstate="collapsed" desc="XML">
    override fun onCreateViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int) = ItemTxtSimpleBinding.inflate(layoutInflater, parent, false)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="数据绑定">
    override fun convert(holder: BinderVBHolder<ItemTxtSimpleBinding>, data: TxtSimpleBean) {
        holder.viewBinding.root.let { tv ->
            tv.text = data.txt
            tv.setTextColor(data.txtColor)
            tv.layoutParams?.height = data.heightDp.dp2Px()
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, data.textSizeSp)
            tv.gravity = data.gravity
            tv.typeface = Typeface.defaultFromStyle(if (data.bold) Typeface.BOLD else Typeface.NORMAL)
            tv.setBackgroundColor(data.bgColor)
            tv.setPadding(data.paddingStartPx, data.paddingTopPx, data.paddingEndPx, data.paddingBottomPx)
        }
    }
    //</editor-fold>
}