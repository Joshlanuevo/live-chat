package com.ym.chat.rxhttp

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.Utils
import com.jeremyliao.liveeventbus.LiveEventBus
import com.ym.base.constant.EventKeys
import com.ym.base.ext.launchError
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.util.save.LoginBean
import com.ym.base.util.save.LoginData
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.*
import com.ym.chat.enum.SendCodeType
import com.ym.chat.util.DoubleClick
import com.ym.chat.utils.AppManagerUtils
import com.ym.chat.utils.DeviceInfoUtils
import com.ym.chat.utils.MD5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import rxhttp.RxHttp
import rxhttp.map
import rxhttp.toOtherJson
import rxhttp.wrapper.cahce.CacheMode

/**
 * @Description
 * @Author：CASE
 * @Date：2021-07-14
 * @Time：14:16
 */
object UserRepository : BaseRepository() {
    //<editor-fold defaultstate="collapsed" desc="登录+注册">
    //注册
    suspend fun register(
        mobile: String,
        pwd: String,
        code: String,
        referee: String?,
        name: String,
    ): LoginBean {
        return RxHttp.postJson(ApiUrl.User.register)
            .addAll(getBaseParams().apply {
                put("deviceUuid", DeviceUtils.getUniqueDeviceId())
                put(
                    "deviceDescription",
                    DeviceUtils.getManufacturer() + " ${DeviceUtils.getModel()}"
                )
                put("deviceType", "Android")
                put("mobile", mobile)
                put("password", pwd)
                put("mobileCode", code)
                referee?.let {
                    put("referee", referee)
                }
                if (!TextUtils.isEmpty(name)) {
                    put("username", name)
                }
            })
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .map {
                if (it.code == 200) {
                    //保存用户信息
                    MMKVUtils.putUser(it.data)
                }
                it
            }
            .await()
    }

    //登录
    suspend fun login(
        mobile: String,
        pwd: String,
        username: String
    ): LoginBean {
        return RxHttp.postJson(ApiUrl.User.login)
            .addAll(getBaseParams().apply {
                if (!TextUtils.isEmpty(mobile)) {
                    //手机号登录
                    put("mobile", mobile)
                }
                if (!TextUtils.isEmpty(username)) {
                    //友聊账号登录
                    put("code", username)
                }
                put("password", pwd)
                put("deviceUuid", DeviceUtils.getUniqueDeviceId())
                put("deviceType", "Android")
                put(
                    "deviceDescription",
                    DeviceUtils.getManufacturer() + " ${DeviceUtils.getModel()}"
                )
                put("osVersion", DeviceInfoUtils.getDeviceAndroidVersion())
                put("clientVersion", AppManagerUtils.getVersionName(Utils.getApp()) ?: "")
            })
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .map {
                if (it.code == 200) {
                    //保存用户信息
                    MMKVUtils.putUser(it.data)
                }
                it
            }
            .await()
    }

    //获取验证码
    suspend fun getCode(account: String, sendCodeType: SendCodeType): LoginBean {
        return RxHttp.get(ApiUrl.User.getCode)
            .add("mobile", account)
            .add("smsType", sendCodeType)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .await()
    }

    //获取用户信息
    //isWsSync 是否是 ws多端同步
    suspend fun getUserInfo(isWsSync: Boolean = false): LoginBean {
        return RxHttp.get(ApiUrl.User.userInfo)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .map {
                if (it.code == 200) {
                    //保存用户信息
                    MMKVUtils.putUser(it.data)
                    if (isWsSync) {
                        //更新界面
                        LiveEventBus.get(EventKeys.EDIT_USER, String::class.java).post("")
                    }
                }
                it
            }
            .await()
    }

    //根据会员id获取用户信息
    suspend fun memberIdGetUserInfo(memberId: String): LoginBean {
        return RxHttp.get(ApiUrl.User.memberGetUserInfo)
            .add("memberId", memberId)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .await()
    }

    //登出
    suspend fun loginOut(): LoginBean {
        return RxHttp.get(ApiUrl.User.loginOut)
            .add("deviceType", "Android")
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>().await()
    }

