package com.ym.chat.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ym.chat.R
import com.ym.chat.databinding.DialogConverMoreBinding

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/22 10:59
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 会话列表更多弹窗
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class ConverChatMoreDialog : BottomSheetDialogFragment() {
    private lateinit var bindView: DialogConverMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.transparentBottomSheetDialogTheme
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindView = DialogConverMoreBinding.inflate(inflater, container, false)
        return bindView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}