package com.common.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.common.base.base.interf.VoidCallback


/**
 * 回到顶部按钮
 */
class BackToTopButton: AppCompatImageView, AbsListView.OnScrollListener {

    //关联的recyclerView
    var recyclerView: RecyclerView? = null
    set(value) {
        if(value != field){
            field = value
            if (field != null) {
                if(backToTopListener != null){
                    field!!.removeOnScrollListener(backToTopListener!!)
                }

                if (backToTopListener == null) {
                    backToTopListener = BackToTopListener()
                }
                field!!.addOnScrollListener(backToTopListener!!)
            } else {
                backToTopListener = null
            }
        }
    }
    private var backToTopListener: BackToTopListener? = null

    //关联的listView gridView
    var listView: AbsListView? = null
    set(value) {
        if(value != field){
            field = value
            if (field != null) {
                field!!.setOnScrollListener(this)
            }
        }
    }
    private var onScrollListener: AbsListView.OnScrollListener? = null

    //显示第几个时显示回到顶部按钮
    var backToTopPosition = 30

    //触发回到顶部回调
    var onBackToTop: VoidCallback? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        setOnClickListener {
            val rView = recyclerView
            if (rView is StickRecyclerView) {
                rView.removeStickItem()
            }

            val lView = listView
            if (lView is StickListView) {
                lView.removeStickItem()
            }

            if (onBackToTop != null) {
                onBackToTop!!()
            }else{
                if (recyclerView != null) {
                    recyclerView!!.smoothScrollToPosition(0)
                } else if (listView != null) {
                    listView!!.smoothScrollToPosition(0)
                }
            }
            visibility = View.GONE
        }
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            val position = view.firstVisiblePosition
            visibility = if (position >= backToTopPosition) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        if (onScrollListener != null) {
            onScrollListener!!.onScrollStateChanged(view, scrollState)
        }
    }

    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (onScrollListener != null) {
            onScrollListener!!.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)
        }
    }


    inner class BackToTopListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            //滑动停下来了
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager || layoutManager is StaggeredGridLayoutManager) {
                    val position: Int
                    if (layoutManager is LinearLayoutManager){
                         position = layoutManager.findFirstVisibleItemPosition()
                    }else{
                        val firstVisibleItem = (layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(null)
                        position = firstVisibleItem[0]
                    }

                    if (position >= backToTopPosition) {
                        visibility = View.VISIBLE
                    } else {
                        visibility = View.GONE
                    }
                }
            }
        }
    }
}