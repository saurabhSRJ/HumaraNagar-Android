package com.humara.nagar.ui.home.create_post

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.Logger
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.utils.StringUtils
import kotlinx.coroutines.launch

class CreatePostViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val CAPTION = "caption"
        private const val DOCUMENT_URI = "document_uri"
        private const val IMAGE_URI = "image_uri"
        private const val ATTACHMENT_AVAILABLE = "attachment_available"
    }

    private val repository = CreatePostRepository(application)
    private val captionLiveData: LiveData<String> = savedStateHandle.getLiveData(CAPTION)
    val documentUriLiveData: LiveData<Uri> = savedStateHandle.getLiveData(DOCUMENT_URI)
    val imageUriLiveData: LiveData<Uri> = savedStateHandle.getLiveData(IMAGE_URI)
    val attachmentAvailableLivedata: LiveData<Boolean> = savedStateHandle.getLiveData(ATTACHMENT_AVAILABLE)
    private val _postCreationSuccessLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val postCreationSuccessLiveData: LiveData<Boolean> = _postCreationSuccessLiveData

    fun createPost() {
        if (imageUriLiveData.value != null) {
            createImagePost()
        } else if (documentUriLiveData.value != null) {
            createDocumentPost()
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
        val response = processCoroutine({ repository.createImagePost(StringUtils.replaceWhitespaces(captionLiveData.value!!.trim()), imageUriLiveData.value!!) })
        response.onSuccess {
            _postCreationSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun createDocumentPost() = viewModelScope.launch {
        val response = processCoroutine({ repository.createDocumentPost(StringUtils.replaceWhitespaces(captionLiveData.value!!.trim()), documentUriLiveData.value!!) })
        response.onSuccess {
            _postCreationSuccessLiveData.postValue(true)
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    fun clearAttachmentData() {
        savedStateHandle[CAPTION] = null
        savedStateHandle[DOCUMENT_URI] = null
        savedStateHandle[IMAGE_URI] = null
        updateAttachmentViewState()
    }

    private fun updateAttachmentViewState() {
        val attachmentAvailable = imageUriLiveData.value == null && documentUriLiveData.value == null
        savedStateHandle[ATTACHMENT_AVAILABLE] = attachmentAvailable.not()
    }

    fun setCaption(caption: String) {
        savedStateHandle[CAPTION] = caption
        updateAttachmentViewState()
    }

    fun setDocumentUri(uri: Uri) {
        savedStateHandle[DOCUMENT_URI] = uri
        updateAttachmentViewState()
    }

    fun setImageUri(uri: Uri) {
        savedStateHandle[IMAGE_URI] = uri
        Logger.debugLog("caption: ${captionLiveData.value}, imageUri: ${imageUriLiveData.value}")
    }
}