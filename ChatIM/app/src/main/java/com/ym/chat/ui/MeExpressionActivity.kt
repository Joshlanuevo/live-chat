package com.ym.chat.ui

import android.util.Log
import android.view.View
import androidx.activity.viewModels
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.dylanc.viewbinding.binding
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.constant.EventKeys.UPDATE_GIF
import com.ym.base.ext.logE
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseActivity
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.adapter.ChatGifAdapter
import com.ym.chat.bean.EmojListBean
import com.ym.chat.databinding.ActivityMeExpressionBinding
import com.ym.chat.utils.ImageUtils
import com.ym.chat.viewmodel.MeExpressionViewModel

/**
 * 我的表情
 */
class MeExpressionActivity : BaseActivity() {
    private val bindView: ActivityMeExpressionBinding by binding()
    private val mViewModel: MeExpressionViewModel by viewModels()
    private val mAdapter = ChatGifAdapter()
    private var isEdMode = false
    private var maxGifPicture = 30 //收藏最大gif图片张数
    private var maxSelectNmb = 9 //一次最多选择数
    private var isSelectGifPicture = false //是否选择了gif图
    private var selectNmb = 0 //选择了多少张


    override fun initView() {
        bindView.loading.playAnimation()
        bindView.viewBack.click {
            if (isEdMode) {
                isEdMode = false
                bindView.fraDelRoot.gone()
                editMode(false)
            } else {
                finish()
            }
        }
        bindView.listExpress.adapter = mAdapter
        bindView.tvBj.click {
            if (isEdMode) {
                //如果已经处于编辑模式，则退出
                isEdMode = false
                bindView.tvBj.text = getString(R.string.bianji)
                bindView.fraDelRoot.gone()
                editMode(false)
            } else {
                //未处于编辑模式，进入编辑
                isEdMode = true
                bindView.tvBj.text = getString(R.string.退出编辑)
                bindView.fraDelRoot.visible()
                editMode(true)
            }
        }
        bindView.tvDelete.click {
            //删除gif
            val selectData = mAdapter.data.filter { it.isSelect }
            if (selectData.isNotEmpty()) {
                val selectIds = mutableListOf<String>().apply {
                    selectData.forEach {
                        add(it.id)
                    }
                }

                showLoading()
                mViewModel.delEmoj(selectIds)
            } else {
                getString(R.string.请先选择需要删除的表情).toast()
            }
        }

        mAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val item = mAdapter.data[position]
                //编辑模式
                if (item.isAddDefault) {
                    if (!isSelectGifPicture) {
                        var selectNum = maxGifPicture - mAdapter.data.size + 1
                        if (selectNum > maxSelectNmb) {//一次最多选择9张
                            selectNum = maxSelectNmb
                        }
                        ImageUtils.goSelImgAndGif(
                            this@MeExpressionActivity,
                            selectNum,

                        ) { localPath, w, h, time, size ->
                            //上传并保存gif到服务器
                            isSelectGifPicture = true
                            selectNmb = size
                            Log.d(
                                "上传文件",
                                "选择文件${Thread.currentThread().name}==$localPath----selectNmb=${selectNmb}"
                            )
                            showLoading()
                            mViewModel.addGif(localPath, w, h)
                        }
                    }
                } else {
                    if (isEdMode) {
                        item.isSelect = !item.isSelect
                        mAdapter.notifyItemChanged(position)
                    }
                }
            }
        })
    }

    private fun showLoading() {
        bindView.conloading.visible()
    }

    private fun hideLoading() {
        bindView.conloading.gone()
    }

    /**
     * 是否编辑模式
     */
    fun editMode(isEdit: Boolean) {
        mAdapter.data.forEach {
            it.isEditInfo = isEdit
            it.isDel = false
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun requestData() {
        showLoading()
        mViewModel.getEmojList()
    }

    override fun observeCallBack() {
        mViewModel.emojListResult.observe(this) {
            hideLoading()
            isEdMode = false
            bindView.tvBj.text = getString(R.string.bianji)
            bindView.fraDelRoot.gone()

            //表情列表
            bindView.tvCount.text = "(${it.size}/$maxGifPicture)"

            //更新聊天页面显示
            if (it.size < maxGifPicture) {
                it.add(0, EmojListBean.EmojBean(isAddDefault = true))
            }
            it.forEach { e -> e.isDel = false }
            mAdapter.setList(it)
        }
        LiveEventBus.get(EventKeys.SENDGIF).observe(this) {
            //添加gif
            if (isSelectGifPicture) {
                selectNmb--
                if (selectNmb <= 0) {
                    hideLoading()
                    isSelectGifPicture = false
                    getString(R.string.图片已上传).toast()
                }
            }
            mViewModel.getEmojList()
            Log.d(
                "上传文件",
                "LiveEventBus+${Thread.currentThread().name}----isSelectGifPicture=${isSelectGifPicture}----selectNmb=${selectNmb}"
            )

            LiveEventBus.get(UPDATE_GIF).post(null)
        }
        mViewModel.delEmojResult.observe(this) {
            //删除gif
            mViewModel.getEmojList()

            LiveEventBus.get(UPDATE_GIF).post(null)
        }

        //其他端更新了gif图片
        LiveEventBus.get(EventKeys.UPDATE_GIF_MSG, Boolean::class.java).observe(this) {
            mViewModel.getEmojList()
        }
    }
}