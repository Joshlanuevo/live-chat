package com.ym.chat.widget.ateditview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ym.base.util.save.MMKVUtils;
import com.ym.chat.bean.GroupMemberBean;
import com.ym.chat.db.ChatDao;
import com.ym.chat.utils.StringExt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by guoshichao on 2021/6/29
 * AtUserHelper
 */
public class AtUserHelper {

    //多端定好的正则
//        public static final String AT_PATTERN = "@\\(name:([^\\n\\r`~\\!@#\\$%\\^&\\*\\(\\)\\+=\\|'\\:;'\\,\\[\\]\\.\\<\\>/\\?！@#￥%……（）——\\{\\}【】‘；：”“’。，、？]+),id:([A-Za-z0-9]+)\\)";
    //@功能正则
    public static final String AT_PATTERN = "@\\(name:([\\s\\S]*?),id:([A-Za-z0-9]+)\\)";
    //网址匹配正则
    public static final String URL_PATTERN = "(([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://|[wW]{3}.|[wW][aA][pP].|[fF][tT][pP].|[fF][iI][lL][eE]).[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|])|" +
            "([-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|][.]([Cc][Nn]|[Cc][Oo][Mm]|[Nn][Ee][Tt]|[Vv][Nn]|[Vv][Ii][Pp]|[Xx][Yy][Zz]|[Ee][Dd][Uu]|[Xx][Yy]|[Qq][Uu]" +
            "|[Aa][Uu]|[Cc][Uu]|[Oo][Rr][Gg]|[Tt][Oo][Pp]|[Zz][Tt]|[Hh][Tt][Mm][Ll]))";
    //手机号码正则
    public static final String PHONE_PATTERN = "\\d{11}";
    //银行卡号
    public static final String BANK_CODE = "[1-9]\\d{12,18}";
//    public static final String AT_PATTERN = "@\\[\\d{19}\\]";

    //@显示颜色
    private static final String atColor = "#3692F7";

    /**
     * @return 解析AtUser
     */
    public static CharSequence parseAtUserLink(CharSequence text) {
        return parseAtUserLink(text, 0);
    }

    /**
     * @return 解析AtUser
     */
    public static CharSequence parseAtUserLink(CharSequence text, @ColorInt int color) {
        return parseAtUserLink(text, color, null);
    }

    /**
     * @return 解析AtUser
     */
    public static CharSequence parseAtUserLink(CharSequence text, @ColorInt int color, AtUserLinkOnClickListener clickListener) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        // 进行正则匹配[文字](链接)
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        try {
            Matcher matcher = Pattern.compile(AT_PATTERN).matcher(text);
            int replaceOffset = 0; //每次替换之后matcher的偏移量
            while (matcher.find()) {
                // 解析链接  格式是[文字](链接)
                final String name = matcher.group(1);
                final String uid = matcher.group(2);

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(uid)) {
                    continue;
                }

                // 把匹配成功的串append进结果串中, 并设置点击效果
                String atName = "@" + name + "";
                int clickSpanStart = matcher.start() - replaceOffset;
                int clickSpanEnd = clickSpanStart + atName.length();
                spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, atName);
                replaceOffset += matcher.end() - matcher.start() - atName.length();

                if (color != 0) {
                    AtUserForegroundColorSpan atUserLinkSpan = new AtUserForegroundColorSpan(color);
                    atUserLinkSpan.name = name;
                    atUserLinkSpan.uid = uid;
//                atUserLinkSpan.atContent = matcher.group();
                    spannableString.setSpan(atUserLinkSpan, clickSpanStart, clickSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                //是否加超链接：
                if (clickListener != null) {
                    spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View v) {
                            //取消选择
                            Spannable spannable = (Spannable) ((TextView) v).getText();
                            Selection.removeSelection(spannable);

                            // 对id进行解密
                            String atUserId = uid;
                            if (!TextUtils.isEmpty(uid)) {
                                atUserId = EncryptTool.hashIdsDecode(uid);
                            }
                            //外面传进来点击监听：
                            clickListener.atUserClick(atUserId);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(color);//设置文字颜色
                            ds.setUnderlineText(false);      //下划线设置
                            ds.setFakeBoldText(false);      //加粗设置
                        }
                    }, clickSpanStart, clickSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } catch (Exception e) {

        }

