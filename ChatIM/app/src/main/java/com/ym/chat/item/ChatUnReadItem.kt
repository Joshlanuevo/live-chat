package com.ym.chat.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.databinding.ItemChatUnreadBinding
import java.text.SimpleDateFormat

/**
 * @Description
 * @Author：
 * @Date：消息未读
 * @Time：16:33
 */
class ChatUnReadItem : QuickVBItemBinderPro<ChatMessageBean, ItemChatUnreadBinding>() {

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemChatUnreadBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemChatUnreadBinding>, data: ChatMessageBean) {

    }
}