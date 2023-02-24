package com.common.base.api

import android.util.Log.INFO
import androidx.annotation.CallSuper
import com.alibaba.fastjson.JSONObject
import com.common.base.base.interf.ValueCallback
import com.common.base.utils.ThreadUtils
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


/**
 * http 任务
 */
@Suppress("unused_parameter", "unchecked_cast")
abstract class HttpTask : Callback, HttpCancelable {

    companion object {

        private val sharedHttpClient: OkHttpClient by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
//            OkHttpClient.Builder()
//                .connectTimeout(15, TimeUnit.SECONDS)
//                .callTimeout(15, TimeUnit.SECONDS)
//                .readTimeout(15, TimeUnit.SECONDS)
//                .writeTimeout(15, TimeUnit.SECONDS)
//                .build()

            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .apply {

                    addInterceptor(
                        LoggingInterceptor.Builder()
                        .setLevel(Level.BASIC)
                        .log(INFO)
                        .tag("okhttp")
                        .build())
                }
                .build()
        }
    }

    //状态
    private enum class Status {

        //准备中
        PREPARING,

        //执行中
        EXECUTING,

        //已取消
        CANCELLED,

        //成功
        SUCCESSFUL,

        //失败
        FAILURE,
    }

    //请求方法
    enum class HttpMethod {

        GET,
        POST,
    }

    //内容类型
    enum class ContentType {

        URL_ENCODED,
        MULTI_PART_FORM_DATA,
        JSON,
    }

    //请求URL
    abstract val currentURL: String

    //请求参数
    abstract fun getParameters(): HashMap<String, Any>?

    //请求头
    open val headers: HashMap<String, String>? = null

    //请求方法
    open val httpMethod = HttpMethod.GET

    //类型
    open val contentType = ContentType.JSON

    //请求名称 用来识别是哪个请求，返回值一定不是空的
    override
    var name: String? = null
        get() {
            return if (field == null) this.javaClass.name else field
        }

    //当前状态
    private val status = AtomicReference(Status.PREPARING)

    //当前call
    private var _call: Call? = null

    //是否需要使用新的httpClient 构建，getHttpClient, 当不是使用默认配置的时候可以设置成true 比如超时时间
    var shouldUseNewBuilder = false

    //是否是网络错误
    var isNetworkError = false
        protected set

    //api是否请求成
    var isApiSuccess = false
        protected set

    //数据是否请解析失败
    var isDataParseFail = false
        protected set

    //下面3个回调保证在主线程
    //回调
    var callback: Callback? = null

    //成功
    var onSuccess: ValueCallback<HttpTask>? = null

    //失败
    var onFailure: ValueCallback<HttpTask>? = null

    //进度 只有上传文件才有，不保证在主线程
    var onProgressChange: ValueCallback<Double>? = null

    //请求参数
    private fun getRequestBody(): RequestBody? {
        val params = getParameters()

        if (params.isNullOrEmpty()) return null

        return when (contentType) {
            ContentType.URL_ENCODED -> {
                val builder = FormBody.Builder()

                params.forEach {
                    builder.add(it.key, it.value.toString())
                }
                builder.build()
            }
            ContentType.MULTI_PART_FORM_DATA -> {
                val builder = MultipartBody.Builder()
                params.forEach {
                    when(it.value) {
                        is File -> {
                            val file = it.value as File
                            builder.addFormDataPart(it.key, file.name, file.asRequestBody())
                        }
                        is FileItem -> {
                            val item = it.value as FileItem
                            builder.addFormDataPart(it.key, item.name, item.bytes.toRequestBody())
                        }
                        else -> builder.addFormDataPart(it.key, it.value.toString())
                    }
                }
                if (onProgressChange != null) {
                    ObservableMultipartBody(builder.build(), onProgressChange!!)
                } else {
                    builder.build()
                }
            }
            ContentType.JSON -> {
                val json = JSONObject.toJSONString(params)
                json.toRequestBody("application/json;charset=utf-8".toMediaType())
            }
        }
    }

    //生成get请求参数
    private fun generateGetParams(): String? {

        val params = getParameters()
        if (!params.isNullOrEmpty()) {
            val builder = StringBuilder()
            var i = 0
            for ((key, value) in params) {
                if (i > 0) {
                    builder.append("&")
                }
                builder.append(key)
                builder.append("=")
                builder.append(URLEncoder.encode(value.toString(), "UTF-8"))
                i ++
            }
            return builder.toString()
        }
        return null
    }

    //开始
    @CallSuper
    open fun start() {
        if (status.compareAndSet(Status.PREPARING, Status.EXECUTING)) {
            prepare()
            onStart()
            val builder: Request.Builder
            when (httpMethod) {
                HttpMethod.GET -> {
                    var url = currentURL
                    val params = generateGetParams()
                    if (!params.isNullOrEmpty()) {
                        url = "$url?$params"
                    }
                    builder = Request.Builder().url(url)
                    builder.get()
                }
                HttpMethod.POST -> {
                    builder = Request.Builder().url(currentURL)
                    val body = getRequestBody()
                    require(body != null) {
                        "${javaClass.name} post must has params"
                    }
                    builder.post(body)
                }
            }

            headers?.forEach {
                builder.addHeader(it.key, it.value)
            }

            val client =
                if (shouldUseNewBuilder) getHttpClient(sharedHttpClient.newBuilder()) else sharedHttpClient
            _call = client.newCall(builder.build())
            _call!!.enqueue(this)
        }
    }

    //取消
    override fun cancel() {
        if (status.compareAndSet(Status.PREPARING, Status.CANCELLED)
            || status.compareAndSet(Status.EXECUTING, Status.CANCELLED)) {
            _call?.cancel()
            onCancelled()
            onComplete()
        }
    }

    override val isExecuting: Boolean
        get() = status.get() == Status.EXECUTING

    val isCancelled: Boolean
        get() = status.get() == Status.CANCELLED

    //<editor-fold desc="okHttp 请求回调">

    final override fun onResponse(call: Call, response: Response) {
        response.use {
            if (it.isSuccessful && processResponse(response.body)) {
                processSuccessResult()
            } else {
                processFailResult()
            }
        }
    }

    final override fun onFailure(call: Call, e: IOException) {
        isNetworkError = true
        processFailResult()
    }

    //</editor-fold>

    //<editor-fold desc="task 回调">

    //获取新的client 通过这个设置超时时间
    protected open fun getHttpClient(builder: OkHttpClient.Builder): OkHttpClient {
        return builder.build()
    }

    //处理结果
    protected abstract fun processResponse(body: ResponseBody?): Boolean

    //准备 开始前调用
    protected abstract fun prepare()

    //任务开始
    protected open fun onStart() {}

    //任务取消
    protected open fun onCancelled() {}

    //任务失败
    protected open fun onFailure() {}

    protected fun processFailResult() {
        callbackFailure()
    }

    //回调失败
    protected fun callbackFailure() {
        if (status.compareAndSet(Status.PREPARING, Status.FAILURE)
            || status.compareAndSet(Status.EXECUTING, Status.FAILURE)) {
            ThreadUtils.runOnMainThread {
                onFailure()
                if (onFailure != null) {
                    onFailure!!(this)
                }
                callback?.onFailure(this)
                onComplete()
            }
        }
    }

    //任务成功 异步
    protected open fun onSuccess() {

    }

    private fun processSuccessResult() {
        try {
            onSuccess()
            callback?.onSuccess(this) //异步解析
            isApiSuccess = true
        } catch (e: Exception) {
            isDataParseFail = true
            onDataParseFail()
            processFailResult()
            return
        }

        if (status.compareAndSet(Status.EXECUTING, Status.SUCCESSFUL)) {
            ThreadUtils.runOnMainThread {
                if (onProgressChange != null) {
                    onProgressChange!!(1.0)
                }
                if (onSuccess != null) {
                    onSuccess!!(this)
                }
                onComplete()
            }
        }
    }

    //数据解析失败
    protected open fun onDataParseFail() {

    }

    //任务完成 无论成功还是失败
    @CallSuper
    protected open fun onComplete() {
        callback?.onComplete(this)
        _call = null
    }

    //</editor-fold>

    //回调
    interface Callback {

        //请求失败
        fun onFailure(task: HttpTask)

        //请求成功
        fun onSuccess(task: HttpTask)

        //请求完成
        fun onComplete(task: HttpTask)
    }
}