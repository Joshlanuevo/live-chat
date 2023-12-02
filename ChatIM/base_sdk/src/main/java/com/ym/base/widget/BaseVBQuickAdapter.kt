package com.ym.base.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import java.lang.reflect.ParameterizedType

/**
 * ViewBinding改造后的QuickAdapter
 */
abstract class BaseVBQuickAdapter<T, VB : ViewBinding> :
    BaseQuickAdapter<T, BaseVBHolder<VB>>(0, null) {
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseVBHolder<VB> {
        val vbClass: Class<VB> =
            (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VB>
        val inflate = vbClass.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )

        val mBinding =
            inflate.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB

        return BaseVBHolder(
            mBinding
        )
    }
}