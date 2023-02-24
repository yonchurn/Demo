package com.common.base.image

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Build.VERSION_CODES
import android.text.TextUtils
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import com.luck.picture.lib.*
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.PictureSelectionConfig
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnCustomCameraInterfaceListener
import com.luck.picture.lib.listener.OnCustomImagePreviewCallback
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.luck.picture.lib.listener.OnVideoSelectedPlayCallback
import com.luck.picture.lib.style.PictureCropParameterStyle
import com.luck.picture.lib.style.PictureParameterStyle
import com.luck.picture.lib.style.PictureSelectorUIStyle
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.luck.picture.lib.tools.DoubleUtils
import com.luck.picture.lib.tools.SdkVersionUtils
import com.yalantis.ucrop.UCrop
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author：luck
 * @date：2017-5-24 21:30
 * @describe：ImagePickerModel
 */
@Suppress("deprecation")
class ImagePickerModel(val pickerConfig: ImagePickerConfig, chooseMode: Int, camera: Boolean = false) {
    private val selectionConfig = PictureSelectionConfig.getCleanInstance()

    init {
        val options = UCrop.Options()
        options.setMaxBitmapSize(2048)
        selectionConfig.uCropOptions = options

        selectionConfig.camera = camera
        selectionConfig.chooseMode = chooseMode
        selectionConfig.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        PictureSelectionConfig.imageEngine = GlideEngine.sharedEngine
    }

    /**
     * @param themeStyleId ImagePicker Theme style
     * @return ImagePickerModel
     * Use [R.style.picture_default_style]
     */
    fun theme(@StyleRes themeStyleId: Int): ImagePickerModel {
        selectionConfig!!.themeStyleId = themeStyleId
        return this
    }

    /**
     * Setting ImagePicker UI Style
     *
     * @param uiStyle
     *
     *
     * [PictureSelectorUIStyle]
     *
     * @return
     */
    fun setPictureUIStyle(uiStyle: PictureSelectorUIStyle?): ImagePickerModel {
        if (uiStyle != null) {
            PictureSelectionConfig.uiStyle = uiStyle
            if (!selectionConfig!!.isWeChatStyle) {
                selectionConfig.isWeChatStyle = PictureSelectionConfig.uiStyle.isNewSelectStyle
            }
        }
        return this
    }

    /**
     * @param language Language
     * @return ImagePickerModel
     */
    fun setLanguage(language: Int): ImagePickerModel {
        selectionConfig!!.language = language
        return this
    }

    /**
     * Change the desired orientation of this activity.  If the activity
     * is currently in the foreground or otherwise impacting the screen
     * orientation, the screen will immediately be changed (possibly causing
     * the activity to be restarted). Otherwise, this will be used the next
     * time the activity is visible.
     *
     * @param requestedOrientation An orientation constant as used in
     */
    fun setRequestedOrientation(requestedOrientation: Int): ImagePickerModel {
        selectionConfig!!.requestedOrientation = requestedOrientation
        return this
    }

    /**
     * @param engine Image Load the engine
     * @return Use [].
     */
    @Deprecated("")
    fun loadImageEngine(engine: ImageEngine): ImagePickerModel {
        if (PictureSelectionConfig.imageEngine !== engine) {
            PictureSelectionConfig.imageEngine = engine
        }
        return this
    }

    /**
     * @param engine Image Load the engine
     * @return
     */
    fun imageEngine(engine: ImageEngine): ImagePickerModel {
        if (PictureSelectionConfig.imageEngine !== engine) {
            PictureSelectionConfig.imageEngine = engine
        }
        return this
    }

    fun setUcropOptions() {
        val config = selectionConfig
        val options = selectionConfig.uCropOptions
        options.setCircleDimmedLayer(config.circleDimmedLayer)
        options.setDimmedLayerColor(config.circleDimmedColor)
        options.setShowCropFrame(config.showCropFrame)
        options.setShowCropGrid(config.showCropGrid)
        options.setHideBottomControls(config.hideBottomControls)
        options.setCompressionQuality(config.cropCompressQuality)
        options.setFreeStyleCropEnabled(config.freeStyleCropEnabled)
        options.withAspectRatio(config.aspect_ratio_x.toFloat(), config.aspect_ratio_y.toFloat())
        if (config.cropWidth > 0 && config.cropHeight > 0) {
            options.withMaxResultSize(config.cropWidth, config.cropHeight)
        }
    }

    /**
     * @param selectionMode ImagePicker Selection model and PictureConfig.MULTIPLE or PictureConfig.SINGLE
     * @return
     */
    fun selectionMode(selectionMode: Int): ImagePickerModel {
        selectionConfig!!.selectionMode = selectionMode
        return this
    }

    /**
     * @param isWeChatStyle Select style with or without WeChat enabled
     * @return
     */
    fun isWeChatStyle(isWeChatStyle: Boolean): ImagePickerModel {
        selectionConfig!!.isWeChatStyle = isWeChatStyle
        return this
    }

    /**
     * @param isUseCustomCamera Whether to use a custom camera
     * @return
     */
    fun isUseCustomCamera(isUseCustomCamera: Boolean): ImagePickerModel {
        selectionConfig!!.isUseCustomCamera =
            Build.VERSION.SDK_INT > VERSION_CODES.KITKAT && isUseCustomCamera
        return this
    }

    /**
     * @param callback Provide video playback control，Users are free to customize the video display interface
     * @return
     */
    fun bindCustomPlayVideoCallback(callback: OnVideoSelectedPlayCallback<LocalMedia>): ImagePickerModel {
        PictureSelectionConfig.customVideoPlayCallback = callback
        return this
    }

