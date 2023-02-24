package com.common.base.drawable

import android.graphics.*
import android.content.Context
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.BitmapShader
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import com.common.base.properties.ObservableProperty
import kotlin.math.min
import kotlin.reflect.KProperty


/**
 * 圆角边框drawable
 */
@Suppress("unused_parameter")
class CornerBorderDrawable : BaseDrawable, ObservableProperty.Callback{

    //左上角圆角 px
    var leftTopCornerRadius by ObservableProperty(0, this)

    //右上角圆角 px
    var rightTopCornerRadius by ObservableProperty(0, this)

    //左下角圆角 px
    var leftBottomCornerRadius by ObservableProperty(0, this)

    //右下角圆角 px
    var rightBottomCornerRadius by ObservableProperty(0, this)

    //是否全圆
    var shouldAbsoluteCircle by ObservableProperty(false, this)

    //边框线条厚度 px
    var borderWidth by ObservableProperty(0, this)

    //边框线条颜色
    var borderColor by ObservableProperty(Color.TRANSPARENT, this)

    //背景填充颜色
    var backgroundColor by ObservableProperty(Color.TRANSPARENT, this)

    //位图
    private var bitmap: Bitmap? = null

    //位图着色器
    private var bitmapShader: BitmapShader? = null

    //位图画笔
    private var bitmapPaint: Paint? = null

    constructor() : super()

    override fun onPropertyValueChange(oldValue: Any?, newValue: Any?, property: KProperty<*>) {
        invalidateSelf()
    }

    /**
     * 通过位图构建
     * @param bitmap 位图
     */
    constructor(bitmap: Bitmap?): super() {
        this.bitmap = bitmap
        if (bitmap != null) {
            bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            bitmapPaint = Paint()
            bitmapPaint?.shader = bitmapShader
            bitmapPaint?.isAntiAlias = true
            bitmapPaint?.style = Paint.Style.FILL_AND_STROKE
        }
    }

    /**
     * 通过资源id构建
     * @param res 资源id
     * @param context context
     */
    constructor(res: Int, context: Context): this(BitmapFactory.decodeResource(context.resources, res))

    override fun draw(canvas: Canvas) {

        drawBorder(canvas)
        drawBackground(canvas)
        drawBitmap(canvas)
    }

    //获取绘制路径
    private fun getPath(bounds: RectF): Path {
        val path = Path()

        if (shouldAbsoluteCircle) {
            //全圆
            val radius = min(bounds.width(), bounds.height()) / 2.0f
            path.addRoundRect(
                bounds, radius, radius, Path.Direction
                    .CW
            )
        } else {
            //从左下角开始 绕一圈
            path.moveTo(bounds.left, bounds.top + leftTopCornerRadius)
            path.lineTo(bounds.left, bounds.bottom - leftBottomCornerRadius)
            if (leftBottomCornerRadius > 0) {
                path.arcTo(
                    RectF(
                        bounds.left, bounds.bottom - leftBottomCornerRadius * 2,
                        bounds.left + leftBottomCornerRadius * 2, bounds.bottom
                    ), 180f, -90f, false
                )
            }

            path.lineTo(bounds.right - rightBottomCornerRadius, bounds.bottom)
            if (rightBottomCornerRadius > 0) {
                path.arcTo(
                    RectF(
                        bounds.right - rightBottomCornerRadius * 2,
                        bounds.bottom - rightBottomCornerRadius * 2,
                        bounds.right, bounds.bottom
                    ), 90f, -90f, false
                )
            }

            path.lineTo(bounds.right, bounds.top + rightTopCornerRadius)
            if (rightTopCornerRadius > 0) {
                path.arcTo(
                    RectF(
                        bounds.right - rightTopCornerRadius * 2, bounds.top,
                        bounds.right, bounds.top + rightTopCornerRadius * 2
                    ), 0f, -90f, false
                )
            }

            path.lineTo(bounds.left + leftTopCornerRadius, bounds.top)
            if (leftTopCornerRadius > 0) {
                path.arcTo(
                    RectF(
                        bounds.left,
                        bounds.top,
                        bounds.left + leftTopCornerRadius * 2,
                        bounds.top + leftTopCornerRadius * 2
                    ), -90f, -90f,
                    false
                )
            }
        }

        return path
    }

