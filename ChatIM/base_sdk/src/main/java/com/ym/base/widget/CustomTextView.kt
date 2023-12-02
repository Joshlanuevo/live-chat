package com.ym.base.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.ym.base_sdk.R

/**
 * 对提示字体大小属性进行扩展
 *
 * <attr name="textSizeHint" format="dimension"></attr>
 */
class CustomTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var hintTextSize = 0.0f
    private var _textSize = 0.0f

    init {
        var typeArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomTextView, 0, 0)
        var textSizeHint = typeArray.getDimension(R.styleable.CustomTextView_textSizeHint, 0.0f)
        hintTextSize = if (textSizeHint == 0.0f) {
            textSize
        } else {
            textSizeHint
        }
        typeArray.recycle()
        _textSize = textSize

        refreshSize()
    }

    private fun refreshSize() {
        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,if (text.isEmpty()) {
            hintTextSize
        } else {
            _textSize
        })
    }

    private var textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            refreshSize()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addTextChangedListener(textWatcher)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeTextChangedListener(textWatcher)
    }

}