package com.humara.nagar.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    val storagePermissions = getStoragePermissions()
    val cameraPermissions = getCameraPermissions()
    val locationPermissions = getLocationPermission()
    val notificationPermissions = getNotificationPermissions()

    @JvmName("getNotificationPermissions1")
    private fun getNotificationPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            permissions = arrayOf(Manifest.permission.CAMERA)
        }
        return permissions
    }

    @JvmName("getStoragePermissions1")
    private fun getStoragePermissions(): Array<String> {
        var permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
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