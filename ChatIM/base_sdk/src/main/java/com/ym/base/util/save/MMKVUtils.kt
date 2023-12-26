package com.ym.base.util.save

import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import androidx.annotation.IntRange
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.TimeUtils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.tencent.mmkv.MMKV
import com.ym.base.constant.EventKeys
import com.ym.base.constant.HostManager
import com.ym.base.ext.dp2Px
import com.ym.base.ext.logE
import com.ym.base.rxhttp.UserBean
import com.ym.base.util.gson.JsonUtil
import java.lang.reflect.Type
import java.util.*

/**
 * 如果增加一个频道对应多个房间号，则需要修改
 */
object MMKVUtils {
    //<editor-fold defaultstate="collapsed" desc="参数值">
    const val GAME_LIST_SORT_TYPE = "GAME_LIST_SORT_TYPE"

    const val imMediaMapKey = "imMediaMapKey"

    const val 记住密码 = "KEY_JIZHUMIMA"

    const val 盘口下注类型置顶 = "Pan_Kou_Xia_Zhu_Zhi_Ding"
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="账号相关">
    private const val KEY_ACCOUNT = "KEY_ACCOUNT"
    private const val KEY_PWD = "KEY_PWD"
    fun saveAccountAndPwd(account: String, pwd: String) {
        mmkv?.encode(KEY_ACCOUNT, account)
        mmkv?.encode(KEY_PWD, pwd)
    }

    fun getAccount(): String {
        return mmkv?.decodeString(KEY_ACCOUNT) ?: ""
    }

    fun getPwd(): String {
        return mmkv?.decodeString(KEY_PWD) ?: ""
    }

