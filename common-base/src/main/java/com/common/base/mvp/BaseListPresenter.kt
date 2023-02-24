package com.common.base.mvp

import com.common.base.app.ApiConfig

/**
 * 列表相关
 */
open class BaseListPresenter<T>(owner: T): BasePresenter<T>(owner) {

    //页码
    var curPage = ApiConfig.HttpFirstPage
}