package com.humara.nagar.ui.home.create_post

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.home.create_post.model.PollRequest
import com.humara.nagar.utils.ImageUtils
import com.humara.nagar.utils.StringUtils
import kotlinx.coroutines.launch

class CreatePostViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val CAPTION = "caption"
        private const val DOCUMENT_URI = "document_uri"
        private const val IMAGE_URI = "image_uri"
        private const val VIDEO_URI = "video_uri"
        private const val POLL_REQUEST = "poll_request"
        private const val ATTACHMENT_AVAILABLE = "attachment_available"
        private const val POST_ENABLED = "post_enabled"
    }

    private val repository = CreatePostRepository(application)
    private val captionLiveData: LiveData<String> = savedStateHandle.getLiveData(CAPTION)
    val documentUriLiveData: LiveData<Uri> = savedStateHandle.getLiveData(DOCUMENT_URI)
    val imageUriLiveData: LiveData<Uri> = savedStateHandle.getLiveData(IMAGE_URI)
    val videoUriLiveData: LiveData<Uri> = savedStateHandle.getLiveData(VIDEO_URI)
    val pollRequestLiveData: LiveData<PollRequest> = savedStateHandle.getLiveData(POLL_REQUEST)
    val attachmentAvailableLivedata: LiveData<Boolean> = savedStateHandle.getLiveData(ATTACHMENT_AVAILABLE)
    private val _postCreationSuccessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val postCreationSuccessLiveData: LiveData<Boolean> = _postCreationSuccessLiveData
    val postButtonStateLiveData: LiveData<Boolean> = savedStateHandle.getLiveData(POST_ENABLED)

    fun createPost() {
        if (imageUriLiveData.value != null) {
            createImagePost()
        } else if (documentUriLiveData.value != null) {
            createDocumentPost()
        } else if (pollRequestLiveData.value != null) {
            createPollPost()
        } else if (videoUriLiveData.value != null) {
            createVideoPost()
        } else if (captionLiveData.value != null) {
            createTextPost()
        }
    }

    private fun createTextPost() = viewModelScope.launch {
        val response = processCoroutine({ repository.createTextPost(captionLiveData.value!!) })
        response.onSuccess {
            _postCreationSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun createImagePost() = viewModelScope.launch {
        val response = processCoroutine({ repository.createImagePost(StringUtils.replaceWhitespaces(captionLiveData.value ?: ""), imageUriLiveData.value!!) })
        response.onSuccess {
            ImageUtils.deleteTempFile(imageUriLiveData.value)
            _postCreationSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun createDocumentPost() = viewModelScope.launch {
        val response = processCoroutine({ repository.createDocumentPost(StringUtils.replaceWhitespaces(captionLiveData.value ?: ""), documentUriLiveData.value!!) })
        response.onSuccess {
            _postCreationSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun createVideoPost() = viewModelScope.launch {
        val response = processCoroutine({ repository.createVideoPost(StringUtils.replaceWhitespaces(captionLiveData.value ?: ""), videoUriLiveData.value!!) })
        response.onSuccess {
            _postCreationSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun createPollPost() = viewModelScope.launch {
        val pollRequest = pollRequestLiveData.value!!.apply {
            caption = StringUtils.replaceWhitespaces(captionLiveData.value ?: "")
        }
        val response = processCoroutine({ repository.createPollPost(pollRequest) })
        response.onSuccess {
            _postCreationSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun clearAttachmentData() {
        savedStateHandle[DOCUMENT_URI] = null
        savedStateHandle[IMAGE_URI] = null
        savedStateHandle[POLL_REQUEST] = null
        savedStateHandle[VIDEO_URI] = null
        updateViewStates()
    }

    fun setCaption(caption: String) {
        savedStateHandle[CAPTION] = caption
        updateViewStates()
    }

    fun setDocumentUri(uri: Uri) {
        savedStateHandle[DOCUMENT_URI] = uri
        updateViewStates()
    }

    fun setImageUri(uri: Uri) {
        savedStateHandle[IMAGE_URI] = uri
        updateViewStates()
    }

    fun setVideoUri(uri: Uri) {
        savedStateHandle[VIDEO_URI] = uri
        updateViewStates()
    }

    fun setPollData(pollRequest: PollRequest) {
        savedStateHandle[POLL_REQUEST] = pollRequest
        updateViewStates()
    }

    private fun updateViewStates() {
        val attachmentAvailable = imageUriLiveData.value == null && documentUriLiveData.value == null && pollRequestLiveData.value == null && videoUriLiveData.value == null
        savedStateHandle[ATTACHMENT_AVAILABLE] = attachmentAvailable.not()
        savedStateHandle[POST_ENABLED] = attachmentAvailableLivedata.value == true || captionLiveData.value.isNullOrEmpty().not()
    }
}