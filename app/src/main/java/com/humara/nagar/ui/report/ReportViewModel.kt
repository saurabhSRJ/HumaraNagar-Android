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
import com.humara.nagar.utils.ImageUtils
import com.humara.nagar.utils.SingleLiveEvent
import com.humara.nagar.utils.StringUtils
import kotlinx.coroutines.launch

class ReportViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val CATEGORY_KEY = "category"
        private const val LOCALITY_KEY = "locality"
        private const val LOCATION_KEY = "location"
        private const val COMMENT_KEY = "comment"
        private const val IMAGES_KEY = "images"
        private const val SUBMIT_BUTTON_KEY = "submit"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
    }

    private val repository = ReportRepository(application)
    val imageUris = mutableListOf<Uri>()
    private val categoryData: LiveData<String> = savedStateHandle.getLiveData(CATEGORY_KEY)
    private val localityData: LiveData<String> = savedStateHandle.getLiveData(LOCALITY_KEY)
    private val locationData: LiveData<String> = savedStateHandle.getLiveData(LOCATION_KEY)
    private val latitudeData: LiveData<Double> = savedStateHandle.getLiveData(LATITUDE)
    private val longitudeData: LiveData<Double> = savedStateHandle.getLiveData(LONGITUDE)
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
        val response = processCoroutine({ repository.postComplaint(complaintsRequest, imageUris) })
        response.onSuccess {
            for (uri in imageUris) {
                ImageUtils.deleteTempFile(uri)
            }
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
        return PostComplaintRequest(category = categoryData.value!!, locality = localityData.value!!, user_id = getUserPreference().userId,
            location = StringUtils.replaceWhitespaces(locationData.value!!.trim()), comments = StringUtils.replaceWhitespaces(commentData.value!!.trim()),
            location_latitude = latitudeData.value, location_longitude = longitudeData.value)
    }
}