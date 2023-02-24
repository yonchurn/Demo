package com.common.base.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.common.base.base.constant.Position
import com.common.base.base.widget.OnSingleClickListener
import com.common.base.refresh.LoadMoreControl
import com.common.base.refresh.LoadMoreFooter
import com.common.base.section.SectionInfo
import com.common.base.viewholder.RecyclerViewHolder
import java.lang.ref.WeakReference

/**
 * Recycler 布局控制器
 */
abstract class RecyclerViewAdapter(recyclerView: RecyclerView) : RecyclerView.Adapter<RecyclerViewHolder>(), ListAdapter, RecyclerViewSectionAdapter {

    companion object{
        const val LOAD_MORE_VIEW_TYPE = Int.MAX_VALUE - 1 //加载更多视图类型

        const val LOAD_MORE_VIEW_NO_DATA_TYPE = Int.MAX_VALUE - 2 //加载更多视图没有数据了

        const val EMPTY_VIEW_TYPE = Int.MAX_VALUE - 3 //空视图类型

        const val HEADER_VIEW_TYPE = Int.MAX_VALUE - 4 //头部视图类型

        const val FOOTER_VIEW_TYPE = Int.MAX_VALUE - 5 //底部视图类型
    }

    private val recyclerViewWeakReference = WeakReference(recyclerView)
    val recyclerView: RecyclerView
        get() = recyclerViewWeakReference.get()!!

    val context: Context
        get() = recyclerView.context

    override var totalCount: Int = 0
    override var realCount: Int = 0

    override val sections: ArrayList<SectionInfo> by lazy {
        ArrayList()
    }
    override var shouldReloadData: Boolean = true

    override var loadMoreEnable: Boolean = false
    override var countToTriggerLoadMore: Int = 0

    override var loadMorePosition: Int = Position.NO_POSITION
    override var loadMoreType: Int = LOAD_MORE_VIEW_TYPE
    override var loadMoreNoMoreDataType: Int = LOAD_MORE_VIEW_NO_DATA_TYPE

    override val loadMoreControl: LoadMoreControl by lazy{
        LoadMoreControl()
    }

    override var emptyType: Int = EMPTY_VIEW_TYPE
    override var emptyPosition: Int = Position.NO_POSITION
    override var shouldDisplayEmptyView: Boolean = true

    override var headerType: Int = HEADER_VIEW_TYPE
    override var footerType: Int = FOOTER_VIEW_TYPE

