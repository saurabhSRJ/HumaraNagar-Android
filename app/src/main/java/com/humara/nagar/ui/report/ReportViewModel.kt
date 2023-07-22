package com.humara.nagar.ui.report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.ApiError
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.report.model.PostComplaintRequest
import com.humara.nagar.ui.signup.model.CategoryDetails
import com.humara.nagar.ui.signup.model.WardDetails
import com.humara.nagar.utils.ImageUtils
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.StringUtils
import kotlinx.coroutines.launch

class ReportViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val CATEGORY_KEY = "category"
        private const val WARD_KEY = "ward"
        private const val LOCATION_KEY = "location"
        private const val COMMENT_KEY = "comment"
        private const val IMAGES_KEY = "images"
        private const val SUBMIT_BUTTON_KEY = "submit"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
    }

    private val repository = ReportRepository(application)
    val imageUris = mutableListOf<Uri>()
    private val categoryData: LiveData<CategoryDetails> = savedStateHandle.getLiveData(CATEGORY_KEY)
    private val wardData: LiveData<WardDetails> = savedStateHandle.getLiveData(WARD_KEY)
    private val locationData: LiveData<String> = savedStateHandle.getLiveData(LOCATION_KEY)
    private val latitudeData: LiveData<Double> = savedStateHandle.getLiveData(LATITUDE)
    private val longitudeData: LiveData<Double> = savedStateHandle.getLiveData(LONGITUDE)
    private val commentData: LiveData<String> = savedStateHandle.getLiveData(COMMENT_KEY)
    val imagesData: LiveData<List<Uri>> = savedStateHandle.getLiveData(IMAGES_KEY, mutableListOf())
    val submitButtonStateData: LiveData<Boolean> = savedStateHandle.getLiveData(SUBMIT_BUTTON_KEY, false)
    private val _postComplaintSuccessLiveData: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val postComplaintSuccessLiveData: SingleLiveEvent<Boolean> = _postComplaintSuccessLiveData
    private val _postComplaintErrorLiveData: SingleLiveEvent<ApiError> = SingleLiveEvent()
    val postComplaintErrorLiveData: LiveData<ApiError> =_postComplaintErrorLiveData

    init {
        imagesData.value?.let { imageUris.addAll(it) }
    }

    fun postComplaint() = viewModelScope.launch {
        val complaintsRequest = getComplaintObjectWithCollectedData()
        val response = processCoroutine({ repository.postComplaint(complaintsRequest, imageUris) })
        for (uri in imageUris) {
            ImageUtils.deleteTempFile(uri)
        }
        response.onSuccess {
            _postComplaintSuccessLiveData.postValue(true)
        }.onError {
            _postComplaintErrorLiveData.postValue(it)
        }
    }

    fun setCategory(categoryDetails: CategoryDetails) {
        savedStateHandle[CATEGORY_KEY] = categoryDetails
        updateSubmitButtonState()
    }

    fun setWard(wardDetails: WardDetails) {
        savedStateHandle[WARD_KEY] = wardDetails
        updateSubmitButtonState()
    }

    fun setLocation(location: String) {
        savedStateHandle[LOCATION_KEY] = location
        updateSubmitButtonState()
    }

    fun setLocationCoordinates(latitude: Double, longitude: Double) {
        savedStateHandle[LATITUDE] = latitude
        savedStateHandle[LONGITUDE] = longitude
    }

    fun setComment(comment: String) {
        savedStateHandle[COMMENT_KEY] = comment
        updateSubmitButtonState()
    }

    fun addImages(imageList: List<Uri>) {
        imageUris.addAll(imageList)
        savedStateHandle[IMAGES_KEY] = imageList
        updateSubmitButtonState()
    }

    fun deleteImage(index: Int) {
        ImageUtils.deleteTempFile(imageUris[index])
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
        val anyRequiredFieldEmpty = categoryData.value == null || wardData.value == null || commentData.value.isNullOrEmpty() || locationData.value.isNullOrEmpty() ||
                imagesData.value.isNullOrEmpty()
        savedStateHandle[SUBMIT_BUTTON_KEY] = anyRequiredFieldEmpty.not()
    }

    private fun getComplaintObjectWithCollectedData(): PostComplaintRequest {
        return PostComplaintRequest(category_id = categoryData.value!!.id, ward_id = wardData.value!!.id,
            location = StringUtils.replaceWhitespaces(locationData.value!!), comments = StringUtils.replaceWhitespaces(commentData.value!!),
            location_latitude = latitudeData.value ?: -1.0, location_longitude = longitudeData.value ?: -1.0)
    }
}