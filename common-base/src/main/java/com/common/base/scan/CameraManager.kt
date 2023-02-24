package com.common.base.scan

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.util.Log
import com.common.base.helper.PermissionHelper
import com.google.zxing.Result
import com.common.base.utils.AlertUtils
import com.common.base.utils.AppUtils
import java.io.IOException
import kotlin.math.abs


/**
 * 相机管理
 */
@Suppress("deprecation")
class CameraManager(val fragment: ScanFragment) {

    //相机
    private var _camera: android.hardware.Camera? = null

    //自动聚焦回调
    private val autoFocusCallback: CameraAutoFocusCallback by lazy {
        CameraAutoFocusCallback()
    }

    //事件
    private val _cameraHandler = CameraHandler(this)

    //预览大小
    var surfaceWidth = 0
        private set

    var surfaceHeight = 0
        private set

    //预览回调
    private var previewCallback = CameraPreviewCallback(_cameraHandler, this)

    //是否正在暂停
    private var mPausing = false

    //是否正在预览
    private var mPreviewing = false

    //关联的
    private var mSurfaceTexture: SurfaceTexture? = null

    //回调
    var callback: Callback? = null

    //扫码框
    private var mScanRect: Rect? = null

    //相机是否已创建
    var isCameraInit = false
        private set

    //是否已提示弹窗
    private var _alert = false

    fun getScanRect(): Rect? {
        if (mScanRect == null && _camera != null) {
            mScanRect = Rect(callback!!.getScanRect(surfaceWidth, surfaceHeight))
            val size = _camera!!.parameters.previewSize
            val widthScale = size.height.toFloat() / surfaceWidth.toFloat()
            val heightScale = size.width.toFloat() / surfaceHeight.toFloat()
            mScanRect!!.apply {
                left = (left * widthScale).toInt()
                right = (right * widthScale).toInt()
                top = (top * heightScale).toInt()
                bottom = (bottom * heightScale).toInt()
            }
        }
        return mScanRect
    }

    //设置预览大小
    fun setPreviewSize(width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
    }

