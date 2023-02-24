package com.common.base.extension

import android.os.Parcel

/**
 * Parcelable 扩展
 */

fun Parcel.getString(): String {
    return readString() ?: ""
}