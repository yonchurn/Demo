package com.common.base.app

import android.os.Bundle
import com.common.base.base.activity.ActivityLifeCycleManager
import com.common.base.base.activity.BaseActivity
import com.common.base.language.LanguageHelper

/**
 * 日志上传接口
 */
interface LogEventWorker {

    //上传当前屏幕
    fun reportScreenName(screenName: String, screenClass: String)

    //上传事件
    fun logEvent(name: String, parameters: Bundle?)
}

/**
 * 日志事件帮助类
 */
object LogEventHelper {

    //日志处理
    private var worker: LogEventWorker? = null

    //初始化
    fun initWorker(worker: LogEventWorker) {
        this.worker = worker
    }

    //公共参数
    val commonParameters: Bundle
        get() {
            val bundle = Bundle()
            bundle.putString("语言", LanguageHelper.currentLanguageToCommon)
            return bundle
        }

    //上传当前屏幕 空不传
    fun reportScreenName(screenName: String?, screenClass: String?) {
        if (screenName != null && screenClass != null) {
            worker?.reportScreenName(screenName, screenClass)
        }
    }

    //上传事件
    fun logEvent(name: String, parameters: Bundle?) {
        worker?.logEvent(name, parameters)
    }

    val beforeScreenName: String
        get() {
            val context = ActivityLifeCycleManager.beforeContext
            if (context is BaseActivity) return context.screenName ?: ""
            return ""
        }
}