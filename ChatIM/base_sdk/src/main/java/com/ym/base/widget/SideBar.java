package com.ym.base.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//https://www.jianshu.com/p/23f7a962cddd
public class SideBar extends View {
    //SideBar上显示的字母
    public static String[] characters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

    //SideBar的高度
    private int width;
    //SideBar的宽度
    private int height;
    //画字母的画笔
    private TextPaint characterPaint;
    //SideBar上字母绘制的矩形区域
    private Rect textRect;
    //手指触摸在SideBar上的横纵坐标
    private float touchY;
    private int colorNormal = Color.parseColor("#000000");
    private int colorSelect = Color.BLACK;
    private String selectChar = "";
    private float mLineHeight = 0f;

    private OnSelectListener listener;

    public SideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SideBar(Context context) {
        super(context);
        init(context);
    }

    private int defaultSize = 10;//dp
    private int smallSize = 3;//dp

    //初始化操作
    private void init(Context context) {
        defaultSize = dp2px(defaultSize * 1f);
        smallSize = dp2px(smallSize * 1f);
        textRect = new Rect();
        characterPaint = new TextPaint();
        characterPaint.setAntiAlias(true);
        characterPaint.setColor(colorNormal);
        characterPaint.setTextSize(defaultSize);
        if (characters.length > 0) selectChar = characters[0];
    }

    private void setBlackMode() {
        colorSelect = Color.WHITE;
    }

    //设置新的数字
    public void setNewChars(String[] chars) {
        characters = chars;
        if (height > 0) invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) { //在这里测量SideBar的高度和宽度
            width = getMeasuredWidth();
            height = getMeasuredHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {//WRAP_CONTENT
            characterPaint.setTextSize(defaultSize);
            Paint.FontMetrics fontMetrics = characterPaint.getFontMetrics();
            //文字实际绘制高度
            mLineHeight = fontMetrics.bottom - fontMetrics.top;
            heightSize = (int) (mLineHeight * characters.length + 1) + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    private int mColorCircle = Color.parseColor("#C91E22");

    //画出SideBar上的字母
    private void drawCharacters(Canvas canvas) {
        Paint.FontMetrics fontMetrics = characterPaint.getFontMetrics();
        //文字偏移量
        float offSet = characterPaint.baselineShift - fontMetrics.top;
        float offSet1 = characterPaint.descent() - fontMetrics.top;
        float txtHeight = characterPaint.descent() - fontMetrics.ascent;
        for (int i = 0; i < characters.length; i++) {
            String s = characters[i];
            //获取画字母的矩形区域
            characterPaint.getTextBounds(s, 0, s.length(), textRect);
            if (selectChar.equals(s)) {
                characterPaint.setColor(mColorCircle);
                canvas.drawCircle(width / 2f, getPaddingTop() + mLineHeight * i + offSet1 - txtHeight / 2f, txtHeight / 2f, characterPaint);
                characterPaint.setColor(Color.WHITE);
            } else {
                characterPaint.setColor(colorNormal);
            }
            //根据上一步获得的矩形区域，画出字母
            canvas.drawText(s, (width - characterPaint.measureText(s)) / 2f, getPaddingTop() + mLineHeight * i + offSet, characterPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCharacters(canvas);
    }

    //根据手指触摸的坐标，获取当前选择的字母
    private String getHint() {
        int index = (int) ((touchY - getPaddingTop()) / mLineHeight);
        if (index >= 0 && index < characters.length) {
            String ch = characters[index];
            if (!selectChar.equals(ch)) {
                selectChar = characters[index];
                postInvalidate();
            } else {
                return null;
            }
            return ch;
        }
        return null;
    }

    //更新显示位置
    public void updateSelectChar(String charSel) {
        boolean has = false;
        for (String s : characters) {
            if (s.equals(charSel)) {
                has = true;
                break;
            }
        }
        if (has) {
            selectChar = charSel;
            postInvalidate();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                //获取手指触摸的坐标
                touchY = event.getY();
                if (touchY >= 0 && touchY <= height) {
                    String select = getHint();
                    if (select != null && listener != null) listener.onSelect(select);
                } else if (touchY < 0) {
                    String select = characters[0];
                    if (!select.equals(selectChar)) {
                        selectChar = select;
                        postInvalidate();
                        if (listener != null) listener.onSelect(select);
                    }
                } else if (touchY > height) {
                    String select = characters[characters.length - 1];
                    if (!select.equals(selectChar)) {
                        selectChar = select;
                        postInvalidate();
                        if (listener != null) listener.onSelect(select);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touchY = event.getY();
                if (listener != null) listener.onMoveUp();
                return true;
        }
        return super.onTouchEvent(event);
    }

    //监听器，监听手指在SideBar上按下和抬起的动作
    public interface OnSelectListener {
        void onSelect(String s);

        void onMoveUp();
    }

    //设置监听器
    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    private int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int measureHeight(Paint paint) {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        return ~fm.top - (~fm.top - ~fm.ascent) - (fm.bottom - fm.descent);
    }
}