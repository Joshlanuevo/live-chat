package com.ym.chat.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ym.chat.R;

public class ToastUtils {

    private static Toast toast;

    /**
     * 显示有image的toast 这是个view
     */
    public static void showToastWithImg(Context context, String tvStr, int imageResource) {
        if (toast == null) {
            toast = new Toast(context.getApplicationContext());
        }
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.toast_comm, null);
//        view.setLayoutParams(new LinearLayout.LayoutParams((int) (DeviceInfoUtils.getDeviceWidth(context) * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView tv = (TextView) view.findViewById(R.id.tvContent);
        tv.setText(TextUtils.isEmpty(tvStr) ? "" : tvStr);
        ImageView iv = (ImageView) view.findViewById(R.id.ivClose);
        if (imageResource != 0) {
            iv.setVisibility(View.VISIBLE);
            iv.setImageResource(imageResource);
        } else {
            iv.setVisibility(View.GONE);
        }
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();

    }
}
