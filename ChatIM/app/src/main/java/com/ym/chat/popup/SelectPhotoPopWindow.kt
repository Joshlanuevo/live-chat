package com.ym.chat.popup

import android.view.View
import android.view.animation.Animation
import androidx.fragment.app.FragmentActivity
import com.ym.base.widget.adapter.BaseSelectBinderAdapter
import com.ym.base.widget.ext.click
import com.ym.base.widget.ext.pressEffectAlpha
import com.ym.chat.R
import com.ym.chat.databinding.PopwindowSelectPhotoBinding
import com.ym.chat.util.DoubleClick
import razerdp.basepopup.BasePopupWindow
import razerdp.util.animation.AnimationHelper
import razerdp.util.animation.TranslationConfig

/**
 *
 *选择相册  拍照
 *
 */
class SelectPhotoPopWindow(val activity: FragmentActivity) : BasePopupWindow(activity) {

  private val doubleClick by lazy { DoubleClick() }
  lateinit var adapter: BaseSelectBinderAdapter

  //适配器和window公用
  var onItemClickListener: ((data: Any?, position: Int, view: View) -> Unit)? = null


  lateinit var bindView: PopwindowSelectPhotoBinding
  override fun onCreateContentView(): View {
    bindView = PopwindowSelectPhotoBinding.bind(createPopupById(R.layout.popwindow_select_photo))
    bindView.clickCarme.pressEffectAlpha().click { onItemClickListener?.invoke(null, 0, it) }
    bindView.clickPhoto.pressEffectAlpha().click { onItemClickListener?.invoke(null, 1, it) }
    bindView.btnCancel.pressEffectAlpha().click { dismiss() }
    return bindView.root
  }

  override fun onCreateShowAnimation(): Animation? {
    return AnimationHelper.asAnimation()
      .withTranslation(TranslationConfig.FROM_BOTTOM)
      .toShow()
  }

  override fun onCreateDismissAnimation(): Animation? {
    return AnimationHelper.asAnimation()
      .withTranslation(TranslationConfig.TO_BOTTOM)
      .toDismiss()
  }
}