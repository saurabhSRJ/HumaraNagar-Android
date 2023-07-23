package com.humara.nagar.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    val storagePermissions = getStoragePermissions()
    val cameraPermissions = getCameraPermissions()
    val locationPermissions = getLocationPermission()
    val notificationPermissions = getNotificationPermissions()
    val sharePostPermissions = getSharePostPermissions()

    @JvmName("getNotificationPermissions1")
    private fun getNotificationPermissions(): Array<String> {
        return if (DeviceHelper.isMinSdk33) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf()
        }
    }

    @JvmName("getLocationPermission1")
    private fun getLocationPermission(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @JvmName("getCameraPermissions1")
    private fun getCameraPermissions(): Array<String> {
        var permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (DeviceHelper.isMinSdk29) {
            permissions = arrayOf(Manifest.permission.CAMERA)
        }
        return permissions
    }

    @JvmName("getStoragePermissions1")
    private fun getStoragePermissions(): Array<String> {
        return if (DeviceHelper.isMinSdk33) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            //TODO: this should be changed to READ_EXTERNAL_STORAGE permission only
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    @JvmName("getSharePostPermissions1")
    private fun getSharePostPermissions(): Array<String> {
        var permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (DeviceHelper.isMinSdk29) {
            permissions = emptyArray()
        }
        return permissions
    }

    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (hasPermission(context, permission).not()) {
                return false
            }
        }
        return true
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the app has requested this permission previously but the user denied the request and we need to show a UI with rationale for asking permission
     * @param activity parent activity
     */
    fun isPermissionTemporaryDenied(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
}