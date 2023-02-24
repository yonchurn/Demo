package com.common.base.utils

import android.os.Handler
import android.os.Looper


/**
 * 线程工具类
 */
object ThreadUtils {

    /**
     * 在主线程上执行
     * @param runnable 要执行的、、
     */
    fun runOnMainThread(runnable: Runnable) {
        if (isRunOnMainThread()) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    /**
     * 是否在主线程上
     * @return 是否
     */
    fun isRunOnMainThread(): Boolean {
        return Thread.currentThread().id == Looper.getMainLooper().thread.id
    }


    //主线程handler
    private val mainHandler
        get() = MainHandlerHolder.handler
}

private object MainHandlerHolder {
    val handler = Handler(Looper.getMainLooper())
}