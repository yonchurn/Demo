package com.common.base.image

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.common.base.loading.LoadingHelper
import com.common.base.loading.LoadingView
import com.luck.picture.lib.PictureSelectorCameraEmptyActivity
import com.luck.picture.lib.R
import com.luck.picture.lib.config.PictureSelectionConfig
import com.luck.picture.lib.entity.LocalMedia

class ImagePickerCameraEmptyActivity: PictureSelectorCameraEmptyActivity(), LoadingHelper, ImageCompressEngine {

    override var loadingView: LoadingView? = null
    override var loading = false

    override val attachedContext: Context
        get() = this
    override val pictureSelectionConfig: PictureSelectionConfig
        get() = config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = LayoutInflater.from(this).inflate(R.layout.picture_empty, null)
        setContentView(container)
    }

    override fun showPleaseDialog() {
        showLoading(container, 0)
    }

    override fun getResourceId(): Int {
        return 0
    }

    override fun dismissDialog() {
        hideLoading()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }

    override fun compressImage(result: MutableList<LocalMedia>?) {
        if (result == null) {
            onResult(result)
            return
        }
        showPleaseDialog()
        compressImage(result) {
            onResult(it)
        }
    }
}