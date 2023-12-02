package com.ym.chat.item

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ClipDrawable
import android.net.Uri
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import coil.request.CachePolicy
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.binder.QuickViewBindingItemBinder
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.util.save.MMKVUtils
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.invisible
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.FileMsgBean
import com.ym.chat.bean.RecordBean
import com.ym.chat.databinding.*
import com.ym.chat.db.ChatDao
import com.ym.chat.dialog.ConfirmDialogCallback
import com.ym.chat.dialog.HintDialog
import com.ym.chat.ui.ChatActivity
import com.ym.chat.ui.ContactActivity
import com.ym.chat.ui.FriendInfoActivity
import com.ym.chat.ui.PictureActivity
import com.ym.chat.utils.*
import com.ym.chat.widget.ateditview.AtUserHelper
import com.ym.chat.widget.ateditview.AtUserLinkOnClickListener
import org.json.JSONObject
import razerdp.basepopup.QuickPopupBuilder
import razerdp.basepopup.QuickPopupConfig
import razerdp.widget.QuickPopup
import java.io.Serializable
import java.util.*

/**
 * 收藏内容显示
 * @Description
 * @Author：
 * @Time：16:33
 */
class CollectContentItem(
    val onItemDownClickListener: ((rb: RecordBean) -> Unit)? = null,
    val onItemDelClickListener: ((rb: RecordBean) -> Unit)? = null,
    val onItemAddEClickListener: ((rb: RecordBean) -> Unit)? = null,
    val onItemSendClickListener: ((rb: RecordBean) -> Unit)? = null,
    val onItemCopyClickListener: ((rb: RecordBean) -> Unit)? = null,
    val onItemEditClickListener: ((rb: RecordBean) -> Unit)? = null,
    val onPlayAudioClickListener: ((bean: RecordBean, position: Int) -> Unit)? = null,
    val onPlayVideoClickListener: ((bean: RecordBean, position: Int) -> Unit)? = null,
    val onCollectItemListener: ((bean: RecordBean) -> Unit)? = null,
    val onPictureClickListener: ((bean: RecordBean) -> Unit)? = null
) :
    QuickViewBindingItemBinder<RecordBean, ItemCollectContentBinding>() {


    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) =
        ItemCollectContentBinding.inflate(layoutInflater, parent, false)

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BinderVBHolder<ItemCollectContentBinding>, data: RecordBean) {
        //显示时间
        holder.viewBinding.tvTime.text = TimeUtils.getStringDate(data.createTime)
        showTypeView(holder, data)
        showTypeContent(holder, data)
        showState(holder, data)
    }

    /**
     * 显示发送状态
     */
    private fun showState(holder: BinderVBHolder<ItemCollectContentBinding>, data: RecordBean) {
        //显示发送状态
        when (data.sendState) {
            0 -> {
                //发送中
                try {
                    if (System.currentTimeMillis() - data.createTime.toLong() > 2 * 60 * 1000) {
                        //如果发送的时长超过2分钟，显示发送失败
                        //发送失败
                        holder.viewBinding.loadView.gone()
                        holder.viewBinding.progress.gone()
                        holder.viewBinding.ivFail.visible()
                        holder.viewBinding.consSendState.visible()
                        //消息重发
                        holder.viewBinding.ivFail.click {
                            onCollectItemListener?.invoke(data)
                        }
                    }else {
                        //发送中
                        holder.viewBinding.loadView.startAnimotion()
                        holder.viewBinding.loadView.visible()
                        holder.viewBinding.ivFail.gone()
                        if (isSendVoiceAndPicture(data))//只有视频图片类型才显示进度
                            holder.viewBinding.progress.visible()
                        else
                            holder.viewBinding.progress.gone()
                        holder.viewBinding.consSendState.visible()
                    }
                } catch (e: Exception) {
                }
            }
            2 -> {
                //发送失败
                holder.viewBinding.loadView.gone()
                holder.viewBinding.progress.gone()
                holder.viewBinding.ivFail.visible()
                holder.viewBinding.consSendState.visible()
                //消息重发
                holder.viewBinding.ivFail.click {
                    onCollectItemListener?.invoke(data)
                }
            }
            else -> {
                //发送成功
                holder.viewBinding.loadView.gone()
                holder.viewBinding.progress.gone()
                holder.viewBinding.ivFail.gone()
                holder.viewBinding.consSendState.gone()
            }
        }
    }

    /**筛选是否是图片和视频类型 */
    private fun isSendVoiceAndPicture(data: RecordBean): Boolean {
        return when (data.type.lowercase(Locale.getDefault())) {
            MsgType.MESSAGETYPE_TEXT.lowercase(Locale.getDefault()), MsgType.MESSAGETYPE_AT.lowercase(
                Locale.getDefault()
            ) -> {
                //显示text内容
                false
            }
            MsgType.MESSAGETYPE_VOICE.lowercase(Locale.getDefault()) -> {
                //显示音频内容
                false
            }
            MsgType.MESSAGETYPE_VIDEO.lowercase(Locale.getDefault()) -> {
                //显示视频内容
                true
            }
            MsgType.MESSAGETYPE_PICTURE.lowercase(Locale.getDefault()) -> {
                //显示图片内容
                true
            }
            MsgType.MESSAGETYPE_FILE.lowercase(Locale.getDefault()) -> {
                //显示文件内容
                false
            }
            else -> false
        }
    }

    /**
     * 根据类型显示不同的内容
     */
    private fun showTypeContent(
        holder: BinderVBHolder<ItemCollectContentBinding>,
        data: RecordBean
    ) {
        when (data.type.lowercase(Locale.getDefault())) {
            MsgType.MESSAGETYPE_TEXT.lowercase(Locale.getDefault()), MsgType.MESSAGETYPE_AT.lowercase(
                Locale.getDefault()
            ) -> {
                //显示text内容
                showTxtView(holder, data)
            }
            MsgType.MESSAGETYPE_VOICE.lowercase(Locale.getDefault()) -> {
                //显示音频内容
                showVoiceView(holder, data)
            }
            MsgType.MESSAGETYPE_VIDEO.lowercase(Locale.getDefault()) -> {
                //显示视频内容
                holder.viewBinding.progress.bindMsg(data)
                showVideoView(holder, data)
            }
            MsgType.MESSAGETYPE_PICTURE.lowercase(Locale.getDefault()) -> {
                //显示图片内容
                holder.viewBinding.progress.bindMsg(data)
                showPictureView(holder, data)
            }
            MsgType.MESSAGETYPE_FILE.lowercase(Locale.getDefault()) -> {
                //显示文件内容
                holder.viewBinding.progress.bindMsg(data)
                showFileView(holder, data)
            }
            else -> ""
        }

        holder.viewBinding.root.setOnLongClickListener {
            var type = PopUtils.calculatePopWindowPos(holder.viewBinding.root)
            var gravityType = when (type[0]) {
                1 -> Gravity.BOTTOM
                2 -> Gravity.TOP
                else -> Gravity.TOP
            }
            var offsetY = when (type[0]) {
                1 -> SizeUtils.dp2px(1.0f)
                2 -> type[1]
                else -> SizeUtils.dp2px(-1.0f)
            }
            showPopupWindow(
                holder.viewBinding.llContent,
                data,
                gravityType = gravityType,
                offsetY = offsetY
            )
            true
        }
    }

    /**
     * 显示文件 view
     */
    private fun showFileView(
        holder: BinderVBHolder<ItemCollectContentBinding>,
        data: RecordBean
    ) {
        try {
            val fileMsg = GsonUtils.fromJson(data.content, FileMsgBean::class.java)
            holder.viewBinding.tvName.text = fileMsg.name
            holder.viewBinding.tvSize.text = fileMsg.size
            holder.viewBinding.layoutFile.click {
                onItemDownClickListener?.invoke(data)
            }
            holder.viewBinding.layoutFile.setOnLongClickListener {
                var type = PopUtils.calculatePopWindowPos(holder.viewBinding.root)
                var gravityType = when (type[0]) {
                    1 -> Gravity.BOTTOM
                    2 -> Gravity.TOP
                    else -> Gravity.TOP
                }
                var offsetY = when (type[0]) {
                    1 -> SizeUtils.dp2px(0.0f)
                    2 -> type[1]
                    else -> SizeUtils.dp2px(-0.0f)
                }
                showPopupWindow(
                    holder.viewBinding.llContent,
                    data,
                    gravityType = gravityType,
                    offsetY = offsetY
                )
                true
            }
            val (isExit, id) = DownloadUtil.queryExist(context, fileMsg.url)
            if (isExit) {
                (holder.viewBinding.ivIcon2.drawable as ClipDrawable).level = 0
                holder.viewBinding.ivDownload.invisible()
            } else {
                (holder.viewBinding.ivIcon2.drawable as ClipDrawable).level =
                    (10000 * (1 - data.downloadProcess)).toInt()
                holder.viewBinding.ivDownload.visible()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 显示图片 view
     */
    private fun showPictureView(
        holder: BinderVBHolder<ItemCollectContentBinding>,
        data: RecordBean
    ) {
        try {
            val jsonObject = JSONObject(data.content)
            val photoUrl = jsonObject.optString("url")
            if (photoUrl.lowercase().contains(".gif".lowercase())) {
                Glide.with(context).asGif().load(photoUrl)
                    .listener(object : RequestListener<GifDrawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: GifDrawable?,
                            model: Any?,
                            target: Target<GifDrawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .error(R.drawable.ic_load_fail)
                    .placeholder(R.drawable.image_chat_placeholder)
                    .into(holder.viewBinding.ivPhoto)
            } else {
                Glide.with(context).load(photoUrl).error(R.drawable.ic_load_fail)
                    .placeholder(R.drawable.image_chat_placeholder)
                    .into(holder.viewBinding.ivPhoto)
            }
            holder.viewBinding.ivPhoto.click {
                onPictureClickListener?.invoke(data)
            }
            holder.viewBinding.ivPhoto.setOnLongClickListener {
                var type = PopUtils.calculatePopWindowPos(holder.viewBinding.root)
                var gravityType = when (type[0]) {
                    1 -> Gravity.BOTTOM
                    2 -> Gravity.TOP
                    else -> Gravity.TOP
                }
                var offsetY = when (type[0]) {
                    1 -> SizeUtils.dp2px(0.0f)
                    2 -> type[1]
                    else -> SizeUtils.dp2px(-0.0f)
                }
                showPopupWindow(
                    holder.viewBinding.llContent,
                    data,
                    gravityType = gravityType,
                    offsetY = offsetY
                )
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示视频view
     */
    private fun showVideoView(holder: BinderVBHolder<ItemCollectContentBinding>, data: RecordBean) {
        try {
            val jsonObject = JSONObject(data.content)
            val photoUrl = jsonObject.optString("coverUrl")
            holder.viewBinding.ivCoverPhoto.load(photoUrl)
            holder.viewBinding.llVideo.click {
                onPlayVideoClickListener?.invoke(data, holder.layoutPosition)
            }
            holder.viewBinding.llVideo.setOnLongClickListener {
                var type = PopUtils.calculatePopWindowPos(holder.viewBinding.root)
                var gravityType = when (type[0]) {
                    1 -> Gravity.BOTTOM
                    2 -> Gravity.TOP
                    else -> Gravity.TOP
                }
                var offsetY = when (type[0]) {
                    1 -> SizeUtils.dp2px(1.0f)
                    2 -> type[1]
                    else -> SizeUtils.dp2px(-1.0f)
                }
                showPopupWindow(
                    holder.viewBinding.llContent,
                    data,
                    gravityType = gravityType,
                    offsetY = offsetY
                )
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示音频view
     */
    private fun showVoiceView(holder: BinderVBHolder<ItemCollectContentBinding>, data: RecordBean) {
        try {
            val jsonObject = JSONObject(data.content)
            val time = jsonObject.optLong("time")
            holder.viewBinding.tvDuration.text = "${time}''"
            holder.viewBinding.llAudio.click {
                onPlayAudioClickListener?.invoke(data, holder.adapterPosition)
            }
            holder.viewBinding.llAudio.setOnLongClickListener {
                var type = PopUtils.calculatePopWindowPos(holder.viewBinding.root)
                var gravityType = when (type[0]) {
                    1 -> Gravity.BOTTOM
                    2 -> Gravity.TOP
                    else -> Gravity.TOP
                }
                var offsetY = when (type[0]) {
                    1 -> SizeUtils.dp2px(1.0f)
                    2 -> type[1]
                    else -> SizeUtils.dp2px(-1.0f)
                }
                showPopupWindow(
                    holder.viewBinding.llContent,
                    data,
                    gravityType = gravityType,
                    offsetY = offsetY
                )
                true
            }
            if (data.isPlaying) {
                val animation = holder.viewBinding.ivAudio.background as? AnimationDrawable
                if (animation?.isRunning == false) {
                    animation.start()
                }
            } else {
                holder.viewBinding.ivAudio.background = null
                holder.viewBinding.ivAudio.setBackgroundResource(R.drawable.audio_animation_left_list)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 显示文本view
     */
    private fun showTxtView(holder: BinderVBHolder<ItemCollectContentBinding>, data: RecordBean) {
        holder.viewBinding.tvText.movementMethod = LinkMovementMethod.getInstance()//不设置点击会失效
        holder.viewBinding.tvText.text = AtUserHelper.parseAtUserLinkJx(data.content,
            ContextCompat.getColor(context, R.color.color_at), object : AtUserLinkOnClickListener {
                override fun ulrLinkClick(str: String?) {
                    str?.let {
                        val linkUrl = if (str.contains("http") || str.contains("https")) {
                            str
                        } else {
                            "http://$str"
                        }
                        try {
                            val uri: Uri = Uri.parse(linkUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            context.getString(R.string.cuowudelianjiedizhi).toast()
                        }
                    }
                }

                override fun atUserClick(str: String?) {
                }

                override fun phoneClick(str: String?) {
                }
            })

        holder.viewBinding.tvText.setOnLongClickListener {
            var type = PopUtils.calculatePopWindowPos(holder.viewBinding.root)
            var gravityType = when (type[0]) {
                1 -> Gravity.BOTTOM
                2 -> Gravity.TOP
                else -> Gravity.TOP
            }
            var offsetY = when (type[0]) {
                1 -> SizeUtils.dp2px(1.0f)
                2 -> type[1]
                else -> SizeUtils.dp2px(-1.0f)
            }
            showPopupWindow(
                holder.viewBinding.llContent,
                data,
                1,
                gravityType = gravityType,
                offsetY = offsetY
            )
            true
        }
    }

    /**
     * 根据类型显示界面
     *
     */
    private fun showTypeView(holder: BinderVBHolder<ItemCollectContentBinding>, data: RecordBean) {
        when (data.type.lowercase(Locale.getDefault())) {
            MsgType.MESSAGETYPE_TEXT.lowercase(Locale.getDefault()), MsgType.MESSAGETYPE_AT.lowercase(
                Locale.getDefault()
            ) -> {
                //显示text内容
                holder.viewBinding.tvText.visible()
                holder.viewBinding.llAudio.gone()
                holder.viewBinding.llVideo.gone()
                holder.viewBinding.ivPhoto.gone()
                holder.viewBinding.layoutFile.gone()
                holder.viewBinding.consSendState.gone()
            }
            MsgType.MESSAGETYPE_VOICE.lowercase(Locale.getDefault()) -> {
                //显示音频内容
                holder.viewBinding.tvText.gone()
                holder.viewBinding.llAudio.visible()
                holder.viewBinding.llVideo.gone()
                holder.viewBinding.ivPhoto.gone()
                holder.viewBinding.layoutFile.gone()
                holder.viewBinding.consSendState.gone()
            }
            MsgType.MESSAGETYPE_VIDEO.lowercase(Locale.getDefault()) -> {
                //显示视频内容
                holder.viewBinding.tvText.gone()
                holder.viewBinding.llAudio.gone()
                holder.viewBinding.llVideo.visible()
                holder.viewBinding.ivPhoto.gone()
                holder.viewBinding.layoutFile.gone()
                holder.viewBinding.consSendState.gone()
            }
            MsgType.MESSAGETYPE_PICTURE.lowercase(Locale.getDefault()) -> {
                //显示图片内容
                holder.viewBinding.tvText.gone()
                holder.viewBinding.llAudio.gone()
                holder.viewBinding.llVideo.gone()
                holder.viewBinding.ivPhoto.visible()
                holder.viewBinding.layoutFile.gone()
                holder.viewBinding.consSendState.gone()
            }
            MsgType.MESSAGETYPE_FILE.lowercase(Locale.getDefault()) -> {
                //显示文件内容
                holder.viewBinding.tvText.gone()
                holder.viewBinding.llAudio.gone()
                holder.viewBinding.llVideo.gone()
                holder.viewBinding.ivPhoto.gone()
                holder.viewBinding.layoutFile.visible()
                holder.viewBinding.consSendState.gone()
            }
            else -> ""
        }
    }

    /**
     * 显示更多菜单
     */
    private var mMorePopUpWindow: QuickPopup? = null
    private fun showPopupWindow(
        view: View, data: RecordBean, type: Int = 0,
        gravityType: Int = 0,
        offsetX: Float = -20.0f,
        offsetY: Int = 0
    ) {
        mMorePopUpWindow = QuickPopupBuilder.with(context)
            .contentView(R.layout.popup_collect)
            .config(
                QuickPopupConfig().apply {
                    if (type == 1)
                        offsetX(view.width / 2 - SizeUtils.dp2px(60.0f))
                    else
                        offsetX(SizeUtils.dp2px(20.0f))
                    offsetY(offsetY)
                    gravity(gravityType)
                    backgroundColor(Color.TRANSPARENT)
                    withClick(R.id.tvEdit) {
                        //编辑
                        onItemEditClickListener?.invoke(data)
                        mMorePopUpWindow?.dismiss()
                    }
                    withClick(R.id.tvCopy) {
                        //复制
                        onItemCopyClickListener?.invoke(data)
                        mMorePopUpWindow?.dismiss()
                    }
                    withClick(R.id.tvSend) {
                        //转发
                        onItemSendClickListener?.invoke(data)
                        mMorePopUpWindow?.dismiss()
                    }
                    withClick(R.id.tvDel) {
                        //删除
                        onItemDelClickListener?.invoke(data)
                        mMorePopUpWindow?.dismiss()
                    }
                    withClick(R.id.tvAddE) {
                        //添加表情
                        onItemAddEClickListener?.invoke(data)
                        mMorePopUpWindow?.dismiss()
                    }
                }
            ).build()
        mMorePopUpWindow?.showPopupWindow(view)
        var tvEdit = mMorePopUpWindow?.findViewById<TextView>(R.id.tvEdit)
        var tvCopy = mMorePopUpWindow?.findViewById<TextView>(R.id.tvCopy)
        var tvAddE = mMorePopUpWindow?.findViewById<TextView>(R.id.tvAddE)
        //只能是管理员账号且是文本消息(@消息)才能复制
        if (MMKVUtils.isAdmin() && (data.type.lowercase() == MsgType.MESSAGETYPE_TEXT.lowercase(
                Locale.getDefault()
            ) || data.type.lowercase() == MsgType.MESSAGETYPE_AT.lowercase(Locale.getDefault()))
        ) {
            tvCopy?.visible()
        }
        //只有文本消息 @消息才能编辑
        if (data.type.lowercase() == MsgType.MESSAGETYPE_TEXT.lowercase(Locale.getDefault()) || data.type.lowercase() == MsgType.MESSAGETYPE_AT.lowercase(
                Locale.getDefault()
            )
        ) {
            tvEdit?.visible()
        }
        //只有gif图片才有添加表情
        try {
            val jsonObject = JSONObject(data.content)
            val photoUrl = jsonObject.optString("url")
            if (PatternUtils.isImageUrlMatcher(photoUrl.lowercase())) {
                tvAddE?.visible()
            }
        } catch (e: Exception) {
        }
    }

}