package com.common.base.base.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import com.common.base.BaseInitializer
import com.common.base.R
import com.common.base.base.constant.OverlayArea
import com.common.base.base.constant.PageStatus
import com.common.base.extension.MATCH_PARENT
import com.common.base.loading.*
import com.common.base.utils.StringUtils
import com.common.base.utils.ToastType
import com.common.base.utils.ToastUtils
import com.common.base.utils.ViewUtils

//基础视图容器
open class BaseContainer: RelativeLayout, InteractionCallback {

    //内容视图
    var contentView: View? = null
    private set

    //标题栏
    var titleBar: TitleBar? = null
        private set

    //页面状态
    private var pageStatus = PageStatus.NORMAL

    //页面是否正在载入
    private var pageLoadingView: PageLoadingView? = null

    //显示菊花
    private var loadingView: LoadingView? = null
    private var loading = false

    //显示空视图信息
    private var emptyView: View? = null

    //添加固定在底部的视图
    var bottomView: View? = null
    private set

    //添加固定在顶部的视图
    var topView: View? = null
    private set

    //顶部视图是否悬浮
    private var topViewFloat = false
    set(value){
        if(value != field){
            field = value
            layoutChildren()
        }
    }

    //事件回调
    var mOnEventCallback: OnEventCallback? = null

    //加载视图覆盖区域 默认都不覆盖
    var overlayArea = 0

