package com.humara.nagar.ui.report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.report.model.PostComplaintRequest
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.Utils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ReportViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val CATEGORY_KEY = "category"
        private const val LOCALITY_KEY = "locality"
        private const val LOCATION_KEY = "location"
        private const val COMMENT_KEY = "comment"
        private const val IMAGES_KEY = "images"
        private const val SUBMIT_BUTTON_KEY = "submit"
    }

    private val repository = ReportRepository(application)
    val imageUris = mutableListOf<Uri>()
    private val categoryData: LiveData<String> = savedStateHandle.getLiveData(CATEGORY_KEY)
    private val localityData: LiveData<String> = savedStateHandle.getLiveData(LOCALITY_KEY)
    private val locationData: LiveData<String> = savedStateHandle.getLiveData(LOCATION_KEY)
    private val commentData: LiveData<String> = savedStateHandle.getLiveData(COMMENT_KEY)
    val imagesData: LiveData<List<Uri>> = savedStateHandle.getLiveData(IMAGES_KEY, mutableListOf())
    val submitButtonStateData: LiveData<Boolean> = savedStateHandle.getLiveData(SUBMIT_BUTTON_KEY, false)
    private val _postComplaintStatusLiveData: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val postComplaintStatusLiveData: SingleLiveEvent<Boolean> = _postComplaintStatusLiveData

    init {
        imagesData.value?.let { imageUris.addAll(it) }
    }

    fun postComplaint() = viewModelScope.launch {
        val complaintsRequest = getComplaintObjectWithCollectedData()
        val response = processCoroutine({ repository.postComplaint(complaintsRequest) })
        response.onSuccess {
            _postComplaintStatusLiveData.postValue(true)
        }.onError {
            _postComplaintStatusLiveData.postValue(false)
        }
    }

    fun setCategory(category: String) {
        savedStateHandle[CATEGORY_KEY] = category
        updateSubmitButtonState()
    }

    fun setLocality(locality: String) {
        savedStateHandle[LOCALITY_KEY] = locality
        updateSubmitButtonState()
    }

    fun setLocation(location: String) {
        savedStateHandle[LOCATION_KEY] = location
        updateSubmitButtonState()
    }

    fun setComment(comment: String) {
        savedStateHandle[COMMENT_KEY] = comment
        updateSubmitButtonState()
    }

    fun addImages(imageList: List<Uri>) {
        imageUris.addAll(imageList)
        savedStateHandle[IMAGES_KEY] = imageUris
        updateSubmitButtonState()
    }

    fun deleteImage(index: Int) {
        imageUris.removeAt(index)
        savedStateHandle[IMAGES_KEY] = imageUris
        updateSubmitButtonState()
    }

    fun deleteAllImages() {
        imageUris.clear()
        savedStateHandle[IMAGES_KEY] = imageUris
        updateSubmitButtonState()
    }

    private fun updateSubmitButtonState() {
        val anyRequiredFieldEmpty = categoryData.value.isNullOrEmpty() || localityData.value.isNullOrEmpty() || commentData.value.isNullOrEmpty() || locationData.value.isNullOrEmpty() || imagesData.value.isNullOrEmpty()
        savedStateHandle[SUBMIT_BUTTON_KEY] = anyRequiredFieldEmpty.not()
    }

    private fun getComplaintObjectWithCollectedData(): PostComplaintRequest {
        return PostComplaintRequest(category = categoryData.value!!, locality = localityData.value!!, phone_number = getUserPreference().mobileNumber,
            location = Utils.replaceWhitespace(locationData.value!!.trim()), comments = Utils.replaceWhitespace(commentData.value!!.trim()), images = getImageMultipart())
    }

    private fun getImageMultipart(): ArrayList<MultipartBody.Part> {
        val imageParts = ArrayList<MultipartBody.Part>()
        for (filePath in imageUris) {
            val file = filePath.path?.let { File(it) }
            val requestBody = file?.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = requestBody?.let { MultipartBody.Part.createFormData("image", file.name, it) }
            if (imagePart != null) {
                imageParts.add(imagePart)
            }
        }
        return imageParts
    }
}