package com.common.base.scan

import android.os.Handler


/**
 * 相机自动聚焦 循环延迟调用 让相机聚焦
 */
@Suppress("deprecation")
class CameraAutoFocusCallback : android.hardware.Camera.AutoFocusCallback {

    //自动聚焦间隔
    private val mAutoFocusInterval: Long = 15000

    //延迟
    private var _handler: Handler? = null

    override fun onAutoFocus(success: Boolean, camera: android.hardware.Camera?) {
        if (_handler != null) {
            _handler!!.sendEmptyMessageDelayed(CameraHandler.MESSAGE_AUTO_FOCUS, mAutoFocusInterval)
        }
    }

    //开始聚焦
    fun startFocus(handler: Handler) {
        _handler = handler
    }

    //是否已聚焦
    fun isAutoFocusing(): Boolean {
        return _handler != null
    }

    //停止聚焦
    fun stopFocus() {
        if (_handler != null) {
            _handler!!.removeMessages(CameraHandler.MESSAGE_AUTO_FOCUS)
            _handler = null
        }
    }
}