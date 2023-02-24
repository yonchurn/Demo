package com.common.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.common.base.R
import com.common.base.base.interf.ValueCallback

/**
 * hint和text 字体不一样
 */
class CustomTextView: FontTextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(attrs)
    }

    private var hintTextSize = 0f
    private var hintBold = false
    private var originTextSize = 0f
    private var originBold = false

    //清空按钮图标
    var clearDrawable: Drawable? = null
        set(value) {
            if(value != field){
                field = value
                if(field != null){
                    field!!.setBounds(0, 0, field!!.intrinsicWidth, field!!.intrinsicHeight)
                }
            }
        }

    //清空回调
    var clearCallback: ValueCallback<String>? = null

    @SuppressLint("CustomViewStyleable")
    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.customText)
            hintTextSize = array.getDimensionPixelSize(R.styleable.customText_hintTextSize, textSize.toInt()).toFloat()
            hintBold = array.getBoolean(R.styleable.customText_hintBold, hintBold)
            val res = array.getResourceId(R.styleable.customText_clearIcon, 0)
            if (res > 0) {
                clearDrawable = ContextCompat.getDrawable(context, res)
            }
            array.recycle()
        }
    }

    var isEmpty: Boolean? = null
    override fun onFinishInflate() {
        super.onFinishInflate()

        originTextSize = textSize
        originBold = typeface.isBold
        textDidChange()

        addTextChangedListener {
            textDidChange()
        }
    }

    private fun textDidChange() {
        val empty = length() == 0
        if (isEmpty != empty) {
            isEmpty = empty
            setTextSize(TypedValue.COMPLEX_UNIT_PX, if (empty) hintTextSize else originTextSize)
            typeface = Typeface.defaultFromStyle(if (empty) Typeface.NORMAL else Typeface.BOLD)
            if (clearDrawable != null) {
                if (!empty)
                    setCompoundDrawablesWithIntrinsicBounds(null, null, clearDrawable, null)
                else
                    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }
        }
    }

    // 处理删除事件
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (clearDrawable != null && length() > 0) {
            when (event.action) {
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x.toInt()
                    val rect = clearDrawable!!.bounds
                    val isInnerWidth = x > width - paddingRight * 2 - rect.width()

                    if (isInnerWidth) {
                        if (event.action == MotionEvent.ACTION_UP) {
                            val old = text.toString()
                            text = ""
                            clearCallback?.also {
                                it(old)
                            }
                        }
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }
}