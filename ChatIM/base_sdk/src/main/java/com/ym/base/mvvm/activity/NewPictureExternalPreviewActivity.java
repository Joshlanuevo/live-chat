package com.ym.base.mvvm.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.Utils;
import com.luck.picture.lib.PictureBaseActivity;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.dialog.PictureCustomDialog;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnImageCompleteCallback;
import com.luck.picture.lib.permissions.PermissionChecker;
import com.luck.picture.lib.photoview.PhotoView;
import com.luck.picture.lib.thread.PictureThreadUtils;
import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.JumpUtils;
import com.luck.picture.lib.tools.MediaUtils;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.luck.picture.lib.tools.ToastUtils;
import com.luck.picture.lib.tools.ValueOf;
import com.luck.picture.lib.widget.PreviewViewPager;
import com.luck.picture.lib.widget.longimage.ImageSource;
import com.luck.picture.lib.widget.longimage.ImageViewState;
import com.luck.picture.lib.widget.longimage.SubsamplingScaleImageView;
import com.ym.base.widget.ext.ViewExtKt;
import com.ym.base_sdk.R;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okio.BufferedSource;
import okio.Okio;

/**
 * @author：luck
 * @data：2017/01/18 下午1:00
 * @描述: 预览图片
 */
public class NewPictureExternalPreviewActivity extends PictureBaseActivity {

  private ImageView ivClose;
  private TextView tvTitleCurrent;
  private TextView tvTitleTotal;
  private PreviewViewPager viewPager;
  private List<LocalMedia> images = new ArrayList<>();
  private int position = 0;
  private SimpleFragmentAdapter adapter;
  private String downloadPath;
  private String mMimeType;

  @Override
  public int getResourceId() {
    return R.layout.activity_pic_preview;
  }

  @Override
  public boolean isRequestedOrientation() {
    return false;
  }

  @Override
  protected void initWidgets() {
    super.initWidgets();
    ivClose = findViewById(R.id.previewClose);
    tvTitleCurrent = findViewById(R.id.previewCurrent);
    tvTitleTotal = findViewById(R.id.previewTotal);
    viewPager = findViewById(R.id.previewPager);
    position = getIntent().getIntExtra(PictureConfig.EXTRA_POSITION, 0);
    images = (List<LocalMedia>) getIntent().getSerializableExtra(PictureConfig.EXTRA_PREVIEW_SELECT_LIST);
    viewPager.setOffscreenPageLimit(8);
    ViewExtKt.pressEffectAlpha(ivClose, 0.7f);
    ViewExtKt.click(ivClose,300, mView -> {
      NewPictureExternalPreviewActivity.this.finish();
      NewPictureExternalPreviewActivity.this.exitAnimation();
      return null;
    });
    initViewPageAdapterData();
  }

  /**
   * 设置样式
   */
  @Override
  public void initPictureSelectorStyle() {
  }

