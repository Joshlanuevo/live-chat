package com.ym.chat.db

import android.annotation.SuppressLint
import com.blankj.utilcode.util.Utils
import com.ym.base.ext.launchError
import com.ym.base.ext.logD
import com.ym.base.ext.logE
import com.ym.base.util.save.LoginData
import com.ym.base.util.save.MMKVUtils
import com.ym.chat.bean.AccountBean
import com.ym.chat.bean.MyObjectBox
import io.objectbox.BoxStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object AccountDao {

    var appName = "jixin_name"
    var mBoxStore: BoxStore? = null

    /**
     * 初始化数据库
     */
    @SuppressLint("MissingPermission")
    fun initAccDb(callFinish: ((suc: Boolean) -> Unit)? = null) {
        mBoxStore?.close()
        launchError(handler = { _, e ->
            ChatDao.mBoxStore.close()
            "account数据库异常:${e.message}".logE()
        }) {
            withContext(Dispatchers.IO) {
                MyObjectBox.builder().androidContext(Utils.getApp()).name(appName).build()
            }.let { tempBox ->
                mBoxStore = tempBox
                callFinish?.invoke(true)
                "account数据库init成功".logD()
            }
        }
    }

    /**
     * 存储登录账号数据
     */
    fun saveAccount(
        account: LoginData,
        accountType: Int = 0,
        mobile: String = "",
        password: String = ""
    ) {
        val boxStore = mBoxStore ?: return
        if (account != null) {
            var accountBean = AccountBean(
                0,
                account.id,
                account.username ?: "",
                account.name ?: "",
                account.code ?: "",
                account.memberLevelId ?: "",
                account.gender ?: "",
                mobile ?: "",
                account.email ?: "",
                account.address ?: "",
                account.headUrl ?: "",
                account.sign ?: "",
                account.status ?: "",
                account.onlineStatus ?: "",
                account.recommendCode ?: "",
                account.registerType ?: "",
                account.remark ?: "",
                account.createTime ?: "",
                account.updateTime ?: "",
                accountType,
                account.displayHead ?: "",
                account.headText ?: "",
                account.levelHeadUrl ?: "",
                password = password
            )
            var isExist = false//是否存在
            getAccounts(accountBean.id)?.let { bean ->
                boxStore.boxFor(AccountBean::class.java).remove(bean)//先移除
                isExist = true
            }

            //再添加
            if (isExist) {
                boxStore.boxFor(AccountBean::class.java).put(accountBean)
            }
        }
    }

    /**
     * 获取指定登录账号数据
     */
    private fun getAccounts(userId: String): MutableList<AccountBean> {
        val boxStore = mBoxStore ?: return mutableListOf<AccountBean>()
        return boxStore.boxFor(AccountBean::class.java).query()
            .filter { f -> f.id == userId }
            .build().find()
    }

    /**
     * 更新账号信息
     */
    private fun updateAccounts(userId: String, name: String) {
        mBoxStore?.boxFor(AccountBean::class.java)?.run {
            val userResult = query().filter { it.id == userId }.build().find()
            if (userResult != null && userResult.size > 0) {
                val accUser = userResult[0]
            }
        }
    }

    /**
     * 删除指定登录账号数据
     */
    fun deleteAccount(id: Long) {
        mBoxStore?.boxFor(AccountBean::class.java)?.remove(id)
    }

    /**
     * 获取所有登录账号数据
     */
    fun getAccounts(): MutableList<AccountBean> {
        val boxStore = mBoxStore ?: return mutableListOf()
        return boxStore.boxFor(AccountBean::class.java).query()
            .build().find()//如果存在则更新
    }

    /**
     * 获取登录账号 手机号码
     */
    fun getAccountMobile(): String {
        val boxStore = mBoxStore ?: return ""
        var accounts = boxStore.boxFor(AccountBean::class.java).query()
            .build().find()
        var userId = MMKVUtils.getUser()?.id
        if (accounts != null && accounts.size > 0) {
            accounts.forEach { a ->
                if (a.id == userId) {
                    return a.mobile
                }
            }
        }
        return ""
    }

    /**
     * 获取登录账号 密码
     */
    fun getAccountPwd(): String {
        val boxStore = mBoxStore ?: return ""
        var accounts = boxStore.boxFor(AccountBean::class.java).query()
            .build().find()
        var userId = MMKVUtils.getUser()?.id
        if (accounts != null && accounts.size > 0) {
            accounts.forEach { a ->
                if (a.id == userId) {
                    return a.password
                }
            }
        }
        return ""
    }


}