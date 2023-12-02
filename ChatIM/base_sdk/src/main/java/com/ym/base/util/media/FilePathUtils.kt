package com.ym.base.util.media

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.constant.MemoryConstants
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.SDCardUtils
import com.blankj.utilcode.util.Utils
import com.ym.base.constant.ConstPath
import java.io.File
import java.io.IOException

object FilePathUtils {
    private var mAppFilePathLasting: String? = null //持久不会被随着卸载自动删除的路径

    private var mAppFilePath: String? = null   //随着卸载自动删除的路径

    /**
     * 公司文件夹名称
     */
    private const val COMPANY_FOLDER = "yongyuan"

    /**
     * APP文件夹名称
     */
    private const val APP_FOLDER = "happyrun"

    /**
     * 图片
     */
    private const val IMAGES = "images"

    fun initAppFile() {
        //==================================希望卸载后跟着删除的====================================
        val mAppFilePath = appPathUnInstallAutoDelte
        //图片
        FileUtils.createOrExistsDir(mAppFilePath + ConstPath.IMAGES)
        //裁剪
        FileUtils.createOrExistsDir(mAppFilePath + ConstPath.CROP)
        //临时
        FileUtils.createOrExistsDir(mAppFilePath + ConstPath.TEMP)
        //视频缓存文件夹
        FileUtils.createOrExistsDir(mAppFilePath + ConstPath.VIDEO)
        //更新文件夹
        FileUtils.createOrExistsDir(mAppFilePath + ConstPath.UPDATA_APK)
        //文件
        FileUtils.createOrExistsDir(mAppFilePath + ConstPath.FILES)
        //录制目录
        FileUtils.createOrExistsDir(mAppFilePath + ConstPath.RECORDER)

        //==================================希望卸载后仍然保存的====================================
        val mAppFilePathLasting = appPathUnInstallNoAutoDelte
        //闪退
        FileUtils.createOrExistsDir(mAppFilePathLasting + ConstPath.CRASH)
        //本地保存的视频
        FileUtils.createOrExistsDir(mAppFilePathLasting + ConstPath.LOCAL_VIDEO)
        //濾鏡
        FileUtils.createOrExistsDir(mAppFilePathLasting + ConstPath.FILTER)
        //字幕文件夹
        FileUtils.createOrExistsDir(mAppFilePathLasting + ConstPath.SUBTITLE)
        //创建字幕资源默认文件夹
        FileUtils.createOrExistsDir(
            mAppFilePathLasting
                    + ConstPath.SUBTITLE
                    + File.separator
                    + ConstPath.DEFAULT
        )
        //==================================区块链相关文件必须存在用户不可访问位置====================================
        //ETH 和JKC 的keystore 的本地文件
        FileUtils.createOrExistsDir(keyStorePath)
        //BTC 的本地文件
        FileUtils.createOrExistsDir(btcFilePathStr)
        //廣告的位置
        FileUtils.createOrExistsDir(adFilePathStr)
        setNomedia()
    }

    /** 返回app的随着卸载会自动清除数据的绝对路径  */
    val appPathUnInstallAutoDelte: String?
        get() {
            if (TextUtils.isEmpty(mAppFilePath) && Utils.getApp().externalCacheDir != null) {
                if (SDCardUtils.isSDCardEnableByEnvironment()) {
                    //  /storage/emulated/0/Android/data/com.caitong.happyrun/cache
                    mAppFilePath = Utils.getApp().externalCacheDir!!.path + File.separator
                } else {
                    //  /data/data/com.caitong.happyrun/cache/FirstWallet
                    mAppFilePath = Utils.getApp().cacheDir.path + File.separator
                }
            }
            return mAppFilePath
        }

    /** 返回app的随着卸载不会自动清除数据的绝对路径  */
    val appPathUnInstallNoAutoDelte: String?
        get() {
            if (TextUtils.isEmpty(mAppFilePathLasting)) {
                if (SDCardUtils.isSDCardEnableByEnvironment()) {
                    //  /storage/emulated/0/FirstWallet/
                    mAppFilePathLasting = SDCardUtils.getSDCardInfo()[0].path + File.separator
                    //+ getAppRelativePath();
                } else {
                    //  /data/data/com.caitong.happyrun/file/FirstWallet/
                    mAppFilePathLasting = Utils.getApp().filesDir.path + appRelativePath
                }
            }
            return mAppFilePathLasting
        }
    val keyStorePath: String
        get() = (Utils.getApp().filesDir.path
                + File.separator
                + ConstPath.KEY_STORE
                + File.separator)
    val btcFilePathStr: String
        get() = (Utils.getApp().filesDir.path
                + File.separator
                + ConstPath.BTC_STORE
                + File.separator)
    val adFilePathStr: String
        get() = (Utils.getApp().filesDir.path
                + File.separator
                + ConstPath.AD_IMAGES
                + File.separator)

