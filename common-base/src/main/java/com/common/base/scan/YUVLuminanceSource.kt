package com.common.base.scan

import android.graphics.Bitmap
import com.google.zxing.LuminanceSource


/**
 * YUV 亮度源
 */
class YUVLuminanceSource(val yuvData: ByteArray,
                         val dataWidth: Int,
                         val dataHeight: Int,
                         val left: Int,
                         val top: Int,
                         width: Int,
                         height: Int) : LuminanceSource(width, height) {

    init {
        require(!(left + width > dataWidth || top + height > dataHeight)) { "Crop rectangle does not fit within image data." }
    }

    override fun getRow(y: Int, row: ByteArray?): ByteArray {

        require(!(y < 0 || y >= height)) { "Requested row is outside the image: $y" }

        var result = row
        val width = width
        if (result == null || result.size < width) {
            result = ByteArray(width)
        }
        val offset = (y + top) * dataWidth + left
        System.arraycopy(yuvData, offset, result, 0, width)

        return result
    }

    override fun getMatrix(): ByteArray {
        val width = width
        val height = height

        // If the caller asks for the entire underlying image, save the copy and
        // give them the
        // original data. The docs specifically warn that result.length must be
        // ignored.
        if (width == dataWidth && height == dataHeight) {
            return yuvData
        }

        val area = width * height
        val matrix = ByteArray(area)
        var inputOffset = top * dataWidth + left

        // If the width matches the full width of the underlying data, perform a
        // single copy.
        if (width == dataWidth) {
            System.arraycopy(yuvData, inputOffset, matrix, 0, area)
            return matrix
        }

        // Otherwise copy one cropped row at a time.
        val yuv = yuvData
        for (y in 0 until height) {
            val outputOffset = y * width
            System.arraycopy(yuv, inputOffset, matrix, outputOffset, width)
            inputOffset += dataWidth
        }
        return matrix
    }

    override fun isCropSupported(): Boolean {
        return true
    }

    /**
     * 获取剪切的图片
     */
    fun renderCroppedGreyScaleBitmap(): Bitmap? {
        val width = width
        val height = height
        val pixels = IntArray(width * height)
        val yuv = yuvData
        var inputOffset = top * dataWidth + left

        for (y in 0 until height) {
            val outputOffset = y * width
            for (x in 0 until width) {
                val grey = yuv[inputOffset + x].toInt() and 0xff
                pixels[outputOffset + x] = -0x1000000 or grey * 0x00010101
            }
            inputOffset += dataWidth
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}