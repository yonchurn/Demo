package com.common.base.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.common.base.BuildConfig
import com.common.base.R
import com.common.base.base.activity.ActivityLifeCycleManager
import com.common.base.extension.firstSafely
import java.util.*
import kotlin.collections.ArrayList


@Suppress("deprecation")
object AppUtils {

    val context: Context
        get() = ActivityLifeCycleManager.currentContext

    /**
     * 设备id
     */
    private var mDeviceId: String? = null
    private const val deviceIdKey = "zego_device_uuid"
    val deviceId: String
        @SuppressLint("HardwareIds")
        get() {
            if (StringUtils.isEmpty(mDeviceId)) {
                mDeviceId = PrefsUtils.loadString(deviceIdKey)
                if (StringUtils.isEmpty(mDeviceId)) {
                    val deviceId = "35" +
                            Build.BOARD.length % 10 +
                            Build.BRAND.length % 10 +
                            Build.CPU_ABI.length % 10 +
                            Build.DEVICE.length % 10 +
                            Build.MANUFACTURER.length % 10 +
                            Build.MODEL.length % 10 +
                            Build.PRODUCT.length % 10

                    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    println("androidId = $androidId, $deviceId")
                    mDeviceId = if (StringUtils.isEmpty(androidId)) {
                        UUID.randomUUID().toString()
                    } else {
                        UUID(deviceId.hashCode().toLong(), androidId.hashCode().toLong()).toString()
                    }
                    PrefsUtils.save(deviceIdKey, mDeviceId)
                }
            }

            return mDeviceId!!
        }

    /**
     * 是否是测试app 用版本号识别是否是测试 如1.0.0.1 长度大于6的是测试包
     */
    val isTestApp: Boolean
        get() = appVersionName.length > 6

    /**
     * 设置自定义的设备id，只用于测试
     */
    internal fun setCustomDeviceId(deviceId: String?) {
        if (BuildConfig.DEBUG && mDeviceId != null) {
            error("setCustomDeviceId use debug only")
        }
        mDeviceId = deviceId
    }

