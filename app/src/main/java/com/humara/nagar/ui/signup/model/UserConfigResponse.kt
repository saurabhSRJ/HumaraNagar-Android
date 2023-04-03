package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class UserConfigResponse(
    @SerializedName("role") val role: String?
)

enum class ROLE(val role: String) {
    ADMIN("admin"),
    RESIDENT("resident")
}