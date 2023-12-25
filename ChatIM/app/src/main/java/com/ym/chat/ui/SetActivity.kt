package com.ym.chat.ui

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.CompoundButton
import androidx.core.view.ContentInfoCompat.Flags
import com.blankj.utilcode.util.SPUtils
import com.dylanc.viewbinding.binding
import com.jakewharton.processphoenix.ProcessPhoenix
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.bean.LanguageChangeEvent
import com.ym.chat.databinding.ActivitySetBinding
import com.ym.chat.dialog.CancelDialogCallback
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.service.WebsocketWork
import com.ym.chat.utils.AppManagerUtils
import com.ym.chat.utils.DataCleanManagerUtils
import com.ym.chat.utils.LanguageUtils
import com.ym.chat.viewmodel.SetViewModel
import java.util.Locale

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/19 17:32
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 设置
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class SetActivity : LoadingActivity() {
    private val bindView: ActivitySetBinding by binding()
    private val mViewModel: SetViewModel = SetViewModel()
    private var cache = "0M"
    override fun initView() {
        //toolbar设置
        bindView.titleBar.apply {
            viewBack.click { finish() }
            tvTitle.text = getString(R.string.设置)
        }

        val lang = SPUtils.getInstance().getString("SP_LANGUAGE")
        Log.d("系统语言", Locale.getDefault().language)

        if (TextUtils.isEmpty(lang)) {
            val lang = Locale.getDefault().language
            bindView.switchLang.isChecked = lang.lowercase() != "zh"
        } else if (lang.equals("ch")) {
            bindView.switchLang.isChecked = false
        } else if (lang.equals("en")) {
            bindView.switchLang.isChecked = true
        }
        initClick()
    }

    var isShowDialog = true;

    /**
     * 初始化点击事件
     */
    private fun initClick() {
        bindView.tvAppName.text = String.format(getString(R.string.关于app))
        bindView.btnLogout.click {
            HintDialog(
                getString(R.string.退出当前账号),
                getString(R.string.您确定要退出当前帐号吗),
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        mViewModel.loginOut()
                        //            //退出登录
                        MMKVUtils.clearUserInfo()
                        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
//                        "已退出登录".toast()
                        WebsocketWork.WS.close()
                    }
                }, headUrl = MMKVUtils.getUser()?.headUrl, isShowHeader = true, isTitleTxt = true
            ).show(supportFragmentManager, "HintDialog")

        }
        bindView.llSafetySet.click {
            //安全设置
            startActivity(Intent(this, SafetySetActivity::class.java))
        }
        bindView.llPrivacySet.click {
            //隐私设置
            startActivity(Intent(this, PrivacySetActivity::class.java))
        }
        bindView.llChatSet.click {
            //聊天设置
            startActivity(Intent(this, ChatSetActivity::class.java))
        }
        bindView.llSysSet.click {
            //系统设置
            startActivity(Intent(this, SystemSetActivity::class.java))
        }
        bindView.llFeedBack.click {
            //意见反馈
            startActivity(Intent(this, PrivacySetActivity::class.java))
        }

        bindView.switchLang.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if (p1) {
                    //设置英文
                    if (!isShowDialog) {
                        return
                    }
                    HintDialog(
                        getString(R.string.更换语言),
                        getString(R.string.更换语言需要重启App才能生效),
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {

                                LiveEventBus.get(EventKeys.LANGUAGE).post(LanguageChangeEvent());
                                LanguageUtils.changeLanguage(this@SetActivity, "en", "en")
                                val i = Intent(this@SetActivity, HomeActivity::class.java)
                                i.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(i)
//                                ProcessPhoenix.triggerRebirth(this@SetActivity,i)
                            }
                        },
                        headUrl = MMKVUtils.getUser()?.headUrl,
                        isShowHeader = true,
                        isTitleTxt = true,
                        cancel = object : CancelDialogCallback {
                            override fun onItemClick() {
                                isShowDialog = false
                                bindView.switchLang.isChecked = false
                                isShowDialog = true
                            }

                        }
                    ).show(supportFragmentManager, "HintDialog")
                } else {
                    if (!isShowDialog) {
                        return
                    }
                    //设置中文
                    HintDialog(
                        getString(R.string.更换语言),
                        getString(R.string.更换语言需要重启App才能生效),
                        object : ConfirmDialogCallback {
                            override fun onItemClick() {
                                LiveEventBus.get(EventKeys.LANGUAGE).post(LanguageChangeEvent())
                                LanguageUtils.changeLanguage(this@SetActivity, "zh", "cn")
                                val i = Intent(this@SetActivity, HomeActivity::class.java)
                                i.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(i)
//                                ProcessPhoenix.triggerRebirth(this@SetActivity,i);
                            }
                        },
                        headUrl = MMKVUtils.getUser()?.headUrl,
                        isShowHeader = true,
                        isTitleTxt = true,
                        cancel = object : CancelDialogCallback {
                            override fun onItemClick() {
                                isShowDialog = false
                                bindView.switchLang.isChecked = true
                                isShowDialog = true
                            }
                        }
                    ).show(supportFragmentManager, "HintDialog")
                }
            }
        })
        bindView.llClearCahce.click {
            //清理缓存
            HintDialog(
                String.format(getString(R.string.清除缓存), "${cache}"),
                getString(R.string.缓存清除后可以回收手机存储空间),
                object : ConfirmDialogCallback {
                    override fun onItemClick() {
                        getString(R.string.缓存已清理).toast()
                        //清楚缓存
                        DataCleanManagerUtils.clearAllCache(applicationContext)
                        //重新获取缓存的大小
                        cache =
                            DataCleanManagerUtils.getTotalCacheSize(applicationContext).toString()
                        bindView.tvCache.text = cache
                    }
                }, iconId = R.drawable.ic_msg_left, isShowHeader = true
            ).show(supportFragmentManager, "HintDialog")
        }
        bindView.llAboutJx.click {
            //关于友聊
            startActivity(Intent(this, AboutJxActivity::class.java))
        }
        bindView.llChange.click {
            //切换账号
            startActivity(Intent(this, AccountListActivity::class.java))
        }
    }


    override fun requestData() {
        cache = DataCleanManagerUtils.getTotalCacheSize(this).toString()
        bindView.tvCache.text = cache
        bindView.tvVersion.text =
            String.format(getString(R.string.版本), "${AppManagerUtils.getVersionName(this)}")
    }

    override fun observeCallBack() {
        //退出登录
        mViewModel.loginOutLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    //退出登录
                    MMKVUtils.clearUserInfo()
                    LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                    getString(R.string.已退出登录).toast()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    MMKVUtils.clearUserInfo()

                    LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
                    getString(R.string.已退出登录).toast()
//                    if (!TextUtils.isEmpty(result.exc?.message)) {
//                        result.exc?.message.toast()
//                    } else {
//                        "退出异常".toast()
//                    }
                }
            }
        }
    }
}