    /**
     * 获取app版本号
     */
    val appVersionCode: Long
        get() {
            var ver: Long = 0
            try {
                val packageName = context.packageName
                val packageManager = context.packageManager
                ver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageManager.getPackageInfo(packageName, 0).longVersionCode
                } else {
                    packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return ver
        }

    /**
     * 获取app版本名
     */
    val appVersionName: String
        get() {
            var ver = ""
            try {
                val packageName: String = context.packageName
                val packageManager: PackageManager = context.packageManager
                ver = packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return ver
        }

    /**
     * 获取app包名
     */
    val appPackageName: String
        get() = context.packageName

    /**
     * app名称
     */
    val appName: String
        get() = context.getString(R.string.app_name)

    /**
     * 获取状态栏样式
     */
    val statusBarStyle: String
        get() = if (context.resources.getBoolean(R.bool.status_bar_is_light)) "white" else "black"

    /**
     * 获取系统版本
     */
    val systemVersion: String
        get() = Build.VERSION.RELEASE

    private const val SHORTCUT_INSTALLED = "SHORTCUT_INSTALLED"

    /**
     * 创建桌面快捷方式
     * @param appName app名称
     * @param appIconRes app图标
     */
    fun createShortcut(context: Context, appName: String?, @DrawableRes appIconRes: Int) {
        if (!PrefsUtils.loadBoolean(SHORTCUT_INSTALLED, false)) {
            PrefsUtils.save(SHORTCUT_INSTALLED, true)
            val main = Intent()
            main.component = ComponentName(context, context.javaClass)
            main.action = Intent.ACTION_MAIN
            main.addCategory(Intent.CATEGORY_LAUNCHER)
            //要添加这句话
            main.flags = Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK
            val shortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
            shortcutIntent.putExtra("duplicate", true)
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName)
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, main)
            shortcutIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, appIconRes)
            )
            context.sendBroadcast(shortcutIntent)
        }
    }

    ///拨打电话
    fun makePhoneCall(context: Context, phone: String?) {
        if (StringUtils.isEmpty(phone)) return
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    ///拨打电话
    fun makePhoneCall(context: Context, phones: ArrayList<String>?){
        if (phones.isNullOrEmpty()) return
        if (phones.size == 1){
            makePhoneCall(context, phones.firstSafely())
        }else{
            AlertUtils.actionSheet(
                cancelButtonTitle = context.getString(R.string.cancel),
                buttonTitles = phones.toTypedArray(),
                onItemClick = {
                    makePhoneCall(context, phones[it])
                }
            ).show()
        }
    }

    /**
     * 关闭软键盘
     * @param context  上下文
     * @param view 当前焦点
     */
    fun hideSoftInputMethod(context: Context, view: View?) {
        if (view != null) {
            try {
                // 隐藏软键盘
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打开软键盘
     * @param context 上下文
     * @param view 当前焦点
     */
    fun showSoftInputMethod(context: Context, view: View?) {
        if (view != null) {
            try {
                // 打开软键盘
                view.requestFocus()
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(view, InputMethodManager.SHOW_FORCED)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     *
     * @param dest 目的地
     * @param destLatitude 目的地维度
     * @param destLongitude 目的地经度
     */
    fun openMapForNavigation(
        context: Context,
        dest: String?,
        destLatitude: Double,
        destLongitude: Double
    ) {

        if (context is FragmentActivity) {
            val fragment = AlertUtils.actionSheet("查看路线", buttonTitles = arrayOf("百度地图", "高德地图"))
            fragment.onItemClick = { position ->
                when (position) {
                    0 -> {
                        if (isInstall("com.baidu.BaiduMap")) {
                            val intent = Intent()
                            intent.data = Uri.parse(
                                java.lang.String.format(
                                    Locale.US,
                                    "baidumap://map/direction?origin={{我的位置}}&destination=latlng:%f," +
                                            "%f|name=%s&mode=driving&coord_type=gcj02",
                                    destLatitude,
                                    destLongitude,
                                    dest
                                )
                            )
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "没有安装百度地图", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        if (isInstall("com.autonavi.minimap")) {
                            val intent = Intent()
                            intent.data = Uri.parse(
                                java.lang.String.format(
                                    Locale.US,
                                    "androidamap://navi?sourceApplication= &backScheme= &lat=%f&lon=%f&dev=0&style=2",
                                    destLatitude, destLongitude
                                )
                            )
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "没有安装高德地图", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            val activity: FragmentActivity = context
            fragment.show(activity.supportFragmentManager, null)
        }
    }

    /**
     * 使用默认浏览器打开
     */
    fun openUrl(url: String?) {

        if (StringUtils.isEmpty(url)) return
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 打开其他app
     */
    fun openApp(url: String?) {
        if (StringUtils.isEmpty(url)) return
        try {
            val intent = Intent()
            intent.data = Uri.parse(url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //判断是否安装了某个应用
    fun isInstall(packageName: String): Boolean {
        val packageInfo = try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        return packageInfo != null
    }

    //打开app设置详情
    fun openAppSettings(@StringRes title: Int) {
        openAppSettings(context.getString(title))
    }

    fun openAppSettings(title: String) {
        AlertUtils.alert(
            title = title,
            buttonTitles = arrayOf(
                context.getString(R.string.cancel),
                context.getString(R.string.go_to_setting)
            ),
            onItemClick = {
                if (it == 1) {
                    openAppSettings()
                }
            })
    }


    //打开app设置详情
    fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(intent)
        } catch (e: Exception) {

        }
    }

    /**
     * 设置状态栏样式
     * @param window 对应window
     * @param backgroundColor 背景颜色 0不改变并且全屏
     * @param isLight 内容是否是浅色(白色）
     * @param overlay 状态栏是否是否覆盖在布局上面
     * @return 是否成功
     */
    fun setStatusBarStyle(
        window: Window?,
        @ColorInt backgroundColor: Int?,
        isLight: Boolean,
        overlay: Boolean = backgroundColor == 0
    ): Boolean {

        if (window == null) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.statusBarColor = backgroundColor ?: ContextCompat.getColor(
                window.context,
                R.color.status_bar_background_color
            )
            if (isLight) {
                var flags: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                if (overlay) {
                    flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
                window.decorView.systemUiVisibility = flags
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //android6.0以后可以对状态栏文字颜色和图标进行修改
                    var flags: Int = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    if (overlay) {
                        flags = flags or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    }
                    window.decorView.systemUiVisibility = flags
                } else {
                    if (overlay) {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    }
                }
            }
            return true
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            if (overlay) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
        return false
    }

    /**
     * 判断是否为鸿蒙系统
     * */
    fun isHarmonyOs(): Boolean{
        try {
            val clz = Class.forName("com.huawei.system.BuildEx")
            val method = clz.getMethod("getOsBrand")
            return "harmony" == method.invoke(clz)
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * 消息通知是否开启
     * */
     fun isNotificationEnabled(): Boolean{
        var isOpened: Boolean
        try {
            isOpened = NotificationManagerCompat.from(context).areNotificationsEnabled()
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            isOpened = false
        }

        return isOpened
    }


    /**
     * 打开app设置通知页
     * */
     fun openAppNotificationSettings(){
        try {
            val intent = Intent()
            when {
                Build.VERSION.SDK_INT >= 26 -> { //android 8.0
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra("android.provider.extra.APP_PACKAGE", appPackageName)
                }
                Build.VERSION.SDK_INT >= 21 -> { //android 5.0-7.0
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra("app_package", appPackageName)
                    intent.putExtra("app_uid", context.applicationInfo.uid)
                }
                else -> { //其他
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.fromParts("package", appPackageName, null)
                }
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
        }

    }

}