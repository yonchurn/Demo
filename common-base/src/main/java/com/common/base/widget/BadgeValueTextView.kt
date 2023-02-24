package com.common.base.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.common.base.R
import com.common.base.drawable.CornerBorderDrawable
import com.common.base.extension.intValue
import kotlin.math.max


/**
 * 角标
 */
class BadgeValueTextView : FontTextView {

    //背景
    private var drawable = CornerBorderDrawable()

    //背景颜色
    @ColorInt
    var fillColor = Color.RED
    set(value) {
        if(value != field){
            field = value
            drawable.backgroundColor = field
        }
    }

    //边框颜色
    @ColorInt
    var strokeColor = Color.TRANSPARENT
    set(value) {
        if(value != field){
            field = value
            drawable.borderColor = field
        }
    }

    //边框
    var strokeWidth = 0
    set(value) {
        if(value != field){
            field = value
            drawable.borderWidth = field
        }
    }

    //达到最大值的图标
    var maxValueIconRes = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        if (attrs != null) {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.BadgeValueTextView)

            fillColor = array.getColor(R.styleable.BadgeValueTextView_badge_fill_color, Color.RED)
            strokeColor = array.getColor(R.styleable.BadgeValueTextView_badge_stroke_color, Color.TRANSPARENT)
            strokeWidth = array.getDimensionPixelOffset(R.styleable.BadgeValueTextView_badge_stroke_width, 0)
            maxValueIconRes = array.getResourceId(R.styleable.BadgeValueTextView_badge_max_icon, 0)

            array.recycle()
        }
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        if (text == null) {
            text = ""
        }
    }

    init {
        drawable.backgroundColor = fillColor
        drawable.borderColor = strokeColor
        drawable.borderWidth = strokeWidth
        drawable.shouldAbsoluteCircle = true
        drawable.attachView(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        val height = measuredHeight
        if (width < height) {
            val size = max(width, height)
            val widthSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
            val heightSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
            super.onMeasure(widthSpec, heightSpec)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (text == null) {
            text = ""
        }
        hideIfNeeded()
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        var value = text
        var drawable: Drawable? = null
        if (text != null && text.toString().intValue() > 99) {
            if (maxValueIconRes > 0) {
                drawable = ContextCompat.getDrawable(context, maxValueIconRes)
            } else {
                value = "99+"
            }
        }

        if (drawable != null) {
            value = ""
            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            setCompoundDrawables(drawable, null, null, null)
            background = null
        }else{
            if (background == null){
                this.drawable?.attachView(this)
            }
        }


        super.setText(value ?: "", type)
        hideIfNeeded()
    }

    //判断是否需要隐藏
    private fun hideIfNeeded() {

        visibility = when {
            compoundDrawables[0] != null -> VISIBLE
            TextUtils.isDigitsOnly(text) -> {
                val value = text?.toString()?.intValue() ?: 0
                if (value > 0) View.VISIBLE else View.GONE
            }
            else -> if (TextUtils.isEmpty(text)) View.GONE else View.VISIBLE
        }
    }
}