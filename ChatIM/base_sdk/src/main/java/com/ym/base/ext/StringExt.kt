package com.ym.base.ext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import coil.util.CoilUtils
import com.blankj.utilcode.util.*
import com.ym.base.constant.HostManager
import com.ym.base.util.EditTextUtils
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * inline报警告暂不处理，否则打印的地方始终是StringExt不好根据log找到相应的类
 * Author:yangcheng
 * Date:2020/8/11
 * Time:17:19
 */
inline fun String?.logE() {
    if (!this.isNullOrBlank()) {
        Log.e("YM-", "$this")
    }
}

inline fun String?.logW() {
    if (!this.isNullOrBlank()) {
        Log.w("YM-", "$this")
    }
}

inline fun String?.logI() {
    if (!this.isNullOrBlank()) {
        Log.i("YM-", "$this")
    }
}

inline fun String?.logD() {
    if (!this.isNullOrBlank()) {
        Log.d("YM-", "$this")
    }
}

inline fun String?.toast() {
    if (!this.isNullOrBlank() && AppUtils.isAppForeground() && this.toLowerCase() != "null") {
        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(this)
    }
}

inline fun String?.toastBottom() {
    if (!this.isNullOrBlank() && AppUtils.isAppForeground() && this.toLowerCase() != "null") {
        ToastUtils.make().setGravity(Gravity.CENTER, 0, 300).show(this)
    }
}

/**
 * 前4位后4位保留，中间打马赛克
 */
inline fun String?.maskBankNum(): String? {
    return if (!this.isNullOrBlank() && length > 8) {
        var startStr = substring(0, 4)
        var endStr = substring(length - 4, length)
        "$startStr****$endStr"
    } else {
        this
    }
}

/**
 * 校验越南手机号(默认为：越南->VN)
 * @see com.google.i18n.phonenumbers.CountryCodeToRegionCodeMap
 */
inline fun String?.matchYnPhone(countryCode: String = "VN"): Boolean {
    if (this.isNullOrBlank()) return false
    if (countryCode.toUpperCase() == "VN" || countryCode.toUpperCase() == "YN") {
        return this.startsWith("0") && this.length == 10
    }
    return this.isPhoneNumber(if (countryCode.toUpperCase(Locale.getDefault()) == "YN") "VN" else countryCode)
        ?: false
}

/**
 * 校验越南号码号段
 * 081 082 083 084 085
 * 032 033 034 035 036 037 038 039
 * 070 076 077 078 079
 * 056 058 059
 */
inline fun String?.isYnPhone(): Boolean {
    val telRegex =
        "^((08[1-5])|(03[2-9])|(07[0,6,7,8,9])|(05[6,8,9]))[0-9]{7}$"
    var p: Pattern = Pattern.compile(telRegex)
    var m: Matcher = p.matcher(this)
    return m.find()
}


/**
 * 去掉小数点后面多余的0
 */
inline fun String?.deleteLastZero(): String {
    var result = this!!
    if (this!!.contains(".")) {
        var p: Pattern = Pattern.compile("0+?$")
        var m: Matcher = p.matcher(this)
        if (m.find()) {
            result = m.replaceAll("")!!
        }
    }
    return result
}


/**
 *检查邮箱号
 */
inline fun String?.matchEmail(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }
    return this!!.contains("@")
}

/**
 * 忘记密码用
 */
inline fun String?.matchPwd(): Boolean {
    if (this.isNullOrBlank()) return false
    return EditTextUtils.isPwdMatcher(this)
}

//inline fun String?.snack() {
//    if (!this.isNullOrBlank() && AppUtils.isAppForeground()) {
//        SnackBarUtils.show(this)
//    }
//}
//
//inline fun String?.snackFinish() {
//    if (!this.isNullOrBlank() && AppUtils.isAppForeground()) {
//        ActivityUtils.getActivityList().takeIf { it.isNotEmpty() }?.let {
//            if (it.size == 1) {
//                snackView(it[0].window.decorView)
//            } else {
//                snackView(it[it.size - 2].window.decorView)
//            }
//        }
//    }
//}
//
//inline fun String?.snackDismiss() {
//    if (!this.isNullOrBlank() && AppUtils.isAppForeground()) {
//        ActivityUtils.getTopActivity()?.let {
//            snackView(it.window.decorView)
//        }
//    }
//}
//
//@SuppressLint("WrongConstant")
//inline fun String?.snackView(view : View?) {
//    if (!this.isNullOrBlank() && AppUtils.isAppForeground()) {
//        view?.let { KokSnackbar.make(it,this,KokSnackbar.LENGTH_SHORT).show() }
//    }
//}

