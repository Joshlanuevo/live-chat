package com.ym.base.ext

import android.content.Intent
import android.graphics.Color
import android.os.Parcelable
import android.os.SystemClock
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ActivityUtils
import com.google.android.material.tabs.TabLayout
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.luck.picture.lib.PictureSelectionModel
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.DoubleUtils
import com.ym.base.mvvm.BaseViewModel
import com.ym.base.mvvm.activity.NewPictureExternalPreviewActivity
import com.ym.base_sdk.R
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


/**
 * Author:yangcheng
 * Date:2020/8/11
 * Time:17:46
 */
//停止惯性滚动
fun RecyclerView.stopInertiaRolling() {
    try {
        //如果是Support的RecyclerView则需要使用"cancelTouch"
        val field = this.javaClass.getDeclaredMethod("cancelScroll")
        field.isAccessible = true
        field.invoke(this)
    } catch (e: Exception) {
        e.printStackTrace()
        "RecyclerView惯性滚动停止失败:${e.message}".logI()
    }
}

//关闭默认局部刷新动画
fun RecyclerView.closeDefaultAnimator() {
    this.itemAnimator?.let { anim ->
        anim.addDuration = 0
        anim.changeDuration = 0
        anim.moveDuration = 0
        anim.removeDuration = 0
        (anim as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }
}

/*产生随机颜色(不含透明度)*/
fun Color.randomNormal(): Int {
    val r = (Math.random() * 256).toInt()
    val g = (Math.random() * 256).toInt()
    val b = (Math.random() * 256).toInt()
    return Color.rgb(r, g, b)
}

/*产生随机颜色(含透明度)*/
fun Color.randomAlpha(): Int {
    val a = (Math.random() * 256).toInt()
    val r = (Math.random() * 256).toInt()
    val g = (Math.random() * 256).toInt()
    val b = (Math.random() * 256).toInt()
    return Color.argb(a, r, g, b)
}

/**需要明确的一点是，通过 async 启动的协程出现未捕获的异常时会忽略
 * CoroutineExceptionHandler，这与 launch 的设计思路是不同的。*/
inline fun launchError(
    context: CoroutineContext = Dispatchers.Main,
    crossinline handler: (CoroutineContext, Throwable) -> Unit = { _, e -> e.message.logE() },
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend CoroutineScope.() -> Unit,
): Job {
    return GlobalScope.launch(context + CoroutineExceptionHandler(handler), start, block)
}

inline fun <reified T : Any> getDefaultObserver(
    crossinline loading: ((it: BaseViewModel.LoadState<T>) -> Unit) = { },
    crossinline fail: ((it: BaseViewModel.LoadState<T>) -> Unit) = { },
    crossinline success: ((it: BaseViewModel.LoadState<T>) -> Unit) = { },
): Observer<BaseViewModel.LoadState<T>> {
    return Observer<BaseViewModel.LoadState<T>> { state ->
        try {
            when (state) {
                is BaseViewModel.LoadState.Loading -> loading.invoke(state)
                is BaseViewModel.LoadState.Fail -> fail.invoke(state)
                is BaseViewModel.LoadState.Success -> success.invoke(state)
            }
        } catch (e: Exception) {
            fail.invoke(BaseViewModel.LoadState.Fail(e))
        }
    }
}

inline fun PictureSelectionModel.openExternalPreview2(position: Int, medias: List<LocalMedia>) {
    if (!DoubleUtils.isFastDoubleClick()) {
        val ac = ActivityUtils.getTopActivity()
        if (ac != null) {
            val intent = Intent(ac, NewPictureExternalPreviewActivity::class.java)
            intent.putParcelableArrayListExtra(
                PictureConfig.EXTRA_PREVIEW_SELECT_LIST,
                medias as ArrayList<out Parcelable?>
            )
            intent.putExtra(PictureConfig.EXTRA_POSITION, position)
            ac.startActivity(intent)
            ac.overridePendingTransition(R.anim.picture_anim_enter, R.anim.picture_anim_fade_in)
        } else {
            throw NullPointerException("Starting the PictureSelector Activity cannot be empty ")
        }
    }
}

//打印异常
fun Throwable?.logE() {
    this?.message?.logE()
}

//找到所有子fragment
fun Fragment.getAllChildFragments(): MutableList<Fragment> {
    val list = mutableListOf<Fragment>()
    val fragments = this.childFragmentManager.fragments
    if (fragments.isNotEmpty()) {
        list.addAll(fragments)
        fragments.forEach { f -> list.addAll(f.getAllChildFragments()) }
    }
    return list
}

inline fun <reified VM : ViewModel> ViewModelStoreOwner.viewModelGetSelf() = lazy {
    ViewModelProvider(this).get(VM::class.java)
}

inline fun <reified VM : ViewModel> ViewModelStoreOwner.viewModelGet() = lazy {
    if (this is Fragment) {
        ViewModelProvider(this.requireActivity())
    } else {
        ViewModelProvider(this)
    }.get(VM::class.java)
}

class FragmentBindingDelegate<VB : ViewBinding>(
    private val clazz: Class<VB>,
) : ReadOnlyProperty<Fragment, VB> {

    private var lifecycleObserver: LifecycleObserver? = null

    private var binding: VB? = null

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
        if (lifecycleObserver == null) {
            lifecycleObserver = object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroyView() {
                    binding = null
                }
            }.also {
                thisRef.viewLifecycleOwner.lifecycle.addObserver(it)
            }
        }
        if (binding == null) {
            binding = clazz.getMethod("bind", View::class.java)
                .invoke(null, thisRef.requireView()) as VB
        }
        return binding!!
    }
}

//开机时间
fun Long.getOpenSysTime(): Long {
    return System.currentTimeMillis() - SystemClock.elapsedRealtime()
}

//关闭TabLayout的长按悬浮
fun TabLayout.closeLongClick() {
    try {
        (this.getChildAt(0) as? LinearLayout)?.let { ll ->
            for (i in 0 until ll.childCount) ll.getChildAt(i).setOnLongClickListener { true }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 判断手机号(国家编码，中国->CN)
 * @see com.google.i18n.phonenumbers.CountryCodeToRegionCodeMap
 */
fun String?.isPhoneNumber(countryCode: String = "VN"): Boolean {
    if (this.isNullOrBlank()) return false
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val numberProto: Phonenumber.PhoneNumber = phoneUtil.parse(this, countryCode.toUpperCase(Locale.getDefault()))
        phoneUtil.isValidNumber(numberProto)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}