    //<editor-fold defaultstate="collapsed" desc="拉取个人信息变化">
    private val doubleClick = DoubleClick()
    val myUserLiveData: MutableLiveData<LoginData?> = MutableLiveData()
    val refreshUserInfo: MutableLiveData<BaseViewModel.LoadState<LoginData>> = MutableLiveData()

    //刷新个人信息
    private var mJobUserInfo: Job? = null
    fun cancelRefreshUserInfo() {
        mJobUserInfo?.cancel()
    }

    fun refreshUserInfo() {
        if (refreshUserInfo.value is BaseViewModel.LoadState.Loading) return
        if (mJobUserInfo?.isActive == true) return
        //3s限制的请求用户信息，及时更新的同时也不忘记限制太过于频繁
        if (doubleClick.isDoubleClick(3000)) return
        mJobUserInfo = launchError(
            handler = { _, e ->
                refreshUserInfo.value = BaseViewModel.LoadState.Fail(e)
                e.printStackTrace()
            }
        ) {
            refreshUserInfo.value = BaseViewModel.LoadState.Loading()
            withContext(Dispatchers.Default) {
                RxHttp.get(ApiUrl.User.userInfo)
                    .addAllHeader(getBaseHeaders())
                    .setCacheMode(CacheMode.ONLY_NETWORK)
                    .toOtherJson<LoginBean>()
                    .await()
            }.let { result ->
                //成功表示网络请求结束,但是不代表数据就可用
                if (result.code == 200 && result.data != null) {
                    MMKVUtils.putUser(result.data)
                    myUserLiveData.value = result.data
                    refreshUserInfo.value = BaseViewModel.LoadState.Success(result.data)
                } else {
                    refreshUserInfo.value = BaseViewModel.LoadState.Fail(Throwable("false"))
                }
            }
        }
    }
    //</editor-fold>

    //修改用户信息
    suspend fun editUserInfo(
        name: String? = null,
        headUrl: String? = null,
        sign: String? = null,
        gender: String? = null
    ): LoginBean {
        return RxHttp.putJson(ApiUrl.User.editUserInfo)
            .addAll(getBaseParams().apply {
                MMKVUtils.getUser()?.id?.let { put("id", it) }
                if (!TextUtils.isEmpty(name)) {
                    //名字
                    name?.let { put("name", it) }
                }
                if (!TextUtils.isEmpty(headUrl)) {
                    //头像
                    headUrl?.let { put("headUrl", it) }
                }
                if (!TextUtils.isEmpty(gender)) {
                    //签名
                    sign?.let { put("sign", it) }
                }
                if (!TextUtils.isEmpty(gender)) {
                    //性别
                    gender?.let { put("gender", it) }
                }
            })
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .map {
                if (it.code == 200 && it.data != null) {
                    //保存用户信息
                    MMKVUtils.putUser(it.data)
                }
                it
            }
            .await()
    }

    //修改用户名友聊号
    suspend fun editUsername(
        username: String
    ): LoginBean {
        return RxHttp.putJson(ApiUrl.User.modifyUsername)
            .addAll(getBaseParams().apply {
                put("username", username)
            })
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .await()
    }

