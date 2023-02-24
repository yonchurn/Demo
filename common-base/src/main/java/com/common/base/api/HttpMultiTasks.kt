package com.common.base.api

import com.common.base.base.interf.ValueCallback
import com.common.base.utils.ThreadUtils
import java.lang.IllegalArgumentException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 多任务处理
 */
class HttpMultiTasks: HttpTask.Callback, HttpCancelable {

    //当有一个任务失败时，是否取消所有任务
    var shouldCancelAllTaskWhileOneFail = true

    //是否只标记网络错误
    var onlyFlagNetworkError = false

    //所有任务完成回调 hasFail 是否有任务失败了
    private var completions = HashSet<ValueCallback<HttpMultiTasks>>()

    //任务列表
    private var tasks = ArrayList<HttpTask>()

    //是否有请求失败
    var hasFail = false
        private set

    //对应任务
    private var taskMap = HashMap<String, HttpTask>()

    //是否正在执行
    private val _isExecuting = AtomicBoolean(false)
    override val isExecuting: Boolean
        get() = _isExecuting.get()

    //添加任务 key 为HttpTask.name
    fun addTask(task: HttpTask, key: String = task.name ?: "") {
        require(!isExecuting){
            "HttpMultiTasks is executing"
        }
        tasks.add(task)
        taskMap[key] = task
        task.callback = this
    }

    //添加回调
    fun addCompletionHandler(callback: ValueCallback<HttpMultiTasks>){
        completions.add(callback)
    }

    //移除回调
    fun removeCompletionHandler(callback: ValueCallback<HttpMultiTasks>){
        completions.remove(callback)
    }

    //开始所有任务
    fun start() {
        if (!_isExecuting.compareAndSet(false, true)) {
            throw IllegalArgumentException("HttpMultiTasks is executing")
        }

        require(tasks.size > 0){
            "HttpMultiTasks has no task"
        }

        for(task in tasks){
            task.start()
        }
    }

    //取消所有请求
    fun cancelAllTasks() {
        if (_isExecuting.compareAndSet(true, false)) {
            for(task in tasks){
                task.cancel()
            }
            tasks.clear()
            taskMap.clear()
        }
    }

    override fun cancel() {
        cancelAllTasks()
    }

    override
    var name: String? = null
        get() {
            return if (field == null) this.javaClass.name else field
        }

    //获取某个请求
    fun taskForKey(key: String): HttpTask {
        val task = taskMap[key]
        require(task != null){
            "taskForKey $key does not exist"
        }
        return task
    }

    @Suppress("unchecked_cast")
    fun <T: HttpTask> taskForKey(clazz: Class<T>): T {
        val key = clazz.name
        val task = taskMap[key]
        require(task != null){
            "taskForKey $key does not exist"
        }
        return task as T
    }

    override fun onFailure(task: HttpTask) {}

    override fun onSuccess(task: HttpTask) {}

    override fun onComplete(task: HttpTask) {
        ThreadUtils.runOnMainThread {
            if (!task.isCancelled) {
                tasks.remove(task)
                val success = task.isApiSuccess || (!task.isNetworkError && onlyFlagNetworkError)
                if(!success){
                    hasFail = true
                    if(shouldCancelAllTaskWhileOneFail){
                        for(tTask in tasks){
                            tTask.cancel()
                        }
                        tasks.clear()
                    }
                }

                if(tasks.size == 0){
                    if (_isExecuting.compareAndSet(true, false)) {
                        for(completion in completions){
                            completion(this)
                        }
                    }
                    taskMap.clear()
                }
            }
        }
    }
}