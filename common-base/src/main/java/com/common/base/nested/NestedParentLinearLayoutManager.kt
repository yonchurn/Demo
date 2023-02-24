package com.common.base.nested

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * 嵌套滑动父视图布局管理
 */
open class NestedParentLinearLayoutManager: LinearLayoutManager {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    var nestedScrollHelper: NestedScrollHelper? = null

    override fun canScrollVertically(): Boolean {
        if (nestedScrollHelper != null) {
            return nestedScrollHelper!!.layoutCanScrollVertically()
        }
        return super.canScrollVertically()
    }
}