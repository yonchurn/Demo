package com.common.base.extension

import android.view.View
import android.view.ViewGroup
import com.common.base.base.widget.OnSingleClickListener
import com.common.base.utils.ViewUtils


/**
 * 视图扩展
 */

const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

/**
 * 防止多次点击
 */
fun View.setOnSingleListener(callback: (v: View) -> Unit) {

    setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(v: View) {
            callback(v)
        }
    })
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.removeFromParent() {
    ViewUtils.removeFromParent(this)
}