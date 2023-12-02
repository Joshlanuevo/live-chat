package com.ym.chat.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.WebViewClient;
import com.ym.chat.R;
import com.ym.chat.bean.UpdateVersionBean;

import java.io.File;


public class DialogUtils {

    /**
     * 加载数据对话框
     */
    public static Dialog mLoadingDialog;

    /**
     * 关闭加载对话框
     */
    public static void cancelDialogForLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.cancel();
            mLoadingDialog = null;
        }
    }

//    public static Dialog showNormalDialog2(Activity context, UpdateVersionBean uvBean, View.OnClickListener noClick, View.OnClickListener okClick) {
//
//        final Dialog dia = new Dialog(context, R.style.edit_AlertDialog_style);
//        dia.setContentView(R.layout.dialog_version);
//        dia.setCancelable(false);
//        TextView dialog_title = (TextView) dia.findViewById(R.id.dialog_title);
//        ImageView dialogCloseTv = (ImageView) dia.findViewById(R.id.dialog_cancel);
//        TextView dialogCommitTv = (TextView) dia.findViewById(R.id.dialog_confirm);
//        TextView tvVersionName = (TextView) dia.findViewById(R.id.tvVersionName);
//        //选择true的话点击其他地方可以使dialog消失，为false的话不会消失
//        dia.setCanceledOnTouchOutside(false); // Sets whether this dialog is
//        Window w = dia.getWindow();
//        WindowManager.LayoutParams lp = w.getAttributes();
//        lp.x = 0;
//        lp.y = 40;
//        dia.onWindowAttributesChanged(lp);
//
//        //设置TextView当内容过长，支持滑动
//        dialog_title.setMovementMethod(ScrollingMovementMethod.getInstance());
//        tvVersionName.setText("V" + uvBean.getVersionNum());
//        //手动更新 Manual,强制更新 Force(强更就隐藏关闭弹窗)
//        if ("Force".equals(uvBean.getUpdateType())) {
//            dialogCloseTv.setVisibility(View.GONE);
//        } else {
//            dialogCloseTv.setVisibility(View.VISIBLE);
//        }
//
//        if (uvBean.getSummarys() != null) {
//            //拼接展示更新说明
//            String updateContent = "";
//            for (int i = 0; i < uvBean.getSummarys().size(); i++) {
//                if (i < uvBean.getSummarys().size() - 1)
//                    updateContent = updateContent + uvBean.getSummarys().get(i) + "\n";
//                else {
//                    updateContent = updateContent + uvBean.getSummarys().get(i);
//                }
//            }
//
//            dialog_title.setText(updateContent);
//        }
//
//
//        dialogCloseTv.setOnClickListener(noClick);
//        dialogCommitTv.setOnClickListener(okClick);
//        dia.show();
//        return dia;
//    }


    public static AgentWeb mAgentWeb;

    //加载url
    private static void loadUrlWebView(String url, LinearLayout webLl, Activity activity, final Dialog dia) {

        if (mAgentWeb == null) {
            mAgentWeb = AgentWeb.with(activity)
                    .setAgentWebParent(webLl, new LinearLayout.LayoutParams(-1, -1))
                    .useDefaultIndicator(0, 0)
                    //                .setMainFrameErrorView(R.layout.agentweb_error_page, -1)
                    .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                    .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                    .interceptUnkownUrl() //拦截找不到相关页面的Scheme
                    .setWebViewClient(new MyWebViewClient(dia))
                    .createAgentWeb()
                    .ready()
                    .go(url);

            WebView webViewSession = mAgentWeb.getWebCreator().getWebView();
            WebSettings mWebSettings = webViewSession.getSettings();
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setAllowContentAccess(true);
            mWebSettings.setAppCacheEnabled(false);
            mWebSettings.setBuiltInZoomControls(false);
            mWebSettings.setUseWideViewPort(true);
            mWebSettings.setLoadWithOverviewMode(true);
            mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            mWebSettings.setAllowFileAccess(true);
            mWebSettings.setDomStorageEnabled(true);//开启本地DOM存储
            mWebSettings.setLoadsImagesAutomatically(true); // 加载图片
            mWebSettings.setMediaPlaybackRequiresUserGesture(false);//播放音频，多媒体需要用户手动？设置为false为可自动播放


            webViewSession.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(url));
                    activity.startActivity(intent);
                }
            });

        } else {
            mAgentWeb.getWebCreator().getWebView().loadUrl(url);
        }
    }

    static class MyWebViewClient extends WebViewClient {
        Dialog dia;

        public MyWebViewClient(final Dialog dialog) {
            this.dia = dialog;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();//忽略证书错误继续加载页面
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            Log.e(WebActivityActivity.class.toString(), "4url == " + url);
            //dia.show();
            super.onPageFinished(view, url);
        }
    }

    /**
     * 安装apk
     *
     * @param
     */
    public static void installApk(Activity activity, String apkFilePath) {
        File apkfile = new File(apkFilePath);
        if (!apkfile.exists()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(activity, "com.ym.chat.utils.AppFileProvider", apkfile);//在AndroidManifest中的android:authorities值
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            activity.startActivity(install);
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(i);
        }
    }

}
