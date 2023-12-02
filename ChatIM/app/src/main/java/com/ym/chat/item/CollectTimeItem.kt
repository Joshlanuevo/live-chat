package com.ym.chat.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.chat.bean.RecordDateBean
import com.ym.chat.databinding.ItemCollectTimeBinding
import com.ym.chat.utils.TimeUtils
import java.text.SimpleDateFormat

/**
 * 收藏时间显示
 * @Description
 * @Author：
 * @Date：时间线显示
 * @Time：16:33
 */
class CollectTimeItem() : QuickViewBindingItemBinder<RecordDateBean, ItemCollectTimeBinding>() {

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemCollectTimeBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemCollectTimeBinding>, data: RecordDateBean) {
        //显示时间
        holder.viewBinding.tvDate.text = TimeUtils.getStringDateMd(data.createTime)
    }

}