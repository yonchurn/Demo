package com.common.base.mvp

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.common.base.api.HttpCancelable
import com.common.base.api.HttpProcessor

/**
 * 基础 presenter
 * @param owner 持有者
 */
open class BasePresenter<T>(val owner: T): HttpProcessor {

    //http可取消的任务
    override var currentTasks: HashSet<HttpCancelable>? = null

    init {
        //监听生命周期
        if (owner is ComponentActivity) {
            owner.lifecycle.addObserver(this)
        } else if (owner is Fragment) {
            owner.lifecycle.addObserver(this)
        }
    }
}