    /** 设置无媒体路径  */
    fun setNomedia() {
        val file = File(appPathUnInstallNoAutoDelte + ".nomedia")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** 返回app的相对路径  */
    val appRelativePath: String
        get() = (File.separator
                + ConstPath.COMPANY_FOLDER
                + File.separator)

    /** 获取当前持久缓存大小(需要在子线程中调用),不需要被清理的位置  */
    val lastingCacheSize: String
        get() {
            //==================================希望卸载后不跟着删除的，我们可以计算缓存，但不建议删除====================================
            // String mAppFilePathLasting = getAppPathUnInstallNoAutoDelte();
            //
            // long lengthCrash      = FileUtils.getLength(mAppFilePathLasting + FilePathConstants.CRASH);
            // long lengthLocalVideo = FileUtils.getLength(mAppFilePathLasting + FilePathConstants.LOCAL_VIDEO);
            // long lengthSubtitles  = FileUtils.getLength(mAppFilePathLasting + FilePathConstants.SUBTITLE);
            // long totalSize = lengthCrash
            //                  + lengthLocalVideo
            //                  + lengthSubtitles
            //                  + cacheSize;
            var totalSize = FileUtils.getLength(appPathUnInstallNoAutoDelte)
            if (totalSize < 0) {
                totalSize = 0
            }
            return byte2FitMemorySize(totalSize)
            // return Formatter.formatFileSize(Utils.getApp(), totalSize);
        }

    /**
     * Size of byte to fit size of memory.
     *
     * to three decimal places
     *
     * @param byteSize Size of byte.
     * @return fit size of memory
     * String.format("%0"+length+"d", arr)中的%0和"d"分别代表什么
     * length代表的是格式化字符串的总长度
     * d是个占位符（表示结果被格式化为十进制整数），会被arr所替换。
     * 0是在arr转化为字符后，长度达不到length的时候，前面以0 补足。
     * String.format("%.0f", arr)中的%.0和"f"分别代表什么
     * f是个占位符（表示结果被小数），会被arr所替换。
     * .0是在arr转化为字符后，小数点后小数精度为0，
     * 那么.2就表示小数点后保留两位小数
     */
    @SuppressLint("DefaultLocale")
    fun byte2FitMemorySize(byteSize: Long): String {
        return if (byteSize < 0) {
            "shouldn't be less than zero!"
        } else if (byteSize < MemoryConstants.KB) {
            String.format("%.0fB",byteSize.toDouble())
        } else if (byteSize < MemoryConstants.MB) {
            String.format("%.0fKB",byteSize.toDouble() / MemoryConstants.KB)
        } else if (byteSize < MemoryConstants.GB) {
            String.format("%.0fMB",byteSize.toDouble() / MemoryConstants.MB)
        } else {
            String.format("%.0fGB",byteSize.toDouble() / MemoryConstants.GB)
        }
    }

    /** 关联到一个BTC块文件，这个路径和文件名，也就是创建WalletAppKit传入的路径和文件名，实质是也是创建了BlockStoreFile  */
    fun getBtcBlockStoreFile(fileName: String?): File {
        val mBlockStoreFile = File(btcFilePathStr,fileName)
        if (!mBlockStoreFile.exists()) {
            try {
                val newFile = mBlockStoreFile.createNewFile()
                if (newFile) {
                    return mBlockStoreFile
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return mBlockStoreFile
    }

    /**
     * 清除缓存并返回清除后的大小
     */
    fun clearCache(): String {
        // List<String> filePath       = new ArrayList<>();
        // String       pathImages     = FilePathUtils.getAppPath() + FilePathConstants.IMAGES;//缓存的图片
        // String       pathFiles      = FilePathUtils.getAppPath() + FilePathConstants.FILES;//下载的文件(歌曲、APK等)
        // String       pathRecorder   = FilePathUtils.getAppPath() + FilePathConstants.RECORDER;//录制的文件(非本地已合成的视频)
        // String       pathCrash      = FilePathUtils.getAppPath() + FilePathConstants.CRASH;//崩溃日志
        // String       pathTemp       = FilePathUtils.getAppPath() + FilePathConstants.TEMP;//临时保存的文件(暂时没有使用)
        // String       pathVideo      = FilePathUtils.getAppPath() + FilePathConstants.VIDEO;//缓存的视频文件，每次打开APP自动删除太早的缓冲
        // String       pathCrop       = FilePathUtils.getAppPath() + FilePathConstants.CROP;//裁切的图片
        // String       pathFilter     = FilePathUtils.getAppPath() + FilePathConstants.FILTER;//濾鏡文件(清理后重新解压)
        // File         fileImages     = new File(pathImages);
        // File         fileFiles      = new File(pathFiles);
        // File         fileRecorder   = new File(pathRecorder);
        // File         fileCrash      = new File(pathCrash);
        // File         fileTemp       = new File(pathTemp);
        // File         fileCacheVideo = new File(pathVideo);
        // File         fileCrop       = new File(pathCrop);
        // File         fileFilter     = new File(pathFilter);
        // if (fileImages.exists() && fileImages.listFiles() != null) {
        //     for (File file : fileImages.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // if (fileFiles.exists() && fileFiles.listFiles() != null) {
        //     for (File file : fileFiles.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // if (fileRecorder.exists() && fileRecorder.listFiles() != null) {
        //     for (File file : fileRecorder.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // if (fileCrash.exists() && fileCrash.listFiles() != null) {
        //     for (File file : fileCrash.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // if (fileTemp.exists() && fileTemp.listFiles() != null) {
        //     for (File file : fileTemp.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // if (fileCacheVideo.exists() && fileCacheVideo.listFiles() != null) {
        //     for (File file : fileCacheVideo.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // if (fileCrop.exists() && fileCrop.listFiles() != null) {
        //     for (File file : fileCrop.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // if (fileFilter.exists() && fileFilter.listFiles() != null) {
        //     for (File file : fileFilter.listFiles()) {
        //         if (file != null && file.exists() && file.isFile()) {
        //             filePath.add(file.getAbsolutePath());
        //         }
        //     }
        // }
        // FileUtils.deleteAllInDir(pathImages);
        // FileUtils.deleteAllInDir(pathFiles);
        // FileUtils.deleteAllInDir(pathRecorder);
        // FileUtils.deleteAllInDir(pathCrash);
        // FileUtils.deleteAllInDir(pathTemp);
        // FileUtils.deleteAllInDir(pathVideo);
        // FileUtils.deleteAllInDir(pathCrop);
        // FileUtils.deleteAllInDir(pathFilter);
        FileUtils.deleteAllInDir(Utils.getApp().externalCacheDir)
        FileUtils.deleteAllInDir(Utils.getApp().cacheDir)
        // if (filePath.size() != 0) {
        //     // 标题：三种方法，刷新 Android 的 MediaStore！让你保存的图片立即出现在相册里！
        //     // 作者：承香墨影
        //     // 链接：https://www.jianshu.com/p/bc8b04bffddf
        //     MediaScannerConnection.scanFile(Utils.getApp(), filePath.toArray(new String[]{}), null,
        //                                     null);
        // }
        return cacheSize
    }

    /** 获取当前非持久缓存大小(需要在子线程中调用)  */
    val cacheSize: String
        get() {
            //==================================希望卸载后跟着删除的，我们可以计算缓存====================================
            // String mAppFilePath = getAppPathUnInstallAutoDelte();

            // long lengthImage    = FileUtils.getLength(mAppFilePath + FilePathConstants.IMAGES);
            // long lengthFiles    = FileUtils.getLength(mAppFilePath + FilePathConstants.FILES);
            // long lengthCrop     = FileUtils.getLength(mAppFilePath + FilePathConstants.CROP);
            // long lengthTemp     = FileUtils.getLength(mAppFilePath + FilePathConstants.TEMP);
            // long lengthVideo    = FileUtils.getLength(mAppFilePath + FilePathConstants.VIDEO);
            // long lengthUpdata   = FileUtils.getLength(mAppFilePath + FilePathConstants.UPDATA_APK);
            // long lengthRecorder = FileUtils.getLength(mAppFilePath + FilePathConstants.RECORDER);
            // long lengthFilter     = FileUtils.getLength(mAppFilePathLasting + FilePathConstants.FILTER);
            var totalSize = (FileUtils.getLength(Utils.getApp().externalCacheDir)
                    + FileUtils.getLength(Utils.getApp().cacheDir) //        //压缩的图片
                    //        + FileUtils.getLength(FileKit.getDefaultFileCompressDirectory())
                    //        //视频播放缓存文件
                    //        + FileUtils.getLength(StorageUtils.getIndividualCacheDirectory(Utils.getApp()))
                    )
            if (totalSize < 0) {
                totalSize = 0
            }
            return byte2FitMemorySize(totalSize)
            // return Formatter.formatFileSize(Utils.getApp(), totalSize);
        }

    // 在sd卡中创建一保存图片（原图和缩略图共用的）文件夹
    fun createFileIfNeed(fileName: String?): File? {
        val fileImages = File(appPathUnInstallAutoDelte + ConstPath.IMAGES)
        FileUtils.createOrExistsDir(fileImages)
        val file = File(fileImages,fileName)
        return if (!file.exists()) {
            try {
                file.createNewFile()
                file
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        } else {
            file
        }
    }

    fun getAppPath(context: Context): String {
        var prefix = ""
        prefix = if (!SDCardUtils.getSDCardInfo().isEmpty()) {
            SDCardUtils.getSDCardInfo()[0].path + File.separator
        } else {
            context.cacheDir.path + File.separator
        }
        return (prefix + COMPANY_FOLDER
                + File.separator
                + APP_FOLDER
                + File.separator)
    }
}