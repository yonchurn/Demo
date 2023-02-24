@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.common.base.utils

import com.common.base.extension.longValue
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*

/**
 * 价格类型
 */
typealias Price = Long

/**
 * 转成价格
 */
fun String.toPrice(): Price {
    return longValue()
}

/**
 * 价格工具类
 */
object PriceUtils {

    fun formatPrice(price: Price): String {
        return "Ks ${formatInteger(price)}"
    }

    fun formatInteger(integer: Price): String {
        return if (integer >= 1000) {
            try {
                Locale.setDefault(Locale.US)
                val df = DecimalFormat("#,###")
                df.format(integer)
            }catch (e: Exception) {
                integer.toString()
            }
        } else {
            integer.toString()
        }
    }
}