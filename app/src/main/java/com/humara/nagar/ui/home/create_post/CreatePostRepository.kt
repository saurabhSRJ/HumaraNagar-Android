package com.humara.nagar.ui.home.create_post

import android.app.Application
import android.net.Uri
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.home.create_post.model.PollRequest
import com.humara.nagar.ui.home.create_post.model.TextPostRequest
import com.humara.nagar.utils.NetworkUtils
import com.humara.nagar.utils.NetworkUtils.createPartFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CreatePostRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(CreatePostService::class.java)

    suspend fun createDocumentPost(caption: String, documentUri: Uri) = withContext(Dispatchers.IO) {
        val partMap = mutableMapOf<String, RequestBody>().apply {
            put(NetworkConstants.NetworkFormDataConstants.CAPTION, caption.createPartFromString())
        }
        val docPart = ArrayList<MultipartBody.Part>()
        NetworkUtils.createDocumentMultiPart(documentUri, NetworkConstants.NetworkFormDataConstants.DOCUMENT)?.let {
            docPart.add(it)
        }
        apiService.createDocumentPost(partMap, docPart)
    }

    suspend fun createImagePost(caption: String, imageUri: Uri) = withContext(Dispatchers.IO) {
        val partMap = mutableMapOf<String, RequestBody>().apply {
            put(NetworkConstants.NetworkFormDataConstants.CAPTION, caption.createPartFromString())
        }
        val imagePart = ArrayList<MultipartBody.Part>()
        NetworkUtils.createImageMultipart(imageUri, NetworkConstants.NetworkFormDataConstants.IMAGE)?.let {
            imagePart.add(it)
        }
        apiService.createImagePost(partMap, imagePart)
    }

    suspend fun createTextPost(caption: String) = withContext(Dispatchers.IO) {
        apiService.createTextPost(TextPostRequest(caption))
    }

    suspend fun createPollPost(request: PollRequest) = withContext(Dispatchers.IO) {
        apiService.createPollPost(request)
    }
}