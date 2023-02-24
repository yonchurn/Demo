package com.common.base.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.common.base.base.interf.ValueCallback
import com.luck.picture.lib.PictureContentResolver
import com.luck.picture.lib.compress.InputStreamAdapter
import com.luck.picture.lib.compress.InputStreamProvider
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.PictureSelectionConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.thread.PictureThreadUtils
import com.luck.picture.lib.tools.*
import java.io.*
import kotlin.math.min

/**
 * 压缩回调
 */
typealias ImageCompressEngineCallback = ValueCallback<List<LocalMedia>>

/**
 * 图片压缩
 */
interface ImageCompressEngine {

    val attachedContext: Context
    val pictureSelectionConfig: PictureSelectionConfig

    private val config: PictureSelectionConfig
        get() = pictureSelectionConfig
    private val context: Context
        get() = attachedContext

    fun compressImage(result: MutableList<LocalMedia>, callback: ImageCompressEngineCallback) {
        PictureThreadUtils.executeByIo(object : PictureThreadUtils.SimpleTask<List<File>>() {
            @Throws(Exception::class)
            override fun doInBackground(): List<File> {
                val files = ArrayList<File>()
                for (media in result) {
                    val provider = streamProviderFromMedia(media)
                    try {
                        files.add(compress(provider, result.size))
                    }finally {
                        provider.close()
                    }
                }
                return files
            }

            override fun onSuccess(files: List<File>) {
                if (files.size == result.size) {
                    handleCompressCallBack(result, files, callback)
                } else {
                    callback(result)
                }
            }
        })
    }

    private fun handleCompressCallBack(images: List<LocalMedia>, files: List<File>, callback: ImageCompressEngineCallback) {
        val isAndroidQ = SdkVersionUtils.isQ()
        val size = images.size
        if (files.size == size) {
            var i = 0
            while (i < size) {
                val file = files[i]
                val path = file.absolutePath
                val image = images[i]
                val http = PictureMimeType.isHasHttp(path)
                val flag = !TextUtils.isEmpty(path) && http
                val isHasVideo = PictureMimeType.isHasVideo(image.mimeType)
                image.isCompressed = !isHasVideo && !flag
                image.compressPath = if (isHasVideo || flag) null else path
                if (isAndroidQ) {
                    image.androidQToPath = image.compressPath
                }
                i++
            }
        }
        callback(images)
    }

    private fun streamProviderFromMedia(media: LocalMedia): InputStreamAdapter {
        return object : InputStreamAdapter() {
            @Throws(IOException::class)
            override fun openInternal(): InputStream? {
                return if (PictureMimeType.isContent(media.path) && !media.isCut) {
                    if (!TextUtils.isEmpty(media.androidQToPath)) {
                        FileInputStream(media.androidQToPath)
                    } else PictureContentResolver.getContentResolverOpenInputStream(context, Uri.parse(media.path))!!
                } else {
                    if (PictureMimeType.isHasHttp(media.path)) null else FileInputStream(if (media.isCut) media.cutPath else media.path)
                }
            }

            override fun getPath(): String {
                return if (media.isCut) {
                    media.cutPath
                } else {
                    if (TextUtils.isEmpty(media.androidQToPath)) media.path else media.androidQToPath
                }
            }

            override fun getMedia(): LocalMedia {
                return media
            }
        }
    }

    private fun compress(provider: InputStreamProvider, totalCount: Int): File {
        val media = provider.media
        val hasVideo = PictureMimeType.isHasVideo(media.mimeType)
        if (!hasVideo) {
            val inputStream = provider.open()
            if (inputStream != null) {
                val newPath =
                    if (media.isCut && !TextUtils.isEmpty(media.cutPath)) media.cutPath else media.realPath
                val suffix = extSuffix(media.mimeType)
                var outFile = getImageCacheFile(
                    context,
                    media,
                    if (TextUtils.isEmpty(suffix)) extSuffix(provider) else suffix
                )
                var filename = ""
                val newFileName = config.renameCompressFileName
                if (!TextUtils.isEmpty(newFileName)) {
                    filename =
                        if (totalCount == 1) newFileName else StringUtils.rename(
                            newFileName
                        )
                    outFile = getImageCustomFile(context, filename)
                }

                // 如果文件存在直接返回不处理
                if (outFile.exists()) {
                    Log.d("CompressFile", "file = ${outFile.absolutePath}")
                    return outFile
                }

                return if (extSuffix(provider).startsWith(".gif")) {
                    // GIF without compression
                    copyIfNeeded(provider, newPath, filename)
                } else {
                    compress(provider, outFile) ?: copyIfNeeded(provider, newPath, filename)
                }
            }
        }

        return File(provider.media.path)
    }

