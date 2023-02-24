package com.common.base.image

import android.content.Context
import com.common.base.loading.LoadingHelper
import com.common.base.loading.LoadingView
import com.luck.picture.lib.PictureSelectorActivity
import com.luck.picture.lib.config.PictureSelectionConfig
import com.luck.picture.lib.entity.LocalMedia

/**
 * 图片选择，主要是为了修改loading样式
 */
class ImagePickerActivity : PictureSelectorActivity(), LoadingHelper, ImageCompressEngine {

    override var loadingView: LoadingView? = null
    override var loading = false

    override val attachedContext: Context
        get() = this
    override val pictureSelectionConfig: PictureSelectionConfig
        get() = config

    override fun showPleaseDialog() {
        showLoading(container, 0)
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