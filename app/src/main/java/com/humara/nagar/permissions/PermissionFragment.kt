package com.humara.nagar.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.ui.common.GenericAlertDialog
import com.humara.nagar.utils.IntentUtils
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
    private val appSettingLauncher = registerForActivityResult(OpenAppSettings()) { information ->
        this.deniedPermissionList = information.permissionList
        this.handler = information.handler
        if (PermissionUtils.hasPermission(requireContext(), deniedPermissionList.last())) {
            handleGrantedPermission()
        } else {
            //TODO: Handle permanently denied permission not given from app settings
            Logger.debugLog(TAG, "permission not granted")
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
        if (showPermissionRationalePopup) {
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
        GenericAlertDialog.show(parentFragmentManager, getString(R.string.permission_required_title), getPermissionGuideMessage(permission),
            isCancelable = true, getString(R.string.ok), getString(R.string.cancel)) {
            singlePermissionLauncher.launch(permission)
        }
    }

    private fun openAppSettingToAskPermanentlyDeniedPermissions() {
        GenericAlertDialog.show(parentFragmentManager, getString(R.string.permission_required_title), getPermanentlyDeniedPermissionRequestMessage(deniedPermissionList.last()),
            isCancelable = true, getString(R.string.grant_permission), getString(R.string.cancel)) {
            appSettingLauncher.launch(PermissionInformation(handler, deniedPermissionList, IntentUtils.getAppSettingIntent(requireContext())))
        }
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

    data class PermissionInformation(
        val handler: PermissionHandler?,
        val permissionList: MutableList<String>,
        var intent: Intent
    )

    class OpenAppSettings : ActivityResultContract<PermissionInformation, PermissionInformation>() {
        companion object {
            private lateinit var information: PermissionInformation
        }

        override fun createIntent(context: Context, input: PermissionInformation): Intent {
            information = input
            return input.intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): PermissionInformation {
            intent?.let { information.intent = it }
            return information
        }
    }

    override fun getScreenName() = "Permission Fragment"
}