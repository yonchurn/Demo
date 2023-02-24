package com.common.base.popup

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.common.base.R
import com.common.base.base.interf.VoidCallback
import com.common.base.extension.*
import com.common.base.utils.SizeUtils
import com.common.base.utils.ViewUtils

//基础弹窗容器，系统的有各种限制
open class BasePopupContainer : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    //背景
    private val backgroundView: View by lazy {
        val view = View(context)
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
        view.setOnSingleListener {
            dismiss(true)
        }
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        view
    }

    //返回键
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dismiss(true)
        }
    }

    init {
        addView(backgroundView)
        if (context is AppCompatActivity) {
            val activity = context as AppCompatActivity
            activity.onBackPressedDispatcher.addCallback(onBackPressedCallback)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        onBackPressedCallback.isEnabled = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onBackPressedCallback.isEnabled = false
    }

    //弹窗消失回调
    private val onDismissHandlers: HashSet<VoidCallback> by lazy { HashSet() }

    //内容
    private var _contentView: View? = null

    //是否要动画
    private var shouldAnimate = true

    //添加弹窗消失回调
    fun addOnDismissHandler(onDismissHandler: VoidCallback) {
        onDismissHandlers.add(onDismissHandler)
    }

    //移除
    fun removeOnDismissHandler(onDismissHandler: VoidCallback) {
        onDismissHandlers.remove(onDismissHandler)
    }

    //动画显示弹窗，默认有动画
    fun showAsDropDown(anchor: View, animate: Boolean) {
        if (parent != null) return

        shouldAnimate = animate
        val parent = ViewUtils.findSuitableParent(anchor)
        if (parent != null) {
            parent.addView(this)

            val locations = IntArray(2)
            anchor.getLocationOnScreen(locations)
            if (layoutParams is MarginLayoutParams) {
                val params = layoutParams as MarginLayoutParams
                params.topMargin = locations[1] + anchor.height - SizeUtils.getStatusBarHeight(context)
                layoutParams = params
            }

            if (shouldAnimate) {
                if (isLaidOut) {
                    executeShowAnimation()
                } else {
                    invisible()
                }
            }
        }
    }

    //设置内容视图
    fun setContentView(view: View) {
        _contentView?.removeFromParent()
        _contentView = view
        addView(view, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (shouldAnimate) {
            executeShowAnimation()
            shouldAnimate = false
        }
    }

    //显示动画
    private fun executeShowAnimation() {
        post {
            visible()
            val animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0f
            )
            animation.duration = 250
            _contentView?.startAnimation(animation)

            val alphaAnimation = AlphaAnimation(0f, 1.0f)
            alphaAnimation.duration = 250
            alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
            backgroundView.startAnimation(alphaAnimation)
        }
    }

    fun dismiss(animate: Boolean) {
        if (animate) {
            executeDismissAnimation()
        } else {
            removeFromParent()
            for (onDismissHandler in onDismissHandlers) {
                onDismissHandler()
            }
        }
    }

    //消失动画
    private fun executeDismissAnimation() {

        val animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, -1f
        )
        animation.duration = 250
        _contentView?.startAnimation(animation)

        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
        alphaAnimation.duration = 250
        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                dismiss(false)
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        backgroundView.startAnimation(alphaAnimation)
    }
}