package com.common.base.scan

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.google.zxing.Result


/**
 * 相机事件回调
 */
class CameraHandler(val cameraManager: CameraManager) : Handler(Looper.getMainLooper()) {

    companion object{
        //事件类型

        //聚焦
        const val MESSAGE_AUTO_FOCUS = 1

        //解码成功
        const val MESSAGE_DECODE_SUCCESS = 2

        //解码失败
        const val MESSAGE_DECODE_FAIL = 3
    }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MESSAGE_AUTO_FOCUS -> {
                cameraManager.autoFocus()
            }
            MESSAGE_DECODE_FAIL -> {
                cameraManager.startDecode()
            }
            MESSAGE_DECODE_SUCCESS -> {
                cameraManager.decodeSuccess(msg.obj as Result)
            }
        }
    }
}