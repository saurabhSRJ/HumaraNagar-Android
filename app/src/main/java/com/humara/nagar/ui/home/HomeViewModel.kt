package com.humara.nagar.ui.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onException
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.home.model.FeedResponse
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : BaseViewModel(application) {
    private val repository = HomeRepository(application)

    private val _initialPostsLiveData: SingleLiveEvent<List<Post>> by lazy { SingleLiveEvent() }
    val initialPostsLiveData: LiveData<List<Post>> = _initialPostsLiveData
    private val _initialPostProgressLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val initialPostProgressLiveData: LiveData<Boolean> = _initialPostProgressLiveData
    private val _loadMorePostsLiveData: SingleLiveEvent<List<Post>> by lazy { SingleLiveEvent() }
    val loadMorePostsLiveData: LiveData<List<Post>> = _loadMorePostsLiveData
    private val _initialPostErrorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }
    val initialPostErrorLiveData: LiveData<ApiError> = _initialPostErrorLiveData
    private val _loadMorePostErrorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }
    val loadMorePostErrorLiveData: LiveData<ApiError> = _loadMorePostErrorLiveData
    private val _postDetailsLiveData: SingleLiveEvent<Post> by lazy { SingleLiveEvent() }
    val postDetailsLiveData: LiveData<Post> = _postDetailsLiveData
    private val _likePostErrorLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val likePostErrorLiveData: LiveData<Long> = _likePostErrorLiveData
    private val _updatePostLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val updatePostLiveData: LiveData<Long> = _updatePostLiveData
    private val _deletePostLiveData: SingleLiveEvent<Long> by lazy { SingleLiveEvent() }
    val deletePostLiveData: LiveData<Long> = _deletePostLiveData
    private val _voteSuccessLiveData: MutableLiveData<Post> by lazy { MutableLiveData() }
    val voteSuccessLiveData: LiveData<Post> = _voteSuccessLiveData
    private val _voteErrorLiveData: SingleLiveEvent<ApiError> by lazy { SingleLiveEvent() }
    val voteErrorLiveData: LiveData<ApiError> = _voteErrorLiveData
    private var currentPage: Int = 1
    private val limit = 5
    var canLoadMoreData = true

    init {
        getPosts()
    }

    fun getPosts() = viewModelScope.launch {
        if (currentPage == 1) _initialPostProgressLiveData.postValue(true)
        val response = processCoroutine({ repository.getPosts(currentPage, limit) }, updateProgress = false)
        if (currentPage == 1) _initialPostProgressLiveData.postValue(false)
        response.onSuccess {
            if (currentPage == 1) processInitialResponse(it)
            else processLoadMoreResponse(it)
        }.onError {
            if (currentPage == 1) _initialPostErrorLiveData.postValue(it)
            else _loadMorePostErrorLiveData.postValue(it)
        }
    }

    fun getPostDetails(postId: Long) = viewModelScope.launch {
        val response = processCoroutine({ processCoroutine({ repository.getPostDetails(postId) }) })
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
        val loadsFetchedFromServer = if (response.posts.isNullOrEmpty())
            emptyList()
        else
            response.posts
        setPaginationState(response)
        _loadMorePostsLiveData.postValue(loadsFetchedFromServer)
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

    fun deletePostFromFeed(postId: Long) {
        _deletePostLiveData.postValue(postId)
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