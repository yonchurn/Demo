package com.common.base.refresh

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.common.base.R
import com.common.base.drawable.LoadingDrawable
import com.common.base.utils.SizeUtils

/**
 * 加载更多底部
 */
class LoadMoreFooter : FrameLayout {

    //菊花
    private var loadingDrawable = LoadingDrawable()

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView

    //状态
    var loadingStatus = LoadMoreStatus.NORMAL
    set(value) {
        if(value != field){
            field = value
            update()
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init{
        loadingDrawable.color = Color.GRAY
        loadingDrawable.intrinsicWidth = SizeUtils.pxFormDip(25f, context)
        loadingDrawable.intrinsicHeight = SizeUtils.pxFormDip(25f, context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)

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

    fun getTextView(): TextView {
        return textView
    }

    //刷新UI
    private fun update() {
        when (loadingStatus) {
            LoadMoreStatus.HAS_MORE, LoadMoreStatus.LOADING -> {
                isClickable = false
                imageView.visibility = View.VISIBLE
                loadingDrawable.start()
                textView.text = context.getString(R.string.loading_text)
            }
            LoadMoreStatus.FAIL -> {
                isClickable = true

                imageView.visibility = View.GONE
                loadingDrawable.stop()
                textView.text = context.getString(R.string.load_more_fail)
            }
            else -> {

            }
        }
    }
}