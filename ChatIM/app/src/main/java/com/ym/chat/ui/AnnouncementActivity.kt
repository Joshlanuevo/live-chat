package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.text.InputFilter
import android.text.TextUtils
import com.dylanc.viewbinding.binding
import com.ym.base.ext.toast
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.gone
import com.ym.base.widget.ext.visible
import com.ym.chat.R
import com.ym.chat.bean.GroupInfoBean
import com.ym.chat.databinding.ActivityAnnouncementBinding
import com.ym.chat.db.ChatDao
import com.ym.chat.viewmodel.ChatGroupViewModel

/**
 * 编辑公告0  群名1
 */
class AnnouncementActivity : LoadingActivity() {
    private val bindView: ActivityAnnouncementBinding by binding()
    private val mViewModel = ChatGroupViewModel()

    private var content: String = ""
    private var groupId: String = ""
    private var groupRole: Int = 0//0 群主  1 管理员  2成员
    private var inType = 0//编辑公告0  群名1
    private var groupInfo: GroupInfoBean? = null

    companion object {
        val INTYPE = "inType"
        val GROUPINFO = "groupInfo"
        fun start(
            context: Context,
            inType: Int,
            groupInfo: GroupInfoBean
        ) {
            val intent = Intent(context, AnnouncementActivity::class.java)
            intent.putExtra(INTYPE, inType)
            intent.putExtra(GROUPINFO, groupInfo)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
        }

        bindView.btnSave.click {
            content = bindView.etContent.text.toString()
            when (inType) {
                0 -> {
                    if (content.isNullOrBlank()) {
                        "${getString(R.string.群公告不能为空)}".toast()
                        return@click
                    }
                    mViewModel.putGroupInfo(groupId, notice = content)
                }
                1 -> {
                    if (content.isNullOrBlank()) {
                        "${getString(R.string.群公告不能为空)}".toast()
                        return@click
                    }
                    mViewModel.putGroupInfo(groupId, name = content)
                }
            }
        }

        bindView.btnCancel.click {
            when (inType) {
                0 -> {
                    bindView.etContent.setText(groupInfo?.notice ?: "")
                }
                1 -> {
                    bindView.etContent.setText(groupInfo?.name ?: "")
                }
            }
        }
    }

    override fun requestData() {
        intent?.let {
            inType = it.getIntExtra(INTYPE, 0)
            groupInfo = it.getSerializableExtra(GROUPINFO) as GroupInfoBean?
            groupId = groupInfo?.id ?: ""
            groupRole = groupInfo?.role!!
            when (inType) {
                0 -> {
                    bindView.toolbar.tvTitle.text = "${getString(R.string.qungonggao)}"
                    content = groupInfo?.notice ?: ""
                }
                1 -> {
                    bindView.toolbar.tvTitle.text = "${getString(R.string.设置群组名称)}"
                    bindView.etContent.hint = "${getString(R.string.新的群组名称)}"
                    content = groupInfo?.name ?: ""
                    val filterArray = arrayOfNulls<InputFilter>(1)
                    filterArray[0] = InputFilter.LengthFilter(15)
                    bindView.etContent.filters = filterArray
                }
            }
            bindView.etContent.setText(content)
        }

        if (groupRole == 2) {
            //群成员不能编辑
            bindView.etContent.isEnabled = false
            bindView.btnSave.gone()
            bindView.btnCancel.gone()
        }else{
            bindView.btnSave.visible()
            bindView.btnCancel.visible()
        }
    }

    override fun observeCallBack() {
        //群设置
        mViewModel.putGroupLiveData.observe(this) { result ->
            when (result) {
                is BaseViewModel.LoadState.Loading -> {
                    showLoading()
                }
                is BaseViewModel.LoadState.Success -> {
                    hideLoading()
                    "${getString(R.string.设置成功)}".toast()
                    when (inType) {
                        0 -> {
                            groupInfo?.notice = content
                            ChatDao.getGroupDb().updateNoticeById(groupInfo?.id?:"",content)
                        }
                        1 -> {
                            groupInfo?.name = content
                            ChatDao.getGroupDb().updateNameById(groupInfo?.id?:"",content)
                        }
                    }
                    this.finish()
                }
                is BaseViewModel.LoadState.Fail -> {
                    hideLoading()
                    if (!TextUtils.isEmpty(result.exc?.message)) {
                        result.exc?.message.toast()
                    }
                }
            }
        }
    }
}