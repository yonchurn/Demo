package com.common.base.language

/**
 * 转成zawgyi 如果可能的话
 */
fun String.zawgyi(): String {
    return LanguageHelper.convertToZawgyiIfNeeded(this)!!
}

/**
 * 转成mm3 如果可能的话
 */
fun String.mm3(): String {
    return LanguageHelper.covertToMm3IfNeeded(this)!!
}