package com.ym.chat.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import coil.load
import com.ym.base.ext.logD
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.bean.VersionBean
import com.ym.chat.dialog.updateDialog


/**
 * 公共的帮助类
 */
object Utils {

    var isShowDaShen =false
    /**
     * 是否显示大神
     * @tv 带背景TextView
     * @content  显示的内容
     * @isShow   是否显示
     */
    fun showDaShenView(
        tv: TextView?,
        content: String?,
        isShow: Boolean,
        levelHeadUrl: String = ""
    ) {
        if (isShow) {
            tv?.visible()
            tv?.text = content
        } else {
            tv?.gone()
        }
    }

    /**
     * 是否显示大神图片
     * @iv 图片view
     * @isShow   是否显示
     * @levelHeadUrl 显示图片的地址
     */
    fun showDaShenImageView(iv: ImageView?, isShow: Boolean, levelHeadUrl: String?) {
        if (isShow) {
            if (levelHeadUrl.isNullOrBlank()) {
//                "大神头像地址错误---levelHeadUrl=${levelHeadUrl}".logD()
                iv?.gone()
            } else {
                iv?.load(levelHeadUrl) {
                    crossfade(true)//淡出显示
                    error(R.color.transparent)//加载错误显示的图片
                }
                iv?.visible()
            }
        } else {
            iv?.gone()
        }
    }

    /**
     * 是否显示 大神头像
     * 群主
     * 群管理
     */
    fun showDaShenImageView(iv: ImageView?, groupMember: GroupMemberBean?) {
        if (groupMember != null) {
            //Owner 群主，admin 管理员，Normal 普通群成员
//            when (groupMember.role.uppercase()) {
//                "Owner".uppercase() -> {
//                    iv?.visible()
//                    iv?.load(R.drawable.ic_group_owner)
//                }
//                "Admin".uppercase() -> {
//                    iv?.visible()
//                    iv?.load(R.drawable.ic_group_admin)
//                }
//                else -> {
                    if (groupMember?.displayHead == "Y") {
                        if (groupMember?.levelHeadUrl.isNullOrBlank()) {
                            iv?.gone()
                        } else {
                            iv?.load(groupMember?.levelHeadUrl) {
                                crossfade(true)//淡出显示
                                error(R.color.transparent)//加载错误显示的图片
                            }
                            iv?.visible()
                        }
                    }else{
                        iv?.gone()
                    }
//                }
//            }
        } else {
            iv?.gone()
        }
    }

    /**
     * 是否显示 大神头像
     * 群主
     * 群管理
     */
    fun showDaShenImageView(iv: ImageView?, groupMember: FriendListBean?) {
        if (groupMember != null) {
            //Owner 群主，admin 管理员，Normal 普通群成员
//            when (groupMember.role.uppercase()) {
//                "Owner".uppercase() -> {
//                    iv?.visible()
//                    iv?.load(R.drawable.ic_group_owner)
//                }
//                "Admin".uppercase() -> {
//                    iv?.visible()
//                    iv?.load(R.drawable.ic_group_admin)
//                }
//                else -> {
                    if (groupMember?.displayHead == "Y") {
                        if (groupMember?.levelHeadUrl.isNullOrBlank()) {
                            iv?.gone()
                        } else {
                            iv?.load(groupMember?.levelHeadUrl) {
                                crossfade(true)//淡出显示
                                error(R.color.transparent)//加载错误显示的图片
                            }
                            iv?.visible()
                        }
                    }else{
                        iv?.gone()
                    }
//                }
//            }
        } else {
            iv?.gone()
        }
    }

    /**
     * 判断是不是有最新版本
     */
    fun isNewAppVersion(version: String, versionNo: String): Boolean {
        if (version.contains(".") && versionNo.contains(".")) {
            var vNmb = version.replace(".", "")
            var vNoNmb = versionNo.replace(".", "")
//            "---vNmb=${vNmb}----vNoNmb=${vNoNmb}---大小=${vNoNmb > vNmb}".logD()
            if (vNoNmb > vNmb) {
                return true
            }
        }
        return false
    }

    /**
     * 弹框提示版本更新
     */
    fun showUpdateDialog(vb: VersionBean, fragmentManager: FragmentManager) {
        updateDialog(fragmentManager = fragmentManager) {
            isForce = vb.mustUpdate == "Y"
            mNewVersion = vb.versionNo ?: ""
            mContent = "最新版本：V${vb.versionNo ?: ""}\n更新内容:\n${vb.versionDesc}"
            mDownUrl = vb.downloadUrl
//            mDownUrl = "https://a4.files.diawi.com/app-file/eh4Wh8ieW2WSlNDsvHxY.apk"
        }
    }

}