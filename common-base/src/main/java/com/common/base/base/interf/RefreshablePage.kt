package com.common.base.base.interf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.CallSuper
import com.common.base.BaseInitializer
import com.common.base.R
import com.common.base.refresh.RefreshHeader
import com.common.base.utils.SizeUtils
import com.common.base.widget.BackToTopButton
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener

/**
 * 可下拉刷新的页面
 */
interface RefreshablePage: BasePage, RefreshHeader.RefreshOnScrollHandler, OnRefreshListener {

    //<editor-fold desc="变量">

    //当前第几页
    var curPage: Int

    //是否正在下拉刷新
    var refreshing: Boolean

    //刷新头部
    var refreshHeader: RefreshHeader?

    //是否有下拉刷新功能
    val hasRefresh: Boolean

    //下拉刷新容器
    var smartRefreshLayout: SmartRefreshLayout?

    //</editor-fold>

    //<editor-fold desc="刷新">

    //开始下拉刷新，子类重写
    @CallSuper
    override fun onRefresh(refreshLayout: RefreshLayout) {
        refreshHeader?.shouldCloseImmediately = false
        refreshing = true
        onRefresh()
    }

    //回调重写这个
    fun onRefresh() {}

    //调用adapter的
    fun notifyDataSetChanged() {

    }

    override fun setContainerContentView(contentView: View?) {
        super.setContainerContentView(contentView)
        initRefreshLayout()
    }

    override fun setContainerContentView(layoutResId: Int) {
        super.setContainerContentView(layoutResId)
        initRefreshLayout()
    }

    //设置是否可以刷新
    fun setRefreshEnable(enable: Boolean) {
        smartRefreshLayout?.setEnableRefresh(enable)
    }

    //初始化刷新控件
    private fun initRefreshLayout() {
        if (hasRefresh) {
            smartRefreshLayout = baseContainer?.findViewById(R.id.smartRefreshLayout)
            refreshHeader = createRefreshHeader()
            refreshHeader?.also {

                it.onScrollHandler = this
                smartRefreshLayout?.apply {
                    setHeaderHeight(50f)
                    setRefreshHeader(it)
                    setOnRefreshListener(this@RefreshablePage)
                    setEnableRefresh(true)
                    setEnableAutoLoadMore(false)
                    setEnableLoadMore(false)
                }
            }

        }
    }

    //获取下拉刷新头部
    fun createRefreshHeader(): RefreshHeader {
        return if (BaseInitializer.refreshHeaderCreator != null) {
            BaseInitializer.refreshHeaderCreator!!(attachedContext!!)
        } else {
            val res = BaseInitializer.refreshHeaderRes ?: R.layout.default_refresh_header
            LayoutInflater.from(attachedContext).inflate(res, null) as RefreshHeader
        }
    }

    //停止下拉刷新
    @CallSuper
    fun stopRefresh() {
        stopRefresh(false)
    }

    //停止下拉刷新
    @CallSuper
    fun stopRefresh(closeImmediately: Boolean) {
        if (smartRefreshLayout != null && refreshing) {
            refreshing = false
            refreshHeader!!.shouldCloseImmediately = closeImmediately
            smartRefreshLayout!!.finishRefresh()
        }
    }

    //手动刷新
    fun startRefresh(){}

    override fun onScroll(isDragging: Boolean, percent: Float, offset: Int) {

    }

    //回到顶部按钮显示
    fun configureBackToTopButton(button: BackToTopButton) {
        button.apply{
            setImageResource(R.drawable.back_to_top_icon)
            val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, SizeUtils.pxFormDip(20f, context), SizeUtils.pxFormDip(20f, context))
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            params.addRule(RelativeLayout.ABOVE, R.id.base_fragment_bottom_id)
            params.alignWithParent = true

            visibility = View.GONE
            layoutParams = params
            baseContainer?.addView(this)
        }
    }

    //</editor-fold>
}