package com.ym.chat.utils

import android.annotation.SuppressLint
import com.blankj.utilcode.util.Utils
import com.ym.chat.R
import org.junit.Test
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    //显示小时分钟
    private val hourAndMinute = SimpleDateFormat("HH:mm")
    private val yymmdd = SimpleDateFormat("yyyy-MM-dd")


    /** 年-月-日 时:分:秒 显示格式  */ // 备注:如果使用大写HH标识使用24小时显示格式,如果使用小写hh就表示使用12小时制格式。
    var DATE_TO_STRING_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"

    /** 年-月-日 时:分 显示格式  */ // 备注:收藏界面显示日期格式
    var DATE_TO_STRING_DATE_PATTERN_COLLECT = "yyyy-MM-dd HH:mm"

    /** 月-日 时:分 显示格式  */ // 备注:群发助手界面显示日期格式
    var DATE_TO_STRING_DATE_PATTERN_COLLECT_CHAIN = "MM-dd HH:mm"

    /** 年-月-日 时:分 显示格式  */ // 备注:收藏界面显示时间格式
    var DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME = "HH:mm"

    /** 年-月-日 显示格式  */
    var DATE_TO_STRING_SHORT_PATTERN = "yyyy-MM-dd"

    /** 年-月-日 显示格式  */
    var DATE_TO_STRING_SHORT_PATTERN_CN = "yyyy-MM-dd"

    /** 月-日 显示格式  */
    var STRING_SHORT_PATTERN_CN = "MM-dd"


    private var simpleDateFormat: SimpleDateFormat? = null

    /**
     * Date类型转为指定格式的String类型
     *
     * @param source
     * @param pattern
     * @return
     */
    fun DateToString(source: Date?, pattern: String?): String? {
        simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat!!.format(source)
    }


    /**
     * 获取现在时间
     *
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
     */
    fun getNowDate(): Date? {
        val currentTime = Date()
        val formatter = SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
        val dateString: String = formatter.format(currentTime)
        val pos = ParsePosition(8)
        return formatter.parse(dateString, pos)
    }

    /**
     * 获取现在时间
     *
     * @return返回短时间格式 yyyy-MM-dd
     */
    fun getNowDateShort(): Date? {
        val currentTime = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val dateString: String = formatter.format(currentTime)
        val pos = ParsePosition(8)
        return formatter.parse(dateString, pos)
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    fun getStringDate(): String? {
        val currentTime = Date()
        val formatter = SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
        return formatter.format(currentTime)
    }

    /**
     * 根据时间撮 显示时间
     *
     * @return返回字符串格式 MM月dd日 HH:mm
     */
    fun getStringDateMd(timeStamp: String): String? {
        try {
            return SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_CHAIN).format(Date(timeStamp.toLong()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 根据时间撮 显示时间
     *
     * @return返回字符串格式 HH:mm
     */
    fun getStringDate(timeStamp: String): String? {
        try {
            return SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME).format(Date(timeStamp.toLong()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 根据时间撮 显示时间
     *
     * @return返回字符串格式 yyyy-MM-dd
     */
    fun getStringDateYYYYMMDD(timeStamp: String): String {
        try {
            return SimpleDateFormat(DATE_TO_STRING_SHORT_PATTERN_CN).format(Date(timeStamp.toLong()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 获取现在时间
     *
     * @return 返回短时间字符串格式yyyy-MM-dd
     */
    fun getStringDateShort(): String? {
        val currentTime = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.format(currentTime)
    }

    /**
     * 获取时间 小时:分;秒 HH:mm:ss
     *
     * @return
     */
    fun getTimeShort(): String? {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val currentTime = Date()
        return formatter.format(currentTime)
    }

    /**
     * 获取时间 离当前时间30天的时间戳
     * @return
     */
    fun getTimeTo30Day(): Long {
        val now = Calendar.getInstance()
        now.add(Calendar.DAY_OF_MONTH, -30)
        return now.timeInMillis
    }


    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     * @return
     */
    fun strToDateLong(strDate: String?): Date? {
        val formatter =
            SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
        val pos = ParsePosition(0)
        return formatter.parse(strDate, pos)
    }

    /**
     * 将长时间格式yyyy-MM-dd HH:mm:ss 字符串
     * 转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    fun getCollectDateDayStr(strDate: String?): String {
        return if (strDate?.isNotBlank() == true) {
            val formatter =
                SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
            val pos = ParsePosition(0)
            var date = formatter.parse(strDate, pos)
            if (date != null) {
                var formatterNew =
                    SimpleDateFormat(DATE_TO_STRING_SHORT_PATTERN)
                return formatterNew.format(date)
            } else {
                strDate
            }
        } else {
            ""
        }
    }

    /**
     * 将长时间格式yyyy-MM-dd HH:mm:ss 字符串
     * 转换为时间 yyyy-MM-dd HH:mm
     *
     * @param strDate
     * @return
     */
    fun getCollectDateStr(strDate: String?): String {
        return if (strDate?.isNotBlank() == true) {
            val formatter =
                SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
            val pos = ParsePosition(0)
            var date = formatter.parse(strDate, pos)
            if (date != null) {
                var formatterNew =
                    SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT)
                return formatterNew.format(date)
            } else {
                strDate
            }
        } else {
            ""
        }
    }

    /**
     * 将长时间格式yyyy-MM-dd HH:mm:ss 字符串
     * 转换为 时间戳
     *
     * @param strDate
     * @return
     */
    fun getCollectDateTimestamp(strDate: String?): Long {
        return if (strDate?.isNotBlank() == true) {
            val formatter = SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
            val pos = ParsePosition(0)
            var date = formatter.parse(strDate, pos)
            if (date != null) {
                return date.time
            } else {
                0
            }
        } else {
            0
        }
    }

    /**
     * 将长时间格式yyyy-MM-dd HH:mm:ss 字符串
     * 转换为时间 MM月dd日 HH:mm
     * @param strDate
     * @return
     */
    fun getCollectDateToChinaStr(strDate: String?): String {
        return if (strDate?.isNotBlank() == true) {
            val formatter =
                SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
            val pos = ParsePosition(0)
            var date = formatter.parse(strDate, pos)
            if (date != null) {
                var formatterNew =
                    SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_CHAIN)
                formatterNew.format(date)
            } else {
                strDate
            }
        } else {
            ""
        }
    }

    /**
     * 将长时间格式yyyy-MM-dd HH:mm:ss 字符串
     * 转换为时间 HH:mm
     *
     * @param strDate
     * @return
     */
    fun getCollectTimeStr(strDate: String?): String {
        return if (strDate?.isNotBlank() == true) {
            val formatter =
                SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
            val pos = ParsePosition(0)
            var date = formatter.parse(strDate, pos)
            if (date != null) {
                var formatterNew =
                    SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME)
                return formatterNew.format(date)
            } else {
                strDate
            }
        } else {
            ""
        }
    }

    /**  * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss  *   * @param dateDate  * @return   */
    fun dateToStrLong(dateDate: Date?): String? {
        val formatter =
            SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME)
        return formatter.format(dateDate)
    }

    /**
     * 将短时间格式时间转换为字符串 yyyy-MM-dd
     *
     * @param dateDate
     * @param k
     * @return
     */
    fun dateToStr(dateDate: Date?): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.format(dateDate)
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    fun strToDate(strDate: String?): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val pos = ParsePosition(0)
        return formatter.parse(strDate, pos)
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    fun strToLong(strDate: String?): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val pos = ParsePosition(0)
        try {
            return formatter.parse(strDate, pos).time
        } catch (e: Exception) {
            return 0
            e.printStackTrace()
        }
    }

    /**
     * 得到现在时间
     *
     * @return
     */
    fun getNow(): Date? {
        return Date()
    }

    /**
     * 提取一个月中的最后一天
     *
     * @param day
     * @return
     */
    fun getLastDate(day: Long): Date? {
        val date = Date()
        val date_3_hm = date.time - 3600000 * 34 * day
        return Date(date_3_hm)
    }

    /**
     * 得到现在时间
     *
     * @return 字符串 yyyyMMdd HHmmss
     */
    fun getStringToday(): String? {
        val currentTime = Date()
        val formatter = SimpleDateFormat("yyyyMMdd HHmmss")
        return formatter.format(currentTime)
    }

    /**
     * 得到现在小时
     */
    fun getHour(): String? {
        val currentTime = Date()
        val formatter = SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
        val dateString = formatter.format(currentTime)
        return dateString.substring(11, 13)
    }

    /**
     * 得到现在分钟
     *
     * @return
     */
    fun getTime(): String? {
        val currentTime = Date()
        val formatter = SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
        val dateString = formatter.format(currentTime)
        return dateString.substring(14, 16)
    }

    /**
     * 根据用户传入的时间表示格式，返回当前时间的格式 如果是yyyyMMdd，注意字母y不能大写。
     *
     * @param sformat
     * yyyyMMddhhmmss
     * @return
     */
    fun getUserDate(sformat: String?): String {
        val currentTime = Date()
        val formatter = SimpleDateFormat(sformat)
        return formatter.format(currentTime)
    }

    /**
     * 二个小时时间间的差值,必须保证二个时间都是"HH:MM"的格式，返回字符型的分钟
     */
    fun getTwoHour(st1: String, st2: String): String? {
        var kk: Array<String>? = null
        var jj: Array<String>? = null
        kk = st1.split(":").toTypedArray()
        jj = st2.split(":").toTypedArray()
        return if (kk[0].toInt() < jj[0].toInt()) "0" else {
            val y = kk[0].toDouble() + kk[1].toDouble() / 60
            val u = jj[0].toDouble() + jj[1].toDouble() / 60
            if (y - u > 0) (y - u).toString() + "" else "0"
        }
    }

    /**
     * 得到二个日期间的间隔天数
     */
    fun getTwoDay(sj1: String?, sj2: String?): String? {
        val myFormatter = SimpleDateFormat("yyyy-MM-dd")
        var day: Long = 0
        day = try {
            val date = myFormatter.parse(sj1)
            val mydate = myFormatter.parse(sj2)
            (date.time - mydate.time) / (24 * 60 * 60 * 1000)
        } catch (e: Exception) {
            return ""
        }
        return day.toString() + ""
    }

    /**
     * 时间前推或后推分钟,其中JJ表示分钟.
     */
    fun getPreTime(sj1: String?, jj: String): String? {
        val format = SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN)
        var mydate1: String? = ""
        try {
            val date1 = format.parse(sj1)
            val Time = date1.time / 1000 + jj.toInt() * 60
            date1.time = Time * 1000
            mydate1 = format.format(date1)
        } catch (e: Exception) {
        }
        return mydate1
    }

    /**
     * 得到一个时间延后或前移几天的时间,nowdate为时间,delay为前移或后延的天数
     */
    fun getNextDay(nowdate: String?, delay: String): String? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd")
            var mdate: String? = ""
            val d = strToDate(nowdate)
            val myTime = d.time / 1000 + delay.toInt() * 24 * 60 * 60
            d.time = myTime * 1000
            mdate = format.format(d)
            mdate
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 判断是否润年
     *
     * @param ddate
     * @return
     */
    fun isLeapYear(ddate: String?): Boolean {
        /**
         * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
         * 3.能被4整除同时能被100整除则不是闰年
         */
        val d = strToDate(ddate)
        val gc = Calendar.getInstance() as GregorianCalendar
        gc.time = d
        val year = gc[Calendar.YEAR]
        return if (year % 400 == 0) true else if (year % 4 == 0) {
            if (year % 100 == 0) false else true
        } else false
    }

    /**
     * 返回美国时间格式 26 Apr 2006
     *
     * @param str
     * @return
     */
    fun getEDate(str: String?): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        val pos = ParsePosition(0)
        val strtodate = formatter.parse(str, pos)
        val j = strtodate.toString()
        val k = j.split(" ").toTypedArray()
        return k[2] + k[1].toUpperCase() + k[5].substring(2, 4)
    }

    /**
     * 获取一个月的最后一天
     *
     * @param dat
     * @return
     */
    fun getEndDateOfMonth(dat: String): String? { // yyyy-MM-dd
        var str = dat.substring(0, 8)
        val month = dat.substring(5, 7)
        val mon = month.toInt()
        str += if (mon == 1 || mon == 3 || mon == 5 || mon == 7 || mon == 8 || mon == 10 || mon == 12) {
            "31"
        } else if (mon == 4 || mon == 6 || mon == 9 || mon == 11) {
            "30"
        } else {
            if (isLeapYear(dat)) {
                "29"
            } else {
                "28"
            }
        }
        return str
    }


    /**
     * 产生周序列,即得到当前时间所在的年度是第几周
     *
     * @return
     */
    fun getSeqWeek(): String? {
        val c = Calendar.getInstance(Locale.CHINA)
        var week = c[Calendar.WEEK_OF_YEAR].toString()
        if (week.length == 1) week = "0$week"
        val year = c[Calendar.YEAR].toString()
        return year + week
    }


    /**
     * 两个时间之间的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    fun getDays(date1: String?, date2: String?): Long {
        if (date1 == null || date1 == "") return 0
        if (date2 == null || date2 == "") return 0
        // 转换为标准时间
        val myFormatter = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = null
        var mydate: Date? = null
        try {
            date = myFormatter.parse(date1)
            mydate = myFormatter.parse(date2)
        } catch (e: Exception) {
        }
        return (date!!.time - mydate!!.time) / (24 * 60 * 60 * 1000)
    }

    /**
     * 取得数据库主键 生成格式为yyyymmddhhmmss+k位随机数
     *
     * @param k
     * 表示是取几位随机数，可以自己定
     */
    fun getNo(k: Int): String? {
        return getUserDate("yyyyMMddhhmmss") + getRandom(k)
    }

    /**
     * 返回一个随机数
     *
     * @param i
     * @return
     */
    fun getRandom(i: Int): String {
        val jjj = Random()
        // int suiJiShu = jjj.nextInt(9);
        if (i == 0) return ""
        var jj = ""
        for (k in 0 until i) {
            jj += jjj.nextInt(9)
        }
        return jj
    }

    fun format(timeStamp: Long): String? {
        val curTimeMillis = System.currentTimeMillis()
        val curDate = Date(curTimeMillis)
        val todayHoursSeconds = curDate.hours * 60 * 60
        val todayMinutesSeconds = curDate.minutes * 60
        val todaySeconds = curDate.seconds
        val todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000
        val todayStartMillis = curTimeMillis - todayMillis
        if (timeStamp >= todayStartMillis) {
            return ChatUtils.getString(R.string.今天) + SimpleDateFormat(
                DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME
            ).format(
                Date(
                    timeStamp
                )
            )
        }
        val oneDayMillis = 24 * 60 * 60 * 1000
        val yesterdayStartMilis = todayStartMillis - oneDayMillis
        if (timeStamp >= yesterdayStartMilis) {
            return ChatUtils.getString(R.string.昨天) + SimpleDateFormat(
                DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME
            ).format(
                Date(
                    timeStamp
                )
            )
        }
        val yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis
        return SimpleDateFormat(DATE_TO_STRING_SHORT_PATTERN_CN).format(Date(timeStamp))
    }

    fun formatDay(timeStamp: Long): String? {
        val curTimeMillis = System.currentTimeMillis()
        val curDate = Date(curTimeMillis)
        val todayHoursSeconds = curDate.hours * 60 * 60
        val todayMinutesSeconds = curDate.minutes * 60
        val todaySeconds = curDate.seconds
        val todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000
        val todayStartMillis = curTimeMillis - todayMillis
        if (timeStamp >= todayStartMillis) {
            return SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME).format(
                Date(
                    timeStamp
                )
            )
        }
        val oneDayMillis = 24 * 60 * 60 * 1000
        val yesterdayStartMilis = todayStartMillis - oneDayMillis
        if (timeStamp >= yesterdayStartMilis) {
            return "昨天 " + SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME).format(
                Date(
                    timeStamp
                )
            )
        }
        val yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis
        return if (timeStamp >= yesterdayBeforeStartMilis) {
            "前天 " + SimpleDateFormat(DATE_TO_STRING_DATE_PATTERN_COLLECT_TIME).format(
                Date(
                    timeStamp
                )
            )
        } else SimpleDateFormat(STRING_SHORT_PATTERN_CN).format(Date(timeStamp))
    }

    /**
     * 会话列表显示时间
     *
     * @param timeStamp
     * @return
     */
    fun formatForConver(timeStamp: Long): String? {
        val curTimeMillis = System.currentTimeMillis()
        val curDate = Date(curTimeMillis)
        val todayHoursSeconds = curDate.hours * 60 * 60
        val todayMinutesSeconds = curDate.minutes * 60
        val todaySeconds = curDate.seconds
        val todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000
        val todayStartMillis = curTimeMillis - todayMillis
        return if (timeStamp >= todayStartMillis) {
            TimeUtils.hourAndMinute.format(Date(timeStamp))
        } else {
            TimeUtils.yymmdd.format(Date(timeStamp))
        }
    }

    @SuppressLint("SimpleDateFormat")
    private val currentYear = SimpleDateFormat("MM-dd")

    @SuppressLint("SimpleDateFormat")
    private val other = SimpleDateFormat("yyyy-MM-dd")

    @SuppressLint("SimpleDateFormat")
    private val mmhh = SimpleDateFormat("HH:mm")

    //获取星期
    private val getWeekSf = SimpleDateFormat("EEEE")

    /**
     * 格式化时间
     */
    fun formatTime(time: Long): String {

        return when {
            DateFormatter.isToday(Date(time)) -> {
                val date = Date(time)
                val sf = SimpleDateFormat("HH")
                val hour = sf.format(date)
                val hourInt = Integer.parseInt(hour)
                when (hourInt) {
                    in 5..10 -> {
                        "${ChatUtils.getString(R.string.上午)} ${mmhh.format(time)}"
                    }

                    in 11..12 -> {
                        "${ChatUtils.getString(R.string.中午)} ${mmhh.format(time)}"
                    }

                    in 13..17 -> {
                        "${ChatUtils.getString(R.string.下午)} ${mmhh.format(time)}"
                    }

                    in 18..23 -> {
                        "${ChatUtils.getString(R.string.晚上)} ${mmhh.format(time)}"
                    }

                    in 0..4 -> {
                        "${ChatUtils.getString(R.string.凌晨)} ${mmhh.format(time)}"
                    }

                    else -> {
                        "${mmhh.format(time)}"
                    }
                }
            }

            DateFormatter.isYesterday(Date(time)) -> {
                "${ChatUtils.getString(R.string.昨天)} ${mmhh.format(time)}"
            }

            DateFormatter.isSameWeek(Date(time)) -> {
                //一周内
                "${getWeek(time)} ${mmhh.format(time)}"
            }

            DateFormatter.isCurrentYear(Date(time)) -> {
                //今年
                currentYear.format(Date(time))
            }

            else -> {
                //往年
                other.format(Date(time))
            }
        }
    }

    private fun getWeek(time: Long): String {
        val week = getWeekSf.format(Date(time))
        return week.replace("星期", "周")
    }
}