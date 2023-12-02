package com.ym.chat.item

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.blankj.utilcode.util.SizeUtils
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.adapter.QuickVBItemBinderPro
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ChatMessageBean
import com.ym.chat.databinding.ItemUndefinedChatLeftBinding
import com.ym.chat.databinding.ItemUndefinedChatRightBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.item.chatlistener.OnChatItemListener
import com.ym.chat.utils.ChatType
import com.ym.chat.utils.ChatUtils
import com.ym.chat.utils.PopUtils
import com.ym.chat.utils.Utils


/**
 * 未知消息-右边
 */
class ChatUndefinedLeft(
    private val onChatItemListener: OnChatItemListener,
) : QuickVBItemBinderPro<ChatMessageBean, ItemUndefinedChatLeftBinding>() {

    init {
        addChildLongClickViewIds(R.id.llTxtContent, R.id.tvContentRight)
        addChildClickViewIds(R.id.llTxtContent, R.id.tvContentRight)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemUndefinedChatLeftBinding {
        return ItemUndefinedChatLeftBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(
        holder: BinderVBHolder<ItemUndefinedChatLeftBinding>,
        data: ChatMessageBean
    ) {

        val userBean = MMKVUtils.getUser()

        if (data.chatType == ChatType.CHAT_TYPE_GROUP) {
            //需要显示对方昵称和头像

            holder.viewBinding.tvFromUserName.visible()
            holder.viewBinding.layoutHeader.flHeader.visible()
        } else {
            //不需要现实对方昵称和头像
            holder.viewBinding.layoutHeader.flHeader.gone()
            holder.viewBinding.tvFromUserName.gone()
        }
        try {
            ChatDao.getGroupDb().getMemberInGroup(data.from, data.groupId)?.let { member ->
                holder.viewBinding.tvFromUserName.text = member.nickname
                holder.viewBinding.layoutHeader.ivHeader.loadImg(member)
                Utils.showDaShenImageView(holder.viewBinding.layoutHeader.ivHeaderMark, member)
            }
        }catch (e:Exception){
            "---------获取成员数据异常-${e.message.toString()}".logE()
        }
    }

    override fun onChildLongClick(
        holder: BinderVBHolder<ItemUndefinedChatLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ): Boolean {
        if (view.id == R.id.llTxtContent || view.id == R.id.tvContentRight) {
            val type = PopUtils.calculatePopWindowPos(view)
            val gravityType = when (type[0]) {
                1 -> Gravity.BOTTOM
                2 -> Gravity.TOP
                else -> Gravity.TOP
            }
            val offsetY = when (type[0]) {
                1 -> SizeUtils.dp2px(10.0f)
                2 -> type[1]
                else -> SizeUtils.dp2px(-0.0f)
            }
            //长按选中
            ChatUtils.showPopupWindow(
                context,
                view,
                data,
                position,
                onChatItemListener,
                gravityType = gravityType,
                offsetY = offsetY
            )
        }
        return true
    }

    override fun onChildClick(
        holder: BinderVBHolder<ItemUndefinedChatLeftBinding>,
        view: View,
        data: ChatMessageBean,
        position: Int
    ) {
        //item点击事件
        val itemBean = adapter.data[position] as ChatMessageBean
        if (itemBean.isEditMode) {
            val sel = itemBean.isSel
            itemBean.isSel = !sel
            adapter.notifyItemChanged(position)
        }
    }
}