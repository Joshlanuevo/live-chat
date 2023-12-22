package com.ym.chat.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import coil.load
import com.blankj.utilcode.util.ActivityUtils
import com.dylanc.viewbinding.binding
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.ym.base.ext.logE
import com.ym.base.ext.mContext
import com.ym.base.mvvm.BaseActivity
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.visible
import com.ym.chat.BuildConfig
import com.ym.chat.R
import com.ym.chat.databinding.ActivitySplashBinding
import com.ym.chat.utils.LanguageUtils
import com.ym.chat.utils.PlatformUtils
import com.ym.chat.viewmodel.HomeViewModel
import kotlinx.coroutines.Job

class SplashActivity : BaseActivity() {
    //<editor-fold defaultstate="collapsed" desc="测试静态模块和StartUp的启动顺序">
    companion object {
        val mSysTime = getSysTime()
        private fun getSysTime(): Long {
            "初始化完成后，第一个页面静态模块加载".logE()
            return System.currentTimeMillis()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        "初始化完成后，第一个非静态模块加载".logE()
        super.onCreate(savedInstanceState)
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="变量">
    private val bindView: ActivitySplashBinding by binding()
    private val mViewModel = HomeViewModel()

    private val SECOND = 1000L
    private val mTimeDown: TimeDown = TimeDown()
    private var is403 = false
    private var downFinish = false

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="初始化View">
    override fun initView() {
        bindView.tvAppName.text = getString(R.string.app_name)

        //显示启动页
        bindView.ivSplash.load(PlatformUtils.getSplashImage())

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="倒计时和跳过">
    override fun requestData() {
        bindView.apply {
            tvCountDown.setOnClickListener {
                mTimeDown.cancel()
                downFinish = true
                goToAct()
            }
        }
        mTimeDown.start()

        //获取oss配置
        mViewModel.getOssConfig()
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="根据网络状态去判断注册">
    override fun observeCallBack() {

    }

    override fun onDestroy() {
        super.onDestroy()
        mTimeDown.cancel()
        mJob?.cancel()
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="去下个页面的判断">
    private fun goToAct() {
        if (!checkFinish || !downFinish) {
            return
        }
        val hasOpen =
            ActivityUtils.getActivityList()?.any { ac -> ac is LoginActivity || ac is HomeActivity }
                ?: false
        if (!hasOpen) {
            if (MMKVUtils.getUser() == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }
        finish()
    }
    //</editor-fold>


    //<editor-fold defaultstate="collapsed" desc="倒计时">
    inner class TimeDown : CountDownTimer(SECOND, 1000L) {
        @SuppressLint("SetTextI18n")
        override fun onTick(millisUntilFinished: Long) {
            bindView.tvCountDown.text = "${millisUntilFinished / 1000 + 1}${getString(R.string.跳过)}"
        }

        override fun onFinish() {
            downFinish = true
            goToAct()
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="SD卡权限处理">
    override fun onResume() {
        super.onResume()
        checkSDPermission()
    }

    private var checkFinish = false
    private var mJob: Job? = null
    private var hasGoSetting = false
    private fun checkSDPermission() {
        val permissions =
            mutableListOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
        val hasSDPermission = XXPermissions.isGranted(mContext, permissions)
        //请求SD卡权限
        if (!hasSDPermission) {
            XXPermissions.with(this)
                .permission(
                    mutableListOf(
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                .request(object : OnPermissionCallback {
                    override fun onGranted(granted: MutableList<String>, all: Boolean) {
                        checkFinish = true
                        goToAct()
                    }

                    override fun onDenied(denied: MutableList<String>, never: Boolean) {
                        checkFinish = true
                        goToAct()
//                        if (never && mJob == null) {//打开APP发现被永久拒绝或者点击了永久拒绝
//                            mJob = launchError {
//                                R.string.需要存储权限.xmlToast()
//                                withContext(Dispatchers.IO) { delay(2000) }.let {
//                                    XXPermissions.startPermissionActivity(mContext, permissions)
//                                    hasGoSetting = true
//                                }
//                            }
//                        } else if (!never || (never && hasGoSetting)) {//正常拒绝直接关闭,或者从权限回来发现还是没给
//                            mTimeDown.cancel()
//                            finish()
//                        }
                    }
                })
        } else checkFinish = true
    }
    //</editor-fold>
}