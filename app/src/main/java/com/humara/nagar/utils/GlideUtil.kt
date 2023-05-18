package com.humara.nagar.utils

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.humara.nagar.BuildConfig
import com.humara.nagar.constants.NetworkConstants

object GlideUtil {
    private const val BASE_IMAGE_URL = "${BuildConfig.BASE_URL}/image/"
    fun getUrlWithHeaders(url: String, context: Context): GlideUrl {
        val authToken = context.getUserSharedPreferences().token
        return GlideUrl(
            BASE_IMAGE_URL + url,
            LazyHeaders.Builder().addHeader(NetworkConstants.NetworkHeaderConstants.AUTHORIZATION, "Bearer $authToken").build()
        )
    }
}