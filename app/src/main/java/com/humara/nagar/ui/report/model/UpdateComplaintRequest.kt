package com.humara.nagar.ui.report.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateComplaintRequest(
    @SerializedName("comment") val comment: String,
    @SerializedName("resolution_expected_time") val resolutionExpectedTime: String? = null
): Parcelable
