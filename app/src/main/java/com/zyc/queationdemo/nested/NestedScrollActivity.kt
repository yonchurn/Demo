package com.zyc.queationdemo.nested

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.common.base.adapter.ItemType
import com.common.base.adapter.RecyclerViewAdapter
import com.common.base.base.activity.RecyclerActivity
import com.common.base.base.widget.BaseContainer
import com.common.base.extension.gone
import com.common.base.extension.visible
import com.common.base.nested.NestedParentLinearLayoutManager
import com.common.base.nested.NestedParentRecyclerView
import com.common.base.nested.NestedScrollHelper
import com.common.base.viewholder.RecyclerViewHolder
import com.zyc.queationdemo.R
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

//嵌套滑动
class NestedScrollActivity: RecyclerActivity() {

    override val hasRefresh: Boolean
        get() = false

    val parentRecyclerView: NestedParentRecyclerView
        get() = recyclerView as NestedParentRecyclerView

    val magicIndicator: MagicIndicator by lazy { findViewById(R.id.magic_indicator) }

    val topContainer: LinearLayout by lazy { findViewById(R.id.top_container) }

    val industryRole: View by lazy { findViewById(R.id.industry_role) }
    val maxOffset by lazy { -pxFromDip(40f) }
    val indicatorHeight by lazy { pxFromDip(40f) }
    var totalOffset = 0

    var viewPager: ViewPager2? = null

    private val helper by lazy {
        NestedScrollHelper(parentRecyclerView) {
            val container = childContainer
            if (container != null) {
                val fragment = container.currentFragment
                if (fragment.isInit) fragment.childRecyclerView else null
            } else {
                null
            }
    } }

    val titles = arrayOf("水果生鲜", "休闲零食", "男装女装", "日用百货", "母婴用品")

    val onScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (industryRole.isVisible) {
                totalOffset -= dy
                if (totalOffset > 0) totalOffset = 0
                if (totalOffset < maxOffset) totalOffset = maxOffset
            }

