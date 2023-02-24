package com.common.base.base.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.R
import com.common.base.api.HttpCancelable
import com.common.base.api.HttpProcessor
import com.common.base.app.BaseContextWrapper
import com.common.base.app.LogEventHelper
import com.common.base.base.fragment.BaseFragment
import com.common.base.base.interf.BaseBundleFetcher
import com.common.base.base.interf.BaseResourceFetcher
import com.common.base.language.LanguageHelper
import com.common.base.route.RouteHelper
import com.common.base.utils.AppUtils


/**
 * startActivityForResult 回调
 */
typealias ResultCallback = (data: Intent?) -> Unit

/**
 * 基础activity
 */
open class BaseActivity : AppCompatActivity(), HttpProcessor, BaseResourceFetcher, BaseBundleFetcher {

    /**
     * 获取 activity 或者 fragment 绑定的bundle
     */
    override val attachedBundle: Bundle?
        get() = intent.extras

    /**
     * 获取context
     */
    override val attachedContext: Context?
        get() = this

    /**
     * 关联的activity
     */
    override val attachedActivity: Activity?
        get() = this

    //activity 名称 为fragment的类名 或者 activity类名
    var name: String? = null

    //当前显示的Fragment
    private var _fragment: BaseFragment? = null
    val fragment: BaseFragment?
        get() = _fragment

    //是否可见
    private var _visible = false
    val isVisible: Boolean
        get() = _visible

    /**
     * 内容视图 需要设置id为 current_content
     */
    protected val currentContentView: View?
        get() = if (_fragment?.baseContainer != null) _fragment!!.baseContainer!!.contentView else findViewById(
            R.id.current_content
        )

    /**
     * activity 启动器
     */
    protected lateinit var activityLauncher: ActivityResultLauncher<Intent>
        private set

    /**
     * 当前回调
     */
    protected var resultCallback: ResultCallback? = null

    /**
     * 是否把回调置空 如果是 cancel
     */
    protected var resultCallbackRemoveWhileCancelled = true

    /**
     * 当前路由地址，如果是通过路由的方式打开
     */
    val routePath: String?
        get() = intent.getStringExtra(RouteHelper.PATH)

