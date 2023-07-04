package com.humara.nagar.ui.signup.signup_or_login.model

import com.google.gson.annotations.SerializedName
import com.humara.nagar.ui.signup.model.UserInfo

data class SignupResponse(
    @SerializedName("user_info") val userInfo: UserInfo
)
