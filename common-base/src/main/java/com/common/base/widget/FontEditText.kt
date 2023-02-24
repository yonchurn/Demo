package com.common.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.common.base.R
import com.common.base.language.LanguageHelper


/**
 *
 */
open class FontEditText: AppCompatEditText {
    var hasBold = false
        set(value){
            field = value
            fontDidChange()
        }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){ init(attrs)}
    @SuppressLint("Recycle", "CustomViewStyleable")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init(attrs)
    }

    @SuppressLint("CustomViewStyleable")
    fun init(attrs: AttributeSet?){
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.FontTextView)
            hasBold = array.getBoolean(R.styleable.FontTextView_bold, false)
            array.recycle()
        }
    }
    private fun fontDidChange() {
        typeface = if (LanguageHelper.isMy){
            if (hasBold) FontType.MY_BOLD_FONT else FontType.MY_REGULAR_FONT
        }else{
            paint.isFakeBoldText = hasBold
            Typeface.DEFAULT
        }
    }
}