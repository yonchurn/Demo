package com.common.base.base.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.GridView
import com.common.base.R
import com.common.base.base.widget.BaseContainer

/**
 * 网格视图
 */
class GridActivity: RefreshableActivity() {

    val gridView: GridView by lazy { findViewById(R.id.gridView) }

    override fun initialize(inflater: LayoutInflater, container: BaseContainer, saveInstanceState: Bundle?) {
        var res = getRefreshableContentRes()
        if (res <= 0) {
            if (hasRefresh) {
                res = R.layout.grid_refresh_fragment
            } else {
                res = R.layout.grid_fragment
            }
        }

        setContainerContentView(res)
        backToTopButton?.listView = gridView
    }

    override fun notifyDataSetChanged() {
        (gridView.adapter as BaseAdapter?)?.notifyDataSetChanged()
    }

    override fun startRefresh() {
        if (smartRefreshLayout != null && !refreshing) {
            if (gridView.childCount > 0) {
                gridView.setSelection(0)
            }
            smartRefreshLayout!!.autoRefresh()
        }
    }
}