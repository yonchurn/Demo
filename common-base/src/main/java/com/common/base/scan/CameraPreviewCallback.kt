package com.common.base.scan

import android.os.Handler

/**
 * 相机预览回调
 */
@Suppress("deprecation")
class CameraPreviewCallback(val handler: Handler, val cameraManager: CameraManager) : android.hardware.Camera.PreviewCallback {

    //解码线程
    private var _decoder: ScanDecoder? = null

    //是否正在解码
    private var _decoding = false

    //开始解码
    fun startDecode() {
        if (_decoder == null) {
            _decoder = ScanDecoder(handler, cameraManager)
            _decoder!!.start()
        }
        _decoding = false
    }

    //停止解码
    fun stopDecode() {
        if (_decoder != null) {
            _decoder!!.stopDecode()
            _decoder = null
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: android.hardware.Camera?) {
        if (!cameraManager.isAutoFocusing()) {
            cameraManager.autoFocus()
        }
        if (!_decoding && _decoder != null) {
            _decoding = true
            _decoder!!.decode(data)
        }
    }
}