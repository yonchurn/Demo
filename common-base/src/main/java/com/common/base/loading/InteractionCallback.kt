package com.common.base.loading

import androidx.annotation.StringRes
import com.common.base.utils.ToastType

/**
 * 交互回调
 */
interface InteractionCallback {

    //显示loading
    fun showLoading(delay: Long = 0, text: CharSequence? = null)

    //隐藏loading
    fun hideLoading()

    //提示文字信息
    fun showToast(text: CharSequence?, @StringRes textRes: Int?, type: ToastType = ToastType.NORMAL)
}