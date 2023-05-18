package com.humara.nagar.fcm

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("fcm_token") val fcmToken: String
)