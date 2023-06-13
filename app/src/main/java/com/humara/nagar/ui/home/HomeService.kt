package com.humara.nagar.ui.home

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.home.model.*
import retrofit2.http.*

interface HomeService {
    @GET(NetworkConstants.NetworkAPIConstants.POSTS)
    suspend fun getPosts(@Query(NetworkConstants.NetworkQueryConstants.PAGE) page: Int, @Query(NetworkConstants.NetworkQueryConstants.LIMIT) limit: Int): NetworkResponse<FeedResponse>

    @GET(NetworkConstants.NetworkAPIConstants.CRUD_POST)
    suspend fun getPostDetails(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<Post>

    @PUT(NetworkConstants.NetworkAPIConstants.CRUD_POST)
    suspend fun editPost(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long, @Body request: EditPostRequest): NetworkResponse<Any>

    @DELETE(NetworkConstants.NetworkAPIConstants.CRUD_POST)
    suspend fun deletePost(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<Any>

    @POST(NetworkConstants.NetworkAPIConstants.CHANGE_POST_LIKE)
    suspend fun likePost(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<Any>

    @DELETE(NetworkConstants.NetworkAPIConstants.CHANGE_POST_LIKE)
    suspend fun unlikePost(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<Any>

    @POST(NetworkConstants.NetworkAPIConstants.POLL_VOTE)
    suspend fun submitVote(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long, @Body request: PollVoteRequest): NetworkResponse<Post>

    @GET(NetworkConstants.NetworkAPIConstants.POST_COMMENTS)
    suspend fun getPostComments(
        @Path(NetworkConstants.NetworkQueryConstants.ID) id: Long,
        @Query(NetworkConstants.NetworkQueryConstants.PAGE) page: Int,
        @Query(NetworkConstants.NetworkQueryConstants.LIMIT) limit: Int
    ): NetworkResponse<PostComments>

    @POST(NetworkConstants.NetworkAPIConstants.ADD_COMMENT)
    suspend fun addComment(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long, @Body request: PostCommentRequest): NetworkResponse<PostComments>
}