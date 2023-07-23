package com.humara.nagar.ui.home.post_details

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.home.HomeRepository
import com.humara.nagar.ui.home.model.LikeDetails
import kotlinx.coroutines.launch

class PostLikesViewModel(application: Application, val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    val repository: HomeRepository = HomeRepository(application)
    val postId: Long = PostLikesFragmentArgs.fromSavedStateHandle(savedStateHandle).postId
    private val _postLikesLiveData: MutableLiveData<List<LikeDetails>> = MutableLiveData()
    val postLikesLiveData: LiveData<List<LikeDetails>> = _postLikesLiveData

    init {
        getPostLikes()
    }

    private fun getPostLikes() = viewModelScope.launch {
        val response = processCoroutine({ repository.getPostLikes(postId) })
        response.onSuccess {
            _postLikesLiveData.postValue(it.likes)
        }.onError {
            errorLiveData.postValue(it)
        }
    }
}