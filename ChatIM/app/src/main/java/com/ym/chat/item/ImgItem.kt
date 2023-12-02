package com.ym.chat.item

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import coil.util.CoilUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.dp2Px
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.ImgBean
import com.ym.chat.bean.RecordBean
import com.ym.chat.databinding.*
import com.ym.chat.ui.PictureActivity
import com.ym.chat.utils.DeviceInfoUtils
import com.ym.chat.utils.MsgType
import com.ym.chat.utils.TimeUtils
import org.json.JSONObject
import razerdp.basepopup.QuickPopupBuilder
import razerdp.basepopup.QuickPopupConfig
import razerdp.widget.QuickPopup
import java.io.File
import java.util.*

/**
 * 收藏内容显示
 * @Description
 * @Author：
 * @Time：16:33
 */
class ImgItem(context: Context,
    val onItemDelClickListener: ((rb: ImgBean) -> Unit)? = null,
    val onItemShowClickListener: ((rb: ImgBean) -> Unit)? = null
) :
    QuickViewBindingItemBinder<ImgBean, ItemImgBinding>() {


    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemImgBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemImgBinding>, data: ImgBean) {
        //显示时间
 //       holder.viewBinding..text = TimeUtils.getCollectTimeStr(data.createTime)
        var width = DeviceInfoUtils.getDeviceWidth(context)
        var layoutParams = holder.viewBinding.layoutRoot.layoutParams
        layoutParams.height = (width - (15 * 2 + 5 * 3).dp2Px()) / 4
        holder.viewBinding.ivUpload.setImageBitmap(BitmapFactory.decodeFile(data.url))
//        holder.viewBinding.ivUpload.load(File(data.content));

    }


}