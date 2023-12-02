package com.ym.base.widget

import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.viewholder.BaseViewHolder

open class BaseVBHolder<VB : ViewBinding>(val viewBinding: VB) : BaseViewHolder(viewBinding.root)