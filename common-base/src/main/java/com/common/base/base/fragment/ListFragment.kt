package com.common.base.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.ListView
import com.common.base.R
import com.common.base.base.widget.BaseContainer
import com.common.base.widget.StickListView

/**
 * 列表 listView
 */
open class ListFragment : RefreshableFragment() {

    protected val listView: StickListView by lazy { requireViewById(R.id.listView) }

    override fun initialize(inflater: LayoutInflater, container: BaseContainer, saveInstanceState: Bundle?) {

        var res: Int = getRefreshableContentRes()
        if (res <= 0) {
            if (hasRefresh) {
                res = R.layout.list_refresh_fragment
            } else {
                res = R.layout.list_fragment
            }
        }

        setContainerContentView(res)
        backToTopButton?.listView = listView
    }

    override fun notifyDataSetChanged() {
        (listView.adapter as BaseAdapter?)?.notifyDataSetChanged()
    }

    override fun startRefresh() {
        if (smartRefreshLayout != null && !refreshing) {
            if (listView.childCount > 0) {
                listView.setSelection(0)
            }
            smartRefreshLayout!!.autoRefresh()
        }
    }
}