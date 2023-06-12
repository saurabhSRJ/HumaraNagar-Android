package com.humara.nagar.ui.home.post_details

import android.app.Application
import com.humara.nagar.network.BaseRepository
import com.humara.nagar.ui.home.model.PollVoteRequest
import com.humara.nagar.ui.home.model.PostCommentRequest
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

    suspend fun submitVote(postId: Long, optionId: Int) = withContext(Dispatchers.IO) {
        apiService.submitVote(postId, PollVoteRequest(optionId))
    }

    suspend fun getPostComments(postId: Long, page: Int, limit: Int) = withContext(Dispatchers.IO) {
        apiService.getPostComments(postId, page, limit)
    }

    suspend fun addComment(postId: Long, request: PostCommentRequest) = withContext(Dispatchers.IO) {
        apiService.addComment(postId, request)
    }
}