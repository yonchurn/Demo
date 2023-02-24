package com.common.base.utils

import android.content.Context
import android.util.TypedValue
import kotlin.math.ceil


/**
 * 尺寸大小工具类
 */
object SizeUtils {

    /**
     * 将dip转换为px
     * @param dipValue 要转换的dip值
     * @return px值
     */
    fun pxFormDip(dipValue: Float, context: Context): Int {

        return if (dipValue == 0f) 0 else ceil(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dipValue, context.resources.displayMetrics
        )).toInt()
    }

    /**
     * 将px转换为dip
     * @param pxValue 要转换的px值
     * @return dp值
     */
    fun dipFromPx(pxValue: Int, context: Context): Float {
        if (pxValue == 0)
            return 0f
        val scale = context.resources.displayMetrics.density
        return pxValue / scale + 0.5f
    }

    /**
     * sp 转 px
     * @param spValue 要转换的sp值
     * @return sp值
     */
    fun pxFromSp(spValue: Float, context: Context): Int {
        return if (spValue == 0f) 0 else ceil(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, spValue, context.resources.displayMetrics
        )).toInt()
    }

    /**
     * 将px值转换为sp值
     * @param pxValue 要转换的px值
     * @return sp值
     */
    fun spFromPx(pxValue: Int, context: Context): Float {
        if (pxValue == 0)
            return 0f
        val fontScale = context.resources.displayMetrics.scaledDensity
        return pxValue / fontScale + 0.5f
    }


    /**
     * 获取屏幕宽度
     * @return 宽度
     */
    fun getWindowWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度
     * @return 高度
     */
    fun getWindowHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 获取状态栏高度
     * @param context Context
     * @return Int
     */
    fun getStatusBarHeight(context: Context): Int {
        var height = 0
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            height = context.resources.getDimensionPixelSize(resId)
        }

        if (height == 0) {
            height = pxFormDip(20f, context)
        }

        return height
    }
}