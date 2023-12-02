package com.ym.chat.utils

import android.content.Context
import androidx.core.content.ContextCompat
import coil.load
import com.airbnb.lottie.animation.content.Content
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.Utils
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.*
import com.ym.chat.databinding.LayoutMsgTopBinding
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener

/**
 * @version V1.0
 * @createAuthor
 *       ___         ___          ___
 *      /  /\       /  /\        /  /\           ___
 *     /  /::\     /  /:/       /  /::\         /__/|
 *    /  /:/\:\   /__/::\      /  /:/\:\    __  | |:|
 *   /  /:/~/::\  \__\/\:\    /  /:/~/::\  /__/\| |:|
 *  /__/:/ /:/\:\    \  \:\  /__/:/ /:/\:\ \  \:\_|:|
 *  \  \:\/:/__\/    \__\:\  \  \:\/:/__\/  \  \:::|
 *   \  \::/         /  /:/   \  \::/        \  \::|
 *    \  \:\        /__/:/     \  \:\         \  \:\
 *     \  \:\       \__\/       \  \:\         \  \:\
 *      \__\/                    \__\/          \__\/
 * @createDate  2022.3.24 4:10 下午
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
object TopUtil {

    fun showItemPoint(layoutTop: LayoutMsgTopBinding?, size: Int, index: Int) {
        when (size) {
            1 -> {
                layoutTop?.view1?.visible()
                layoutTop?.view2?.gone()
                layoutTop?.view3?.gone()
                layoutTop?.view4?.gone()
                layoutTop?.view5?.gone()
            }
            2 -> {
                layoutTop?.view1?.visible()
                layoutTop?.view2?.visible()
                layoutTop?.view3?.gone()
                layoutTop?.view4?.gone()
                layoutTop?.view5?.gone()
            }
            3 -> {
                layoutTop?.view1?.visible()
                layoutTop?.view2?.visible()
                layoutTop?.view3?.visible()
                layoutTop?.view4?.gone()
                layoutTop?.view5?.gone()
            }
            4 -> {
                layoutTop?.view1?.visible()
                layoutTop?.view2?.visible()
                layoutTop?.view3?.visible()
                layoutTop?.view4?.visible()
                layoutTop?.view5?.gone()
            }
            5 -> {
                layoutTop?.view1?.visible()
                layoutTop?.view2?.visible()
                layoutTop?.view3?.visible()
                layoutTop?.view4?.visible()
                layoutTop?.view5?.visible()
            }
        }
        when (index + 1) {
            1 -> {
                layoutTop?.view1?.setBackgroundResource(R.drawable.bg_red_10dp)
                layoutTop?.view2?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view3?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view4?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view5?.setBackgroundResource(R.drawable.bg_gray_r5)
            }
            2 -> {
                layoutTop?.view1?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view2?.setBackgroundResource(R.drawable.bg_red_10dp)
                layoutTop?.view3?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view4?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view5?.setBackgroundResource(R.drawable.bg_gray_r5)
            }
            3 -> {
                layoutTop?.view1?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view2?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view3?.setBackgroundResource(R.drawable.bg_red_10dp)
                layoutTop?.view4?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view5?.setBackgroundResource(R.drawable.bg_gray_r5)
            }
            4 -> {
                layoutTop?.view1?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view2?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view3?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view4?.setBackgroundResource(R.drawable.bg_red_10dp)
                layoutTop?.view5?.setBackgroundResource(R.drawable.bg_gray_r5)
            }
            5 -> {
                layoutTop?.view1?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view2?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view3?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view4?.setBackgroundResource(R.drawable.bg_gray_r5)
                layoutTop?.view5?.setBackgroundResource(R.drawable.bg_red_10dp)
            }
        }
    }

    fun showMsgContent(
        context: Context,
        layoutTop: LayoutMsgTopBinding?,
        data: MsgTopBean,
        index: Int
    ) {
        layoutTop?.tvIndex?.text = "${ChatUtils.getString(R.string.zhidingxiaoxi)}#${index + 1}"
        if (data?.contentType != null)
            when (data?.contentType) {
                MsgType.MESSAGETYPE_TEXT -> {
                    //文本消息
                    layoutTop?.ivMsg?.gone()
                    layoutTop?.tvContent?.text = AtUserHelper.parseAtUserLinkJx(data.content,
                        ContextCompat.getColor(context, R.color.color_at), object :
                            AtUserLinkOnClickListener {
                            override fun ulrLinkClick(str: String?) {
                            }

                            override fun atUserClick(str: String?) {
                            }

                            override fun phoneClick(str: String?) {
                            }
                        })
                }
                MsgType.MESSAGETYPE_AT -> {
                    //@消息
                    layoutTop?.ivMsg?.gone()
                    layoutTop?.tvContent?.text = AtUserHelper.parseAtUserLinkJx(data.content,
                        ContextCompat.getColor(context, R.color.color_at), object :
                            AtUserLinkOnClickListener {
                            override fun ulrLinkClick(str: String?) {
                            }

                            override fun atUserClick(str: String?) {
                            }

                            override fun phoneClick(str: String?) {
                            }
                        })
                }
                MsgType.MESSAGETYPE_PICTURE -> {
                    //图片消息
                    layoutTop?.ivMsg?.visible()
                    try {
                        val imageMsg = GsonUtils.fromJson(data.content, ImageBean::class.java)
                        layoutTop?.ivMsg?.load(imageMsg.url) {
                            placeholder(R.drawable.image_chat_placeholder)
                            error(R.drawable.image_chat_placeholder)
                        }
                    } catch (e: Exception) {
                    }
                    layoutTop?.tvContent?.text = ChatUtils.getString(R.string.tupian)
                }
                MsgType.MESSAGETYPE_VIDEO -> {
                    //视频消息
                    layoutTop?.ivMsg?.visible()
                    data.videoInfo?.apply {
                        layoutTop?.ivMsg?.load(this.coverUrl) {
                            placeholder(R.drawable.image_chat_placeholder)
                            error(R.drawable.image_chat_placeholder)
                        }
                    }
                    layoutTop?.tvContent?.text = ChatUtils.getString(R.string.视频)
                }
                MsgType.MESSAGETYPE_VOICE -> {
                    //语音消息
                    layoutTop?.ivMsg?.gone()
                    layoutTop?.tvContent?.text = ChatUtils.getString(R.string.语音消息)
                }
                MsgType.MESSAGETYPE_FILE -> {
                    //文件消息
                    layoutTop?.ivMsg?.visible()
                    layoutTop?.ivMsg?.load(R.drawable.icon_chat_file)
                    layoutTop?.tvContent?.text = "文件"
                }
            }
    }

}