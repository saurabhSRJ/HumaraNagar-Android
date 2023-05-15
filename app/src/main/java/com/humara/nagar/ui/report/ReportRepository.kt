package com.humara.nagar.ui.report

import android.app.Application
import android.net.Uri
import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.report.model.PostComplaintRequest
import com.humara.nagar.utils.NetworkUtils
import com.humara.nagar.utils.NetworkUtils.createPartFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ReportRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(ReportService::class.java)

    suspend fun postComplaint(request: PostComplaintRequest, imageUris: List<Uri>) = withContext(Dispatchers.IO) {
        val partMap = mutableMapOf<String, RequestBody>().apply {
            put(NetworkConstants.NetworkFormDataConstants.LOCATION, request.location.createPartFromString())
            put(NetworkConstants.NetworkFormDataConstants.CATEGORY, request.category.createPartFromString())
            put(NetworkConstants.NetworkFormDataConstants.LOCALITY, request.locality.createPartFromString())
            put(NetworkConstants.NetworkFormDataConstants.COMMENTS, request.comments.createPartFromString())
            put(NetworkConstants.NetworkFormDataConstants.USER_ID, request.user_id.toString().createPartFromString())
            put(NetworkConstants.NetworkFormDataConstants.LATITUDE, request.location_latitude.toString().createPartFromString())
            put(NetworkConstants.NetworkFormDataConstants.LONGITUDE, request.location_longitude.toString().createPartFromString())
        }
        val imageParts = ArrayList<MultipartBody.Part>()
        for (filePath in imageUris) {
            val imagePart = NetworkUtils.createImageMultipart(filePath, NetworkConstants.NetworkFormDataConstants.IMAGE)
            if (imagePart != null) {
                imageParts.add(imagePart)
            }
        }
        apiService.postComplaint(partMap, imageParts)
    }
}