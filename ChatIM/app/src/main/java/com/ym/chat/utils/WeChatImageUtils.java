package com.ym.chat.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.blankj.utilcode.util.PathUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * @ClassName WeChatImageUtils
 * @Description 高仿微信的图片宽高比, 获取小视频的参数
 * @Author dhl
 * @Date 2021/1/7 13:49
 * @Version 1.0
 */
public class WeChatImageUtils {

    private static final String TAG = "WeChatImageUtils";

    private static int deviceWidth;

    /**
     * 适当的宽高
     *
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static int[] getImageSizeByOrgSizeToWeChat(int outWidth, int outHeight, Context context) {
        if (deviceWidth < 1) {
            deviceWidth = DeviceInfoUtils.getDeviceWidth(context);
        }
        int imageWidth = 300;
        int imageHeight = 300;
        int maxWidth = deviceWidth / 2;
        int maxHeight = deviceWidth;
        int minWidth = 100;
        int minHeight = 100;
        if (outWidth == 0 && outHeight == 0) {
            return new int[]{imageWidth, imageHeight};
        }
        if (((float) outWidth) / maxWidth > ((float) outHeight) / maxHeight) {//宽的比例大于高的比例
            if (outWidth >= maxWidth) {
                imageWidth = maxWidth;
                imageHeight = outHeight * maxWidth / outWidth;
            } else {
                imageWidth = outWidth;
                if (imageWidth < minWidth) {
                    imageWidth = minWidth;
                }
                imageHeight = outHeight;
            }
            if (imageHeight < minHeight) {
                imageHeight = minHeight;
                int width = outWidth * minHeight / outHeight;
                imageWidth = Math.min(width, maxWidth);
            }
        } else {
            if (outHeight >= maxHeight) {
                imageHeight = maxHeight;
//                if (outHeight / maxHeight > 10) {
//                    imageWidth = outWidth * 5 * maxHeight / outHeight;
//                } else {
                imageWidth = outWidth * maxHeight / outHeight;
                //               }
            } else {
                imageHeight = outHeight;
                if (imageHeight < minHeight) {
                    imageHeight = minHeight;
                }
                imageWidth = outWidth;
            }
            if (imageWidth < minWidth) {
                imageWidth = minWidth;
                int height = outHeight * minWidth / outWidth;
                imageHeight = Math.min(height, maxHeight);
            }
        }

        int displayWidth = deviceWidth / 3;
        if (imageWidth < displayWidth && imageHeight < displayWidth) {
            if (imageWidth > imageHeight) {
                imageHeight = displayWidth * imageHeight / imageWidth;
                imageWidth = displayWidth;
            } else {
                imageWidth = displayWidth * imageWidth / imageHeight;
                imageHeight = displayWidth;
            }
        }
//        Log.d(TAG, "getImageSizeByOrgSizeToWeChat: " + "---imageWidth=" + imageWidth +
//                "----imageHeight=" + imageHeight + "---outWidth=" + outWidth + "---outHeight=" + outHeight
//                + "---maxWidth=" + maxWidth + "---maxHeight=" + maxHeight
//                + "---minWidth=" + minWidth + "---minHeight=" + minHeight);

        return new int[]{imageWidth, imageHeight};
    }


    /**
     * 适当的宽高
     *
     * @param outWidth
     * @param outHeight
     * @return
     */
    public static int[] getImageSizeByOrgSize(int outWidth, int outHeight, Context context) {
        if (deviceWidth < 1) {
            deviceWidth = DeviceInfoUtils.getDeviceWidth(context);
        }
        int imageWidth = 300;
        int imageHeight = 300;
        int maxWidth = deviceWidth / 2;
//        int maxHeight = 800;
        int minWidth = deviceWidth / 3;
//        int minHeight = 300;
        if (outWidth == 0 && outHeight == 0) {
            return new int[]{imageWidth, imageHeight};
        }

        if (outWidth >= maxWidth) {//
            imageWidth = maxWidth;
            imageHeight = outHeight * maxWidth / outWidth;
        } else {
            imageWidth = outWidth;
            imageHeight = outHeight;
            if (imageWidth < minWidth) {
                imageWidth = minWidth;
                imageHeight = outHeight * minWidth / outWidth;
            }
        }

//        if (msg.getImgWidth() > msg.getImgHeight()) {
//            params.width = DEFAULT_MAX_SIZE;
//            params.height = DEFAULT_MAX_SIZE * msg.getImgHeight() / msg.getImgWidth();
//        } else {
//            params.width = DEFAULT_MAX_SIZE * msg.getImgWidth() / msg.getImgHeight();
//            params.height = DEFAULT_MAX_SIZE;
//        }
//        Log.d(TAG, "getImageSizeByOrgSizeToWeChat: " + "---imageWidth=" + imageWidth + "---outWidth=" + outWidth + "----imageHeight=" + imageHeight + "---outHeight=" + outHeight);
        return new int[]{imageWidth, imageHeight};
    }


