package com.common.base.base.constant

//覆盖区域
object OverlayArea {

    //页面加载中时，将覆盖顶部视图
    const val PAGE_LOADING_TOP = 1

    //页面加载中时，将覆盖底部视图
    const val PAGE_LOADING_BOTTOM = 1.shl(1)

    //空视图将覆盖顶部视图
    const val EMPTY_TOP = 1.shl(2)

    //空视图将覆盖底部视图
    const val EMPTY_BOTTOM = 1.shl(3)
}