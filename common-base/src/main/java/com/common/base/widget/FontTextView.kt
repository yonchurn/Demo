package com.common.base.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.common.base.R
import com.common.base.app.BaseApplication
import com.common.base.language.LanguageHelper
import com.common.base.utils.SizeUtils

object FontType {
    val MY_REGULAR_FONT: Typeface = Typeface.createFromAsset(BaseApplication.sharedApplication.assets, "fonts/Oxygen-Regular.ttf")
    val MY_BOLD_FONT: Typeface = Typeface.createFromAsset(BaseApplication.sharedApplication.assets, "fonts/Oxygen-Bold.ttf")
}

/**
 *
 */
open class FontTextView: AppCompatTextView {

    var hasBold = false
    set(value){
            field = value
            fontDidChange()
    }
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    @SuppressLint("Recycle")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

        if (attrs != null) {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.FontTextView)
            hasBold = array.getBoolean(R.styleable.FontTextView_bold, false)
            array.recycle()
        }
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        setLineSpacing(SizeUtils.pxFormDip(5f, context).toFloat(), 1.0f)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        val extra = SizeUtils.pxFormDip(2f, context)
        super.setPadding(left, top + extra, right, bottom + extra)
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