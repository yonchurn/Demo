package com.common.base.api

/**
 * 可取消的
 */
interface HttpCancelable {

    //是否正在执行
    val isExecuting: Boolean

    //标识
    var name: String?

    //取消
    fun cancel()
}