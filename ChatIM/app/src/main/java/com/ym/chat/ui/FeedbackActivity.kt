package com.ym.chat.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.util.Log
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseBinderAdapter
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.chat.R
import com.ym.chat.bean.ImgAddBean
import com.ym.chat.bean.ImgBean
import com.ym.chat.config.FeedBackUrl
import com.ym.chat.databinding.ActivityFeedbackBinding
import com.ym.chat.item.ImgAddItem
import com.ym.chat.item.ImgItem
import com.ym.chat.utils.ImageUtils
import com.ym.chat.viewmodel.FeedbackModel


/**
 * 建议反馈界面
 */
class FeedbackActivity : LoadingActivity() {
    private val bindView: ActivityFeedbackBinding by binding()
    private val mViewModel = FeedbackModel()
    private val mAdapter = BaseBinderAdapter()
    private var collects: MutableList<Any> = mutableListOf()
    private var imgList: MutableList<String> = mutableListOf()
    private val MAX_PICTURE_COUNT = 5 //最大选择图片数
    private val MAX_CONTENT_COUNT = 300
    private var type = "Suggest"
    override fun initView() {
        Log.e("FeedbackActivity", "initView")
        bindView.toolbar.run {
            viewBack.click {
                FeedBackUrl.urls.clear()
                finish()
            }
            tvTitle.text = getString(R.string.建议反馈)
        }
        initRadioGroup()
        initEditText()
        initRVCollect()
        initListener()
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        Log.e("FeedbackActivity", "onSaveInstanceState outPersistentState")
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        Log.e("FeedbackActivity", "onSaveInstanceState outPersistentState")
        super.onSaveInstanceState(outState, outPersistentState)
//        outState.putStringArrayList("urls", urls)

    }


    private fun initListener() {
        bindView.btnCancel.click {
            FeedBackUrl.urls.clear()
            finish()
        }
        bindView.btnSave.click {
            val content = bindView.etContent.text.toString().trim()
            if (content.isEmpty()) {
                getString(R.string.请输入反馈内容).toast()
                return@click
            }
            showLoading()
            if (collects.isNotEmpty() && collects.size > 1) {
                imgList.clear()
                for (bean in collects) {
                    if (bean is ImgBean)
                        updateImageFileGoogle(bean.url)
                }
            } else {
                sendFeedBack()
            }
        }
    }


    override fun requestData() {
        Log.e("addImgs", "")
        if (FeedBackUrl.urls.isNotEmpty()) {
            collects.add(ImgAddBean())
            for (url in FeedBackUrl.urls) {
                addImgsInit(ImgBean(url))
            }
        } else {
            addImgs(ImgBean())
        }
    }

    private fun initRadioGroup() {
        bindView.rgType.apply {
            setOnCheckedChangeListener { group, checkedId ->
                run {
                    if (group.checkedRadioButtonId == checkedId) {
                        when (checkedId) {
                            group[0].id -> {
                                type = "Suggest"
                            }
                            group[1].id -> {
                                type = "Error"
                            }
                            group[2].id -> {
                                type = "Other"
                            }
                        }
                    }
                }
            }
            val child = get(0)
            if (child is RadioButton) {
                child.isChecked = true
            }
        }


    }

    private fun initEditText() {
        bindView.etContent.doAfterTextChanged {
            try {
                it?.let {
                    bindView.tvCountLimit.text = "${it.length}/$MAX_CONTENT_COUNT"
                    if (it.length > MAX_CONTENT_COUNT) {
                        val content = it.delete(MAX_CONTENT_COUNT, it.length)
                        bindView.etContent.text = content
                        bindView.etContent.setSelection(content.length)
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRVCollect() {
        addItemBinder()
        bindView.rvImg.layoutManager = GridLayoutManager(this@FeedbackActivity, 4);
        bindView.rvImg.adapter = mAdapter
    }


    private fun addImgsInit(imgBean: ImgBean) {

        if (collects.isEmpty()) {
            Log.e("addImgs", "collects.isEmpty()")
            collects.add(ImgAddBean())
        } else {
            Log.e("addImgs", "collects.notEmpty()")
            collects.add(collects.lastIndex, imgBean)
        }
        mAdapter.setList(collects)
        bindView.tvImgCount.text = "(${collects.size - 1}/5)"
    }


    private fun addImgs(imgBean: ImgBean) {

        if (collects.isEmpty()) {
            Log.e("addImgs", "collects.isEmpty()")
            collects.add(ImgAddBean())
        } else {
            Log.e("addImgs", "collects.notEmpty()")
            collects.add(collects.lastIndex, imgBean)
            FeedBackUrl.urls.add(imgBean.url)
        }
        mAdapter.setList(collects)
        bindView.tvImgCount.text = "(${collects.size - 1}/5)"
    }


    private fun addItemBinder() {
        mAdapter.addItemBinder(ImgItem(this@FeedbackActivity, onItemDelClickListener = {

        }, onItemShowClickListener = {

        }))
            .addItemBinder(ImgAddItem(onItemAddClickListener = {
                if (collects.size > MAX_PICTURE_COUNT) {
                    getString(R.string.最多只能上传5张).toast()
                    return@ImgAddItem
                }
                //最多可以选择多少张图片
                val selectPictureNmb = MAX_PICTURE_COUNT - collects.size + 1
                ImageUtils.goSelImg(
                    this@FeedbackActivity,
                    selectPictureNmb,
                    onResultCallBack = { localPath: String, w: Int, h: Int, time: Long, listSize: Int ->
                        addImgs(ImgBean(localPath))
                    })
            }))

    }


    override fun observeCallBack() {//获取收藏列表
        mViewModel.feedbackResult.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {

                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    getString(R.string.建议反馈成功).toast()
                    FeedBackUrl.urls.clear()
                    finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    } else {
                        getString(R.string.建议反馈失败).toast()
                    }
                }
            }
        }
    }


    /**
     * 上传图片文件到谷歌云
     */
    private fun updateImageFileGoogle(localPath: String) {
        //1、上传图片
        mViewModel.uploadFile(localPath, "Picture", progress = {}, success = { result ->
            val url = result.data.filePath
            imgList.add(url)
            if (collects.size - 1 == imgList.size) {
                sendFeedBack()
            }
        }, error = {
            getString(R.string.图片上传失败).toast()
        })
    }

    private fun sendFeedBack() {
        val content = bindView.etContent.text.toString().trim()
        mViewModel.sendFeedBack(content, imgList, type)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("onDestroy", "onDestroy")

    }

    override fun onBackPressed() {
        super.onBackPressed()
        FeedBackUrl.urls.clear()
    }
}
