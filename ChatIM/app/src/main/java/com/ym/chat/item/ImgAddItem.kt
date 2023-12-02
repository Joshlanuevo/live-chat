package com.ym.chat.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.dp2Px
import com.ym.chat.bean.ImgAddBean
import com.ym.chat.databinding.ItemImgAddBinding
import com.ym.chat.utils.DeviceInfoUtils

/**
 * 收藏时间显示
 * @Description
 * @Author：
 * @Date：时间线显示
 * @Time：16:33
 */
class ImgAddItem(val onItemAddClickListener: ((rb: ImgAddBean) -> Unit)? = null) :
    QuickViewBindingItemBinder<ImgAddBean, ItemImgAddBinding>() {

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemImgAddBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemImgAddBinding>, data: ImgAddBean) {
        //显示时间
        //       holder.viewBinding.tvDate.text = TimeUtils.getCollectDateToChinaStr(data.createTime)
        val width = DeviceInfoUtils.getDeviceWidth(context)
        val layoutParams = holder.viewBinding.layoutRoot.layoutParams
        layoutParams.height = (width - (15 * 2 + 5 * 3).dp2Px()) / 4
    }

    override fun onClick(
        holder: BinderVBHolder<ItemImgAddBinding>,
        view: View,
        data: ImgAddBean,
        position: Int
    ) {
        onItemAddClickListener?.invoke(data)
    }

}