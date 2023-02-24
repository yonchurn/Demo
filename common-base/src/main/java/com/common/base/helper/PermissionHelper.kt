package com.common.base.helper

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.common.base.base.interf.PermissionRequester
import java.util.*
import kotlin.collections.HashMap

/**
 * 授权回调
 */
typealias PermissionCallback = (granted: Boolean) -> Unit

/**
 * 申请权限帮助类，对应的activity类实现这个 PermissionRequester
 */
object PermissionHelper {

    //回调
    private val callbacks by lazy { HashMap<Int, PermissionCallback>() }

    private fun hasPermissions(
        context: Context,
        perms: Array<String>
    ): Boolean {
        //安卓6.0以上才有动态权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    /**
     * 申请权限，必须在 activity onRequestPermissionsResult 调用该类的onRequestPermissionsResult
     */
    fun requestPermissionsIfNeeded(
        activity: Activity,
        requestCode: Int,
        perms: Array<String>,
        callback: PermissionCallback
    ) {
        if (hasPermissions(activity, perms)) {
            callback(true)
        } else {
            callbacks[requestCode] = callback
            ActivityCompat.requestPermissions(activity, perms, requestCode)
        }
    }

    /**
     * 回调
     */
    private var permissionCallback: PermissionCallback? = null

    /**
     * 申请权限
     */
    fun requestPermissionsIfNeeded(
        requester: PermissionRequester,
        perms: Array<String>,
        callback: PermissionCallback
    ) {
        if (hasPermissions(requester.attachedActivity!!, perms)) {
            callback(true)
        } else {
            permissionCallback = callback
            requester.permissionLauncher.launch(perms)
        }
    }

    /**
     * 处理
     */
    fun onRequestMultiplePermissions(map: Map<String, Boolean>) {
        if (permissionCallback == null || map.isEmpty())
            return

        val granted = ArrayList<String>()
        val denied = ArrayList<String>()
        for (pair in map) {
            val perm = pair.key
            if (pair.value) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }
        permissionCallback!!(granted.isNotEmpty() && denied.isEmpty())
        permissionCallback = null
    }

    /**
     * 处理权限结果
     */
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val granted = ArrayList<String>()
        val denied = ArrayList<String>()
        for (i in permissions.indices) {
            val perm = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm)
            } else {
                denied.add(perm)
            }
        }

        val callback = callbacks[requestCode]
        if (callback != null) {
            callback(granted.isNotEmpty() && denied.isEmpty())
            callbacks.remove(requestCode)
        }
    }
}