    /**
     * @param callback Custom preview callback function
     * @return
     */
    fun bindCustomPreviewCallback(callback: OnCustomImagePreviewCallback<LocalMedia>): ImagePickerModel {
        PictureSelectionConfig.onCustomImagePreviewCallback = callback
        return this
    }

    /**
     * # The developer provides an additional callback interface to the user where the user can perform some custom actions
     * {link 如果是自定义相机则必须使用.startActivityForResult(this,PictureConfig.REQUEST_CAMERA);方式启动否则ImagePicker处理不了相机后的回调}
     *
     * @param listener
     * @return Use ${bindCustomCameraInterfaceListener}
     */
    @Deprecated("")
    fun bindImagePickerInterfaceListener(listener: OnCustomCameraInterfaceListener): ImagePickerModel {
        PictureSelectionConfig.onCustomCameraInterfaceListener = WeakReference(listener).get()
        return this
    }

    /**
     * # The developer provides an additional callback interface to the user where the user can perform some custom actions
     * {link 如果是自定义相机则必须使用.startActivityForResult(this,PictureConfig.REQUEST_CAMERA);方式启动否则ImagePicker处理不了相机后的回调}
     *
     * @param listener
     * @return
     */
    fun bindCustomCameraInterfaceListener(listener: OnCustomCameraInterfaceListener): ImagePickerModel {
        PictureSelectionConfig.onCustomCameraInterfaceListener = WeakReference(listener).get()
        return this
    }

    /**
     * @param buttonFeatures Set the record button function
     * # 具体参考 CustomCameraView.BUTTON_STATE_BOTH、BUTTON_STATE_ONLY_CAPTURE、BUTTON_STATE_ONLY_RECORDER
     * @return
     */
    fun setButtonFeatures(buttonFeatures: Int): ImagePickerModel {
        selectionConfig!!.buttonFeatures = buttonFeatures
        return this
    }

    /**
     * Set Custom Camera Photo Loading color
     *
     * @param color
     * @return
     */
    fun setCaptureLoadingColor(color: Int): ImagePickerModel {
        selectionConfig!!.captureLoadingColor = color
        return this
    }

    /**
     * @param enableCrop Do you want to start cutting ?
     * @return Use {link .isEnableCrop()}
     */
    @Deprecated("")
    fun enableCrop(enableCrop: Boolean): ImagePickerModel {
        selectionConfig!!.enableCrop = enableCrop
        return this
    }

    /**
     * @param enableCrop Do you want to start cutting ?
     * @return
     */
    fun isEnableCrop(enableCrop: Boolean): ImagePickerModel {
        selectionConfig!!.enableCrop = enableCrop
        return this
    }

    /**
     * @param uCropOptions UCrop parameter configuration is provided
     * @return
     */
    fun basicUCropConfig(uCropOptions: UCrop.Options?): ImagePickerModel {
        selectionConfig!!.uCropOptions = uCropOptions
        return this
    }

    /**
     * @param isMultipleSkipCrop Whether multiple images can be skipped when cropping
     * @return
     */
    fun isMultipleSkipCrop(isMultipleSkipCrop: Boolean): ImagePickerModel {
        selectionConfig!!.isMultipleSkipCrop = isMultipleSkipCrop
        return this
    }

    /**
     * @param enablePreviewAudio [isEnablePreviewAudio][use]
     * @return
     */
    @Deprecated("")
    fun enablePreviewAudio(enablePreviewAudio: Boolean): ImagePickerModel {
        selectionConfig!!.enablePreviewAudio = enablePreviewAudio
        return this
    }

    /**
     * @param enablePreviewAudio
     * @return
     */
    fun isEnablePreviewAudio(enablePreviewAudio: Boolean): ImagePickerModel {
        selectionConfig!!.enablePreviewAudio = enablePreviewAudio
        return this
    }

    /**
     * @param freeStyleCropEnabled Crop frame is move ?
     * @return
     */
    fun freeStyleCropEnabled(freeStyleCropEnabled: Boolean): ImagePickerModel {
        selectionConfig!!.freeStyleCropEnabled = freeStyleCropEnabled
        return this
    }

    /**
     * @param scaleEnabled Crop frame is zoom ?
     * @return
     */
    fun scaleEnabled(scaleEnabled: Boolean): ImagePickerModel {
        selectionConfig!!.scaleEnabled = scaleEnabled
        return this
    }

    /**
     * @param rotateEnabled Crop frame is rotate ?
     * @return
     */
    fun rotateEnabled(rotateEnabled: Boolean): ImagePickerModel {
        selectionConfig!!.rotateEnabled = rotateEnabled
        return this
    }

    /**
     * @param circleDimmedLayer Circular head cutting
     * @return
     */
    fun circleDimmedLayer(circleDimmedLayer: Boolean): ImagePickerModel {
        selectionConfig!!.circleDimmedLayer = circleDimmedLayer
        return this
    }

    /**
     * @param circleDimmedColor setCircleDimmedColor
     * @return
     */
    @Deprecated("")
    fun setCircleDimmedColor(circleDimmedColor: Int): ImagePickerModel {
        selectionConfig!!.circleDimmedColor = circleDimmedColor
        return this
    }

    /**
     * @param dimmedColor
     * @return
     */
    fun setCropDimmedColor(dimmedColor: Int): ImagePickerModel {
        selectionConfig!!.circleDimmedColor = dimmedColor
        return this
    }

    /**
     * @param circleDimmedBorderColor setCircleDimmedBorderColor
     * @return
     */
    fun setCircleDimmedBorderColor(circleDimmedBorderColor: Int): ImagePickerModel {
        selectionConfig!!.circleDimmedBorderColor = circleDimmedBorderColor
        return this
    }

