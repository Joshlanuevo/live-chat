package com.ym.chat.ui.fragment

import android.content.Intent
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import com.blankj.utilcode.util.ActivityUtils
import com.dylanc.viewbinding.binding
import com.just.agentweb.AgentWeb
import com.just.agentweb.DefaultWebClient
import com.ym.base.mvvm.BaseFragment
import com.ym.base.mvvm.BaseViewModel
import com.ym.chat.R
import com.ym.chat.databinding.FragmentMovementBinding
import com.ym.chat.utils.GameWebViewClient
import com.ym.chat.utils.LogHelp
import com.ym.chat.utils.WebViewUtils
import com.ym.chat.viewmodel.MovementModel

/**
 * 动态Fragment
 */
class MovementFragment : LoadingFragment(R.layout.fragment_movement) {
    private val bindView: FragmentMovementBinding by binding()
    private val mViewModel: MovementModel by viewModels()

    private var mAgentWeb: AgentWeb? = null

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    override fun initView() {
    }

    override fun requestData() {
        showLoading()
        mViewModel.getFindUrl()
    }

    override fun observeCallBack() {
        mViewModel.urlResult.observe(this) {
            when (it) {
                is BaseViewModel.LoadState.Loading -> {
                }
                is BaseViewModel.LoadState.Success -> {
                    initWebview(it.data ?: "")
                }
                is BaseViewModel.LoadState.Fail -> {

                }
            }
        }
    }

    private fun initWebview(url: String) {
        mAgentWeb = AgentWeb.with(this)
            .setAgentWebParent(bindView.fraWebview, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator(
                0,
                0
            )
//            .setMainFrameErrorView(errorView)
            .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
            .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK) //打开其他应用时，弹窗咨询用户是否前往其他应用
            .setWebViewClient(GameWebViewClient { isShowTitle ->
            })
            .createAgentWeb()
            .ready()
            .go(url)
        WebViewUtils.initWebViewSetting(mAgentWeb?.webCreator?.webView?.settings)
        mAgentWeb?.webCreator?.webView?.setWebChromeClient(object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {

                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                ActivityUtils.getTopActivity().startActivityForResult(
                    Intent.createChooser(i, "File Browser"), 101
                )
                return true
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    hideLoading()
                }
            }
        })
    }
}