package com.common.base.base.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.common.base.R
import com.common.base.api.HttpCancelable
import com.common.base.base.activity.BaseActivity
import com.common.base.base.activity.ResultCallback
import com.common.base.base.interf.BasePage
import com.common.base.base.widget.BaseContainer


/**
 * 基础fragment
 */
@Suppress("unused_parameter")
abstract class BaseFragment : Fragment(), BasePage {

    //容器
    private var _container: BaseContainer? = null

    /**
     * 基础容器
     */
    override val baseContainer: BaseContainer?
        get() = _container

    /**
     * 获取 activity 或者 fragment 绑定的bundle
     */
    override val attachedBundle: Bundle?
        get() = if (arguments != null) arguments else activity?.intent?.extras

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
     * activity 启动器
     */
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>

    /**
     * 当前回调
     */
    private var resultCallback: ResultCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(this)
        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (resultCallback != null && it.resultCode == Activity.RESULT_OK) {
                    resultCallback!!(it.data)
                }
                resultCallback = null
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_container == null) { //创建容器视图
            _container = BaseContainer(context)
            _container?.mOnEventCallback = this
            _container?.setShowTitleBar(showTitleBar())
            //内容视图
            initialize(inflater, _container!!, savedInstanceState)
        }
        return _container
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (showBackItem()) {
            setShowBackButton(true)
        }
    }

    //返回键
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onStart() {
        super.onStart()
        if (onBackPressedCallback == null) {
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    back()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback!!)
        }
        onBackPressedCallback!!.isEnabled = true
    }

    override fun onStop() {
        super.onStop()
        onBackPressedCallback?.isEnabled = false
    }

    //<editor-fold desc="动画">

    //获取开场动画
    @AnimRes
    fun getEnterAnim(): Int {
        return 0
    }

    //获取出场动画
    @AnimRes
    fun getExitAnim(): Int {
        return 0
    }

    //无动画
    @AnimRes
    fun getNoneAnim(): Int {
        return R.anim.anim_no
    }

    //打开activity 不要动画
    fun closeAnimate() {
//        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    //</editor-fold>

    //<editor-fold desc="返回">

    //返回
    open fun back() {
        requireActivity().finish()
    }

    fun back(resultCode: Int, data: Intent? = null) {
        requireActivity().setResult(resultCode)
        requireActivity().finish()
    }

    fun backToFragment(
        fragmentClass: Class<out BaseFragment>,
        include: Boolean = false,
        resultCode: Int = Int.MAX_VALUE
    ) {
        backTo(fragmentClass.name, include, resultCode)
    }

    //</editor-fold>

    //<editor-fold desc="启动Activity">

    //启动一个带activity的fragment
    fun startActivity(fragmentClass: Class<out BaseFragment>, extras: Bundle? = null) {
        val intent = BaseActivity.getIntentWithFragment(requireContext(), fragmentClass)
        if (extras != null) {
            extras.remove(BaseActivity.FRAGMENT_STRING)
            intent.putExtras(extras)
        }
        startActivity(intent)
    }

    fun startActivityForResult(
        fragmentClass: Class<out BaseFragment>,
        extras: Bundle? = null,
        callback: ResultCallback
    ) {
        val intent = BaseActivity.getIntentWithFragment(requireContext(), fragmentClass)
        if (extras != null) {
            extras.remove(BaseActivity.FRAGMENT_STRING)
            intent.putExtras(extras)
        }
        startActivityForResult(intent, callback)
    }

    fun startActivityForResult(intent: Intent, callback: ResultCallback) {
        resultCallback = callback
        activityLauncher.launch(intent)
    }

    //</editor-fold>

    ///获取子视图
    fun <T : View> findViewById(@IdRes id: Int): T? {
        return _container?.findViewById(id)
    }

    fun <T : View> requireViewById(@IdRes id: Int): T {
        return findViewById(id)
            ?: throw IllegalArgumentException("ID does not reference a View inside this View")
    }

    //分发点击物理键事件
    fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return false
    }

    //屏幕焦点改变
    fun onWindowFocusChanged(hasFocus: Boolean) {
    }

    //http可取消的任务
    override var currentTasks: HashSet<HttpCancelable>? = null
}