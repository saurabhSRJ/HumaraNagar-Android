package com.humara.nagar.ui.signup.model

import com.google.gson.annotations.SerializedName

data class AppConfigResponse(
    @SerializedName("role") val role: String,
    @SerializedName("role_id") val roleId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String?,
    @SerializedName("app_update_config") val appUpdateConfig: AppUpdateConfig? = null
)

data class AppUpdateConfig(
    @SerializedName("update_type") val updateType: String,
    @SerializedName("description") val description: String?
)

enum class AppUpdateType {
    FORCE_UPDATE,
    OPTIONAL_UPDATE
}