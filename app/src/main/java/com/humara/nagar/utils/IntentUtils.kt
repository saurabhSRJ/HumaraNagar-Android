package com.humara.nagar.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import com.humara.nagar.BuildConfig
import com.humara.nagar.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object IntentUtils {
    fun hasIntent(context: Context, intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }

    fun getCallIntent(phoneNumber: String): Intent {
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
    }

    fun getAppSettingIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
    }

    fun getOpenDocumentIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
    }

    fun shareViaIntent(activity: Activity, bitmap: Bitmap?, text: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            bitmap?.let {
                val filesDir: File = activity.applicationContext.filesDir
                val imageFile = File(filesDir, "JPEG_${ImageUtils.getTimestampString()}.jpeg")
                val os: OutputStream
                try {
                    os = FileOutputStream(imageFile)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                    os.flush()
                    os.close()
                } catch (e: Exception) {
                    Logger.logException("SharePostStorage", e, Logger.LogLevel.ERROR, true)
                }
                val imgBitmapUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID, imageFile)
                putExtra(Intent.EXTRA_STREAM, imgBitmapUri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        activity.startActivity(shareIntent)
    }

    fun getCameraIntent(context: Context, uri: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        grantWritePermission(context, intent, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return intent
    }

    fun getVideoCaptureIntent(context: Context, uri: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        grantWritePermission(context, intent, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return intent
    }

    fun getImageGalleryIntent(): Intent {
        return getGalleryIntent(arrayOf("image/*"))
    }

    fun getGalleryIntent(): Intent {
        return getGalleryIntent(arrayOf("image/*", "video/*"))
    }

    private fun getGalleryIntent(mimetypes: Array<String>): Intent {
        if (DeviceHelper.isMinSdk33) {
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            if (mimetypes.size == 1) {
                // ACTION_PICK_IMAGES supports only two mimetypes.
                intent.type = mimetypes[0]
            }
            return intent
        }
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return Intent.createChooser(intent, "Select Images")
    }

    /**
     * This method constructs an Intent that can be used to launch the Google Maps app and display
     * the location specified by the latitude and longitude coordinates. The location is marked with
     * a label "Complaint Location" on the map.
     * For more information, please refer to the (https://developers.google.com/maps/documentation/urls/android-intents)
     */
    fun getGoogleMapIntent(latitude: Double, longitude: Double): Intent {
        val uri = Uri.parse("geo:0,0?q=$latitude,$longitude(Complaint+Location)")
        val mapIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = uri
            setPackage("com.google.android.apps.maps")
        }
        return mapIntent
    }

    private fun grantWritePermission(context: Context, intent: Intent, uri: Uri) {
        val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }
}