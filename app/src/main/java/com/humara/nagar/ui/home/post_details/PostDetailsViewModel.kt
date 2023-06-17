package com.humara.nagar.ui.home.post_details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onException
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.home.HomeRepository
import com.humara.nagar.ui.home.model.CommentDetails
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostCommentRequest
import com.humara.nagar.ui.home.model.PostComments
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.StringUtils
import kotlinx.coroutines.launch

class PostDetailsViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val IS_LIKED = "is_liked"
        private const val TOTAL_LIKES = "total_likes"
    }

    private val repository = HomeRepository(application)
    private val postId: Long = PostDetailsFragmentArgs.fromSavedStateHandle(savedStateHandle).postId
    private val _postDetailsLiveData: MutableLiveData<Post> by lazy { MutableLiveData() }
    val postDetailsLiveData: LiveData<Post> = _postDetailsLiveData
    private val _postDetailsErrorLiveData: MutableLiveData<ApiError> by lazy { MutableLiveData() }
    val postDetailsErrorLiveData: LiveData<ApiError> = _postDetailsErrorLiveData
    private val _likePostSuccessLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val likePostSuccessLiveData: LiveData<Long> = _likePostSuccessLiveData
    private val _likePostErrorLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val likePostErrorLiveData: LiveData<Boolean> = _likePostErrorLiveData
    private val _voteSuccessLiveData: MutableLiveData<Post> by lazy { MutableLiveData() }
    val voteSuccessLiveData: LiveData<Post> = _voteSuccessLiveData
    private val _voteErrorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }
    val voteErrorLiveData: LiveData<ApiError> = _voteErrorLiveData
    private val _initialCommentsLiveData: MutableLiveData<PostComments> by lazy { MutableLiveData() }
    val initialCommentsLiveData: LiveData<PostComments> = _initialCommentsLiveData
    private val _loadMoreCommentsLiveData: MutableLiveData<List<CommentDetails>> by lazy { MutableLiveData() }
    val loadMoreCommentsLiveData: LiveData<List<CommentDetails>> = _loadMoreCommentsLiveData
    private val _postCommentsErrorLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val postCommentsErrorLiveData: LiveData<Boolean> = _postCommentsErrorLiveData
    private val _commentLoaderLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val commentLoaderLiveData: LiveData<Boolean> = _commentLoaderLiveData
    private val _addCommentSuccessLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val addCommentSuccessLiveData: LiveData<Long> = _addCommentSuccessLiveData
    private val _addCommentErrorLiveData: MutableLiveData<ApiError> by lazy { MutableLiveData() }
    val addCommentErrorLiveData: LiveData<ApiError> = _addCommentErrorLiveData
    private val _deletePostLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val deletePostLiveData: LiveData<Long> = _deletePostLiveData
    val isLikedLiveData: LiveData<Boolean> = savedStateHandle.getLiveData<Boolean>(IS_LIKED, false)
    val totalLikesLiveData: LiveData<Int> = savedStateHandle.getLiveData<Int>(TOTAL_LIKES, 0)

    private var currentPage: Int = 1
    private val limit = 5
    var canLoadMoreData = true

    init {
        getPostDetails()
        getPostComments()
    }

    fun getPostDetails() = viewModelScope.launch {
        val response = processCoroutine({ repository.getPostDetails(postId) }, updateProgress = false)
        response.onSuccess {
            _postDetailsLiveData.postValue(it)
            savedStateHandle[IS_LIKED] = it.isLikedByUser != 0
            savedStateHandle[TOTAL_LIKES] = it.totalLikes
        }.onError {
            _postDetailsErrorLiveData.postValue(it)
        }
    }

    fun flipUserLike() {
        if (isLikedLiveData.value == true) {
            unlikePost()
        } else {
            likePost()
        }
    }

    fun submitVote(optionId: Int) = viewModelScope.launch {
        val response = processCoroutine({ repository.submitVote(postId, optionId) })
        response.onSuccess {
            _voteSuccessLiveData.postValue(it)
        }.onError {
            _voteErrorLiveData.postValue(it)
        }
    }

    fun getPostComments() = viewModelScope.launch {
        val response = processCoroutine({ repository.getPostComments(postId, currentPage, limit) }, progressLiveData = _commentLoaderLiveData)
        response.onSuccess {
            if (currentPage == 1) processInitialResponse(it)
            else processLoadMoreResponse(it)
        }.onError {
            _postCommentsErrorLiveData.postValue(true)
        }.onException {
            _postCommentsErrorLiveData.postValue(true)
        }
    }

    fun addComment(comment: String) = viewModelScope.launch {
        val request = PostCommentRequest(StringUtils.replaceWhitespaces(comment))
        val response = processCoroutine({ repository.addComment(postId, request) })
        response.onSuccess {
            _addCommentSuccessLiveData.postValue(postId)
            processInitialResponse(it)
        }.onError {
            _addCommentErrorLiveData.postValue(it)
        }
    }

    fun deletePost(id: Long) = viewModelScope.launch {
        val response = processCoroutine({ repository.deletePost(id) })
        response.onSuccess {
            _deletePostLiveData.postValue(id)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun processInitialResponse(response: PostComments) {
        setPaginationState(response)
        _initialCommentsLiveData.postValue(response)
    }

    private fun processLoadMoreResponse(response: PostComments) {
        val list = arrayListOf<CommentDetails>().apply {
            if (currentPage == 2) _initialCommentsLiveData.value?.comments?.let { addAll(it) }
            else _loadMoreCommentsLiveData.value?.let { addAll(it) }
            addAll(response.comments)
        }
        setPaginationState(response)
        _loadMoreCommentsLiveData.postValue(list)
    }

    private fun setPaginationState(response: PostComments) {
        currentPage = response.page + 1
        canLoadMoreData = response.page < response.totalPages
    }

    private fun likePost() = viewModelScope.launch {
        updateLikeData(true)
        val response = processCoroutine({ repository.likePost(postId) }, updateProgress = false)
        response.onSuccess {
            _likePostSuccessLiveData.postValue(postId)
        }.onError {
            updateLikeData(false)
            _likePostErrorLiveData.postValue(true)
        }.onException {
            updateLikeData(false)
        }
    }

    private fun unlikePost() = viewModelScope.launch {
        updateLikeData(false)
        val response = processCoroutine({ repository.unlikePost(postId) }, updateProgress = false)
        response.onSuccess {
            _likePostSuccessLiveData.postValue(postId)
        }.onError {
            updateLikeData(true)
            _likePostErrorLiveData.postValue(true)
        }.onException {
            updateLikeData(true)
        }
    }

    private fun updateLikeData(isLiked: Boolean) {
        savedStateHandle[IS_LIKED] = isLiked
        savedStateHandle[TOTAL_LIKES] = if (isLiked) totalLikesLiveData.value?.plus(1) else totalLikesLiveData.value?.minus(1)
    }
}