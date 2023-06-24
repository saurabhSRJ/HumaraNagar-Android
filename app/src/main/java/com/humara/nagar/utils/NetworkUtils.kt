package com.humara.nagar.utils

import android.content.ContentResolver
import android.net.Uri
import com.humara.nagar.network.retrofit.ContentUriRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object NetworkUtils {
    fun String.createPartFromString(): RequestBody {
        return this.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun createImageMultipart(uri: Uri, fieldName: String): MultipartBody.Part {
        val file = File(uri.path ?: "")
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return requestBody.let { MultipartBody.Part.createFormData(fieldName, file.name, it) }
    }

    fun createDocumentMultiPart(contentResolver: ContentResolver, uri: Uri, fieldName: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            fieldName,
            FileUtils.getFileName(contentResolver, uri),
            ContentUriRequestBody(contentResolver, uri)
        )
    }

    fun createVideoMultiPart(contentResolver: ContentResolver, uri: Uri, fieldName: String): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            fieldName,
            FileUtils.getFileName(contentResolver, uri),
            ContentUriRequestBody(contentResolver, uri)
        )
    }

}