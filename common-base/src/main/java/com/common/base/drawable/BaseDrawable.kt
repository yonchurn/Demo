package com.common.base.drawable

import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.View
import com.common.base.utils.ViewUtils

/**
 * 基础drawable
 */
abstract class BaseDrawable : Drawable() {

    //画笔
    protected val paint = Paint()

    //范围
    protected val rectF = RectF()

    //内在宽度
    private var intrinsicWidth = -1

    //内在盖度
    private var intrinsicHeight = -1

    init {
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.isAntiAlias = true //设置抗锯齿
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        //必须的，否则会出现不可预料的bug，如键盘弹出后消失，直接getBounds() 返回越来越小的rect
        rectF.left = left.toFloat()
        rectF.top = top.toFloat()
        rectF.right = right.toFloat()
        rectF.bottom = bottom.toFloat()
    }

    fun setIntrinsicWidth(width: Int){
        intrinsicWidth = width
    }

    fun setIntrinsicHeight(height: Int){
        intrinsicHeight = height
    }

    override fun getIntrinsicWidth(): Int {
        return intrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return intrinsicHeight
    }

    //如果drawable用于多个view, 使用这个方法 关联view 将copy一份
    fun attachView(view: View?, shouldCopy: Boolean = false){
        if(shouldCopy){
            ViewUtils.setBackground(this.copy(), view)
        }else{
            ViewUtils.setBackground(this, view)
        }
    }

    //复制一份
    abstract fun copy() : BaseDrawable
}