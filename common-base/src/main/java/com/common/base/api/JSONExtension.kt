package com.common.base.api

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.common.base.extension.intValue
import com.common.base.utils.Price
import java.math.BigDecimal
import java.util.*


/**
 * 安全的json解析，防止数据类型不同或者null
 */

fun JSONObject.stringValue(key: String): String {
    return getString(key) ?: ""
}

fun JSONObject.booleValue(key: String): Boolean {
    return try {
        return when(val value = get(key)) {
            is Boolean -> value
            is BigDecimal -> value.intValueExact() != 0
            is Number -> value.toInt() != 0
            is String -> {
                val str = value.toLowerCase(Locale.US)
                str == "true" || str == "y" || str == "yes" || value.intValue() != 0
            }
            else -> false
        }
    }catch (e: Exception) {
        false
    }
}

fun JSONObject.doubleValue(key: String): Double {
    return try {
        getDouble(key) ?: 0.0
    }catch (e: Exception) {
        0.0
    }
}

fun JSONObject.floatValue(key: String): Float {
    return try {
        getFloat(key) ?: 0f
    }catch (e: Exception) {
        0f
    }
}

fun JSONObject.intValue(key: String): Int {
    return try {
        getInteger(key) ?: 0
    }catch (e: Exception) {
        0
    }
}

fun JSONObject.longValue(key: String): Long {
    return try {
        getLong(key) ?: 0
    }catch (e: Exception) {
        0
    }
}

fun JSONObject.priceValue(key: String): Price {
    return try {
        getLong(key) ?: 0
    }catch (e: Exception) {
        0
    }
}

fun JSONArray.forEachObject(action: (obj: JSONObject) -> Unit) {
    for (obj in this) {
        if (obj is JSONObject) {
            action(obj)
        }
    }
}

fun JSONArray.getJSONObjectSafely(index: Int): JSONObject? {
    if (index < size) {
        return getJSONObject(index)
    }
    return null
}
