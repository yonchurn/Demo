package com.common.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import com.common.base.R
import com.common.base.base.constant.Position
import com.common.base.base.widget.OnSingleClickListener
import com.common.base.refresh.LoadMoreControl
import com.common.base.refresh.LoadMoreFooter
import com.common.base.section.SectionInfo

/**
 * listView 适配器
 */
abstract class AbsListViewAdapter : BaseAdapter(), ListAdapter, AbsListViewSectionAdapter {

    override var totalCount: Int = 0
    override var realCount: Int = 0

    override val sections: ArrayList<SectionInfo> by lazy {
        ArrayList()
    }
    override var shouldReloadData: Boolean = true

    override var loadMoreEnable: Boolean = false
    override var countToTriggerLoadMore: Int = 0

    override var loadMorePosition: Int = Position.NO_POSITION
    override var loadMoreType: Int = 0
    override var loadMoreNoMoreDataType: Int = 0

    override val loadMoreControl: LoadMoreControl by lazy {
        LoadMoreControl()
    }

    override var emptyType: Int = 0
    override var emptyPosition: Int = Position.NO_POSITION
    override var shouldDisplayEmptyView: Boolean = true

    override var headerType: Int = 0
    override var footerType: Int = 0

    override fun notifyDataSetChanged() {
        shouldReloadData = true
        super.notifyDataSetChanged()
    }

    override fun stopLoadMore(hasMore: Boolean) {
        super.stopLoadMore(hasMore)
        notifyDataSetChanged()
    }

    final override fun getCount(): Int {

        createSectionsIfNeeded()
        return totalCount
    }

    final override fun getItem(position: Int): Any? {

        val sectionInfo: SectionInfo? = sectionInfoForPosition(position)
        return when {
            isEmptyItem(position) || isLoadMoreItem(position) -> {
                null
            }
            position == sectionInfo?.getHeaderPosition() && sectionInfo.isExistHeader -> {
                //存在头部
                getItem(0, sectionInfo.section, ItemType.HEADER)
            }
            position == sectionInfo?.getFooterPosition() && sectionInfo.isExistFooter -> {
                //存在底部
                getItem(0, sectionInfo.section, ItemType.FOOTER)
            }
            else -> {
                getItem(sectionInfo!!.getItemPosition(position), sectionInfo.section, ItemType.VIEW)
            }
        }
    }

    final override fun getItemViewType(position: Int): Int {
        return getListItemViewType(position)
    }

    final override fun getViewTypeCount(): Int {
        var count = numberOfViewTypes()
        headerType = count
        count++

        footerType = count
        count++

        loadMoreType = count
        count++

        loadMoreNoMoreDataType = count
        count++

        emptyType = count
        count++

        return count
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    final override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        //触发加载更多
        triggerLoadMoreIfNeeded(position)

        var result = convertView

        //判断重用的view是否正确
        val type = getItemViewType(position)
        if (convertView != null) {
            val tag = convertView.getTag(R.id.list_view_type_tag_key)
            if (tag is Int) {
                if (tag != type) {
                    result = null
                }
            } else {
                result = null
            }
        }

        //显示空视图
        if (isEmptyItem(position)) {
            val emptyView = if (result == null) {
                LayoutInflater.from(parent.context).inflate(getEmptyViewRes(), parent, false)
            } else {
                result
            }
            emptyView.setTag(R.id.list_view_type_tag_key, type)
            var params = emptyView.layoutParams

            if (params !is AbsListView.LayoutParams) {
                params = AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT
                )
            }
            var height = getEmptyViewHeight()
            if (height <= 0) {
                height = parent.height
            }

            params.height = height
            return emptyView.apply {
                layoutParams = params
                onEmptyViewDisplay(this)
            }
        }

        if (isLoadMoreItem(position)) {
            result = getLoadMoreContentView(result, parent)
            result.setTag(R.id.list_view_type_tag_key, type)
            if (result is LoadMoreFooter) {
                result.loadingStatus = loadMoreControl.loadingStatus
                loadMoreControl.loadMoreFooter = result
            }
            return result
        }


        val sectionInfo: SectionInfo = sectionInfoForPosition(position)!!
        val view = when {
            sectionInfo.isHeaderForPosition(position) -> {
                getSectionHeader(sectionInfo.section, result, parent)
            }
            sectionInfo.isFooterForPosition(position) -> {
                getSectionFooter(sectionInfo.section, result, parent)
            }
            else -> {
                getView(sectionInfo.getItemPosition(position), sectionInfo.section, result, parent)
            }
        }

        return view.apply {
            setTag(R.id.list_view_type_tag_key, type)
            setTag(R.id.list_view_item_position_tag_key, position)
            if (getTag(R.id.list_view_item_onclick_tag_key) == null) {

                //添加点击事件
                setTag(R.id.list_view_item_onclick_tag_key, true)
                setOnClickListener(object : OnSingleClickListener() {

                    override fun onSingleClick(v: View) {
                        val p = v.getTag(R.id.list_view_item_position_tag_key) as Int
                        val info: SectionInfo = sectionInfoForPosition(p)!!

                        when {
                            info.isHeaderForPosition(p) -> {
                                onHeaderClick(info.section, v)
                            }
                            info.isFooterForPosition(p) -> {
                                onFooterClick(info.section, v)
                            }
                            else -> {
                                onItemClick(info.getItemPosition(p), info.section, v)
                            }
                        }
                    }
                })
            }
        }
    }
}