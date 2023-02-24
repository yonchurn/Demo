package com.common.base.scan

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultLauncher
import com.common.base.R
import com.common.base.base.fragment.BaseFragment
import com.common.base.base.interf.PermissionRequester
import com.common.base.base.widget.BaseContainer
import com.common.base.utils.ViewUtils


/**
 * 扫一扫
 */
abstract class ScanFragment: BaseFragment(), PermissionRequester, TextureView.SurfaceTextureListener, CameraManager.Callback {

    //相机管理
    private lateinit var cameraManager: CameraManager

    //是否正在暂停
    private var _pausing = false

    private lateinit var textureView: TextureView

    override lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    final override fun initialize(inflater: LayoutInflater, container: BaseContainer, saveInstanceState: Bundle?) {
        setContainerContentView(R.layout.scan_fragment)
        val frameLayout = getContentView(inflater, getContainerContentView() as FrameLayout) as FrameLayout
        val view = getContentView(inflater, frameLayout)
        if (view.parent == null || view.parent !== frameLayout) {
            ViewUtils.removeFromParent(view)
            frameLayout.addView(view)
        }

        textureView = findViewById(R.id.textureView)!!

        cameraManager = CameraManager(this)
        cameraManager.callback = this
        textureView.surfaceTextureListener = this
    }


    override fun onResume() {
        super.onResume()
        cameraManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraManager.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.onDestroy()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        cameraManager.setPreviewSize(width, height)
        cameraManager.setSurfaceTexture(surface)
        view?.postDelayed({
            cameraManager.openCamera()
        }, 300)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    //暂停相机
    protected fun pauseCamera() {
        if (cameraManager.isCameraInit) {
            _pausing = true
            cameraManager.onPause()
        }
    }

    //重启相机
    protected fun resumeCamera() {
        if (_pausing) {
            cameraManager.onResume()
            _pausing = false
        }
    }

    //设置开灯状态
    protected fun setOpenLamp(open: Boolean): String? {
        return cameraManager.setOpenLamp(open)
    }

    //没有权限 返回true 说明自己提示权限信息
    override fun onPermissionsDenied(permissions: Array<String>): Boolean {
        return false
    }

    //获取内容视图
    abstract fun getContentView(inflater: LayoutInflater, container: FrameLayout): View
}