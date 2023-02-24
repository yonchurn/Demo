package com.common.base.api

import com.alibaba.fastjson.JSONObject
import com.common.base.R
import com.common.base.loading.InteractionCallback
import com.common.base.utils.StringUtils
import com.common.base.utils.ToastType
import okhttp3.ResponseBody

/**
 * json http 任务
 */
abstract class HttpJSONTask : HttpTask() {

    //交互回调
    var interactionCallback: InteractionCallback? = null

    //是否需要显示loading
    var shouldShowLoading = false

    //loading 显示延迟 毫秒
    var loadingDelay = 500L

    //是否需要显示错误信息
    var shouldShowErrorMessage = false

    //提示信息
    open var message: String? = null

    //api错误码
    var apiCode = ""
        protected set

    //原始json数据
    var rawData: JSONObject? = null
        protected set

    //使用的数据
    var data: JSONObject? = null
        protected set

    //快速设置loading
    fun interactionSetting(callback: InteractionCallback?,
                           showLoading: Boolean = true,
                           delay: Long = 500L,
                           showErrorMessage: Boolean = true) {
        interactionCallback = callback
        shouldShowLoading = showLoading
        loadingDelay = delay
        shouldShowErrorMessage = showErrorMessage
    }

    final override fun processResponse(body: ResponseBody?): Boolean {
        if (body != null) {
            try {
                val string = body.string()
                val json = JSONObject.parse(string)
                if (json is JSONObject) {
                    return processJSON(json)
                } else {
                    return false
                }
            } catch (e: Exception) {
                return false
            }
        }
        return false
    }

    override fun onStart() {

        if (shouldShowLoading && interactionCallback != null) {
            interactionCallback!!.showLoading(loadingDelay)
        }
    }

    override fun onDataParseFail() {
        apiCode = "dataParseFail"
    }

    override fun onFailure() {
        if (shouldShowErrorMessage && interactionCallback != null) {
            val text = message
            if (StringUtils.isNotEmpty(text)) {
                interactionCallback!!.showToast(text, null, ToastType.ERROR)
            } else {
                interactionCallback!!.showToast(null, R.string.network_error, ToastType.ERROR)
            }
        }
    }

    override fun onComplete() {
        if (shouldShowLoading && interactionCallback != null) {
            interactionCallback!!.hideLoading()
        }
        super.onComplete()
    }

    //处理json 返回api是否成功
    protected abstract fun processJSON(json: JSONObject): Boolean
}