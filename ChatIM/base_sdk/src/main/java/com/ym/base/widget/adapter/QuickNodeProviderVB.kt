package com.ym.base.widget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *  对BaseNodeProvider的优化，快速实现对viewBing的开发
 *  继承于BaseNodeProvider，现在不推荐使用XXXProvider系列多布局
 *  推荐使用 @link com.ym.base.widget.adapter.BaseNodeBinderAdapter
 *  XXXBinder系列多布局样式更加简洁和 无需定义 viewType
 *
 * */
abstract class QuickNodeProviderVB<VB : ViewBinding> : BaseNodeProvider() {
    /**
     * 此 Holder 不适用于其他 BaseAdapter，仅针对[BaseNodeAdapter]
     */
    class BinderVBHolder<VB : ViewBinding>(val viewBinding: VB) : BaseViewHolder(viewBinding.root)

    override val layoutId: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BinderVBHolder(onCreateViewBinding(LayoutInflater.from(parent.context), parent, viewType))
    }

    abstract fun onCreateViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB

    /**扩展 -- 针对当前子类的作用域范围的免去多次 as 强转的无用代码 */
    protected fun BaseViewHolder?.getViewBinding(): VB = (this as BinderVBHolder<VB>).viewBinding
}
