package com.common.base.loading

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.common.base.base.constant.PageStatus
import com.common.base.base.interf.VoidCallback

/**
 * 页面加载接口
 */
abstract class PageLoadingView: FrameLayout {

    /**
     * 当前状态
     */
    open var status: PageStatus = PageStatus.NORMAL

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * 刷新回到
     */
    var reloadCallback: VoidCallback? = null
}