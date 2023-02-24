package com.common.base.web

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.luck.picture.lib.config.PictureConfig
import com.common.base.R
import com.common.base.api.booleValue
import com.common.base.app.AppVersionManager
import com.common.base.helper.LocationHelper
import com.common.base.helper.PermissionHelper
import com.common.base.image.ImagePicker
import com.common.base.language.LanguageHelper
import com.common.base.utils.*

/**
 * 基础js交互
 */
open class BaseJavascriptMethod(
    private val fragment: WebFragment,
    private val webView: CustomWebView
) : LifecycleObserver{

    protected val activity: Activity
        get() = fragment.requireActivity()

    //返回
    @JavascriptInterface
    fun goBack() {
        val activity = fragment.activity
        if (activity != null) {
            activity.runOnUiThread {
                activity.finish()
            }
        }
    }

    var locationHelper: LocationHelper? = null
        private set

    //获取当前位置
    @JavascriptInterface
    fun getLocation() {
        if (locationHelper == null) {
            locationHelper = LocationHelper(fragment)
            locationHelper!!.callback = { location ->
                val jsonObject = JSONObject()
                jsonObject["latitude"] = location?.latitude ?: 0
                jsonObject["longitude"] = location?.latitude ?: 0

                evaluateJsMethod("setLocation", null, jsonObject.toString())
            }
        }
        locationHelper!!.startLocation()
    }

    //获取系统信息
    @JavascriptInterface
    fun getSystemInfo() {
        val json = JSONObject()
        json["language"] = LanguageHelper.currentLanguageToCommon
        json["systemVersion"] = AppUtils.systemVersion
        json["appVersion"] = AppUtils.appVersionName
        json["appVersionCode"] = AppUtils.appVersionCode
        json["appName"] = AppUtils.appName
        json["statusHeight"] = SizeUtils.getStatusBarHeight(activity)
        json["statusStyle"] = AppUtils.statusBarStyle
        json["screenWidth"] = SizeUtils.getWindowWidth(activity)
        json["screenHeight"] = SizeUtils.getWindowHeight(activity)
        json["navigationBackgroundColor"] = ColorUtils.colorToHex(
            ContextCompat.getColor(
                activity,
                R.color.title_bar_background_color
            )
        )
        json["navigationTitleColor"] =
            ColorUtils.colorToHex(ContextCompat.getColor(activity, R.color.title_bar_title_color))
        json["navigationTintColor"] =
            ColorUtils.colorToHex(ContextCompat.getColor(activity, R.color.title_bar_tint_color))
        json["themeColor"] =
            ColorUtils.colorToHex(ContextCompat.getColor(activity, R.color.theme_color))
        json["themeTintColor"] =
            ColorUtils.colorToHex(ContextCompat.getColor(activity, R.color.theme_tint_color))
        json["uuid"] = AppUtils.deviceId
        json["channel"] = AppVersionManager.channel

        evaluateJsMethod("onGetSystemInfo", null, json.toString())
    }

    private val imagePicker by lazy { ImagePicker() }

    //选择图片
    @JavascriptInterface
    fun selectPhoto(data: String?) {

        if (StringUtils.isEmpty(data)) return
        try {
            val jsonObject = JSON.parseObject(data)
            val type = jsonObject.getIntValue("type")
            var count = jsonObject.getIntValue("count")
            val cropWidth = jsonObject.getIntValue("cropWidth")
            val cropHeight = jsonObject.getIntValue("cropHeight")

            val maxWidth = jsonObject.getIntValue("maxWidth")
            val maxHeight = jsonObject.getIntValue("maxHeight")

            if (count == 0) count = 1
            if (count > 9) count = 9

            val mode = if (type == 0) PictureConfig.SINGLE else PictureConfig.MULTIPLE

            activity.runOnUiThread {
                imagePicker.config = {
                    it.selectionMode(mode)
                        .withAspectRatio(cropWidth, cropHeight)
                        .isEnableCrop(mode == PictureConfig.SINGLE && cropWidth > 0 && cropHeight > 0)
                        .cropImageWideHigh(maxWidth, maxHeight)
                }
                imagePicker.pick(activity, count) {
                    if (it.isNotEmpty()) {
                        val array = JSONArray()
                        for (value in it) {
                            array.add("data:image/jpeg;base64,${FileUtils.getBase64(value.path)}")
                        }
                        evaluateJsMethod("selectPhotoCallback", null, array.toString())
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            ToastUtils.showToast(e.message ?: "")
        }
    }

    //选择联系人
    @JavascriptInterface
    fun pickContact() {
        activity.runOnUiThread {
            PermissionHelper.requestPermissionsIfNeeded(
                fragment,
                arrayOf(Manifest.permission.READ_CONTACTS)
            ) {
                if (it) {
                    pickContactAfterGranted()
                } else {
                    AppUtils.openAppSettings("No contacts permission")
                }
            }
        }
    }

    private fun pickContactAfterGranted() {
        val intent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.Contacts.CONTENT_URI
        )
        fragment.startActivityForResult(intent) { data ->
            if (data != null) {
                val contactPhone = getContactFix(data)
                if (!contactPhone.isNullOrEmpty()) {
                    val json = JSONObject()
                    json["name"] = contactPhone[0]
                    json["mobile"] = contactPhone[1].replace(Regex("[^\\d]"), "")
                    evaluateJsMethod("onPickContact", null, json.toString())
                }
            }
        }
    }

    @Suppress("deprecation")
    fun getContactFix(data: Intent): Array<String>? {
        val contactData = data.data ?: return null
        val cursor = activity.managedQuery(contactData, null, null, null, null) ?: return null
        if (cursor.moveToFirst()) {
            val result = arrayOf("", "")
            var hasPhone = cursor
                .getString(
                    cursor
                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                )
            val id = cursor.getString(
                cursor
                    .getColumnIndex(ContactsContract.Contacts._ID)
            )
            hasPhone = if (hasPhone.equals("1", ignoreCase = true)) {
                "true"
            } else {
                "false"
            }
            if (hasPhone.toBoolean()) {
                val phones = activity.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            + " = " + id, null, null
                )
                var phone: String? = ""
                var name: String? = ""
                while (phones!!.moveToNext()) {
                    phone =
                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    name =
                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                }
                phones.close()
                result[0] = name ?: ""
                result[1] = phone ?: ""
                return result
            }
        }
        return null
    }

    //打电话
    @JavascriptInterface
    fun makePhoneCall(data: String?) {

        if (StringUtils.isEmpty(data)) return
        try {
            val json = JSONObject.parseObject(data)
            val mobile = json.getString("mobile")
            activity.runOnUiThread {
                AppUtils.makePhoneCall(activity, mobile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //刷新当前网页
    @JavascriptInterface
    fun refreshCurrentPage(isClearCache: Boolean) {
        activity.runOnUiThread {
            if (isClearCache) {
                webView.clearCache(true)
            } else {
                webView.reload()
            }
        }
    }

    //打开默认浏览器浏览器
    @JavascriptInterface
    fun openBrowser(data: String?) {

        if (StringUtils.isEmpty(data)) return
        try {
            val json = JSONObject.parseObject(data)
            val url = json.getString("url")
            if (StringUtils.isEmpty(url)) return

            activity.runOnUiThread {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse(url)
                activity.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //打开一个路由链接
    @JavascriptInterface
    fun openRoute(data: String?) {

        if (StringUtils.isEmpty(data)) return
        try {
            val json = JSONObject.parseObject(data)
            val path = json.getString("path")
            if (StringUtils.isEmpty(path)) return

            val uri = Uri.parse(path)
            if (uri != null) {
                activity.runOnUiThread {
                    ARouter.getInstance().build(uri).navigation()
                    if (json.getIntValue("closeCurrent") == 1) {
                        activity.finish()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //设置app样式
    @JavascriptInterface
    fun setAppStyle(data: String?) {
        if (StringUtils.isEmpty(data)) return
        try {
            val json = JSONObject.parseObject(data)
            val barBackgroundColor = json.getString("barBackgroundColor")
            val barTitleColor = json.getString("barTitleColor")
            val barTintColor = json.getString("barTintColor")
            val statusBarStyle = json.getString("statusBarStyle")

            activity.runOnUiThread {
                var bgColor: Int? = null
                if (StringUtils.isNotEmpty(barBackgroundColor)) {
                    bgColor = Color.parseColor(barBackgroundColor)
                    fragment.baseContainer?.titleBar?.setBackgroundColor(bgColor)
                }
                if (StringUtils.isNotEmpty(barTitleColor)) {
                    fragment.baseContainer?.titleBar?.titleColor = Color.parseColor(barTitleColor)
                }
                if (StringUtils.isNotEmpty(barTintColor)) {
                    fragment.baseContainer?.titleBar?.tintColor = Color.parseColor(barTintColor)
                }
                if (StringUtils.isNotEmpty(statusBarStyle)) {
                    AppUtils.setStatusBarStyle(
                        activity.window,
                        bgColor,
                        statusBarStyle == "white"
                    )
                }

                if (json[WebConfig.GO_BACK_ENABLED] != null) {
                    fragment.goBackEnabled = json.booleValue(WebConfig.GO_BACK_ENABLED)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 执行js方法
     */
    fun evaluateJsMethod(
        methodName: String,
        callback: ValueCallback<String?>?,
        vararg params: String?
    ) {
        var finalParams = ""
        params.forEach {
            if (StringUtils.isNotEmpty(finalParams)) {
                finalParams += ","
            }
            finalParams += "'$it'"
        }

        val js = if (StringUtils.isEmpty(finalParams)) {
            "javascript:$methodName()"
        } else {
            "javascript:$methodName($finalParams)"
        }
        activity.runOnUiThread { webView.evaluateJavascript(js, callback) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        locationHelper?.stopLocation()
    }
}