    private fun copyIfNeeded(provider: InputStreamProvider, newPath: String, filename: String): File {
        val media = provider.media
        // 这种情况判断一下，如果是小于设置的图片压缩阀值，再Android 10以上做下拷贝的处理
        return if (SdkVersionUtils.isQ()) {
            val newFilePath =
                if (media.isCut) media.cutPath else AndroidQTransformUtils.copyPathToAndroidQ(
                    context,
                    media.id,
                    provider.path,
                    media.width,
                    media.height,
                    media.mimeType,
                    filename
                )
            File(if (TextUtils.isEmpty(newFilePath)) newPath else newFilePath)
        } else {
            File(newPath)
        }
    }

    /**
     * Returns a file with a cache image name in the private cache directory.
     *
     * @param context A context.
     */
    private fun getImageCacheFile(
        context: Context,
        media: LocalMedia,
        suffix: String
    ): File {
        var path = config.compressSavePath ?: ""
        if (TextUtils.isEmpty(path)) {
            val imageCacheDir = getImageCacheDir(context)
            if (imageCacheDir != null) {
                path = imageCacheDir.absolutePath
            }
        }
        var cacheBuilder = ""
        try {
            val encryptionValue =
                StringUtils.getEncryptionValue(media.id, media.width, media.height)
            cacheBuilder = if (!TextUtils.isEmpty(encryptionValue) && !media.isCut) {
                "$path/IMG_CMP_$encryptionValue${if (TextUtils.isEmpty(suffix)) ".jpg" else suffix}"
            } else {
                "$path/${DateUtils.getCreateFileName("IMG_CMP_")}${if (TextUtils.isEmpty(suffix)) ".jpg" else suffix}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return File(cacheBuilder)
    }

    private fun getImageCustomFile(context: Context, filename: String): File {

        var path = config.compressSavePath ?: ""
        if (TextUtils.isEmpty(path)) {
            path = getImageCacheDir(context)!!.absolutePath
        }
        return File("$path/$filename")
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context A context.
     * @see .getImageCacheDir
     */
    private fun getImageCacheDir(context: Context): File? {
        val cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (cacheDir != null) {
            return if (!cacheDir.mkdirs() && (!cacheDir.exists() || !cacheDir.isDirectory)) {
                // File wasn't able to create a directory, or the result exists but not a directory
                null
            } else cacheDir
        }
        return null
    }

    private fun extSuffix(mimeType: String): String {
        if (TextUtils.isEmpty(mimeType)) {
            return ".jpg"
        }
        return try {
            if (mimeType.startsWith("video")) mimeType.replace("video/", ".") else mimeType.replace(
                "image/",
                "."
            )
        } catch (e: Exception) {
            ".jpg"
        }
    }

    private fun extSuffix(provider: InputStreamProvider): String {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(provider.open(), null, options)
            options.outMimeType.replace("image/", ".")
        } catch (e: Exception) {
            ".jpg"
        }
    }

    private fun needCompressToLocalMedia(leastCompressSize: Int, file: File): Boolean {
        if (leastCompressSize > 0) {
            return file.exists() && file.length() > leastCompressSize shl 10
        }
        return true
    }

    //判断格式是否支持
    private fun supportFormat(stream: InputStream): Boolean {
        return when(ImageUtils.getFormat(stream)) {
            ImageFormat.JPEG, ImageFormat.PNG, ImageFormat.WEBP -> true
            else -> false
        }
    }

    private fun rotatingAndScaleIfNeeded(bitmap: Bitmap, provider: InputStreamProvider): MatrixResult {

        var size: ImageUtils.Size? = null
        val width = bitmap.width
        val height = bitmap.height

        if (!config.enableCrop) {
            val maxWidth = config.cropWidth
            val maxHeight = config.cropHeight
            if (maxWidth > 0 || maxHeight > 0) {
                val result = ImageUtils.fitSize(width, height, maxWidth, maxHeight)
                if (result.width < width || result.height < height) {
                    //图片比裁剪大小大 才裁剪
                    size = result
                }
            }
        }

        val media = provider.media
        var rotate = false
        var orientation = 0

        if (isJPG(media.mimeType)) {
            val isCut = media.isCut && !TextUtils.isEmpty(media.cutPath)
            val url = if (isCut) media.cutPath else media.path
            val degree = if (PictureMimeType.isContent(url)) {
                BitmapUtils.readPictureDegree(provider.open())
            } else {
                BitmapUtils.readPictureDegree(context, url)
            }
            if (degree > 0) {
                orientation = degree
                rotate = true
            }
        }

        if (size != null || rotate) {
            val matrix = Matrix()
            if (rotate) {
                matrix.postRotate(orientation.toFloat())
            }

            if (size != null) {
                matrix.postScale(size.width / width.toFloat(),
                    size.height / height.toFloat())
            }

            val result = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
            return MatrixResult(
                result != null,
                rotate,
                result ?: bitmap
            )
        }
        return MatrixResult(false, false, bitmap)
    }

    private fun compress(provider: InputStreamProvider, outFile: File): File? {

        var opts: BitmapFactory.Options? = null
        if (!config.enableCrop) {
            val maxWidth = config.cropWidth
            val maxHeight = config.cropHeight
            if (maxWidth > 0 || maxHeight > 0) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(provider.open(), null, options)
                val width = options.outWidth
                val height = options.outHeight
                val result = ImageUtils.fitSize(width, height, maxWidth, maxHeight)
                if (result.width < width || result.height < height) {
                    //图片比裁剪大小大 才裁剪
                    val size = min(width / result.width, height / result.height)
                    if (size >= 2) {
                        opts = BitmapFactory.Options()
                        opts.inSampleSize = size
                    }
                }
            }
        }

        var bitmap = BitmapFactory.decodeStream(provider.open(), null, opts)
        bitmap ?: return null

        val media = provider.media
        val stream = ByteArrayOutputStream()
        val rotateResult = rotatingAndScaleIfNeeded(bitmap, provider)
        bitmap = rotateResult.bitmap
        val path =
            if (media.isCut && !TextUtils.isEmpty(media.cutPath)) media.cutPath else media.realPath

        path ?: return null

        val file = File(path)
        val isCompress = needCompressToLocalMedia(config.minimumCompressSize, file)
        val isSupport = supportFormat(provider.open())
        var useEnabled = false

        if (isCompress || rotateResult.change || !isSupport) {
            useEnabled = true
            var compressQuality = config.compressQuality
            if (compressQuality <= 0 || compressQuality > 100) {
                compressQuality = 80
            }
            bitmap.compress(
                if (bitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                compressQuality,
                stream
            )

            if (stream.size() > file.length() && isSupport && !rotateResult.roating) {
                //压缩后变大了并且是支持的格式，用原图
                useEnabled = false
            }
        }
        bitmap.recycle()

        if (useEnabled) {
            val fos = FileOutputStream(outFile)
            try {
                fos.write(stream.toByteArray())
                fos.flush()
            } finally {
                fos.close()
                stream.close()
            }
            return outFile
        }

        return null
    }

    private fun isJPG(mimeType: String): Boolean {
        return if (TextUtils.isEmpty(mimeType)) {
            false
        } else mimeType.startsWith("image/heic")
                || mimeType.startsWith("image/jpeg")
                || mimeType.startsWith("image/jpg")
    }
}

//缩放 旋转结果
private class MatrixResult(val change: Boolean, val roating: Boolean, val bitmap: Bitmap)