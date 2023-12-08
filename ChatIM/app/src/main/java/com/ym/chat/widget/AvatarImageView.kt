package com.ym.chat.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ym.chat.R

class AvatarImageView : FrameLayout {
    private lateinit var ivHeaderUrl: ImageFilterView
    private lateinit var tvHeaderDefault: TextView
    private var roundRadius = 100F
    private var chatId = ""
    private var chatName = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_header_new, this)
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        ivHeaderUrl = findViewById(R.id.ivHeaderUrl)
        tvHeaderDefault = findViewById(R.id.tvHeaderDefault)
    }

    fun setChatId(chatId: String): AvatarImageView {
        this.chatId = chatId
        return this
    }

    fun setChatName(name: String): AvatarImageView {
        this.chatName = name
        return this
    }

    fun setRoundRadius(radius: Float): AvatarImageView {
        this.roundRadius = radius
        return this
    }

    fun showUrl(imageUrl: String?) {
        if (TextUtils.isEmpty(imageUrl) || !imageUrl!!.startsWith("http")) {
            showDefault()
        } else {
            tvHeaderDefault.visibility = GONE
            ivHeaderUrl.load(imageUrl) {
                transformations(RoundedCornersTransformation(roundRadius))//显示圆形图片
                placeholder(R.drawable.ic_mine_header)
                listener(onCancel = {}, onError = { r, e ->
                    showDefault()
                }, onSuccess = { request, metadata ->
                })
            }
        }
    }

    fun showImageRes(resId: Int) {
        tvHeaderDefault.visibility = GONE
        ivHeaderUrl.load(resId) {
            transformations(RoundedCornersTransformation(roundRadius))//显示圆形图片
        }
    }

    fun showDefault() {
        try {
            if (!TextUtils.isEmpty(chatName)) {
                tvHeaderDefault.visibility = VISIBLE
                tvHeaderDefault.text = chatName.substring(0, 1).uppercase()
            } else {
                tvHeaderDefault.text = ""
            }

            val lastStr = chatId.substring(chatId.length - 1, chatId.length)
            ivHeaderUrl.load(getImgRes(lastStr)) {
                transformations(RoundedCornersTransformation(roundRadius))//显示圆形图片
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tvHeaderDefault.text = chatName.substring(0, 1).uppercase()
        }
    }

    private fun getImgRes(lastStr: String): Int {
        return when (lastStr) {
            "0" -> R.drawable.shape_img_1
            "1" -> R.drawable.shape_img_2
            "2" -> R.drawable.shape_img_3
            "3" -> R.drawable.shape_img_4
            "4" -> R.drawable.shape_img_5
            "5" -> R.drawable.shape_img_6
            "6" -> R.drawable.shape_img_7
            "7" -> R.drawable.shape_img_8
            "8" -> R.drawable.shape_img_9
            "9" -> R.drawable.shape_img_10
            else -> R.drawable.shape_img_1
        }
    }
}