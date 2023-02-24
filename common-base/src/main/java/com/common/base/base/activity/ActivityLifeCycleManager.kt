package com.common.base.base.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.common.base.app.BaseApplication
import com.common.base.event.AppEvent
import com.common.base.extension.lastSafely
import org.greenrobot.eventbus.EventBus

/**
 * activity 堆栈
 */
object ActivityLifeCycleManager: Application.ActivityLifecycleCallbacks {

    //当前的activity
    val activities: ArrayList<Activity> = ArrayList()

    //当前创建activity数量
    val count: Int
        get() = activities.size

    //当前显示activity的数量
    private var activityCount = 0

    val currentActivity: Activity?
        get() {
            var activity = activities.lastSafely()
            if (activity != null && (activity.isFinishing || activity.isDestroyed)) {
                if (activities.size > 1) {
                    activity = activities[activities.size - 2]
                } else {
                    activity = null
                }
            }
            return activity
        }

    val currentContext: Context
        get() = currentActivity ?: BaseApplication.sharedApplication

    //前一个
    val beforeContext: Context
        get() {
            return if (activities.size >= 2) {
                activities[activities.size - 2]
            } else {
                currentContext
            }
        }

    /**
     * 获取对应名称的 activity
     * @param name 名称
     * @return activity
     */
    fun getActivity(name: String?): BaseActivity? {
        if (name != null) {
            for (activity in activities) {
                if (activity is BaseActivity && name == activity.name) {
                    return activity
                }
            }
        }
        return null
    }

    /**
     * 关闭所有activity到 root
     */
    fun finishActivitiesToRoot() { //要关闭的activity
        val closeActivities: MutableSet<Activity> = HashSet()
        for (activity in activities) {
            if (!activity.isTaskRoot) {
                closeActivities.add(activity)
            }
        }
        val iterator: Iterator<Activity> = closeActivities.iterator()
        while (iterator.hasNext()) {
            val activity = iterator.next()
            activity.finish()
        }
    }

    /**
     * 关闭多个activity
     * @param toName 在这个activity名称之后的都关闭
     * @param include 是否包含toName
     * @param resultCode [android.app.Activity.setResult]
     */
    fun finishActivities(toName: String, include: Boolean = false, resultCode: Int = Int.MAX_VALUE) {
        var index = -1
        for (i in activities.size - 1 downTo 0) {
            val activity = activities[i]
            if (activity is BaseActivity && toName == activity.name) {
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
            val iterator: Iterator<Activity> = closeActivities.iterator()
            while (iterator.hasNext()) {
                val activity = iterator.next()
                if (resultCode != Int.MAX_VALUE) {
                    activity.setResult(resultCode)
                }
                activity.finish()
            }
        }
    }

    /**
     * 销毁所有activity
     */
    fun finishAllActivities() {
        for (activity in activities) {
            activity.finish()
        }
    }

    /**
     * 启动的activity名称，主要是用来判断app是否是正常启动的，如果不是 重启app。
     * 防止因为设置中的权限改变导致activity恢复
     */
    private var launchActivityName: String? = null
    private var restartDisable = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
       if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O){
           when(activity.javaClass.simpleName){ //解决8.0系统，设置透明主题的activity不能设置屏幕方向的问题
               "ImageMojitoActivity", "ImagePickerCameraEmptyActivity" -> {}
               else -> {
                   activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
               }
           }
       }else{
           activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
       }

        activities.add(activity)
        restartIfNeeded(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        if(activityCount == 0){
            //app进入前台
            EventBus.getDefault()
                .post(AppEvent(AppEvent.Type.ENTER_FOREGROUND,this))
        }
        activityCount ++
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        activityCount --
        if(activityCount == 0){
            //app进入后台
            EventBus.getDefault()
                .post(AppEvent(AppEvent.Type.ENTER_BACKGROUND, this))
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activities.remove(activity)
    }

    //是否是启动
    fun isLaunch(activity: Activity): Boolean {
        if (activities.size > 1) return false

        getLaunchActivityNameIfNeeded(activity)

        val name = activity.javaClass.canonicalName
        if (TextUtils.isEmpty(name))
            return false

        if (launchActivityName != null && name.equals(name, true)) {
            return true
        }

        return false
    }

    //判断是否需要重启
    private fun restartIfNeeded(activity: Activity) {
        if (restartDisable) return

        getLaunchActivityNameIfNeeded(activity)

        //重新恢复activity时 savedInstanceState 一定不是空的
        if (TextUtils.isEmpty(launchActivityName))
            return

        val name = activity.javaClass.canonicalName
        if (TextUtils.isEmpty(name))
            return

        if (name.equals(launchActivityName, true)) {
            restartDisable = true //是正常启动的，通过 launch activity 启动的
        }

        if (!restartDisable) {
            activities.clear()
            activityCount = 0

            //重启
            val intent = Intent()
            intent.setClassName(activity, launchActivityName!!)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    private fun getLaunchActivityNameIfNeeded(activity: Activity) {
        if (launchActivityName == null) {
            launchActivityName = getLaunchActivityName(activity.application)
        }
    }

    //获取启动的activity名称
    @SuppressLint("QueryPermissionsNeeded")
    private fun getLaunchActivityName(application: Application): String {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setPackage(application.packageName)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = application.packageManager.queryIntentActivities(intent, 0)
        if (resolveInfos.isNotEmpty()) {
            return resolveInfos.first().activityInfo.name
        }
        return ""
    }

}