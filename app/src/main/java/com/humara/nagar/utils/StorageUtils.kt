package com.humara.nagar.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import com.humara.nagar.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reference: https://developer.android.com/topic/performance/graphics/load-bitmap.html#load-bitmap
 * https://github.com/ShashankPednekar/ImagePicker-and-Compression
 */
object StorageUtils {
    @Throws(IOException::class)
    fun Context.getBitmapFromUri(uri: Uri, options: BitmapFactory.Options? = null): Bitmap? {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image: Bitmap? = if (options != null)
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
        else
            BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //save a temp file
        return File.createTempFile("JPEG_${getTimestampString()}", ".jpeg", storageDir)
    }

    // Create a unique image file name based on timestamp
    fun getTimestampString(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    }

    /**
     * Reduces the image dimensions and memory size through scaling and compression
     *
     * @param context The context used for accessing resources and file operations.
     * @param uri The URI of the image file to be compressed.
     * @param maxHeight The maximum height of the compressed image.
     * @param maxWidth The maximum width of the compressed image.
     * @return The URI of the compressed image file, or the original URI if compression fails.
     */
    suspend fun compressImageFile(context: Context, uri: Uri, maxHeight: Int = 360, maxWidth: Int = 480): Uri {
        return withContext(Dispatchers.IO) {
            var scaledBitmap: Bitmap? = null
            try {
                // Step 1: Decode and scale down the image file according to maxHeight and maxWidth
                scaledBitmap = decodeSampleBitmapFromFile(context, uri, maxWidth, maxHeight)
                // Step 2: Compress and save the image to the temporary file
                val tmpFile = createImageFile(context)
                val fos = FileOutputStream(tmpFile)
                scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()
                Logger.debugLog("final height: ${scaledBitmap?.height} width: ${scaledBitmap?.width}")
                // Step 3: Check if the compressed file was successfully created
                if (tmpFile.exists() && tmpFile.length() > 0) {
                    Logger.debugLog("final image size in KB: ${tmpFile.length() / 1024}")
                    return@withContext Uri.fromFile(tmpFile)
                } else {
                    Logger.debugLog("Failed to create compressed image file")
                    return@withContext uri
                }
            } catch (e: Throwable) {
                Logger.debugLog("Failed to compress image: ${e.message}")
                return@withContext uri
            } finally {
                // Step 4: Recycle the scaled bitmap to release memory
                scaledBitmap?.recycle()
            }
        }
    }

    /**
     * Decodes an image file from the given URI with the desired target width and height.
     * Reduces the in-memory size before loading the image using [BitmapFactory.Options.inSampleSize]
     */
    private fun decodeSampleBitmapFromFile(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        // First decode with inJustDecodeBounds = true to get original dimensions while decoding avoids memory allocation
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            context.getBitmapFromUri(uri, this)
            inSampleSize = calculateInSampleSize(this.outWidth, this.outHeight, reqWidth, reqHeight)
            inJustDecodeBounds = false
            context.getBitmapFromUri(uri, this)
        }
    }

    /**
     * Calculate optimal down-sampling factor given the dimensions of a source image and the dimensions of a destination area.
     */
    private fun calculateInSampleSize(srcWidth: Int, srcHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        val maxDimension = maxOf(srcHeight, srcWidth)
        val reqDimension = maxOf(reqHeight, reqWidth)
        // Calculate the inSampleSize value as the nearest power of 2 that keeps the maximum dimension larger than or equal to the requested dimension.
        if (maxDimension > reqDimension) {
            val ratio = maxDimension / reqDimension
            inSampleSize = Integer.highestOneBit(ratio)
            if (inSampleSize < ratio) {
                inSampleSize *= 2
            }
        }
        Logger.debugLog("inSampleSize: $inSampleSize")
        return inSampleSize
    }
}