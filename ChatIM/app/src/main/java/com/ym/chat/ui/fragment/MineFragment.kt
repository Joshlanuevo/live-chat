package com.ym.chat.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import coil.load
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.copyToClipboard
import com.ym.base.mvvm.BaseFragment
import com.ym.base.util.save.LoginData
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.FragmentMineBinding
import com.ym.chat.ext.loadImg
import com.ym.chat.rxhttp.UserRepository
import com.ym.chat.ui.*
import com.ym.chat.utils.ToastUtils
import com.ym.chat.utils.Utils

/**
 * 我的Fragment
 */
class MineFragment : BaseFragment(R.layout.fragment_mine) {
    private val bindView: FragmentMineBinding by binding()
    private var user: LoginData? = null

    override fun onResume() {
        super.onResume()
        updateUI()
    }
    override fun initView() {

        if (MMKVUtils.isAdmin()) {
            bindView.llGroupSend.visible()
            bindView.vGroupSend.visible()
            bindView.llGroupSend.click {
                //群发助手
                startActivity(Intent(activity, GroupSendUtilsActivity::class.java))
            }
//            bindView.llShareCode.visible()
            bindView.vShareCode.visible()
            bindView.llShareCode.click {
                //邀请码
                startActivity(Intent(activity, ShareCodeActivity::class.java))
            }
        }
        bindView.tvSet.click {
            //设置
            startActivity(Intent(activity, SetActivity::class.java))
        }
        bindView.tvFeedback.click {
            //意见反馈
            startActivity(Intent(activity, FeedbackActivity::class.java))
        }
//        bindView.tvLanguages.click {
//            startActivity(Intent(activity, FeedbackActivity::class.java))
//        }
        bindView.consInfo.click {
            //编辑用户信息
            startActivity(Intent(activity, PersonalInfoActivity::class.java))
        }

        bindView.tvJxh.setOnLongClickListener {
            user?.username.copyToClipboard()
            ToastUtils.showToastWithImg(activity, getString(R.string.已复制), R.drawable.ic_dialog_success)
            true
        }
    }

    /**
     * 用户信息显示
     */
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        MMKVUtils.getUser()?.let { userInfo ->
            user = userInfo
            bindView.tvNickName.text = userInfo.name
            user = MMKVUtils.getUser()
//            var username = user?.username
//            if (username?.isEmpty() == true) {
//                username = user?.code
//            }
            bindView.tvJxh.text = "${getText(R.string.zhanghao)}:${user?.showUserName()}"
            bindView.layoutHeader.apply {
                setRoundRadius(200f)
                setChatId(user?.id?:"")
                setChatName(user?.name?:"")
            }.showUrl(user?.headUrl)
//            bindView.layoutHeader.ivHeader.loadImg(userInfo)
//            Utils.showDaShenImageView(
//                bindView.layoutHeader.ivHeaderMark,
//                userInfo.displayHead == "Y",
//                userInfo.levelHeadUrl
//            )
            setGender()
        }
    }

    private fun setGender() {
        when (user?.gender?.lowercase()) {
            "male".lowercase() -> {
                //男
                bindView.tvGender.visible()
                bindView.tvGender.text = "（${getString(R.string.nan)}）"
            }
            "female".lowercase() -> {
                //女
                bindView.tvGender.visible()
                bindView.tvGender.text = "${getString(R.string.nv)}）"
            }
            else -> {
                bindView.tvGender.gone()
            }
        }

    }

    override fun requestData() {
        UserRepository.refreshUserInfo()
    }

    override fun observeCallBack() {
        /*接收修改名字 广播*/
        LiveEventBus.get(EventKeys.EDIT_USER_NAME, String::class.java).observe(this) {
            user = MMKVUtils.getUser()
//            var username = user?.username
//            if (username?.isEmpty() == true) {
//                username = user?.code
//            }
            bindView.tvJxh.text = String.format(getString(R.string.友聊号),user?.showUserName())

            bindView.tvNickName.text = user?.name
            setGender()

//            bindView.layoutHeader.ivHeader.loadImg(user)
            MMKVUtils.putUser(user)//保存到本地
        }
        /*接收修改头像 广播*/
        LiveEventBus.get(EventKeys.EDIT_USER_HEAD, String::class.java).observe(this) {
            user?.headUrl = it
//            bindView.layoutHeader.ivHeader.loadImg(user)
            MMKVUtils.putUser(user)//保存到本地
        }

        /*接收ws修改个人信息同步 广播*/
        LiveEventBus.get(EventKeys.EDIT_USER, String::class.java).observe(this) {
            user = MMKVUtils.getUser()
            bindView.tvNickName.text = user?.name
            bindView.tvJxh.text = "友聊号:${user?.showUserName()}"
            setGender()

//            bindView.layoutHeader.ivHeader.loadImg(user)
        }
    }
}