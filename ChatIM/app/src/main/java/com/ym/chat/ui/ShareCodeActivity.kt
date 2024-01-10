package com.ym.chat.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import coil.load
import com.dylanc.viewbinding.binding
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.ym.base.ext.copyToClipboard
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseActivity
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.chat.R
import com.ym.chat.databinding.ActivityShareCodeBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.ext.loadImg
import com.ym.chat.ext.roundLoad
import com.ym.chat.utils.BitmapUtils
import com.ym.chat.utils.ToastUtils
import com.ym.chat.utils.Utils
import com.ym.chat.viewmodel.SetViewModel


/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/18 17:45
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 我的邀请码
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ShareCodeActivity : LoadingActivity() {
    private val bindView: ActivityShareCodeBinding by binding()
    private val mViewModel = SetViewModel()

    private var url: String = ""

    override fun initView() {

        getReadWritePermissions()

        bindView.run {
            viewBack.click {
                finish()
            }
            tvTitle.text = "我的邀请码"
        }
        bindView.btnSave.pressEffectAlpha().click { BitmapUtils.saveScreenshotFromView(bindView.clView, this) }
//        bindView.btnShare.click {
//            sendImage()
//        }

        bindView.btnShare.pressEffectAlpha().click {
            url.copyToClipboard()
            ToastUtils.showToastWithImg(this@ShareCodeActivity, "${getString(R.string.已复制)}", R.drawable.ic_dialog_success) // "已复制"
        }


        bindView.tvNumber.setOnLongClickListener {
            bindView.tvNumber.text.toString().substring(4).copyToClipboard()
            ToastUtils.showToastWithImg(this@ShareCodeActivity, "${getString(R.string.已复制)}", R.drawable.ic_dialog_success) // "已复制"
            true
        }

        bindView.tvServiceId.setOnLongClickListener {
            bindView.tvServiceId.text.toString().trim().copyToClipboard()
            ToastUtils.showToastWithImg(this@ShareCodeActivity, "${getString(R.string.已复制)}", R.drawable.ic_dialog_success) // "已复制"
            true
        }
    }

    /**
     * 发送图片资源
     */
    private fun sendImage() {
//        var file: File = BitmapUtils.saveScreenshotFromViewToFile(bindView.clView, this)
//        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            FileProvider.getUriForFile(
//                this,
//                this.packageName.toString() + ".fileprovider",
//                file
//            )
//        } else {
//            Uri.fromFile(file)
//        }
//
//        // 授权给微信访问路径
//        grantUriPermission(
//            "com.tencent.mm",  // 这里填微信包名
//            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
//        )


        var imageIntent = Intent(Intent.ACTION_SEND)
        imageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

//        val resInfoList: List<ResolveInfo> = packageManager
//            .queryIntentActivities(imageIntent, PackageManager.MATCH_DEFAULT_ONLY)
//        for (resolveInfo in resInfoList) {
//            val packageName: String = resolveInfo.activityInfo.packageName
//            "分享的包名授权==$packageName"
//            grantUriPermission(
//                packageName,
//                uri,
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
//            )
//        }

        //指定分享到微信
        imageIntent.setPackage("com.tencent.mm")
//        imageIntent.type = "image/jpeg"
//        imageIntent.putExtra(Intent.EXTRA_STREAM, uri)

//        //分享到所有端
//        imageIntent.type = "image/jpeg"
//        imageIntent.putExtra(Intent.EXTRA_STREAM, uri)

        //分享文字
        // 指定发送的内容
        imageIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
        // 指定发送内容的类型
        imageIntent.type = "text/plain"
        startActivity(Intent.createChooser(imageIntent, "分享"))
    }

    /**
     * 获取权限
     */
    private fun getReadWritePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission
                (Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
    }

    override fun requestData() {
        MMKVUtils.getUser().run {
            var username = this?.username
            var recommendCode = this?.recommendCode
            var name = this?.name
            var headUrl = this?.headUrl
            bindView.tvName.text = name
            bindView.tvNumber.text = "友聊号：$username"
            bindView.tvServiceId.text = "我的推广码：$username"
//            bindView.tvInfo.text = "${name}邀请您使用友聊，在微信中长按识别二维码可下载友聊APP并完成注册登录"
            bindView.layoutHeader.ivHeader.loadImg(this)
            Utils.showDaShenImageView(
                bindView.layoutHeader.ivHeaderMark, this?.displayHead == "Y", this?.levelHeadUrl
            )

//            var url = "${MMKVUtils.getAppVersionUrl()}?params=$username"
//            var url = "https://a4.files.diawi.com/app-file/eh4Wh8ieW2WSlNDsvHxY.apk?params=$username"

            mViewModel.getRefereeLink()//获取自己友聊号秘文
        }
    }

    override fun observeCallBack() {
        //获取自己友聊号秘文 回调
        mViewModel.refereeLink.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    url = "${result.data?.link}?${result.data?.encode}"
                    //生成一个即可以用外部浏览器下载apk内部二维码扫描可以添加好友 的  二维码
                    bindView.ivCode.setImageBitmap(
                        CodeUtils.createImage(
                            url,
                            210,
                            210,
                            null
                        )
                    )
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                }
            }
        }
    }

}