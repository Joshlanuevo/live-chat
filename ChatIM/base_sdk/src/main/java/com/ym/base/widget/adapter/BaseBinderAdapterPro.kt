package com.ym.base.widget.adapter

import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 使用 Binder 来实现adapter，既可以实现单布局，也能实现多布局
 * 数据实体类也不存继承问题
 *
 * 当有多种条目的时候，避免在convert()中做太多的业务逻辑，把逻辑放在对应的 BaseItemBinderProLin 中。
 * 适用于以下情况：
 * 1、实体类不方便扩展，此Adapter的数据类型可以是任意类型，默认情况下不需要实现 getItemType
 * 2、item 类型较多，在convert()中管理起来复杂
 *
 * ViewHolder 由 [BaseItemBinderPro] 实现，并且每个[BaseItemBinderPro]可以拥有自己类型的ViewHolder类型。
 *
 * 数据类型为Any
 */
open class BaseBinderAdapterPro(list: MutableList<Any>? = null) : BaseQuickAdapter<Any, BaseViewHolder>(0, list) {

    /**
     * 用于存储每个 Binder 类型对应的 Diff
     */
    private val classDiffMap = HashMap<Class<*>, DiffUtil.ItemCallback<Any>?>()

    private val mTypeMap = HashMap<Class<*>, Int>()
    private val mBinderArray = SparseArray<BaseItemBinderPro<Any, *>>()

    init {
        setDiffCallback(ItemCallback())
    }

    /**
     * 添加 ItemBinder
     */
    @JvmOverloads
    fun <T : Any> addItemBinder(clazz: Class<out T>, baseItemBinderPro: BaseItemBinderPro<T, *>, callback: DiffUtil.ItemCallback<T>? = null): BaseBinderAdapterPro {
        val itemType = mTypeMap.size + 1
        mTypeMap[clazz] = itemType
        mBinderArray.append(itemType, baseItemBinderPro as BaseItemBinderPro<Any, *>)
        baseItemBinderPro._adapter = this
        callback?.let {
            classDiffMap[clazz] = it as DiffUtil.ItemCallback<Any>
        }
        return this
    }

    /**
     * 添加 ItemBinder
     */
    @JvmOverloads
    fun <T : Any> addItemBinderMany(baseItemBinderPro: BaseItemBinderPro<T, *>, callback: DiffUtil.ItemCallback<T>? = null): BaseBinderAdapterPro {
        val itemType = mTypeMap.size + 1
        mTypeMap[baseItemBinderPro.javaClass] = itemType
        mBinderArray.append(itemType, baseItemBinderPro as BaseItemBinderPro<Any, *>)
        baseItemBinderPro._adapter = this
        callback?.let {
            classDiffMap[baseItemBinderPro.javaClass] = it as DiffUtil.ItemCallback<Any>
        }
        return this
    }

    /**
     * kotlin 可以使用如下方法添加 ItemBinder，更加简单
     */
    inline fun <reified T : Any> addItemBinder(baseItemBinderPro: BaseItemBinderPro<T, *>, callback: DiffUtil.ItemCallback<T>? = null): BaseBinderAdapterPro {
        addItemBinder(T::class.java, baseItemBinderPro, callback)
        return this
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return getItemBinder(viewType).let {
            it._context = context
            it.onCreateViewHolder(parent,viewType)
        }
    }

    override fun convert(holder: BaseViewHolder, item: Any) {
        getItemBinder(holder.itemViewType).convert(holder, item)
    }

    override fun convert(holder: BaseViewHolder, item: Any, payloads: List<Any>) {
        getItemBinder(holder.itemViewType).convert(holder, item, payloads)
    }

    open fun getItemBinder(viewType: Int): BaseItemBinderPro<Any, BaseViewHolder> {
        val binder = mBinderArray[viewType]
        checkNotNull(binder) { "getItemBinder: viewType '$viewType' no such Binder found，please use addItemBinder() first!" }
        return binder as BaseItemBinderPro<Any, BaseViewHolder>
    }

    open fun getItemBinderOrNull(viewType: Int): BaseItemBinderPro<Any, BaseViewHolder>? {
        val binder = mBinderArray[viewType]
        return binder as? BaseItemBinderPro<Any, BaseViewHolder>
    }

