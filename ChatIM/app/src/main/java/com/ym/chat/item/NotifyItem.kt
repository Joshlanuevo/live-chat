package com.ym.chat.item

import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import coil.transform.CircleCropTransformation
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.invisible
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.NotifyBean
import com.ym.chat.databinding.ItemNotifyMsgBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadHeader
import com.ym.chat.ext.loadImg
import com.ym.chat.ext.setColorAndString
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.ContactActivity
import com.ym.chat.ui.FriendInfoActivity
import com.ym.chat.utils.TimeUtils
import com.ym.chat.utils.Utils
import java.io.Serializable

/**
 * 系统通知 item
 */
class NotifyItem(
    private val onClickListener: ((type: Int, bean: NotifyBean) -> Unit)? = null,//@type 0 同意  1 拒绝 2 删除
) : QuickViewBindingItemBinder<NotifyBean, ItemNotifyMsgBinding>() {
    //<editor-fold defaultstate="collapsed" desc="XML">
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = ItemNotifyMsgBinding.inflate(layoutInflater, parent, false)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="数据绑定">
    override fun convert(holder: BinderVBHolder<ItemNotifyMsgBinding>, data: NotifyBean) {
        holder.viewBinding.let { vb ->
            vb.tvContentTitle.gone()
            vb.tvContent.gone()
            vb.cFriendInfo.gone()
            vb.vBottom.gone()
            vb.llVerify.gone()
            vb.tvLook.gone()
            vb.tvTypeTitle.text = data.title
            vb.tvTypeTime.text = TimeUtils.format(data.createTime)

            //已读未读显示
            if (data.msgReadState == 0) {
                vb.viewUnRead.visible()
            } else {
                vb.viewUnRead.gone()
            }

            if (data.isShowCheck) {
                vb.cbSelect.visible()
                when (data.selectType) {
                    0 -> {
                        //能点击状态
                        vb.cbSelect.isEnabled = true
                        vb.cbSelect.setBackgroundResource(R.drawable.tab_select)
                        //没选中状态
                        vb.cbSelect.isChecked = false
                    }
                    1 -> {
                        //能点击状态
                        vb.cbSelect.isEnabled = true
                        vb.cbSelect.setBackgroundResource(R.drawable.tab_select)
                        //选中状态
                        vb.cbSelect.isChecked = true
                    }
                    else -> {
                        //不能点击,置灰
                        vb.cbSelect.isEnabled = false
                        vb.cbSelect.setBackgroundResource(R.drawable.ic_selsect_e)

                        vb.cbSelect.isChecked = true
                    }
                }
            } else {
                vb.cbSelect.isEnabled = true
                vb.cbSelect.gone()
            }

            when (data.type) {
                0 -> {
                    //系统消息
                    vb.tvContentTitle.visible()
                    vb.tvContent.visible()
                    vb.tvContentTitle.text = data.contentTitle
                    if (data.systemType == 1) {
                        vb.ivTypeIcon.setImageResource(R.drawable.ic_notify_msg)
                        vb.tvTypeTitle.text = context.getString(R.string.xitongtongzhi) // "系统通知"
                        if (data.contentBriefly != null)
                            vb.tvContent.text = Html.fromHtml(data.contentBriefly)
                        vb.tvLook.visible()
                    } else {
                        vb.ivTypeIcon.setImageResource(R.drawable.ic_notify_feedback_msg)
                        vb.tvTypeTitle.text = context.getString(R.string.fankuiwenjianhuifu) // "反馈回复通知"
                        vb.tvContent.text = Html.fromHtml(data.content)
                    }
                }
                1 -> {
                    //系统验证码消息
                    vb.ivTypeIcon.setImageResource(R.drawable.ic_notify_verify)
                    vb.tvContent.visible()
                    var contentTitle = context.getString(R.string.xinshebeidenglu) // "【新设备登录验证】"
                    var keepTime = data.keepTime?.toInt()?.div(60) ?: 15
                    var content =
                        "${R.string.xinshebeidenglu}：${data.verfiyCode}， ${R.string.xinshebeixitong}：${data.deviceDescription}。 " +
                                "${R.string.如您授权新设备登录}：$keepTime ${R.string.请检查您的帐号是否泄漏的可能}"
//                        "【新设备登录验证】：${data.verfiyCode}， 新设备系统为：${data.deviceDescription}。 " +
//                                "如您授权新设备登录，请在新设备输入验证码。验证后，该设备下次登录无需验证。有效期：$keepTime 分钟。请勿泄漏！如果不是您本人操作，请检查您的帐号是否泄漏的可能。"
                    vb.tvContent.text = content.setColorAndString(contentTitle)
                }
                2 -> {
                    //系统好友验证消息
                    vb.cFriendInfo.visible()
                    vb.llVerify.visible()
                    vb.vBottom.visible()
                    vb.tvVerifyContent.visible()
                    vb.ivTypeIcon.setImageResource(R.drawable.ic_notify_friend)

                    var isSend = data.from == MMKVUtils.getUser()?.id //是我发的申请 还是 收的申请
                    var name = if (isSend) data.to else data.from
                    var friend = name?.let {
                        ChatDao.getFriendDb().getFriendById(it)
                    }
                    var isMakeDate = false
                    if (friend == null) {
                        friend = FriendListBean(
                            -1,
                            name = data.friendMemberName ?: "",
                        ).apply {
                            this.headUrl = data.friendMemberHeadUrl ?: ""
                        }
                        friend.id = data.from ?: ""
                        friend.friendMemberId = data.from ?: ""
                        isMakeDate = true
                    }
                    vb.tvNickName.text = friend?.nickname
                    vb.tvMsgPre.text = data.content
                    vb.layoutHeader.ivHeader.loadImg(friend, vb.layoutHeader.tvHeader)
                    vb.layoutHeader.root.click {
                        val intent = Intent(context, FriendInfoActivity::class.java)
                        intent.putExtra(ContactActivity.IN_TYPE, 3)
                        intent.putExtra("isMakeDate", isMakeDate)
                        context.startActivity(
                            intent.putExtra(ChatActivity.CHAT_INFO, friend)
                        )
                    }
                    Utils.showDaShenImageView(
                        vb.layoutHeader.ivHeaderMark,
                        friend?.displayHead == "Y",
                        friend?.levelHeadUrl
                    )

                    //0 需要验证  2 已拒绝  1 已同意
                    when (data.verifyType) {
                        0 -> {
                            //申请加好友，等待添加
                            if (isSend) {
                                vb.tvVerifyContent.text = context.getString(R.string.已提交好友申请) // "已提交好友申请"
                                vb.btnRefuse.gone()
                                vb.btnAgree.gone()
                            } else {
                                vb.tvVerifyContent.gone()
                                vb.btnRefuse.visible()
                                vb.btnAgree.visible()
                            }
                        }
                        1 -> {

                            vb.btnRefuse.gone()
                            vb.btnAgree.gone()

                            //同意加好友
                            if (isSend) {
                                vb.tvVerifyContent.text = context.getString(R.string.yitongyinideshenqing) // "对方已同意你的申请"
                            } else {
                                vb.tvVerifyContent.text = context.getString(R.string.已成为好友) // "已成为好友"
                            }
                        }
                        2 -> {

                            vb.btnRefuse.gone()
                            vb.btnAgree.gone()

                            //拒绝加好友
                            if (isSend) {
                                vb.tvVerifyContent.text = context.getString(R.string.duifangyijujuenideshenqing) // "对方已拒绝你的申请"
                            } else {
                                vb.tvVerifyContent.text = context.getString(R.string.yijujue) // "已拒绝"
                            }
                        }
                    }
                }
                3 -> {
                    //系统群成员验证消息
                    vb.cFriendInfo.visible()
                    vb.llVerify.visible()
                    vb.vBottom.visible()
                    vb.ivTypeIcon.setImageResource(R.drawable.ic_notify_group)
                    try {
//                        //获取操作者信息
//                        var userAdmin = data.memberId?.let {
//                            data.groupId?.let { it1 ->
//                                ChatDao.getGroupDb().getMemberInGroup(
//                                    it,
//                                    it1
//                                )
//                            }
//                        }
//                        //获取被邀请者成员信息
//                        var userMember = data.friendMemberId?.let {
//                            data.groupId?.let { it1 ->
//                                ChatDao.getGroupDb().getMemberInGroup(
//                                    it,
//                                    it1
//                                )
//                            }
//                        }
                        var groupName =
                            data.groupId?.let { ChatDao.getGroupDb().getGroupInfoById(it)?.name }
                        holder.viewBinding.layoutHeader.ivHeader.loadHeader(
                            data.friendMemberId ?: "",
                            data.friendMemberName ?: "",
                            data.friendMemberHeadUrl ?: "",
                            holder.viewBinding.layoutHeader.tvHeader
                        )
//                        if (userMember != null) {
//                            //如果好友里面找到了，显示头像
//                            holder.viewBinding.layoutHeader.ivHeader.loadImg(userMember)
//                            Utils.showDaShenImageView(
//                                holder.viewBinding.layoutHeader.ivHeaderMark,
//                                userMember?.displayHead == "Y",
//                                userMember?.levelHeadUrl
//                            )
//                        } else {
//                            //如果好友里面没有找到
//                            holder.viewBinding.layoutHeader.ivHeader.loadImg(
//                                data.friendMemberHeadUrl,
//                                data.friendMemberName,
//                                R.drawable.ic_mine_header
//                            )
//                        }

                        var text = "${R.string.guanliyuan}${data.memberName}${R.string.yaoqingjiaru}${groupName} 群"
                        vb.tvNickName.text =
                            groupName?.let {
                                data.memberName?.let { it1 ->
                                    text.setColorAndString(
                                        it1,
                                        it, context.getColor(R.color.color_main)
                                    )
                                }
                            }
                        vb.tvMsgPre.visible()
                        vb.tvMsgPre.text = "${R.string.huiyuan} ${data.friendMemberName} ${R.string.shenqingruqun}"
                    } catch (e: Exception) {
                        "---------获取成员数据异常-${e.message.toString()}".logE()
                    }
                    var isSend = data.from == MMKVUtils.getUser()?.id //是我发的申请 还是 收的申请
                    when (data.verifyType) {
                        0 -> {
                            //申请入群
                            if (isSend) {
                                //如果我发的申请
                                vb.tvVerifyContent.visible()
                                vb.btnAgree.gone()
                                vb.btnRefuse.gone()
                                vb.tvVerifyContent.text = context.getString(R.string.yitijiaoshenqing) // "已提交申请，等待验证"
                            } else {
                                //如果是别人提交的申请
                                vb.btnAgree.visible()
                                vb.btnRefuse.visible()
                                vb.tvVerifyContent.gone()
                            }
                        }
                        1 -> {
                            //同意入群
                            vb.tvVerifyContent.visible()
                            if (isSend) {
                                //如果我发的申请
                                vb.tvVerifyContent.text = context.getString(R.string.yitongyinideshenqing) // "对方已同意你的申请"
                            } else {
                                //如果是别人提交的申请
                                vb.tvVerifyContent.text = context.getString(R.string.已同意) // "已同意"
                            }
                        }
                        2 -> {
                            //拒绝入群
                            vb.tvVerifyContent.visible()
                            if (isSend) {
                                //如果我发的申请
                                vb.tvVerifyContent.text = context.getString(R.string.duifangyijujuenideshenqing) // "对方已拒绝你的申请"
                            } else {
                                //如果是别人提交的申请
                                vb.tvVerifyContent.text = context.getString(R.string.yijujue) // "已拒绝"
                            }
                        }
                        3 -> {
                            //已入群
                            vb.tvVerifyContent.visible()
                            if (isSend) {
                                //如果我发的申请
                                vb.tvVerifyContent.text = context.getString(R.string.已同意) // "已同意"
                            } else {
                                //如果是别人提交的申请
                                vb.tvVerifyContent.text = context.getString(R.string.已同意) // "已同意"
                            }
                        }
                    }
                }
            }
            vb.btnAgree.click {
                onClickListener?.invoke(0, data)
            }
            vb.btnRefuse.click {
                onClickListener?.invoke(1, data)
            }
            vb.ivDel.click {
                onClickListener?.invoke(2, data)
            }
            vb.cbSelect.click {
                data.selectType = if (vb.cbSelect.isChecked) 1 else 0
                onClickListener?.invoke(3, data)
            }
            vb.root.click {
                //必须是 系统通知
                if (data.type == 0 && data.systemType == 1) {
                    onClickListener?.invoke(4, data)
                }
            }
        }
    }
    //</editor-fold>
}