package com.humara.nagar.ui.common

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.humara.nagar.base.BaseViewModel

class MediaSelectionViewModel(application: Application): BaseViewModel(application) {
    private val _imagesLiveData: MutableLiveData<List<Uri>> = MutableLiveData()
    val imagesLiveData: LiveData<List<Uri>> = _imagesLiveData

    fun addImages(images: List<Uri>) {
        _imagesLiveData.value = images
    }
}