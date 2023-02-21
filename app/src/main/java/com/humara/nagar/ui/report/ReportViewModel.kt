package com.humara.nagar.ui.report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.network.onError
import com.humara.nagar.network.onSuccess
import com.humara.nagar.ui.report.model.ComplaintsRequest
import com.humara.nagar.ui.report.model.ComplaintIDResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.util.ArrayList


class ReportViewModel(
    application: Application
) : BaseViewModel(application) {

    private val _inputCategory: MutableLiveData<String> by lazy { MutableLiveData() }
    val inputCategory: LiveData<String> = _inputCategory
    private val _inputLocality: MutableLiveData<String> by lazy { MutableLiveData() }
    val inputLocality: LiveData<String> = _inputLocality
    private val _inputLocation: MutableLiveData<String> by lazy { MutableLiveData() }
    val inputLocation: LiveData<String> = _inputLocation
    private val _inputComment: MutableLiveData<String> by lazy { MutableLiveData() }
    val inputComment: LiveData<String> = _inputComment
    private val _inputImages: MutableLiveData<List<Uri>> by lazy { MutableLiveData() }
    val inputImages: LiveData<List<Uri>> = _inputImages
    private val _enableSubmitButtonLiveData: MutableLiveData<Boolean> by lazy { MutableLiveData() }
    val enableSubmitButtonLiveData: LiveData<Boolean> = _enableSubmitButtonLiveData
    private val repository = ReportRepository(application)
    val postReportComplaintLiveData: MutableLiveData<ComplaintIDResponse> = MutableLiveData()


    fun setCategory(category: String) {
        _inputCategory.value = category
        updateSubmitButtonState()
    }

    fun setLocality(locality: String) {
        _inputLocality.value = locality
        updateSubmitButtonState()
    }

    fun setLocation(location: String) {
        _inputLocation.value = location
        updateSubmitButtonState()
    }

    fun setComment(comment: String) {
        _inputComment.value = comment
        updateSubmitButtonState()
    }

    fun setImageList(imageList: List<Uri>) {
        _inputImages.value = imageList
        updateSubmitButtonState()
    }

    private fun createComplaintObjectWithCollectedData(imageMultiParts: ArrayList<MultipartBody.Part>): ComplaintsRequest {
        val complaints = ComplaintsRequest(
            category = inputCategory.value.toString(),
            locality = inputLocality.value.toString(),
            phone_number = getUserPreference().mobileNumber,
            location = inputLocation.value.toString(),
            comments = inputComment.value.toString(),
            images = imageMultiParts
        )
        return complaints
    }

    fun reportComplaint(imageMultiParts: ArrayList<MultipartBody.Part>) = viewModelScope.launch {
        val complaintsRequest = createComplaintObjectWithCollectedData(imageMultiParts)

        val response = processCoroutine({ repository.postReportComplaint(complaintsRequest) })
        response.onSuccess {
            postReportComplaintLiveData.value = it
        }.onError {
            errorLiveData.postValue(it)
        }
    }

    private fun updateSubmitButtonState() {
        val anyRequiredFieldEmpty = _inputCategory.value.isNullOrEmpty() || _inputLocality.value.isNullOrEmpty() || _inputComment.value.isNullOrEmpty() || _inputLocation.value.isNullOrEmpty() || _inputImages.value.isNullOrEmpty()
        _enableSubmitButtonLiveData.value = anyRequiredFieldEmpty.not()
    }
}