package com.ym.chat.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     author:
 *                                      ___           ___           ___         ___
 *         _____                       /  /\         /__/\         /__/|       /  /\
 *        /  /::\                     /  /::\        \  \:\       |  |:|      /  /:/
 *       /  /:/\:\    ___     ___    /  /:/\:\        \  \:\      |  |:|     /__/::\
 *      /  /:/~/::\  /__/\   /  /\  /  /:/~/::\   _____\__\:\   __|  |:|     \__\/\:\
 *     /__/:/ /:/\:| \  \:\ /  /:/ /__/:/ /:/\:\ /__/::::::::\ /__/\_|:|____    \  \:\
 *     \  \:\/:/~/:/  \  \:\  /:/  \  \:\/:/__\/ \  \:\~~\~~\/ \  \:\/:::::/     \__\:\
 *      \  \::/ /:/    \  \:\/:/    \  \::/       \  \:\  ~~~   \  \::/~~~~      /  /:/
 *       \  \:\/:/      \  \::/      \  \:\        \  \:\        \  \:\         /__/:/
 *        \  \::/        \__\/        \  \:\        \  \:\        \  \:\        \__\/
 *         \__\/                       \__\/         \__\/         \__\/
 *     blog  : http://blankj.com
 *     time  : 16/12/08
 *     desc  : utils about initialization
 * </pre>
 */
public final class VPNUtils {


    @SuppressLint("StaticFieldLeak")
    private static Application sApplication;

    private static final ActivityLifecycleImpl ACTIVITY_LIFECYCLE = new ActivityLifecycleImpl();

    private final static String PERMISSION_ACTIVITY_CLASS_NAME =
            "com.blankj.utilcode.util.PermissionUtils$PermissionActivity";

    private VPNUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * Init utils.
     * <p>Init it in the class of Application.</p>
     *
     * @param context context
     */
    public static void init(final Context context) {
        if (context == null) {
            init(getApplicationByReflect());
            return;
        }
        init((Application) context.getApplicationContext());
    }

