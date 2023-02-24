package com.common.base.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * 日期工具类
 */
@Suppress("ConstantLocale")
object DateUtils {

    //yyyy-MM-dd HH:mm:ss
    const val YMD_HMS = "yyyy-MM-dd HH:mm:ss"

    //yyyy-MM-dd HH:mm
    const val YMD_HM = "yyyy-MM-dd HH:mm"

    //yyyy-MM-dd
    const val YMD = "yyyy-MM-dd"

    //年月日时分秒 {@link #formatMs}
    const val YEAR = 0
    const val MONTH = 1
    const val DAY = 2
    const val HOUR = 3
    const val MINUTES = 4
    const val SECONDS = 5


    //使用单例， 提升效率
    val YMdHmsDateFormat: SimpleDateFormat = SimpleDateFormat(YMD_HMS, Locale.US)
    val YMdHmDateFormat: SimpleDateFormat = SimpleDateFormat(YMD_HM, Locale.US)
    val YMdDateFormat: SimpleDateFormat = SimpleDateFormat(YMD, Locale.US)


    /**
     * 把yyyy-MM-dd HH:mm:ss时间转成给定格式
     * @param time 要转的时间字符串
     * @param targetFormat 目标格式
     * @return 格式化的时间
     */
    fun formatTime(time: String?, targetFormat: String): String? {
        if(StringUtils.isEmpty(time))
            return ""
        return try {
            formatDate(YMdHmsDateFormat.parse(time!!), targetFormat)
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 时间转成给定格式
     * @param time 要转的时间字符串
     * @param timeFormat 当前时间格式
     * @param targetFormat 目标格式
     * @return 格式化的时间
     */
    fun formatTime(time: String?, timeFormat: String, targetFormat: String): String? {
        return formatDate(parseTime(time, timeFormat), targetFormat)
    }

    /**
     * 把时间戳转成给定格式
     * @param timestamp 要转的时间戳
     * @param targetFormat 目标格式
     * @return 格式化的时间
     */
    fun formatTime(timestamp: Long, targetFormat: String): String {
        return formatDate(Date(getMicroTimestamp(timestamp)), targetFormat)
    }

    /**
     * 把时间转成给定格式
     * @param date 要转的时间
     * @param targetFormat 目标格式
     * @return 格式化的时间
     */
    fun formatDate(date: Date?, targetFormat: String): String {
        if(date == null)
            return ""
        return when(targetFormat){
            YMD -> {
                YMdDateFormat.format(date)
            }
            YMD_HM -> {
                YMdHmDateFormat.format(date)
            }
            YMD_HMS -> {
                YMdHmsDateFormat.format(date)
            }
            else -> {
                val dateFormat = SimpleDateFormat(targetFormat, Locale.US)
                dateFormat.format(date)
            }
        } ?: ""
    }

    /**
     * 获取时间戳
     * @param time 时间字符串
     * @param format 时间格式
     * @return 时间戳
     */
    fun timestampFromTime(time: String?, format: String): Long {
        val date = parseTime(time, format)
        return date?.time ?: 0
    }

    /**
     * 获取当前时间
     * @param format 时间格式
     */
    fun getCurrentTime(format: String): String {
        return formatDate(Date(), format)
    }

    /**
     * 解析时间
     * @param time 时间
     * @param format 格式
     * @return date
     */
    fun parseTime(time: String?, format: String): Date? {
        if(time == null)
            return null
        return try {
            when(format){
                YMD -> {
                    YMdDateFormat.parse(time)
                }
                YMD_HM -> {
                    YMdHmDateFormat.parse(time)
                }
                YMD_HMS -> {
                    YMdHmsDateFormat.parse(time)
                }
                else -> {
                    val dateFormat = SimpleDateFormat(format, Locale.US)
                    dateFormat.parse(time)
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 格式化毫秒
     * @param ms 毫秒
     * @param type 返回的类型 [.Year]
     * @return
     */
    fun formatMs(ms: Long, type: Int): String {
        val seconds = ms / 1000
        if (type == SECONDS) {
            return seconds.toString()
        }
        val minutes = seconds / 60
        if (type == MINUTES) {
            return minutes.toString()
        }
        val hour = minutes / 60
        if (type == HOUR) {
            return hour.toString()
        }
        val day = hour / 24
        if (type == DAY) {
            return day.toString()
        }
        val month = day / 30
        if (type == MONTH) {
            return month.toString()
        }
        val year = month / 12
        return if (type == YEAR) {
            year.toString()
        } else ms.toString()
    }

    /**
     * 格式化秒
     * @param seconds 要格式化的秒
     * @return 00:00:00
     */
    fun formatSeconds(seconds: Int): String? {
        val result = seconds / 60
        val second = seconds % 60
        val minute = result % 60
        val hour = result / 60
        return java.lang.String.format(Locale.US, "%02d:%02d:%02d", hour, minute, second)
    }

    /**
     * 获取符合java的时间戳
     * @param timestamp 时间戳
     * @return 13位时间戳
     */
    fun getMicroTimestamp(timestamp: Long): Long {
        return if (timestamp.toString().length == 13) {
            timestamp
        } else timestamp * 1000
    }
}