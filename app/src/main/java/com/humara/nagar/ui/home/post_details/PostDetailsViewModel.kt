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
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.utils.SingleLiveEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PostDetailsViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val IS_LIKED = "is_liked"
        private const val TOTAL_LIKES = "total_likes"
        private const val TOTAL_COMMENTS = "total_comments"
    }

    private val repository = PostRepository(application)
    private val postId: Long = PostDetailsFragmentArgs.fromSavedStateHandle(savedStateHandle).postId
    private val _postDetailsLiveData: MutableLiveData<Post> by lazy { MutableLiveData() }
    val postDetailsLiveData: LiveData<Post> = _postDetailsLiveData
    private val _postDetailsErrorLiveData: MutableLiveData<ApiError> by lazy { MutableLiveData() }
    val postDetailsErrorLiveData: LiveData<ApiError> = _postDetailsErrorLiveData
    private val _likePostErrorLiveData: SingleLiveEvent<Boolean> by lazy { SingleLiveEvent() }
    val likePostErrorLiveData: LiveData<Boolean> = _likePostErrorLiveData
    val isLikedLiveData: LiveData<Boolean> = savedStateHandle.getLiveData<Boolean>(IS_LIKED, false)
    val totalLikesLiveData: LiveData<Int> = savedStateHandle.getLiveData<Int>(TOTAL_LIKES, 0)

    init {
        getPostDetails()
    }

    private fun getPostDetails() = viewModelScope.launch {
        val postDetailsDeferred = async { processCoroutine({ repository.getPostDetails(postId) }) }
        val postDetailsResult = postDetailsDeferred.await()
        postDetailsResult.onSuccess {
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

    private fun likePost() = viewModelScope.launch {
        updateLikeData(true)
        val response = processCoroutine({ repository.likePost(postId) })
        response.onError {
            updateLikeData(false)
            _likePostErrorLiveData.postValue(true)
        }.onException {
            updateLikeData(false)
        }
    }

    private fun unlikePost() = viewModelScope.launch {
        updateLikeData(false)
        val response = processCoroutine({ repository.unlikePost(postId) })
        response.onError {
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