package com.common.base.widget

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.common.base.drawable.CornerBorderDrawable
import com.common.base.pager.CyclePagerAdapter


/**
 * 轮播广告点
 */
class PageControl: LinearLayout {

    //显示的点
    private val points: ArrayList<PageControlPoint> = ArrayList()
    var curPage = 0
        set(value) {
            var page = value
            if (viewPager != null && viewPager!!.adapter is CyclePagerAdapter) {
                val adapter = viewPager!!.adapter as CyclePagerAdapter?
                page = adapter!!.getRealPosition(page)
            }
            if (page < points.size) {
                val previousPage = field
                if (previousPage < points.size && previousPage >= 0) {
                    val point = points[previousPage]
                    if (normalRes != 0) {
                        point.setBackgroundResource(normalRes)
                    } else {
                        point.setDefaultDrawable()
                        point.drawable!!.backgroundColor = normalColor
                    }
                }
                field = page
                val point = points[page]
                if (selectedRes != 0) {
                    point.setBackgroundResource(selectedRes)
                } else {
                    point.setDefaultDrawable()
                    point.drawable!!.backgroundColor = selectedColor
                }
            }
        }

    //只有一个点时是否隐藏
    var hideForSingle = true
        set(value) {
            if (value != field) {
                field = value
                visibility = if (field && points.size <= 1) {
                    INVISIBLE
                } else {
                    VISIBLE
                }
            }
        }

    //关联的viewPager，如果已关联了viewPager，则不需要额外设置pageCount
    var viewPager: ViewPager? = null
        set(value) {
            if (value != field) {
                field = value
                field?.also {
                    //监听viewPager数据变化
                    it.addOnPageChangeListener(object : OnPageChangeListener {
                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {
                        }

                        override fun onPageSelected(position: Int) {
                            curPage = position
                        }

                        override fun onPageScrollStateChanged(state: Int) {}
                    })
                    it.addOnAdapterChangeListener { _, oldAdapter, newAdapter ->
                        if (newAdapter != null) {

                            //第一次设置
                            if (oldAdapter == null) {
                                setPageCount(newAdapter)
                            }
                            newAdapter.registerDataSetObserver(object : DataSetObserver() {
                                override fun onChanged() {
                                    setPageCount(newAdapter)
                                }

                                override fun onInvalidated() {
                                    super.onInvalidated()
                                    setPageCount(0)
                                }
                            })
                        }
                    }
                }
            }
        }

    //点大小
    var pointSizeDip = 5
        set(value) {
            if (value != field) {
                field = value
                //重新设置点大小
                val scale = context.resources.displayMetrics.density
                val size = (scale * field).toInt() // XP与DP转换，适应不同分辨率
                val margin = (scale * pointIntervalDip).toInt()
                for (i in 0 until points.size) {
                    val point = points[i]
                    val layoutParams = point.layoutParams as LayoutParams
                    layoutParams.setMargins(0, 0, margin, 0)
                    layoutParams.width = size
                    layoutParams.height = size
                    point.layoutParams = layoutParams
                }
            }
        }

    //点间隔
    var pointIntervalDip = 5
        set(value) {
            if (value != field) {
                field = value
                //重新设置点间隔
                val scale = context.resources.displayMetrics.density
                val margin = (scale * field).toInt()
                for (i in 0 until points.size) {
                    val point = points[i]
                    val layoutParams = point.layoutParams as LayoutParams
                    layoutParams.setMargins(0, 0, margin, 0)
                    point.layoutParams = layoutParams
                }
            }
        }

    //点颜色
    var normalColor: Int = Color.GRAY
        set(value) {
            if (value != field) {
                field = value
                for (i in 0 until points.size) {
                    if (i == curPage) continue
                    val point = points[i]
                    point.setDefaultDrawable()
                    point.drawable!!.backgroundColor = field
                }
            }
        }

    //点高亮颜色
    var selectedColor: Int = Color.RED
        set(value) {
            if (value != field) {
                field = value
                if (curPage < points.size) {
                    val point = points[curPage]
                    point.setDefaultDrawable()
                    point.drawable!!.backgroundColor = field
                }
            }
        }

    //点背景
    @DrawableRes
    var normalRes = 0
        set(value) {
            if (value != field) {
                field = value
                for (i in 0 until points.size) {
                    if (i == curPage) continue
                    val point = points[i]
                    point.setBackgroundResource(field)
                }
            }
        }

    //点高亮背景
    @DrawableRes
    var selectedRes = 0
        set(value) {
            if (value != field) {
                field = value
                if (curPage < points.size) {
                    val point = points[curPage]
                    point.setBackgroundResource(field)
                }
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        orientation = HORIZONTAL
    }

    //设置page总数
    fun setPageCount(pageCount: Int) {
        if (pageCount != points.size) {
            curPage = 0
            points.clear()
            removeAllViews()

            //创建点
            val context: Context = context
            val scale = context.resources.displayMetrics.density
            val size = (scale * pointSizeDip).toInt() // XP与DP转换，适应不同分辨率
            val margin = (scale * pointIntervalDip).toInt()
            for (i in 0 until pageCount) {
                val point = PageControlPoint(context)
                val layoutParams = LayoutParams(size, size)
                layoutParams.setMargins(0, 0, margin, 0)
                layoutParams.gravity = Gravity.CENTER_VERTICAL
                point.layoutParams = layoutParams
                if (normalRes != 0) {
                    point.setBackgroundResource(normalRes)
                } else {
                    point.setDefaultDrawable()
                    point.drawable!!.backgroundColor = normalColor
                }
                addView(point)
                points.add(point)
            }
            if (viewPager != null) {
                curPage = viewPager!!.currentItem
            }
            visibility = if (hideForSingle && pageCount <= 1) {
                INVISIBLE
            } else {
                VISIBLE
            }
        }
    }

    //从adapter中获取数量
    private fun setPageCount(pagerAdapter: PagerAdapter) {
        if (pagerAdapter is CyclePagerAdapter) {
            setPageCount(pagerAdapter.realCount)
        } else {
            setPageCount(pagerAdapter.count)
        }
    }

    private class PageControlPoint(context: Context?) : View(context) {
        var drawable: CornerBorderDrawable? = null
        fun setDefaultDrawable() {
            if (drawable == null) {
                drawable = CornerBorderDrawable()
                drawable!!.shouldAbsoluteCircle = true
                background = drawable
            }
        }
    }
}