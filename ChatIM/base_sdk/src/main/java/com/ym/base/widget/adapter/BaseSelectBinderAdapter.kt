package com.ym.base.widget.adapter

import android.util.SparseArray
import androidx.annotation.IntRange
import androidx.core.util.size

/**具有单选多选,功能的适配器,和 BaseViewHolder 相结合完成对应功能*/
open class BaseSelectBinderAdapter(list: MutableList<Any>? = null, @IntRange(from = 0) maxSelect: Int = 0) : BaseBinderAdapterPro(list) {
    //<editor-fold defaultstate="collapsed" desc="被记录的和选择相关的参数">
    /**当前选择的位置--- 单选专用*/
    var selectPosition = -1

    /**当前选择的item--- 单选专用*/
    var selectItem: Any? = null

    /**当前选择的位置--- 多选专用*/
    val selectList: SparseArray<Any> = SparseArray()

    /**当前为 单选模式还是多选模式的判断值*/
    private var maxSelect = maxSelect
        set(value) {
            selectList.clear()
            if (value == 0) selectPosition = -1
            field = value
        }

    //</editor-fold>
    /**
     * 点击选中并刷新的逻辑请调用这个方法,会判断是否可以选中并过滤快递点击
     *  isSameItemCheck 单选的是否响应同一个item
     */
    fun setItemChecked(mNewChecked: Int, isSameItemCheck: Boolean = false): Boolean {
        //if (isFastClickNoDiZeng(50)) {
        //    return false
        //}
        when (maxSelect) {
            0 -> {
                if (selectPosition == mNewChecked && !isSameItemCheck) {
                    return false
                }
                if (selectPosition != -1 && selectPosition != mNewChecked) {
                    notifyItemChanged(selectPosition, false)
                }
                selectItem = data[mNewChecked]
                selectPosition = mNewChecked
                notifyItemChanged(mNewChecked, true)
                return true
            }
            else -> {
                return if (selectList.get(mNewChecked) == null) {
                    //之前未存储则进行存储,并执行存储器判断
                    if (selectList.size >= maxSelect) false else {
                        //存储未到上限,则可以继续选中并刷新
                        selectList.append(mNewChecked, data[mNewChecked])
                        notifyItemChanged(mNewChecked, true)
                        true
                    }
                } else {
                    selectList.remove(mNewChecked)
                    notifyItemChanged(mNewChecked, false)
                    true
                }
            }
        }
    }

    /**当第一次初始化适配器,并且想选中部分的时候可以通过此方法设置,请先设置 数据源*/
    fun setInitSelect(vararg initSelect: Int) {
        if (data.isEmpty()) return
        when (maxSelect) {
            0 -> {
                selectPosition = initSelect[0]
                if (selectPosition > 0) selectItem = data[selectPosition]

            }
            else -> repeat(initSelect.count().coerceAtMost(maxSelect)) {
                //对于传入的参数必须判断是否合规.比如传入的参数个数是否超过最大上限,参数是否大于数据集合的长度
                if (initSelect[it] < data.size && initSelect[it] > 0) {
                    selectList.append(initSelect[it], data[initSelect[it]])
                }
            }
        }
    }

    /**本方法用于ViewBinder来判断,是否是在初始化的时候默认选中item */
    fun isSelected(position: Int, item: Any): Boolean {
        return when (maxSelect) {
            0 -> if (selectPosition != position) false else {
                selectItem = item
                true
            }
            else -> if ((selectList.get(position) == null)) false else {
                selectList.put(position, item)
                true
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="快速点击处理">
    private var lastClickTime: Long = 0 //上次点击的时间
    fun isFastClick(spaceTime: Int): Boolean {
        val currentTime = System.currentTimeMillis() //当前系统时间
        val isFastClick: Boolean //是否允许点击
        isFastClick = currentTime - lastClickTime <= spaceTime
        lastClickTime = currentTime
        return isFastClick
    }

    fun isFastClickNoDiZeng(spaceTime: Int): Boolean {
        val currentTime = System.currentTimeMillis() //当前系统时间
        val isFastClick: Boolean //是否允许点击
        isFastClick = currentTime - lastClickTime <= spaceTime
        if (!isFastClick) {
            lastClickTime = currentTime
        }
        return isFastClick
    }
    //</editor-fold>

}
