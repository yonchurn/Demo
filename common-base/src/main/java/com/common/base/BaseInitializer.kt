package com.common.base

import android.content.Context
import com.common.base.loading.LoadingView
import com.common.base.loading.PageLoadingView
import com.common.base.refresh.RefreshHeader

//该库 初始化器
object BaseInitializer {

    //页面加载类
    var pageLoadingViewCreator: ((context: Context) -> PageLoadingView)? = null
    var loadingViewCreator: ((context: Context) -> LoadingView)? = null

    //自定义下拉刷新头部 要实现 RefreshHeader
    var refreshHeaderCreator: ((context: Context) -> RefreshHeader)? = null
    var refreshHeaderRes: Int? = null
}

