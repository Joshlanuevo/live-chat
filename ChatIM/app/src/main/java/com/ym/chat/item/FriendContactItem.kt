package com.ym.chat.item

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.databinding.ItemFriendBinding
import com.ym.chat.databinding.ItemGroupMemberBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.popup.GroupHeaderPopupWindow
import com.ym.chat.utils.PopUtils
import com.ym.chat.utils.Utils
import java.util.*


class FriendContactItem(
    private val onCheckListener: ((position: Int, bean: FriendListBean) -> Unit)? = null,//选择框
    private val onClickListener: ((position: Int, bean: FriendListBean) -> Unit)? = null,//点击头像
    private val onLongClickListener: ((type: Int, position: Int, bean: FriendListBean) -> Unit)? = null//长按弹框设置
) : QuickViewBindingItemBinder<FriendListBean, ItemFriendBinding>() {
    //<editor-fold defaultstate="collapsed" desc="XML">
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemFriendBinding.inflate(layoutInflater, parent, false)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="数据绑定">
    override fun convert(holder: BinderVBHolder<ItemFriendBinding>, data: FriendListBean) {
        holder.viewBinding.let { vb ->
            vb.layoutHeader.ivHeader.loadImg(data)
//            Utils.showDaShenImageView(
//                vb.layoutHeader.ivHeaderMark, data?.displayHead == "Y", data?.levelHeadUrl
//            )
            Utils.showDaShenImageView(vb.layoutHeader.ivHeaderMark,data)
            vb.tvName.text = data.nickname
            if (data.isShowCheck) {
                vb.cbSelect.visible()
                vb.cbSelect.isChecked = data.isSelect
                vb.cbSelect.click {
                    data.isSelect = !data.isSelect
                    onCheckListener?.invoke(holder.adapterPosition, data)
                }
            } else {
                vb.cbSelect.gone()
            }
            if (data.showLine) vb.vLine.visible() else vb.vLine.gone()
            vb.layoutHeader.ivHeader.click {
                onClickListener?.invoke(holder.adapterPosition, data) }
            vb.tvName.click {
                vb.cbSelect.isChecked = !vb.cbSelect.isChecked
                data.isSelect = !data.isSelect
                onClickListener?.invoke(holder.adapterPosition, data) }
            vb.tvName.setOnLongClickListener {
                if (data.id != MMKVUtils.getUser()?.id) {
                    var roleType = ChatDao.getGroupDb().getGroupInfoById(data.groupId)?.roleType
                    when (roleType?.lowercase()) {
                        "Owner".lowercase() -> {//我是群主
                            showPopup(it, data, true,holder.adapterPosition)
                        }
                        "admin".lowercase() -> {//我是管理员
                            when (data.role.lowercase()) {
                                "normal".lowercase() -> {//被操作人是普通成员
                                    if (data.role?.lowercase(Locale.getDefault()) == "normal".lowercase(
                                            Locale.getDefault()
                                        )
                                    ) {
                                        //只能踢人和禁言
                                        showPopup(it, data, false,holder.adapterPosition)
                                    }
                                }
                            }
                        }
                    }
                }
                true
            }
            vb.clRoot.setOnLongClickListener {
                if (data.id != MMKVUtils.getUser()?.id) {
                    var roleType = ChatDao.getGroupDb().getGroupInfoById(data.groupId)?.roleType
                    when (roleType?.lowercase()) {
                        "Owner".lowercase() -> {//我是群主
                            showPopup(it, data, true,holder.adapterPosition)
                        }
                        "admin".lowercase() -> {//我是管理员
                            when (data.role.lowercase()) {
                                "normal".lowercase() -> {//被操作人是普通成员
                                    if (data.role?.lowercase(Locale.getDefault()) == "normal".lowercase(
                                            Locale.getDefault()
                                        )
                                    ) {
                                        //只能踢人和禁言
                                        showPopup(it, data, false,holder.adapterPosition)
                                    }
                                }
                            }
                        }
                    }
                }
                true
            }
        }
    }
    //</editor-fold>


    private fun showPopup(
        view: View, data: FriendListBean,
        isShowAll: Boolean = false,
        position: Int
    ) {
        var groupHeaderPopupWindow =
            GroupHeaderPopupWindow(context, onItemClickListener = { type ->
                onLongClickListener?.invoke(type, position, data)
            })
        groupHeaderPopupWindow.setSettingAdmin(data.role.lowercase() != "Normal".lowercase())
        groupHeaderPopupWindow.setSettingMute(data.allowSpeak == "N")
        groupHeaderPopupWindow.setShowView(isShowAll)
        groupHeaderPopupWindow.showPopupAtViewTop(view)
        groupHeaderPopupWindow.showPopup(
            view,
            SizeUtils.dp2px(60f),
            SizeUtils.dp2px(-20f),
            Gravity.TOP
        )

    }
}