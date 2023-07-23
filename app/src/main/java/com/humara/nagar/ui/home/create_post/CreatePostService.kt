package com.humara.nagar.ui.home.create_post

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.home.create_post.model.PollRequest
import com.humara.nagar.ui.home.create_post.model.TextPostRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap

interface CreatePostService {
    @Multipart
    @POST(NetworkConstants.NetworkAPIConstants.DOCUMENT_POST)
    suspend fun createDocumentPost(@PartMap partMap: MutableMap<String, RequestBody>, @Part document: ArrayList<MultipartBody.Part>): NetworkResponse<Any>

    @Multipart
    @POST(NetworkConstants.NetworkAPIConstants.IMAGE_POST)
    suspend fun createImagePost(@PartMap partMap: MutableMap<String, RequestBody>, @Part image: ArrayList<MultipartBody.Part>): NetworkResponse<Any>

    @POST(NetworkConstants.NetworkAPIConstants.TEXT_POST)
    suspend fun createTextPost(@Body request: TextPostRequest): NetworkResponse<Any>

    @POST(NetworkConstants.NetworkAPIConstants.POLL_POST)
    suspend fun createPollPost(@Body request: PollRequest): NetworkResponse<Any>

    @Multipart
    @POST(NetworkConstants.NetworkAPIConstants.VIDEO_POST)
    suspend fun createVideoPost(@PartMap partMap: MutableMap<String, RequestBody>, @Part video: ArrayList<MultipartBody.Part>): NetworkResponse<Any>
}