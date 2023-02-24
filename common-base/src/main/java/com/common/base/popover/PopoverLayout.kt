package com.common.base.popover

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import com.common.base.R
import com.common.base.properties.ObservableProperty
import com.common.base.utils.SizeUtils
import kotlin.reflect.KProperty


/**
 * 气泡弹窗
 */
@Suppress("unused_parameter", "LeakingThis")
open class PopoverLayout : FrameLayout, ObservableProperty.Callback{

    //气泡颜色
    var popoverColor by ObservableProperty(Color.WHITE, this)

    //圆角 px
    var cornerRadius by ObservableProperty(0, this)

    //箭头高度 px
    var arrowHeight by ObservableProperty(0, this)

    //箭头宽度 px
    var arrowWidth by ObservableProperty(0, this)

    //箭头方向
    var arrowDirection by ObservableProperty(ArrowDirection.top, this)

    //设置箭头偏移量 0为居中
    var arrowOffset = 0

    //画笔
    private val _paint by lazy{
        Paint()
    }

    //绘制路径
    private val _path by lazy{
        Path()
    }

    //大小
    private var _width = 0  
    private var _height = 0

    //当前padding
    private var _paddingLeft = 0
    private var _paddingTop = 0
    private var _paddingRight = 0
    private var _paddingBottom = 0

    init {
        _paint.strokeJoin = Paint.Join.ROUND
        _paint.strokeCap = Paint.Cap.ROUND
        _paint.isAntiAlias = true //设置抗锯齿
        _paint.style = Paint.Style.FILL
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        _paddingLeft = paddingLeft
        _paddingTop = paddingTop
        _paddingBottom = paddingBottom
        _paddingRight = paddingRight
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PopoverLayout)
        popoverColor = typedArray.getColor(R.styleable.PopoverLayout_popover_color, Color.WHITE)
        cornerRadius = typedArray.getDimensionPixelSize(R.styleable.PopoverLayout_corner_radius, SizeUtils.pxFormDip(5.0f, context))

        arrowWidth = typedArray.getDimensionPixelSize(R.styleable.PopoverLayout_arrow_width, SizeUtils.pxFormDip(20.0f, context))
        arrowHeight = typedArray.getDimensionPixelSize(R.styleable.PopoverLayout_arrow_height, SizeUtils.pxFormDip(12.0f, context))
        arrowOffset = typedArray.getDimensionPixelSize(R.styleable.PopoverLayout_arrow_offset, 0)
        arrowDirection = typedArray.getInt(R.styleable.PopoverLayout_arrow_direction, ArrowDirection.top)

        typedArray.recycle()

        setWillNotDraw(false)