        return spannableString;
    }

    /**
     * @return 解析AtUser
     */
    public static CharSequence parseAtUserLinkJxEditText(CharSequence text, int color) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        // 进行正则匹配[文字](链接)
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        try {
            Matcher matcher = Pattern.compile(StringExt.INSTANCE.getAT_PATTERN()).matcher(text);
            int replaceOffset = 0; //每次替换之后matcher的偏移量
            while (matcher.find()) {
                // 解析链接  格式是[文字](链接)
                final String name = matcher.group(0);
                String uid = name.substring(2, name.length() - 1);

                if ("0000000000000000000".equals(uid)) {
                    String atName = "@" + "所有人 ";
                    int clickSpanStart = matcher.start() - replaceOffset;
                    int clickSpanEnd = clickSpanStart + atName.length();
                    spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, atName);
                    replaceOffset += matcher.end() - matcher.start() - atName.length();
                    if (color != 0) {
                        AtUserForegroundColorSpan atUserLinkSpan = new AtUserForegroundColorSpan(color);
                        atUserLinkSpan.name = name;
                        atUserLinkSpan.uid = uid;
//                    atUserLinkSpan.atContent = matcher.group();
                        spannableString.setSpan(atUserLinkSpan, clickSpanStart, clickSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                } else {
                    // 把匹配成功的串append进结果串中, 并设置点击效果
                    GroupMemberBean groupMemberBean = ChatDao.INSTANCE.getGroupDb().getMemberById(uid);
                    if (groupMemberBean != null) {
                        String atName = "@" + groupMemberBean.getName() + " ";
                        int clickSpanStart = matcher.start() - replaceOffset;
                        int clickSpanEnd = clickSpanStart + atName.length();
                        spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, atName);
                        replaceOffset += matcher.end() - matcher.start() - atName.length();
                        if (color != 0) {
                            AtUserForegroundColorSpan atUserLinkSpan = new AtUserForegroundColorSpan(color);
                            atUserLinkSpan.name = name;
                            atUserLinkSpan.uid = uid;
//                        atUserLinkSpan.atContent = matcher.group();
                            spannableString.setSpan(atUserLinkSpan, clickSpanStart, clickSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                    }
                }
            }
        } catch (Exception e) {

        }


        return spannableString;
    }

    /**
     * @return 解析AtUser
     */
    public static CharSequence parseAtUserLinkJx(CharSequence text, int color, AtUserLinkOnClickListener clickListener) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        // 进行正则匹配[文字](链接)
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);
        //解析url
        parseUrl(text, color, spannableString, clickListener);

        try {
            Matcher matcher = Pattern.compile(StringExt.INSTANCE.getAT_PATTERN()).matcher(text);
            int replaceOffset = 0; //每次替换之后matcher的偏移量
            while (matcher.find()) {
                // 解析链接  格式是[文字](链接)
                final String name = matcher.group(0);
                String uid = name.substring(2, name.length() - 1);

                if ("0000000000000000000".equals(uid)) {
                    String atName = "@" + "所有人 ";
                    int clickSpanStart = matcher.start() - replaceOffset;
                    int clickSpanEnd = clickSpanStart + atName.length();
                    spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, atName);
                    replaceOffset += matcher.end() - matcher.start() - atName.length();
                    if (color != 0) {
                        AtUserForegroundColorSpan atUserLinkSpan = new AtUserForegroundColorSpan(color);
                        atUserLinkSpan.name = name;
                        atUserLinkSpan.uid = uid;
//                    atUserLinkSpan.atContent = matcher.group();
                        spannableString.setSpan(atUserLinkSpan, clickSpanStart, clickSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    // 把匹配成功的串append进结果串中, 并设置点击效果
                    GroupMemberBean groupMemberBean = ChatDao.INSTANCE.getGroupDb().getMemberById(uid);
                    if (groupMemberBean != null) {
                        String atName = "@" + groupMemberBean.getName() + " ";
                        int clickSpanStart = matcher.start() - replaceOffset;
                        int clickSpanEnd = clickSpanStart + atName.length();
                        spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, atName);
                        replaceOffset += matcher.end() - matcher.start() - atName.length();

//                        方案2，只有@跟我有关才变成红色
                        //@到的人是我
                        if (color != 0) {
                            AtUserForegroundColorSpan atUserLinkSpan = new AtUserForegroundColorSpan(color);
                            atUserLinkSpan.name = name;
                            atUserLinkSpan.uid = uid;
//                    atUserLinkSpan.atContent = matcher.group();
                            spannableString.setSpan(atUserLinkSpan, clickSpanStart, clickSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                        ClickableSpan click = new ClickableSpan() {

                            @Override
                            public void onClick(@NonNull View view) {
                                clickListener.atUserClick(uid);
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setColor(color);//设置文字颜色
                                ds.setUnderlineText(false);      //下划线设置
                            }
                        };
                        spannableString.setSpan(click, clickSpanStart, clickSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        //方案1，所有@都显示红色
//                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor(atColor));
//                    spannableString.setSpan(colorSpan, clickSpanStart, clickSpanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //解析手机号
//        parsePhone(text, color, spannableString, clickListener);

        //解析银行卡号
//        parseBank(text, color, spannableString, clickListener);

        return spannableString;
    }

    /**
     * 超链接解析
     *
     * @return
     */
    private static SpannableStringBuilder parseUrl(CharSequence text, int color, SpannableStringBuilder spannableString, AtUserLinkOnClickListener clickListener) {
        //超链接转化
        Matcher matcher = Pattern.compile(URL_PATTERN).matcher(text);
        int replaceOffset = 0; //每次替换之后matcher的偏移量
        while (matcher.find()) {
            // 解析链接  格式是[文字](链接)
            final String name = matcher.group(0);
//            Log.e("parseUrl","name="+name+"--------spannableString="+spannableString);
            String uid = name.substring(2, name.length() - 1);
            int clickSpanStart = matcher.start() - replaceOffset;
            int clickSpanEnd = clickSpanStart + name.length();
//            Log.e("parseUrl","clickSpanStart="+clickSpanStart+"-----------clickSpanEnd="+clickSpanEnd);
            spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, name);
            replaceOffset += matcher.end() - matcher.start() - name.length();

            //是否加超链接：
            ClickableSpan click = new ClickableSpan() {

                @Override
                public void onClick(@NonNull View view) {
                    clickListener.ulrLinkClick(name);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(color);//设置文字颜色
                    ds.setUnderlineText(true);      //下划线设置
                    ds.setFakeBoldText(false);      //加粗设置
                }
            };
            spannableString.setSpan(click, clickSpanStart, clickSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    /**
     * 手机号解析
     *
     * @return
     */
    private static SpannableStringBuilder parsePhone(CharSequence text, int color, SpannableStringBuilder spannableString, AtUserLinkOnClickListener clickListener) {
        //正则匹配
        Matcher matcher = Pattern.compile(PHONE_PATTERN).matcher(text);
        int replaceOffset = 0; //每次替换之后matcher的偏移量
        while (matcher.find()) {
            // 解析链接  格式是[文字](链接)
            final String name = matcher.group(0);
            String uid = name.substring(2, name.length() - 1);
            int clickSpanStart = matcher.start() - replaceOffset;
            int clickSpanEnd = clickSpanStart + name.length();
            spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, name);
            replaceOffset += matcher.end() - matcher.start() - name.length();

            //是否加超链接：
            ClickableSpan click = new ClickableSpan() {

                @Override
                public void onClick(@NonNull View view) {
                    //获取剪贴板管理器：
                    ClipboardManager cm = (ClipboardManager) ActivityUtils.getTopActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", name);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    ToastUtils.showShort("已复制到剪切板");
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(color);//设置文字颜色
                    ds.setUnderlineText(true);      //下划线设置
                    ds.setFakeBoldText(false);      //加粗设置
                }
            };
            spannableString.setSpan(click, clickSpanStart, clickSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    /**
     * 银行卡号解析
     *
     * @return
     */
    private static SpannableStringBuilder parseBank(CharSequence text, int color, SpannableStringBuilder spannableString, AtUserLinkOnClickListener clickListener) {
        //正则匹配
        Matcher matcher = Pattern.compile(BANK_CODE).matcher(text);
        int replaceOffset = 0; //每次替换之后matcher的偏移量
        while (matcher.find()) {
            // 解析链接  格式是[文字](链接)
            final String name = matcher.group(0);
            String uid = name.substring(2, name.length() - 1);
            int clickSpanStart = matcher.start() - replaceOffset;
            int clickSpanEnd = clickSpanStart + name.length();
            spannableString.replace(matcher.start() - replaceOffset, matcher.end() - replaceOffset, name);
            replaceOffset += matcher.end() - matcher.start() - name.length();

            //是否加超链接：
            ClickableSpan click = new ClickableSpan() {

                @Override
                public void onClick(@NonNull View view) {
                    //获取剪贴板管理器：
                    ClipboardManager cm = (ClipboardManager) ActivityUtils.getTopActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", name);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    ToastUtils.showShort("已复制到剪切板");
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(color);//设置文字颜色
                    ds.setUnderlineText(true);      //下划线设置
                    ds.setFakeBoldText(false);      //加粗设置
                }
            };
            spannableString.setSpan(click, clickSpanStart, clickSpanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    /**
     * @return 是否输入了At
     */
    public static boolean isInputAt(String beforeStr, String afterStr, int editSelectionEnd) {
        if (!TextUtils.isEmpty(afterStr)) {
            if (TextUtils.isEmpty(beforeStr) || afterStr.length() > beforeStr.length()) {//输入内容的操作
                if (afterStr.length() >= 1 && editSelectionEnd - 1 >= 0 && (afterStr.subSequence(editSelectionEnd - 1, editSelectionEnd)).equals("@")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return 是否删除AtUser整体
     */
    public static boolean isRemoveAt(EditText editText, TextWatcher watcher,
                                     CharSequence beforeStr, CharSequence afterStr, Editable s,
                                     int editSelectionStart, int editSelectionEnd) {
        editText.removeTextChangedListener(watcher);
        boolean isRemove = isRemoveAt(editText, beforeStr, afterStr, s, editSelectionStart, editSelectionEnd);
        editText.addTextChangedListener(watcher);
        return isRemove;
    }

    /**
     * @return 是否删除AtUser整体
     */
    public static boolean isRemoveAt(EditText editText,
                                     CharSequence beforeStr, CharSequence afterStr, Editable s,
                                     int editSelectionStart, int editSelectionEnd) {
        if (TextUtils.isEmpty(afterStr) || TextUtils.isEmpty(beforeStr)
                || !(afterStr instanceof SpannableStringBuilder)
                || !(beforeStr instanceof SpannableStringBuilder)) {
            return false;
        }
        if (afterStr.length() < beforeStr.length()) {//删除内容的操作
            SpannableStringBuilder beforeSp = (SpannableStringBuilder) beforeStr;
            AtUserForegroundColorSpan[] beforeSpans = beforeSp.getSpans(0, beforeSp.length(), AtUserForegroundColorSpan.class);
            boolean mReturn = false;
            for (AtUserForegroundColorSpan span : beforeSpans) {
                int start = beforeSp.getSpanStart(span);
                int end = beforeSp.getSpanEnd(span);

                boolean isRemove = false;
                if (editSelectionStart == editSelectionEnd && editSelectionEnd == end) {
                    //如果刚后在后面，先选中，下次点击才删除
//                    editText.setText(beforeStr);
//                    editText.setSelection(start, end);

                    //方案二是直接删除
                    isRemove = true;
                    s.delete(start, end - 1);
                } else if (editSelectionStart <= start && editSelectionEnd >= end) {
                    return false;
                } else if (editSelectionStart <= start && editSelectionEnd > start) {
                    isRemove = true;
                    s.delete(editSelectionStart, end - editSelectionEnd);
                } else if (editSelectionStart < end && editSelectionEnd >= end) {
                    isRemove = true;
                    s.delete(start, editSelectionStart);
                }

                if (isRemove) {
                    mReturn = true;
                    beforeSp.removeSpan(span);
                }
            }
            return mReturn;
        }
        return false;
    }

    /**
     * 将User添加到At之后
     */
    public static void appendChooseUser(EditText editText, String name, String uid, TextWatcher watcher) {
        appendChooseUser(editText, name, uid, watcher, 0);
    }

    /**
     * 将User添加到At之后 并且变色
     */
    public static void appendChooseUser(EditText editText, String name, String uid, TextWatcher watcher, @ColorInt int color) {
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(uid)) {
            editText.removeTextChangedListener(watcher);
            //@(name:xxxxx,id:XOVo9x)
            String atUserId = EncryptTool.hashIdsEncode(uid);
            //和服务端商量好的拼接规则
            String result = "@(name:" + name + ",id:" + atUserId + ")";
            int beforeTextLength = editText.length();
            int selectionEnd = editText.getSelectionEnd();
            String text = editText.getText().toString();
            int index = text.lastIndexOf("@");
            if (index >= 0) {
                editText.getText().replace(index, selectionEnd, result);
            }
//            editText.getText().replace(selectionEnd - 1, selectionEnd, result);
            editText.setText(parseAtUserLink(editText.getText(), color));
            int afterTextLength = editText.length();
            editText.setSelection(afterTextLength - beforeTextLength + selectionEnd);
            editText.addTextChangedListener(watcher);
        }
    }

    /**
     * 将User添加到At之后 并且变色
     */
    public static void longClickAppendChooseUser(EditText editText, String name, String uid, TextWatcher watcher, @ColorInt int color) {
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(uid)) {
            editText.removeTextChangedListener(watcher);
            //@(name:xxxxx,id:XOVo9x)
            String atUserId = EncryptTool.hashIdsEncode(uid);
            //和服务端商量好的拼接规则
            String result = "@(name:" + name + ",id:" + atUserId + ")";
            int beforeTextLength = editText.length();
            int selectionEnd = editText.getSelectionEnd();
            String text = editText.getText().toString();
            editText.getText().replace(editText.getSelectionStart(), selectionEnd, result);
//            editText.getText().replace(selectionEnd - 1, selectionEnd, result);
            editText.setText(parseAtUserLink(editText.getText(), color));
            int afterTextLength = editText.length();
            editText.setSelection(afterTextLength - beforeTextLength + selectionEnd);
            editText.addTextChangedListener(watcher);
        }
    }

    /**
     * 给EditText添加选择监听，使AtUser成为一个整体
     */
    public static void addSelectionChangeListener(AtUserEditText editText) {
        editText.addOnSelectionChangeListener(new AtUserEditText.OnSelectionChangeListener() {
            @Override
            public void onSelectionChange(int selStart, int selEnd) {
                Editable editable = editText.getText();
                if (editable instanceof SpannableStringBuilder) {
                    SpannableStringBuilder spanStr = (SpannableStringBuilder) editable;
                    AtUserForegroundColorSpan[] beforeSpans = spanStr.getSpans(0, spanStr.length(), AtUserForegroundColorSpan.class);
                    for (AtUserForegroundColorSpan span : beforeSpans) {
                        int start = spanStr.getSpanStart(span);
                        int end = spanStr.getSpanEnd(span);

                        boolean isChange = false;
                        if (selStart > start && selStart < end) {
                            selStart = start;
                            isChange = true;
                        }
                        if (selEnd < end && selEnd > start) {
                            selEnd = end;
                            isChange = true;
                        }

                        if (isChange) {
                            editText.setSelection(selStart, selEnd);
                        }
                    }
                }
            }
        });
    }

    /**
     * AtUser解析
     */
    public static Editable toAtUser(final Editable editable) {
        if (TextUtils.isEmpty(editable)) {
            return null;
        }
        Editable result = editable;
        if (editable instanceof SpannableStringBuilder) {
            SpannableStringBuilder spanStr = (SpannableStringBuilder) editable;
            AtUserForegroundColorSpan[] beforeSpans = spanStr.getSpans(0, spanStr.length(), AtUserForegroundColorSpan.class);
            for (AtUserForegroundColorSpan span : beforeSpans) {
                int start = spanStr.getSpanStart(span);
                int end = spanStr.getSpanEnd(span);
//                span.atContent
                String content = "@[" + span.uid + "]";
                result.replace(start, end, content);
            }
        }
        return result;
    }

    /**
     * AtUser解析
     */
    public static String toAtUserString(Editable editable) {
        if (TextUtils.isEmpty(editable)) {
            return null;
        }
        StringBuilder result = new StringBuilder(editable.toString());
        if (editable instanceof SpannableStringBuilder) {
            SpannableStringBuilder spanStr = (SpannableStringBuilder) editable;
            AtUserForegroundColorSpan[] beforeSpans = spanStr.getSpans(0, spanStr.length(), AtUserForegroundColorSpan.class);
            for (AtUserForegroundColorSpan span : beforeSpans) {
                int start = spanStr.getSpanStart(span);
                int end = spanStr.getSpanEnd(span);
//                span.atContent
                String content = "@[" + span.uid + "]";
                result.replace(start, end, content);
            }
        }
        return result.toString();
    }

    /**
     * 判断String中是否有At
     */
    public static boolean hasAt(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        Matcher matcher = Pattern.compile(AT_PATTERN).matcher(text);
        while (matcher.find()) {
            // 解析链接
            final String name = matcher.group(1);
            final String uid = matcher.group(2);

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(uid)) {
                continue;
            }
            return true;
        }
        return false;
    }


}
