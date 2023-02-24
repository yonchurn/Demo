package com.common.base.toast

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.common.base.R

internal class ToastContentLayout: FrameLayout {

    val textView: TextView by lazy { findViewById(R.id.text) }
    val container: FrameLayout by lazy { findViewById(R.id.container) }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

        isClickable = false
        fitsSystemWindows = true
    }
}