    //设置圆角半径
    fun setCornerRadius(cornerRadius: Int){
        
        leftTopCornerRadius = cornerRadius
        leftBottomCornerRadius = cornerRadius
        rightTopCornerRadius = cornerRadius
        rightBottomCornerRadius = cornerRadius
        invalidateSelf()
    }

    //设置圆角半径
    fun setCornerRadius(leftTopCornerRadius: Int, leftBottomCornerRadius: Int, rightTopCornerRadius: Int, rightBottomCornerRadius: Int){

        this.leftTopCornerRadius = leftTopCornerRadius
        this.leftBottomCornerRadius = leftBottomCornerRadius
        this.rightTopCornerRadius = rightTopCornerRadius
        this.rightBottomCornerRadius = rightBottomCornerRadius
    }

    //复制一份
    override fun copy(): CornerBorderDrawable {

        val drawable = CornerBorderDrawable()
        drawable.shouldAbsoluteCircle = shouldAbsoluteCircle
        drawable.backgroundColor = backgroundColor
        drawable.borderColor = borderColor
        drawable.borderWidth = borderWidth
        drawable.bitmap = bitmap
        drawable.bitmapShader = bitmapShader
        drawable.bitmapPaint = bitmapPaint
        drawable.leftTopCornerRadius = leftTopCornerRadius
        drawable.leftBottomCornerRadius = leftBottomCornerRadius
        drawable.rightTopCornerRadius = rightTopCornerRadius
        drawable.rightBottomCornerRadius = rightBottomCornerRadius

        return drawable
    }

    //绘制边框
    private fun drawBorder(canvas: Canvas) {

        val existBorder = borderWidth > 0 && Color.alpha(borderColor) != 0

        //绘制边框
        if (existBorder) {

            val bounds = RectF(rectF)
            bounds.inset(borderWidth / 2.0f, borderWidth / 2.0f)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth.toFloat()
            paint.color = borderColor
            canvas.drawPath(getPath(bounds), paint)
        }
    }

    //绘制背景
    private fun drawBackground(canvas: Canvas) {

        //绘制背景
        if (Color.alpha(backgroundColor) != 0) {

            val bounds = RectF(rectF)

            val existBorder = borderWidth > 0 && Color.alpha(borderColor) != 0
            val margin = if (existBorder) borderWidth else 0
            bounds.inset(margin.toFloat(), margin.toFloat())

            paint.color = backgroundColor
            paint.style = Paint.Style.FILL
            canvas.drawPath(getPath(bounds), paint)
        }
    }

    //获取位图圆角半径
    private fun drawBitmap(canvas: Canvas) {

        //绘制位图
        if (bitmapPaint != null) {

            val bounds = RectF(rectF)

            val existBorder = borderWidth > 0 && Color.alpha(borderColor) != 0
            val margin = if (existBorder) borderWidth / 2 else 0
            bounds.inset(margin.toFloat(), margin.toFloat())

            //如果绘制区域不等于位图大小，设置缩放矩阵
            val width = bitmap!!.width.toFloat()
            val height = bitmap!!.height.toFloat()
            if (bounds.width() != width || bounds.height() != height) {
                val matrix = Matrix()
                matrix.setScale(
                    bounds.width() / width,
                    bounds.height() / height
                )
                bitmapShader!!.setLocalMatrix(matrix)
            }

            canvas.drawPath(getPath(bounds), bitmapPaint!!)
        }
    }

    //以下是父类方法
    override fun getIntrinsicHeight(): Int {
        return if (bitmap != null) {
            bitmap!!.height
        } else {
            super.getIntrinsicHeight()
        }
    }

    override fun getIntrinsicWidth(): Int {
        return if (bitmap != null) {
            bitmap!!.height
        } else {
            super.getIntrinsicWidth()
        }
    }
}