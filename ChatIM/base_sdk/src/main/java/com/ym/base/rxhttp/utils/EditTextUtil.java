package com.ym.base.rxhttp.utils;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.widget.EditText;

/**
 * https://www.jianshu.com/p/a641846f60ab
 */
public class EditTextUtil {
    /*** 小数点后的位数 */
    private static final int POINTER_LENGTH = 2;
    private static final int ONE_POINTER_LENGTH = 1;

    private static final String POINTER = ".";

    private static final String ZERO = "0";
    private static final String SUFFIX = "K";
    private static String number;
    private static int curSelection;

    /***
     * 保留两位小数
     * @param editText
     * @param length 整数数字长度
     */
    @SuppressLint("SetTextI18n")
    public static void keepTwoDecimals(EditText editText, int length) {
        number = editText.getText().toString();
        //第一位不能输入小点
        if (number.length() == 1 && TextUtils.equals(number.substring(0, 1), POINTER)) {
            editText.setText("");
            return;
        }

        //第一位0时，后续不能输入其他数字
        if (number.length() > 1 && TextUtils.equals(number.substring(0, 1), ZERO) &&
                !TextUtils.equals(number.substring(1, 2), POINTER)) {
            editText.setText(number.substring(0, 1));
            editText.setSelection(editText.length());
            return;
        }

        String[] numbers = number.split("\\.");
        //已经输入小数点的情况下
        if (numbers.length == 2) {
            //小数处理
            int decimalsLength = numbers[1].length();
            if (decimalsLength > POINTER_LENGTH) {
                curSelection = editText.getSelectionEnd();
                editText.setText(number.substring(0, numbers[0].length() + 1 + POINTER_LENGTH));
                editText.setSelection(curSelection > number.length() ?
                        number.length() :
                        curSelection);
            }
            //整数处理
            if (numbers[0].length() > length) {
                curSelection = editText.getSelectionEnd();
                editText.setText(number.substring(0, length) + number.substring(length + 1));
                editText.setSelection(curSelection > length ?
                        length :
                        curSelection);
            }
        } else {
            //整数处理
            if (editText.length() > length) {
                if (number.contains(POINTER)) return;
                curSelection = editText.getSelectionEnd();
                editText.setText(number.substring(0, length));
                editText.setSelection(curSelection > length ?
                        length :
                        curSelection);
            }
        }
    }

    /***
     * 保留1位小数
     * @param editText
     * @param length 整数数字长度
     */
    @SuppressLint("SetTextI18n")
    public static void keepOneDecimals(EditText editText, int length) {
        number = editText.getText().toString();
        if (!TextUtils.isEmpty(number) && number.contains(SUFFIX)) {
            number =  number.replace(SUFFIX,"");
            if (TextUtils.isEmpty(number))return;
        }

            //第一位不能输入小点 和  0
            if (number.length() == 1 && (TextUtils.equals(number.substring(0, 1), POINTER) || TextUtils.equals(number.substring(0, 1), ZERO))) {
                editText.setText("");
                return;
            }


            String[] numbers = number.split("\\.");
            //已经输入小数点的情况下
            if (numbers.length == 2) {
                //小数处理
                int decimalsLength = numbers[1].length();
                if (decimalsLength > ONE_POINTER_LENGTH) {
                    curSelection = editText.getSelectionEnd();
                    editText.setText(number.substring(0, numbers[0].length() + 1 + ONE_POINTER_LENGTH) + SUFFIX);
                    editText.setSelection(
                            curSelection - 1);
                    return;
                }
                //整数处理
                if (numbers[0].length() > length) {
                    curSelection = editText.getSelectionEnd();
                    editText.setText(number.substring(0, length) + number.substring(length + 1) + SUFFIX);
                    editText.setSelection(curSelection > length  ?
                            length - 1 :
                            curSelection - 1);
                    return;
                }
            } else {
                //整数处理
                if (editText.length() > length) {
                    if (number.contains(POINTER)) return;
                    curSelection = editText.getSelectionEnd();
                    editText.setText(number.substring(0, length) + SUFFIX);
                    editText.setSelection(curSelection > length ?
                            length - 1 :
                            curSelection - 1);
                    return;
                }
            }

//        if (!TextUtils.isEmpty(number) && !number.contains(SUFFIX)){
//            editText.setText(number + SUFFIX);
//            editText.setSelection(number.length());
//        }
    }
}