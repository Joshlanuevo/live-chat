package com.ym.chat.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ym.base.ext.logD
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.databinding.ItemAtSelectBinding
import com.ym.chat.ext.loadImg
import com.ym.chat.ext.setColorAndString
import com.ym.chat.utils.Utils

/**
 * @功能
 */
class ChatAtSelectItem(val onItemClick: ((bean: GroupMemberBean, position: Int) -> Unit)? = null) :
    QuickVBItemBinderPro<GroupMemberBean, ItemAtSelectBinding>() {

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemAtSelectBinding = ItemAtSelectBinding.inflate(layoutInflater, parent, false)

    override fun convert(holder: BinderVBHolder<ItemAtSelectBinding>, data: GroupMemberBean) {
        holder.viewBinding.let { bindView ->
//            var atMsg = if(data?.atStr?.contains("@")) data?.atStr?.removePrefix("@") else ""
            val atMsg = data?.atStr?.removePrefix("@") ?: ""
            bindView.tvName.text = data.name.setColorAndString(atMsg,context.getColor(R.color.color_at))
            bindView.tvJxCode.text = data.code
            bindView.layoutHeader.ivHeader.loadImg(data)
//            Utils.showDaShenImageView(
//                bindView.layoutHeader.ivHeaderMark, data?.displayHead == "Y", data?.levelHeadUrl
//            )
            Utils.showDaShenImageView(bindView.layoutHeader.ivHeaderMark,data)
            bindView.root.setOnClickListener {
                onItemClick?.invoke(data, holder.adapterPosition)
            }
        }
    }
}