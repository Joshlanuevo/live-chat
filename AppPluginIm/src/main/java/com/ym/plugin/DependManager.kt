package com.ym.plugin

object Versions {
    const val appcompat = "1.3.0"
    const val annotation = "1.1.0"
    const val autosize = "1.2.1"
    const val backgroundCore = "1.6.5"
    const val bankjUtilcodex = "1.30.5"
    const val banner = "2.1.0"
    const val baseAdapter = "3.0.4"
    const val cardview = "1.0.0"
    const val circleImageView = "3.1.0"
    const val coil = "1.4.0"
    const val constraintlayout = "2.0.4"
    const val core = "1.3.2"
    const val espressoCore = "3.3.0"
    const val extJunit = "1.1.2"
    const val fragment = "1.3.1"
    const val gson = "2.8.6"
    const val junit = "4.+"
    const val kotlin_version = "1.5.10"
    const val kotlinxCoroutines = "1.4.2"
    const val liveEventBus = "1.7.3"
    const val loopView = "0.2.2"
    const val magicIndicator = "1.7.0"
    const val material = "1.3.0"
    const val okhttp3 = "4.9.1"
    const val rxAndroid = "2.1.1"
    const val rxhttp = "2.8.3"
    const val rxJava = "2.2.10"
    const val rxPermission = "0.12"
    const val rxLife = "2.1.0"
    const val smartRefresh = "2.0.3"
    const val smartRefresh_classics = "2.0.3"
    const val tencentMmkv = "1.2.7"
    const val viewbinding = "1.1.0"
    const val pickerView = "1.1.0"
    const val stickItemDecoration = "1.3.2"
    const val logger = "2.2.0"
    const val xxpermissions = "13.2"
    const val patternLocker = "2.5.7"
    const val ankoVersion = "0.10.8"
    const val immersionbar = "3.0.0"
    const val immersionbar_ktx = "3.0.0"
}

/**
 * Kotlin扩展相关库
 */
object Ktx {
    // 以下都是可选，请根据需要进行添加
    const val viewbinding = "com.dylanc:viewbinding-ktx:${Versions.viewbinding}"
    const val viewbinding_nonreflection = "com.dylanc:viewbinding-nonreflection-ktx:${Versions.viewbinding}"
    const val viewbinding_base = "com.dylanc:viewbinding-base-ktx:${Versions.viewbinding}"
    const val viewbinding_brvah = "com.dylanc:viewbinding-brvah-ktx:${Versions.viewbinding}"
}

/**
 * Material相关库
 */
object Material {
    const val material = "com.google.android.material:material:${Versions.material}"
}

/**
 * AndroidX相关库
 */
object AndroidX {
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val cardview = "androidx.cardview:cardview:${Versions.cardview}"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    const val core = "androidx.core:core:${Versions.core}"
    const val core_ktx = "androidx.core:core-ktx:${Versions.core}"
    const val core_role = "androidx.core:core-role:${Versions.core}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    const val extJunit = "androidx.test.ext:junit:${Versions.extJunit}"
    const val fragment = "androidx.fragment:fragment:${Versions.fragment}"
    const val fragment_ktx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
}

object Http {
    const val okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okhttp3}"
    const val rxhttp = "com.github.liujingxing.rxhttp:rxhttp:${Versions.rxhttp}"
    const val rxhttpCompiler = "com.github.liujingxing.rxhttp:rxhttp-compiler:${Versions.rxhttp}"
}

object Coil {
    const val coil = "io.coil-kt:coil:${Versions.coil}"
    const val coilGif = "io.coil-kt:coil-gif:${Versions.coil}"
    const val coilVideo = "io.coil-kt:coil-video:${Versions.coil}"
}

/**
 * 其他
 */
