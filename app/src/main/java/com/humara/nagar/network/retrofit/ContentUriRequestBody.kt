package com.humara.nagar.network.retrofit

import android.content.ContentResolver
import android.net.Uri
import com.humara.nagar.utils.FileUtils.length
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.IOException
import okio.source
import java.io.InputStream

/**
 * Helper class to convert content uris into [RequestBody]
 * Reference: https://techenum.com/retrofit-file-upload-using-fileprovider-for-content-uris/
 */
class ContentUriRequestBody(private val contentResolver: ContentResolver, private val contentUri: Uri) : RequestBody() {

    override fun contentType(): MediaType? {
        val contentType = contentResolver.getType(contentUri)
        return contentType?.toMediaTypeOrNull()
    }

    override fun contentLength(): Long {
        return contentUri.length(contentResolver)
    }

    override fun writeTo(sink: BufferedSink) {
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(contentUri)
            inputStream?.run {
                source().use { source ->
                    sink.writeAll(source)
                }
            }
        } catch (ioException: IOException) {
            throw IOException("Couldn't open content URI for reading")
        } finally {
            inputStream?.close()
        }
    }
}