package com.common.base.adapter

import android.view.View
import com.common.base.R

/**
 * 空视图适配器
 */
internal interface EmptyAdapter {

    //空视图 itemType
    var emptyType: Int

    //空视图下标
    var emptyPosition: Int

    //是否需要显示空视图
    var shouldDisplayEmptyView: Boolean

    //空视图高度 默认和容器一样高 负数
    fun getEmptyViewHeight(): Int {
        return -1
    }

    //空视图已显示
    fun onEmptyViewDisplay(emptyView: View) {

    }

    fun getEmptyViewRes(): Int {
        return R.layout.page_empty_view
    }
}