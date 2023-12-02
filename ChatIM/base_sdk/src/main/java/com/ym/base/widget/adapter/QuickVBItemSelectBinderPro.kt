package com.ym.base.widget.adapter

import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.CanUseString
import com.ym.base.widget.ext.click

/**
 * Binder 的基类
 */
abstract class QuickVBItemSelectBinderPro<T, VB : ViewBinding>(
    var onItemClick: ((data: T, position: Int, view: View) -> Unit)? = null,
    var onItemLongClick: ((data: T, position: Int, view: View) -> Boolean)? = null,
) : QuickVBItemBinderPro<T, VB>() {

    /**选中之类的UI请重写这两个方法,然后,默认会调用,*/
    open fun onSelectTrue(holder: BinderVBHolder<VB>, data: T, @CanUseString("初始化适配器需要默认选中部分item则为true") isInit: Boolean) {}

    open fun onSelectFalse(holder: BinderVBHolder<VB>, data: T, @CanUseString("初始化适配器需要默认选中部分item则为true") isInit: Boolean) {}

    /**
     * 在此处对设置item数据
     * @param holder VH
     * @param data T
     */
    @CallSuper
    override fun convert(holder: BinderVBHolder<VB>, data: T) {
        //第一次初始化适配器,会这里,因为没有payloads,
        holder.itemView.click {
            var position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@click
            }
            position -= adapter.headerLayoutCount
            onItemClick?.invoke(data, position, it)
        }
        holder.itemView.setOnLongClickListener {
            var position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnLongClickListener false
            }
            position -= adapter.headerLayoutCount
            onItemLongClick?.invoke(data, position, it) ?: false
        }
        //第一次初始化适配器,我们要判断是否需要预先选中部分item
        (adapter as? BaseSelectBinderAdapter)?.let {
            if (it.isSelected(holder.getPositionReal(), data as Any)) {
                onSelectTrue(holder, data, true)
            } else {
                onSelectFalse(holder, data, true)
            }
        }
    }


    /**
     * 使用局部刷新时候，会调用此方法
     * @param holder VH
     * @param data T
     * @param payloads List<Any>
     */
    override fun convert(holder: BinderVBHolder<VB>, data: T, payloads: List<Any>) {
        when (payloads[0] as Boolean) {
            true -> onSelectTrue(holder, data, false)
            false -> onSelectFalse(holder, data, false)
        }
    }

}