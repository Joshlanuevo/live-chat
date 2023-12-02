package com.ym.chat.dialog

import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.ScreenUtils
import com.ym.base.ext.dp2Px
import com.ym.base.widget.ext.*
import com.ym.chat.databinding.DialogCommBinding
import com.ym.chat.databinding.LayoutPairBinding


class CommDialog : BaseBindFragmentDialog<DialogCommBinding>() {
    //<editor-fold defaultstate="collapsed" desc="变量">
    //是否单按钮
    var isSingleBtn: Boolean? = null

    //加粗
    var isBoldTitle: Boolean? = null
    var isBoldContent: Boolean? = null
    var isBoldCancel: Boolean? = null
    var isBoldConfirm: Boolean? = null

    //颜色
    var mColorTitle: Int? = null
    var mColorContent: Int? = null
    var mColorCancel: Int? = null
    var mColorConfirm: Int? = null

    //背景
    var mBgCancelRes: Int? = null
    var mBgConfirmRes: Int? = null

    //是否居中
    var isHorizontalTitle: Boolean? = null
    var isHorizontalContent: Boolean? = null
    var isShowClose: Boolean? = null

    //文字
    var mTxtTitle: CharSequence? = null
    var mTxtContent: CharSequence? = null
    var mTxtCancel: CharSequence? = null
    var mTxtConfirm: CharSequence? = null

    //大小
    var mTxtTitleSizeSp: Float? = null
    var mTxtContentSizeSp: Float? = null
    var mTxtCancelSizeSp: Float? = null
    var mTxtConfirmSizeSp: Float? = null

    //点击回调
    var mCallCancel: (() -> Unit)? = null
    var mCallConfirm: (() -> Unit)? = null

    //额外的展示
    var mListPairs = mutableListOf<Pair<String, String>>()
    var isPairTop: Boolean? = null
    var mPairHeightPx: Int? = null
    var mColorLeftPair: Int? = null
    var mColorRightPair: Int? = null
    var mTxtPairSizeSp: Float? = null
    var isBoldLeftPair: Boolean? = null
    var isBoldRightPair: Boolean? = null
    var isPackedPair: Boolean? = null

