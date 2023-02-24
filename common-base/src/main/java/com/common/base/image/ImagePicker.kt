package com.common.base.image

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import com.common.base.R
import com.common.base.base.interf.ValueCallback
import com.common.base.utils.AlertUtils
import com.common.base.utils.StringUtils
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.listener.OnResultCallbackListener

/**
 * 选择图片回调
 */
typealias ImagePickerCallback = ValueCallback<List<ImageData>>

/**
 * 图片选择器
 */
class ImagePicker(val mode: Int = PictureConfig.MULTIPLE): OnResultCallbackListener<LocalMedia> {

    //回调
    private var callback: ImagePickerCallback? = null

    //配置
    var config: ValueCallback<ImagePickerModel>? = null

    fun pick(context: Context, count: Int, callback: ImagePickerCallback) {
        this.callback = callback
        AlertUtils.actionSheet(
            title = context.getString(R.string.select_photo),
            cancelButtonTitle = context.getString(R.string.cancel),
            buttonTitles = arrayOf(context.getString(R.string.camera), context.getString(R.string.album)),
            onItemClick = {
                when (it) {
                    0 -> {
                        val model = ImagePickerConfig
                            .create(context as Activity)
                            .openCamera(PictureMimeType.ofImage())
                            .setLanguage(LanguageConfig.ENGLISH)
                            .cropImageWideHigh(1080, 0)
                        open(model)
                    }
                    1 -> {
                        val model = ImagePickerConfig
                            .create(context as Activity)
                            .openGallery(PictureMimeType.ofImage())
                            .selectionMode(mode)
                            .maxSelectNum(count)
                            .imageSpanCount(4)
                            .isCamera(false)
                            .setLanguage(LanguageConfig.ENGLISH)
                            .cropImageWideHigh(1080, 0)
                        open(model)
                    }
                }
            }
        ).show()
    }

    private fun open(model: ImagePickerModel) {
        model.compressQuality(90)
            .compress(true)

        config?.also {
            it(model)
        }

        if (!model.isCompress) {
            model.isAndroidQTransform(true)
        }

        model.setUcropOptions()
        model.setLanguage(LanguageConfig.ENGLISH)
            .imageEngine(GlideEngine.sharedEngine)
            .forResult(this)
    }

    override fun onResult(result: MutableList<LocalMedia>?) {
        callback?.also {
            if (!result.isNullOrEmpty()) {
                val list = ArrayList<ImageData>()
                for (media in result) {
                    val path = if (media.isCompressed) {
                        media.compressPath
                    } else if (media.isCut) {
                        media.cutPath
                    } else if (!StringUtils.isEmpty(media.androidQToPath)) {
                        media.androidQToPath
                    } else {
                        media.realPath
                    }

                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(path, options)

                    list.add(ImageData(path, options.outWidth, options.outHeight))
                }
                it(list)
            }
        }
    }

    override fun onCancel() {

    }
}

class ImageData(val path: String, val width: Int, val height: Int)