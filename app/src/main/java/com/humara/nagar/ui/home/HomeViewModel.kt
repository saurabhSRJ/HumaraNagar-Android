package com.humara.nagar.ui.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onException
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.home.model.EditPostRequest
import com.humara.nagar.ui.home.model.FeedResponse
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.StringUtils
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : BaseViewModel(application) {
    private val repository = HomeRepository(application)

    private val _initialPostsLiveData: SingleLiveEvent<List<Post>> by lazy { SingleLiveEvent() }
    val initialPostsLiveData: LiveData<List<Post>> = _initialPostsLiveData
    private val _initialPostProgressLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val initialPostProgressLiveData: LiveData<Boolean> = _initialPostProgressLiveData
    private val _initialPostErrorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }
    val initialPostErrorLiveData: LiveData<ApiError> = _initialPostErrorLiveData
    private val _loadMorePostsLiveData: SingleLiveEvent<List<Post>> by lazy { SingleLiveEvent() }
    val loadMorePostsLiveData: LiveData<List<Post>> = _loadMorePostsLiveData
    private val _loadMorePostProgressLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val loadMorePostProgressLiveData: LiveData<Boolean> = _loadMorePostProgressLiveData
    private val _loadMorePostErrorLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val loadMorePostErrorLiveData: LiveData<Boolean> = _loadMorePostErrorLiveData
    private val _postDetailsLiveData: SingleLiveEvent<Post> by lazy { SingleLiveEvent() }
    val postDetailsLiveData: LiveData<Post> = _postDetailsLiveData
    private val _likePostErrorLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val likePostErrorLiveData: LiveData<Long> = _likePostErrorLiveData
    private val _updatePostLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val updatePostLiveData: LiveData<Long> = _updatePostLiveData
    private val _deletePostLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val deletePostLiveData: LiveData<Long> = _deletePostLiveData
    private val _voteSuccessLiveData: SingleLiveEvent<Post> by lazy { SingleLiveEvent() }
    val voteSuccessLiveData: LiveData<Post> = _voteSuccessLiveData
    private val _voteErrorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }
    val voteErrorLiveData: LiveData<ApiError> = _voteErrorLiveData
    private val _editPostSuccessLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val editPostSuccessLiveData: LiveData<Boolean> = _editPostSuccessLiveData
    private var currentPage: Int = 1
    private val limit = 5
    var canLoadMoreData = true

    init {
        getPosts()
    }

    fun getPosts() = viewModelScope.launch {
        val response = processCoroutine({ repository.getPosts(currentPage, limit) }, progressLiveData = if (currentPage == 1) _initialPostProgressLiveData else _loadMorePostProgressLiveData)
        response.onSuccess {
            if (currentPage == 1) processInitialResponse(it)
            else processLoadMoreResponse(it)
        }.onError {
            if (currentPage == 1) _initialPostErrorLiveData.postValue(it)
            else _loadMorePostErrorLiveData.postValue(true)
        }.onException {
            if (currentPage > 1) _loadMorePostErrorLiveData.postValue(true)
        }
    }

    fun getPostDetails(postId: Long) = viewModelScope.launch {
        val response = processCoroutine({ repository.getPostDetails(postId) })
        response.onSuccess {
            _postDetailsLiveData.postValue(it)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun processInitialResponse(response: FeedResponse) {
        setPaginationState(response)
        _initialPostsLiveData.postValue(response.posts)
    }

    private fun processLoadMoreResponse(response: FeedResponse) {
        setPaginationState(response)
        _loadMorePostsLiveData.postValue(response.posts)
    }

    fun flipUserLike(post: Post) {
        if (post.hasUserLike()) {
            unlikePost(post)
        } else {
            likePost(post)
        }
    }

    fun submitVote(postId: Long, optionId: Int) = viewModelScope.launch {
        val response = processCoroutine({ repository.submitVote(postId, optionId) })
        response.onSuccess {
            _voteSuccessLiveData.postValue(it)
        }.onError {
            _voteErrorLiveData.postValue(it)
        }
    }

    fun editPost(id: Long, caption: String) = viewModelScope.launch {
        val response = processCoroutine({ repository.editPost(id, EditPostRequest(StringUtils.replaceWhitespaces(caption.trim()))) })
        response.onSuccess {
            _editPostSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
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

    private fun likePost(post: Post) = viewModelScope.launch {
        val response = processCoroutine({ repository.likePost(post.postId) }, updateProgress = false)
        response.onError {
            _likePostErrorLiveData.postValue(post.postId)
        }.onException {
            _likePostErrorLiveData.postValue(post.postId)
        }
    }

    private fun unlikePost(post: Post) = viewModelScope.launch {
        val response = processCoroutine({ repository.unlikePost(post.postId) }, updateProgress = false)
        response.onError {
            _likePostErrorLiveData.postValue(post.postId)
        }.onException {
            _likePostErrorLiveData.postValue(post.postId)
        }
    }

    fun setPostUpdateRequired(postId: Long) {
        _updatePostLiveData.postValue(postId)
    }

    private fun setPaginationState(response: FeedResponse) {
        currentPage = response.page + 1
        canLoadMoreData = response.page < response.totalPages
    }

    fun resetPaginationState() {
        currentPage = 1
        canLoadMoreData = true
    }
}