    /**
     * @param circleStrokeWidth setCircleStrokeWidth
     * @return
     */
    fun setCircleStrokeWidth(circleStrokeWidth: Int): ImagePickerModel {
        selectionConfig!!.circleStrokeWidth = circleStrokeWidth
        return this
    }

    /**
     * @param showCropFrame Whether to show crop frame
     * @return
     */
    fun showCropFrame(showCropFrame: Boolean): ImagePickerModel {
        selectionConfig!!.showCropFrame = showCropFrame
        return this
    }

    /**
     * @param showCropGrid Whether to show CropGrid
     * @return
     */
    fun showCropGrid(showCropGrid: Boolean): ImagePickerModel {
        selectionConfig!!.showCropGrid = showCropGrid
        return this
    }

    /**
     * @param hideBottomControls Whether is Clipping function bar
     * 单选有效
     * @return
     */
    fun hideBottomControls(hideBottomControls: Boolean): ImagePickerModel {
        selectionConfig!!.hideBottomControls = hideBottomControls
        return this
    }

    /**
     * @param aspect_ratio_x Crop Proportion x
     * @param aspect_ratio_y Crop Proportion y
     * @return
     */
    fun withAspectRatio(aspect_ratio_x: Int, aspect_ratio_y: Int): ImagePickerModel {
        selectionConfig!!.aspect_ratio_x = aspect_ratio_x
        selectionConfig.aspect_ratio_y = aspect_ratio_y
        return this
    }

    /**
     * @param isWithVideoImage Whether the pictures and videos can be selected together
     * @return
     */
    fun isWithVideoImage(isWithVideoImage: Boolean): ImagePickerModel {
        selectionConfig!!.isWithVideoImage =
            selectionConfig.selectionMode != PictureConfig.SINGLE && selectionConfig.chooseMode == PictureMimeType.ofAll() && isWithVideoImage
        return this
    }

    /**
     * When the maximum number of choices is reached, does the list enable the mask effect
     *
     * @param isMaxSelectEnabledMask
     * @return
     */
    fun isMaxSelectEnabledMask(isMaxSelectEnabledMask: Boolean): ImagePickerModel {
        selectionConfig!!.isMaxSelectEnabledMask = isMaxSelectEnabledMask
        return this
    }

    /**
     * @param maxSelectNum ImagePicker max selection
     * @return
     */
    fun maxSelectNum(maxSelectNum: Int): ImagePickerModel {
        selectionConfig!!.maxSelectNum = maxSelectNum
        return this
    }

    /**
     * @param minSelectNum ImagePicker min selection
     * @return
     */
    fun minSelectNum(minSelectNum: Int): ImagePickerModel {
        selectionConfig!!.minSelectNum = minSelectNum
        return this
    }

    /**
     * @param maxVideoSelectNum ImagePicker video max selection
     * @return
     */
    fun maxVideoSelectNum(maxVideoSelectNum: Int): ImagePickerModel {
        selectionConfig!!.maxVideoSelectNum =
            if (selectionConfig.chooseMode == PictureMimeType.ofVideo()) 0 else maxVideoSelectNum
        return this
    }

    /**
     * @param minVideoSelectNum ImagePicker video min selection
     * @return
     */
    fun minVideoSelectNum(minVideoSelectNum: Int): ImagePickerModel {
        selectionConfig!!.minVideoSelectNum = minVideoSelectNum
        return this
    }


    /**
     * By clicking the title bar consecutively, RecyclerView automatically rolls back to the top
     *
     * @param isAutomaticTitleRecyclerTop
     * @return
     */
    fun isAutomaticTitleRecyclerTop(isAutomaticTitleRecyclerTop: Boolean): ImagePickerModel {
        selectionConfig!!.isAutomaticTitleRecyclerTop = isAutomaticTitleRecyclerTop
        return this
    }

    /**
     * @param isSingleDirectReturn whether to return directly
     * @return
     */
    fun isSingleDirectReturn(isSingleDirectReturn: Boolean): ImagePickerModel {
        selectionConfig!!.isSingleDirectReturn = selectionConfig.selectionMode == PictureConfig.SINGLE && isSingleDirectReturn
        selectionConfig.isOriginalControl =
            (selectionConfig.selectionMode != PictureConfig.SINGLE || !isSingleDirectReturn) && selectionConfig.isOriginalControl
        return this
    }

    /**
     * Whether to turn on paging mode
     *
     * @param isPageStrategy
     * @param pageSize       Maximum number of pages [is preferably no less than 20]
     * @return
     */
    fun isPageStrategy(isPageStrategy: Boolean, pageSize: Int): ImagePickerModel {
        selectionConfig!!.isPageStrategy = isPageStrategy
        selectionConfig.pageSize =
            if (pageSize < PictureConfig.MIN_PAGE_SIZE) PictureConfig.MAX_PAGE_SIZE else pageSize
        return this
    }

    /**
     * Whether to turn on paging mode
     *
     * @param isPageStrategy
     * @param pageSize            Maximum number of pages [is preferably no less than 20]
     * @param isFilterInvalidFile Whether to filter invalid files [of the query performance is consumed,Especially on the Q version]
     * @return
     */
    fun isPageStrategy(
        isPageStrategy: Boolean,
        pageSize: Int,
        isFilterInvalidFile: Boolean
    ): ImagePickerModel {
        selectionConfig!!.isPageStrategy = isPageStrategy
        selectionConfig.pageSize =
            if (pageSize < PictureConfig.MIN_PAGE_SIZE) PictureConfig.MAX_PAGE_SIZE else pageSize
        selectionConfig.isFilterInvalidFile = isFilterInvalidFile
        return this
    }

