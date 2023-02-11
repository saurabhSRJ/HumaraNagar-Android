package com.humara.nagar.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.humara.nagar.R


object PermissionsUtils {

    const val REQUEST_CODE = 1

    val storagePermissions = getStoragePermissions()
    val cameraPermissions = getCameraPermissions()
    val locationPermissions = getLocationPermission()

    private fun getLocationPermission(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    @JvmName("getCameraPermissions1")
    private fun getCameraPermissions(): Array<String> {
        var permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            permissions = emptyArray()
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return permissions
    }

    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun requestPermissions(activity: Activity, permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE)
    }

    fun requestPermissions(fragment: Fragment, permissions: Array<String>) {
        fragment.requestPermissions(permissions, REQUEST_CODE)
    }

    fun handlePermissionDenied(permissions: Array<String>, context: Context) {
        val showPermissionRationalePopup: Boolean = permissions.isNotEmpty()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && showPermissionRationalePopup) {
            for (permission in permissions) {
                showRationaleDialog(
                    context.resources.getString(R.string.permission_required_title),
                    context.resources.getString(R.string.premission_required_description),
                    permission, REQUEST_CODE, context
                )
                break
            }
        } else {
            gotoAppSettingsPermission(context)
        }
    }

    private fun showRationaleDialog(
        title: String,
        message: String,
        permission: String,
        requestCode: Int,
        context: Context
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setNegativeButton(context.resources.getString(R.string.cancel)) { _, _ ->
                builder.create().dismiss()
            }
            .setPositiveButton(context.resources.getString(R.string.gotoSettings)) { _, _ ->
                gotoAppSettingsPermission(context)
            }
            .setCancelable(true)
        builder.create().show()
    }

    private fun gotoAppSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        (context as Activity).startActivityForResult(intent, REQUEST_CODE)
    }
}