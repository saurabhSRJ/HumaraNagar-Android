package com.humara.nagar.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


object PermissionsUtils {

    const val REQUEST_CODE = 1

    fun checkUserRequestedDontAskAgain(fragment: Fragment, permissions: Array<out String>) : Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var isPermissionDenies = false
            for (permission in permissions) {
                isPermissionDenies = isPermissionDenies || fragment.shouldShowRequestPermissionRationale(permission).not()
            }
            return isPermissionDenies
        }
        return false
    }

    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
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

}