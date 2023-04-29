package com.humara.nagar.ui.report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.humara.nagar.base.BaseViewModel

class ReportViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val CATEGORY_KEY = "category"
        private const val LOCALITY_KEY = "locality"
        private const val LOCATION_KEY = "location"
        private const val COMMENT_KEY = "comment"
        private const val IMAGES_KEY = "images"
        private const val SUBMIT_BUTTON_KEY = "submit"
    }
    val imageUris = mutableListOf<Uri>()
    private val categoryData: String? = savedStateHandle[CATEGORY_KEY]
    private val localityData: String? = savedStateHandle[LOCALITY_KEY]
    private val locationData: String? = savedStateHandle[LOCATION_KEY]
    fun getImages(): LiveData<List<Uri>> = savedStateHandle.getLiveData(IMAGES_KEY, mutableListOf())
    fun getSubmitButtonState(): LiveData<Boolean> = savedStateHandle.getLiveData(SUBMIT_BUTTON_KEY, false)

    init {
        getImages().value?.let { imageUris.addAll(it) }
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

    private fun getComment(): String? = savedStateHandle[COMMENT_KEY]

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

    private fun updateSubmitButtonState() {
        val anyRequiredFieldEmpty = categoryData.isNullOrEmpty() || localityData.isNullOrEmpty() || getComment().isNullOrEmpty() || locationData.isNullOrEmpty() || getImages().value.isNullOrEmpty()
        savedStateHandle[SUBMIT_BUTTON_KEY] = anyRequiredFieldEmpty.not()
    }
}