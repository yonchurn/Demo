package com.common.base.extension

import androidx.core.text.isDigitsOnly
import com.common.base.R
import com.common.base.app.MobileHelper
import com.common.base.base.activity.ActivityLifeCycleManager
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 判断是否是缅甸手机号
 */
fun String.isMyanmarMobile(): Boolean {
    val mobile = completionMyanmarMobile()
    val min = ActivityLifeCycleManager.currentContext.resources.getInteger(R.integer.mobile_min)
    val max = ActivityLifeCycleManager.currentContext.resources.getInteger(R.integer.mobile_max)
    if (mobile.length in min..max && mobile.isDigitsOnly()) {
        val beans = MobileHelper.mobileOperatorBeans
        for (bean in beans) {
            for (rule in bean.rules) {
                if (mobile.length == rule.length && mobile.startsWith(rule.prefix)) {
                    return true
                }
            }
        }
    }
    return false
}

/**
 * 获取完整的缅甸手机号
 */
fun String.completionMyanmarMobile(): String {
    if (!startsWith("0") && startsWith("9") && isDigitsOnly()) {
        return "0$this"
    }
    return this
}

/**
 * 编码
 */
fun String.encodedString(): String {
    return URLEncoder.encode(this, "UTF-8")
}

fun String.intValue(): Int {
    return try {
        toInt()
    }catch (e: Exception) {
        0
    }
}

fun String.longValue(): Long {
    return try {
        toLong()
    }catch (e: Exception) {
        0
    }
}

fun String.boolValue(): Boolean {
    return try {
        toBoolean()
    }catch (e: Exception) {
        false
    }
}

fun String.floatValue(): Float {
    return try {
        toFloat()
    }catch (e: Exception) {
        0f
    }
}

fun String.doubleValue(): Double {
    return try {
        toDouble()
    }catch (e: Exception) {
        0.0
    }
}

/**
 * 获取MD5
 */
fun String.md5Value(): String {
    try {
        val instance = MessageDigest.getInstance("MD5") //获取md5加密对象
        val digest:ByteArray = instance.digest(this.toByteArray()) //对字符串加密，返回字节数组
        val sb = StringBuilder()
        for (b in digest) {
            val i = b.toInt() and 0xff //获取低八位有效值
            var hexString = Integer.toHexString(i) //将整数转化为16进制
            if (hexString.length < 2) {
                hexString = "0$hexString" //如果是一位的话，补0
            }
            sb.append(hexString)
        }
        return sb.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return ""
}