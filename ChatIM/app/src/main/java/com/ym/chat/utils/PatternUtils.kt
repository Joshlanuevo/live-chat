package com.ym.chat.utils

import android.text.InputFilter
import java.util.regex.Pattern

/**
 * @Description
 * @Author：CASE
 * @Date：2021-07-14
 * @Time：21:48
 */
object PatternUtils {
    //4-9个字符，前2位必须为字母，数字可选，不支持符号
    fun isAccountMatcher(str: String): Boolean {
        return Pattern.matches("^[A-Za-z]{2}[0-9A-Za-z]{2,7}$", str)
    }

    //判断图片链接
    fun isImageUrlMatcher(str: String): Boolean {
        return Pattern.matches(".*?(gif|png|jpeg|jpg|bmp)", str)
    }

    //判断图片链接排除gif
    fun isImageUrlGifMatcher(str: String): Boolean {
        return Pattern.matches(".*?(gif)", str)
    }

    //8-16位，支持任意中英文或数字，不支持符号
    fun isNameMatcher(str: String): Boolean {
//         Pattern.compile("[\\u4E00-\\u9FA5A-Za-z]+").matcher(str.lowercase()).find() &&
//                Pattern.compile("[0-9]+").matcher(str).find() &&
        return Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]{8,16}$", str)
    }

    //3-25位，支持任意中英文或数字，不支持符号
    fun isGroupNameMatcher(str: String): Boolean {
        //Pattern.compile("[\\u4E00-\\u9FA5A-Za-z]+").matcher(str.lowercase()).find() &&
        //Pattern.compile("[0-9]+").matcher(str).find() &&
        return Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]{3,25}$", str)
    }

    //8-16位，仅支持字母和数字组合，账号和密码不能一致，不支持符号
    fun isPwdMatcher(str: String): Boolean {
        return Pattern.compile("[a-z]+").matcher(str.lowercase()).find() &&
                Pattern.compile("[0-9]+").matcher(str).find() &&
                Pattern.matches("^[0-9A-Za-z]{8,16}$", str)
    }

    //1-15位，仅支持字母和数字组合，账号和密码不能一致，不支持符号
    fun isUsername(str: String): Boolean {
        return Pattern.matches("^[0-9A-Za-z]{1,15}$", str)
    }

    //手机号判断
    fun isPhoneNumber(str: String): Boolean {
        return Pattern.matches("^1[0-9]{10}$", str)
    }

    //友聊号判断
    fun isJinXin(str: String): Boolean {
        return Pattern.matches("^J{1}$", str)
    }

    //是否是英文字符
    fun isEnglish(str: String): Boolean {
        return Pattern.matches("^[A-Za-z]+$", str)
    }

    //验证码格式验证
    fun isCodeMatcher(str: String): Boolean {
        return Pattern.matches("^[0-9A-Za-z]{6}$", str)
    }

    val isNumber = InputFilter { source, _, _, _, _, _ ->
        val p = Pattern.compile("^[0-9]+$")
        val m = p.matcher(source.toString())
        if (!m.matches()) "" else null
    }

    val isNumberAndLetter = InputFilter { source, _, _, _, _, _ ->
        //数字和字母
        val p = Pattern.compile("^[A-Za-z0-9]+$")
        val m = p.matcher(source.toString())
        if (!m.matches()) "" else null
    }

    val isNumBerAndLetterSupper = InputFilter { source, _, _, _, _, _ ->
//        英文数字下划线格式
        val p = Pattern.compile("^[A-Za-z0-9_]+$")
        val m = p.matcher(source.toString())
        if (!m.matches()) "" else null
    }

    val zhNumberLetter = InputFilter { source, _, _, _, _, _ ->
//        中文英文数字格式
        val p = Pattern.compile("^[\\u4E00-\\u9FA5A-Za-z0-9]+$")
        val m = p.matcher(source.toString())
        if (!m.matches()) "" else null
    }

    val disEnter = InputFilter { source, _, _, _, _, _ ->
        //禁止换行
        source.toString().replace("\n", "")
    }
}