package com.ym.chat.ui

import android.content.Intent
import android.text.TextUtils
import android.view.View
import coil.load
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.copyToClipboard
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginData
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityPersonalBinding
import com.ym.chat.db.AccountDao
import com.ym.chat.ext.loadImg
import com.ym.chat.ext.roundLoad
import com.ym.chat.popup.SelectPhotoPopWindow
import com.ym.chat.utils.ImageUtils
import com.ym.chat.utils.ToastUtils
import com.ym.chat.utils.Utils
import com.ym.chat.viewmodel.ChatViewModel
import com.ym.chat.viewmodel.ModifyNameViewModel

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 个人信息
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class PersonalInfoActivity : LoadingActivity() {
    private val bindView: ActivityPersonalBinding by binding()
    private val mViewModel: ModifyNameViewModel = ModifyNameViewModel()
    private var headUrl = ""
    private var user: LoginData? = null
    override fun initView() {
        bindView.ivBack.click {
            finish()
        }
        bindView.layoutHeader.ivHeader.click {
            SelectPhotoPopWindow(this)
                .apply {
                    onItemClickListener = { data, position, view ->
                        dismiss()
                        when (position) {
                            0 -> ImageUtils.goCamera(
                                activity,
                                true,
                                onResultCallBack = { localPath: String, w: Int, h: Int, time: Long, listSize ->
                                    updateImageFileGoogle(localPath)
                                })
                            1 -> ImageUtils.goSelImg(
                                activity,
                                1,
                                true,
                                onResultCallBack = { localPath: String, w: Int, h: Int, time: Long, listSize: Int ->
                                    updateImageFileGoogle(localPath)
                                }, isGif = false,isWebP=false
                            )
                        }
                    }
                }.showPopupWindow()
        }
        bindView.cons1.click {
            //修改
            startActivity(Intent(this, ModifyNameActivity::class.java))
        }
        bindView.layoutPhone.click {
            //修改手机号
            startActivity(Intent(this, ModifyPhoneActivity::class.java))

        }
        bindView.layoutPwd.click {
            //修改密码
            startActivity(Intent(this, ModifyPwdActivity::class.java))
        }
        bindView.cvUserName.click {
            //修改友聊号
            startActivity(Intent(this, ModifyUserNameActivity::class.java))
        }
        bindView.tvUsername.setOnLongClickListener {
            bindView.tvUsername.text.toString().trim().copyToClipboard()
            ToastUtils.showToastWithImg(
                this@PersonalInfoActivity,
                getString(R.string.已复制),
                R.drawable.ic_dialog_success
            )
            true
        }

    }

    override fun requestData() {
        setUserInfoView()
    }

    /**
     * 显示view
     */
    private fun setUserInfoView() {
        user = MMKVUtils.getUser()
        user?.apply {
            bindView.layoutHeader.ivHeader.loadImg(user)
            bindView.tvNickName.text = this?.name
            var username = this.username
            if (username.isEmpty()) {
                username = this.code
            }
            bindView.tvUsername.text = username
            bindView.tvPhone.text = this?.mobile
            setGender()

            Utils.showDaShenImageView(
                bindView.layoutHeader.ivHeaderMark, this?.displayHead == "Y", this?.levelHeadUrl
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setUserInfoView()
    }

    override fun onRestart() {
        super.onRestart()
        bindView.tvPhone.text = MMKVUtils.getUser()?.mobile
    }


    private fun setGender() {

        when (user?.gender?.lowercase()) {
            "male".lowercase() -> {
                //男
                bindView.tvGender.visible()
                bindView.tvGender.text = getString(R.string.nan)
            }
            "female".lowercase() -> {
                //女
                bindView.tvGender.visible()
                bindView.tvGender.text = getString(R.string.nv)
            }

            else -> {
                bindView.tvGender.gone()
            }
        }

    }

    override fun observeCallBack() {
        //编辑用户头像
        mViewModel.editUserInfoLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (headUrl != null) {
                        MMKVUtils.getUser()?.headUrl = headUrl
                        LiveEventBus.get(EventKeys.EDIT_USER_HEAD, String::class.java).post(headUrl)
                        MMKVUtils.getUser()?.let {
                            AccountDao.saveAccount(
                                it,
                                mobile = AccountDao.getAccountMobile(),
                                password = AccountDao.getAccountPwd()
                            )
                        } //把修改的信息保存到本地账号
                        getString(R.string.编辑头像成功).toast()
                    }
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.编辑头像失败).toast()
                    }
                }
            }
        }
        /*接收修改名字 广播*/
        LiveEventBus.get(EventKeys.EDIT_USER_NAME, String::class.java).observe(this) {
            user = MMKVUtils.getUser()
//            var username = user?.username
//            if (username?.isEmpty() == true) {
//                username = user?.code
//            }
            bindView.tvUsername.text = user?.showUserName()
            bindView.tvNickName.text = user?.name
            //增加性别
            setGender()
            bindView.layoutHeader.ivHeader.loadImg(user)
        }

        /*接收ws修改个人信息同步 广播*/
        LiveEventBus.get(EventKeys.EDIT_USER, String::class.java).observe(this) {
            user = MMKVUtils.getUser()
            bindView.tvNickName.text = user?.name
            setGender()

            bindView.layoutHeader.ivHeader.loadImg(user)
        }
    }


    /**
     * 上传图片文件到谷歌云
     */
    private fun updateImageFileGoogle(localPath: String) {
        //1、上传图片
        ChatViewModel().uploadFile(localPath, "Picture", progress = {}, success = { result ->
            headUrl = result.data.filePath
            bindView.layoutHeader.ivHeader.load(headUrl)
            //提交自己头像url 到User
            mViewModel.editUserInfo(headUrl = headUrl)
        }, error = {
            getString(R.string.图片上传失败).toast()
        })
    }
}