    override fun getDefItemViewType(position: Int): Int {
        val clazz = (data[position] as? Types)?.clazz ?: data[position].javaClass
        return findViewType(clazz)
    }

    override fun bindViewClickListener(viewHolder: BaseViewHolder, viewType: Int) {
        super.bindViewClickListener(viewHolder, viewType)
        bindClick(viewHolder)
        bindChildClick(viewHolder, viewType)
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder) {
        super.onViewAttachedToWindow(holder)
        getItemBinderOrNull(holder.itemViewType)?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        getItemBinderOrNull(holder.itemViewType)?.onViewDetachedFromWindow(holder)

    }

    override fun onFailedToRecycleView(holder: BaseViewHolder): Boolean {
        return getItemBinderOrNull(holder.itemViewType)?.onFailedToRecycleView(holder) ?: false
    }

    protected fun findViewType(clazz: Class<*>): Int {
        val type = mTypeMap[clazz]
        checkNotNull(type) { "findViewType: ViewType: $clazz Not Find!" }
        return type
    }

    protected open fun bindClick(viewHolder: BaseViewHolder) {
        //如果没有设置点击监听，则回调给 itemProvider
        //Callback to itemProvider if no click listener is set
        viewHolder.itemView.setOnClickListener {
            var position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            position -= headerLayoutCount

            val itemViewType = viewHolder.itemViewType
            val binder = getItemBinder(itemViewType)

            if (getOnItemClickListener() == null) {
                binder.onClick(viewHolder, it, data[position], position)
            } else {
                setOnItemClick(it,position)
            }
        }
        //如果没有设置长按监听，则回调给itemProvider
        // If you do not set a long press listener, callback to the itemProvider
        viewHolder.itemView.setOnLongClickListener {
            var position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnLongClickListener false
            }
            position -= headerLayoutCount

            val itemViewType = viewHolder.itemViewType
            val binder = getItemBinder(itemViewType)
            if (getOnItemLongClickListener() == null) {
                binder.onLongClick(viewHolder, it, data[position], position)
            } else {
                setOnItemLongClick(it, position)
            }
        }
    }

    protected open fun bindChildClick(viewHolder: BaseViewHolder, viewType: Int) {
        val provider = getItemBinder(viewType)
        val ids = provider.getChildClickViewIds()
        ids.forEach { id ->
            viewHolder.itemView.findViewById<View>(id)?.let {
                if (!it.isClickable) {
                    it.isClickable = true
                }
                it.setOnClickListener { v ->
                    var position: Int = viewHolder.adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@setOnClickListener
                    }
                    position -= headerLayoutCount
                    if (getOnItemChildClickListener() == null) {
                        provider.onChildClick(viewHolder, v, data[position], position)
                    } else {
                        setOnItemChildClick(v, position)
                    }
                }
            }
        }

        val ids2 = provider.getChildLongClickViewIds()
        ids2.forEach { id ->
            viewHolder.itemView.findViewById<View>(id)?.let {
                if (!it.isLongClickable) {
                    it.isLongClickable = true
                }
                it.setOnLongClickListener { v ->
                    var position: Int = viewHolder.adapterPosition
                    if (position == RecyclerView.NO_POSITION) {
                        return@setOnLongClickListener false
                    }
                    position -= headerLayoutCount
                    if (getOnItemChildLongClickListener() == null) {
                        provider.onChildLongClick(viewHolder, v, data[position], position)
                    } else {
                        setOnItemChildLongClick(v, position)
                    }
                }
            }
        }

    }


    /**
     * Diff Callback
     */
    private inner class ItemCallback : DiffUtil.ItemCallback<Any>() {

        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem.javaClass == newItem.javaClass) {
                classDiffMap[oldItem.javaClass]?.let {
                    return it.areItemsTheSame(oldItem, newItem)
                }
            }

            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem.javaClass == newItem.javaClass) {
                classDiffMap[oldItem.javaClass]?.let {
                    return it.areContentsTheSame(oldItem, newItem)
                }
            }

            return true
        }

        override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
            if (oldItem.javaClass == newItem.javaClass) {
                return classDiffMap[oldItem.javaClass]?.getChangePayload(oldItem, newItem)
            }
            return null
        }
    }

    interface Types {
        var clazz: Class<out BaseItemBinderPro<*,*>>?
    }
}
