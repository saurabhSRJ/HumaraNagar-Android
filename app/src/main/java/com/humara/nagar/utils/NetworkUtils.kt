package com.humara.nagar.utils

import android.net.Uri
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

    fun createImageMultipart(uri: Uri, fieldName: String): MultipartBody.Part? {
        val file = uri.path?.let { File(it) }
        val requestBody = file?.asRequestBody("image/*".toMediaTypeOrNull())
        return requestBody?.let { MultipartBody.Part.createFormData(fieldName, file.name, it) }
    }

    fun createDocumentMultiPart(uri: Uri, fieldName: String): MultipartBody.Part? {
        val file = uri.path?.let { File(it) }
        val requestBody = file?.asRequestBody("application/pdf".toMediaTypeOrNull())
        return requestBody?.let { MultipartBody.Part.createFormData(fieldName, file.name, it) }
    }
}