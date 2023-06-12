package com.humara.nagar.ui.home.post_details

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.home.model.Post
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PostService {
    @GET(NetworkConstants.NetworkAPIConstants.POST_DETAILS)
    suspend fun getPostDetails(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<Post>

    @POST(NetworkConstants.NetworkAPIConstants.CHANGE_POST_LIKE)
    suspend fun likePost(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<Any>

    @DELETE(NetworkConstants.NetworkAPIConstants.CHANGE_POST_LIKE)
    suspend fun unlikePost(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<Any>
}