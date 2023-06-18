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
import com.humara.nagar.constants.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

object FileUtils {
    private const val MAX_FILE_NAME_LENGTH = 20
    const val TAG = "FileUtil"

    suspend fun createFileFromContentUri(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName: String = getFileName(context, uri)
        val splitName: Array<String> = splitFileName(fileName)
        var tempFile = File.createTempFile(splitName[0], splitName[1])
        tempFile = rename(tempFile, fileName)
        tempFile.deleteOnExit()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(tempFile)
        } catch (e: FileNotFoundException) {
            Logger.logException(TAG, e, Logger.LogLevel.ERROR, true)
        }
        out?.let {
            copyStreamToFile(inputStream, out)
            inputStream?.close()
        }
        out?.close()
        tempFile
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result = ""
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
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

    private fun splitFileName(fileName: String): Array<String> {
        var name = fileName
        var extension = ""
        val i = fileName.lastIndexOf(".")
        if (i != -1) {
            name = fileName.substring(0, i)
            extension = fileName.substring(i)
        }
        return arrayOf(name, extension)
    }

    private fun rename(file: File, newName: String): File {
        val newFile = File(file.parent, newName)
        if (newFile != file) {
            if (newFile.exists() && newFile.delete()) {
                Logger.debugLog("Delete old $newName file")
            }
            if (file.renameTo(newFile)) {
                Logger.debugLog("Rename file to $newName")
            }
        }
        return newFile
    }

    private fun copyStreamToFile(inputStream: InputStream?, outputStream: OutputStream) {
        inputStream?.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    fun isValidDocumentSize(context: Context, uri: Uri): Boolean {
        return if (getDocumentSize(context, uri) > Constants.MAX_DOCUMENT_SIZE_IN_BYTES) {
            context.showToast("Maximum file size should be 4MB", true)
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
            context.showToast("Error opening file", true)
        }
    }

    fun openPdfUrl(context: Context, url: String) {
        try {
            val pdfOpeningIntent = Intent(Intent.ACTION_VIEW)
            pdfOpeningIntent.setDataAndType(Uri.parse(url), "application/pdf")
            pdfOpeningIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(pdfOpeningIntent)
        } catch (ex: Exception) {
            context.showToast("Error opening file")
        }
    }

    /**
     * @returns the document size in bytes
     */
    private fun getDocumentSize(context: Context, uri: Uri): Int {
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