    //<editor-fold desc="父类方法">

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)

        ARouter.getInstance().inject(this)

        //状态栏
        if(shouldSetStatusBarStyle){
            AppUtils.setStatusBarStyle(
                window, ContextCompat.getColor(this, R.color.status_bar_background_color),
                resources.getBoolean(R.bool.status_bar_is_light)
            )
        }

        val layoutRes = getContentViewRes()
        if (layoutRes != 0) {
            setContentView(layoutRes)
        }

        //生成fragment实例
        val className = intent.getStringExtra(FRAGMENT_STRING)
        if (className != null && layoutRes != 0) {
            val clazz: Class<*>
            try {
                clazz = Class.forName(className)
                name = className
                val currentFragment = clazz.newInstance() as BaseFragment
                val bundle = intent.extras
                if (bundle != null) {
                    currentFragment.arguments = bundle
                }
                configFragment(currentFragment)
                setFragment(currentFragment)
                val enter = _fragment?.getEnterAnim()
                if (enter != 0) {
                    overridePendingTransition(enter!!, R.anim.anim_no)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            name = javaClass.name
        }

        lifecycle.addObserver(this)
        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (resultCallback != null && it.resultCode == Activity.RESULT_OK) {
                    resultCallback!!(it.data)
                }
                if (it.resultCode != Activity.RESULT_CANCELED || resultCallbackRemoveWhileCancelled) {
                    resultCallback = null
                }
            }
    }

    override fun onResume() {
        super.onResume()
        _visible = true
        LogEventHelper.reportScreenName(screenName, javaClass.simpleName)
    }

    override fun onPause() {
        super.onPause()
        _visible = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //不让重建fragment
        removeFragmentStateInBundle(outState)
    }

    //移除fragment 装信息
    private fun removeFragmentStateInBundle(bundle: Bundle) {
        bundle.remove("android:support:fragments")
        val keys = bundle.keySet()
        for (key in keys) {
            val value = bundle.get(key)
            if (value is Bundle) {
                removeFragmentStateInBundle(value)
            }
        }
    }

    //语言国际化
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(BaseContextWrapper.wrap(newBase, LanguageHelper.currentLanguage))
    }

    //</editor-fold>

    //<editor-fold desc="内容">

    //是否需要设置状态栏样式
    open val shouldSetStatusBarStyle = true

    //获取视图内容，如果为0，则忽略 可以包含 fragment_container
    @LayoutRes
    open fun getContentViewRes(): Int {
        return R.layout.base_activity
    }

    /**
     * 设置当前显示的fragment,可设置动画效果
     *
     * @param currentFragment 当前要显示的fragment
     * @param enter           进场动画
     * @param exit            出场动画
     */
    fun setFragment(
        currentFragment: BaseFragment,
        @AnimRes enter: Int = 0,
        @AnimRes exit: Int = 0
    ) {
        _fragment = currentFragment
        val transaction = supportFragmentManager.beginTransaction()
        if (enter != 0 && exit != 0) {
            transaction.setCustomAnimations(enter, exit)
        }
        transaction.replace(R.id.fragment_container, currentFragment)
        transaction.commitAllowingStateLoss()
    }

    fun addFragment(fragment: BaseFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, fragment)
        transaction.commitAllowingStateLoss()
    }

    fun removeFragment(fragment: BaseFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commitAllowingStateLoss()
    }

    open fun configFragment(fragment: BaseFragment) {}

    companion object {

        //activity里面fragment的类名
        const val FRAGMENT_STRING = "fragmentString"

        //获取 fragment对应的 intent
        fun getIntentWithFragment(
            context: Context,
            fragmentClass: Class<out BaseFragment?>
        ): Intent {
            val intent = Intent(context, BaseActivity::class.java)
            intent.putExtra(FRAGMENT_STRING, fragmentClass.name)
            return intent
        }
    }

    //</editor-fold>


    //启动一个activity
    fun startActivity(activityClass: Class<out Activity>, extras: Bundle? = null) {
        val intent = Intent(this, activityClass)
        if (extras != null) {
            intent.putExtras(extras)
        }
        startActivity(intent)
    }

    fun startActivityForResult(
        activityClass: Class<out Activity>,
        extras: Bundle? = null,
        callback: ResultCallback
    ) {
        val intent = Intent(this, activityClass)
        if (extras != null) {
            intent.putExtras(extras)
        }
        resultCallback = callback
        activityLauncher.launch(intent)
    }

    override fun finish() {
        super.finish()
        if (_fragment != null) {
            val exit = _fragment!!.getExitAnim()
            if (exit != 0) {
                overridePendingTransition(R.anim.anim_no, exit)
            }
        }
    }

    fun finish(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (_fragment != null && _fragment!!.dispatchKeyEvent(event)) {
            true
        } else super.dispatchKeyEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        _fragment?.onWindowFocusChanged(hasFocus)
    }

    //http可取消的任务
    override var currentTasks: HashSet<HttpCancelable>? = null

    //回调
    private var callbackEntities: HashMap<Int, CallbackEntity>? = null

    //添加一个回调 onActivityResult
    fun addCallback(
        callback: ResultCallback,
        requestCode: Int,
        removeAfterUse: Boolean = true
    ) {
        if (callbackEntities == null) {
            callbackEntities = HashMap()
        }
        callbackEntities!![requestCode] = CallbackEntity(callback, removeAfterUse)
    }

    @Suppress("deprecation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && !callbackEntities.isNullOrEmpty()) {
            val entity = callbackEntities!![requestCode]
            if (entity != null) {
                entity.callback(data)
                if (entity.removeAfterUse) {
                    callbackEntities!!.remove(requestCode)
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //回调
    private class CallbackEntity(
        val callback: ResultCallback,
        val removeAfterUse: Boolean
    )

    //<editor-fold desc="日志">

    //日志屏幕名称 子类重写
    open val screenName: String? = null

    //</editor-fold>
}