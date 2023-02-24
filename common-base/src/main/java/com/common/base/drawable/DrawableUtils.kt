package com.common.base.drawable

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

///
object DrawableUtils{

    /**
     * 获取着色的 drawable
     * @param drawable 要着色的
     * @param tintColor 对应颜色
     * @return 着色后的drawable
     */
    fun getTintDrawable(drawable: Drawable, @ColorInt tintColor: Int) : Drawable{
        val wrapDrawable = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrapDrawable, tintColor)
        return wrapDrawable
    }

    fun getTintListDrawable(drawable: Drawable, color: ColorStateList) : Drawable{
        val wrapDrawable = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTintList(wrapDrawable, color)
        return wrapDrawable
    }

    //设置圆角边框
    fun setDrawable(targetView: View, cornerRadius: Int, @ColorInt backgroundColor: Int
    ): CornerBorderDrawable {
        return setDrawable(targetView, cornerRadius, backgroundColor, 0, 0)
    }

    fun setDrawable(targetView: View, cornerRadius: Int, @ColorInt backgroundColor: Int,
                    borderWidth: Int, @ColorInt borderColor: Int): CornerBorderDrawable {
        val drawable = CornerBorderDrawable()
        drawable.setCornerRadius(cornerRadius)
        drawable.backgroundColor = backgroundColor
        drawable.borderWidth = borderWidth
        drawable.borderColor = borderColor
        drawable.attachView(targetView)
        return drawable
    }

    fun setCircleDrawable(targetView: View, @ColorInt backgroundColor: Int): CornerBorderDrawable {
        val drawable = CornerBorderDrawable()
        drawable.shouldAbsoluteCircle = true
        drawable.backgroundColor = backgroundColor
        drawable.attachView(targetView)

        return drawable
    }

    fun setCircleDrawable(targetView: View, @ColorInt backgroundColor: Int,
                          borderWidth: Int, @ColorInt borderColor: Int): CornerBorderDrawable {
        val drawable = CornerBorderDrawable()
        drawable.shouldAbsoluteCircle = true
        drawable.backgroundColor = backgroundColor
        drawable.borderWidth = borderWidth
        drawable.borderColor = borderColor
        drawable.attachView(targetView)

        return drawable
    }
}