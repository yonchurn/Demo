package com.common.base.nested

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.common.base.widget.StickRecyclerView

/**
 * 可嵌套滑动的父容器
 * 要增加 android:descendantFocusability="blocksDescendants"  防止嵌套的recyclerView自动滑动
 */
class NestedParentRecyclerView: StickRecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var nestedScrollHelper: NestedScrollHelper? = null

    val isScrollEnd: Boolean
        get() {
            val count = childCount
            if (count > 0) {
                val last = getChildAt(count - 1)
                return last.bottom == height - paddingBottom && getChildAdapterPosition(last!!) == adapter!!.itemCount - 1
            }
            return false
        }
    // get() = !canScrollVertically(1)

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    childFlingIfEnabled()
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
    var touching = false
        private set

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val helper = nestedScrollHelper
        helper ?: return super.onTouchEvent(e)

        if(lastY == 0f) lastY = e.y
        when(e.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                touching = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching = false
            }
        }

        if(isScrollEnd) {
            //如果父RecyclerView已经滑动到底部，需要让子RecyclerView滑动剩余的距离
            helper.child?.apply {
                val deltaY = (lastY - e.y).toInt()
                if(deltaY != 0) {
                    scrollBy(0, deltaY)
                }
            }
        }
        lastY = e.y
        return super.onTouchEvent(e)
    }

    //快速滑动
    private var targetVelocityY = 0
    private var flingStarting = false
    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val fling = super.fling(velocityX, velocityY)
        if (fling && velocityY > 0) {
            //向下快速滑动了，如果滑动距离超过父视图的可滑动范围，继续让子视图滑动
            flingStarting = true
            totalDyConsumed = 0
            targetVelocityY = velocityY
        } else {
            flingStarting = false
            targetVelocityY = 0
        }
        return fling
    }

    private fun childFlingIfEnabled() {
        if (flingStarting) {
            flingStarting = false
            val helper = nestedScrollHelper
            helper ?: return

            val child = helper.child
            child ?: return

            val distance = helper.getSplineFlingDistance(targetVelocityY)
            val remain = distance - totalDyConsumed
            if (remain > 0) {
                val velocity = helper.getSplineFlingVelocity(remain)
                child.fling(0, velocity)
            }
        }
    }
}