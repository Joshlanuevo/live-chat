package com.ym.chat.item.chatlistener

import com.ym.chat.bean.ChatMessageBean

enum class ChatPopClickType {
    Forward, Edit, Collect, Destory, Copy, Reply, Top, AddPhiz
}

interface OnChatItemListener {
    /**
     * item点击事件
     */
    fun onItemClick(bean: ChatMessageBean, position: Int)

    /**
     * 消息已读回执
     */
    fun readCallBack(data: ChatMessageBean)

    /**
     * 消息重发
     */
    fun reSendMsg(data: ChatMessageBean)

    /**
     * 弹窗点击事件
     */
    fun onPopMenuClickListener(data: ChatMessageBean, position: Int, type: ChatPopClickType)

    /**
     * 点击头像查看资料
     */
    fun onItemHeaderClick(data: ChatMessageBean)

    /**
     * 长按头像 @人
     */
    fun onItemHeaderLongClick(type: Int, data: ChatMessageBean)

    /**
     * 点击回复消息
     */
    fun clickReplyMsg(data: ChatMessageBean)

}