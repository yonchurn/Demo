package com.common.base.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.common.base.base.interf.ValueCallback
import com.common.base.utils.FileUtils
import com.common.base.utils.ThreadUtils
import com.luck.picture.lib.tools.PictureFileUtils
import java.io.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

//图片格式
enum class ImageFormat {
    JPEG,
    PNG,
    GIF,
    WEBP,
    UNKNOWN
}

/**
 * 图片工具类
 */
object ImageUtils {

    data class Size(val width: Int, val height: Int)

    /**
    通过给定的大小，获取等比例缩小后的尺寸，如果maxWidth或者maxHeight小于等于0，则缩小相对的值，
    比如maxWidth=0，则缩小height的值
     *@return 返回要缩小的尺寸
     */
    fun fitSize(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Size {
        return fitSize(bitmap.width, bitmap.height, maxWidth, maxHeight)
    }

    fun fitSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Size {
        if (maxWidth == 0 && maxHeight == 0) {
            return Size(width, height)
        }

        if (width == height) {
            val max = if (maxWidth > 0 && maxHeight > 0) min(maxWidth, maxHeight) else max(maxWidth, maxHeight)
            val value = min(max, width)
            return Size(value, value)
        } else {

            var w = width.toDouble()
            var h = height.toDouble()
            val heightScale: Double =
                if (maxHeight > 0) height.toDouble() / maxHeight.toDouble() else 0.0
            val widthScale: Double =
                if (maxWidth > 0) width.toDouble() / maxWidth.toDouble() else 0.0

            if (heightScale != 0.0 && widthScale != 0.0) {
                if (height >= maxHeight && width >= maxWidth) {
                    if (heightScale > widthScale) {
                        h = floor(height.toDouble() / heightScale)
                        w = floor(width.toDouble() / heightScale)
                    } else {
                        h = floor(height.toDouble() / widthScale)
                        w = floor(width.toDouble() / widthScale)
                    }
                } else {
                    if (height >= maxHeight && width <= maxWidth) {
                        h = floor(height.toDouble() / heightScale)
                        w = floor(width.toDouble() / heightScale)
                    } else if (height <= maxHeight && width >= maxWidth) {
                        h = floor(height.toDouble() / widthScale)
                        w = floor(width.toDouble() / widthScale)
                    }
                }
            } else if (heightScale == 0.0) {
                if (width > maxWidth) {
                    h = floor(height.toDouble() / widthScale)
                    w = floor(width.toDouble() / widthScale)
                }
            } else {
                if (height > maxHeight) {
                    h = floor(height.toDouble() / heightScale)
                    w = floor(width.toDouble() / heightScale)
                }
            }
            return Size(w.toInt(), h.toInt())
        }
    }

    /**
     * 缩放图片
     */
    fun scaleDown(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val size = fitSize(bitmap.width, bitmap.height, maxWidth, maxHeight)
        if(size.width >= bitmap.width || size.height >= bitmap.height){
            return bitmap
        }
        return Bitmap.createScaledBitmap(bitmap, size.width, size.height, true)
    }

    /**
     * 生成临时图片文件
     * @param quality 压缩质量 0 - 100
     * @param keepAlpha 是否保留透明通道，只有PNG才有效
     */
    fun writeToFile(
        context: Context,
        bitmap: Bitmap,
        quality: Int,
        keepAlpha: Boolean = false
    ): File? {
        val format =
            if (keepAlpha && bitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        return writeToFileInternal(context, bitmap, format, quality)
    }

    fun writeToFile(
        context: Context,
        bitmaps: Array<Bitmap>,
        quality: Int,
        keepAlpha: Boolean = false,
        failBitmaps: ArrayList<Bitmap>? = null
    ): List<File> {
        val files = ArrayList<File>()
        for (bitmap in bitmaps) {
            val file = writeToFile(context, bitmap, quality, keepAlpha)
            if (file != null) {
                files.add(file)
            } else {
                failBitmaps?.add(bitmap)
            }
        }
        return files
    }

    private fun writeToFileInternal(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        quality: Int
    ): File? {
        val extension = if (format == Bitmap.CompressFormat.PNG) "png" else "jpeg"
        val file = FileUtils.createTemporaryFile(context, extension)

        var success = false
        val outputStream = ByteArrayOutputStream()
        val fos = FileOutputStream(file)
        try {
            bitmap.compress(format, quality, outputStream)
            fos.write(outputStream.toByteArray())
            fos.flush()
            success = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream.close()
            fos.close()
        }

        if (!success) {
            file.delete()
            return null
        }
        return file
    }

    //保存图片
    //需要申请权限 Manifest.permission.WRITE_EXTERNAL_STORAGE
    @Suppress("deprecation")
    fun saveImage(file: File, context: Context, completion: ValueCallback<Boolean>) {
        val mimeType = FileUtils.getMimeType(file.absolutePath)
        val filename = "${FileUtils.getUniqueFileName()}${getLastImgSuffix(mimeType)}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}${File.separator}Camera")
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis().toString())

            val contentResolver = context.contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                Thread {
                    var inputStream: InputStream? = null
                    var outputStream: OutputStream? = null
                    var result = false
                    try {
                        outputStream = contentResolver.openOutputStream(uri)
                        if (outputStream != null) {
                            inputStream = FileInputStream(file)
                            android.os.FileUtils.copy(inputStream, outputStream)
                            result = true
                        }
                    }catch (e: Exception) {

                    }finally {
                        inputStream?.close()
                        outputStream?.close()
                        ThreadUtils.runOnMainThread {
                            if (result) {
                                val path = PictureFileUtils.getPath(context, uri)
                                if (path != null) {
                                    refreshAlbum(context, path, mimeType)
                                }
                            }
                            completion(result)
                        }
                    }
                }.start()
            } else {
                completion(false)
            }
        } else {
            val state = Environment.getExternalStorageState()
            val rootDir = if (state == Environment.MEDIA_MOUNTED) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            }

            if (rootDir != null) {
                Thread {
                    if (!rootDir.exists()) rootDir.mkdirs()
                    val path = if (state != Environment.MEDIA_MOUNTED) {
                        rootDir.absolutePath
                    } else {
                        "${rootDir.absolutePath}${File.separator}Camera${File.separator}"
                    }
                    val foldDir = File(path)
                    if (!foldDir.exists()) {
                        foldDir.mkdirs()
                    }

                    val imageFile = File(foldDir, filename)
                    FileUtils.copyFile(file.absolutePath, imageFile.absolutePath)
                    ThreadUtils.runOnMainThread {
                        refreshAlbum(context, imageFile.absolutePath, mimeType)
                        completion(true)
                    }
                }.start()
            } else {
                completion(false)
            }
        }
    }

    //通知相册刷新
    private fun refreshAlbum(context: Context, path: String, mineType: String) {
       MediaScanner(context, path, mineType)
    }

    private fun getLastImgSuffix(mineType: String): String {
        val defaultSuffix = ".png"
        try {
            val index = mineType.lastIndexOf("/") + 1
            if (index > 0) {
                return ".${mineType.substring(index)}"
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return defaultSuffix
        }
        return defaultSuffix
    }

    //获取图片 mineType
    fun getMineType(file: File?): String? {
        return getMineType(getFormat(file))
    }

    fun getMineType(inputStream: InputStream): String? {
        return getMineType(getFormat(inputStream))
    }

    fun getMineType(format: ImageFormat): String? {
        return when(format) {
            ImageFormat.JPEG -> "image/jpeg"
            ImageFormat.PNG -> "image/png"
            ImageFormat.WEBP -> "image/webp"
            ImageFormat.GIF -> "image/gif"
            ImageFormat.UNKNOWN -> null
        }
    }

    //从图片文件获取图片格式
    fun getFormat(file: File?): ImageFormat {
        try {
            val fileInputStream = FileInputStream(file)
            return getFormat(fileInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ImageFormat.UNKNOWN
    }

    fun getFormat(inputStream: InputStream): ImageFormat {
        try {
            val bufferedInputStream = BufferedInputStream(inputStream)
            bufferedInputStream.mark(Int.MAX_VALUE)

            // https://en.wikipedia.org/wiki/JPEG
            var start = ByteArray(2)
            bufferedInputStream.read(start)
            if (start[0] == 0xFF.toByte() &&
                start[1] == 0xD8.toByte()
            ) {
                return ImageFormat.JPEG
            }
            bufferedInputStream.reset()

            // https://en.wikipedia.org/wiki/Portable_Network_Graphics
            start = ByteArray(8)
            bufferedInputStream.read(start)
            if (start[0] == 0x89.toByte() && start[1] == 0x50.toByte() && start[2] == 0x4E.toByte() && start[3] == 0x47.toByte() && start[4] == 0x0D.toByte() && start[5] == 0x0A.toByte() && start[6] == 0x1A.toByte() && start[7] == 0x0A.toByte()) {
                return ImageFormat.PNG
            }
            bufferedInputStream.reset()

            // https://developers.google.com/speed/webp/docs/riff_container
            // https://en.wikipedia.org/wiki/WebP
            start = ByteArray(4)
            val end = ByteArray(4)
            bufferedInputStream.read(start)
            bufferedInputStream.skip(4)
            bufferedInputStream.read(end)
            if (start[0] == 0x52.toByte() && start[1] == 0x49.toByte() && start[2] == 0x46.toByte() && start[3] == 0x46.toByte() && end[0] == 0x57.toByte() && end[1] == 0x45.toByte() && end[2] == 0x42.toByte() && end[3] == 0x50.toByte()) {
                return ImageFormat.WEBP
            }
            bufferedInputStream.reset()

            // https://en.wikipedia.org/wiki/File:Empty_GIF_hex.png
            start = ByteArray(6)
            bufferedInputStream.read(start)
            if (start[0] == 0x47.toByte() && start[1] == 0x49.toByte() && start[2] == 0x46.toByte() && start[3] == 0x38.toByte() && start[4] == 0x39.toByte() && start[5] == 0x61.toByte()) {
                return ImageFormat.GIF
            }
            bufferedInputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ImageFormat.UNKNOWN
    }
}

class MediaScanner(context: Context, val path: String, val mineType: String): MediaScannerConnection.MediaScannerConnectionClient {

    private val connection = MediaScannerConnection(context, this)

    override fun onScanCompleted(path: String?, uri: Uri?) {
        connection.disconnect()
    }

    override fun onMediaScannerConnected() {
        connection.scanFile(path, mineType)
    }

    init {
        connection.connect()
    }
}