        adjustPadding()
    }

    final override fun setWillNotDraw(willNotDraw: Boolean) {
        super.setWillNotDraw(willNotDraw)
    }

    override fun onPropertyValueChange(oldValue: Any?, newValue: Any?, property: KProperty<*>) {
        postInvalidate()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        _paddingLeft = left
        _paddingTop = top
        _paddingRight = right
        _paddingBottom = bottom
        adjustPadding()
    }

    //调整padding
    private fun adjustPadding() {
        var left = _paddingLeft
        var top = _paddingTop
        var right = _paddingRight
        var bottom = _paddingBottom
        when (arrowDirection) {
            ArrowDirection.top -> {
                top += arrowHeight
            }
            ArrowDirection.left -> {
                left += arrowHeight
            }
            ArrowDirection.right -> {
                right += arrowHeight
            }
            ArrowDirection.bottom -> {
                bottom += arrowHeight
            }
        }
        super.setPadding(left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _width = w
        _height = h
        configurePath()
    }

    override fun invalidate() {
        configurePath()
        super.invalidate()
    }

    override fun postInvalidate() {
        configurePath()
        super.postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (_width != 0 && _height != 0) {
            val saveCount: Int = canvas.save()
            _paint.color = popoverColor
            canvas.drawPath(_path, _paint)
            canvas.restoreToCount(saveCount)
        }
    }

    //配置路径
    private fun configurePath() {
        if (_width == 0 || _height == 0)
            return

        var left = 0f
        var top = 0f
        var right = _width.toFloat()
        var bottom = _height.toFloat()
        val width = _width.toFloat()
        val height = _height.toFloat()
        var arrowX = 0f
        var arrowY = 0f

        _path.reset()
        when (arrowDirection) {
            ArrowDirection.left -> {
                left += arrowHeight
                arrowY = if (arrowOffset == 0) height / 2 else arrowOffset.toFloat()

                //从左边箭头开始
                _path.moveTo(arrowX, arrowY)
                _path.lineTo(left, arrowY + arrowWidth / 2)

                //绘制左下角
                _path.lineTo(left, bottom - cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom), 180f, -90f, false)
                }

                //绘制右下角
                _path.lineTo(right - cornerRadius, bottom)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom), 90f, -90f, false)
                }

                //绘制右上角
                _path.lineTo(width, top + cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, top, right, top + cornerRadius * 2), 0f, -90f, false)
                }

                //绘制左上角
                _path.lineTo(left + cornerRadius, top)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, top, left + cornerRadius * 2, top + cornerRadius * 2), -90f, -90f, false)
                }

                //链接箭头
                _path.lineTo(left, arrowY - arrowWidth / 2)
                _path.lineTo(arrowX, arrowY)
            }
            ArrowDirection.top -> {
                top += arrowHeight
                arrowX = if (arrowOffset == 0) width / 2 else arrowOffset.toFloat()

                //从顶部箭头开始开始
                _path.moveTo(arrowX, arrowY)
                _path.lineTo(arrowX - arrowWidth / 2, top)

                //绘制左上角
                _path.lineTo(left + cornerRadius, top)

                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, top, left + cornerRadius * 2, top + cornerRadius * 2), -90f, -90f, false)
                }

                //绘制左下角
                _path.lineTo(left, bottom - cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom), 180f, -90f, false)
                }

                //绘制右下角
                _path.lineTo(right - cornerRadius, bottom)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom), 90f, -90f, false)
                }

                //绘制右上角
                _path.lineTo(width, top + cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, top, right, top + cornerRadius * 2), 0f, -90f, false)
                }

                //链接箭头
                _path.lineTo(arrowX + arrowWidth / 2, top)
                _path.lineTo(arrowX, arrowY)
            }
            ArrowDirection.right -> {
                right -= arrowHeight
                arrowX = width
                arrowY = if (arrowOffset == 0) height / 2 else arrowOffset.toFloat()

                //从右边箭头开始开始
                _path.moveTo(arrowX, arrowY)
                _path.lineTo(right, arrowY - arrowWidth / 2)

                //绘制右上角
                _path.lineTo(width, top + cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, top, right, top + cornerRadius * 2), 0f, -90f, false)
                }

                //绘制左上角
                _path.lineTo(left + cornerRadius, top)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, top, left + cornerRadius * 2, top + cornerRadius * 2), -90f, -90f, false)
                }

                //绘制左下角
                _path.lineTo(left, bottom - cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom), 180f, -90f, false)
                }

                //绘制右下角
                _path.lineTo(right, bottom - cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom), 90f, -90f, false)
                }

                //链接箭头
                _path.lineTo(right, arrowY + arrowWidth / 2)
                _path.lineTo(arrowX, arrowY)
            }
            ArrowDirection.bottom -> {
                bottom -= arrowHeight
                arrowY = height
                arrowX = if (arrowOffset == 0) width / 2 else arrowOffset.toFloat()

                //从底部箭头开始开始
                _path.moveTo(arrowX, arrowY)
                _path.lineTo(arrowX + arrowWidth / 2, bottom)

                //绘制右下角
                _path.lineTo(right - cornerRadius, bottom)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, bottom - cornerRadius * 2, right, bottom), 90f, -90f, false)
                }

                //绘制右上角
                _path.lineTo(width, top + cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(right - cornerRadius * 2, top, right, top + cornerRadius * 2), 0f, -90f, false)
                }

                //绘制左上角
                _path.lineTo(left + cornerRadius, top)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, top, left + cornerRadius * 2, top + cornerRadius * 2), -90f, -90f, false)
                }

                //绘制左下角
                _path.lineTo(left, bottom - cornerRadius)
                if (cornerRadius > 0) {
                    _path.arcTo(RectF(left, bottom - cornerRadius * 2, left + cornerRadius * 2, bottom), 180f, -90f, false)
                }

                //链接箭头
                _path.lineTo(arrowX - arrowWidth / 2, bottom)
                _path.lineTo(arrowX, arrowY)
            }
        }
        _path.close()
    }
}