package com.common.base.adapter


import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.SparseArray
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.common.base.R
import com.common.base.section.EdgeInsets
import com.common.base.section.GridSectionInfo
import com.common.base.section.SectionInfo


/**
 * 网格布局
 */
@Suppress("unused_parameter")
abstract class RecyclerViewGridAdapter(recyclerView: RecyclerView,
                              @RecyclerView.Orientation val orientation: Int = RecyclerView.VERTICAL): RecyclerViewAdapter(recyclerView) {

    //item之间的间隔 px
    var itemSpace = 0

    //item和header之间的间隔 px
    var itemHeaderSpace = 0

    //item和footer之间的间隔 px
    var itemFooterSpace = 0

    //section偏移量
    var sectionInsets: EdgeInsets = EdgeInsets(0, 0, 0, 0)

    //是否需要绘制分割线
    var shouldDrawDivider = false

    //分割线颜色
    @ColorInt
    var dividerColor = 0

    //布局方式
    private var _layoutManager: GridLayoutManager? = null

    //所有不同的列的最小公倍数
    private var _differentColumnProduct = 0

    //布局信息
    private val _layoutInfos: SparseArray<LayoutInfo> by lazy{
        SparseArray<LayoutInfo>()
    }

    
    init {
        recyclerView.apply {
            dividerColor = ContextCompat.getColor(context, R.color.divider_color)
            _differentColumnProduct = getDifferentColumnProduct()

            _layoutManager = GridLayoutManager(context, _differentColumnProduct)
            _layoutManager?.apply {

                this.orientation = this@RecyclerViewGridAdapter.orientation

                //通过设置这里来确定分区
                spanCount = _differentColumnProduct
                spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (totalCount == 0) return _differentColumnProduct

                        return when(getItemViewType(position)){
                            LOAD_MORE_VIEW_TYPE, LOAD_MORE_VIEW_NO_DATA_TYPE, EMPTY_VIEW_TYPE, HEADER_VIEW_TYPE, FOOTER_VIEW_TYPE -> {
                                _differentColumnProduct
                            }
                            else -> {
                                this@RecyclerViewGridAdapter.spanCountForPosition(position)
                            }
                        }
                    }

                    override fun setSpanIndexCacheEnabled(cacheSpanIndices: Boolean) {
                        super.setSpanIndexCacheEnabled(true)
                    }
                }
            }

            layoutManager = _layoutManager
            addItemDecoration(GridItemDecoration(orientation))
        }
    }

    override fun createSectionsIfNeeded() {
        if(shouldReloadData){
            _differentColumnProduct = getDifferentColumnProduct()
            _layoutManager?.spanCount = _differentColumnProduct
            _layoutInfos.clear()
        }
        super.createSectionsIfNeeded()
    }

    //创建网格信息
    override fun createSectionInfo(section: Int, numberOfItems: Int, position: Int): SectionInfo {
        val sectionInfo = GridSectionInfo()
        sectionInfo.section = section
        sectionInfo.numberItems = numberOfItems
        sectionInfo.isExistHeader = shouldExistSectionHeader(section)
        sectionInfo.isExistFooter = shouldExistSectionFooter(section)
        sectionInfo.sectionBegin = position

        sectionInfo.sectionInsets = getSectionInsets(section)
        sectionInfo.sectionBegin = position
        sectionInfo.numberOfColumns = numberOfColumns(section)
        sectionInfo.footerUseSectionInsets = footerShouldUseSectionInsets(section)
        sectionInfo.headerUseSectionInsets = headerShouldUseSectionInsets(section)
        sectionInfo.itemSpace = getItemSpace(section)
        sectionInfo.itemHeaderSpace = getItemHeaderSpace(section)
        sectionInfo.itemFooterSpace = getItemFooterSpace(section)

        return sectionInfo
    }
    

    /**
     * 列数
     */
    abstract fun numberOfColumns(section: Int): Int

    /**
     * 不同的列数的最小公倍数
     */
    abstract fun getDifferentColumnProduct(): Int

    /**
     * item之间的间隔
     */
    fun getItemSpace(section: Int): Int {
        return itemSpace
    }

    /**
     * item和 header之间的间隔
     */
    fun getItemHeaderSpace(section: Int): Int {
        return itemHeaderSpace
    }

    /**
     * item和 footer之间的间隔
     */
    fun getItemFooterSpace(section: Int): Int {
        return itemFooterSpace
    }

    /**
     * section的偏移量
     */
    fun getSectionInsets(section: Int): EdgeInsets {
        return sectionInsets
    }

    /**
     * divider颜色
     */
    @ColorInt
    fun getDividerColor(section: Int): Int {
        return dividerColor
    }

    /**
     * header 是否使用insets
     */
    fun headerShouldUseSectionInsets(section: Int): Boolean {
        return true
    }

    /**
     * footer 是否使用insets
     */
    fun footerShouldUseSectionInsets(section: Int): Boolean {
        return true
    }

    //获取spanCount
    private fun spanCountForPosition(position: Int): Int {
        val sectionInfo: GridSectionInfo = sectionInfoForPosition(position)!!

        return if (sectionInfo.isHeaderForPosition(position) || sectionInfo.isFooterForPosition(position)) {
            _differentColumnProduct
        } else {
            _differentColumnProduct / sectionInfo.numberOfColumns
        }
    }


    //布局信息
    private inner class LayoutInfo(position: Int) {

        //偏移量
        var left = 0
        var top = 0
        var right = 0
        var bottom = 0

        //是否靠右
        private fun isOnTheRight(sectionInfo: GridSectionInfo, position: Int): Boolean {
            if (sectionInfo.isFooterForPosition(position) || sectionInfo.isHeaderForPosition(position))
                return true
            return (sectionInfo.getItemPosition(position) + 1) % sectionInfo.numberOfColumns == 0
        }

        //是否靠左
        private fun isOnTheLeft(sectionInfo: GridSectionInfo, position: Int): Boolean {
            if (sectionInfo.isFooterForPosition(position) || sectionInfo.isHeaderForPosition(position))
                return true
            return sectionInfo.getItemPosition(position) % sectionInfo.numberOfColumns == 0
        }

        //是否靠顶
        private fun isOnTheTop(sectionInfo: GridSectionInfo, position: Int): Boolean {

            //头部靠顶
            if (sectionInfo.isHeaderForPosition(position)) return true

            //当不存在item和顶部时，如果是底部
            if (!sectionInfo.isExistHeader && sectionInfo.numberItems == 0 && sectionInfo.isFooterForPosition(position))
                return true

            //存在头部，item都不靠顶
            if (sectionInfo.isExistHeader)
                return false

            //不存在头部，第一行靠顶
            return sectionInfo.getItemPosition(position) < sectionInfo.numberOfColumns
        }

        //是否靠底
        fun isOnTheBottom(sectionInfo: GridSectionInfo, position: Int): Boolean {

            //底部靠底
            if (sectionInfo.isFooterForPosition(position))
                return true

            //当不存在item和底部时，如果是顶部
            if (!sectionInfo.isExistFooter && sectionInfo.numberItems == 0 && sectionInfo.isHeaderForPosition(position))
                return true

            val totalRow = (sectionInfo.numberItems - 1) / sectionInfo.numberOfColumns + 1
            val curRow = sectionInfo.getItemPosition(position) / sectionInfo.numberOfColumns + 1
            return curRow == totalRow
        }

        init {
            when (getItemViewType(position)) {
                EMPTY_VIEW_TYPE, HEADER_VIEW_TYPE, FOOTER_VIEW_TYPE -> {
                    bottom = 0
                    right = bottom
                    top = right
                    left = top
                }
                LOAD_MORE_VIEW_TYPE -> {
                    bottom = 0
                    right = bottom
                    left = right
                    val insets = getSectionInsets(sections.size - 1)
                    if (insets.bottom == 0) {
                        top = getItemSpace(sections.size - 1)
                    }
                }
                else -> {
                    val sectionInfo: GridSectionInfo = sectionInfoForPosition(position)!!
                    
                    val onTheTop = isOnTheTop(sectionInfo, position)
                    val onTheLeft = isOnTheLeft(sectionInfo, position)
                    val onTheRight = isOnTheRight(sectionInfo, position)
                    val onTheBottom = isOnTheBottom(sectionInfo, position)
                    
                    sectionInfo.sectionInsets?.apply {
                        left = if (onTheLeft) left else 0
                        top = if (onTheTop) top else 0
                        right = if (onTheRight) right else 0
                        bottom = if (onTheBottom) bottom else 0
                    }
                    
                    
                    if (sectionInfo.isHeaderForPosition(position)) {
                        if (!sectionInfo.headerUseSectionInsets) {
                            left = 0
                            top = 0
                            right = 0
                            bottom = 0
                        }
                        //头部和item之间的间隔，如果该section内没有item,则忽略间隔
                        if (sectionInfo.numberItems > 0) bottom = sectionInfo.itemHeaderSpace
                    } else if (sectionInfo.isFooterForPosition(position)) {
                        if (!sectionInfo.footerUseSectionInsets) {
                            left = 0
                            top = 0
                            right = 0
                            bottom = 0
                        }
                        //存在item
                        if (sectionInfo.numberItems > 0) {
                            top = sectionInfo.itemFooterSpace
                        }
                    } else {
                        //中间的添加两边间隔，旁边的添加一边间隔， 低于1px无法显示，所以只添加一边间隔
                        if (sectionInfo.itemSpace >= 2) {
                            val space: Int =
                                sectionInfo.itemSpace * (sectionInfo.numberOfColumns - 1) / sectionInfo.numberOfColumns
                            if (onTheRight && !onTheLeft) {
                                left = space
                            } else if (onTheLeft && !onTheRight) {
                                right = space
                            } else if (!onTheLeft && !onTheRight) {
                                left = space / 2
                                right = space / 2
                            }
                        } else {
                            //如果不是最右的item，则添加间隔，否则添加section右边的偏移量
                            if (!onTheRight) {
                                right = sectionInfo.itemSpace
                            }
                        }
                        //如果不是最后一行，添加item间隔
                        if (!onTheBottom) {
                            bottom = sectionInfo.itemSpace
                        } else if (!sectionInfo.isExistFooter) { 
                            //不存在底部，设置section偏移量
                            bottom = if (sectionInfo.sectionInsets == null) 0 else sectionInfo.sectionInsets!!.bottom
                        }
                    }
                }
            }
        }
    }


    //网格分割线
    private inner class GridItemDecoration(private val orientation: Int): RecyclerView.ItemDecoration() {

        //分割线
        private var divider = ColorDrawable()

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            if (!shouldDrawDivider || sections.size == 0) return

            //绘制分割线
            val count = parent.childCount
            for (i in 0 until count) {
                val child = parent.getChildAt(i)
                val layoutParams = child.layoutParams as GridLayoutManager.LayoutParams
                val position = layoutParams.viewLayoutPosition

                val layoutInfo = getLayoutInfoAtPosition(position)
                if (sections.size > 0) {
                    val sectionInfo: GridSectionInfo = sectionInfoForPosition(position)!!
                    divider.color = getDividerColor(sectionInfo.section)
                } else {
                    divider.color = dividerColor
                }

                var left: Int
                var right: Int
                var top: Int
                var bottom: Int
                if (layoutInfo.left > 0) {
                    left = child.left - layoutParams.leftMargin - layoutInfo.left
                    top = child.top - layoutInfo.top
                    right = left + layoutInfo.left
                    bottom = child.bottom + layoutInfo.bottom
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(c)
                }

                if (layoutInfo.right > 0) {
                    left = child.right + layoutParams.rightMargin
                    top = child.top - layoutInfo.top
                    right = left + layoutInfo.right
                    bottom = child.bottom + layoutInfo.bottom
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(c)
                }

                if (layoutInfo.bottom > 0) {
                    left = child.left - layoutInfo.left
                    top = child.bottom + layoutParams.bottomMargin
                    right = child.right + layoutInfo.right
                    bottom = top + layoutInfo.bottom
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(c)
                }
                if (layoutInfo.top > 0) {
                    left = child.left - layoutInfo.left
                    top = child.top - layoutParams.topMargin - layoutInfo.top
                    right = child.right + layoutInfo.right
                    bottom = top + layoutInfo.top
                    divider.setBounds(left, top, right, bottom)
                    divider.draw(c)
                }
            }
        }

        //获取item布局信息
        private fun getLayoutInfoAtPosition(position: Int): LayoutInfo {
            var layoutInfo = _layoutInfos.get(position)
            if (layoutInfo != null) return layoutInfo

            layoutInfo = LayoutInfo(position)
            _layoutInfos.put(position, layoutInfo)
            return layoutInfo
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            if (sections.size > 0) {
                val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams
                val position = layoutParams.viewLayoutPosition
                val info = getLayoutInfoAtPosition(position)
                if (orientation == RecyclerView.HORIZONTAL) {
                    outRect.set(info.top, info.left, info.bottom, info.right)
                } else {
                    outRect.set(info.left, info.top, info.right, info.bottom)
                }
            }
        }
    }
}