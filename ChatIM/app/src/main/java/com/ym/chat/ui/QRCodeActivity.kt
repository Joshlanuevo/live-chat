package com.ym.chat.ui

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.view.Gravity
import com.blankj.utilcode.util.ToastUtils
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.tbruyelle.rxpermissions3.RxPermissions
import com.uuzuche.lib_zxing.activity.CaptureFragment
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.databinding.ActivityQrcodeBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.utils.QRCodeUtils
import com.ym.chat.viewmodel.FriendViewModel
import java.io.Serializable

class QRCodeActivity : LoadingActivity() {
    private val bindView: ActivityQrcodeBinding by binding()
    private val mViewModel = FriendViewModel()
    private var captureFragment: CaptureFragment? = null

    override fun initView() {
        RxPermissions(this).request(Manifest.permission.CAMERA)
            .subscribe { granted: Boolean ->
                if (granted) {
                    captureFragment = CaptureFragment()
                    CodeUtils.setFragmentArgs(captureFragment, R.layout.view_my_camera)
                    captureFragment?.analyzeCallback = analyzeCallback
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.flContainer, captureFragment!!).commit()
                } else {
                    getString(R.string.权限被拒绝).toast()
                }
            }

        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
            ivSearch.visible()
//            toolbar.setBackgroundResource(R.drawable.bg_conver_top)
//            ivSearch.setImageResource(R.drawable.ic_photo)
//            ivBack.setImageResource(R.drawable.ic_back_white)
//            tvTitle.setTextColor(getColor(R.color.white))
            ivSearch.click {
                val intent = Intent()
                intent.action = Intent.ACTION_PICK  //Pick an item from the data
                intent.type = "image/*"             //从所有图片中进行选择
                startActivityForResult(intent, 110)
            }
            tvTitle.text = getString(R.string.saoyisao)
        }
    }

    override fun requestData() {
    }

    override fun observeCallBack() {
        //搜索结果
        mViewModel.addFriendByEncode.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    result.exc?.message.toast()
                    captureFragment?.onResume()//重新初始化，可以重复扫描
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    if (result.data == null) {
                        captureFragment?.onResume()//重新初始化，可以重复扫描
                        getString(R.string.该用户不存在).toast()
                    } else {
                        result.data?.data?.let {
                            if (it.friendRelationFlag) {
                                val friendBean =
                                    it.friendMemberId?.let { it1 ->
                                        ChatDao.getFriendDb().getFriendById(it1)
                                    }
                                if (friendBean != null) {
                                    startActivity(
                                        Intent(this, FriendInfoActivity::class.java)
                                            .putExtra(
                                                ChatActivity.CHAT_INFO,
                                                friendBean as Serializable
                                            ).putExtra(ContactActivity.IN_TYPE, 1)
                                    )
                                }
                            } else {
                                getString(R.string.已成功添加二维码用户为好友).toast()
                                //发广播
                                LiveEventBus.get(EventKeys.ADD_FRIEND, Boolean::class.java)
                                    .post(true)
                            }
                        }

                        this.finish()
                    }
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        "----requestCode=$requestCode---${data.toString()}".logD()
        if (resultCode == RESULT_OK && requestCode == 110) {
            if (data != null) {
                val uri = data.data
                try {
                    "二维码扫描结果====$uri".logD()
                    CodeUtils.analyzeBitmap(
                        QRCodeUtils.getImageAbsolutePath(this, uri),
                        analyzeCallback
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    "----Exception=0=${e.message.toString()}".logE()
                }
            }
        }
    }

    /**
     * 二维码解析回调函数
     */
    private var analyzeCallback: CodeUtils.AnalyzeCallback = object : CodeUtils.AnalyzeCallback {
        override fun onAnalyzeSuccess(mBitmap: Bitmap, result: String) {
            "二维码扫描结果=$result".logD()
            when {
                result.isBlank() -> {
                    getString(R.string.信息解析为空).toast()
                }
                result.contains("?") -> {
                    captureFragment?.onPause()//先停止，后面再启动
                    var jx = result.split("?")
                    mViewModel.addFriendByEncode(jx[1])
                }
                else -> {
                    getString(R.string.不支持的扫描协议).toast()
                }
            }
        }

        override fun onAnalyzeFailed() {
            "解析异常".logE()
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.二维码识别异常))
        }
    }


}