package com.common.base.extension

/**
 * 列表扩展
 */

/**
 * 安全获取，防止越界
 */
fun <T> List<T>.getSafely(index: Int): T? {
    return if (index < size) this[index] else null
}

fun <T> List<T>.lastSafely(): T? {
    return if (isNotEmpty()) last() else null
}

fun <T> List<T>.firstSafely(): T? {
    return if (isNotEmpty()) first() else null
}