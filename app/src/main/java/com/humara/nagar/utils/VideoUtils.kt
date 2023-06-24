package com.humara.nagar.utils

import android.content.Context
import android.net.Uri
import com.humara.nagar.R
import com.humara.nagar.constants.Constants
import com.humara.nagar.constants.NetworkConstants

object VideoUtils {
    fun isValidVideoSize(context: Context, uri: Uri): Boolean {
        return if (FileUtils.getMediaSize(context, uri) > Constants.MAX_VIDEO_SIZE_IN_BYTES) {
            context.showToast(context.getString(R.string.video_file_size_error_message, Constants.MAX_VIDEO_SIZE_IN_MB), true)
            false
        } else {
            true
        }
    }

    fun getVideoUrl(url: String): String {
        return NetworkConstants.NetworkAPIConstants.BASE_MEDIA_URL.plus(url)
    }
}