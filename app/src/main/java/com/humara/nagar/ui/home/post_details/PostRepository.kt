package com.humara.nagar.ui.home.post_details

import android.app.Application
import com.humara.nagar.network.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(PostService::class.java)

    suspend fun getPostDetails(id: Long) = withContext(Dispatchers.IO) {
        apiService.getPostDetails(id)
    }

    suspend fun likePost(id: Long) = withContext(Dispatchers.IO) {
        apiService.likePost(id)
    }

    suspend fun unlikePost(id: Long) = withContext(Dispatchers.IO) {
        apiService.unlikePost(id)
    }
}