    init {

        //关闭默认动画
        recyclerView.apply {
            val animator = itemAnimator
            if (animator is DefaultItemAnimator) {
                animator.supportsChangeAnimations = false
            }
        }

        this.setHasStableIds(true)

        //添加数据改变监听
        this.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                shouldReloadData = true
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                shouldReloadData = true
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                shouldReloadData = true
            }
            override fun onChanged() {
                //数据改变，刷新数据
                shouldReloadData = true
            }
        })
    }

    override fun stopLoadMore(hasMore: Boolean) {
        super.stopLoadMore(hasMore)
        notifyDataSetChanged()
    }

    fun stopLoadMore(hasMore: Boolean, itemCount: Int) {
        super.stopLoadMore(hasMore)
        if (itemCount > 0) {
            val positionStart = if (shouldExistFooter() && loadMorePosition != Position
                    .NO_POSITION){
                getItemCount() - 2
            }else if (shouldExistFooter() || loadMorePosition != Position
                    .NO_POSITION){
                getItemCount() - 1
            }else{
                getItemCount()
            }
            notifyItemRangeInserted(positionStart, itemCount)
        } else if (!loadMoreEnable) {
            notifyDataSetChanged()
        }
    }

    /**
     * 获取item
     */
    fun getItem(positionInSection: Int, section: Int): View? {
        return recyclerView.layoutManager?.let {
            val sectionInfo = sections[section]
            val position = sectionInfo.getItemStartPosition() + positionInSection
            it.findViewByPosition(position)
        }
    }

    /**
     * 获取section header
     */
    fun getSectionHeader(section: Int): View? {
        return recyclerView.layoutManager?.let {
            val sectionInfo = sections[section]
            val position = sectionInfo.getHeaderPosition()
            it.findViewByPosition(position)
        }
    }

    /**
     * 获取section footer
     */
    fun getSectionFooter(section: Int): View? {
        return recyclerView.layoutManager?.let {
            val sectionInfo = sections[section]
            val position = sectionInfo.getFooterPosition()
            it.findViewByPosition(position)
        }
    }

    /**
     * 获取item viewHolder
     */
    fun getItemViewHolder(positionInSection: Int, section: Int): RecyclerViewHolder? {
        val item = getItem(positionInSection, section)
        return item?.let {
            recyclerView.getChildViewHolder(it) as RecyclerViewHolder
        }
    }

    /**
     * 获取header viewHolder
     */
    fun getSectionHeaderViewHolder(section: Int): RecyclerViewHolder? {
        val header = getSectionHeader(section)
        return header?.let {
            recyclerView.getChildViewHolder(it) as RecyclerViewHolder
        }
    }

    /**
     * 获取footer viewHolder
     */
    fun getSectionFooterViewHolder(section: Int): RecyclerViewHolder? {
        val footer = getSectionFooter(section)
        return footer?.let {
            recyclerView.getChildViewHolder(it) as RecyclerViewHolder
        }
    }


    final override fun getItemCount(): Int {
        createSectionsIfNeeded()
        return totalCount
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    final override fun getItemViewType(position: Int): Int {
        return getListItemViewType(position)
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return when (viewType) {
            loadMoreType, loadMoreNoMoreDataType -> {
                RecyclerViewHolder(getLoadMoreContentView(null, parent))
            }
            emptyType -> {
                RecyclerViewHolder(LayoutInflater.from(parent.context).inflate(getEmptyViewRes(), parent, false))
            }
            headerType -> {
                onCreateHeaderViewHolder(viewType, parent)!!
            }
            footerType -> {
                onCreateFooterViewHolder(viewType, parent)!!
            }
            else -> {
                val holder = onCreateViewHolder(viewType, parent)
                holder.itemView.setOnClickListener(object : OnSingleClickListener() {

                    override fun onSingleClick(v: View) {

                        //添加点击事件
                        val p = holder.bindingAdapterPosition
                        if (p == RecyclerView.NO_POSITION)
                            return

                        val info: SectionInfo = sectionInfoForPosition(p)!!

                        when{
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
                holder
            }
        }
    }

    final override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {

        triggerLoadMoreIfNeeded(position)

        when (holder.itemViewType) {
            loadMoreType, loadMoreNoMoreDataType -> {
                if (holder.itemViewType == LOAD_MORE_VIEW_TYPE) {
                    val footer = holder.itemView as LoadMoreFooter
                    footer.loadingStatus = loadMoreControl.loadingStatus
                    loadMoreControl.loadMoreFooter = footer
                }

                val params = holder.itemView.layoutParams
                if (params is StaggeredGridLayoutManager.LayoutParams) {
                    params.isFullSpan = true
                } else if (params !is RecyclerView.LayoutParams) {
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
                }

                if (holder.itemViewType == loadMoreNoMoreDataType) onLoadMoreNoMoreViewDisplay(holder.itemView)
            }
            emptyType -> {
                val params = if (holder.itemView.layoutParams is RecyclerView.LayoutParams) {
                    holder.itemView.layoutParams
                } else {
                    RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
                }

                if (params is StaggeredGridLayoutManager.LayoutParams) {
                    params.isFullSpan = true
                }

                var height = getEmptyViewHeight()
                if (height < 0) {
                    height = recyclerView.height
                }
                params.height = height
                holder.itemView.layoutParams = params
                onEmptyViewDisplay(holder.itemView)
            }
            headerType -> {
                onBindHeaderViewHolder(holder)
            }
            footerType -> {
                onBindFooterViewHolder(holder)
            }
            else -> {
                val sectionInfo: SectionInfo = sectionInfoForPosition(position)!!
                when{
                    sectionInfo.isHeaderForPosition(position) -> {
                        onBindSectionHeaderViewHolder(holder, sectionInfo.section)
                    }
                    sectionInfo.isFooterForPosition(position) -> {
                        onBindSectionFooterViewHolder(holder, sectionInfo.section)
                    }
                    else -> {
                        onBindItemViewHolder(holder, sectionInfo.getItemPosition(position), sectionInfo.section)
                    }
                }
            }
        }
    }

    //滚到对应的位置
    fun scrollTo(section: Int) {
        scrollTo(section, false)
    }

    fun scrollTo(section: Int, smooth: Boolean) {
        scrollTo(section, -1, smooth)
    }

    fun scrollTo(section: Int, positionInSection: Int, smooth: Boolean) {
        if (section < sections.size) {
            val info = sections[section]
            var position = info.getHeaderPosition()
            if (positionInSection >= 0) {
                position = info.getItemStartPosition() + positionInSection
            }
            if (smooth) {
                recyclerView.smoothScrollToPosition(position)
            } else {
                recyclerView.scrollToPosition(position)
            }
        }
    }

    //移动到对应位置，如果能置顶则置顶
    fun scrollToWithOffset(positionInSection: Int, offset: Int) {
        scrollToWithOffset(0, positionInSection, offset)
    }

    fun scrollToWithOffset(section: Int, positionInSection: Int, offset: Int) {

        if (recyclerView.layoutManager is LinearLayoutManager) {
            val layoutManager =
                recyclerView.layoutManager as LinearLayoutManager
            val info = sections[section]
            var position = info.getHeaderPosition()
            if (positionInSection >= 0) {
                position = info.getItemStartPosition() + positionInSection
            }
            layoutManager.scrollToPositionWithOffset(position, offset)
        } else {
            throw RuntimeException("scrollToWithOffset 仅仅支持 LinearLayoutManager")
        }
    }
}