    //设置预览视图
    fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
        mSurfaceTexture = surfaceTexture
    }

    //获取当前预览大小
    fun getPreviewSize(): android.hardware.Camera.Size? {
        return if (_camera != null) {
            _camera!!.parameters.previewSize
        } else null
    }

    //获取预览格式
    fun getPreviewFormat(): Int {
        return if (_camera != null) {
            _camera!!.parameters.previewFormat
        } else 0
    }

    //获取预览格式字符串
    fun getPreviewFormatString(): String? {
        return if (_camera != null) {
            _camera!!.parameters.get("preview-format")
        } else null
    }

    //所需权限
    private val neededPermissions by lazy { arrayOf(Manifest.permission.CAMERA) }

    //打开相机
    fun openCamera() {
        PermissionHelper.requestPermissionsIfNeeded(fragment, neededPermissions) {
            if (it) {
                openCameraAfterGranted()
            } else {
                var alert = true
                if (callback != null) {
                    alert = !callback!!.onPermissionsDenied(neededPermissions)
                }
                if (alert) {
                    AppUtils.openAppSettings("扫一扫需要您的相机权限才能使用")
                }
            }
        }
    }

    private fun openCameraAfterGranted() {
        if (_camera == null) {
            _camera = android.hardware.Camera.open()
        }
        if (_camera == null) {
            AlertUtils.alert(title = "摄像头不可用", buttonTitles = arrayOf("确定"))
                .show(fragment.childFragmentManager)
        } else {
            try {
                _camera!!.apply {
                    setPreviewTexture(mSurfaceTexture)
                    setOptimalPreviewSize(surfaceWidth, surfaceHeight)
                    setDisplayOrientation(90)
                    setPreviewCallback(previewCallback)
                    startPreview()
                }

                mPreviewing = true
                startDecode()
                if (callback != null) {
                    callback!!.onCameraStart()
                }
                isCameraInit = true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("ScanFragment", "相机预览失败")
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
    }

    //暂停相机
    fun onPause() {
        if (_camera != null && mPreviewing) {
            mPausing = true
            mPreviewing = false
            stopDecode()
            stopFocus()
            _camera!!.stopPreview()
        }
    }

    //关闭相机
    fun onDestroy() {
        if (_camera != null) {
            stopDecode()
            mPreviewing = false
            _camera!!.stopPreview()
            _camera!!.setPreviewCallback(null)
            mPausing = false
            stopFocus()
            _camera!!.release()
            _camera = null
        }
    }

    //重启相机
    fun onResume() {
        if (mPausing && _camera != null && !mPreviewing) {
            _camera!!.startPreview()
            mPreviewing = true
            startDecode()
            if (callback != null) {
                callback!!.onCameraStart()
            }
        }
    }

    //聚焦
    fun autoFocus() {
        if (_camera != null && mPreviewing) {
            autoFocusCallback.startFocus(_cameraHandler)
            _camera!!.autoFocus(autoFocusCallback)
        }
    }

    //是否已聚焦
    fun isAutoFocusing(): Boolean {
        return autoFocusCallback.isAutoFocusing()
    }

    //停止聚焦
    private fun stopFocus() {
        autoFocusCallback.stopFocus()
    }

    //开始解码
    fun startDecode() {
        previewCallback.startDecode()
    }

    //停止解码
    private fun stopDecode() {
        previewCallback.stopDecode()
    }

    //设置开灯状态
    fun setOpenLamp(open: Boolean): String? {
        if (isCameraInit) {

            //判断设备是否支持闪光灯
            var support = false
            val featureInfos = fragment.context?.packageManager?.systemAvailableFeatures
            if (featureInfos != null) {
                for (info in featureInfos) {
                    if (PackageManager.FEATURE_CAMERA_FLASH == info.name) {
                        support = true
                        break
                    }
                }
            }
            if (support) {
                val parameters = _camera!!.parameters
                parameters.flashMode = if (open)
                    android.hardware.Camera.Parameters.FLASH_MODE_TORCH
                else android.hardware.Camera.Parameters.FLASH_MODE_AUTO

                _camera!!.parameters = parameters
                return null
            }
            return "该设备不支持开灯"
        }
        return "相机正在初始化"
    }

    //设置最优预览尺寸
    private fun setOptimalPreviewSize(width: Int, height: Int) {
        if (_camera != null) {
            val parameters = _camera!!.parameters
            val sizes = parameters.supportedPreviewSizes
            var landScapeWidth = width
            var landScapeHeight = height

            if (isPortrait()) {
                landScapeWidth = height
                landScapeHeight = width
            }

            var optimalSize: android.hardware.Camera.Size? = null

            //如果存在相等的尺寸 直接设置
            for (size in sizes) {
                if (size.width == landScapeWidth && size.height == landScapeHeight) {
                    optimalSize = size
                    break
                }
            }
            if (optimalSize == null) {

                //使用宽高比例最接近的
                val ratio = landScapeWidth.toFloat() / landScapeHeight.toFloat()
                var differ = Float.MAX_VALUE
                for (size in sizes) {
                    val r = size.width.toFloat() / size.height.toFloat()
                    val d = abs(ratio - r)
                    if (d < differ) {
                        differ = d
                        optimalSize = size
                    }
                }
            }
            if (optimalSize != null) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height)
                _camera!!.parameters = parameters
            }
        }
    }

    //调用扫码成功代理
    fun decodeSuccess(result: Result) {
        if (callback != null) {
            onPause()
            callback!!.onScanSuccess(result)
            callback!!.onCameraStop()
        }
    }

    //获取屏幕方向 判断是否是竖屏
    private fun isPortrait(): Boolean {
        val configuration = fragment.resources.configuration
        val orientation: Int = configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }

    //回调
    interface Callback {

        //没有权限 返回true 说明自己提示权限信息
        fun onPermissionsDenied(permissions: Array<String>): Boolean

        //获取扫描框位置 width、height预览surface的宽高
        fun getScanRect(width: Int, height: Int): Rect

        //解码成功
        fun onScanSuccess(result: Result)

        //开始
        fun onCameraStart()

        //停止
        fun onCameraStop()
    }
}