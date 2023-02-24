package com.common.base.refresh

/**
 * 加载更多状态
 */
enum class LoadMoreStatus {

    NORMAL, //什么都没

    HAS_MORE, //可以加载更多数据

    LOADING, //加载中

    FAIL, //加载失败 点击可加载

    NO_MORE_DATA, //没有数据了
}