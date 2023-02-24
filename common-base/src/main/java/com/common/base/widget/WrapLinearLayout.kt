package com.common.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.common.base.R


/**
 * 可换行的线性布局 用xml
 */
class WrapLinearLayout : ViewGroup {

    /**
     * 每行子View的对齐方式
     */
    companion object {
        const val RIGHT = 0
        const val LEFT = 1
        const val CENTER = 2
    }
    
    
    //每一行信息
    private val _wrapLines = ArrayList<WrapLine>()

    //可重用的行信息
    private val _reusedLines = ArrayList<WrapLine>()

    //对齐方式 right 0，left 1，center 2
    var gravity = LEFT

    //水平间距,单位px
    var horizontalSpace = 20

    //垂直间距,单位px
    var verticalSpace = 20

    //是否自动填满
    var isFull: Boolean = false

    //边距
    var inset = 0

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

        if (attrs != null) {
            val typedArray = context!!.obtainStyledAttributes(attrs, R.styleable.WrapLinearLayout)

            gravity = typedArray.getInt(R.styleable.WrapLinearLayout_gravity, 1)
            horizontalSpace = typedArray.getDimensionPixelSize(R.styleable.WrapLinearLayout_horizontal_space, horizontalSpace)
            verticalSpace = typedArray.getDimensionPixelSize(R.styleable.WrapLinearLayout_vertical_space, verticalSpace)
            inset = typedArray.getDimensionPixelSize(R.styleable.WrapLinearLayout_inset, inset)
            isFull = typedArray.getBoolean(R.styleable.WrapLinearLayout_is_full, isFull)

            typedArray.recycle()
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        
        //在调用childView。getMeasure之前必须先调用该行代码，用于对子View大小的测量
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        //计算宽度
        var width = 0
        when (widthMode) {
            MeasureSpec.EXACTLY -> width = widthSize
            MeasureSpec.AT_MOST -> {
                var i = 0
                while (i < childCount) {
                    if (i != 0) {
                        width += horizontalSpace
                    }
                    width += getChildAt(i).measuredWidth
                    i++
                }
                width += paddingLeft + paddingRight
                width = if (width > widthSize) widthSize else width
            }
            MeasureSpec.UNSPECIFIED -> {
                var i = 0
                while (i < childCount) {
                    if (i != 0) {
                        width += horizontalSpace
                    }
                    width += getChildAt(i).measuredWidth
                    i++
                }
                width += paddingLeft + paddingRight
            }
            else -> width = widthSize
        }

        //不能够在定义属性时初始化，因为onMeasure方法会多次调用
        var wrapLine = dequeueWrapLine(true)

        //根据计算出的宽度，计算出所需要的行数
        for (i in 0 until childCount) {
            if (wrapLine.lineWidth + getChildAt(i).measuredWidth + horizontalSpace > width) {
                wrapLine = dequeueWrapLine(false)
            }
            wrapLine.addView(getChildAt(i))
        }
        
        //添加最后一行
        if (wrapLine.lineViews.size > 0 && !_wrapLines.contains(wrapLine)) {
            _wrapLines.add(wrapLine)
        }

        //移除多余的
        _reusedLines.clear()
        
        //计算宽度
        var height = paddingTop + paddingBottom + inset * 2
        for (i in _wrapLines.indices) {
            if (i != 0) {
                height += verticalSpace
            }
            height += _wrapLines[i].height
        }
        when (heightMode) {
            MeasureSpec.EXACTLY -> height = heightSize
            MeasureSpec.AT_MOST -> height = if (height > heightSize) heightSize else height
            MeasureSpec.UNSPECIFIED -> {
            }
            else -> {
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = paddingTop + inset
        for (i in _wrapLines.indices) {
            var left = paddingLeft + inset
            val wrapLine = _wrapLines[i]
            val lastWidth = measuredWidth - wrapLine.lineWidth
            
            for (j in wrapLine.lineViews.indices) {
                
                val view = wrapLine.lineViews[j]
                if (isFull) {
                    
                    //需要充满当前行时
                    view.layout(left, top, left + view.measuredWidth + lastWidth / wrapLine.lineViews.size, top + view.measuredHeight)
                    left += view.measuredWidth + horizontalSpace + lastWidth / wrapLine.lineViews.size
                } else {
                    when (gravity) {
                        RIGHT -> {
                            //右对齐
                            view.layout(left + lastWidth, top, left + lastWidth + view.measuredWidth, top + view.measuredHeight)
                        }
                        CENTER -> {
                            //居中对齐
                            view.layout(left + lastWidth / 2, top, left + lastWidth / 2 + view.measuredWidth, top + view.measuredHeight)
                        }
                        else -> {
                            //左对齐
                            view.layout(left, top,left + view.measuredWidth, top + view.measuredHeight)
                        }
                    }
                    left += view.measuredWidth + horizontalSpace
                }
            }
            top += wrapLine.height + verticalSpace
        }
    }

    //获取行信息 WrapLine 重用，防止重复创建
    private fun dequeueWrapLine(reset: Boolean) : WrapLine{

        if(reset){
            _reusedLines.clear()
            _reusedLines.addAll(_wrapLines)
            _wrapLines.clear()
        }

        val line = if(_reusedLines.size > 0){
            _reusedLines.removeFirst()
        }else{
            WrapLine()
        }
        line.reset()
        _wrapLines.add(line)
        return line
    }
    
    //用于存放一行子View
    private inner class WrapLine {

        val lineViews = ArrayList<View>()

        //当前行中所需要占用的宽度
        var lineWidth = paddingLeft + paddingRight + inset * 2

        //该行View中所需要占用的最大高度
        var height = 0

        fun addView(view: View) {
            if (lineViews.size != 0) {
                lineWidth += horizontalSpace
            }
            height = if (height > view.measuredHeight) height else view.measuredHeight
            lineWidth += view.measuredWidth
            lineViews.add(view)
        }

        fun reset(){
            lineViews.clear()
            lineWidth = paddingLeft + paddingRight
            height = 0
        }
    }
}