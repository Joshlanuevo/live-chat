package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logE
import com.ym.base.ext.mContext
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ConversationBean
import com.ym.chat.bean.LanguageChangeEvent
import com.ym.chat.databinding.ActivityHomeBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.popup.HomeAddPopupWindow
import com.ym.chat.service.WebsocketServiceManager
import com.ym.chat.ui.fragment.ContactFragment
import com.ym.chat.ui.fragment.MineFragment
import com.ym.chat.ui.fragment.MovementFragment
import com.ym.chat.ui.fragment.MsgFragment
import com.ym.chat.utils.*
import com.ym.chat.viewmodel.HomeViewModel
import com.ym.chat.viewmodel.SetViewModel

class HomeActivity : LoadingActivity() {
    private val bindView: ActivityHomeBinding by binding()
    private val mViewModel = HomeViewModel()
    private val mSetViewModel = SetViewModel()

    var mFragments = mutableListOf<Fragment>()
    var mIndex = 0
    var page = -1
    var mLastCheckedId = R.id.item_bottom_1
    override fun initView() {
    }

    //<editor-fold defaultstate="collapsed" desc="联系人同步">

    override fun requestData() {

        //开启websocket服务
        WebsocketServiceManager.connect(this)

        getCount()

        //初始化首页Fragment数据
        mFragments.apply {
            add(MsgFragment())
            add(ContactFragment())
            add(MovementFragment())
            add(MineFragment())
        }

        supportFragmentManager.beginTransaction().add(R.id.frameLayout, mFragments[0])
            .commitAllowingStateLoss()
//        bindView.tvTitle.text = "友聊"
        setRadioGroupListener()

        //获取版本号
        mSetViewModel.getAppVersion()

        mViewModel.getCollectData()//更新最新一条收藏数据
        "-----本手机尺寸---1dp=${dip2px(this, 1f)}px".logE()
    }

    // 根据手机的分辨率从 dp 的单位 转成为 px(像素)
    fun dip2px(context: Context, dpValue: Float): Float {
        // 获取当前手机的像素密度（1个dp对应几个px）
        val scale: Float = context.getResources().getDisplayMetrics().density
        return (dpValue * scale + 0.5f)
    }

    // 根据手机的分辨率从 px(像素) 的单位 转成为 dp
    fun px2dip(context: Context, pxValue: Float): Float {
        // 获取当前手机的像素密度（1个dp对应几个px）
        val scale: Float = context.getResources().getDisplayMetrics().density
        return (pxValue / scale + 0.5f)
    }


    override fun onResume() {
        super.onResume()
        isStartGame = false
    }

    private var beforeX: Float = 0.0f
    private var beforeY: Float = 0.0f
    private var isStartGame = false

    private fun getCount() {
        val count = ChatDao.getConversationDb().getConverunReadCount()
        if (count > 0) {
            bindView.msgCount.visible()
            bindView.msgCount.setText(if (count > 99) "99+" else count.toString())
        } else {
            bindView.msgCount.setText("")
            bindView.msgCount.gone()
        }
    }

