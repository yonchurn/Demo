package com.common.base.api

import com.common.base.base.interf.ValueCallback
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

//可观察进度的
class ObservableMultipartBody internal constructor(
    private val requestBody: RequestBody,
    private val progressCallback: ValueCallback<Double>
) : RequestBody() {

    override fun contentType(): MediaType? = requestBody.contentType()

    override fun contentLength(): Long = requestBody.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength().toDouble()
        val forwardingSink = object : ForwardingSink(sink) {
            private var bytesWritten = 0.0

            override fun write(source: Buffer, byteCount: Long) {
                bytesWritten += byteCount
                progressCallback(bytesWritten / totalBytes)
                super.write(source, byteCount)
            }
        }

        val bufferedSink = forwardingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}