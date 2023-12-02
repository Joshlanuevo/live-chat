package com.ym.chat.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.BarUtils

/**
 * @Description
 * @Author：CASE
 * @Date：2021/3/24
 * @Time：17:55
 */
abstract class BaseBindFragmentDialog<T : ViewBinding> : BaseDialogFragment() {
    //<editor-fold defaultstate="collapsed" desc="变量">
    //ViewBinding
    var viewBinding: T? = null

    //外部是否可以点击关闭
    var canTouchOutside = true

    //是否需要背景
    var mColorBgOver = Color.parseColor("#99000000")

    //弹窗布局位置相关
    var mWidth = ViewGroup.LayoutParams.WRAP_CONTENT
    var mHeight = ViewGroup.LayoutParams.WRAP_CONTENT
    var mGravity = Gravity.CENTER
    var mOffsetX = 0//单位px
    var mOffsetY = 0//单位px

    //关闭的回调
    var mCallDismiss: (() -> Unit)? = null
    //确定的回调
    var mCallConfirmDismiss: (() -> Unit)? = null

    //根布局
    protected lateinit var mRootFrameLayout: FrameLayout
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="View创建和销毁">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val f = FrameLayout(requireContext()).also {
            it.setBackgroundColor(mColorBgOver)
            mRootFrameLayout = it
        }
        val vb = loadViewBinding(inflater, f).also { viewBinding = it }
        if (vb.root.parent == null) mRootFrameLayout.addView(vb.root)
        setMyStyle()
        return f
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let { w -> BarUtils.setStatusBarColor(w, Color.TRANSPARENT) }
        //监听返回按钮
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                onBackPress()
                if (canTouchOutside) dismissAfterAnim()
                true
            } else {
                false
            }
        }
        (viewBinding?.root?.layoutParams as? FrameLayout.LayoutParams)?.let { p ->
            p.width = mWidth
            p.height = mHeight
            p.gravity = mGravity
        }
        //设置偏移量
        viewBinding?.root?.translationX = mOffsetX * 1f
        viewBinding?.root?.translationY = mOffsetY * 1f
        //设置外部点击关闭
        mRootFrameLayout.setOnClickListener { if (canTouchOutside) dismissAfterAnim() }
        //防止点击透过去
        viewBinding?.root?.isClickable = true
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="关闭的回调">
    override fun onDismiss(dialog: DialogInterface) {
        mCallDismiss?.invoke()
        super.onDismiss(dialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCallDismiss = null
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="默认弹窗风格">
    protected fun setMyStyle() {
        //无标题
        dialog?.let { d ->
            d.requestWindowFeature(STYLE_NO_TITLE)
            d.setCanceledOnTouchOutside(canTouchOutside)
            //获取Window
            d.window?.let { window ->
                // 透明背景
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                //设置宽高
                window.decorView.setPadding(0, 0, 0, 0)
                window.attributes?.let { wlp ->
                    wlp.width = -1
                    wlp.height = -1
                    //设置对齐方式
                    wlp.gravity = Gravity.CENTER
                    window.attributes = wlp
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="点击反回">
    protected open fun onBackPress() {
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="需要执行动画后的关闭，自己重写">
    protected open fun dismissAfterAnim() {
        dismissAllowingStateLoss()
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="子类需要重新的方法">
    //XML
    abstract fun loadViewBinding(inflater: LayoutInflater, parent: FrameLayout): T

    //初始化
    abstract fun initView()
    //</editor-fold>
}