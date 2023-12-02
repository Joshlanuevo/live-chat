package com.ym.chat.adapter

import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ym.base.ext.logE
import com.ym.base.widget.BaseVBHolder
import com.ym.base.widget.BaseVBQuickAdapter
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.EmojListBean
import com.ym.chat.databinding.ItemExpressGifBinding

class ChatGifAdapter : BaseVBQuickAdapter<EmojListBean.EmojBean, ItemExpressGifBinding>() {
    init {
        addChildClickViewIds(R.id.ivDel)
    }

    override fun convert(holder: BaseVBHolder<ItemExpressGifBinding>, item: EmojListBean.EmojBean) {
        holder.viewBinding.let { bindView ->
            if (item.isAddDefault) {
                //新增
                bindView.ivSelect.gone()
                bindView.ivDel.gone()
                bindView.gifLogo.gone()
//                bindView.ivGif.load(R.drawable.ic_add_emoj)
                Glide.with(context).load(R.drawable.ic_add_emoj).into(holder.viewBinding.ivGif)
            } else {
                bindView.gifLogo.visible()
                if (item.isEditInfo) {
                    //编辑模式
                    bindView.ivSelect.visible()
                    if (item.isSelect) {
                        //选中
                        bindView.ivSelect.load(R.drawable.ic_select)
                    } else {
                        //未选中
                        bindView.ivSelect.load(R.drawable.ic_unselect)
                    }
                } else {
                    //正常模式
                    bindView.ivSelect.gone()
                }
                if (item.isDel) {
                    //显示删除按钮
                    bindView.ivDel.visible()
                } else {
                    bindView.ivDel.gone()
                }
//                bindView.ivGif.load(item.url)
                if (item.url.lowercase().contains(".gif".lowercase())) {
                    bindView.gifLogo.visible()
                    Glide.with(context).asGif().load(item.url)
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
                        .into(holder.viewBinding.ivGif)
                } else {
                    bindView.gifLogo.gone()
                    Glide.with(context).load(item.url).error(R.drawable.ic_load_fail)
                        .placeholder(R.drawable.image_chat_placeholder)
                        .into(holder.viewBinding.ivGif)
                }
            }
        }
    }
}