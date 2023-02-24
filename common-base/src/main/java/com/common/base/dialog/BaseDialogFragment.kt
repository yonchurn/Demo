package com.common.base.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.common.base.R
import com.common.base.base.activity.ActivityLifeCycleManager
import com.common.base.base.interf.VoidCallback


/**
 * 基础弹窗fragment
 */
abstract class BaseDialogFragment: DialogFragment() {

    //弹窗消失回调
    private val onDismissHandlers: HashSet<VoidCallback> by lazy {
        HashSet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.Theme_dialog_noTitle_noBackground)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //设置弹窗样式
        val window = dialog?.window
        if (window != null) {
            val outValue = TypedValue()
            resources.getValue(R.dimen.dialog_bg_alpha, outValue, true)
            window.setDimAmount(outValue.float)
            val view = window.decorView
            view.setPadding(0, 0, 0, 0)
        }

        dialog?.setCanceledOnTouchOutside(true)

        return getContentView(inflater, container, savedInstanceState)
    }

    fun show(manager: FragmentManager) {
        val transaction = manager.beginTransaction()
        transaction.add(this, tag)
        transaction.commitNowAllowingStateLoss()
    }

    fun show() {
        val fragmentActivity = ActivityLifeCycleManager.currentActivity
        if (fragmentActivity is FragmentActivity) {
            show(fragmentActivity.supportFragmentManager)
        } else {
            Log.d("AlertDialogFragment", "AlertDialogFragment show currentActivity is not a FragmentActivity")
        }
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    //添加弹窗消失回调
    fun addOnDismissHandler(onDismissHandler: VoidCallback?) {
        if (onDismissHandler == null) return
        onDismissHandlers.add(onDismissHandler)
    }

    //移除
    fun removeOnDismissHandler(onDismissHandler: VoidCallback?) {
        if (onDismissHandler == null) return
        onDismissHandlers.remove(onDismissHandler)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if (onDismissHandlers.isNotEmpty()) {
            for (onDismissHandler in onDismissHandlers) {
                onDismissHandler()
            }
        }
    }

    //获取内容视图
    abstract fun getContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View
}