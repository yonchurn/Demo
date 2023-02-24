package com.zyc.queationdemo.nested

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.common.base.extension.removeFromParent


class NestedScrollItem: FrameLayout {


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var oldView: View? = null
    fun setView(view: View) {
        if (oldView != view) {
            oldView?.removeFromParent()
            view.removeFromParent()
            addView(view, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }
}