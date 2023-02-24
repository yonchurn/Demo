package com.common.base.base.fragment

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import com.common.base.R
import com.common.base.app.ApiConfig
import com.common.base.base.interf.RefreshablePage
import com.common.base.refresh.RefreshHeader
import com.common.base.utils.SizeUtils
import com.common.base.widget.BackToTopButton
import com.scwang.smartrefresh.layout.SmartRefreshLayout

/**
 * 可下拉刷新的
 */
@Suppress("unused_parameter")
abstract class RefreshableFragment: BaseFragment(), RefreshablePage {

    override var curPage = ApiConfig.HttpFirstPage

    override var refreshing = false
    override var refreshHeader: RefreshHeader? = null
    override val hasRefresh: Boolean = false

    override var smartRefreshLayout: SmartRefreshLayout? = null

    //</editor-fold>

    //<editor-fold desc="回到顶部">

    //回到顶部按钮
    open var backToTopButton: BackToTopButton? = null
        get() {
            if (!shouldDisplayBackToTop) return null
            if (field == null) {
                field = BackToTopButton(requireContext())
                configureBackToTopButton(field!!)
            }
            return field
        }

    //是否需要显示回到顶部按钮
    open val shouldDisplayBackToTop = false

    //</editor-fold>

    //返回自定义的 layout res
    @LayoutRes
    open fun getRefreshableContentRes(): Int {
        return 0
    }
}