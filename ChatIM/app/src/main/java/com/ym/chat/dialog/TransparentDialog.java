package com.ym.chat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import com.ym.chat.R;

public class TransparentDialog extends Dialog {
    public TransparentDialog(Context context) {
        super(context, R.style.loading_dialog_fragment);
        init();
    }

    public TransparentDialog(Context context, int i) {
        super(context, R.style.loading_dialog_fragment);
        init();
    }

    protected TransparentDialog(Context context, boolean z, OnCancelListener onCancelListener) {
        super(context, z, onCancelListener);
        init();
    }

    private void init() {
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setDimAmount(0.0f);
    }
}
