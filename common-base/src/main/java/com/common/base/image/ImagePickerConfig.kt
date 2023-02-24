package com.common.base.image

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.luck.picture.lib.*
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.style.PictureParameterStyle
import com.luck.picture.lib.tools.DoubleUtils
import java.lang.ref.WeakReference
import java.util.*

/**
 * 图片选择配置
 */
@Suppress("deprecation")
class ImagePickerConfig {

    private var mActivity: WeakReference<Activity>? = null
    private var mFragment: WeakReference<Fragment>? = null

    private constructor(activity: Activity): this(activity, null)

    private constructor(fragment: Fragment): this(fragment.activity, fragment)

    private constructor(activity: Activity?, fragment: Fragment?) {
        mActivity = WeakReference(activity)
        mFragment = WeakReference(fragment)
        themeStyle(R.style.picture_default_style)
    }

    companion object {
        /**
         * Start ImagePicker for Activity.
         *
         * @param activity
         * @return ImagePicker instance.
         */
        fun create(activity: Activity): ImagePickerConfig {
            return ImagePickerConfig(activity)
        }

        /**
         * Start ImagePicker for Fragment.
         *
         * @param fragment
         * @return ImagePicker instance.
         */
        fun create(fragment: Fragment): ImagePickerConfig {
            return ImagePickerConfig(fragment)
        }
    }

    /**
     * @param chooseMode Select the type of picture you want，all or Picture or Video .
     * @return LocalMedia PictureSelectionModel
     * Use [].
     */
    fun openGallery(chooseMode: Int): ImagePickerModel {
        return ImagePickerModel(this, chooseMode)
    }

    /**
     * @param chooseMode Select the type of picture you want，Picture or Video.
     * @return LocalMedia PictureSelectionModel
     * Use [].
     */
    fun openCamera(chooseMode: Int): ImagePickerModel {
        return ImagePickerModel(this, chooseMode, true)
    }

    /**
     * 外部预览时设置样式
     *
     * @param themeStyle
     * @return
     */
    fun themeStyle(themeStyle: Int): ImagePickerModel {
        return ImagePickerModel(this, PictureMimeType.ofImage())
            .theme(themeStyle)
    }

    /**
     * 外部预览时动态代码设置样式
     *
     * @param style
     * @return
     */
    fun setPictureStyle(style: PictureParameterStyle?): ImagePickerModel {
        return ImagePickerModel(this, PictureMimeType.ofImage())
            .setPictureStyle(style)
    }

    /**
     * @param data
     * @return Selector Multiple LocalMedia
     */
    fun obtainMultipleResult(data: Intent?): List<LocalMedia> {
        if (data != null) {
            val result: List<LocalMedia>? =
                data.getParcelableArrayListExtra(PictureConfig.EXTRA_RESULT_SELECTION)
            if(result != null){
                return result
            }
        }
        return ArrayList()
    }

    /**
     * @param data
     * @return Put image Intent Data
     */
    fun putIntentResult(data: List<LocalMedia?>?): Intent {
        return Intent().putParcelableArrayListExtra(
            PictureConfig.EXTRA_RESULT_SELECTION,
            data as ArrayList<out Parcelable?>?
        )
    }

    /**
     * @param bundle
     * @return get Selector  LocalMedia
     */
    fun obtainSelectorList(bundle: Bundle?): List<LocalMedia?>? {
        return bundle?.getParcelableArrayList(PictureConfig.EXTRA_SELECT_LIST)
    }

    /**
     * @param selectedImages
     * @return put Selector  LocalMedia
     */
    fun saveSelectorList(outState: Bundle, selectedImages: List<LocalMedia?>?) {
        outState.putParcelableArrayList(
            PictureConfig.EXTRA_SELECT_LIST,
            selectedImages as ArrayList<out Parcelable?>?
        )
    }

    /**
     * set preview image
     *
     * @param position
     * @param medias
     */
    fun externalPicturePreview(position: Int, medias: List<LocalMedia?>?, enterAnimation: Int) {
        if (!DoubleUtils.isFastDoubleClick()) {
            if (getActivity() != null) {
                val intent = Intent(getActivity(), PictureExternalPreviewActivity::class.java)
                intent.putParcelableArrayListExtra(
                    PictureConfig.EXTRA_PREVIEW_SELECT_LIST,
                    medias as ArrayList<out Parcelable?>?
                )
                intent.putExtra(PictureConfig.EXTRA_POSITION, position)
                getActivity()!!.startActivity(intent)
                getActivity()!!.overridePendingTransition(
                    if (enterAnimation != 0) enterAnimation else R.anim.picture_anim_enter,
                    R.anim.picture_anim_fade_in
                )
            } else {
                throw NullPointerException("Starting the PictureSelector Activity cannot be empty ")
            }
        }
    }

    /**
     * set preview image
     *
     * @param position
     * @param medias
     * @param directory_path
     */
    fun externalPicturePreview(
        position: Int,
        directory_path: String?,
        medias: List<LocalMedia?>?,
        enterAnimation: Int
    ) {
        if (!DoubleUtils.isFastDoubleClick()) {
            if (getActivity() != null) {
                val intent = Intent(getActivity(), PictureExternalPreviewActivity::class.java)
                intent.putParcelableArrayListExtra(
                    PictureConfig.EXTRA_PREVIEW_SELECT_LIST,
                    medias as ArrayList<out Parcelable?>?
                )
                intent.putExtra(PictureConfig.EXTRA_POSITION, position)
                intent.putExtra(PictureConfig.EXTRA_DIRECTORY_PATH, directory_path)
                getActivity()!!.startActivity(intent)
                getActivity()!!.overridePendingTransition(
                    if (enterAnimation != 0) enterAnimation else R.anim.picture_anim_enter,
                    R.anim.picture_anim_fade_in
                )
            } else {
                throw NullPointerException("Starting the PictureSelector Activity cannot be empty ")
            }
        }
    }

    /**
     * set preview video
     *
     * @param path
     */
    fun externalPictureVideo(path: String?) {
        if (!DoubleUtils.isFastDoubleClick()) {
            if (getActivity() != null) {
                val intent = Intent(getActivity(), PictureVideoPlayActivity::class.java)
                intent.putExtra(PictureConfig.EXTRA_VIDEO_PATH, path)
                intent.putExtra(PictureConfig.EXTRA_PREVIEW_VIDEO, true)
                getActivity()!!.startActivity(intent)
            } else {
                throw NullPointerException("Starting the PictureSelector Activity cannot be empty ")
            }
        }
    }

    /**
     * set preview audio
     *
     * @param path
     */
    fun externalPictureAudio(path: String?) {
        if (!DoubleUtils.isFastDoubleClick()) {
            if (getActivity() != null) {
                val intent = Intent(getActivity(), PicturePlayAudioActivity::class.java)
                intent.putExtra(PictureConfig.EXTRA_AUDIO_PATH, path)
                getActivity()!!.startActivity(intent)
                getActivity()!!.overridePendingTransition(R.anim.picture_anim_enter, 0)
            } else {
                throw NullPointerException("Starting the PictureSelector Activity cannot be empty ")
            }
        }
    }

    /**
     * @return Activity.
     */
    fun getActivity(): Activity? {
        return mActivity!!.get()
    }

    /**
     * @return Fragment.
     */
    fun getFragment(): Fragment? {
        return if (mFragment != null) mFragment!!.get() else null
    }
}