    /**
     * Whether to turn on paging mode
     *
     * @param isPageStrategy
     * @return
     */
    fun isPageStrategy(isPageStrategy: Boolean): ImagePickerModel {
        selectionConfig!!.isPageStrategy = isPageStrategy
        return this
    }

    /**
     * Whether to turn on paging mode
     *
     * @param isPageStrategy
     * @param isFilterInvalidFile Whether to filter invalid files [of the query performance is consumed,Especially on the Q version]
     * @return
     */
    fun isPageStrategy(
        isPageStrategy: Boolean,
        isFilterInvalidFile: Boolean
    ): ImagePickerModel {
        selectionConfig!!.isPageStrategy = isPageStrategy
        selectionConfig.isFilterInvalidFile = isFilterInvalidFile
        return this
    }

    /**
     * @param videoQuality video quality and 0 or 1
     * @return
     */
    fun videoQuality(videoQuality: Int): ImagePickerModel {
        selectionConfig!!.videoQuality = videoQuality
        return this
    }

    /**
     *
     *
     * if Android SDK >=Q Please use the video/mp4 or video/jpeg ... PictureMimeType.MP4_Q or PictureMimeType.PNG_Q
     * else PictureMimeType.PNG or PictureMimeType.JPEG
     *
     *
     * @param type ImagePicker media format
     * @return
     */
    fun imageFormat(type: String?): ImagePickerModel {
        var suffixType = type
        if (SdkVersionUtils.isQ() || SdkVersionUtils.isR()) {
            if (TextUtils.equals(suffixType, PictureMimeType.PNG)) {
                suffixType = PictureMimeType.PNG_Q
            }
            if (TextUtils.equals(suffixType, PictureMimeType.JPEG)) {
                suffixType = PictureMimeType.JPEG_Q
            }
            if (TextUtils.equals(suffixType, PictureMimeType.MP4)) {
                suffixType = PictureMimeType.MP4_Q
            }
        }
        selectionConfig!!.suffixType = suffixType
        return this
    }

    /**
     * @param cropWidth  crop width
     * @param cropHeight crop height
     * @return this
     */
    @Deprecated(
        """Crop image output width and height
      {@link cropImageWideHigh()}"""
    )
    fun cropWH(cropWidth: Int, cropHeight: Int): ImagePickerModel {
        selectionConfig!!.cropWidth = cropWidth
        selectionConfig.cropHeight = cropHeight
        return this
    }

    /**
     * @param cropWidth  crop width
     * @param cropHeight crop height
     * @return this
     */
    fun cropImageWideHigh(cropWidth: Int, cropHeight: Int): ImagePickerModel {
        selectionConfig!!.cropWidth = cropWidth
        selectionConfig.cropHeight = cropHeight
        return this
    }

    /**
     * @param videoMaxSecond selection video max second
     * @return
     */
    fun videoMaxSecond(videoMaxSecond: Int): ImagePickerModel {
        selectionConfig!!.videoMaxSecond = videoMaxSecond * 1000
        return this
    }

    /**
     * @param videoMinSecond selection video min second
     * @return
     */
    fun videoMinSecond(videoMinSecond: Int): ImagePickerModel {
        selectionConfig!!.videoMinSecond = videoMinSecond * 1000
        return this
    }

    /**
     * @param recordVideoSecond video record second
     * @return
     */
    fun recordVideoSecond(recordVideoSecond: Int): ImagePickerModel {
        selectionConfig!!.recordVideoSecond = recordVideoSecond
        return this
    }

    /**
     * @param recordVideoMinSecond video record second
     * @return
     */
    fun recordVideoMinSecond(recordVideoMinSecond: Int): ImagePickerModel {
        selectionConfig!!.recordVideoMinSecond = recordVideoMinSecond
        return this
    }

    /**
     * @param imageSpanCount ImagePicker image span count
     * @return
     */
    fun imageSpanCount(imageSpanCount: Int): ImagePickerModel {
        selectionConfig!!.imageSpanCount = imageSpanCount
        return this
    }

    /**
     * @param size than how many KB images are not compressed
     * @return
     */
    fun minimumCompressSize(size: Int): ImagePickerModel {
        selectionConfig!!.minimumCompressSize = size
        return this
    }

    /**
     * @param compressQuality crop compress quality default 90
     * @return 请使用 cutOutQuality();方法
     */
    @Deprecated("")
    fun cropCompressQuality(compressQuality: Int): ImagePickerModel {
        selectionConfig!!.cropCompressQuality = compressQuality
        return this
    }

    /**
     * @param cutQuality crop compress quality default 90
     * @return
     */
    fun cutOutQuality(cutQuality: Int): ImagePickerModel {
        selectionConfig!!.cropCompressQuality = cutQuality
        return this
    }

    /**
     * @param isCompress Whether to open compress
     * @return
     */
    fun compress(isCompress: Boolean): ImagePickerModel {
        selectionConfig!!.isCompress = isCompress
        return this
    }

    val isCompress: Boolean
        get() = selectionConfig!!.isCompress

    /**
     * @param compressQuality Image compressed output quality
     * @return
     */
    fun compressQuality(compressQuality: Int): ImagePickerModel {
        selectionConfig!!.compressQuality = compressQuality
        return this
    }

    /**
     * @param returnEmpty No data can be returned
     * @return
     */
    fun isReturnEmpty(returnEmpty: Boolean): ImagePickerModel {
        selectionConfig!!.returnEmpty = returnEmpty
        return this
    }

