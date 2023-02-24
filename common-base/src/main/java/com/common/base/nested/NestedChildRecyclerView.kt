package com.common.base.nested

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.common.base.widget.StickRecyclerView
import kotlin.math.abs

/**
 * 可嵌套滑动的子列表
 */
class NestedChildRecyclerView: StickRecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var nestedScrollHelper: NestedScrollHelper? = null

    val isScrollTop: Boolean
        get() = !canScrollVertically(-1)

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    parentFlingIfEnabled()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (flingStarting) {
                    totalDyConsumed += dy
                }
            }
        })
    }

    //滑动
    private var totalDyConsumed = 0
    private var lastY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val helper = nestedScrollHelper
        helper ?: return super.onTouchEvent(e)

        if (e.action == MotionEvent.ACTION_MOVE) {
            val deltaY = (lastY - e.y).toInt()
            if((isScrollTop && deltaY < 0) || !helper.parent.isScrollEnd) {
                //如果子RecyclerView已经滑动到顶部，需要让父RecyclerView滑动剩余的距离
                if(deltaY != 0) {
                    helper.parent.scrollBy(0, deltaY)
                    return false
                }
            }
        }

        lastY = e.y
        return super.onTouchEvent(e)
    }

    private var targetVelocityY = 0
    private var flingStarting = false
    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val fling = super.fling(velocityX, velocityY)
        if (fling && velocityY < 0) {
            //向上快速滑动了，如果滑动距离超过子视图的可滑动范围，继续让父视图滑动
            flingStarting = true
            totalDyConsumed = 0
            targetVelocityY = velocityY
        } else {
            flingStarting = false
            targetVelocityY = 0
        }
        return fling
    }

    private fun parentFlingIfEnabled() {
        if (flingStarting) {
            flingStarting = false

            val helper = nestedScrollHelper
            helper ?: return

            val distance = helper.getSplineFlingDistance(targetVelocityY)
            val remain = distance - abs(totalDyConsumed)
            if (remain > 0) {
                val velocity = helper.getSplineFlingVelocity(remain)
                helper.parent.fling(0, -velocity)
            }
        }
    }
}