    private var wsState = ImConnectSatus.CONNECTING
    override fun observeCallBack() {
        mSetViewModel.appVersion.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }

                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
//                    result.exc?.message.toast()
                    result.exc?.message.logE()
                }

                is BaseViewModel.LoadState.Success -> {
                    hideLoading()

                    result.data?.let {
                        MMKVUtils.saveAppVersionUrl(it.downloadUrl)//保存app最新版本的下载地址
                        val version = AppManagerUtils.getVersionName(this)//获取当前app的版本号
                        //判断版本号是否合法
                        if (version?.isNotBlank() == true && it.versionNo.isNotBlank()) {
                            //当前版本跟服务器版本比较
                            if (Utils.isNewAppVersion(version, result.data?.versionNo!!)) {
                                //有最新版本 弹框提示下载
                                if (it.mustUpdate == "Y") {
                                    //如果是强制更新
                                    Utils.showUpdateDialog(it, supportFragmentManager)
                                } else {
                                    //如果不是强制更新
                                    if (!MMKVUtils.isAppVersion(TimeUtils.getStringDateShort())) {//今天是否已经弹框
                                        Utils.showUpdateDialog(it, supportFragmentManager)
                                        MMKVUtils.saveToday(TimeUtils.getStringDateShort())//记录今天的日期
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LiveEventBus.get(EventKeys.WS_STATUS, ImConnectSatus::class.java).observe(this) {
            //连接状态回调
            wsState = it

            initTitleStr()
        }

        LiveEventBus.get(EventKeys.LANGUAGE, LanguageChangeEvent::class.java).observe(this) {
            //语言切换
            ActivityCompat.recreate(this);//重新创建activity
        }

        //会话列表数据更新
        LiveEventBus.get(EventKeys.UPDATE_CONVER, ConversationBean::class.java)
            .observe(this) { conver ->
                if (ImCache.isUpdateNotifyMsg) {
                    getCount()
                }
            }

//        LiveEventBus.get(EventKeys.DELETE_NOTIFY_MSG, String::class.java).observe(this) {
//            getCount()
//        }

        //更新通知数量
        LiveEventBus.get(EventKeys.UPDATE_COUNT, String::class.java).observe(this) {
            getCount()
        }

//        LiveEventBus.get(EventKeys.GET_HIS_START, String::class.java).observe(this) {
//            showLoading(cancelTouch = false, "")
//        }
//
//        LiveEventBus.get(EventKeys.GET_HIS_COMPLETE, String::class.java).observe(this) {
//            hideLoading()
//        }
    }

    private fun setIndexSelected(index: Int) {
        checkUpdate()
        if (mIndex == index) {
            return
        }
        page = index
        val ft = supportFragmentManager.beginTransaction()
        //隐藏
        ft.hide(mFragments[mIndex])
        //判断是否添加
        if (!mFragments[index].isAdded) {
            ft.add(R.id.frameLayout, mFragments[index]).show(mFragments[index])
        } else {
            ft.show(mFragments[index])
        }
        ft.commitAllowingStateLoss()
        //再次赋值
        mIndex = index
    }

    override fun loginOut() {
        ActivityUtils.finishAllActivities()
        mContext.startActivity(Intent(mContext, LoginActivity::class.java))
    }

    private fun setRadioGroupListener() {
        bindView.apply {
            radioGroupButton.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.item_bottom_1 -> {
                        setIndexSelected(0)
                    }

                    R.id.item_bottom_2 -> {
                        setIndexSelected(1)
                    }

                    R.id.item_bottom_3 -> {
                        setIndexSelected(2)
                    }

                    R.id.item_bottom_4 -> {
                        setIndexSelected(3)
                    }
                }
                initTitleStr()
                mLastCheckedId = checkedId
            }
        }
    }

    /**
     * 初始化title显示
     */
    private fun initTitleStr() {
//        when (wsState) {
//            ImConnectSatus.CONNECTING -> {
//                //连接中
//                bindView.tvTitle.text = "正在连接..."
//            }
//            ImConnectSatus.SUCCESS -> {
//                //连接成功
//                when (mIndex) {
//                    0 -> {
//                        bindView.tvTitle.text = "友聊"
//                    }
//                    1 -> {
//                        bindView.tvTitle.text = "好友"
//                    }
//                    2 -> {
//                        bindView.tvTitle.text = "我的"
//                    }
//                }
//                mViewModel.getCollectData()//更新最新一条收藏数据
//            }
//            ImConnectSatus.CONNECT_FAIL -> {
//                //连接失败
//                bindView.tvTitle.text = "连接失败"
//            }
//            ImConnectSatus.CLOSE -> {
//                //连接已断开
////                bindView.tvTitle.text = "未连接"
//                if (bindView.tvTitle.text != "连接中...") {
//                    bindView.tvTitle.text = "连接中..."
//                }
//            }
//            ImConnectSatus.NET_ERROR -> {
//                //网络已断开
//                bindView.tvTitle.text = "无网络"
//            }
//            ImConnectSatus.RECONNECT -> {
//                //自动重连中
//                if (bindView.tvTitle.text != "连接中...") {
//                    bindView.tvTitle.text = "连接中..."
//                }
//            }
//        }
    }

    private fun showPopupWindows() {
        val popupWindow = HomeAddPopupWindow(this)
//        popupWindow.showPopup(bindView.ivAdd, 200, 4, Gravity.BOTTOM)
    }

    private fun checkUpdate() {
        mViewModel.checkUpdate(false)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
    }

}