    //修改用户密码
    suspend fun changedUserPwd(
        oldPaw: String,
        newPaw1: String, newPaw2: String
    ): BaseBean<String> {
        return RxHttp.putJson(ApiUrl.User.editUserPaw)
            .addAll(getBaseParams().apply {
                MMKVUtils.getUser()?.id?.let { put("id", it) }
                put("oldPassword", MD5.MD532(oldPaw).lowercase())
                put("password", MD5.MD532(newPaw1).lowercase())
                put("confirmPassword", MD5.MD532(newPaw2).lowercase())
            })
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

    //重置用户密码
    suspend fun forgetUserPwd(
        mobile: String,
        mobileCode: String,
        newPaw1: String, newPaw2: String
    ): LoginBean {
        return RxHttp.putJson(ApiUrl.User.forgetPassword)
            .addAll(getBaseParams().apply {
                put("mobile", mobile)
                put("mobileCode", mobileCode)
                put("password", MD5.MD532(newPaw1).lowercase())
                put("confirmPassword", MD5.MD532(newPaw2).lowercase())
            })
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .await()
    }

    //修改手机号
    suspend fun modifyPhone(
        mobile: String,
        mobileCode: String, password: String
    ): BaseBean<String> {
        return RxHttp.putJson(ApiUrl.User.modifyPhone)
            .addAll(getBaseParams().apply {
                MMKVUtils.getUser()?.id?.let { put("id", it) }
                put("mobile", mobile)
                put("mobileCode", mobileCode)
                put("password", MD5.MD532(password).lowercase())
            })
            .addAllHeader(getBaseHeaders())
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<String>>()
            .await()
    }

    //修改用户状态
    suspend fun changeModifyStatus(status: String): LoginBean {
        return RxHttp.putJson(ApiUrl.User.modifyStatus)
            .addAll(getBaseParams().apply {
                MMKVUtils.getUser()?.id?.let { put("id", it) }
                //状态：启用 enable,禁用 disable,注销 closed
                put("status", status)
            })
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>().await()
    }

    //获取版本信息
    suspend fun getAppVersion(): BaseBean<VersionBean> {
        return RxHttp.get(ApiUrl.Version.getAppVersion)
            .add("type", "Android")
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<VersionBean>>().await()
    }

    //获取友聊号的秘文
    suspend fun getRefereeLink(): BaseBean<EncodeBean> {
        return RxHttp.get(ApiUrl.Chat.getRefereeLink)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<EncodeBean>>().await()
    }

    //获取注册方式
    suspend fun isMobileRegister(): BaseBean<RegisterConfigBean> {
        return RxHttp.get(ApiUrl.User.isMobileRegister)
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<RegisterConfigBean>>().await()
    }


    //多端登录获取验证码
    suspend fun sendVerifyCode(codeOrMobile: String): BaseBean<Boolean> {
        return RxHttp.postJson(ApiUrl.User.sendVerifyCode)
            .add("codeOrMobile", codeOrMobile)
            .add("deviceUuid", DeviceUtils.getUniqueDeviceId())
            .add(
                "deviceDescription",
                DeviceUtils.getManufacturer() + " ${DeviceUtils.getModel()}"
            )
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<BaseBean<Boolean>>().await()
    }

    //校验验证码
    suspend fun checkVerifyCode(codeOrMobile: String, verifyCode: String): LoginBean {
        return RxHttp.postJson(ApiUrl.User.checkVerifyCode)
            .add("codeOrMobile", codeOrMobile)
            .add("verifyCode", verifyCode)
            .add("deviceUuid", DeviceUtils.getUniqueDeviceId())
            .add("deviceType", "Android")
            .add(
                "deviceDescription",
                DeviceUtils.getManufacturer() + " ${DeviceUtils.getModel()}"
            )
            .add("osVersion", DeviceInfoUtils.getDeviceAndroidVersion())
            .add("clientVersion", AppManagerUtils.getVersionName(Utils.getApp()) ?: "")
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>().map {
                if (it.code == 200) {
                    //保存用户信息
                    MMKVUtils.putUser(it.data)
                }
                it
            }.await()
    }


    //建议反馈
    suspend fun sendFeedBack(
        content: String,
        createTime: String? = "",
        fileUrlList: List<String>,
        id: String? = "",
        memberId: String? = "",
        status: String? = "",
        type: String
    ): LoginBean {
        return RxHttp.postJson(ApiUrl.Version.sendFeedBack)
            .addAll(getBaseParams().apply {
                MMKVUtils.getUser()?.id?.let { put("memberId", it) }
                MMKVUtils.getUser()?.code?.let { put("code", it) }
                put("clientType", "Android")
                put("content", content)
                put("type", type)
                if (fileUrlList.isNullOrEmpty()) {
                    put("hasAnnes", "N")
                } else {
                    put("fileUrlList", fileUrlList)
                    put("hasAnnes", "Y")
                }
            })
            .setCacheMode(CacheMode.ONLY_NETWORK)
            .toOtherJson<LoginBean>()
            .await()
    }


}