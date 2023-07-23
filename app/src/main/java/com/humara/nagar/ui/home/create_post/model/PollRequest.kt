package com.humara.nagar.ui.home.create_post.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PollRequest(
    @SerializedName("caption") var caption: String = "",
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("expiry_time") val expiryTime: String
) : Parcelable