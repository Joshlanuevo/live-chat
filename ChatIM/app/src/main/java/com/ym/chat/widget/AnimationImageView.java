package com.ym.chat.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ym.chat.R;

public class AnimationImageView extends FrameLayout {
    private View hour;
    private View min;
    private AnimatorSet animatorSet;

    public AnimationImageView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AnimationImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnimationImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_clock, this, true);
        hour = findViewById(R.id.ivH);
        min = findViewById(R.id.ivM);
    }

    /**
     * 开启动画
     */
    public void startAnimotion() {
        if (animatorSet == null) {
            AnimatorSet animatorSet = new AnimatorSet();
            //构造ObjectAnimator对象的方法
            ObjectAnimator animator = ObjectAnimator.ofFloat(min, "rotation", 0.0F, 360.0F);//设置先顺时针360度旋转然后逆时针360度旋转动画
            animator.setDuration(500);//设置旋转时间
            animator.setRepeatCount(-1);
            animator.setRepeatMode(ValueAnimator.RESTART);

            ObjectAnimator animator2 = ObjectAnimator.ofFloat(hour, "rotation", 0.0F, 90F);//设置先顺时针360度旋转然后逆时针360度旋转动画
            animator2.setDuration(1000);//设置旋转时间
//        animator2.setRepeatCount(-1);
//        animator2.setRepeatMode(ValueAnimator.RESTART);
//        animator2.start();//开始执行动画（顺时针旋转动画）

            ObjectAnimator animator3 = ObjectAnimator.ofFloat(hour, "rotation", 90f, 450f);//设置先顺时针360度旋转然后逆时针360度旋转动画
            animator3.setDuration(1000);//设置旋转时间
            animator3.setRepeatCount(-1);
            animator3.setRepeatMode(ValueAnimator.RESTART);
//        animator3.start();//开始执行动画（顺时针旋转动画）

            animatorSet.play(animator2).after(200);
            animatorSet.playTogether(animator3, animator);
            animatorSet.start();
        } else {
            animatorSet.start();
        }
    }

    /***
     * 停止动画
     */
    public void stopAnimation() {
        animatorSet.cancel();
    }
}
