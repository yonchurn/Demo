package com.common.base.loading

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.base.R
import com.common.base.drawable.CornerBorderDrawable
import com.common.base.drawable.LoadingDrawable
import com.common.base.utils.SizeUtils

/**
 * 默认的loading
 */
class DefaultLoadingView: LoadingView {

    //菊花
    private var loadingDrawable = LoadingDrawable()
    lateinit var textView: TextView
        private set

    private lateinit var imageView: ImageView
    private lateinit var container: View

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init{
        loadingDrawable.intrinsicWidth = SizeUtils.pxFormDip(25f, context)
        loadingDrawable.intrinsicHeight = SizeUtils.pxFormDip(25f, context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        container = findViewById(R.id.container)
        textView = findViewById(R.id.textView)
        imageView = findViewById(R.id.imageView)

        if (container.background == null) {
            val drawable = CornerBorderDrawable()
            drawable.setCornerRadius(SizeUtils.pxFormDip(8f, context))
            drawable.backgroundColor = Color.parseColor("#4c4c4c")
            drawable.attachView(container)
        }

        imageView.setImageDrawable(loadingDrawable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        loadingDrawable.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loadingDrawable.stop()
    }

    override fun getContentView(): View {
        return container
    }
}