package com.humara.nagar.ui.home

import com.humara.nagar.constants.NetworkConstants
import com.humara.nagar.network.NetworkResponse
import com.humara.nagar.ui.home.model.*
import retrofit2.http.*

interface HomeService {
    @GET(NetworkConstants.NetworkAPIConstants.POSTS)
    suspend fun getPosts(
        @Query(NetworkConstants.NetworkQueryConstants.PAGE) page: Int,
        @Query(NetworkConstants.NetworkQueryConstants.LIMIT) limit: Int,
        @Query(NetworkConstants.NetworkQueryConstants.FILTER_ID) filterId: Int
    ): NetworkResponse<FeedResponse>

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

    @GET(NetworkConstants.NetworkAPIConstants.GET_POST_LIKES)
    suspend fun getPostLikes(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long): NetworkResponse<PostLikesResponse>

    @POST(NetworkConstants.NetworkAPIConstants.CRUD_COMMENT)
    suspend fun addComment(@Path(NetworkConstants.NetworkQueryConstants.ID) id: Long, @Body request: PostCommentRequest): NetworkResponse<PostComments>

    @HTTP(method = "DELETE", path = NetworkConstants.NetworkAPIConstants.CRUD_COMMENT, hasBody = true)
    suspend fun deleteComment(@Path(NetworkConstants.NetworkQueryConstants.ID) postId: Long, @Body request: DeleteCommentRequest): NetworkResponse<PostComments>
}