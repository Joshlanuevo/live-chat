package com.ym.base.ext

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import java.math.RoundingMode
import java.text.DecimalFormat

//<editor-fold defaultstate="collapsed" desc="Int">
//保留2位小数
fun Int.twoPoint(): String {
    return this.toDouble().twoPoint()
}

/*将xml定义的颜色变为 计算后的颜色值*/
fun Int.xmlToColor(): Int {
    return if (this == 0) this else ColorUtils.getColor(this)
}

/*将xml定义的字符串引用，获取真实String值*/
fun Int.xmlToString(): String {
    return StringUtils.getString(this)
}

/*将xml定义的多个字符串引用格式化，获取真实String值*/
fun Int.xmlFormat(vararg args: Int): String {
    val toTypedArray = args.map { it1 -> it1.xmlToString() }.toTypedArray()
    return StringUtils.getString(this).format(*toTypedArray)
}

//XML吐司
fun Int.xmlToast() {
    StringUtils.getString(this).toast()
}

//fun Int.xmlSnack() {
//    StringUtils.getString(this).snack()
//}
//
//fun Int.xmlSnackView(view : View?) {
//    StringUtils.getString(this).snackView(view)
//}

//将数字转为多少万显示
fun Int.toWanStr(): String {
    if (this < 1000) {
        return this.toString()
    } else {
        val toString = (this / 1000F).toString()
        val index = toString.indexOf(".")
        if (index != -1) {
            val split: List<String> = toString.split(".")
            return when {
                split[1].isBlank() -> {
                    split[0]
                }
                split[1].length > 1 -> {
                    toString.substring(index + 1)
                }
                else -> {
                    toString
                }
            } + "w"
        } else {
            return toString
        }
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Float">
//保留2位小数
fun Float.twoPoint(): String {
    return this.toDouble().twoPoint()
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Double">
//保留2位小数
fun Double.twoPoint(): String {
    val decimalFormat = DecimalFormat("#0.00")
    return try {
        decimalFormat.format(this)
    } catch (e: Exception) {
        this.toString()
    }
}
//</editor-fold>


fun Number.dp2Px(): Int {
    return SizeUtils.dp2px(this.toFloat())
}

fun Number.sp2Px(): Int {
    return SizeUtils.sp2px(this.toFloat())
}

//代替android sdk自带的 toDrawable,使用 kotlin core中的减少错误
fun Int.xmlToDrawable(mContext: Context): Drawable? {
    return if (this <= 0) null else ContextCompat.getDrawable(mContext, this)
}

//<editor-fold defaultstate="collapsed" desc="保留小数">
//保留2位小数
fun Number?.to2point(): String {
    return if (this == null) "0.00" else DecimalFormat("#0.00").also { it.roundingMode = RoundingMode.DOWN }.format(this.toDouble())
}

//保留4位小数
fun Number?.to4point(): String {
    return if (this == null) "0.00" else DecimalFormat("#0.0000").also { it.roundingMode = RoundingMode.DOWN }.format(this.toDouble())
}
//保留2位小数+逗号分隔
fun Number?.to2point2(): String {
    return if (this == null) "0.00" else DecimalFormat("###,###,###,###,##0.00").also { it.roundingMode = RoundingMode.DOWN }.format(this.toDouble())
}
//逗号分隔
fun String?.toSegmentation(): String {
    return if (this == null) "0" else try {
        DecimalFormat("###,###,###,###,###").also { it.roundingMode = RoundingMode.DOWN }.format(this.toInt())
    } catch (e: Exception) {
        this
    }
}
//保留2位小数
fun String?.to2point(): String {
    return if (this.isNullOrBlank()) "0.00" else try {
        DecimalFormat("#0.00").also { it.roundingMode = RoundingMode.DOWN }.format(this.toDouble())
    } catch (e: Exception) {
        this
    }
}

//保留2位小数+逗号分隔
fun String?.to2point2(): String {
    return if (this.isNullOrBlank()) "0.00" else try {
        DecimalFormat("###,###,###,###,##0.00").also { it.roundingMode = RoundingMode.DOWN }.format(this.toDouble())
    } catch (e: Exception) {
        this
    }
}

//保留4位小数+逗号分隔
fun String?.to4point(suffix: String = "K"): String {
    //除以1000 补K
    return if (this.isNullOrBlank()) "0" else try {
        DecimalFormat("###,###,###,###,##0.####").also { it.roundingMode = RoundingMode.DOWN }.format(this.toDouble()/1000) + suffix
    } catch (e: Exception) {
        this
    }
}

//小于10显示0开头
fun Number?.toStart0(): String {
    return when {
        this == null -> "00"
        this.toDouble() < 10.0 -> String.format("0%s", this)
        else -> this.toString()
    }
}

//删除默尾0
fun Number?.delEnd0(): String {
    return if (this == null) "0" else DecimalFormat("##########.##########").format(this.toDouble())
}
//</editor-fold>