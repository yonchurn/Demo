package com.zyc.queationdemo.nested

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.common.base.adapter.RecyclerViewAdapter
import com.common.base.base.fragment.RecyclerFragment
import com.common.base.base.widget.BaseContainer
import com.common.base.extension.setOnSingleListener
import com.common.base.nested.NestedChildRecyclerView
import com.common.base.nested.NestedScrollHelper
import com.common.base.viewholder.RecyclerViewHolder
import com.zyc.queationdemo.R
import com.zyc.queationdemo.nested.NestedScrollActivity

class NestedScrollFragment: RecyclerFragment() {

    override fun getRefreshableContentRes(): Int {
        return R.layout.nested_scroll_fragment
    }

    var nestedScrollHelper: NestedScrollHelper? = null
    var filterView: View? = null

    var offset: Float = 0f
        set(value) {
            if (value != field) {
                field = value
//                filterView?.translationY = value
            }
        }

    var onScrollListener: RecyclerView.OnScrollListener? = null
        set(value) {
            field = value
            if (value != null && isInit) {
                recyclerView.addOnScrollListener(value)
            }
        }

    val childRecyclerView: NestedChildRecyclerView
        get() = recyclerView as NestedChildRecyclerView

    var hasFilter = true

    override fun initialize(
        inflater: LayoutInflater,
        container: BaseContainer,
        saveInstanceState: Bundle?
    ) {
        super.initialize(inflater, container, saveInstanceState)

        childRecyclerView.nestedScrollHelper = nestedScrollHelper
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = Adapter(recyclerView)

        filterView = findViewById(R.id.filter)
        filterView!!.setOnSingleListener {
            if (attachedActivity is NestedScrollActivity){
                (attachedActivity as NestedScrollActivity).rollingChildToTop()
            }
        }

        if (onScrollListener != null) {
            recyclerView.addOnScrollListener(onScrollListener!!)
        }
    }

    fun scrollToTopIfNeeded() {
        if (isInit && recyclerView.childCount > 0) {
            recyclerView.scrollToPosition(0)
        }
    }

    override fun showTitleBar(): Boolean {
        return false
    }

    inner class Adapter(recyclerView: RecyclerView) : RecyclerViewAdapter(recyclerView) {

        override fun onCreateViewHolder(viewType: Int, parent: ViewGroup): RecyclerViewHolder {
            return RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_item, parent, false))
        }

        override fun onCreateHeaderViewHolder(
            viewType: Int,
            parent: ViewGroup
        ): RecyclerViewHolder {
            val view = View(context)
            view.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, pxFromDip(40f))
            return RecyclerViewHolder(view)
        }

        override fun numberOfItems(section: Int): Int {
            return 100
        }

        override fun shouldExistHeader(): Boolean {
            return hasFilter
        }

        override fun onBindItemViewHolder(
            viewHolder: RecyclerViewHolder,
            position: Int,
            section: Int
        ) {
            viewHolder.getView<TextView>(R.id.textView).text = "Child Item $position"
        }

        override fun onItemClick(position: Int, section: Int, item: View) {
            println("click child")
        }
    }
}