object Depend {
    const val autosize = "me.jessyan:autosize:${Versions.autosize}"
    const val backgroundCore = "com.noober.background:core:${Versions.backgroundCore}"
    const val bankjUtilcodex = "com.blankj:utilcodex:${Versions.bankjUtilcodex}"
    const val banner = "com.youth.banner:banner:${Versions.banner}"
    const val baseAdapter = "com.github.CymChad:BaseRecyclerViewAdapterHelper:${Versions.baseAdapter}"
    const val circleImageView = "de.hdodenhof:circleimageview:${Versions.circleImageView}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val junit = "junit:junit:${Versions.junit}"
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_version}"
    const val kotlinxCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlinxCoroutines}"
    const val liveEventBus = "com.jeremyliao:live-event-bus-x:${Versions.liveEventBus}"
    const val loopView = "com.weigan:loopView:${Versions.loopView}"
    const val magicIndicator = "com.github.hackware1993:MagicIndicator:${Versions.magicIndicator}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJava}"
    const val rxPermissiona = "com.github.tbruyelle:rxpermissions:${Versions.rxPermission}"
    const val rxlife = "com.github.liujingxing.rxlife:rxlife-rxjava2:${Versions.rxLife}"
    const val rxlifeCoroutine = "com.github.liujingxing.rxlife:rxlife-coroutine:${Versions.rxLife}"
    const val smartRefresh = "com.scwang.smart:refresh-layout-kernel:${Versions.smartRefresh}"
    const val smartRefresh_classics = "com.scwang.smart:refresh-header-classics:${Versions.smartRefresh_classics}"
    const val tencentMmkv = "com.tencent:mmkv-static:${Versions.tencentMmkv}"
    const val pickerView = "com.github.limxing:DatePickerView:${Versions.pickerView}"
    const val stickItemDecoration = "com.oushangfeng:PinnedSectionItemDecoration:${Versions.stickItemDecoration}-androidx"
    const val logger = "com.orhanobut:logger:2.2.0"
    const val txVideo = "com.tencent.liteav:LiteAVSDK_Player:latest.release"

    //https://github.com/getActivity/XXPermissions
    const val xxpermissions = "com.github.getActivity:XXPermissions:${Versions.xxpermissions}"
    const val picker = "com.contrarywind:Android-PickerView:4.1.9"
    const val basePopup = "com.github.razerdp:BasePopup:2.2.20"

    //https://github.com/lihangleo2/ShadowLayout    可定制化阴影的万能阴影布局
    const val shadowLayout = "com.github.lihangleo2:ShadowLayout:3.2.0"
    const val shadowLayoutMosect = "com.github.Mosect:ViewUtils:1.0.8"
    const val viewUtilsMosect = "com.github.Mosect:AShadow2:2.0.6"
    const val patternLocker = "com.github.ihsg:PatternLocker:${Versions.patternLocker}"
    const val anko = "org.jetbrains.anko:anko-commons:${Versions.ankoVersion}"

    const val mqtt = "org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0"
    const val mqtt4android = "org.eclipse.paho:org.eclipse.paho.android.service:1.1.1"

    const val sonic = "com.tencent.sonic:sdk:3.1.0"

    //手机号判断 https://github.com/google/libphonenumber
    const val libphonenumber = "com.googlecode.libphonenumber:libphonenumber:8.12.22"

    //https://github.com/yipianfengye/android-zxingLibrary  //小而且简单
    const val zxingLibrary = "cn.yipianfengye.android:zxing-library:2.2"
    const val rxandroid = "io.reactivex.rxjava3:rxandroid:3.0.0"
    const val android_startup = "io.github.idisfkj:android-startup:1.0.62"
    const val companyEdittext = "com.github.OneGreenHand:CompanyEdittext:v1.0"
    const val ping = "com.github.stealthcopter:AndroidNetworkTools:0.4.5.3"
    const val fontresize = "com.github.liujingxing:fontresize:1.1.0"
    const val xPopupWindow = "com.github.XuDeveloper:XPopupWindow:1.0.1"
    const val websocket = "org.java-websocket:Java-WebSocket:1.5.2"
    //数据库浏览服务
    const val debugObjectBrowser = "io.objectbox:objectbox-android-objectbrowser:3.0.1"
    const val releaseObjectbrowser = "io.objectbox:objectbox-android:3.0.1"

    const val dkplayerJava = "xyz.doikki.android.dkplayer:dkplayer-java:3.3.5"
    const val dkplayerUI = "xyz.doikki.android.dkplayer:dkplayer-ui:3.3.5"
    const val dkplayerExo = "xyz.doikki.android.dkplayer:player-exo:3.3.5"
    const val work = "androidx.work:work-runtime:2.5.0"
    const val loadingView = "com.github.ybq:Android-SpinKit:1.4.0"
    const val fragmentationx = "me.yokeyword:fragmentationx:1.0.2"

    const val commons = "commons-codec:commons-codec:1.9"
    const val flexbox = "com.google.android:flexbox:1.0.0"
    const val immersionbar = "com.gyf.immersionbar:immersionbar:${Versions.immersionbar}"
    const val immersionbar_ktx = "com.gyf.immersionbar:immersionbar-ktx:${Versions.immersionbar_ktx}"
}