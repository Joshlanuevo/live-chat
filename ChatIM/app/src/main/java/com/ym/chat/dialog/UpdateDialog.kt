package com.ym.chat.dialog

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.*
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.ym.base.ext.dp2Px
import com.ym.base.ext.toast
import com.ym.base.widget.ext.*
import com.ym.chat.R
import com.ym.chat.app.LiveDataConfig
import com.ym.chat.databinding.DialogUpdateBinding
import com.ym.chat.update.YmUpdateService
import com.ym.chat.utils.PathConfig
import java.io.File


class UpdateDialog : BaseBindFragmentDialog<DialogUpdateBinding>() {
    //<editor-fold defaultstate="collapsed" desc="变量">
    //是否强制更新
    var mContent: String = ""

    //新版本
    var mNewVersion: String = ""

    //下载地址
    var mDownUrl: String = ""

    //是否强制更新
    var isForce: Boolean = false

    //取消的回调
    var mCallCancel: (() -> Unit)? = null
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="XML">
    override fun loadViewBinding(inflater: LayoutInflater, parent: FrameLayout) =
        DialogUpdateBinding.inflate(inflater, parent, true)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="下载状态提示语">
    private val mTextDownloading = "升级中..."
    private val mTextDownFail = getString(R.string.xiazaishibai)
    private val mTextDownSuc = getString(R.string.xiazaichenggong)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="初始化">
    @SuppressLint("SetTextI18n")
    override fun initView() {
        viewBinding?.let { vb ->
            vb.tvContent.text = mContent
            vb.tvCancel.visibleGone(!isForce)
            vb.tvCancel.click {
                if (vb.tvConfirm.text.toString() == mTextDownloading) {
                    getString(R.string.zhengzaishengji).toast()
                    return@click
                }
                mCallCancel?.invoke()
                NotificationUtils.cancel(mDownUrl.hashCode())
                dismissAllowingStateLoss()
            }
            vb.tvConfirm.click {
                val downLoadName = EncryptUtils.encryptMD5ToString("$mDownUrl-$mNewVersion")
                val apkName = "$downLoadName.apk"
                if (File(PathConfig.TEMP_CACHE_DIR, apkName).exists()) {
                    vb.tvConfirm.text = mTextDownSuc
                    viewBinding?.tvTitle?.invisible()
//                    viewBinding?.nsvContent?.gone()
                    viewBinding?.llProgress?.visible()
                    vb.tvConfirm.visible()
                    vb.tvCancel.visibleGone(!isForce)
                    vb.tvBottom.gone()
                    (viewBinding?.vProgress?.layoutParams as? LinearLayout.LayoutParams)?.weight =
                        100f
                    viewBinding?.tvProgress?.text = "100%"
                    viewBinding?.pbProgress?.progress = 100
                    AppUtils.installApp(File(PathConfig.TEMP_CACHE_DIR, apkName).path)
                    return@click
                }
                checkNoticePermission()
            }
            LiveDataConfig.downProgressLiveData.observe(this) { progress ->
                progress?.let { p ->
                    when {
                        p < 0 -> {
                            //下载失败
                            vb.tvConfirm.text = mTextDownFail
                            viewBinding?.tvTitle?.invisible()
//                            viewBinding?.nsvContent?.gone()
                            viewBinding?.llProgress?.gone()
                            vb.tvConfirm.visible()
                            vb.tvCancel.visibleGone(!isForce)
                            vb.tvBottom.gone()
                        }
                        p == 100f -> {
                            //下载完成
                            vb.tvConfirm.text = mTextDownSuc
                            viewBinding?.tvTitle?.invisible()
//                            viewBinding?.nsvContent?.gone()
                            viewBinding?.llProgress?.visible()
                            vb.tvConfirm.visible()
                            vb.tvCancel.visibleGone(!isForce)
                            vb.tvBottom.gone()
                            (viewBinding?.vProgress?.layoutParams as? LinearLayout.LayoutParams)?.weight =
                                100f
                            viewBinding?.tvProgress?.text = "100%"
                            viewBinding?.pbProgress?.progress = 100
                        }
                        else -> {
                            //下载中
                            (vb.vProgress.layoutParams as LinearLayout.LayoutParams).weight = p
                            vb.tvProgress.text = "${p.toInt()}%"
                            vb.pbProgress.progress = p.toInt()
                            viewBinding?.tvTitle?.invisible()
//                            viewBinding?.nsvContent?.gone()
                            viewBinding?.tvConfirm?.gone()
                            viewBinding?.tvCancel?.gone()
                            viewBinding?.llProgress?.visible()
                            viewBinding?.tvBottom?.visible()
//                            viewBinding?.tvBottom?.setTextColor(Color.parseColor("#8598FA"))
                            viewBinding?.tvBottom?.text = mTextDownloading
                        }
                    }
                }
            }
        }
    }
    //</editor-fold>

    //更新需要检查通知栏权限
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun checkNoticePermission() {
        val txt = viewBinding?.tvConfirm?.text ?: ""
        if (txt != mTextDownloading) {
            viewBinding?.tvConfirm?.text = mTextDownloading
            val value = LiveDataConfig.downProgressLiveData.value ?: -1f
            if (value < 0 || value == 100f) {
                viewBinding?.tvTitle?.invisible()
//                viewBinding?.nsvContent?.gone()
                viewBinding?.tvConfirm?.gone()
                viewBinding?.tvCancel?.gone()
                viewBinding?.llProgress?.visible()
                viewBinding?.tvBottom?.visible()
//                viewBinding?.tvBottom?.setTextColor(Color.parseColor("#8598FA"))
                viewBinding?.tvBottom?.text = mTextDownloading
                YmUpdateService.startIntent(
                    path = mDownUrl,
                    version = mNewVersion,
                    showNotification = true
                )
            } else {
                (viewBinding?.vProgress?.layoutParams as? LinearLayout.LayoutParams)?.weight = value
                viewBinding?.tvProgress?.text = "${value.toInt()}%"
                viewBinding?.pbProgress?.progress = value.toInt()
                viewBinding?.tvBottom?.text = mTextDownloading
                viewBinding?.tvTitle?.invisible()
//                viewBinding?.nsvContent?.gone()
                viewBinding?.tvConfirm?.gone()
                viewBinding?.tvCancel?.gone()
                viewBinding?.llProgress?.visible()
                viewBinding?.tvBottom?.visible()
            }
        }

        val activity = context
        if (activity !is FragmentActivity) return
        //通知栏权限
        val has = XXPermissions.isGranted(activity, Permission.NOTIFICATION_SERVICE)
        if (!has) {
            commDialog(activity.supportFragmentManager) {
                ignoreForceUpdate = true
                isSingleBtn = isForce
                mTxtTitle = "温馨提示"
                mTxtContent = "需要通知权限才能更新，是否去打开？"
                mTxtCancel = "不更新"
                mTxtConfirm = "去打开"
                mCallConfirm = {
                    XXPermissions.with(context).permission(Permission.NOTIFICATION_SERVICE)
                        .request { _, _ -> }
                }
            }
        }
    }
}

//DSL style
inline fun updateDialog(fragmentManager: FragmentManager, dsl: UpdateDialog.() -> Unit) {
    val dialog = UpdateDialog()
    LiveDataConfig.downProgressLiveData.value = null//重置，防止打开就是失败、成功
    dialog.mWidth = ScreenUtils.getScreenWidth() - (38 + 38).dp2Px()
    dialog.canTouchOutside = false
    dialog.apply(dsl).show(fragmentManager, UpdateDialog::class.java.name)
}