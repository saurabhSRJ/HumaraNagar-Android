package com.humara.nagar.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.humara.nagar.BuildConfig
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

object FileUtils {
    const val TAG = "FileUtil"
    private const val MAX_FILE_NAME_LENGTH = 20
    private const val TEMP_FILE_NAME_PREFIX = BuildConfig.APPLICATION_ID.plus("_document")
    private const val TEMP_FILE_NAME_SUFFIX = ".pdf"
    private const val TEMP_FILE_NAME_FULL = TEMP_FILE_NAME_PREFIX.plus(TEMP_FILE_NAME_SUFFIX)

    suspend fun createTempUriFromContentUri(context: Context, uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            var tempFile: File = File.createTempFile(TEMP_FILE_NAME_PREFIX, TEMP_FILE_NAME_SUFFIX)
            val newFile: File? = rename(tempFile, TEMP_FILE_NAME_FULL)
            tempFile = newFile ?: return@withContext null
            val outputStream = FileOutputStream(tempFile)
            copyStreamToFile(inputStream, outputStream)
            inputStream?.close()
            Uri.fromFile(tempFile)
        } catch (e: FileNotFoundException) {
            Logger.logException(TAG, e, Logger.LogLevel.ERROR, true)
            return@withContext null
        }
    }

    /**
     * Get the file's display name from content uri
     * @return display name if content uri else empty string
     */
    fun getFileName(context: Context, uri: Uri): String {
        var result = ""
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    result = cursor.getString(nameIndex)
                }
            }
        }
        return StringUtils.showDotStringAfterLimitReached(MAX_FILE_NAME_LENGTH, result)
    }

    fun getFileName(url: String?): String {
        var result = ""
        url?.let {
            val cut = url.lastIndexOf(File.separator)
            if (cut != -1) {
                result = url.substring(cut + 1)
            }
        }
        return StringUtils.showDotStringAfterLimitReached(MAX_FILE_NAME_LENGTH, result)
    }

    fun rename(file: File, newName: String): File? {
        val newFile = File(file.parent, newName)
        try {
            if (newFile.exists() && !newFile.delete()) {
                Logger.debugLog("File delete operation failed")
                return null
            } else {
                Logger.debugLog("Delete old $newName file")
            }

            if (file.renameTo(newFile)) {
                Logger.debugLog("Rename file to $newName")
                return newFile
            } else {
                Logger.debugLog("Rename file operation failed")
            }
        } catch (e: IOException) {
            Logger.debugLog("Exception in delete or rename file operation: ${e.message}")
        }
        return null
    }

    fun copyStreamToFile(inputStream: InputStream?, outputStream: OutputStream) {
        /*
            use extension executes the given block function on this resource and then closes it down correctly whether an exception is thrown or not.
            This technique is also known as try-with-resources in the JVM community
         */
        inputStream?.let { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    fun isValidDocumentSize(context: Context, uri: Uri): Boolean {
        return if (getMediaSize(context, uri) > Constants.MAX_DOCUMENT_SIZE_IN_BYTES) {
            context.showToast(context.getString(R.string.document_file_size_error_message, Constants.MAX_DOCUMENT_SIZE_IN_MB), true)
            false
        } else {
            true
        }
    }

    fun openPdfFile(context: Context, file: File) {
        val path = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
        val pdfOpeningIntent = Intent(Intent.ACTION_VIEW)
        pdfOpeningIntent.setDataAndType(path, "application/pdf")
        pdfOpeningIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(pdfOpeningIntent)
        } catch (ignore: ActivityNotFoundException) {
            context.showToast(context.getString(R.string.error_opening_file))
        }
    }

    fun openPdfUrl(context: Context, url: String) {
        try {
            val pdfOpeningIntent = Intent(Intent.ACTION_VIEW)
            pdfOpeningIntent.setDataAndType(Uri.parse(url), "application/pdf")
            pdfOpeningIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(pdfOpeningIntent)
        } catch (ex: Exception) {
            context.showToast(context.getString(R.string.error_opening_file))
        }
    }

    /**
     * @returns the document size in bytes
     */
    fun getMediaSize(context: Context, uri: Uri): Int {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null, null)
        var size: Int = Int.MAX_VALUE
        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                // If the size is unknown, the value stored is null. But because an int can't be null, the behavior is implementation-specific, and unpredictable.
                // So as a rule, check if it's null before assigning to an int. This will happen often: The storage API allows for remote files, whose size might not be locally known.
                if (!it.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString() will do the conversion automatically.
                    size = it.getInt(sizeIndex)
                }
            }
        }
        Logger.debugLog("File size: $size")
        return size
    }
}