  @SuppressLint("SetTextI18n")
  private void initViewPageAdapterData() {
    if (images == null) images = new ArrayList<>();
    tvTitleCurrent.setText((position + 1) + "");
    tvTitleTotal.setText("/" + (images.size()));
    adapter = new SimpleFragmentAdapter();
    viewPager.setAdapter(adapter);
    viewPager.setCurrentItem(position);
    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int index) {
        tvTitleCurrent.setText((index + 1) + "");
        position = index;
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });
  }

  public class SimpleFragmentAdapter extends PagerAdapter {

    /**
     * 最大缓存图片数量
     */
    private static final int MAX_CACHE_SIZE = 20;
    /**
     * 缓存view
     */
    private SparseArray<View> mCacheView;

    private void clear() {
      if (null != mCacheView) {
        mCacheView.clear();
        mCacheView = null;
      }
    }

    public void removeCacheView(int position) {
      if (mCacheView != null && position < mCacheView.size()) {
        mCacheView.removeAt(position);
      }
    }

    public SimpleFragmentAdapter() {
      super();
      this.mCacheView = new SparseArray<>();
    }

    @Override
    public int getCount() {
      return images != null ? images.size() : 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      (container).removeView((View) object);
      if (mCacheView.size() > MAX_CACHE_SIZE) {
        mCacheView.remove(position);
      }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
      return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      View contentView = mCacheView.get(position);
      if (contentView == null) {
        contentView = LayoutInflater.from(container.getContext())
            .inflate(R.layout.preview_image_down, container, false);
        mCacheView.put(position, contentView);
      }
      // 常规图控件
      final PhotoView imageView = contentView.findViewById(R.id.preview_image);
      // 长图控件
      final SubsamplingScaleImageView longImageView = contentView.findViewById(R.id.longImg);
      // 视频播放按钮
      ImageView ivPlay = contentView.findViewById(R.id.iv_play);
      ImageView saveBtn = contentView.findViewById(R.id.previewDown);
      if (BarUtils.isNavBarVisible(NewPictureExternalPreviewActivity.this)) {
        saveBtn.setTranslationY(-BarUtils.getNavBarHeight());
      } else {
        saveBtn.setTranslationY(0f);
      }
      LocalMedia media = images.get(position);
      if (media != null) {
        final String path;
        if (media.isCut() && !media.isCompressed()) {
          // 裁剪过
          path = media.getCutPath();
        } else if (media.isCompressed() || (media.isCut() && media.isCompressed())) {
          // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
          path = media.getCompressPath();
        } else if (!TextUtils.isEmpty(media.getAndroidQToPath())) {
          // AndroidQ特有path
          path = media.getAndroidQToPath();
        } else {
          // 原图
          path = media.getPath();
        }
        boolean isHttp = PictureMimeType.isHasHttp(path);
        String mimeType = isHttp ? PictureMimeType.getImageMimeType(media.getPath()) : media.getMimeType();
        boolean isHasVideo = PictureMimeType.isHasVideo(mimeType);
        ivPlay.setVisibility(isHasVideo ? View.VISIBLE : View.GONE);
        boolean isGif = PictureMimeType.isGif(mimeType);
        boolean eqLongImg = MediaUtils.isLongImg(media);
        imageView.setVisibility(eqLongImg && !isGif ? View.GONE : View.VISIBLE);
        longImageView.setVisibility(eqLongImg && !isGif ? View.VISIBLE : View.GONE);
        // 压缩过的gif就不是gif了
        if (isGif && !media.isCompressed()) {
          if (config != null && PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine.loadAsGifImage
                (getContext(), path, imageView);
          }
        } else {
          if (config != null && PictureSelectionConfig.imageEngine != null) {
            if (isHttp) {
              // 网络图片
              PictureSelectionConfig.imageEngine.loadImage(contentView.getContext(), path,
                  imageView, longImageView, new OnImageCompleteCallback() {
                    @Override
                    public void onShowLoading() {
                      showPleaseDialog();
                    }

                    @Override
                    public void onHideLoading() {
                      dismissDialog();
                    }
                  });
            } else {
              if (eqLongImg) {
                displayLongPic(PictureMimeType.isContent(path)
                    ? Uri.parse(path) : Uri.fromFile(new File(path)), longImageView);
              } else {
                PictureSelectionConfig.imageEngine.loadImage(contentView.getContext(), path, imageView);
              }
            }
          }
        }
        imageView.setOnViewTapListener((view, x, y) -> {
          finish();
          exitAnimation();
        });
        longImageView.setOnClickListener(v -> {
          finish();
          exitAnimation();
        });
        if (!isHasVideo) {
          longImageView.setOnLongClickListener(v -> {
            if (config.isNotPreviewDownload) {
              if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                downloadPath = path;
                String currentMimeType =
                    PictureMimeType.isHasHttp(path) ? PictureMimeType.getImageMimeType(media.getPath()) : media.getMimeType();
                mMimeType = PictureMimeType.isJPG(currentMimeType) ? PictureMimeType.MIME_TYPE_JPEG : currentMimeType;
                showDownLoadDialog(true);
              } else {
                PermissionChecker.requestPermissions(NewPictureExternalPreviewActivity.this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
              }
            }
            return true;
          });
        }
        if (!isHasVideo) {
          imageView.setOnLongClickListener(v -> {
            if (config.isNotPreviewDownload) {
              if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                downloadPath = path;
                String currentMimeType =
                    PictureMimeType.isHasHttp(path) ? PictureMimeType.getImageMimeType(media.getPath()) : media.getMimeType();
                mMimeType = PictureMimeType.isJPG(currentMimeType) ? PictureMimeType.MIME_TYPE_JPEG : currentMimeType;
                showDownLoadDialog(true);
              } else {
                PermissionChecker.requestPermissions(NewPictureExternalPreviewActivity.this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
              }
            }
            return true;
          });
        }

        ViewExtKt.visibleGone(saveBtn, !isHasVideo);
        ViewExtKt.pressEffectAlpha(saveBtn, 0.7f);
        ViewExtKt.click(saveBtn,300, mView -> {
          if (config.isNotPreviewDownload) {
            if (PermissionChecker.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
              downloadPath = path;
              String currentMimeType =
                  PictureMimeType.isHasHttp(path) ? PictureMimeType.getImageMimeType(media.getPath()) : media.getMimeType();
              mMimeType = PictureMimeType.isJPG(currentMimeType) ? PictureMimeType.MIME_TYPE_JPEG : currentMimeType;
              showDownLoadDialog(true);
            } else {
              PermissionChecker.requestPermissions(NewPictureExternalPreviewActivity.this,
                  new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE);
            }
          }
          return null;
        });
        ivPlay.setOnClickListener(v -> {
          if (PictureSelectionConfig.customVideoPlayCallback != null) {
            PictureSelectionConfig.customVideoPlayCallback.startPlayVideo(media);
          } else {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(PictureConfig.EXTRA_VIDEO_PATH, path);
            intent.putExtras(bundle);
            JumpUtils.startPictureVideoPlayActivity(container.getContext(), bundle, PictureConfig.PREVIEW_VIDEO_CODE);
          }
        });
      }
      (container).addView(contentView, 0);
      return contentView;
    }
  }

  /**
   * 加载长图
   */
  private void displayLongPic(Uri uri, SubsamplingScaleImageView longImg) {
    longImg.setQuickScaleEnabled(true);
    longImg.setZoomEnabled(true);
    longImg.setPanEnabled(true);
    longImg.setDoubleTapZoomDuration(100);
    longImg.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
    longImg.setDoubleTapZoomDpi(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
    longImg.setImage(ImageSource.uri(uri), new ImageViewState(0, new PointF(0, 0), 0));
  }

  /**
   * 下载图片提示
   */
  private void showDownLoadDialog(boolean showDialog) {
    if (!isFinishing() && !TextUtils.isEmpty(downloadPath)) {
      if (showDialog) {
        final PictureCustomDialog dialog =
            new PictureCustomDialog(getContext(), R.layout.picture_wind_base_dialog);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_commit = dialog.findViewById(R.id.btn_commit);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tv_content = dialog.findViewById(R.id.tv_content);
        tvTitle.setText(getString(R.string.picture_prompt));
        tv_content.setText(getString(R.string.picture_prompt_content));
        btn_cancel.setOnClickListener(v -> {
          if (!isFinishing()) {
            dialog.dismiss();
          }
        });
        btn_commit.setOnClickListener(view -> {
          clickSave();
          if (!isFinishing()) {
            dialog.dismiss();
          }
        });
        dialog.show();
      }
    } else {
      clickSave();
    }
  }

  private void clickSave() {
    boolean isHttp = PictureMimeType.isHasHttp(downloadPath);
    showPleaseDialog();
    if (isHttp) {
      PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<String>() {
        @Override
        public String doInBackground() {
          return showLoadingImage(downloadPath);
        }

        @Override
        public void onSuccess(String result) {
          onSuccessful(result);
        }
      });
    } else {
      // 有可能本地图片
      try {
        if (PictureMimeType.isContent(downloadPath)) {
          savePictureAlbumAndroidQ(
              PictureMimeType.isContent(downloadPath) ? Uri.parse(downloadPath) : Uri.fromFile(new File(downloadPath)));
        } else {
          // 把文件插入到系统图库
          savePictureAlbum();
        }
      } catch (Exception e) {
        ToastUtils.s(getContext(), getString(R.string.picture_save_error) + "\n" + e.getMessage());
        dismissDialog();
        e.printStackTrace();
      }
    }
  }

  /**
   * 保存相片至本地相册
   *
   * @throws Exception
   */
  private void savePictureAlbum() throws Exception {
    String suffix = PictureMimeType.getLastImgSuffix(mMimeType);
    String state = Environment.getExternalStorageState();
    File rootDir = state.equals(Environment.MEDIA_MOUNTED)
        ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        : getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if (rootDir != null && !rootDir.exists() && rootDir.mkdirs()) {
    }
    File folderDir = new File(SdkVersionUtils.checkedAndroid_Q() || !state.equals(Environment.MEDIA_MOUNTED)
        ? rootDir.getAbsolutePath() : rootDir.getAbsolutePath() + File.separator + PictureMimeType.CAMERA + File.separator);
    if (folderDir != null && !folderDir.exists() && folderDir.mkdirs()) {
    }
    String fileName = DateUtils.getCreateFileName("IMG_") + suffix;
    File file = new File(folderDir, fileName);
    PictureFileUtils.copyFile(downloadPath, file.getAbsolutePath());
    onSuccessful(file.getAbsolutePath());
  }

  /**
   * 图片保存成功
   */
  private void onSuccessful(String result) {
    dismissDialog();
    if (!TextUtils.isEmpty(result)) {
      try {
        if (!SdkVersionUtils.checkedAndroid_Q()) {
          File file = new File(result);
          //MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), null);
          //new PictureMediaScannerConnection(getContext(), file.getAbsolutePath(), () -> {
          //});
          //保存图片后发送广播通知更新数据库
          Uri mUri = Uri.fromFile(file);
          Utils.getApp().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, mUri));
        }
        ToastUtils.s(getContext(), getString(R.string.picture_save_success) + "\n" + result);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      ToastUtils.s(getContext(), getString(R.string.picture_save_error));
    }
  }

  /**
   * 保存图片到picture 目录，Android Q适配，最简单的做法就是保存到公共目录，不用SAF存储
   */
  private void savePictureAlbumAndroidQ(Uri inputUri) {
    ContentValues contentValues = new ContentValues();
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, DateUtils.getCreateFileName("IMG_"));
    contentValues.put(MediaStore.Images.Media.DATE_TAKEN, ValueOf.toString(System.currentTimeMillis()));
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, mMimeType);
    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, PictureMimeType.DCIM);
    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    if (uri == null) {
      ToastUtils.s(getContext(), getString(R.string.picture_save_error));
      return;
    }
    PictureThreadUtils.executeByIo(new PictureThreadUtils.SimpleTask<String>() {

      @Override
      public String doInBackground() {
        BufferedSource buffer = null;
        try {
          buffer = Okio.buffer(Okio.source(Objects.requireNonNull(getContentResolver().openInputStream(inputUri))));
          OutputStream outputStream = getContentResolver().openOutputStream(uri);
          boolean bufferCopy = PictureFileUtils.bufferCopy(buffer, outputStream);
          if (bufferCopy) {
            return PictureFileUtils.getPath(getContext(), uri);
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          if (buffer != null && buffer.isOpen()) {
            PictureFileUtils.close(buffer);
          }
        }
        return "";
      }

      @Override
      public void onSuccess(String result) {
        PictureThreadUtils.cancel(PictureThreadUtils.getIoPool());
        onSuccessful(result);
      }
    });
  }

  /**
   * 针对Q版本创建uri
   */
  private Uri createOutImageUri() {
    ContentValues contentValues = new ContentValues();
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, DateUtils.getCreateFileName("IMG_"));
    contentValues.put(MediaStore.Images.Media.DATE_TAKEN, ValueOf.toString(System.currentTimeMillis()));
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, mMimeType);
    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, PictureMimeType.DCIM);

    return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
  }

  // 下载图片保存至手机
  public String showLoadingImage(String urlPath) {
    Uri outImageUri = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;
    BufferedSource inBuffer = null;
    try {
      if (SdkVersionUtils.checkedAndroid_Q()) {
        outImageUri = createOutImageUri();
      } else {

        String suffix = PictureMimeType.getLastImgSuffix(mMimeType);
        String state = Environment.getExternalStorageState();
        File rootDir =
            state.equals(Environment.MEDIA_MOUNTED)
                ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                : getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (rootDir != null) {
          if (!rootDir.exists()) {
            rootDir.mkdirs();
          }
          File folderDir = new File(!state.equals(Environment.MEDIA_MOUNTED)
              ? rootDir.getAbsolutePath() : rootDir.getAbsolutePath() + File.separator + PictureMimeType.CAMERA + File.separator);
          if (!folderDir.exists() && folderDir.mkdirs()) {
          }
          String fileName = DateUtils.getCreateFileName("IMG_") + suffix;
          File file = new File(folderDir, fileName);
          outImageUri = Uri.fromFile(file);
        }
      }
      if (outImageUri != null) {
        outputStream = Objects.requireNonNull(getContentResolver().openOutputStream(outImageUri));
        URL u = new URL(urlPath);
        inputStream = u.openStream();
        inBuffer = Okio.buffer(Okio.source(inputStream));
        boolean bufferCopy = PictureFileUtils.bufferCopy(inBuffer, outputStream);
        if (bufferCopy) {
          return PictureFileUtils.getPath(this, outImageUri);
        }
      }
    } catch (Exception e) {
      if (outImageUri != null && SdkVersionUtils.checkedAndroid_Q()) {
        getContentResolver().delete(outImageUri, null, null);
      }
    } finally {
      PictureFileUtils.close(inputStream);
      PictureFileUtils.close(outputStream);
      PictureFileUtils.close(inBuffer);
    }
    return null;
  }

  @Override
  public void onBackPressed() {
    //大于Android Q会有内存泄漏 https://blog.csdn.net/oLengYueZa/article/details/109207492
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      finishAfterTransition();
    } else {
      super.onBackPressed();
    }
    finish();
    exitAnimation();
  }

  private void exitAnimation() {
    overridePendingTransition(R.anim.picture_anim_fade_in, config.windowAnimationStyle != null
        && config.windowAnimationStyle.activityPreviewExitAnimation != 0
        ? config.windowAnimationStyle.activityPreviewExitAnimation : R.anim.picture_anim_exit);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (adapter != null) {
      adapter.clear();
    }
    PictureSelectionConfig.destroy();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case PictureConfig.APPLY_STORAGE_PERMISSIONS_CODE:
        // 存储权限
        for (int i = 0; i < grantResults.length; i++) {
          if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
            showDownLoadDialog(true);
          } else {
            ToastUtils.s(getContext(), getString(R.string.picture_jurisdiction));
          }
        }
        break;
    }
  }
}
