package com.common.base.utils

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.common.base.R
import com.common.base.base.activity.ActivityLifeCycleManager


/**
 * toast类型
 */
enum class ToastType {

    //正常
    NORMAL,

    //成功
    SUCCESS,

    //错误
    ERROR,
}

/**
 * Toast 工具类
 */
object ToastUtils {

    val context: Context
        get() = ActivityLifeCycleManager.currentContext

//    fun showToast(view: View, text: CharSequence) {
//        ToastManager.show(text, view)
//    }
//
//    fun showToast(container: ToastContainer, text: CharSequence) {
//        showToast(container.toastContainer, text)
//    }

    fun showSuccessToast(@StringRes text: Int) {
        showToast(text, ToastType.SUCCESS)
    }
    
    fun showSuccessToast(text: CharSequence) {
        showToast(text, ToastType.SUCCESS)
    }

    fun showErrorToast(@StringRes text: Int) {
        showToast(text, ToastType.ERROR)
    }

    fun showErrorToast(text: CharSequence) {
        showToast(text, ToastType.ERROR)
    }

    fun showToast(@StringRes text: Int, type: ToastType = ToastType.NORMAL) {
        show(context.getString(text), type)
//        val context = ActivityLifeCycleManager.currentContext
//        if (context is Activity) {
//            try {
//                val view = context.window.decorView.findViewById<FrameLayout>(android.R.id.content)
//                ToastManager.show(context.getString(text), view, mask, dismissCallback)
//            }catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    fun showToast(text: CharSequence, type: ToastType = ToastType.NORMAL) {
        show(text, type)
//        val context = ActivityLifeCycleManager.currentContext
//        if (context is Activity) {
//            try {
//                val view = context.window.decorView.findViewById<FrameLayout>(android.R.id.content)
//                ToastManager.show(text, view, mask, dismissCallback)
//            }catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    //当前显示的toast
    private var currentToast: Toast? = null

    //获取toast
    private fun getToast(context: Context): Toast {
        close()
        currentToast = Toast(context)
        return currentToast!!
    }


    //关闭上一个toast
    private fun close() {
        if (currentToast != null) {
            currentToast!!.cancel()
            currentToast = null
        }
    }

    @Suppress("deprecation")
    private fun show(text: CharSequence, type: ToastType) {
        currentToast = getToast(context)
        val res = when(type){
            ToastType.NORMAL -> R.layout.toast_normal_layout
            ToastType.SUCCESS -> R.layout.toast_success_layout
            ToastType.ERROR -> R.layout.toast_error_layout
        }
        val view = View.inflate(context, res, null)
        val textView = view.findViewById<TextView>(R.id.text)
        textView.text = text
        currentToast?.apply {
            setGravity(Gravity.CENTER, 0, 0)
            setView(view)
            duration = Toast.LENGTH_SHORT
            show()
        }
    }
}