    /**
     * @param synOrAsy Synchronous or asynchronous compression
     * @return
     */
    fun synOrAsy(synOrAsy: Boolean): ImagePickerModel {
        selectionConfig!!.synOrAsy = synOrAsy
        return this
    }

    /**
     * @param focusAlpha After compression, the transparent channel is retained
     * @return
     */
    fun compressFocusAlpha(focusAlpha: Boolean): ImagePickerModel {
        selectionConfig!!.focusAlpha = focusAlpha
        return this
    }

    /**
     * After recording with the system camera, does it support playing the video immediately using the system player
     *
     * @param isQuickCapture
     * @return
     */
    fun isQuickCapture(isQuickCapture: Boolean): ImagePickerModel {
        selectionConfig!!.isQuickCapture = isQuickCapture
        return this
    }

    /**
     * @param isOriginalControl Whether the original image is displayed
     * @return
     */
    fun isOriginalImageControl(isOriginalControl: Boolean): ImagePickerModel {
        selectionConfig!!.isOriginalControl = (!selectionConfig.camera
                && selectionConfig.chooseMode != PictureMimeType.ofVideo() && selectionConfig.chooseMode != PictureMimeType.ofAudio() && isOriginalControl)
        return this
    }

    /**
     * @param path save path
     * @return
     */
    fun compressSavePath(path: String?): ImagePickerModel {
        selectionConfig!!.compressSavePath = path
        return this
    }

    /**
     * Camera custom local file name
     * # Such as xxx.png
     *
     * @param fileName
     * @return
     */
    fun cameraFileName(fileName: String?): ImagePickerModel {
        selectionConfig!!.cameraFileName = fileName
        return this
    }

    /**
     * crop custom local file name
     * # Such as xxx.png
     *
     * @param renameCropFileName
     * @return
     */
    fun renameCropFileName(renameCropFileName: String?): ImagePickerModel {
        selectionConfig!!.renameCropFileName = renameCropFileName
        return this
    }

    /**
     * custom compress local file name
     * # Such as xxx.png
     *
     * @param renameFile
     * @return
     */
    fun renameCompressFile(renameFile: String?): ImagePickerModel {
        selectionConfig!!.renameCompressFileName = renameFile
        return this
    }

    /**
     * @param zoomAnim Picture list zoom anim
     * @return
     */
    fun isZoomAnim(zoomAnim: Boolean): ImagePickerModel {
        selectionConfig!!.zoomAnim = zoomAnim
        return this
    }

    /**
     * @param previewEggs preview eggs  It doesn't make much sense
     * @return Use {link .isPreviewEggs()}
     */
    @Deprecated("")
    fun previewEggs(previewEggs: Boolean): ImagePickerModel {
        selectionConfig!!.previewEggs = previewEggs
        return this
    }

    /**
     * @param previewEggs preview eggs  It doesn't make much sense
     * @return
     */
    fun isPreviewEggs(previewEggs: Boolean): ImagePickerModel {
        selectionConfig!!.previewEggs = previewEggs
        return this
    }

    /**
     * @param isCamera Whether to open camera button
     * @return
     */
    fun isCamera(isCamera: Boolean): ImagePickerModel {
        selectionConfig!!.isCamera = isCamera
        return this
    }

    /**
     * Extra used with [+  File.separator + &quot;CustomCamera&quot; + File.separator][.Environment.getExternalStorageDirectory]  to indicate that
     *
     * @param outPutCameraPath Camera save path 只支持Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
     * @return
     */
    fun setOutputCameraPath(outPutCameraPath: String?): ImagePickerModel {
        selectionConfig!!.outPutCameraPath = outPutCameraPath
        return this
    }

    /**
     * # file size The unit is M
     *
     * @param fileMSize Filter max file size
     * Use []
     * @return
     */
    @Deprecated("")
    fun queryFileSize(fileMSize: Float): ImagePickerModel {
        selectionConfig!!.filterFileSize = fileMSize
        return this
    }

    /**
     * # file size The unit is KB
     *
     * @param fileKbSize Filter max file size
     * @return
     */
    fun filterMaxFileSize(fileKbSize: Long): ImagePickerModel {
        if (fileKbSize >= PictureConfig.MB) {
            selectionConfig!!.filterMaxFileSize = fileKbSize
        } else {
            selectionConfig!!.filterMaxFileSize = fileKbSize * 1024
        }
        return this
    }

    /**
     * # file size The unit is KB
     *
     * @param fileKbSize Filter min file size
     * @return
     */
    fun filterMinFileSize(fileKbSize: Long): ImagePickerModel {
        if (fileKbSize >= PictureConfig.MB) {
            selectionConfig!!.filterMinFileSize = fileKbSize
        } else {
            selectionConfig!!.filterMinFileSize = fileKbSize * 1024
        }
        return this
    }

    /**
     * query specified mimeType
     *
     * @param mimeTypes Use example [{]
     * @return
     */
    fun queryMimeTypeConditions(vararg mimeTypes: String?): ImagePickerModel {
        if (!mimeTypes.isNullOrEmpty()) {
            selectionConfig!!.queryMimeTypeHashSet = HashSet(listOf(*mimeTypes))
        } else {
            selectionConfig!!.queryMimeTypeHashSet = null
        }
        return this
    }

    /**
     * @param isGif Whether to open gif
     * @return
     */
    fun isGif(isGif: Boolean): ImagePickerModel {
        selectionConfig!!.isGif = isGif
        return this
    }

