package com.common.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.common.base.R
import com.common.base.base.widget.OnSingleClickListener
import com.common.base.refresh.LoadMoreControl
import com.common.base.refresh.LoadMoreFooter
import com.common.base.refresh.LoadMoreStatus

/**
 * 加载更多适配器
 */
internal interface LoadMoreAdapter {

    /**
     * 是否可以加载更多
     */
    var loadMoreEnable: Boolean

    /**
     * 所有数据加载完成后是否还显示 加载更多的视图
     */
    var countToTriggerLoadMore: Int

    /**
     *  加载更多对应的位置
     */
    var loadMorePosition: Int

    /**
     * 加载更多 itemType
     */
    var loadMoreType: Int

    /**
     * 加载更多 没有更多数据 itemType
     */
    var loadMoreNoMoreDataType: Int

    /**
     * 加载更多控制器
     */
    val loadMoreControl: LoadMoreControl

    //获取加载更多内容视图
    fun getLoadMoreContentView(reusedView: View?, parent: ViewGroup): View {
        if (loadMoreControl.loadingStatus == LoadMoreStatus.NO_MORE_DATA) {
            return if (reusedView == null) {
                LayoutInflater.from(parent.context).inflate(R.layout.common_load_more_no_data, parent, false)
            } else reusedView
        } else {
            return if (reusedView == null) {
                val loadMoreFooter = LayoutInflater.from(parent.context).inflate(R.layout.load_more_footer, parent, false) as LoadMoreFooter
                loadMoreFooter.setOnClickListener(object : OnSingleClickListener() {
                    override fun onSingleClick(v: View) {
                        if (loadMoreControl.loadingStatus == LoadMoreStatus.FAIL) {
                            startLoadMore()
                        }
                    }
                })
                loadMoreFooter
            } else reusedView
        }
    }

    //当没有数据时是否可以加载更多 默认不行
    fun loadMoreEnableForData(count: Int): Boolean {
        return count > 0
    }

    //是否可以加载更多
    fun hasMore(): Boolean {
        return loadMoreEnable && loadMoreControl.loadingStatus == LoadMoreStatus.HAS_MORE
    }

    //是否显示加载更多视图
    fun shouldDisplay(): Boolean {
        return loadMoreEnable && loadMoreControl.loadingStatus != LoadMoreStatus.NORMAL
    }

    //是否是没有数据的视图
    fun isNoData(): Boolean {
        return loadMoreEnable && loadMoreControl.loadingStatus == LoadMoreStatus.NO_MORE_DATA
    }

    //是否可以显示空视图
    fun displayEmptyViewEnable(): Boolean {
        if(!loadMoreEnable)
            return true
        return loadMoreControl.loadingStatus.let {
            it == LoadMoreStatus.NORMAL || it == LoadMoreStatus.NO_MORE_DATA
        }
    }

    //是否正在加载更多
    fun isLoadingMore(): Boolean {
        return loadMoreEnable && loadMoreControl.loadingStatus == LoadMoreStatus.LOADING
    }

    /**
     * 开始加载
     */
    fun startLoadMore(){
        if(loadMoreEnable){
            if(loadMoreControl.loadingStatus != LoadMoreStatus.LOADING){
                loadMoreControl.loadingStatus = LoadMoreStatus.LOADING
                onLoadMore()
            }
        }
    }

    /**
     * 停止加载
     */
    @CallSuper
    fun stopLoadMore(hasMore: Boolean){
        if(loadMoreEnable){
            loadMoreControl.loadingStatus = if (hasMore) LoadMoreStatus.HAS_MORE else LoadMoreStatus.NO_MORE_DATA
        }
    }

    /**
     * 加载更多失败了
     */
    fun stopLoadMoreFailed(){
        if(loadMoreEnable){
            loadMoreControl.loadingStatus = LoadMoreStatus.FAIL
        }
    }

    /**
     * 触发加载更多
     */
    fun onLoadMore(){

    }

    /**
     * 没有更多数据加载视图显示
     * */
    fun onLoadMoreNoMoreViewDisplay(lodeMoreView: View){}
}