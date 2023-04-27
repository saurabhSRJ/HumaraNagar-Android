package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class AppConfigResponse(
    @SerializedName("role") val role: String?
)

enum class Role(val role: String) {
    ADMIN("admin"),
    RESIDENT("resident")
}