    public static int[] getImageSize(int outWidth, int outHeight, Context context) {
        if (deviceWidth < 1) {
            deviceWidth = DeviceInfoUtils.getDeviceWidth(context);
        }
        int imageWidth = 300;
        int imageHeight = 300;
        int maxWidth = deviceWidth / 2;
        int maxHeight = 800;
        int minWidth = deviceWidth / 3;
//        int minHeight = 300;
        if (outWidth == 0 && outHeight == 0) {
            return new int[]{imageWidth, imageHeight};
        }

        if (outWidth >= maxWidth) {//
            imageWidth = maxWidth;
            imageHeight = outHeight * maxWidth / outWidth;
        } else {
            imageWidth = outWidth;
            imageHeight = outHeight;
            if (imageWidth < minWidth) {
                imageWidth = minWidth;
                imageHeight = outHeight * minWidth / outWidth;
            }
        }


//        if (msg.getImgWidth() > msg.getImgHeight()) {
//            params.width = DEFAULT_MAX_SIZE;
//            params.height = DEFAULT_MAX_SIZE * msg.getImgHeight() / msg.getImgWidth();
//        } else {
//            params.width = DEFAULT_MAX_SIZE * msg.getImgWidth() / msg.getImgHeight();
//            params.height = DEFAULT_MAX_SIZE;
//        }
//        Log.d(TAG, "getImageSizeByOrgSizeToWeChat: " + "---imageWidth=" + imageWidth + "---outWidth=" + outWidth + "----imageHeight=" + imageHeight + "---outHeight=" + outHeight);
        return new int[]{imageWidth, imageHeight};
    }

    /**
     * 获取图片的尺寸，只获取尺寸，不加载bitmap 不会耗时
     */
    public static int[] getSize(File file) {
        if (file == null) return new int[]{0, 0};
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        return new int[]{opts.outWidth, opts.outHeight};
    }
    /**
     * 获取小视频的参数的尺寸，时长
     *
     */

//    /**
//     * 获取小视频的参数
//     * 通过第一帧获取宽高
//     * 通过源文件获取会有点小问题
//     */
//    public static VideoParam getVideoParam(String path){
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(path);
//        VideoParam videoParam = null;
//        long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) ;
//        Bitmap bitmap = retriever.getFrameAtTime();
//        if(bitmap != null){
//            int width = bitmap.getWidth();
//            int height = bitmap.getHeight();
//            int[] imageSize = WeChatImageUtils.getImageSizeByOrgSizeToWeChat(width,height);
//            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, imageSize[0]/2, imageSize[1]/2);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, baos);
//            Log.e(TAG,"thumbnailBytes="+ baos.toByteArray().length/1024+"kb");
//            videoParam = new VideoParam(width,height,duration,baos.toByteArray());
//        }
//        retriever.release();
//        return videoParam;
//    }


    /**
     * 获取网络图片
     *
     * @param imageUrl 图片网络地址
     * @return Bitmap 返回位图
     */
    public static Bitmap GetImageInputStream(String imageUrl) {
        URL url;
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        try {
            url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000); //超时设置
            connection.setDoInput(true);
            connection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 保存gif图片文件
     *
     * @param oldPath
     * @throws IOException
     */
    public final static String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
    public final static String SAVE_REAL_PATH = SAVE_PIC_PATH + "/gif_picture";//保存图片的确切位置
    public final static String mFileDir = PathUtils.getExternalAppCachePath() + File.separator + "GroupVideoAndPicture";

    /**
     * 保存gif图片文件
     *
     * @param oldPath
     * @throws IOException
     */
    public static boolean saveGifFile(Context context, String oldPath, String strMime) throws IOException {
        Log.d(TAG, "saveGifFile: SAVE_REAL_PATH=" + SAVE_REAL_PATH);
        File directoryFile = new File(SAVE_REAL_PATH);
        if (!directoryFile.exists()) {
            directoryFile.mkdirs();
        }
        File newFile = new File(directoryFile, String.valueOf(System.currentTimeMillis()) + strMime);
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        String newPath = newFile.getPath();
        int bytesum = 0;
        int byteread = 0;
        File oldfile = new File(oldPath);
        if (oldfile.exists()) { //文件存在时
            InputStream inStream = new FileInputStream(oldPath); //读入原文件
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            int length;
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread; //字节数 文件大小
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
        }
        if (newFile != null) {
            Intent intentBroadcast = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(newPath);
            intentBroadcast.setData(Uri.fromFile(file));
            context.sendBroadcast(intentBroadcast);
            return true;
        }
        return false;
    }

    private String getMimeType(String strUrl) {
        String fileName = strUrl.toLowerCase(Locale.ROOT);
        String strMime = ".jpg";
        if (fileName.endsWith(".png")) {
            strMime = ".png";
        } else if (fileName.endsWith(".jpg")) {
            strMime = ".jpg";
        } else if (fileName.endsWith(".jpeg")) {
            strMime = ".jpeg";
        } else if (fileName.endsWith(".gif")) {
            strMime = ".gif";
        } else if (fileName.endsWith(".mp4")) {
            strMime = ".mp4";
        }
        return strMime;
    }


}
