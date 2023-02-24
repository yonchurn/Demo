package com.common.base.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.FloatRange


/**
 * 进度条
 */
class ProgressBar : View {

    //画笔
    private val _paint = Paint()

    //进度条颜色
    var progressColor: Int = Color.BLUE
        set(value) {
            if (value != field) {
                field = value
                _paint.color = progressColor
                invalidate()
            }
        }

    //当前进度 0 ~ 1.0f
    private var _currentProgress = 0f

    //目标进度
    private var _targetProgress = 0f

    //动画
    private var _animator: ValueAnimator? = null

    //动画监听
    private val _animatorListenerAdapter: AnimatorListenerAdapter =
        object : AnimatorListenerAdapter() {
            var isCancelled = false
            override fun onAnimationStart(animation: Animator?) {
                isCancelled = false
            }
            override fun onAnimationEnd(animator: Animator) {
                if (_targetProgress >= 1.0f && !isCancelled) {
                    val animation = AlphaAnimation(1.0f, 0f)
                    animation.duration = 200
                    animation.setAnimationListener(object : Animation.AnimationListener{
                        override fun onAnimationStart(animation: Animation?) {

                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            visibility = INVISIBLE
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }
                    })
                    startAnimation(animation)
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                isCancelled = true
            }
        }

    //动画更新
    private val _animatorUpdateListener =
        AnimatorUpdateListener { animation ->
            val value = animation.animatedValue as Float
            _currentProgress = value
            invalidate()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        visibility = INVISIBLE
        setWillNotDraw(false)
    }

    init {
        _paint.isAntiAlias = true
        _paint.isDither = true
        _paint.style = Paint.Style.FILL
        _paint.strokeCap = Paint.Cap.SQUARE
        _paint.color = progressColor
    }

    /**
     * 设置进度
     * @param progress Float
     */
    fun setProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {

        if (_targetProgress != progress) {
            _targetProgress = progress
            if (_currentProgress > _targetProgress) {
                _currentProgress = 0f
            }
            clearAnimation()
            alpha = 1.0f
            visibility = VISIBLE
            if (_targetProgress > _currentProgress) {
                startAnimate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, _currentProgress * width, height.toFloat(), _paint)
    }

    //开始动画
    private fun startAnimate() {
        stopAnimate()
        _animator = ValueAnimator.ofFloat(_currentProgress, _targetProgress)
        if (_targetProgress >= 0.95f) {
            _animator!!.duration = (300 * (1.0f - _currentProgress)).toLong()
            _animator!!.interpolator = DecelerateInterpolator()
        } else {
            _animator!!.duration = (800 * (1.0f - _currentProgress)).toLong()
            _animator!!.interpolator = LinearInterpolator()
        }
        _animator!!.addUpdateListener(_animatorUpdateListener)
        _animator!!.addListener(_animatorListenerAdapter)
        _animator!!.start()
    }

    //停止动画
    private fun stopAnimate() {
        if (_animator != null) {
            _animator!!.cancel()
            _animator = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimate()
    }
}