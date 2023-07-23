package com.humara.nagar.utils

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.humara.nagar.constants.NetworkConstants

object GlideUtil {
    fun getUrlWithHeaders(url: String, context: Context): GlideUrl {
        val authToken = context.getUserSharedPreferences().token
        return GlideUrl(
            NetworkConstants.NetworkAPIConstants.BASE_MEDIA_URL.plus(url),
            LazyHeaders.Builder().addHeader(NetworkConstants.NetworkHeaderConstants.AUTHORIZATION, "Bearer $authToken")
                .addHeader(NetworkConstants.NetworkHeaderConstants.ACCEPT_LANGUAGE, context.getAppSharedPreferences().appLanguage)
                .build()
        )
    }
}