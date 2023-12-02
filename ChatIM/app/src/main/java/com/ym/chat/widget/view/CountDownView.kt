package com.ym.chat.widget.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.ym.chat.R

class CountDownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var millisInFuture: Int = 30000
    var interval: Int = 1000
    var contentColor: Int = R.color.text_red
    var contentSize: Float = 16f
    var content: String = "获取验证码"
    var countDown: TimeDown
    var tvContent: TextView
    private lateinit var clickListener: SendClickListener

    init {
        View.inflate(context, R.layout.countdown, this)
        //获取自定义属性
        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.CountDownView)
            content = a.getString(R.styleable.CountDownView_contentText)!!
            contentColor =
                a.getColor(R.styleable.CountDownView_contentColor, Color.RED)
            contentSize = a.getDimension(R.styleable.CountDownView_contentSize, 16f)
            millisInFuture = a.getInt(R.styleable.CountDownView_duration, 30000)
            interval = a.getInt(R.styleable.CountDownView_interval, 1000)
            a.recycle()
        }
        countDown = TimeDown(millisInFuture.toLong(), interval.toLong())

        tvContent = findViewById<TextView>(R.id.tvContent)
        tvContent.setOnClickListener {
            clickListener.onSendClick()
        }
        setTextColor(contentColor)
        setTextSize(contentSize)
        setText(content)

    }

    private fun setText(content: String) {
        tvContent.text = content
    }

    private fun setTextSize(contentSize: Float) {
        Log.e("setTextSize", "$contentSize")
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentSize)
    }

    private fun setTextColor(contentColor: Int) {
        tvContent.setTextColor(contentColor)
    }

    fun startTimer() {
        countDown.start()
    }

    fun cancelTimer() {
        countDown.cancel()
    }

    inner class TimeDown(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            tvContent.isEnabled = false
            tvContent.text = "${millisUntilFinished / 1000}S"
        }

        override fun onFinish() {
            tvContent.isEnabled = true
            tvContent.text = "重新获取"
        }
    }

    override fun onDetachedFromWindow() {
        countDown.cancel()
        super.onDetachedFromWindow()
    }

    fun setClickListener(onClickListener: SendClickListener){
        clickListener = onClickListener
    }

}

interface SendClickListener{
    fun onSendClick()
}