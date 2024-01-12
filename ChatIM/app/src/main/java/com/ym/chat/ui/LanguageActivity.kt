package com.ym.chat.ui

import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.CompoundButton
import com.blankj.utilcode.util.SPUtils
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.bean.LanguageChangeEvent
import com.ym.chat.databinding.ActivityLanguageBinding
import com.ym.chat.databinding.FragmentMineBinding
import com.ym.chat.databinding.ActivitySetBinding
import com.ym.chat.dialog.CancelDialogCallback
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.rxhttp.UserRepository
import com.ym.chat.service.WebsocketWork
import com.ym.chat.utils.AppManagerUtils
import com.ym.chat.utils.DataCleanManagerUtils
import com.ym.chat.utils.LanguageUtils
import com.ym.chat.viewmodel.SetViewModel
import java.util.Locale

class LanguageActivity : LoadingActivity() {
    private val bindView: FragmentMineBinding by binding()
    private val mViewModel: SetViewModel = SetViewModel()
    private var cache = "0M"
    private var currentLanguage: String = "" // Declare currentLanguage as a class-level property

    override fun initView() {
//        bindView.titleBar.apply {
//            viewBack.click { finish() }
//            tvTitle.text = getString(R.string.语言设置)
//        }
        initClick()
//        setupLanguageChange()
    }

//    private fun setupLanguageChange() {
//        val appLocale = LanguageUtils.getAppLocale(this)
//        currentLanguage = appLocale.language // Assign the value to the class-level property
//
//        // Uncheck all radio buttons initially
//        bindView.chineseRadioButton.isChecked = false
//        bindView.englishRadioButton.isChecked = false
//        bindView.vietnameseRadioButton.isChecked = false
//
//        // Check the radio button based on the current app language
//        when (currentLanguage) {
//            "zh" -> bindView.chineseRadioButton.isChecked = true
//            "en" -> bindView.englishRadioButton.isChecked = true
//            "vi" -> bindView.vietnameseRadioButton.isChecked = true
//        }
//
//        bindView.chineseRadioButton.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked && currentLanguage != "zh") {
//                changeAppLanguage("zh", "cn")
//                // Uncheck other radio buttons
//                bindView.englishRadioButton.isChecked = false
//                bindView.vietnameseRadioButton.isChecked = false
//            }
//        }
//
//        bindView.englishRadioButton.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked && currentLanguage != "en") {
//                changeAppLanguage("en", "us")
//                // Uncheck other radio buttons
//                bindView.chineseRadioButton.isChecked = false
//                bindView.vietnameseRadioButton.isChecked = false
//            }
//        }
//
//        bindView.vietnameseRadioButton.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked && currentLanguage != "vi") {
//                changeAppLanguage("vi", "VN")
//                // Uncheck other radio buttons
//                bindView.chineseRadioButton.isChecked = false
//                bindView.englishRadioButton.isChecked = false
//            }
//        }
//    }

//    private fun changeAppLanguage(language: String, country: String) {
//        HintDialog(
//            getString(R.string.更换语言),
//            getString(R.string.更换语言需要重启App才能生效),
//            object : ConfirmDialogCallback {
//                override fun onItemClick() {
//                    LanguageUtils.changeLanguage(this@LanguageActivity, language, country)
//                    restartApp()
//                }
//            },
//            headUrl = MMKVUtils.getUser()?.headUrl,
//            isShowHeader = true,
//            isTitleTxt = true,
//            cancel = object : CancelDialogCallback {
//                override fun onItemClick() {
//                    // Uncheck other radio buttons except the one corresponding to the current app language
//                    if (currentLanguage != "zh") {
//                        bindView.chineseRadioButton.isChecked = false
//                    } else {
//                        bindView.chineseRadioButton.isChecked = true
//                    }
//                    if (currentLanguage != "en") {
//                        bindView.englishRadioButton.isChecked = false
//                    } else {
//                        bindView.englishRadioButton.isChecked = true
//                    }
//                    if (currentLanguage != "vi") {
//                        bindView.vietnameseRadioButton.isChecked = false
//                    } else {
//                        bindView.vietnameseRadioButton.isChecked = true
//                    }
//                }
//            }
//        ).show(supportFragmentManager, "HintDialog")
//    }

    private fun showChangeLanguageDialog(language: String, country: String) {
        HintDialog(
            getString(R.string.更换语言),
            getString(R.string.更换语言需要重启App才能生效),
            object : ConfirmDialogCallback {
                override fun onItemClick() {
                    LanguageUtils.changeLanguage(this@LanguageActivity, language, country)
                    restartApp()
                }
            },
            headUrl = MMKVUtils.getUser()?.headUrl,
            isShowHeader = true,
            isTitleTxt = true,
            cancel = object : CancelDialogCallback {
                override fun onItemClick() {
                    // Handle cancellation if needed
                }
            }
        ).show(supportFragmentManager, "HintDialog")
    }

    private fun restartApp() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // If using ProcessPhoenix
        // ProcessPhoenix.triggerRebirth(this, intent)
    }

    private fun initClick() {
//        bindView.tvChinese.setOnClickListener {
//            showChangeLanguageDialog("zh", "cn")
//        }
//
//        bindView.tvEnglish.setOnClickListener {
//            showChangeLanguageDialog("en", "us")
//        }
//
//        bindView.tvVietnamese.setOnClickListener {
//            showChangeLanguageDialog("vi", "VN")
//        }
        // Set up click listeners for other views
//        bindView.llLanguage.click {
//            // Navigate to LanguageActivity
//        }
        // Individual click listeners for each flag
//        bindView.ivChinese.setOnClickListener {
//            changeAppLanguage("zh", "cn")
//        }
//
//        bindView.ivEnglish.setOnClickListener {
//            changeAppLanguage("en", "us")
//        }
//
//        bindView.ivVietnamese.setOnClickListener {
//            changeAppLanguage("vi", "VN")
//        }
    }

    override fun requestData() {
        UserRepository.refreshUserInfo()
    }

    override fun observeCallBack() {
        // Observe ViewModel LiveData and handle responses
        mViewModel.loginOutLiveData.observe(this) { result ->
            // Handle the result
        }
        // Other LiveData observations...
    }
}