    //<editor-fold desc="constructor">

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        setBackgroundColor(Color.WHITE)
    }

    //</editor-fold>

    //<editor-fold desc="标题栏">

    //设置是否需要显示标题栏
    fun setShowTitleBar(show: Boolean) {
        if (show) { //创建导航栏
            if (titleBar == null) {
                titleBar = TitleBar(context)
                val params = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    context.resources.getDimensionPixelOffset(R.dimen.title_bar_height)
                )
                titleBar!!.id = R.id.base_fragment_title_bar_id
                addView(titleBar, 0, params)
                layoutChildren()
            }
            titleBar!!.visibility = View.VISIBLE
        } else {
            if (titleBar != null) {
                titleBar!!.visibility = View.GONE
            }
        }
    }

    fun isTitleBarShowing(): Boolean {
        return titleBar != null && titleBar!!.visibility == View.VISIBLE
    }

    //设置标题
    fun setTitle(title: CharSequence?) {
        titleBar?.setTitle(title)
    }

    fun getTitle(): CharSequence? {
        return titleBar?.getTitle()
    }

    //显示返回按钮
    fun setShowBackButton(show: Boolean) {
        if (titleBar != null) {
            val textView = titleBar!!.setShowBackButton(show)
            textView?.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    if (mOnEventCallback != null) {
                        mOnEventCallback!!.onBack()
                    }
                }
            })
        }
    }

    //</editor-fold>

    //<editor-fold desc="内容">

    fun setContentView(@LayoutRes layoutResId: Int) {
        setContentView(LayoutInflater.from(context).inflate(layoutResId, this, false))
    }

    fun setContentView(view: View?){
        if(contentView != view){
            if(contentView != null){
                removeView(contentView)
            }

            contentView = view
            contentView?.apply {
                id = R.id.base_fragment_content_id
                val params = if (layoutParams is LayoutParams) {
                    layoutParams as LayoutParams
                } else {
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                }
                params.alignWithParent = true
                layoutParams = params

                if (parent !== this@BaseContainer) {
                    ViewUtils.removeFromParent(this)
                    addView(this, 0)
                }
            }
            layoutChildren()
        }
    }

    //重新布局子视图
    private fun layoutChildren() {
        if (contentView != null) {
            val params = contentView!!.layoutParams as LayoutParams

            params.addRule(BELOW, 0)
            if (topView != null && !topViewFloat) {
                params.addRule(BELOW, R.id.base_fragment_top_id)
            } else {
                params.addRule(BELOW, R.id.base_fragment_title_bar_id)
            }

            params.addRule(ABOVE, 0)
            if (bottomView != null) {
                params.addRule(ABOVE, R.id.base_fragment_bottom_id)
            }
            contentView!!.layoutParams = params
        }

        if(pageLoadingView != null){

            val params = pageLoadingView!!.layoutParams as LayoutParams
            params.alignWithParent = true

            params.addRule(BELOW, 0)
            if (topView != null && !topViewFloat && (overlayArea and OverlayArea.PAGE_LOADING_TOP == OverlayArea.PAGE_LOADING_TOP)) {
                params.addRule(BELOW, R.id.base_fragment_top_id)
            } else {
                params.addRule(BELOW, R.id.base_fragment_title_bar_id)
            }

            params.addRule(ABOVE, 0)
            if (bottomView != null && (overlayArea and OverlayArea.PAGE_LOADING_BOTTOM == OverlayArea.PAGE_LOADING_BOTTOM)) {
                params.addRule(ABOVE, R.id.base_fragment_bottom_id)
            }
            pageLoadingView!!.layoutParams = params
        }

        if(emptyView != null){

            val params = emptyView!!.layoutParams as LayoutParams
            params.alignWithParent = true

            params.addRule(BELOW, 0)
            if (topView != null && !topViewFloat && (overlayArea and OverlayArea.EMPTY_TOP == OverlayArea.EMPTY_TOP)) {
                params.addRule(BELOW, R.id.base_fragment_top_id)
            } else {
                params.addRule(BELOW, R.id.base_fragment_title_bar_id)
            }

            params.addRule(ABOVE, 0)
            if (bottomView != null && (overlayArea and OverlayArea.EMPTY_BOTTOM == OverlayArea.EMPTY_BOTTOM)) {
                params.addRule(ABOVE, R.id.base_fragment_bottom_id)
            }
            emptyView!!.layoutParams = params
        }
    }

    //</editor-fold>

    //<editor-fold desc="InteractionCallback">

    override fun showLoading(delay: Long, text: CharSequence?) {
        if (!loading) {
            loading = true
            loadingView = if (BaseInitializer.loadingViewCreator != null) {
                BaseInitializer.loadingViewCreator!!(context)
            } else {
                LayoutInflater.from(context).inflate(R.layout.default_loading_view, this, false) as LoadingView?
            }
            loadingView!!.delay = delay
            if (loadingView is DefaultLoadingView) {
                val loadingText = if(StringUtils.isEmpty(text)) context.getString(R.string.loading_text) else text
                (loadingView as DefaultLoadingView).textView.text = loadingText
            }
            val params = if (loadingView!!.layoutParams is LayoutParams) loadingView!!.layoutParams as LayoutParams else {
                LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
            params.width = LayoutParams.MATCH_PARENT
            params.height = LayoutParams.MATCH_PARENT
            params.alignWithParent = true
            params.addRule(BELOW, R.id.base_fragment_title_bar_id)
            params.addRule(ALIGN_PARENT_BOTTOM)
            addView(loadingView, params)
        }
    }

    override fun hideLoading() {
        if(loading){
            loading = false
            removeView(loadingView)
            loadingView = null
        }
    }

    override fun showToast(text: CharSequence?, textRes: Int?, type: ToastType) {
        var message = text
        if (text == null && textRes != null) {
            message = context.getString(textRes)
        }
        if (message != null) {
            ToastUtils.showToast(message, type)
        }
    }

    //</editor-fold>

    //<editor-fold desc="Loading">

    fun setPageStatus(status: PageStatus, layoutRes: Int = 0){
        if(status != pageStatus){
            when(pageStatus){
                PageStatus.LOADING -> {
                    if(status != PageStatus.FAIL){
                        removeView(pageLoadingView)
                        pageLoadingView = null
                    }
                }
                PageStatus.FAIL -> {
                    if(status != PageStatus.LOADING){
                        removeView(pageLoadingView)
                        pageLoadingView = null
                    }
                }
                PageStatus.EMPTY -> {
                    removeView(emptyView)
                    emptyView = null
                }
                else -> {

                }
            }
            pageStatus = status

            when(pageStatus){
                PageStatus.LOADING -> {
                    loadPageLoadingViewIfNeeded()
                    mOnEventCallback?.onShowPageLoadingView(pageLoadingView!!)
                }
                PageStatus.FAIL -> {
                    loadPageLoadingViewIfNeeded()
                    mOnEventCallback?.onShowPageLoadingView(pageLoadingView!!)
                }
                PageStatus.EMPTY -> {
                    loadEmptyViewIfNeeded(layoutRes)
                    mOnEventCallback?.onShowEmptyView(emptyView!!)
                }
                else -> {

                }
            }

            layoutChildren()
        }
    }

    //加载 pageLoading 如果需要
    private fun loadPageLoadingViewIfNeeded(){
        if((pageStatus == PageStatus.LOADING || pageStatus == PageStatus.FAIL) && pageLoadingView == null){
            pageLoadingView = if (BaseInitializer.pageLoadingViewCreator != null) {
                BaseInitializer.pageLoadingViewCreator!!(context)
            } else {
                LayoutInflater.from(context)
                    .inflate(R.layout.default_page_loading_view, this, false) as PageLoadingView
            }

            pageLoadingView!!.setBackgroundColor(getBackgroundColor())
            pageLoadingView!!.isClickable = true
            pageLoadingView!!.reloadCallback = {
                if(pageStatus == PageStatus.FAIL){
                    mOnEventCallback?.onReloadPage()
                }
            }
            addView(pageLoadingView, LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
        pageLoadingView?.status = pageStatus
    }

    //加载空视图如果需要
    private fun loadEmptyViewIfNeeded(layoutRes: Int){
        if(pageStatus == PageStatus.EMPTY && emptyView == null){
            val res = if (layoutRes > 0) layoutRes else R.layout.page_empty_view
            emptyView = LayoutInflater.from(context).inflate(res, this, false)
            emptyView!!.setBackgroundColor(getBackgroundColor())
            emptyView!!.isClickable = true
            addView(emptyView)
        }
    }

    //获取背景颜色
    private fun getBackgroundColor(): Int {
        return if (background is ColorDrawable) {
            val colorDrawable = background as ColorDrawable
            colorDrawable.color
        } else {
            Color.WHITE
        }
    }

    fun isPageLoading(): Boolean {
        return pageStatus == PageStatus.LOADING
    }

    fun isPageLoadFail(): Boolean {
        return pageStatus == PageStatus.FAIL
    }

    fun isLoading(): Boolean {
        return loading
    }

    //</editor-fold>

    //<editor-fold desc="顶部底部视图">

    //设置底部视图

    fun setBottomView(view: View?, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) {
        if (bottomView !== view) {
            if (bottomView != null) {
                removeView(bottomView)
            }
            bottomView = view
            bottomView?.apply {
                val params = if (layoutParams is LayoutParams) {
                    layoutParams as LayoutParams
                } else {
                    LayoutParams(LayoutParams.MATCH_PARENT, height)
                }

                params.addRule(ALIGN_PARENT_BOTTOM)
                layoutParams = params
                id = R.id.base_fragment_bottom_id
                if (parent !== this@BaseContainer) {
                    ViewUtils.removeFromParent(this)
                    addView(this)
                }
            }

            layoutChildren()
        }
    }

    fun setBottomView(@LayoutRes res: Int) {
        setBottomView(LayoutInflater.from(context).inflate(res, this, false))
    }

    //设置顶部视图
    fun setTopView(view: View?, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) {
        if (topView !== view) {
            if (topView != null) {
                removeView(topView)
            }
            topView = view
            topView?.apply {
                val params = if (layoutParams is LayoutParams) {
                    layoutParams as LayoutParams
                } else {
                    LayoutParams(LayoutParams.MATCH_PARENT, height)
                }
                params.addRule(BELOW, R.id.base_fragment_title_bar_id)
                layoutParams = params

                id = R.id.base_fragment_top_id
                if (parent !== this@BaseContainer) {
                    ViewUtils.removeFromParent(this)
                    addView(this)
                }
            }

            layoutChildren()
        }
    }

    fun setTopView(@LayoutRes res: Int) {
        setTopView(LayoutInflater.from(context).inflate(res, this, false))
    }

    //</editor-fold>


    //事件回调
    interface OnEventCallback {

        /**
         * 页面刷新
         */
        fun onReloadPage()

        /**
         * 点击返回按钮
         */
        fun onBack()

        /**
         * 页面加载视图显示
         * @param pageLoadingView 页面加载视图
         */
        fun onShowPageLoadingView(pageLoadingView: View)

        /**
         * 空视图显示
         * @param emptyView 空视图
         */
        fun onShowEmptyView(emptyView: View)
    }
}