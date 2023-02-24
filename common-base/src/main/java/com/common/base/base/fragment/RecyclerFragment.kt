package com.common.base.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import com.common.base.R
import com.common.base.base.widget.BaseContainer
import com.common.base.widget.StickRecyclerView

/**
 * RecyclerView
 */
open class RecyclerFragment : RefreshableFragment() {

    protected val recyclerView: StickRecyclerView by lazy { requireViewById(R.id.recyclerView) }

    override fun initialize(inflater: LayoutInflater, container: BaseContainer, saveInstanceState: Bundle?) {

        var res = getRefreshableContentRes()
        if (res <= 0) {
            if (hasRefresh) {
                res = R.layout.recycler_refresh_fragment
            } else {
                res = R.layout.recycler_fragment
            }
        }
        setContainerContentView(res)
        backToTopButton?.recyclerView = recyclerView
    }

    override fun notifyDataSetChanged() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun startRefresh() {
        if (smartRefreshLayout != null && !refreshing) {
            if (recyclerView.childCount > 0) {
                recyclerView.scrollToPosition(0)
            }
            smartRefreshLayout!!.autoRefresh()
        }
    }
}