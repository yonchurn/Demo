package com.common.base.utils


import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.DiskCache
import com.common.base.base.activity.ActivityLifeCycleManager
import com.common.base.base.interf.ValueCallback
import java.io.*

/**
 * 缓存工具类
 */

@Suppress("unchecked_cast")
object CacheUtils {

    private val context: Context
        get() = ActivityLifeCycleManager.currentContext

    // 删除缓存目录
    fun deleteCacheFolder(context: Context, runnable: Runnable?) {

        val glide = Glide.get(context)
        glide.clearMemory()
        Thread {
            glide.clearDiskCache()
            if (runnable != null) {
                ThreadUtils.runOnMainThread(runnable)
            }
        }.start()


    }

    // 获取缓存大小
    fun getCacheSize(context: Context, callback: ValueCallback<String>) {
        Thread {
            val folder = File(context.cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR)
            val size = FileUtils.formatBytes(FileUtils.getFileSize(folder))
            ThreadUtils.runOnMainThread {
                callback(size)
            }
        }.start()
    }
}