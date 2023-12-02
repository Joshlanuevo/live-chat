package com.ym.base.util;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * KOK的正则判断
 */
public class EditTextUtils {
    private static final String ALL_LETTER = "^[A-Za-z]+$";
    private static final String ALL_NUMBER = "^[0-9]*$";
    private static final String REGEX_ILLEGAL_INPUT = "^[a-zA-Z0-9]+$";
    private static final String REGEX_PHONE = "^1[0-9]{10}$";
    private static final String REGEX_USERNAME_INPUT = "^[A-Za-z](?=(.*[a-zA-Z])+)(?=(.*[0-9])+)[0-9A-Za-z]{3,10}$";
    private static final String TIP_1 = "密码长度为8-12位，字母+数字的组合";
    private static final String TIP_2 = "密码必须包含大写字母和小写字母";

    public static boolean checkInputEmpty(EditText[] editTextArr, boolean z) {
        for (EditText editText : editTextArr) {
            if (z) {
                if (isEmpty(editText)) {
                    return false;
                }
            } else if (isEmpty(editText)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static void combine1EditAndButton(EditText editText, View view) {
        boolean z = !isEmpty(editText);
        if (view != null && z != view.isClickable()) {
            view.setEnabled(z);
        }
    }

    @Deprecated
    public static void combine3EditAndButton(EditText editText, EditText editText2, EditText editText3, View view, View view2) {
        boolean z = view.getVisibility() != View.VISIBLE ? !(isEmpty(editText) || isEmpty(editText2)) : !(isEmpty(editText) || isEmpty(editText2) || isEmpty(editText3));
        if (z != view2.isClickable()) {
            view2.setEnabled(z);
        }
    }

    public static String getEdtString(EditText editText) {
        return editText.getText().toString().trim();
    }

//    public static String getPwdTipBy(String str) {
//        int length = str.length();
//        boolean z = length >= 8 && length <= 15;
//        String string2 = StringUtils.getString(R.string.密码提示);
//        if (!z) {
//            return string2;
//        }
//        if (!Pattern.matches("^[a-zA-Z0-9]+$", str.toLowerCase())) {
//            return string2;
//        }
//        String string22 = "[0-9]+";
//        if (str.matches(string22)) {
//            return string2;
//        }
//        return (!Pattern.compile("[a-z]+").matcher(str.toLowerCase()).find() || !Pattern.compile(string22).matcher(str).find()) ? string2 : "";
//    }

    public static boolean isEmpty(EditText editText) {
        return TextUtils.isEmpty(editText.getText().toString());
    }

    public static boolean isNameMatcher2(String str) {
        return Pattern.matches("^[A-Za-z][A-Za-z][0-9A-Za-z]{3,8}$", str.toLowerCase()) && !Pattern.matches("^[0-9]*$", str.toLowerCase()) && !Pattern.matches("^[A-Za-z][A-Za-z]+$", str.toLowerCase());
    }

    public static boolean isNameMatcher(String str) {
        return Pattern.matches("^[A-Za-z]{2}[0-9A-Za-z]{2,7}$", str.toLowerCase());
    }

    public static boolean isPhoneNumber(String str) {
        return Pattern.matches("^1[0-9]{10}$", str);
    }

    public static boolean isPwdMatcher(String str) {
        int length = str.length();
        if (length > 15 || length < 8) {
            return false;
        }
        return Pattern.compile("[a-z]+").matcher(str.toLowerCase()).find() && Pattern.compile("[0-9]+").matcher(str).find() && Pattern.matches("^[a-zA-Z0-9]+$", str.toLowerCase());
    }

    public static boolean isPwdMatcher2(String str) {
        int length = str.length();
        if (length > 15 || length < 8) {
            return false;
        }
        return Pattern.matches("^[0-9A-Za-z]{1,15}$", str.toLowerCase());
    }

    public static boolean isRegexMatcher(String str) {
        return Pattern.matches("^[a-zA-Z0-9]+$", str.toLowerCase());
    }

    public static boolean isUserNameOld(String str) {
        if (!Pattern.matches("^[a-zA-Z0-9]+$", str.toLowerCase())) {
            if (!Pattern.matches("^[A-Za-z]+$", str.toLowerCase())) {
                return Pattern.matches("^[0-9]*$", str.toLowerCase());
            }
        }
        return false;
    }
}
