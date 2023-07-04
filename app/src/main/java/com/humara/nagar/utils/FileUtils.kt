package com.humara.nagar.utils

import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.constants.Constants
import java.io.File
import java.io.FileNotFoundException

object FileUtils {
    private const val MAX_FILE_NAME_LENGTH = 20

    /**
     * Get the file's display name from uri
     * @return display name if uri is content uri else empty string
     */
    fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        var result = ""
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    result = cursor.getString(nameIndex)
                }
            }
        }
        return result
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

    fun isValidDocumentSize(context: Context, uri: Uri): Boolean {
        return if (getMediaSize(context, uri) > Constants.MAX_DOCUMENT_SIZE_IN_BYTES) {
            context.showToast(context.getString(R.string.document_file_size_error_message, Constants.MAX_DOCUMENT_SIZE_IN_MB), true)
            false
        } else {
            true
        }
    }

    fun openPdfFile(context: Context, contentUri: Uri) {
        val pdfOpeningIntent = Intent(Intent.ACTION_VIEW)
        pdfOpeningIntent.setDataAndType(contentUri, "application/pdf")
        pdfOpeningIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(pdfOpeningIntent)
        } catch (ignore: ActivityNotFoundException) {
            context.showToast(context.getString(R.string.error_opening_file))
        }
    }

    fun openPdfUrl(context: Context, url: String) {
        try {
            val pdfOpeningIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(url), "application/pdf")
            }
            context.startActivity(pdfOpeningIntent)
        } catch (ex: Exception) {
            context.showToast(context.getString(R.string.error_opening_file))
        }
    }

    /**
     * @returns the document size in bytes
     */
    fun getMediaSize(context: Context, uri: Uri): Int {
        val cursor: Cursor? = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null, null)
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

    fun Uri.length(contentResolver: ContentResolver): Long {
        val assetFileDescriptor = try {
            contentResolver.openAssetFileDescriptor(this, "r")
        } catch (e: FileNotFoundException) {
            null
        }
        // uses ParcelFileDescriptor#getStatSize underneath if failed
        val length = assetFileDescriptor?.use { it.length } ?: -1L
        if (length != -1L) {
            return length
        }

        // if "content://" uri scheme, try contentResolver table
        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            return contentResolver.query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex == -1) {
                        return@use -1L
                    }
                    cursor.moveToFirst()
                    return try {
                        cursor.getLong(sizeIndex)
                    } catch (_: Throwable) {
                        -1L
                    }
                } ?: -1L
        } else {
            return -1L
        }
    }
}