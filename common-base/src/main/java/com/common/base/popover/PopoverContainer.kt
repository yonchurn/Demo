package com.common.base.popover

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.ScaleAnimation
import androidx.core.view.ViewCompat
import com.common.base.base.widget.OnSingleClickListener
import com.common.base.utils.ViewUtils
import kotlin.math.min


/**
 * 弹窗容器
 */
open class PopoverContainer : ViewGroup {

    //当前视图在屏幕中位置
    private val _locations = IntArray(2)

    //气泡
    val popoverLayout = PopoverLayout(context)

    //遮罩
    val overlayView = View(context)

    //点击的视图
    private var _clickedView: View? = null
    private val _clickedLocations = IntArray(2)

    //是否需要动画
    private var _shouldAnimate = false

    //当前padding
    private var _paddingLeft = 0
    private var _paddingTop = 0
    private var _paddingRight = 0
    private var _paddingBottom = 0

    //回调
    var onShowListener: ((container: PopoverContainer) -> Unit)? = null
    var onDismissListener: ((container: PopoverContainer) -> Unit)? = null

    //是否要执行动画
    private var _shouldExecuteAnimate = true
    
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View) {
                dismiss(true)
            }
        })

        overlayView.setBackgroundColor(Color.TRANSPARENT)
        addView(overlayView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(popoverLayout, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    final override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
    }

    final override fun addView(child: View?, params: LayoutParams?) {
        super.addView(child, params)
    }

    //设置内容视图
    open fun setContentView(view: View) {
        popoverLayout.addView(view)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        _paddingLeft = left
        _paddingRight = right
        _paddingTop = top
        _paddingBottom = bottom
        super.setPadding(0, 0, 0, 0)
    }

    //显示
    open fun show(clickedView: View?, animate: Boolean) {
        _clickedView = clickedView
        _shouldAnimate = animate

        _clickedView?.also {
            it.getLocationOnScreen(_clickedLocations)

            //如果没有父视图，添加
            if(parent == null){
                val parent = ViewUtils.findSuitableParent(it)
                if(parent != null){
                    parent.addView(this)
                }
            }
        }

        val params = layoutParams
        params.width = LayoutParams.MATCH_PARENT
        params.height = LayoutParams.MATCH_PARENT

        if(_shouldAnimate){
            visibility = INVISIBLE
        }

        _shouldExecuteAnimate = true
        if(ViewCompat.isLaidOut(this)){
            executeAnimate()
        }
    }

    //移除
    open fun dismiss(animate: Boolean) {
        if (animate) {
            val animation = ScaleAnimation(1.0f, 0f, 1.0f, 0f, getCurrentPivotX(), 0f)
            animation.duration = 250
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    onDismiss()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            popoverLayout.startAnimation(animation)
            val alphaAnimation = AlphaAnimation(1.0f, 0f)
            alphaAnimation.duration = 250
            overlayView.startAnimation(alphaAnimation)
        } else {
            onDismiss()
        }
    }

    private fun onDismiss(){
        ViewUtils.removeFromParent(this)
        if (onDismissListener != null) {
            onDismissListener!!(this)
        }
    }

    //获取动画x
    private fun getCurrentPivotX(): Float {
        return (if (_clickedView != null) {
            popoverLayout.arrowOffset
        } else {
            popoverLayout.measuredWidth / 2
        }).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //测量子视图大小
        overlayView.measure(widthMeasureSpec, heightMeasureSpec)
        val params: LayoutParams = popoverLayout.layoutParams

        val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, _paddingLeft + _paddingRight, params.width)
        val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, _paddingTop + _paddingBottom, params.height)
        popoverLayout.measure(childWidthMeasureSpec, childHeightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                min(widthSize, popoverLayout.measuredWidth)
            }
            MeasureSpec.UNSPECIFIED -> {
                popoverLayout.measuredWidth
            }
            else -> widthSize
        }
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                min(heightSize, popoverLayout.measuredHeight)
            }
            MeasureSpec.UNSPECIFIED -> {
                popoverLayout.measuredHeight
            }
            else -> heightSize
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

        val width = right - left
        val height = bottom - top
        overlayView.layout(0, 0, width, height)

        var pLeft = _paddingLeft
        var pTop = _paddingTop
        var pRight = pLeft + popoverLayout.measuredWidth
        var pBottom = pTop + popoverLayout.measuredHeight

        getLocationOnScreen(_locations)

        if (_clickedView != null) {
            val x1 = _clickedLocations[0]
            val y1 = _clickedLocations[1]
            val x2 = _locations[0]
            val y2 = _locations[1]

            //中心点位置
            val centerX: Int = x1 - x2 + _clickedView!!.width / 2
            pLeft = centerX - popoverLayout.measuredWidth / 2
            if (pLeft + popoverLayout.measuredWidth > width) {
                pLeft = width - popoverLayout.measuredWidth - _paddingRight
            } else if (pLeft < _paddingLeft){
                pLeft = _paddingLeft
            }

            popoverLayout.arrowOffset = centerX - pLeft
            pRight = pLeft + popoverLayout.measuredWidth

            if (y1 + _clickedView!!.measuredHeight > y2) {
                //点击视图在范围内
                if (y1 < y2 + measuredHeight) {
                    if (y1 - y2 + _clickedView!!.height + popoverLayout.measuredHeight< measuredHeight) {
                        popoverLayout.arrowDirection = ArrowDirection.top
                        pTop = y1 - y2 + _clickedView!!.height + _paddingTop
                        pBottom = pTop + popoverLayout.measuredHeight
                    } else {
                        pBottom = y1 - y2 - _paddingBottom
                        pTop = pBottom - popoverLayout.measuredHeight
                        popoverLayout.arrowDirection = ArrowDirection.bottom
                    }
                } else {
                    //点击视图在弹窗底部
                    popoverLayout.arrowDirection = ArrowDirection.bottom
                    pBottom = measuredHeight - _paddingBottom
                    pTop = pBottom - popoverLayout.measuredHeight
                }
            } else {
                //点击视图在弹窗上面
                popoverLayout.arrowDirection = ArrowDirection.top
                pTop = _paddingTop
                pBottom = pTop + popoverLayout.measuredHeight
            }
        }
        popoverLayout.layout(pLeft, pTop, pRight, pBottom)
        executeAnimate()
    }

    //执行动画
    private fun executeAnimate() {
        if (_shouldExecuteAnimate) {
            _shouldExecuteAnimate = false
            if (_shouldAnimate) {
                post {
                    visibility = VISIBLE
                    val animation = ScaleAnimation(0f, 1.0f, 0f, 1.0f, getCurrentPivotX(), 0f)
                    animation.duration = 250
                    animation.setAnimationListener(object : AnimationListener {
                        override fun onAnimationStart(animation: Animation) {}
                        override fun onAnimationEnd(animation: Animation) {
                            onShow()
                        }

                        override fun onAnimationRepeat(animation: Animation) {}
                    })
                    popoverLayout.startAnimation(animation)
                    val alphaAnimation = AlphaAnimation(0f, 1.0f)
                    alphaAnimation.duration = 250
                    overlayView.startAnimation(alphaAnimation)
                }
            } else {
                onShow()
            }
        }
    }

    //显示
    private fun onShow(){
        if (onShowListener != null) {
            onShowListener!!(this@PopoverContainer)
        }
    }
}