package com.ym.base.mvvm

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.util.save.MMKVUtils

open abstract class BaseActivity : AppCompatActivity() {
    var isShowNoNetDialog = false
    var isLogin = !MMKVUtils.getUserId().isNullOrBlank()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isTranslucentStatus()) {
            setTranslucentStatus()
        }

        //监听登录退出
        LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).observe(this) {
            isLogin = it
            if (!it) loginOut()
        }

        initView()

        requestData()

        if (!canScreenCapture()) {
            //禁止截屏
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            "----------本群已禁止截屏--".logD()
        }

        observeCallBack()

    }

    /**
     *
     * 用来未登录 点击事件 判断
     * 返回true 跳转到Login  返回false 继续
     * **/
    fun orGoLogin(): Boolean {
        return if (!isLogin) {
            LiveEventBus.get(EventKeys.LOGIN_OR_OUT, Boolean::class.java).post(false)
//            ARouter.getInstance().build(ArouterConstnts.LOGIN_ACT)
//                .navigation()
            true
        } else {
            false
        }
    }

    abstract fun initView()

    abstract fun requestData()

    abstract fun observeCallBack()

    open fun isTranslucentStatus(): Boolean {
        return true
    }

    /**
     * 设置状态栏透明 沉浸式
     */
    open fun setTranslucentStatus() {
        //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
        val window = window
        val decorView = window.decorView
        //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        decorView.systemUiVisibility =
            decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.TRANSPARENT
    }

    open fun loginOut() {}

    /**
     * 隐藏软键盘
     */
    open fun hideSoftKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val inputMethodManager: InputMethodManager = getSystemService(
                INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /**
     * 是否开启 点击输入框外自动收起键盘，如需关闭请重写该方法 retur false;
     * 默认为开启点击输入框外自动收起键盘
     *
     * @return
     */
    protected open fun isAutoHideKeyBord(): Boolean {
        return true
    }

    /**
     * 是否允许截屏,默认允许截屏
     * true可以截屏，false不允许截屏
     */
    open fun canScreenCapture(): Boolean {
        return true
    }

    /**
     * 处理点击软键盘之外的空白处，隐藏软件盘
     *
     * @param ev
     * @return
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (isAutoHideKeyBord()) {
                val v = currentFocus
                if (isShouldHide(v, ev)) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm?.hideSoftInputFromWindow(v!!.windowToken, 0)
                }
                return super.dispatchTouchEvent(ev)
            } else {
                return super.dispatchTouchEvent(ev)
            }
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else onTouchEvent(ev)

//        return super.dispatchTouchEvent(ev);
    }

    open fun isShouldHide(v: View?, event: MotionEvent): Boolean {
        //这里是用常用的EditText作判断参照的,可根据情况替换成其它View
        if (v != null &&
            v is EditText
        ) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.getHeight()
            val right = left + v.getWidth()
            val b = event.x > left && event.x < right && event.y > top && event.y < bottom
            return !b
        }
        return false
    }


    // Return whether touch the view.
    open fun isTouchViewOut(v: View, event: MotionEvent): Boolean {
        val l = intArrayOf(0, 0)
        v.getLocationInWindow(l)
        val left = l[0]
        val top = l[1]
        val bottom = top + v.height
        val right = left + v.width
        return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
    }

    //override fun onBackPressed() {
    //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //Android Q的bug https://blog.csdn.net/oLengYueZa/article/details/109207492
    //        finishAfterTransition()
    //    } else {
    //        super.onBackPressed()
    //    }
    //}


}