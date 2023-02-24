package com.common.base.app

import androidx.multidex.MultiDexApplication
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.common.base.BuildConfig
import com.common.base.base.activity.ActivityLifeCycleManager
import java.util.*
import kotlin.system.exitProcess

/**
 * 基础app
 */
open class BaseApplication: MultiDexApplication() {

    init {
        //禁止app闪退后恢复，必须放在这里，否则会覆盖掉firebase的crash收集
        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { _, _ -> //闪退后不让恢复
                ActivityLifeCycleManager.finishAllActivities()
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(1)
            }
        }
    }

    companion object {
        lateinit var sharedApplication: BaseApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Rangoon"))

        sharedApplication = this
        if (BuildConfig.DEBUG) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog()     // 打印日志
            ARouter.openDebug()   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        ARouter.init(this) // 尽可能早，推荐在Application中初始化

        //初始化activity声明周期管理
        registerActivityLifecycleCallbacks(ActivityLifeCycleManager)
    }

    //低内存处理
    override fun onLowMemory() {
        super.onLowMemory()
        Glide.get(this).clearMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory()
        }
    }
}