            changeViewOffset()
        }
    }

    //子视图回到顶部，防止左右滑动切换时不一致
    fun childrenScrollToTopIfNeeded() {
        val container = childContainer
        if (container != null) {
            for (fragment in container.fragments) {
                fragment.scrollToTopIfNeeded()
            }
        }
    }

    fun changeViewOffset() {
        topContainer.translationY = totalOffset.toFloat()
        val count = parentRecyclerView.childCount
        if (count > 0) {
            val last = parentRecyclerView.getChildAt(count - 1)
            val value = if (industryRole.isVisible) totalOffset - maxOffset + indicatorHeight else indicatorHeight
            if (last.top <= value
                && parentRecyclerView.getChildAdapterPosition(last!!) == parentRecyclerView.adapter!!.itemCount - 1) {
                magicIndicator.visible()
            } else {
                if (magicIndicator.isVisible) {
                    magicIndicator.gone()
                    childrenScrollToTopIfNeeded()
                }
            }
            val container = childContainer
            if (container != null) {
                var offset = 0
                if (magicIndicator.isVisible && industryRole.isVisible) {
                    offset = totalOffset - maxOffset - (last.top - indicatorHeight)
                }
                for (fragment in container.fragments) {
                    fragment.offset = offset.toFloat()
                }
            }
        } else {
            magicIndicator.gone()
        }
    }

    override fun initialize(
        inflater: LayoutInflater,
        container: BaseContainer,
        saveInstanceState: Bundle?
    ) {
        super.initialize(inflater, container, saveInstanceState)
        setBarTitle("嵌套滑动")
        val layoutManager = NestedParentLinearLayoutManager(this)
        layoutManager.nestedScrollHelper = helper

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = Adapter(recyclerView)

        //关闭默认动画
        recyclerView.itemAnimator = null

        //到底部就 不让下拉刷新了，不然滑动 indicator 的时候会触发
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val parent = recyclerView as NestedParentRecyclerView
                    smartRefreshLayout?.setEnableRefresh(!parent.isScrollEnd)
                }
            }
        })

        recyclerView.addOnScrollListener(onScrollListener)

        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return titles.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val colorTransitionPagerTitleView = ColorTransitionPagerTitleView(context)
                colorTransitionPagerTitleView.normalColor = Color.GRAY
                colorTransitionPagerTitleView.selectedColor = Color.BLACK
                colorTransitionPagerTitleView.text = titles[index]
                colorTransitionPagerTitleView.setOnClickListener { viewPager?.currentItem = index }
                return colorTransitionPagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
                return indicator
            }
        }
        magicIndicator.navigator = commonNavigator
    }

    override fun onRefresh() {
        baseContainer?.postDelayed({
            stopRefresh()
        }, 1000)
    }

    private var childContainer: NestedScrollChildContainer? = null

    override fun getRefreshableContentRes(): Int {
        return R.layout.nested_scroll_activity
    }

    inner class Adapter(recyclerView: RecyclerView) : RecyclerViewAdapter(recyclerView) {

        override fun onCreateViewHolder(viewType: Int, parent: ViewGroup): RecyclerViewHolder {
            return RecyclerViewHolder(LayoutInflater.from(context).inflate(viewType, parent, false))
        }

        override fun onCreateHeaderViewHolder(
            viewType: Int,
            parent: ViewGroup
        ): RecyclerViewHolder {
            val view = View(context)
            view.layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, pxFromDip(40f))
            return RecyclerViewHolder(view)
        }

        override fun onCreateFooterViewHolder(
            viewType: Int,
            parent: ViewGroup
        ): RecyclerViewHolder {
            val holder = RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.nested_scroll_item, parent, false))
            holder.itemView.layoutParams.height = parent.measuredHeight - indicatorHeight
            return holder
        }

        override fun getItemViewType(position: Int, section: Int, type: ItemType): Int {
            return R.layout.layout_item
        }

        override fun numberOfItems(section: Int): Int {
            return 10
        }

        override fun shouldExistHeader(): Boolean {
            return industryRole.isVisible
        }

        override fun shouldExistFooter(): Boolean {
            return true
        }

        override fun onBindItemViewHolder(
            viewHolder: RecyclerViewHolder,
            position: Int,
            section: Int
        ) {
            viewHolder.getView<TextView>(R.id.textView).text = "Item $position"
        }

        override fun onBindFooterViewHolder(viewHolder: RecyclerViewHolder) {
            val item = viewHolder.itemView as NestedScrollItem
            if (childContainer == null) {
                val container = LayoutInflater.from(context).inflate(R.layout.nested_scroll_child_container, null) as NestedScrollChildContainer
                container.nestedScrollHelper = helper
                container.onScrollListener = onScrollListener
                viewPager = container.viewPager
                viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    var currentPosition = 0
                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        magicIndicator.onPageScrollStateChanged(state)
                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
                            val position = viewPager?.currentItem ?: 0
                            if (position != currentPosition) {
                                currentPosition = position
//                                if (position % 2 == 0) {
//                                    if (!industryRole.isVisible) {
//                                        industryRole.visible()
//                                        notifyItemInserted(0)
//                                    }
////                                    (parentRecyclerView.layoutManager as LinearLayoutManager)
////                                        .scrollToPositionWithOffset(itemCount - 1, pxFromDip(80f))
//                                } else {
//                                    if (industryRole.isVisible) {
//                                        industryRole.gone()
//                                        notifyItemRemoved(0)
//                                    }
//                                    (parentRecyclerView.layoutManager as LinearLayoutManager)
//                                        .scrollToPositionWithOffset(itemCount - 1, 0)
//                                }
                                rollingChildToTop()
                                totalOffset = 0
                                changeViewOffset()
                                magicIndicator.visible()
                            }
                        }
                    }

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    }

                    override fun onPageSelected(position: Int) {
                        magicIndicator.onPageSelected(position)

                    }
                })

                childContainer = container
            }
            item.setView(childContainer!!)
        }

        override fun onItemClick(position: Int, section: Int, item: View) {
            println("click parent")
        }
    }

    /**
     * 滚动子列表置顶
     * */
    fun rollingChildToTop(){
        if (parentRecyclerView.adapter != null){

            if (industryRole.isVisible) {
                industryRole.gone()
                parentRecyclerView.adapter!!.notifyItemRemoved(0)
            }

            (parentRecyclerView.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(parentRecyclerView.adapter!!.itemCount - 1, 0)

            if (industryRole.isVisible) {
                totalOffset = maxOffset
            } else {
                totalOffset = 0
            }
            changeViewOffset()
            magicIndicator.visible()
        }
    }
}