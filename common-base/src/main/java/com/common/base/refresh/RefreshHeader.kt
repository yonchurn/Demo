package com.common.base.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.SpinnerStyle


/**
 * 下拉刷新头部
 */
abstract class RefreshHeader: FrameLayout, RefreshHeader {

    //回调
    var onScrollHandler: RefreshOnScrollHandler? = null

    //是否需要立刻关闭刷新
    var shouldCloseImmediately = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @CallSuper
    override fun onMoving(isDragging: Boolean, percent: Float, offset: Int, height: Int, maxDragHeight: Int) {
        if (onScrollHandler != null) {
            onScrollHandler!!.onScroll(isDragging, percent, offset)
        }
    }

    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {}

    override fun onReleased(
        refreshLayout: RefreshLayout,
        height: Int,
        maxDragHeight: Int
    ) {
    }

    override fun onStartAnimator(
        refreshLayout: RefreshLayout,
        height: Int,
        maxDragHeight: Int
    ) {
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {}

    override fun isSupportHorizontalDrag(): Boolean {
        return false
    }

    //下拉刷新滑动监听
    interface RefreshOnScrollHandler {

        fun onScroll(isDragging: Boolean, percent: Float, offset: Int)
    }
}