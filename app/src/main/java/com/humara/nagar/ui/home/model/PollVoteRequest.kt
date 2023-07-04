package com.humara.nagar.ui.home.model

import com.google.gson.annotations.SerializedName

data class PollVoteRequest(
    @SerializedName("option_id") val optionId: Int
)