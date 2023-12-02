package com.ym.chat.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import com.blankj.utilcode.util.Utils;
import java.io.*;
import java.util.Locale;

/**
 * 获取设备信息
 */
public class DeviceInfoUtils {

    /**
     * 获取设备宽度（px）
     */
    public static int getDeviceWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取设备高度（px）
     */
    public static int getDeviceHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取设备的唯一标识， 需要 “android.permission.READ_Phone_STATE”权限
     */
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String deviceId = tm.getDeviceId();
        if (deviceId == null) {
            return "UnKnown";
        } else {
            return deviceId;
        }
    }

    /**
     * 获取厂商名
     **/
    public static String getDeviceManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    /**
     * 获取产品名
     **/
    public static String getDeviceProduct() {
        return android.os.Build.PRODUCT;
    }

    /**
     * 获取手机品牌
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机型号
     */
    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机主板名
     */
    public static String getDeviceBoard() {
        return android.os.Build.BOARD;
    }

    /**
     * 设备名
     **/
    public static String getDeviceDevice() {
        return android.os.Build.DEVICE;
    }

    /**
     * fingerprit 信息
     **/
    public static String getDeviceFubgerprint() {
        return android.os.Build.FINGERPRINT;
    }

    /**
     * 硬件名
     **/
    public static String getDeviceHardware() {
        return android.os.Build.HARDWARE;
    }

    /**
     * 主机
     **/
    public static String getDeviceHost() {
        return android.os.Build.HOST;
    }

    /**
     * 显示ID
     **/
    public static String getDeviceDisplay() {
        return android.os.Build.DISPLAY;
    }

    /**
     * ID
     **/
    public static String getDeviceId() {
        return android.os.Build.ID;
    }

    /**
     * 获取手机用户名
     **/
    public static String getDeviceUser() {
        return android.os.Build.USER;
    }

    /**
     * 获取手机 硬件序列号
     **/
    public static String getDeviceSerial() {
        return android.os.Build.SERIAL;
    }

    /**
     * 获取手机Android 系统SDK
     *
     * @return
     */
    public static int getDeviceSDK() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机Android 版本
     *
     * @return
     */
    public static String getDeviceAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取当前手机系统语言。
     */
    public static String getDeviceDefaultLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     */
    public static String getDeviceSupportLanguage() {
        Log.e("wangjie", "Local:" + Locale.GERMAN);
        Log.e("wangjie", "Local:" + Locale.ENGLISH);
        Log.e("wangjie", "Local:" + Locale.US);
        Log.e("wangjie", "Local:" + Locale.CHINESE);
        Log.e("wangjie", "Local:" + Locale.TAIWAN);
        Log.e("wangjie", "Local:" + Locale.FRANCE);
        Log.e("wangjie", "Local:" + Locale.FRENCH);
        Log.e("wangjie", "Local:" + Locale.GERMANY);
        Log.e("wangjie", "Local:" + Locale.ITALIAN);
        Log.e("wangjie", "Local:" + Locale.JAPAN);
        Log.e("wangjie", "Local:" + Locale.JAPANESE);
        return Locale.getAvailableLocales().toString();
    }


    private static final int INTERNAL_STORAGE = 0;
    private static final int EXTERNAL_STORAGE = 1;

    /**
     * 获取 手机 RAM 信息
     */
    public static String getRAMInfo(Context context) {
        long totalSize = 0;
        long availableSize = 0;

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        totalSize = memoryInfo.totalMem;
        availableSize = memoryInfo.availMem;

        return "可用/总共：" + Formatter.formatFileSize(context, availableSize)
                + "/" + Formatter.formatFileSize(context, totalSize);
    }

    /**
     * 判断SD是否挂载
     */
    public static boolean isSDCardMount() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取 手机 RAM 信息 方法 一
     */
    public static String getTotalRAM(Context context) {
        long size = 0;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(outInfo);
        size = outInfo.totalMem;

        return Formatter.formatFileSize(context, size);
    }

    /**
     * 手机 RAM 信息 方法 二
     */
    public static String getTotalRAMOther(Context context) {
        String path = "/proc/meminfo";
        String firstLine = null;
        int totalRam = 0;
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader, 8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (firstLine != null) {

            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine)
                    / (1024 * 1024)).doubleValue()));

            long totalBytes = 0;

        }

        return Formatter.formatFileSize(context, totalRam);
    }

    /**
     * 获取 手机 可用 RAM
     */
    public static String getAvailableRAM(Context context) {
        long size = 0;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(outInfo);
        size = outInfo.availMem;

        return Formatter.formatFileSize(context, size);
    }

    /**
     * 获取手机内部存储空间
     *
     * @param context
     * @return 以M, G为单位的容量
     */
    public static String getTotalInternalMemorySize(Context context) {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        long size = blockCountLong * blockSizeLong;
        return Formatter.formatFileSize(context, size);
    }

    /**
     * 获取手机内部可用存储空间
     *
     * @param context
     * @return 以M, G为单位的容量
     */
    public static String getAvailableInternalMemorySize(Context context) {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        return Formatter.formatFileSize(context, availableBlocksLong
                * blockSizeLong);
    }

    /**
     * 获取手机外部存储空间
     *
     * @param context
     * @return 以M, G为单位的容量
     */
    public static String getTotalExternalMemorySize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getBlockCountLong();
        return Formatter
                .formatFileSize(context, blockCountLong * blockSizeLong);
    }

    /**
     * 获取手机外部可用存储空间
     *
     * @param context
     * @return 以M, G为单位的容量
     */
    public static String getAvailableExternalMemorySize(Context context) {
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        return Formatter.formatFileSize(context, availableBlocksLong
                * blockSizeLong);
    }

    /**
     * SD 卡信息
     */

    public static String getSDCardInfo() {

        SDCardInfo sd = new SDCardInfo();
        if (!isSDCardMount())
            return "SD card 未挂载!";

        sd.isExist = true;
        StatFs sf = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());

        sd.totalBlocks = sf.getBlockCountLong();
        sd.blockByteSize = sf.getBlockSizeLong();
        sd.availableBlocks = sf.getAvailableBlocksLong();
        sd.availableBytes = sf.getAvailableBytes();
        sd.freeBlocks = sf.getFreeBlocksLong();
        sd.freeBytes = sf.getFreeBytes();
        sd.totalBytes = sf.getTotalBytes();
        return sd.toString();
    }

    public static class SDCardInfo {
        boolean isExist;
        long totalBlocks;
        long freeBlocks;
        long availableBlocks;
        long blockByteSize;
        long totalBytes;
        long freeBytes;
        long availableBytes;

        @Override
        public String toString() {
            return "isExist=" + isExist + "\ntotalBlocks=" + totalBlocks
                    + "\nfreeBlocks=" + freeBlocks + "\navailableBlocks="
                    + availableBlocks + "\nblockByteSize=" + blockByteSize
                    + "\ntotalBytes=" + totalBytes + "\nfreeBytes=" + freeBytes
                    + "\navailableBytes=" + availableBytes;
        }
    }

    // add start by wangjie for SDCard TotalStorage
    public static String getSDCardTotalStorage(long totalByte) {

        double byte2GB = totalByte / 1024.00 / 1024.00 / 1024.00;
        double totalStorage;
        if (byte2GB > 1) {
            totalStorage = Math.ceil(byte2GB);
            if (totalStorage > 1 && totalStorage < 3) {
                return 2.0 + "GB";
            } else if (totalStorage > 2 && totalStorage < 5) {
                return 4.0 + "GB";
            } else if (totalStorage >= 5 && totalStorage < 10) {
                return 8.0 + "GB";
            } else if (totalStorage >= 10 && totalStorage < 18) {
                return 16.0 + "GB";
            } else if (totalStorage >= 18 && totalStorage < 34) {
                return 32.0 + "GB";
            } else if (totalStorage >= 34 && totalStorage < 50) {
                return 48.0 + "GB";
            } else if (totalStorage >= 50 && totalStorage < 66) {
                return 64.0 + "GB";
            } else if (totalStorage >= 66 && totalStorage < 130) {
                return 128.0 + "GB";
            }
        } else {
            // below 1G return get values
            totalStorage = totalByte / 1024.00 / 1024.00;

            if (totalStorage >= 515 && totalStorage < 1024) {
                return 1 + "GB";
            } else if (totalStorage >= 260 && totalStorage < 515) {
                return 512 + "MB";
            } else if (totalStorage >= 130 && totalStorage < 260) {
                return 256 + "MB";
            } else if (totalStorage > 70 && totalStorage < 130) {
                return 128 + "MB";
            } else if (totalStorage > 50 && totalStorage < 70) {
                return 64 + "MB";
            }
        }

        return totalStorage + "GB";
    }


    //获取ip地址
    public static String getInNetIp() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) Utils.getApp().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }


    //这段是转换成点分式IP的码
    private static String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }


}
