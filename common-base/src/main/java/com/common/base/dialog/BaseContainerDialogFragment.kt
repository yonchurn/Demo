package com.common.base.dialog

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import com.common.base.api.HttpCancelable
import com.common.base.base.interf.BasePage
import com.common.base.base.widget.BaseContainer


/**
 * 基础内容视图弹窗
 */
abstract class BaseContainerDialogFragment : BaseDialogFragment(), BasePage {

    /**
     * 获取 activity 或者 fragment 绑定的bundle
     */
    override val attachedBundle: Bundle?
        get() = arguments

    /**
     * 获取context
     */
    override val attachedContext: Context?
        get() = context

    /**
     * 关联的activity
     */
    override val attachedActivity: Activity?
        get() = activity

    /**
     * 基础容器
     */
    private var _container: BaseContainer? = null
    override val baseContainer: BaseContainer?
        get() = _container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _container = BaseContainer(context)
        _container?.run {
            setShowTitleBar(showTitleBar())
            mOnEventCallback = this@BaseContainerDialogFragment
        }

        initialize(inflater, _container!!, savedInstanceState)
        onConfigure(dialog!!.window!!, _container!!.contentView!!.layoutParams as RelativeLayout.LayoutParams)

        return _container!!
    }

    //http可取消的任务
    override var currentTasks: HashSet<HttpCancelable>? = null

    /**
     * 配置弹窗信息
     * @param window 弹窗
     * @param contentViewLayoutParams 内容视图布局
     */
    abstract fun onConfigure(
        window: Window,
        contentViewLayoutParams: RelativeLayout.LayoutParams
    )
}