package com.humara.nagar.permissions

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.humara.nagar.R
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.utils.PermissionUtils

/**
 * A headless fragment that provides a simple way to request runtime permissions from the user using requestPermissions().
 */
open class PermissionFragment : BaseFragment() {
    companion object {
        const val TAG = "PermissionFragment"
    }

    // A callback function that will be invoked when the permissions have been granted or denied
    private var handler: PermissionHandler? = null
    private var deniedPermissionList = mutableListOf<String>()
    private var isPermissionNecessary: Boolean = true

    // Activity result launcher for opening app settings
    private val appSettingLauncher = registerForActivityResult(OpenAppSettings()) {
        if (PermissionUtils.hasPermission(requireContext(), deniedPermissionList.last())) {
            handleGrantedPermission()
        }
    }

    // Activity result launcher for requesting multiple permissions initially
    private val requestMultiplePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
        val deniedPermissions = permissionsMap.filterValues { !it }.keys.toList()
        if (deniedPermissions.isEmpty()) {
            handler?.onPermissionGranted()
        } else {
            // We will request for each of the denied permission one-by-one using singlePermissionLauncher and deniedPermissionList
            deniedPermissionList.addAll(deniedPermissions)
            handleDeniedPermission(deniedPermissionList.last())
        }
    }

    // Activity result launcher for requesting a single permission which is denied once
    private val singlePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            handleGrantedPermission()
        } else {
            handlePermanentlyDeniedPermission()
        }
    }

    /**
     * Requests the specified permissions from the user.
     *
     * @param permissions An array of permission strings to be requested.
     * @param handler A callback function that will be invoked when the permissions have been granted or denied.
     * @param isPermissionNecessary: If true, we will open app settings to ask for each permanently denied permission. If false, requesting fragment can handle using [PermissionHandler.onPermissionDenied] callback
     */
    fun requestPermissions(permissions: Array<String>, handler: PermissionHandler, isPermissionNecessary: Boolean = true) {
        this.handler = handler
        this.isPermissionNecessary = isPermissionNecessary
        //Check if all the permission are already allowed
        if (PermissionUtils.hasPermissions(requireContext(), permissions)) {
            handler.onPermissionGranted()
        } else {
            // Launch the permission request for the specified permissions
            requestMultiplePermissionLauncher.launch(permissions)
        }
    }

    private fun handleDeniedPermission(permission: String) {
        // In some cases permissions are coupled in same group like (WRITE_EXTERNAL_STORAGE & READ_EXTERNAL_STORAGE). Granting WRITE_EXTERNAL_STORAGE first automatically provide READ_EXTERNAL_STORAGE
        // This check avoids that scenario
        if (PermissionUtils.hasPermission(requireContext(), permission)) {
            handleGrantedPermission()
            return
        }
        // If permission is temporarily denied, show rationale dialog
        val showPermissionRationalePopup = PermissionUtils.isPermissionTemporaryDenied(requireActivity(), permission)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && showPermissionRationalePopup) {
            showPermissionRationaleDialog(permission)
        } else {
            handlePermanentlyDeniedPermission()
        }
    }

    private fun handleGrantedPermission() {
        deniedPermissionList.removeLast()
        if (deniedPermissionList.isEmpty()) {
            handler?.onPermissionGranted()
        } else {
            handleDeniedPermission(deniedPermissionList.last())
        }
    }

    private fun handlePermanentlyDeniedPermission() {
        if (isPermissionNecessary) {
            openAppSettingToAskPermanentlyDeniedPermissions()
        } else {
            handler?.onPermissionDenied(deniedPermissionList)
        }
    }

    private fun showPermissionRationaleDialog(permission: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.permission_required_title))
            .setMessage(getPermissionGuideMessage(permission))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                builder.create().dismiss()
                singlePermissionLauncher.launch(permission)
            }
            .setCancelable(true)
        builder.create().show()
    }

    private fun openAppSettingToAskPermanentlyDeniedPermissions() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.permission_required_title))
            .setMessage(getPermanentlyDeniedPermissionRequestMessage(deniedPermissionList.last()))
            .setPositiveButton(getString(R.string.grant_permission)) { _, _ ->
                builder.create().dismiss()
                appSettingLauncher.launch(requireContext().packageName)
            }
            .setCancelable(true)
        builder.create().show()
    }

    private fun getPermissionGuideMessage(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> getString(R.string.camera_permission_denied_message)
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE -> getString(R.string.storage_permission_denied_message)
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION -> getString(R.string.location_permission_denied_message)
            Manifest.permission.POST_NOTIFICATIONS -> getString(R.string.notification_permission_denied_message)
            else -> getString(R.string.please_provide_requested_permission)
        }
    }

    private fun getPermanentlyDeniedPermissionRequestMessage(permission: String): String {
        val permissionName = when (permission) {
            Manifest.permission.CAMERA -> getString(R.string.camera_permission)
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE -> getString(R.string.storage_permission)
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION -> getString(R.string.location_permission)
            else -> " "
        }
        return getString(R.string.permnanetly_denied_permission_message, permissionName)
    }

    inner class OpenAppSettings : ActivityResultContract<String, Unit>() {
        override fun createIntent(context: Context, input: String): Intent {
            val uri = Uri.fromParts("package", input, null)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = uri
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?) {
            // This method is not used for this contract
        }
    }

    override fun getScreenName() = "Permission Fragment"

    override fun shouldLogScreenView() = false
}