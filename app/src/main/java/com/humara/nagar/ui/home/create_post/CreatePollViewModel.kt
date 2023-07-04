package com.humara.nagar.ui.home.create_post

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.humara.nagar.base.BaseViewModel
import com.humara.nagar.ui.home.create_post.model.PollRequest
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.StringUtils

class CreatePollViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : BaseViewModel(application) {
    companion object {
        private const val POLL_QUESTION = "poll_question"
        private const val FIRST_OPTION = "first_option"
        private const val SECOND_OPTION = "second_option"
        private const val THIRD_OPTION = "third_option"
        private const val FOURTH_OPTION = "fourth_option"
        private const val POLL_DURATION = "poll_duration"
        private const val DONE_BUTTON_STATE = "done_state"
    }

    private val pollQuestionLiveData: LiveData<String> = savedStateHandle.getLiveData(POLL_QUESTION)
    private val firstOptionLiveData: LiveData<String> = savedStateHandle.getLiveData(FIRST_OPTION)
    private val secondOptionLiveData: LiveData<String> = savedStateHandle.getLiveData(SECOND_OPTION)
    val thirdOptionLiveData: LiveData<String> = savedStateHandle.getLiveData(THIRD_OPTION)
    val fourthOptionLiveData: LiveData<String> = savedStateHandle.getLiveData(FOURTH_OPTION)
    private val pollDurationLiveData: LiveData<Int> = savedStateHandle.getLiveData(POLL_DURATION, 3)
    val doneButtonStateLiveData: LiveData<Boolean> = savedStateHandle.getLiveData(DONE_BUTTON_STATE)
    private val _pollRequestLiveData: MutableLiveData<PollRequest> by lazy { MutableLiveData() }
    val pollRequestLiveData: LiveData<PollRequest> = _pollRequestLiveData

    fun setQuestion(question: String) {
        savedStateHandle[POLL_QUESTION] = question
        updateDoneButtonState()
    }

    fun setFirstOption(option: String) {
        savedStateHandle[FIRST_OPTION] = option
        updateDoneButtonState()
    }

    fun setSecondOption(option: String) {
        savedStateHandle[SECOND_OPTION] = option
        updateDoneButtonState()
    }

    fun setThirdOption(option: String) {
        savedStateHandle[THIRD_OPTION] = option
    }

    fun setFourthOption(option: String) {
        savedStateHandle[FOURTH_OPTION] = option
    }

    fun setPollDuration(duration: Int) {
        savedStateHandle[POLL_DURATION] = duration
    }

    fun getPollRequestObjectWithCollectedData() {
        val request = PollRequest(
            question = StringUtils.replaceWhitespaces(pollQuestionLiveData.value!!),
            options = getPollOptions(),
            expiryTime = DateTimeUtils.getFutureDateTimeInIsoFormat(pollDurationLiveData.value!!)
        )
        _pollRequestLiveData.postValue(request)
    }

    private fun updateDoneButtonState() {
        val anyRequiredFieldEmpty = pollQuestionLiveData.value.isNullOrEmpty() || firstOptionLiveData.value.isNullOrEmpty() || secondOptionLiveData.value.isNullOrEmpty()
        savedStateHandle[DONE_BUTTON_STATE] = anyRequiredFieldEmpty.not()
    }

    private fun getPollOptions(): List<String> {
        return mutableListOf(StringUtils.replaceWhitespaces(firstOptionLiveData.value!!), StringUtils.replaceWhitespaces(secondOptionLiveData.value!!)).apply {
            thirdOptionLiveData.value?.let { add(StringUtils.replaceWhitespaces(it)) }
            fourthOptionLiveData.value?.let { add(StringUtils.replaceWhitespaces(it)) }
        }
    }
}