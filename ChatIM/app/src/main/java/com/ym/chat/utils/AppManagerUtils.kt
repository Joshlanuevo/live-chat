package com.ym.chat.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.Utils
import java.io.*


object AppManagerUtils {

    /**
     * 获取版本号
     */
    fun getVersionName(context: Context): String? {
        val manager = context.packageManager
        var name: String? = null
        try {
            val info = manager.getPackageInfo(context.packageName, 0)
            name = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return name
    }

    /**
     * 检测是否有新版本
     */
    fun detectUpgrade(context: Context, remoteVersion: String): Boolean {
        var remote = splitPoint(remoteVersion)
        var local = splitPoint(
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            ).versionName
        )
        var minSize = if (remote.size < local.size) remote.size else local.size
        for (index in 0 until minSize) {
            if (remote[index] > local[index]) {
                return true
            } else if (remote[index] < local[index]) {
                break
                return false
            }
        }
        return false
    }

    /**
     * 获取FileProvider路径
     */
    fun getProviderPath(context: Context): String {
        return context.cacheDir.absolutePath + File.separator + "outside"
    }

    /**
     * 安装APP
     */
    fun installAPP(context: Context, path: String) {
        var intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            val uri = FileProvider.getUriForFile(Utils.getApp(), Utils.getApp().packageName + ".fileprovider", File(path))
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(
                Uri.parse("file://$path"),
                "application/vnd.android.package-archive"
            )
        }
        context.startActivity(intent)
    }

    private fun splitPoint(version: String): ArrayList<String> {
        var list = arrayListOf<String>()
        var count = 0
        while (count < version.length) {
            var temp = version.indexOf(".", count)
            if (temp < 0) break
            list.add(version.substring(count, temp))
            count = temp + 1
        }
        list.add(version.substring(count, version.length))
        return list
    }


    /**
     * 判断手机是否安装某个应用
     * @param context
     * @param appPackageName  应用包名
     * @return   true：安装，false：未安装
     */
    fun isApplicationAvilible(context: Context, appPackageName: String): Boolean {
        var packageManager: PackageManager = context.packageManager// 获取packagemanager
        var pinfo: List<PackageInfo> = packageManager.getInstalledPackages(0)// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (index in pinfo.indices) {
                val pn: String = pinfo[index].packageName
                if (appPackageName == pn) {
                    return true
                }
//                Logger.e("包名=${pinfo[index].packageName}--appName=${pinfo[index].applicationInfo.loadLabel(packageManager)}----$appPackageName")
            }
        }
        return false
    }

}