    fun clearAccountAndPwd() {
        mmkv?.removeValueForKey(KEY_ACCOUNT)
        mmkv?.removeValueForKey(KEY_PWD)
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="通用">
    val mmkv: MMKV? = MMKV.defaultMMKV()

    @JvmStatic
    fun <T> getObject(str: String?, type: Type?): T? {
        val string: String = mmkv?.getString(str, null) ?: return null
        return JsonUtil.fromJson(string, type)
    }

    @JvmStatic
    fun putObject(str: String?, obj: Any?) {
        if (obj != null) {
            mmkv?.putString(str, JsonUtil.toJson(obj))
        }
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="保存银行列表">
    const val BANK_LIST = "KEY_BANK_LIST"
    const val VENUE_LIST = "KEY_VENUE_LIST"

    @JvmStatic
    fun putBankList(obj: Any?) {
        obj?.let {
            mmkv?.putString(BANK_LIST, JsonUtil.toJson(obj))
        }
    }

    @JvmStatic
    fun putVenueList(obj: Any?) {
        obj?.let {
            mmkv?.putString(VENUE_LIST, JsonUtil.toJson(obj))
        }
    }

    @JvmStatic
    fun getBankListJson(): MutableList<BankListInfo> {
        mmkv?.getString(BANK_LIST, null)?.let {
            if (!TextUtils.isEmpty(it)) {
                val bankList = GsonUtils.fromJson(it, SelectBankList::class.java)
                return bankList.data
            }
        }
        return mutableListOf()
    }

    @JvmStatic
    fun getVenueListJson(): String? {
        return mmkv?.getString(VENUE_LIST, null)
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="版本管理相关信息">
    private const val KEY_DATE_TODAY = "key_date_today"
    private const val KEY_APP_URL = "key_app_url"

    /**
     * 保存最新版本的url地址
     */
    fun saveAppVersionUrl(url: String?) {
        mmkv?.putString(KEY_APP_URL, url ?: "")
    }

    /**
     * 获取最新版本的url地址
     */
    fun getAppVersionUrl() {
        mmkv?.getString(KEY_APP_URL, "")
    }

    /**
     * 保存今天的日期
     */
    fun saveToday(date: String?) {
        mmkv?.putString(KEY_DATE_TODAY, date ?: "")
    }

    /**
     * 判断今天是否已经弹框显示版本更新
     */
    fun isAppVersion(date: String?): Boolean {
        if (mmkv?.getString(KEY_DATE_TODAY, "") == date) {
            return true
        }
        return false
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="用户相关信息">
    private const val KEY_YM_UID = "KEY_YM_UID"
    private const val KEY_YM_USER_NAME = "KEY_YM_USER_NAME"
    private const val KEY_USER_BEAN = "KEY_USER_BEAN"
    private const val CG_IM_TOKEN = "CG_IM_TOKEN"
    private val mmkvUser: MMKV? = MMKV.mmkvWithID("YM_USER_INFO")
    private val mmkvLockUser: MMKV? = MMKV.mmkvWithID("YM_LOCK_USER_INFO")
    private var mUserBean: LoginData? = null
    private var mToken: String? = null

    fun saveUserId(uid: String?) {
        mmkvUser?.encode(KEY_YM_UID, uid)
    }

    fun getUserId(): String? {
        return mmkvUser?.decodeString(KEY_YM_UID)
    }

    fun saveUserName(name: String?) {
        mmkvUser?.encode(KEY_YM_USER_NAME, name)
    }

    fun getUserName(): String? {
        return mmkvUser?.decodeString(KEY_YM_USER_NAME)
    }

    fun getUser(): LoginData? {
        if (mUserBean != null) return mUserBean
        val string: String = mmkvUser?.getString(KEY_USER_BEAN, null) ?: return null
        return try {
            JsonUtil.fromJson(string, LoginData::class.java).also {
                mUserBean = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 判断账号是不是管理员
     *  系统 暂时权限跟管理员一样
     */
    fun isAdmin(): Boolean {
        if (getUser()?.memberLevelCode == "Admin" || getUser()?.memberLevelCode == "System") {
            return true
        }
        return false
    }

    fun putUser(userBean: LoginData?) {
        mUserBean = userBean
        mmkvUser?.putString(KEY_USER_BEAN, JsonUtil.toJson(userBean))
    }

    fun clearUserInfo() {
        try {
            mUserBean = null
            mmkvUser?.clearAll()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun saveToken(token: String?) {
        mToken = token
        mmkvUser?.encode(CG_IM_TOKEN, token)
    }

    fun getToken(): String? {
        return mToken ?: mmkvUser?.decodeString(CG_IM_TOKEN)?.also { mToken = it }
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="手势登录 保存String值">
    const val LockScreenPwd = "LockScreenPwd"
    const val IsLoginReturnToken = "isLoginReturnToken"
    const val SaveLockScreenPwd = "saveLoginReturnToken"

    fun putString(key: String, value: String?) {
        MMKV.defaultMMKV()?.encode(key, value)
    }

    fun getString(key: String): String? {
        return MMKV.defaultMMKV()?.decodeString(key)
    }

    /**
     * 保存userName
     */
    fun saveLockUserId(uid: String?) {
        mmkvLockUser?.encode(KEY_YM_UID, uid)
    }

    /**
     * 获取userName
     */
    fun getLockUserId(): String? {
        return mmkvLockUser?.decodeString(KEY_YM_UID)
    }

    /**
     * 获取密码
     */
    fun getLockPassword(): String? {
        return mmkvLockUser?.decodeString("${getLockUserId()}${SaveLockScreenPwd}")
    }

    /**
     * 保存密码
     */
    fun saveUserPaw(str: String?) {
        mmkvLockUser?.encode("${getLockUserId()}${SaveLockScreenPwd}", str)
    }


    /**
     * 是否设置手势设置登录
     */
    fun saveLockEditPaw(isLockEditPaw: Boolean) {
        mmkvLockUser?.encode("${getLockUserId()}${IsLoginReturnToken}", isLockEditPaw)
    }

    /**
     * 获取是否 设置了手势登录
     */
    fun isLockEditPaw(): Boolean? {
        return if (getLockUserId().isNullOrEmpty()) {
            false
        } else {
            mmkvLockUser?.decodeBool("${getLockUserId()}${IsLoginReturnToken}")
        }
    }

    fun clearLockUserInfo() {
        mmkvLockUser?.clearAll()
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="开关默认值">
    private const val KEY_BET_NEW_USER = "KEY_BET_NEW_USER"
    private const val KEY_BET_SORT_TIME = "KEY_BET_SORT_TIME"
    fun saveBetSortTime(time: Boolean) {
        mmkvUser?.encode(KEY_BET_SORT_TIME, time)
    }

    fun getBetSortTime(): Boolean {
        return mmkvUser?.decodeBool(KEY_BET_SORT_TIME, true) ?: true
    }

    fun saveBetNewUser(new: Boolean) {
        mmkvUser?.encode(KEY_BET_NEW_USER, new)
    }

    fun getBetNewUser(): Boolean {
        return mmkvUser?.decodeBool(KEY_BET_NEW_USER, false) ?: false
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="服务器时间戳计算">
    private val mmkvTime: MMKV? = MMKV.mmkvWithID("YM_TIME_SERVICE")
    private const val KEY_OPEN_SYSTEM = "KEY_OPEN_SYSTEM"

    //保存拿到的服务器时间
    fun saveServiceTime(time: Long) {
        //开机时长
        val openSysDuration = SystemClock.elapsedRealtime()
        mmkvTime?.encode(KEY_OPEN_SYSTEM, "${time},${openSysDuration}")
    }

    //获取当前服务器时间(拿到为0则证明需要先获取服务器时间)
    private var timeLog: Long = 0
    fun getServiceTime(): Long {
        mmkvTime?.decodeString(KEY_OPEN_SYSTEM)?.let { r ->
            if (r.contains(",")) {
                val split = r.split(",")
                val timeService = split[0].toLong()
                val timeDuration = split[1].toLong()
                val openSysDuration = SystemClock.elapsedRealtime()
                val currentServiceTime = timeService + openSysDuration - timeDuration
                if (timeLog / 1000 != currentServiceTime / 1000) {
                    timeLog = currentServiceTime
                    "Local Compute Server Time：${currentServiceTime}".logE()
                }
                return currentServiceTime
            }
        }
        return 0
    }

    //为了防止误差变大，每次打开APP都要清理重新校正
    fun clearServiceTime() {
        mmkvTime?.clearAll()
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Host选择">
    private val mmkvHost: MMKV? = MMKV.mmkvWithID("YM_HOST_INFO")
    private const val KEY_YM_HOST_API = "KEY_YM_HOST_API"
    private const val KEY_YM_HOST_DL = "KEY_YM_HOST_DL"
    private const val KEY_YM_HOST_YunQue = "KEY_YM_HOST_YunQue"

    //防止每次都要从MMKV读取，所以如果存在，就直接使用内存中的值
    private var mHostApi: String? = null
    private var mHostDl: String? = null
    private var mHostYunQue: String? = null

    fun saveHostApi(host: String) {
        mHostApi = if (host.isBlank()) {
            mmkvHost?.removeValueForKey(KEY_YM_HOST_API)
            null
        } else {
            mmkvHost?.encode(KEY_YM_HOST_API, host)
            host
        }
    }

    fun getHostApi(): String {
        return if (!HostManager.isOpenLinkSwitch) {
            HostManager.YnHost.apiUrlDefault
        } else {
            val url = HostManager.YnHost.apiUrlDefault
            mHostApi ?: mmkvHost?.decodeString(KEY_YM_HOST_API, url)?.also { mHostApi = it } ?: url
        }
    }

    fun saveHostDl(host: String) {
        mHostDl = if (host.isBlank()) {
            mmkvHost?.removeValueForKey(KEY_YM_HOST_DL)
            null
        } else {
            mmkvHost?.encode(KEY_YM_HOST_DL, host)
            host
        }
    }

    fun getHostDl(): String {
        return if (!HostManager.isOpenLinkSwitch) {
            HostManager.YnHost.dlUrlDefault
        } else {
            val url = HostManager.YnHost.dlUrlDefault
            mHostDl ?: mmkvHost?.decodeString(KEY_YM_HOST_DL, url)?.also { mHostDl = it } ?: url
        }
    }

    fun saveHostYunQue(host: String) {
        mHostYunQue = if (host.isBlank()) {
            mmkvHost?.removeValueForKey(KEY_YM_HOST_YunQue)
            null
        } else {
            mmkvHost?.encode(KEY_YM_HOST_YunQue, host)
            host
        }
    }

    fun getHostYunQue(): String {
        return if (!HostManager.isOpenLinkSwitch) {
            HostManager.YnHost.yunQueUrlDefault
        } else {
            val url = HostManager.YnHost.yunQueUrlDefault
            mHostYunQue ?: mmkvHost?.decodeString(KEY_YM_HOST_YunQue, url)
                ?.also { mHostYunQue = it } ?: url
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="首页当天不再弹窗逻辑">
    private val mmkvNoShowDialog: MMKV? = MMKV.mmkvWithID("YM_NO_SHOW_DIALOG")
    private const val KEY_YM_NO_SHOW_DIALOG = "KEY_YM_NO_SHOW_DIALOG_"

    //根据当前账号保存不需要弹窗的日期
    fun setNoShowDialog() {
        getUserId()?.let { uid ->
            //保存不需要弹窗的日期
            val date = TimeUtils.date2String(Date(), TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
            mmkvNoShowDialog?.encode("${KEY_YM_NO_SHOW_DIALOG}$uid", date)
        }
    }

    //判断当前账号是否需要弹窗
    fun needShowDialog(): Boolean {
        val uid = getUserId()
        return if (uid.isNullOrBlank()) false else {
            //当前日期
            val date = TimeUtils.date2String(Date(), TimeUtils.getSafeDateFormat("yyyy-MM-dd"))
            //不需要弹窗的日期
            val saveDate = mmkvNoShowDialog?.decodeString("${KEY_YM_NO_SHOW_DIALOG}$uid") ?: ""
            //如果不同，则需要弹窗
            date != saveDate
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="暗黑模式判断">
    private val mmkvDark: MMKV? = MMKV.mmkvWithID("YM_DARK_INFO")
    private const val KEY_YM_BLACK_MODE = "KEY_YM_BLACK_MODE"
    private const val KEY_YM_BLACK_MODE_INDEX = "KEY_YM_BLACK_MODE_INDEX"
    private var isBlackMode: Boolean? = null
    private var blackModeType: Int? = null
    fun saveBlackMode(black: Boolean) {
        if (isBlackMode == black) return
        isBlackMode = black
        LiveEventBus.get(EventKeys.BLACK_MODE, Boolean::class.java).post(black)
        mmkvDark?.encode(KEY_YM_BLACK_MODE, black)
    }

    fun isBlackMode(): Boolean {
        return isBlackMode ?: mmkvDark?.decodeBool(KEY_YM_BLACK_MODE, false)
            ?.also { b -> isBlackMode = b } ?: false
    }

    fun saveBlackModeIndex(@IntRange(from = 1, to = 3) index: Int) {
        if (blackModeType == index) return
        blackModeType = index
        LiveEventBus.get(EventKeys.BLACK_MODE_INDEX, Int::class.java).post(index)
        mmkvDark?.encode(KEY_YM_BLACK_MODE_INDEX, index)
    }

    fun getBlackModeIndex(): Int {
        return blackModeType ?: mmkvDark?.decodeInt(KEY_YM_BLACK_MODE_INDEX, 1)
            ?.also { i -> blackModeType = i } ?: 1
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="密码错误次数判断">
    private val mmkvPwd: MMKV? = MMKV.mmkvWithID("YM_PWD_ERROR")
    private const val KEY_YM_PWD_COUNT = "KEY_YM_PWD_COUNT"
    private const val KEY_YM_PWD_PHONE_COUNT = "KEY_YM_PWD_PHONE_COUNT"
    private const val KEY_YM_PWD_TIME = "KEY_YM_PWD_TIME"

    fun savePwdCount(count: Int) {
        mmkvPwd?.encode(KEY_YM_PWD_COUNT, count)
    }

    fun getPwdCount(): Int? {
        return mmkvPwd?.decodeInt(KEY_YM_PWD_COUNT, 0)
    }

    fun savePhonePwdCount(count: Int) {
        mmkvPwd?.encode(KEY_YM_PWD_PHONE_COUNT, count)
    }

    fun getPhonePwdCount(): Int? {
        return mmkvPwd?.decodeInt(KEY_YM_PWD_PHONE_COUNT, 0)
    }

    fun savePwdTime(time: Long) {
        mmkvPwd?.encode(KEY_YM_PWD_TIME, time)
    }

    fun getPwdTime(): Long? {
        return mmkvPwd?.decodeLong(KEY_YM_PWD_TIME, 1)
    }

    //</editor-fold>
}

/**
 * 银行列表
 * "id": 20,
"level": "004003",
"name": "招商银行",
"sort": 0
 * */

data class SelectBankList(
    val status: Boolean,
    val data: MutableList<BankListInfo>
)

data class BankListInfo(
    var id: String,
    var level: String = "",
    var name: String,
) {
    var icon: Int = 0
    var isSel = false
    var paddingStartPx: Int = 14.dp2Px()
    var paddingEndPx: Int = 14.dp2Px()
}