    /**
     * Init utils.
     * <p>Init it in the class of Application.</p>
     *
     * @param app application
     */
    public static void init(final Application app) {
        if (sApplication == null) {
            if (app == null) {
                sApplication = getApplicationByReflect();
            } else {
                sApplication = app;
            }
            sApplication.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE);
        }
    }

    /**
     * Return the context of Application object.
     *
     * @return the context of Application object
     */
    public static Application getApp() {
        if (sApplication != null) return sApplication;
        Application app = getApplicationByReflect();
        init(app);
        return app;
    }

    private static Application getApplicationByReflect() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            return (Application) app;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }

    static ActivityLifecycleImpl getActivityLifecycle() {
        return ACTIVITY_LIFECYCLE;
    }

    static LinkedList<Activity> getActivityList() {
        return ACTIVITY_LIFECYCLE.mActivityList;
    }

    static Context getTopActivityOrApp() {
        if (isAppForeground()) {
            Activity topActivity = ACTIVITY_LIFECYCLE.getTopActivity();
            return topActivity == null ? VPNUtils.getApp() : topActivity;
        } else {
            return VPNUtils.getApp();
        }
    }

    static boolean isAppForeground() {
        ActivityManager am =
                (ActivityManager) VPNUtils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        //noinspection ConstantConditions
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return aInfo.processName.equals(VPNUtils.getApp().getPackageName());
            }
        }
        return false;
    }

    static final AdaptScreenArgs ADAPT_SCREEN_ARGS = new AdaptScreenArgs();

    static void restoreAdaptScreen() {
        final DisplayMetrics systemDm = Resources.getSystem().getDisplayMetrics();
        final DisplayMetrics appDm = VPNUtils.getApp().getResources().getDisplayMetrics();
        final Activity activity = ACTIVITY_LIFECYCLE.getTopActivity();
        if (activity != null) {
            final DisplayMetrics activityDm = activity.getResources().getDisplayMetrics();
            if (ADAPT_SCREEN_ARGS.isVerticalSlide) {
                activityDm.density = activityDm.widthPixels / (float) ADAPT_SCREEN_ARGS.sizeInPx;
            } else {
                activityDm.density = activityDm.heightPixels / (float) ADAPT_SCREEN_ARGS.sizeInPx;
            }
            activityDm.scaledDensity = activityDm.density * (systemDm.scaledDensity / systemDm.density);
            activityDm.densityDpi = (int) (160 * activityDm.density);

            appDm.density = activityDm.density;
            appDm.scaledDensity = activityDm.scaledDensity;
            appDm.densityDpi = activityDm.densityDpi;
        } else {
            if (ADAPT_SCREEN_ARGS.isVerticalSlide) {
                appDm.density = appDm.widthPixels / (float) ADAPT_SCREEN_ARGS.sizeInPx;
            } else {
                appDm.density = appDm.heightPixels / (float) ADAPT_SCREEN_ARGS.sizeInPx;
            }
            appDm.scaledDensity = appDm.density * (systemDm.scaledDensity / systemDm.density);
            appDm.densityDpi = (int) (160 * appDm.density);
        }
    }

    static void cancelAdaptScreen() {
        final DisplayMetrics systemDm = Resources.getSystem().getDisplayMetrics();
        final DisplayMetrics appDm = VPNUtils.getApp().getResources().getDisplayMetrics();
        final Activity activity = ACTIVITY_LIFECYCLE.getTopActivity();
        if (activity != null) {
            final DisplayMetrics activityDm = activity.getResources().getDisplayMetrics();
            activityDm.density = systemDm.density;
            activityDm.scaledDensity = systemDm.scaledDensity;
            activityDm.densityDpi = systemDm.densityDpi;
        }
        appDm.density = systemDm.density;
        appDm.scaledDensity = systemDm.scaledDensity;
        appDm.densityDpi = systemDm.densityDpi;
    }

    static boolean isAdaptScreen() {
        final DisplayMetrics systemDm = Resources.getSystem().getDisplayMetrics();
        final DisplayMetrics appDm = VPNUtils.getApp().getResources().getDisplayMetrics();
        return systemDm.density != appDm.density;
    }

    static class AdaptScreenArgs {
        int sizeInPx;
        boolean isVerticalSlide;
    }

    static class ActivityLifecycleImpl implements ActivityLifecycleCallbacks {

        final LinkedList<Activity> mActivityList = new LinkedList<>();
        final HashMap<Object, OnAppStatusChangedListener> mStatusListenerMap = new HashMap<>();

        private int mForegroundCount = 0;
        private int mConfigCount = 0;

        void addListener(final Object object, final OnAppStatusChangedListener listener) {
            mStatusListenerMap.put(object, listener);
        }

        void removeListener(final Object object) {
            mStatusListenerMap.remove(object);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            setTopActivity(activity);
            if (mForegroundCount <= 0) {
                postStatus(true);
            }
            if (mConfigCount < 0) {
                ++mConfigCount;
            } else {
                ++mForegroundCount;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {/**/}

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity.isChangingConfigurations()) {
                --mConfigCount;
            } else {
                --mForegroundCount;
                if (mForegroundCount <= 0) {
                    postStatus(false);
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {/**/}

        @Override
        public void onActivityDestroyed(Activity activity) {
            mActivityList.remove(activity);
        }

        private void postStatus(final boolean isForeground) {
            if (mStatusListenerMap.isEmpty()) return;
            for (OnAppStatusChangedListener onAppStatusChangedListener : mStatusListenerMap.values()) {
                if (onAppStatusChangedListener == null) return;
                if (isForeground) {
                    onAppStatusChangedListener.onForeground();
                } else {
                    onAppStatusChangedListener.onBackground();
                }
            }
        }

        private void setTopActivity(final Activity activity) {
            if (PERMISSION_ACTIVITY_CLASS_NAME.equals(activity.getClass().getName())) return;
            if (mActivityList.contains(activity)) {
                if (!mActivityList.getLast().equals(activity)) {
                    mActivityList.remove(activity);
                    mActivityList.addLast(activity);
                }
            } else {
                mActivityList.addLast(activity);
            }
        }

        Activity getTopActivity() {
            if (!mActivityList.isEmpty()) {
                final Activity topActivity = mActivityList.getLast();
                if (topActivity != null) {
                    return topActivity;
                }
            }
            Activity topActivityByReflect = getTopActivityByReflect();
            if (topActivityByReflect != null) {
                setTopActivity(topActivityByReflect);
            }
            return topActivityByReflect;
        }

        private Activity getTopActivityByReflect() {
            try {
                @SuppressLint("PrivateApi")
                Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
                Field activitiesField = activityThreadClass.getDeclaredField("mActivityList");
                activitiesField.setAccessible(true);
                Map activities = (Map) activitiesField.get(activityThread);
                if (activities == null) return null;
                for (Object activityRecord : activities.values()) {
                    Class activityRecordClass = activityRecord.getClass();
                    Field pausedField = activityRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (!pausedField.getBoolean(activityRecord)) {
                        Field activityField = activityRecordClass.getDeclaredField("activity");
                        activityField.setAccessible(true);
                        return (Activity) activityField.get(activityRecord);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public static final class FileProvider4UtilCode extends FileProvider {

        @Override
        public boolean onCreate() {
            VPNUtils.init(getContext());
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////

    public interface OnAppStatusChangedListener {
        void onForeground();

        void onBackground();
    }


    public static int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 检测是否安装微信
     *
     * @param context
     * @return
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int packageCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    public static String packageName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
            Log.d(ctx.toString(), "本软件的版本名：" + localVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    //检测域名是否可用
    public static boolean checkURL(String url) {
        boolean value = false;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            int code = conn.getResponseCode();
            if (code != 200) {
                value = false;
            } else {
                value = true;
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    public static void myShowToast(Context context, String subMsg) {

        Toast toast = Toast.makeText(context, subMsg + "", Toast.LENGTH_SHORT);
        LinearLayout linearLayout = (LinearLayout) toast.getView();
        TextView messageTextView = (TextView) linearLayout.getChildAt(0);
        messageTextView.setTextSize(14);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     *  *
     *  * 判断某activity是否处于栈顶
     *  * @return true在栈顶 false不在栈顶
     *  
     */
    public static boolean isActivityTop(Class cls, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(cls.getName());
    }

    public static void exitApp(Activity activity){
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList)
        {
            if (runningAppProcessInfo.pid != android.os.Process.myPid())
            {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
//        closeVPN(activity);
        System.runFinalization();
        activity.finish();
        System.exit(0);
    }

    public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

//    public static void closeVPN(Activity activity) {
//        if(LocalVpnService.IsRunning){
//            LocalVpnService.IsRunning = false;
//            LocalVpnService.Instance.disconnectVPN();
//            activity.stopService(new Intent(activity, LocalVpnService.class));
//        }
//    }
}