    /**
     * @param isWebp Whether to open .webp
     * @return
     */
    fun isWebp(isWebp: Boolean): ImagePickerModel {
        selectionConfig!!.isWebp = isWebp
        return this
    }

    /**
     * @param isBmp Whether to open .isBmp
     * @return
     */
    fun isBmp(isBmp: Boolean): ImagePickerModel {
        selectionConfig!!.isBmp = isBmp
        return this
    }

    /**
     * @param enablePreview Do you want to preview the picture?
     * @return Use {link .isPreviewImage()}
     */
    @Deprecated("")
    fun previewImage(enablePreview: Boolean): ImagePickerModel {
        selectionConfig!!.enablePreview = enablePreview
        return this
    }

    /**
     * @param enablePreview Do you want to preview the picture?
     * @return
     */
    fun isPreviewImage(enablePreview: Boolean): ImagePickerModel {
        selectionConfig!!.enablePreview = enablePreview
        return this
    }

    /**
     * @param enPreviewVideo Do you want to preview the video?
     * @return Use {link .isPreviewVideo()}
     */
    @Deprecated("")
    fun previewVideo(enPreviewVideo: Boolean): ImagePickerModel {
        selectionConfig!!.enPreviewVideo = enPreviewVideo
        return this
    }

    /**
     * @param enPreviewVideo Do you want to preview the video?
     * @return
     */
    fun isPreviewVideo(enPreviewVideo: Boolean): ImagePickerModel {
        selectionConfig!!.enPreviewVideo = enPreviewVideo
        return this
    }

    /**
     * @param isNotPreviewDownload Previews do not show downloads
     * @return
     */
    fun isNotPreviewDownload(isNotPreviewDownload: Boolean): ImagePickerModel {
        selectionConfig!!.isNotPreviewDownload = isNotPreviewDownload
        return this
    }

    /**
     * @param specifiedFormat get image format
     * Use []
     * @return
     */
    @Deprecated("")
    fun querySpecifiedFormatSuffix(specifiedFormat: String?): ImagePickerModel {
        selectionConfig!!.specifiedFormat = specifiedFormat
        return this
    }

    /**
     * @param openClickSound Whether to open click voice
     * @return Use {link .isOpenClickSound()}
     */
    @Deprecated("")
    fun openClickSound(openClickSound: Boolean): ImagePickerModel {
        selectionConfig!!.openClickSound = !selectionConfig.camera && openClickSound
        return this
    }

    /**
     * @param openClickSound Whether to open click voice
     * @return
     */
    fun isOpenClickSound(openClickSound: Boolean): ImagePickerModel {
        selectionConfig!!.openClickSound = !selectionConfig.camera && openClickSound
        return this
    }

    /**
     * 是否可拖动裁剪框(setFreeStyleCropEnabled 为true 有效)
     */
    fun isDragFrame(isDragFrame: Boolean): ImagePickerModel {
        selectionConfig!!.isDragFrame = isDragFrame
        return this
    }

    /**
     * Whether the multi-graph clipping list is animated or not
     *
     * @param isAnimation
     * @return
     */
    fun isMultipleRecyclerAnimation(isAnimation: Boolean): ImagePickerModel {
        selectionConfig!!.isMultipleRecyclerAnimation = isAnimation
        return this
    }

    /**
     * 设置摄像头方向(前后 默认后置)
     */
    fun isCameraAroundState(isCameraAroundState: Boolean): ImagePickerModel {
        selectionConfig!!.isCameraAroundState = isCameraAroundState
        return this
    }

    /**
     * @param selectionMedia Select the selected picture set
     * @return Use {link .selectionData()}
     */
    @Deprecated("")
    fun selectionMedia(selectionMedia: List<LocalMedia?>?): ImagePickerModel {
        if (selectionConfig!!.selectionMode == PictureConfig.SINGLE && selectionConfig.isSingleDirectReturn) {
            selectionConfig.selectionMedias = null
        } else {
            selectionConfig.selectionMedias = selectionMedia
        }
        return this
    }

    /**
     * @param selectionData Select the selected picture set
     * @return
     */
    fun selectionData(selectionData: List<LocalMedia?>?): ImagePickerModel {
        if (selectionConfig!!.selectionMode == PictureConfig.SINGLE && selectionConfig.isSingleDirectReturn) {
            selectionConfig.selectionMedias = null
        } else {
            selectionConfig.selectionMedias = selectionData
        }
        return this
    }

    /**
     * 是否改变状态栏字段颜色(黑白字体转换)
     * #适合所有style使用
     *
     * @param isChangeStatusBarFontColor
     * @return
     */
    @Deprecated("")
    fun isChangeStatusBarFontColor(isChangeStatusBarFontColor: Boolean): ImagePickerModel {
        selectionConfig!!.isChangeStatusBarFontColor = isChangeStatusBarFontColor
        return this
    }

    /**
     * 选择图片样式0/9
     * #适合所有style使用
     *
     * @param isOpenStyleNumComplete
     * @return 使用setPictureStyle方法
     */
    @Deprecated("")
    fun isOpenStyleNumComplete(isOpenStyleNumComplete: Boolean): ImagePickerModel {
        selectionConfig!!.isOpenStyleNumComplete = isOpenStyleNumComplete
        return this
    }

    /**
     * 是否开启数字选择模式
     * #适合qq style 样式使用
     *
     * @param isOpenStyleCheckNumMode
     * @return 使用setPictureStyle方法
     */
    @Deprecated("")
    fun isOpenStyleCheckNumMode(isOpenStyleCheckNumMode: Boolean): ImagePickerModel {
        selectionConfig!!.isOpenStyleCheckNumMode = isOpenStyleCheckNumMode
        return this
    }

