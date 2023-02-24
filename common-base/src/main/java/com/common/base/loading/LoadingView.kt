package com.common.base.loading

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout


/**
 * 加载中菊花
 */
abstract class LoadingView : FrameLayout{

    //延迟
    var delay: Long = 0

    //延迟handler
    private var delayHandler: Handler? = null
    private var delayRunnable: Runnable? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val contentView = getContentView()
        if (delay > 0) {
            contentView.visibility = View.INVISIBLE
            if (delayHandler == null) {
                delayHandler = Handler(Looper.getMainLooper())
                delayRunnable = Runnable {
                    contentView.visibility = View.VISIBLE
                }
            }
            delayHandler!!.postDelayed(delayRunnable!!, delay)
        } else {
            contentView.visibility = View.VISIBLE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        delayHandler?.removeCallbacksAndMessages(null)
    }

    abstract fun getContentView(): View
}