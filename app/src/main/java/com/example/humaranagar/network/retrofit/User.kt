package com.example.humaranagar.network.retrofit

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("avatar")
    val image: String,
    @SerializedName("email")
    val userEmail: String,
    @SerializedName("id")
    val userId: String,
    @SerializedName("name")
    val userName: String
)