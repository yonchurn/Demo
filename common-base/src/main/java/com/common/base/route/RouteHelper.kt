package com.common.base.route

import android.app.Activity
import com.common.base.base.activity.ActivityLifeCycleManager
import com.common.base.base.activity.BaseActivity

//路由帮助类
object RouteHelper {

    //存在activity extras 里面的路由 path
    const val PATH = "com.common.base.routePath"

    //当前的activity
    private val activities: ArrayList<Activity>
        get() = ActivityLifeCycleManager.activities

    //关闭对应路由的界面
    fun close(path: String) {
        for (activity in activities) {
            if (activity is BaseActivity && path == activity.routePath) {
                activity.finish()
                break
            }
        }
    }

    fun close(paths: Set<String>) {
        val closeActivities: MutableSet<Activity> = HashSet()
        for (activity in activities) {
            if (activity is BaseActivity && paths.contains(activity.routePath)) {
                closeActivities.add(activity)
            }
        }
        val iterator = closeActivities.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            activity.finish()
        }
    }

    /**
     * 返回到某个路由界面
     * @param include 是否包含toName
     * @param resultCode [android.app.Activity.setResult]
     */
    fun backTo(path: String, include: Boolean = false, resultCode: Int = Int.MAX_VALUE) {
        var index = -1
        for (i in activities.size - 1 downTo 0) {
            val activity = activities[i]
            if (activity is BaseActivity && activity.routePath == path) {
                index = i
                break
            }
        }
        if (index != -1) { //要关闭的activity
            val closeActivities: MutableSet<Activity> = HashSet()
            if (!include) {
                index++
            }
            for (i in index until activities.size) {
                closeActivities.add(activities[i])
            }
            val iterator = closeActivities.iterator()
            while (iterator.hasNext()) {
                val activity = iterator.next()
                if (resultCode != Int.MAX_VALUE) {
                    activity.setResult(resultCode)
                }
                activity.finish()
            }
        }
    }
}