package com.common.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.common.base.extension.removeFromParent

///RecyclerView ListView 置顶容器，为了修复部分机型 在removeView addView 会闪退的问题
class StickContainer: ViewGroup {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var stickItem: View? = null
        set(value) {
            if (value != field) {
                field?.removeFromParent()
                field = value
                if (field != null) {
                    addView(field)
                }
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val item = stickItem
        if (item != null && isVisible) {
            val params = item.layoutParams as MarginLayoutParams
            val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, params.leftMargin + params.rightMargin, params.height)
            measureChild(item, widthMeasureSpec, childHeightMeasureSpec)
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), item.measuredHeight)
        } else {
            setMeasuredDimension(0, 0)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val item = stickItem
        if (item != null) {
            val params = item.layoutParams as MarginLayoutParams
            item.layout(params.leftMargin, 0, params.leftMargin + item.measuredWidth, item.measuredHeight)
        }
    }
}