    /**
     * 设置标题栏背景色
     *
     * @param color
     * @return 使用setPictureStyle方法
     */
    @Deprecated("")
    fun setTitleBarBackgroundColor(@ColorInt color: Int): ImagePickerModel {
        selectionConfig!!.titleBarBackgroundColor = color
        return this
    }

    /**
     * 状态栏背景色
     *
     * @param color
     * @return 使用setPictureStyle方法
     */
    @Deprecated("")
    fun setStatusBarColorPrimaryDark(@ColorInt color: Int): ImagePickerModel {
        selectionConfig!!.pictureStatusBarColor = color
        return this
    }

    /**
     * 裁剪页面标题背景色
     *
     * @param color
     * @return 使用setPictureCropStyle方法
     */
    @Deprecated("")
    fun setCropTitleBarBackgroundColor(@ColorInt color: Int): ImagePickerModel {
        selectionConfig!!.cropTitleBarBackgroundColor = color
        return this
    }

    /**
     * 裁剪页面状态栏背景色
     *
     * @param color
     * @return 使用setPictureCropStyle方法
     */
    @Deprecated("")
    fun setCropStatusBarColorPrimaryDark(@ColorInt color: Int): ImagePickerModel {
        selectionConfig!!.cropStatusBarColorPrimaryDark = color
        return this
    }

    /**
     * 裁剪页面标题文字颜色
     *
     * @param color
     * @return 使用setPictureCropStyle方法
     */
    @Deprecated("")
    fun setCropTitleColor(@ColorInt color: Int): ImagePickerModel {
        selectionConfig!!.cropTitleColor = color
        return this
    }

    /**
     * 设置相册标题右侧向上箭头图标
     *
     * @param resId
     * @return 使用setPictureStyle方法
     */
    @Deprecated("")
    fun setUpArrowDrawable(resId: Int): ImagePickerModel {
        selectionConfig!!.upResId = resId
        return this
    }

    /**
     * 设置相册标题右侧向下箭头图标
     *
     * @param resId
     * @return 使用setPictureStyle方法
     */
    @Deprecated("")
    fun setDownArrowDrawable(resId: Int): ImagePickerModel {
        selectionConfig!!.downResId = resId
        return this
    }

    /**
     * 动态设置裁剪主题样式
     *
     * @param style 裁剪页主题
     *
     * @return
     */
    @Deprecated("")
    fun setPictureCropStyle(style: PictureCropParameterStyle?): ImagePickerModel {
        if (style != null) {
            PictureSelectionConfig.cropStyle = style
        } else {
            PictureSelectionConfig.cropStyle = PictureCropParameterStyle.ofDefaultCropStyle()
        }
        return this
    }

    /**
     * 动态设置相册主题样式
     *
     * @param style 主题

     * @return
     */
    @Deprecated("")
    fun setPictureStyle(style: PictureParameterStyle?): ImagePickerModel {
        if (style != null) {
            PictureSelectionConfig.style = style
            if (!selectionConfig!!.isWeChatStyle) {
                selectionConfig.isWeChatStyle = style.isNewSelectStyle
            }
        } else {
            PictureSelectionConfig.style = PictureParameterStyle.ofDefaultStyle()
        }
        return this
    }

    /**
     * Dynamically set the album to start and exit the animation
     *
     * @param windowAnimationStyle Activity Launch exit animation theme
     * @return
     */
    fun setPictureWindowAnimationStyle(windowAnimationStyle: PictureWindowAnimationStyle?): ImagePickerModel {
        if (windowAnimationStyle != null) {
            PictureSelectionConfig.windowAnimationStyle = windowAnimationStyle
        } else {
            PictureSelectionConfig.windowAnimationStyle =
                PictureWindowAnimationStyle.ofDefaultWindowAnimationStyle()
        }
        return this
    }

    /**
     * Photo album list animation {}
     * Use [or SLIDE_IN_BOTTOM_ANIMATION] directly.
     *
     * @param animationMode
     * @return
     */
    fun setRecyclerAnimationMode(animationMode: Int): ImagePickerModel {
        selectionConfig!!.animationMode = animationMode
        return this
    }

    /**
     * # If you want to handle the Android Q path, if not, just return the uri，
     * The getAndroidQToPath(); field will be empty
     *
     * @param isAndroidQTransform
     * @return
     */
    fun isAndroidQTransform(isAndroidQTransform: Boolean): ImagePickerModel {
        selectionConfig!!.isAndroidQTransform = isAndroidQTransform
        return this
    }

    /**
     * # 内部方法-要使用此方法时最好先咨询作者！！！
     *
     * @param isFallbackVersion 仅供特殊情况内部使用 如果某功能出错此开关可以回退至之前版本
     * @return
     */
    fun isFallbackVersion(isFallbackVersion: Boolean): ImagePickerModel {
        selectionConfig!!.isFallbackVersion = isFallbackVersion
        return this
    }

    /**
     * # 内部方法-要使用此方法时最好先咨询作者！！！
     *
     * @param isFallbackVersion 仅供特殊情况内部使用 如果某功能出错此开关可以回退至之前版本
     * @return
     */
    fun isFallbackVersion2(isFallbackVersion: Boolean): ImagePickerModel {
        selectionConfig!!.isFallbackVersion2 = isFallbackVersion
        return this
    }