    //按钮是否均分
    var isBtnAverage = false
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="XML">
    override fun loadViewBinding(inflater: LayoutInflater, parent: FrameLayout) = DialogCommBinding.inflate(inflater, parent, true)
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="初始化">
    override fun initView() {
        viewBinding?.let { bind ->
            //单双按钮
            isSingleBtn?.let { b ->
                if (b) {
                    bind.tvCancel.gone()
                    bind.vLineV.gone()
                }
            }//单按钮隐藏取消，保留确认
            //加粗
            isBoldTitle?.let { if (it) bind.tvTitle.typeface = Typeface.defaultFromStyle(Typeface.BOLD) }
            isBoldContent?.let { if (it) bind.tvContent.typeface = Typeface.defaultFromStyle(Typeface.BOLD) }
            isBoldCancel?.let { if (it) bind.tvCancel.typeface = Typeface.defaultFromStyle(Typeface.BOLD) }
            isBoldConfirm?.let { if (it) bind.tvConfirm.typeface = Typeface.defaultFromStyle(Typeface.BOLD) }
            //颜色
            mColorTitle?.let { bind.tvTitle.setTextColor(it) }
            mColorContent?.let { bind.tvContent.setTextColor(it) }
            mColorCancel?.let { bind.tvCancel.setTextColor(it) }
            mColorConfirm?.let { bind.tvConfirm.setTextColor(it) }
            //大小
            mTxtTitleSizeSp?.let { bind.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
            mTxtContentSizeSp?.let { bind.tvContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
            mTxtCancelSizeSp?.let { bind.tvCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
            mTxtConfirmSizeSp?.let { bind.tvConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, it) }
            //背景
            mBgCancelRes?.let { bind.tvCancel.setBackgroundResource(it) }
            mBgConfirmRes?.let { bind.tvConfirm.setBackgroundResource(it) }
            //位置
            isHorizontalTitle?.let { if (!it) bind.tvTitle.gravity = Gravity.CENTER_VERTICAL }
            isHorizontalContent?.let { if (!it) bind.tvContent.gravity = Gravity.CENTER_VERTICAL }
            //文字
            mTxtTitle?.let { bind.tvTitle.text = it }
            mTxtContent?.let { bind.tvContent.text = it }
            mTxtCancel?.let { bind.tvCancel.text = it }
            mTxtConfirm?.let { bind.tvConfirm.text = it }
            bind.tvTitle.visibleGone(!mTxtTitle.isNullOrBlank())
            bind.tvContent.visibleGone(!mTxtContent.isNullOrBlank())
            bind.tvCancel.pressEffectAlpha()
            bind.tvConfirm.pressEffectAlpha()
            //点击回调
            bind.tvCancel.click {
                mCallCancel?.invoke()
                dismissAllowingStateLoss()
            }
            bind.tvConfirm.click {
                mCallConfirm?.invoke()
                dismissAllowingStateLoss()
            }
            bind.ivClose?.click {
                dismissAllowingStateLoss()
            }
            isShowClose?.let {
                bind.ivClose?.visibleGone(it)
            }

            //如果需要均分
            if (isBtnAverage) {
                bind.tvCancel.layoutParams.width = 0
                bind.tvConfirm.layoutParams.width = 0
                (bind.tvConfirm.layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin = 0
            }
            //额外的展示
            bind.llPairsTop.removeAllViews()
            bind.llPairsBottom.removeAllViews()
            if (mListPairs.isEmpty()) {
                bind.llPairsTop.gone()
                bind.llPairsBottom.gone()
                (bind.tvCancel.layoutParams as? ConstraintLayout.LayoutParams)?.topMargin = 17.dp2Px()
                (bind.vLineV.layoutParams as? ConstraintLayout.LayoutParams)?.topMargin = 17.dp2Px()
                (bind.tvConfirm.layoutParams as? ConstraintLayout.LayoutParams)?.topMargin = 17.dp2Px()
            } else {
                val parentPairs = if (isPairTop == true) bind.llPairsTop else bind.llPairsBottom
                parentPairs.visible()
                (bind.tvCancel.layoutParams as? ConstraintLayout.LayoutParams)?.topMargin = 0
                (bind.vLineV.layoutParams as? ConstraintLayout.LayoutParams)?.topMargin = 0
                (bind.tvConfirm.layoutParams as? ConstraintLayout.LayoutParams)?.topMargin = 0
                val layoutInflater = LayoutInflater.from(parentPairs.context)
                mListPairs.forEach { p ->
                    val vb = LayoutPairBinding.inflate(layoutInflater, null, false)
                    if (isPackedPair == true) {
                        vb.tvLeft.layoutParams.width = -2
                        vb.tvRight.layoutParams.width = -1
                        (vb.tvLeft.layoutParams as? LinearLayout.LayoutParams)?.weight = 0f
                        (vb.tvRight.layoutParams as? LinearLayout.LayoutParams)?.let { p2 ->
                            p2.weight = 1f
                            p2.width = 0
                        }
                    }
                    mPairHeightPx?.let { vb.root.layoutParams?.height = it }
                    vb.tvLeft.text = p.first
                    vb.tvRight.text = p.second
                    parentPairs.addView(vb.root)
                    mColorLeftPair?.let { vb.tvLeft.setTextColor(it) }
                    mColorRightPair?.let { vb.tvRight.setTextColor(it) }
                    mTxtPairSizeSp?.let {
                        vb.tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
                        vb.tvRight.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
                    }
                    isBoldLeftPair?.let { if (it) vb.tvLeft.typeface = Typeface.defaultFromStyle(Typeface.BOLD) }
                    isBoldRightPair?.let { if (it) vb.tvRight.typeface = Typeface.defaultFromStyle(Typeface.BOLD) }
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="生命周期">
    override fun onDestroy() {
        super.onDestroy()
        mCallCancel = null
        mCallConfirm = null
    }
    //</editor-fold>
}

//DSL style
inline fun commDialog(fragmentManager: FragmentManager, dsl: CommDialog.() -> Unit) {
    val dialog = CommDialog()
    dialog.mWidth = (ScreenUtils.getScreenWidth() * 340f / 375).toInt() + 3//3为阴影+误差
    dialog.apply(dsl).show(fragmentManager, CommDialog::class.java.name)
}