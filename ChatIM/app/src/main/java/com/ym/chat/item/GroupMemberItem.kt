package com.ym.chat.item

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.blankj.utilcode.util.ResourceUtils.getDrawable
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.chat.R
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.databinding.ItemGroupMemberBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.popup.GroupHeaderPopupWindow
import com.ym.chat.utils.Utils
import java.util.*

/**
 * 群设置显示头像
 */
class GroupMemberItem(
    val onItemClickListener: ((memberBean: GroupMemberBean) -> Unit)? = null,
    private val onLongClickListener: ((type: Int, position: Int, memberBean: GroupMemberBean) -> Unit)? = null
) : QuickViewBindingItemBinder<GroupMemberBean, ItemGroupMemberBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemGroupMemberBinding.inflate(layoutInflater, parent, false)


    override fun convert(holder: BinderVBHolder<ItemGroupMemberBinding>, data: GroupMemberBean) {
        holder.viewBinding.let {
            if (data.id == "10086") {
                it.layoutHeader.ivHeader.setImageResource(R.drawable.ic_group_add_member)
                it.layoutHeader.ivHeaderMark.gone()
            } else {
                it.layoutHeader.ivHeader.loadImg(data)
                Utils.showDaShenImageView(it.layoutHeader.ivHeaderMark, data)
                it.root.setOnLongClickListener { view ->
                    if (data.id != MMKVUtils.getUser()?.id) {
                        var roleType = ChatDao.getGroupDb().getGroupInfoById(data.groupId)?.roleType
                        when (roleType?.lowercase()) {
                            "Owner".lowercase() -> {//我是群主
                                showPopup(holder, data, true)
                            }
                            "admin".lowercase() -> {//我是管理员
                                when (data.role.lowercase()) {
                                    "normal".lowercase() -> {//被操作人是普通成员
                                        if (data.role?.lowercase(Locale.getDefault()) == "normal".lowercase(
                                                Locale.getDefault()
                                            )
                                        ) {
                                            //只能踢人和禁言
                                            showPopup(holder, data, false)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    true
                }
            }
            it.tvName.text = data.nickname
            it.root.click { onItemClickListener?.invoke(data) }
        }
    }

    private fun showPopup(
        holder: QuickViewBindingItemBinder.BinderVBHolder<ItemGroupMemberBinding>,
        data: GroupMemberBean,
        isShowAll: Boolean = false
    ) {
        var groupHeaderPopupWindow =
            GroupHeaderPopupWindow(context, onItemClickListener = { type ->
                onLongClickListener?.invoke(type, holder.adapterPosition, data)
            })
        groupHeaderPopupWindow.setSettingAdmin(data.role.lowercase() != "Normal".lowercase())
        groupHeaderPopupWindow.setSettingMute(data.allowSpeak == "N")
        groupHeaderPopupWindow.setShowView(isShowAll)
        groupHeaderPopupWindow.showPopup(
            holder.viewBinding.root,
            SizeUtils.dp2px(-42f),
            SizeUtils.dp2px(-56f),
            Gravity.RIGHT
        )
    }
}