package com.humara.nagar.utils

import android.content.Context
import android.net.Uri
import com.humara.nagar.BuildConfig
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

object VideoUtils {
    const val TAG = "VideoUtil"
    private const val TEMP_FILE_NAME_PREFIX = BuildConfig.APPLICATION_ID.plus("_video")
    private const val TEMP_FILE_NAME_SUFFIX = ".mp4"
    private const val TEMP_FILE_NAME_FULL = TEMP_FILE_NAME_PREFIX.plus(TEMP_FILE_NAME_SUFFIX)

    suspend fun createTempUriFromContentUri(context: Context, uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            var tempFile: File = File.createTempFile(TEMP_FILE_NAME_PREFIX, TEMP_FILE_NAME_SUFFIX)
            tempFile = FileUtils.rename(tempFile, TEMP_FILE_NAME_FULL) ?: return@withContext null
            val outputStream = FileOutputStream(tempFile)
            FileUtils.copyStreamToFile(inputStream, outputStream)
            inputStream?.close()
            Uri.fromFile(tempFile)
        } catch (e: FileNotFoundException) {
            Logger.logException(TAG, e, Logger.LogLevel.ERROR, true)
            return@withContext null
        }
    }

    fun isValidVideoSize(context: Context, uri: Uri): Boolean {
        return if (FileUtils.getMediaSize(context, uri) > Constants.MAX_VIDEO_SIZE_IN_BYTES) {
            context.showToast(context.getString(R.string.video_file_size_error_message, Constants.MAX_VIDEO_SIZE_IN_MB), true)
            false
        } else {
            true
        }
    }

    fun getVideoUrl(url: String): String {
        return BuildConfig.BASE_URL.plus("/media/$url")
    }
}