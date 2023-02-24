package com.common.base.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.common.base.R
import com.common.base.utils.SizeUtils


/**
 * 虚线
 */
class DashedView : View {

    companion object{
        //虚线形状
        const val SHAPE_LINE = 0 //只是一条线

        const val SHAPE_RECT = 1 //矩形 可设置圆角


        //方向
        const val HORIZONTAL = 0 //水平

        const val VERTICAL = 1 //垂直
    }

    //画笔
    private val _paint: Paint = Paint()

    //路径
    private val _path: Path = Path()

    //方向
    var orientation = HORIZONTAL
    set(value) {
        if(value != field){
            field = value
            postInvalidate()
        }
    }

    //虚线每段宽度
    var dashLength = 0
        set(value) {
            if(value != field){
                field = value
                if(dashInterval != 0){
                    _paint.pathEffect = DashPathEffect(floatArrayOf(field.toFloat(), dashInterval.toFloat()), 0f)
                }
                postInvalidate()
            }
        }

    //虚线间隔宽度
    var dashInterval = 0
        set(value) {
            if(value != field){
                field = value
                if(dashLength != 0){
                    _paint.pathEffect = DashPathEffect(floatArrayOf(dashLength.toFloat(), field.toFloat()), 0f)
                }
                postInvalidate()
            }
        }

    //虚线颜色
    @ColorInt
    var dashesColor = 0
        set(value) {
            if(value != field){
                field = value
                postInvalidate()
            }
        }

    //线宽度
    var strokeWidth = 0
        set(value) {
            if(value != field){
                field = value
                postInvalidate()
            }
        }

    //形状
    var shape = SHAPE_LINE
        set(value) {
            if(value != field){
                field = value
                postInvalidate()
            }
        }

    //圆角 SHAPE_LINE 无效
    var cornerRadius = 0
        set(value) {
            if(value != field){
                field = value
                postInvalidate()
            }
        }

    //矩形范围
    private val _rectF = RectF()

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

        if (attrs != null) {
            val array = context!!.obtainStyledAttributes(attrs, R.styleable.DashedView)

            orientation = array.getInt(R.styleable.DashedView_orientation, HORIZONTAL)
            dashLength = array.getDimensionPixelOffset(R.styleable.DashedView_dash_length, SizeUtils.pxFormDip(10f, context))
            dashInterval = array.getDimensionPixelOffset(R.styleable.DashedView_dash_interval, SizeUtils.pxFormDip(5f, context))

            dashesColor = array.getColor(R.styleable.DashedView_dash_color, Color.GRAY)
            strokeWidth = array.getDimensionPixelOffset(R.styleable.DashedView_dash_stroke_width, SizeUtils.pxFormDip(1f, context))

            shape = array.getInt(R.styleable.DashedView_shape, SHAPE_LINE)
            cornerRadius = array.getDimensionPixelOffset(R.styleable.DashedView_rect_corner_radius, 0)

            array.recycle()
        }

        setWillNotDraw(false)
    }

    init {
        _paint.isAntiAlias = true
        _paint.style = Paint.Style.STROKE
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveCount: Int = canvas.save()
        _path.reset()

        _paint.strokeWidth = strokeWidth.toFloat()
        _paint.color = dashesColor

        val w = width.toFloat()
        val h = height.toFloat()

        when (shape) {
            SHAPE_LINE -> {
                when (orientation) {
                    HORIZONTAL -> {
                        _path.moveTo(0f, h / 2)
                        _path.lineTo(w, h / 2)
                    }
                    VERTICAL -> {
                        _path.moveTo(w / 2, 0f)
                        _path.lineTo(w / 2, h)
                    }
                }
            }
            SHAPE_RECT -> {

                //画矩形
                _rectF.left = strokeWidth / 2.toFloat()
                _rectF.top = strokeWidth / 2.toFloat()
                _rectF.right = width - strokeWidth.toFloat()
                _rectF.bottom = height - strokeWidth.toFloat()
                _path.addRoundRect(_rectF, cornerRadius.toFloat(), cornerRadius.toFloat(), Path.Direction.CCW)
            }
        }
        canvas.drawPath(_path, _paint)
        canvas.restoreToCount(saveCount)
    }
}