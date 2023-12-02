package com.ym.base.widget.ext

import android.R.attr
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.max

/**
 * Author:yangcheng
 * Date:2020/8/21
 * Time:11:00
 */
//不保留末尾为0的数据
fun TextView.setNumberNo00(num: Double) {
    val number = max(0, num.toLong())
    text = if (number > 9999f) {
        val tenThousand = number / 10000 //万
        val thousand = (number % 10000) / 1000 //千
        if (thousand > 0) {
            String.format("%s.%sw", tenThousand, thousand)
        } else {
            String.format("%dw", tenThousand)
        }
    } else {
        val decimalFormat = DecimalFormat("##########.##########")
        decimalFormat.format(num)
    }
}

//创建一个点击变色的文本颜色列表
fun TextView.setTextColorState(normal: Int, pressed: Int, noEnable: Int) {
    setTextColor(
        ColorStateList(
            arrayOf(
                intArrayOf(attr.state_pressed, attr.state_enabled),
                intArrayOf(-attr.state_enabled),
                intArrayOf(),
            ),
            intArrayOf(pressed, noEnable, normal)
        )
    )
}

//创建一个点击变色的文本颜色列表
@SuppressLint("SetTextI18n")
fun TextView.setFirstWordUpperCase() {
    text = text.toString().toCharArray()[0].toUpperCase().toString() + text.toString().substring(1)
}

//不保留末尾为0的数据
fun View.getNumberNo00ZH(num: Double): String {
    val number = max(0, num.toLong())
    return if (number > 9999f) {
        val tenThousand = number / 10000 //万
        val thousand = (number % 10000) / 1000 //千
        if (thousand > 0) {
            String.format("%s.%s万", tenThousand, thousand)
        } else {
            String.format("%d万", tenThousand)
        }
    } else {
        val decimalFormat = DecimalFormat("##########.##########")
        decimalFormat.format(num)
    }
}

//保留2位小数+逗号分隔
@SuppressLint("SetTextI18n")
fun TextView.setNumber2Point(num: String, prefix: String = "", suffix: String = "") {
    val number = if (num.trim().isBlank()) "0.00" else num
    val decimalFormat = DecimalFormat("###,###,###,###,##0.00").also { it.roundingMode = RoundingMode.DOWN }
    text = prefix + try {
        decimalFormat.format(number.toDouble())
    } catch (e: Exception) {
        number
    } + suffix
}
@SuppressLint("SetTextI18n")
fun TextView.setNumber4Point(num: String, prefix: String = "", suffix: String = "K") {
    // 补K
    val number = if (num.trim().isBlank()) "0" else num
    val decimalFormat = DecimalFormat("###,###,###,###,##0.####").also { it.roundingMode = RoundingMode.DOWN }
    text = prefix + try {
        decimalFormat.format(number.toDouble())
    } catch (e: Exception) {
        number
    } + suffix
}
@SuppressLint("SetTextI18n")
fun TextView.setNumber4Point2(num: String, prefix: String = "", suffix: String = "K") {
    // 补K
    val number = if (num.trim().isBlank()) "0" else num
    val decimalFormat = DecimalFormat("#0.####").also { it.roundingMode = RoundingMode.DOWN }
    text = prefix + try {
        decimalFormat.format(number.toDouble())
    } catch (e: Exception) {
        number
    } + suffix
}

//保留2位小数
@SuppressLint("SetTextI18n")
fun TextView.setNumber2Point2(num: String, prefix: String = "", suffix: String = "") {
    val number = if (num.trim().isBlank()) "0.00" else num
    val decimalFormat = DecimalFormat("#0.00").also { it.roundingMode = RoundingMode.DOWN }
    text = prefix + try {
        decimalFormat.format(number.toDouble())
    } catch (e: Exception) {
        number
    } + suffix
}
//保留1位小数
@SuppressLint("SetTextI18n")
fun TextView.setNumber1Point2(num: String, prefix: String = "", suffix: String = "K") {
    val number = if (num.trim().isBlank()) "0.0" else num
    val decimalFormat = DecimalFormat("#0.0").also { it.roundingMode = RoundingMode.DOWN }
    text = prefix + try {
        decimalFormat.format(number.toDouble())
    } catch (e: Exception) {
        number
    } + suffix
}

//保留0位小数
@SuppressLint("SetTextI18n")
fun TextView.setNumber0Point2(num: String, prefix: String = "", suffix: String = "K") {
    val number = if (num.trim().isBlank()) "0" else num
    val decimalFormat = DecimalFormat("#0").also { it.roundingMode = RoundingMode.DOWN }
    text = prefix + try {
        decimalFormat.format(number.toDouble())
    } catch (e: Exception) {
        number
    } + suffix
}

//整数显示
@SuppressLint("SetTextI18n")
fun TextView.setNumberInteger(num: String, prefix: String = "", suffix: String = "K") {
    val number = if (num.trim().isBlank()) "0" else num
    text = prefix + try {
       num.toDouble().toInt()
    } catch (e: Exception) {
        number
    } + suffix
}
//设置2位数显示
fun TextView.setNumberStart0(number: Int) {
    text = if (number < 10) String.format("0%s", number) else number.toString()
}