    /**
     * # 内部方法-要使用此方法时最好先咨询作者！！！
     *
     * @param isFallbackVersion 仅供特殊情况内部使用 如果某功能出错此开关可以回退至之前版本
     * @return
     */
    fun isFallbackVersion3(isFallbackVersion: Boolean): ImagePickerModel {
        selectionConfig!!.isFallbackVersion3 = isFallbackVersion
        return this
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    fun forResult(requestCode: Int) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity = pickerConfig.getActivity()
            if (activity == null || selectionConfig == null) {
                return
            }
            val intent = if (selectionConfig.camera && selectionConfig.isUseCustomCamera) {
                Intent(activity, PictureCustomCameraActivity::class.java)
            } else {
                Intent(
                    activity,
                    when {
                        selectionConfig.camera -> ImagePickerCameraEmptyActivity::class.java
                        selectionConfig.isWeChatStyle -> PictureSelectorWeChatStyleActivity::class.java
                        else -> ImagePickerActivity::class.java
                    }
                )
            }
            selectionConfig.isCallbackMode = false
            val fragment = pickerConfig.getFragment()
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode)
            } else {
                activity.startActivityForResult(intent, requestCode)
            }
            val windowAnimationStyle = PictureSelectionConfig.windowAnimationStyle
            activity.overridePendingTransition(
                windowAnimationStyle.activityEnterAnimation,
                R.anim.picture_anim_fade_in
            )
        }
    }

    /**
     * # replace for setPictureWindowAnimationStyle();
     * Start to select media and wait for result.
     *
     *
     * # Use PictureWindowAnimationStyle to achieve animation effects
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    @Deprecated("")
    fun forResult(requestCode: Int, enterAnim: Int, exitAnim: Int) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity = pickerConfig.getActivity() ?: return
            val intent = Intent(
                activity,
                if (selectionConfig != null && selectionConfig.camera)
                    ImagePickerCameraEmptyActivity::class.java
                else if (selectionConfig!!.isWeChatStyle)
                    PictureSelectorWeChatStyleActivity::class.java
                else ImagePickerActivity::class.java
            )
            selectionConfig.isCallbackMode = false
            val fragment = pickerConfig.getFragment()
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode)
            } else {
                activity.startActivityForResult(intent, requestCode)
            }
            activity.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    /**
     * Start to select media and wait for result.
     *
     * @param listener The resulting callback listens
     */
    fun forResult(listener: OnResultCallbackListener<LocalMedia>) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity = pickerConfig.getActivity()
            if (activity == null || selectionConfig == null) {
                return
            }
            // 绑定回调监听
            PictureSelectionConfig.listener = listener
            selectionConfig.isCallbackMode = true
            val intent = if (selectionConfig.camera && selectionConfig.isUseCustomCamera) {
                Intent(activity, PictureCustomCameraActivity::class.java)
            } else {
                Intent(
                    activity,
                    when {
                        selectionConfig.camera -> ImagePickerCameraEmptyActivity::class.java
                        selectionConfig.isWeChatStyle -> PictureSelectorWeChatStyleActivity::class.java
                        else -> ImagePickerActivity::class.java
                    }
                )
            }
            val fragment = pickerConfig.getFragment()
            if (fragment != null) {
                fragment.startActivity(intent)
            } else {
                activity.startActivity(intent)
            }
            val windowAnimationStyle = PictureSelectionConfig.windowAnimationStyle
            activity.overridePendingTransition(
                windowAnimationStyle.activityEnterAnimation, R.anim.picture_anim_fade_in
            )
        }
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     * @param listener    The resulting callback listens
     */
    fun forResult(requestCode: Int, listener: OnResultCallbackListener<LocalMedia>) {
        if (!DoubleUtils.isFastDoubleClick()) {
            val activity = pickerConfig.getActivity()
            if (activity == null || selectionConfig == null) {
                return
            }
            // 绑定回调监听
            PictureSelectionConfig.listener = listener
            selectionConfig.isCallbackMode = true
            val intent = if (selectionConfig.camera && selectionConfig.isUseCustomCamera) {
                Intent(activity, PictureCustomCameraActivity::class.java)
            } else {
                Intent(
                    activity,
                    when {
                        selectionConfig.camera -> ImagePickerCameraEmptyActivity::class.java
                        selectionConfig.isWeChatStyle -> PictureSelectorWeChatStyleActivity::class.java
                        else -> ImagePickerActivity::class.java
                    }
                )
            }
            val fragment = pickerConfig.getFragment()
            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode)
            } else {
                activity.startActivityForResult(intent, requestCode)
            }
            val windowAnimationStyle = PictureSelectionConfig.windowAnimationStyle
            activity.overridePendingTransition(
                windowAnimationStyle.activityEnterAnimation,
                R.anim.picture_anim_fade_in
            )
        }
    }

    /**
     * 提供外部预览图片方法
     *
     * @param position
     * @param medias
     */
    fun openExternalPreview(position: Int, medias: List<LocalMedia?>?) {
        pickerConfig.externalPicturePreview(
            position,
            medias,
            PictureSelectionConfig.windowAnimationStyle.activityPreviewEnterAnimation
        )
    }

    /**
     * 提供外部预览图片方法-带自定义下载保存路径
     * # 废弃 由于Android Q沙盒机制 此方法不在需要了
     *
     * @param position
     * @param medias
     */
    @Deprecated("", ReplaceWith("openExternalPreview(position, medias)"))
    fun openExternalPreview(position: Int, directory_path: String?, medias: List<LocalMedia?>?) {
        pickerConfig.externalPicturePreview(
            position, directory_path, medias,
            PictureSelectionConfig.windowAnimationStyle.activityPreviewEnterAnimation
        )
    }

    /**
     * set preview video
     *
     * @param path
     */
    fun externalPictureVideo(path: String?) {
        pickerConfig.externalPictureVideo(path)
    }
}