package com.common.base.loading

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.common.base.BaseInitializer
import com.common.base.R
import com.common.base.utils.StringUtils
import com.common.base.utils.ViewUtils

/**
 * loading显示帮助类
 */
interface LoadingHelper {

    //显示菊花
    var loadingView: LoadingView?
    var loading: Boolean

    fun showLoading(view: View, delay: Long, text: CharSequence? = null) {
        if(!loading){
            val parent = ViewUtils.findSuitableParent(view)
                ?: throw java.lang.IllegalArgumentException(
                    "No suitable parent found from the given view. Please provide a valid view.")
            showLoading(parent, delay, text)
        }else{
            Log.d("LoadingView", "Already show")
        }
    }

    fun showLoading(parent: ViewGroup, delay: Long, text: CharSequence? = null) {
        if (!loading) {
            loading = true
            loadingView = if (BaseInitializer.loadingViewCreator != null) {
                BaseInitializer.loadingViewCreator!!(parent.context)
            } else {
                LayoutInflater.from(parent.context).inflate(R.layout.default_loading_view, parent, false) as LoadingView?
            }

            loadingView!!.delay = delay
            if (loadingView is DefaultLoadingView) {
                val loadingText = if(StringUtils.isEmpty(text)) parent.context.getString(R.string.loading_text) else text
                (loadingView as DefaultLoadingView).textView.text = loadingText
            }
            parent.addView(loadingView)
            val params = loadingView!!.layoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT

            if (params is RelativeLayout.LayoutParams){
                params.alignWithParent = true
                params.addRule(RelativeLayout.BELOW, R.id.base_fragment_title_bar_id)
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            }
        }
    }

    fun hideLoading() {
        if(loading){
            loading = false
            ViewUtils.removeFromParent(loadingView)
            loadingView = null
        }
    }
}