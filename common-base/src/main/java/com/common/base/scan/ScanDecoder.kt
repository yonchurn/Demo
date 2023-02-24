package com.common.base.scan

import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.*
import kotlin.collections.HashMap

/**
 * 扫描图片解码
 */
@Suppress("deprecation")
class ScanDecoder(val handler: Handler, val cameraManager: CameraManager) : Thread() {

    //解码器
    private val _reader = MultiFormatReader()

    //是否已停止
    private var _stop = false

    //运行环，防止线程被系统杀掉
    private var _looper : Looper? = null

    init {

        val hints = HashMap<DecodeHintType, Any>()

        //解码格式
        val formats = Vector<BarcodeFormat>()

        //条形码
        formats.add(BarcodeFormat.UPC_A)
        formats.add(BarcodeFormat.UPC_E)
        formats.add(BarcodeFormat.EAN_13)
        formats.add(BarcodeFormat.EAN_8)
        formats.add(BarcodeFormat.RSS_14)
        formats.add(BarcodeFormat.CODE_39)
        formats.add(BarcodeFormat.CODE_93)
        formats.add(BarcodeFormat.CODE_128)

        //二维码
        formats.add(BarcodeFormat.QR_CODE)

        hints[DecodeHintType.POSSIBLE_FORMATS] = formats

        _reader.setHints(hints)
    }

    override fun run() {
        Looper.prepare()
        _looper = Looper.myLooper()
        Looper.loop()
    }


    //停止解码
    fun stopDecode(){
        _stop = true
        if(_looper != null){
            _looper!!.quit()
            _looper = null
        }
    }

    //获取解码所需的
    fun buildLuminanceSource(data: ByteArray, width: Int, height: Int): YUVLuminanceSource {

        val rect = cameraManager.getScanRect()!!
        val previewFormat = cameraManager.getPreviewFormat()

        when(previewFormat) {
            PixelFormat.YCbCr_420_SP, PixelFormat.YCbCr_422_SP -> {
               return YUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height())
            }
            else -> {
                val previewFormatString = cameraManager.getPreviewFormatString()
                if("yuv420p" == previewFormatString) {
                    return YUVLuminanceSource(data, width, height,rect.left, rect.top, rect.width(), rect.height())
                }
            }
        }
        throw IllegalArgumentException("Unsupported picture format: $previewFormat / ${cameraManager.getPreviewFormatString()}")
    }

    //解码
    fun decode(data: ByteArray?) {

        if(data == null)
            return

        val size = cameraManager.getPreviewSize()
        var width = 0
        var height = 0
        if(size != null){
            width = size.width
            height = size.height
        }

        //将相机获取的图片数据转化为binaryBitmap格式
        val rotatedData = ByteArray(data.size)
        for (y in 0 until height) {
            for (x in 0 until width)
                rotatedData[x * height + height - y - 1] = data[x + y * width]
        }
        val tmp = width
        width = height
        height = tmp

        val source = buildLuminanceSource(rotatedData, width, height)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        var rawResult: Result? = null
        try {
            //解析转化后的图片，得到结果
            rawResult = _reader.decodeWithState(bitmap)
        } catch (e: ReaderException) {
            // continue
        } finally {
            _reader.reset()
        }

        if(!_stop){
            if (rawResult != null) {

                //如果解析结果不为空，就是解析成功了，则发送成功消息，将结果放到message中
                val message = handler.obtainMessage(CameraHandler.MESSAGE_DECODE_SUCCESS)
                message.obj = rawResult
                message.sendToTarget()
            } else {
                //解码失败
                handler.sendEmptyMessage(CameraHandler.MESSAGE_DECODE_FAIL)
            }
        }
    }
}