package com.common.base.loading

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.base.R
import com.common.base.base.constant.PageStatus
import com.common.base.drawable.LoadingDrawable
import com.common.base.extension.setOnSingleListener
import com.common.base.utils.SizeUtils

//页面加载视图
class DefaultPageLoadingView: PageLoadingView {

    //加载菊花
    private var loadingDrawable = LoadingDrawable()

    //加载失败
    private var pageFailView: View? = null

    private lateinit var imageView: ImageView
    private lateinit var contentView: View
    lateinit var textView: TextView
        private set

    //状态
    override var status = PageStatus.NORMAL
        set(value) {
            if (value != field) {
                field = value
                statusDidChange()
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        loadingDrawable.intrinsicWidth = SizeUtils.pxFormDip(20f, context)
        loadingDrawable.intrinsicHeight = SizeUtils.pxFormDip(20f, context)
        loadingDrawable.color = Color.GRAY
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        contentView = findViewById(R.id.contentView)

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

    //状态改变了
    private fun statusDidChange() {

        when (status) {
            PageStatus.LOADING -> {
                contentView.visibility = VISIBLE
                pageFailView?.visibility = GONE
            }
            PageStatus.FAIL -> {
                loadFailViewIfNeeded()
                contentView.visibility = GONE
                pageFailView?.visibility = VISIBLE
            }
            PageStatus.EMPTY -> {

            }
            PageStatus.NORMAL -> {

            }
        }
    }

    //创建失败界面
    private fun loadFailViewIfNeeded() {
        if (pageFailView == null) {
            pageFailView =
                LayoutInflater.from(context).inflate(R.layout.page_fail_view, this, false)
            pageFailView!!.findViewById<TextView>(R.id.refresh).setOnSingleListener {
                if (reloadCallback != null) {
                    reloadCallback!!()
                }
            }
            addView(pageFailView)
        }
    }
}