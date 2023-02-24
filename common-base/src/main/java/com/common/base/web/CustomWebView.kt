package com.common.base.web

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewParent
import android.webkit.WebView
import androidx.viewpager.widget.ViewPager

/**
 * web
 */
@Suppress("ClickableViewAccessibility")
open class CustomWebView : WebView {

    //滑动回调
    var onScrollChanged: ((offsetY: Int, contentHeight: Float) -> Unit)? = null

    //水平滑动
    private var _clampedX = false

    //关联的viewPager 防止webView和viewPager 滑动冲突
    private var _viewPager: ViewPager? = null

    //是否需要获取viewPager
    private var _shouldGetViewPager = true

    //每个构造方法必须调用super，否则会出现不可预料的问题，比如键盘弹不出来
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    /**
     * 设置要显示的html文本
     * @param html String?
     */
    fun setHtml(html: String?) {
        var data = html
        if (!TextUtils.isEmpty(data)) {
            data += "<style>img {width:100%;}</style><meta name=\"viewport\" content=\"width=device-width," +
                    "initial-scale=1\"/>"
            loadDataWithBaseURL(null, data!!, "text/html", "utf-8", null)
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        if (this.onScrollChanged != null) {
            this.onScrollChanged!!(scrollY, contentHeight.toFloat() * scaleY)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (_viewPager == null && _shouldGetViewPager) {
            _shouldGetViewPager = false
            _viewPager = getViewPager(parent)
        }
        if (_viewPager != null) {
            if (event.pointerCount == 1) {

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {

                        _clampedX = false

                        //事件由webView处理
                        _viewPager!!.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_MOVE -> {

                        //嵌套Viewpager时
                        _viewPager!!.requestDisallowInterceptTouchEvent(!_clampedX)
                    }
                    else -> {
                        _viewPager!!.requestDisallowInterceptTouchEvent(false)
                    }
                }
            } else {

                //使webView可以双指缩放(前提是webView必须开启缩放功能,并且加载的网页也支持缩放)
                _viewPager!!.requestDisallowInterceptTouchEvent(true)
            }
        }
        return super.onTouchEvent(event)
    }

    //获取对应viewPager
    private fun getViewPager(viewParent: ViewParent?): ViewPager? {
        return if (viewParent != null) {
            if (viewParent is ViewPager) {
                viewParent
            } else {
                getViewPager(viewParent.parent)
            }
        } else null
    }

    //当webView滚动到边界时执行
    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        _clampedX = clampedX
    }
}