inline fun String?.isNetImageUrl(): Boolean {
    return if (this.isNullOrBlank()) {
        false
    } else if (!this.startsWith("http", true)) {
        false
    } else {
        Pattern.compile(".*?(gif|jpeg|png|jpg|bmp)").matcher(this.toLowerCase(Locale.getDefault()))
            .matches()
    }
}

inline fun File?.isImage(): Boolean {
    return if (this == null) {
        false
    } else {
        Pattern.compile(".*?(gif|jpeg|png|jpg|bmp)")
            .matcher(this.path.toLowerCase(Locale.getDefault())).matches()
    }
}

inline fun String?.isVideoUrl(): Boolean {
    return if (this.isNullOrBlank()) {
        false
    } else if (!this.toLowerCase(Locale.getDefault()).startsWith("http", true)) {
        false
    } else {
        Pattern.compile(".*?(avi|rmvb|rm|asf|divx|mpg|mpeg|mpe|wmv|mp4|mkv|vob)")
            .matcher(this.toLowerCase(Locale.getDefault())).matches()
    }
}

inline fun String?.hasEnglishOrNum(): Boolean {
    return if (this.isNullOrBlank()) {
        return false
    } else {
        Pattern.compile("[a-zA-Z0-9]").matcher(this).find()
    }
}

inline fun String?.isLiveUrl(): Boolean {
    return if (this.isNullOrBlank()) {
        false
    } else {
        this.toLowerCase(Locale.getDefault()).run {
            startsWith("rtmp") || startsWith("rtsp")
        }
    }
}

fun String?.maxLengthFixed(maxLength: Int): String? {
    return if (this.isNullOrBlank()) "" else if (maxLength >= this.length || maxLength <= 0) this else {
        "${this.substring(0, maxLength)}..."
    }
}

/**复制内容到剪切板*/
inline fun String?.copyToClipboard(): Boolean {
    return try {
        //获取剪贴板管理器
        val cm = Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 创建普通字符型ClipData
        val mClipData = ClipData.newPlainText("Label", this ?: "")
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData)
        true
    } catch (e: Exception) {
        false
    }
}

fun String?.getHost(): String {
    return if (this.isNullOrBlank()) "" else Uri.parse(this).host ?: this
}

//文件目录转file
fun String?.toFile(): File? {
    if (this != null) {
        return if (this.startsWith("http", true)) null else {
            val f = File(this)
            if (f.exists()) f else UriUtils.uri2File(Uri.parse(this))
        }
    }
    return null
}

fun String?.getHttpUrl(): String? {
    return this?.let { if (it.startsWith("http", true)) it else "http://$it" }
}

fun String?.openOutLink() {
    if (!this.isNullOrBlank()) {
        try {
            val newUrl = if (this.startsWith("http", true)) this else "http://$this"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl))
            ActivityUtils.getTopActivity()?.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

//Coil获取缓存图片文件
fun String?.getCoilCacheFile(): File? {
    return this?.toFile() ?: this?.toHttpUrlOrNull()?.let { u ->
        CoilUtils.createDefaultCache(Utils.getApp()).directory.listFiles()
            ?.lastOrNull { it.name.endsWith(".1") && it.name.contains(Cache.key(u)) }
    }
}

//替换空格
fun String?.replaceNbsp(): String {
    return this?.replace("&nbsp;", " ") ?: ""
}

//替换为webp地址
fun String?.getWebpUrl(): String {
    return if (this.isNullOrBlank()) "" else if (this.startsWith("http", true)) {
        "${this}.webp"
    } else {
        if (HostManager.YnHost.dlUrl.endsWith("/")) {
            "${HostManager.YnHost.dlUrl.substring(0, HostManager.YnHost.dlUrl.length - 1)}${this}.webp"
        } else {
            "${HostManager.YnHost.dlUrl}${this}.webp"
        }
    }
}

fun String?.ifToDouble(): Double {
    return try {
        this!!.toDouble()
    } catch (e: Exception) {
        0.0
    }
}
