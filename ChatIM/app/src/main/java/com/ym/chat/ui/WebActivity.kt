package com.ym.chat.ui

import android.content.Context
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dylanc.viewbinding.binding
import com.ym.base.widget.ext.click
import com.ym.chat.databinding.*

/**
 * 隐私协议
 * 用户协议
 */
class WebActivity : LoadingActivity() {
    private val bindView: ActivityWebBinding by binding()

    companion object {
        val WEBURL = "webUrl"
        val WEBTITLE = "webTitle"
        val WEBTYPE = "webType"
        fun start(context: Context, webUrl: String, webTitle: String, webType: Int = 0) {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(WEBURL, webUrl)
            intent.putExtra(WEBTITLE, webTitle)
            intent.putExtra(WEBTYPE, webType)
            context.startActivity(intent)
        }
    }

    override fun initView() {
        bindView.toolbar.run {
            viewBack.click {
                finish()
            }
        }
    }

    override fun requestData() {
        intent?.let {
            var webUrl = it.getStringExtra(WEBURL)
            var webTitle = it.getStringExtra(WEBTITLE)
            var webType = it.getIntExtra(WEBTYPE, 0)
            bindView.toolbar.tvTitle.text = webTitle
            bindView.webView.settings.let { ws ->
                //支持javascript
                ws.javaScriptEnabled = true
                //设置可以支持缩放
                ws.setSupportZoom(true)
                ws.textZoom = 100
                if (webType == 1)
                    ws.textSize = WebSettings.TextSize.LARGEST
                //设置内置的缩放控件
                ws.builtInZoomControls = true
                //隐藏原生的缩放控件
                ws.displayZoomControls = false
                //扩大比例的缩放
                ws.useWideViewPort = true
                ws.allowFileAccess = true
                //自适应屏幕
                ws.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                ws.loadWithOverviewMode = true
                //开启http和https混合加载
                ws.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                //ws.userAgentString = KeyConfig.WEB_AGENT
            }
            //解决键盘不弹起的BUG
            bindView.webView.requestFocus(View.FOCUS_DOWN)
            bindView.webView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> if (!v.hasFocus()) {
                        v.requestFocus()
                    }
                }
                false
            }
//            bindView.webView.webViewClient = object : WebViewClient(){
//                override fun onPageFinished(view: WebView?, url: String?) {
//                    super.onPageFinished(view, url)
//                    val javascript = "javascript:function ResizeImages() {" +
//                            "var myimg,oldwidth;" +
//                            "var maxwidth = document.body.clientWidth;" +
//                            "for(i=0;i <document.images.length;i++){" +
//                            "myimg = document.images[i];" +
//                            "if(myimg.width > maxwidth){" +
//                            "oldwidth = myimg.width;" +
//                            "myimg.width = maxwidth;" +
//                            "}" +
//                            "}" +
//                            "}"
//                    view!!.loadUrl(javascript)
//                    view.loadUrl("javascript:ResizeImages();")
//                }
//            }
            when (webType) {
                1 -> webUrl?.let { it1 ->
                    bindView.webView?.loadData(it1, "text/html", "UTF-8")
                }
                else -> webUrl?.let { it1 -> bindView.webView?.loadUrl(it1) }
            }
        }
    }

    override fun observeCallBack() {
    }

    override fun onBackPressed() {
        bindView.webView?.let {
            if (it.canGoBack()) {
                it.goBack()
            } else {
                finish()
            }
        }
    }

}