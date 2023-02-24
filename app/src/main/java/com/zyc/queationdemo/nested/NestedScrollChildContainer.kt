package com.zyc.queationdemo.nested

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.common.base.base.activity.BaseActivity
import com.common.base.nested.NestedScrollHelper
import com.zyc.queationdemo.R

class NestedScrollChildContainer: LinearLayout {

    val viewPager: ViewPager2 by lazy { findViewById(R.id.view_pager) }
    var onScrollListener: RecyclerView.OnScrollListener? = null
        set(value) {
            field = value
            for (fragment in fragments) {
                fragment.onScrollListener = value
            }
        }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var nestedScrollHelper: NestedScrollHelper? = null

    val fragments = arrayOf(
        NestedScrollFragment(),
        NestedScrollFragment(),
        NestedScrollFragment(),
        NestedScrollFragment(),
        NestedScrollFragment())

    val currentFragment: NestedScrollFragment
        get() = fragments[viewPager.currentItem]

    override fun onFinishInflate() {
        super.onFinishInflate()
        
        val activity = context as BaseActivity
        viewPager.adapter = object: FragmentStateAdapter(activity) {
            override fun getItemCount(): Int {
                return fragments.size
            }

            override fun createFragment(position: Int): Fragment {
                val fragment = fragments[position]
                fragment.onScrollListener = onScrollListener
                fragment.nestedScrollHelper = nestedScrollHelper
                return fragment
            }
        }
    }
}