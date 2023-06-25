package com.humara.nagar.ui.home

import android.app.Application
import com.humara.nagar.database.AppDatabase
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.home.model.EditPostRequest
import com.humara.nagar.ui.home.model.PollVoteRequest
import com.humara.nagar.ui.home.model.PostCommentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeRepository(application: Application) : BaseRepository(application) {
    private val apiService = getRetrofit().create(HomeService::class.java)
    private val database: AppDatabase by lazy { AppDatabase.getDataBase(application) }

    suspend fun getPosts(page: Int, limit: Int, filterId: Int) = withContext(Dispatchers.IO) {
        apiService.getPosts(page, limit, filterId)
    }

    suspend fun getPostDetails(id: Long) = withContext(Dispatchers.IO) {
        apiService.getPostDetails(id)
    }

    suspend fun editPost(id: Long, request: EditPostRequest) = withContext(Dispatchers.IO) {
        apiService.editPost(id, request)
    }

    suspend fun deletePost(id: Long) = withContext(Dispatchers.IO) {
        apiService.deletePost(id)
    }

    suspend fun likePost(id: Long) = withContext(Dispatchers.IO) {
        apiService.likePost(id)
    }

    suspend fun unlikePost(id: Long) = withContext(Dispatchers.IO) {
        apiService.unlikePost(id)
    }

    suspend fun submitVote(postId: Long, optionId: Int) = withContext(Dispatchers.IO) {
        apiService.submitVote(postId, PollVoteRequest(optionId))
    }

    suspend fun getPostComments(postId: Long, page: Int, limit: Int) = withContext(Dispatchers.IO) {
        apiService.getPostComments(postId, page, limit)
    }

    suspend fun addComment(postId: Long, request: PostCommentRequest) = withContext(Dispatchers.IO) {
        apiService.addComment(postId, request)
    }

    suspend fun getFeedFilters() = withContext(Dispatchers.IO) {
        database.referenceDataDao().getAllFilters()
    }
}