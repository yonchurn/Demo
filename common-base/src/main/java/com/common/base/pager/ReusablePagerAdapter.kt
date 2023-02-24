package com.common.base.pager

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.common.base.R


/**
 * 可复用item的PagerAdapter
 */
@Suppress("unused_parameter")
abstract class ReusablePagerAdapter(
    //关联的viewPager
    protected val viewPager: ViewPager
) : PagerAdapter() {


    //当前显示出来的view
    private val visibleViews: SparseArray<HashSet<View>> = SparseArray()

    //可重用的view
    private val reusedViews: SparseArray<HashSet<View>> = SparseArray()

    //当前显示出来的view中的 subview
    private val visibleSubviews: SparseArray<HashSet<View>> = SparseArray()

    //可重用的view中的 subview
    private val reusedSubviews: SparseArray<HashSet<View>> = SparseArray()

    //是否有子视图
    var existSubview = false


    override fun isViewFromObject(view: View, obj : Any): Boolean {
        return view === obj
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var type = getViewType(position)

        //获取可重用的view
        var views = reusedViews[type]
        var convertView: View? = null

        if (views != null && views.isNotEmpty()) {
            val iterator: Iterator<View?> = views.iterator()
            convertView = iterator.next()
            views.remove(convertView)

            //必须加入，否则重用时会导致某些视图 刷新延迟
            container.addView(convertView)
        }

        val realPosition = getRealPosition(position)
        val view = instantiateItemForRealPosition(convertView, realPosition, type)
        if (view is View) { //加入可见队列
            views = visibleViews[type]

            if (views == null) {
                views = HashSet()
                visibleViews.put(type, views)
            }


            view.setTag(R.id.view_pager_position_tag_key, position)
            if (view.parent == null) {
                container.addView(view)
            }
            views.add(view)

            //获取可重用的子视图
            if (existSubview && view is ViewGroup) {

                val count = numberOfSubviewInPage(realPosition)
                for (i in 0 until count) {
                    type = getSubviewType(realPosition, i)

                    //获取重用的subview
                    var subConvertView: View? = null
                    var subviews = reusedSubviews[type]

                    if (subviews != null && subviews.isNotEmpty()) {
                        val iterator: Iterator<View?> = subviews.iterator()
                        subConvertView = iterator.next()
                        subviews.remove(subConvertView)
                    }
                    val subview = getSubview(subConvertView, realPosition, i, type)
                        ?: throw NullPointerException("$TAG getSubview 不能返回 null")

                    //加入可见队列
                    subviews = visibleSubviews[type]
                    if (subviews == null) {
                        subviews = HashSet()
                        visibleSubviews.put(type, subviews)
                    }
                    view.addView(subview)
                    subviews.add(subview)
                }
            }
        }
        return view
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {

        if (obj is View) {
            var type = getViewType(position)

            //移出可见队列
            var views = visibleViews[type]
            views!!.remove(obj)

            //加入重用队列
            views = reusedSubviews[type]
            if (views == null) {
                views = HashSet()
                reusedSubviews.put(type, views)
            }

            views.add(obj)
            container.removeView(obj)

            val realPosition = getRealPosition(position)
            destroyItemForRealPosition(obj, realPosition, type, obj)

            //子视图
            if (existSubview && obj is ViewGroup) {
                val count = numberOfSubviewInPage(realPosition)
                for (i in 0 until count) {
                    type = getSubviewType(realPosition, i)
                    val subview: View = obj.getChildAt(i)

                    //移出可见队列
                    var subviews = visibleSubviews[type]
                    subviews!!.remove(obj)

                    //加入重用队列
                    subviews = reusedSubviews[type]
                    if (subviews == null) {
                        subviews = HashSet()
                        reusedSubviews.put(type, subviews)
                    }
                    subviews.add(subview)
                }
                obj.removeAllViews()
            }
        }
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()

        //viewPager 本身是不会刷新的，要手动刷新可见视图
        val count = count
        for (i in 0 until visibleViews.size()) {
            val viewHashSet = visibleViews[visibleViews.keyAt(i)]
            val iterator: Iterator<View?> = viewHashSet.iterator()
            while (iterator.hasNext()) {
                val view = iterator.next()
                val position = view!!.getTag(R.id.view_pager_position_tag_key) as Int

                if (position < count) {
                    instantiateItemForRealPosition(view, getRealPosition(position), getViewType(position))
                }
            }
        }
    }

    //移动到第一个位置
    protected open fun scrollToFirstPosition() {
        if (count > 0) {
            viewPager.setCurrentItem(0, false)
        }
    }

    //获取当前已预载的view
    val reloadingViews: HashSet<View>
        get() {
            val views: HashSet<View> = HashSet()
            for (i in 0 until visibleViews.size()) {
                val viewHashSet = visibleViews[visibleViews.keyAt(i)]
                views.addAll(viewHashSet)
            }
            return views
        }

    //获取对应position的view，如果不存在，则返回null
    fun getViewForPosition(position: Int): View? {
        for (i in 0 until visibleViews.size()) {
            val viewHashSet= visibleViews[visibleViews.keyAt(i)]
            val iterator: Iterator<View?> = viewHashSet.iterator()
            while (iterator.hasNext()) {
                val view= iterator.next()
                val tag = view!!.getTag(R.id.view_pager_position_tag_key) as Int
                if (getRealPosition(tag) == position) return view
            }
        }
        return null
    }

    //获取当前显示的view
    val currentView: View?
        get() = getViewForPosition(viewPager.currentItem)

    /**
     * 通过viewPager 位置获取真实的数据源位置
     * @param position viewPager 位置
     * @return 真实的数据源位置
     */
    open fun getRealPosition(position: Int): Int {
        return position
    }

    /**
     * 显示真实的页面
     * @param convertView 和listView中的一样 如果不为空，则该视图可重用
     * @param position [.instantiateItem]
     * @param viewType 和listView中的一样 视图类型
     * @return [.instantiateItem]
     */
    abstract fun instantiateItemForRealPosition(
        convertView: View?,
        position: Int,
        viewType: Int
    ): Any

    /**
     * 销毁真实的页面
     * @param convertView 需要销毁的view，该view会进入重用队列
     * @param position [.destroyItem]
     * @param viewType 和listView中的一样 视图类型
     * @param object [.destroyItem]
     */

    fun destroyItemForRealPosition(
        convertView: View?,
        position: Int,
        viewType: Int,
        `object`: Any?
    ) {
    }

    /**
     * 获取view类型，用于识别重用
     * @param position 视图位置
     * @return view类型
     */
    open fun getViewType(position: Int): Int {
        return 0
    }

    /**
     * 获取subview类型，用于识别重用
     * @param position 当前页位置
     * @param subviewPosition 子视图在当前页的位置
     * @return subview类型
     */
    open fun getSubviewType(position: Int, subviewPosition: Int): Int {
        return 0
    }

    /**
     * 当前页有多少个subview
     * @param position 当前页
     * @return 子视图数量
     */
    open fun numberOfSubviewInPage(position: Int): Int {
        return 0
    }

    /**
     * 显示子视图
     * @param convertView 和listView中的一样 如果不为空，则该视图可重用
     * @param position 当前页位置
     * @param subviewPosition 子视图在当前页的位置
     * @param subviewType 子视图类型
     * @return 子视图
     */
    open fun getSubview(
        convertView: View?,
        position: Int,
        subviewPosition: Int,
        subviewType: Int
    ): View? {
        return null
    }

    companion object {
        const val TAG = "ReusablePagerAdapter"
    }

    init {

        viewPager.addOnAdapterChangeListener{ _, _, _ ->

            scrollToFirstPosition()
        }
    }
}