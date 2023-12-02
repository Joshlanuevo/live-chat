package com.ym.base.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import com.ym.base_sdk.R


class BaseWebView(context: Context, attrs: AttributeSet?) :
    WebView(context, attrs) {
    constructor(context: Context) : this(context, null)

    private var mProgressBar: ProgressBar? = null
    private var mLinkHttpError: LinkHttpError? = null
    var onFinishLoad = {}


    init {
        settings.javaScriptEnabled = true // 设置WebView支持JavaScript
        settings.setSupportZoom(false) //支持放大缩小
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true  // 缩放至屏幕的大小

        settings.builtInZoomControls = false //显示缩放按钮

        settings.blockNetworkImage = true // 把图片加载放在最后来加载渲染
        settings.allowFileAccess = true // 允许访问文件

        //允许http和https混用
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        settings.saveFormData = true
        settings.setGeolocationEnabled(true)
        settings.domStorageEnabled = true //防止显示白屏

        settings.javaScriptCanOpenWindowsAutomatically = true /// 支持通过JS打开新窗口

        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN


        settings.blockNetworkImage = false

        //实现下载功能
        setDownloadListener(MyWebViewDownLoadListener())
    }

    /**
     * 加载网页url
     *
     * @param url
     */
    fun loadMessageUrl(url: String?) {
        super.loadUrl(url!!)
        webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                //Android8.0以下的需要返回true 并且需要loadUrl；8.0之后效果相反
                return if (Build.VERSION.SDK_INT < 26) {
                    loadUrl(url!!)
                    true
                } else {
                    false
                }
            }
        }
    }

    /**
     * 添加进度条
     */
    fun addProgressBar() {
        mProgressBar = ProgressBar(
            context, null,
            R.attr.progressBarStyle
        )
        mProgressBar?.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, 10, 0, 0
        )
        mProgressBar?.progressDrawable =
            context.resources.getDrawable(R.drawable.progress_web_bar_states)
        addView(mProgressBar) //添加进度条至LoadingWebView中
        webChromeClient = WebChromeClient() //设置setWebChromeClient对象
    }

    inner class WebChromeClient : android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (newProgress == 100) {
                onFinishLoad.invoke()
                mProgressBar?.visibility = View.GONE
            } else {
                if (mProgressBar?.visibility == View.GONE) mProgressBar?.visibility == View.VISIBLE
                mProgressBar?.progress = newProgress
            }
            super.onProgressChanged(view, newProgress)
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            if (title?.contains("404") == true || title?.contains("500") == true || title?.contains(
                    "Error"
                ) == true || title?.contains("about:blank") == true
            ) {
                mLinkHttpError?.onLinkHttpError(0)
            } else if (title?.contains("网页无法打开") == true) {
                mLinkHttpError?.onLinkHttpError(1)
            }
        }
    }

    open fun setLinkHttpError(mLinkHttpError: LinkHttpError) {
        this.mLinkHttpError = mLinkHttpError
    }

    fun setFinish(onFinishLoad: () -> Unit) {
        this.onFinishLoad = onFinishLoad
    }


    inner class MyWebViewDownLoadListener : DownloadListener {
        override fun onDownloadStart(
            url: String?, userAgent: String?, contentDisposition: String?, mimetype: String?,
            contentLength: Long
        ) {
            val uri: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }
}

interface LinkHttpError {
    fun onLinkHttpError(type: Int)
}