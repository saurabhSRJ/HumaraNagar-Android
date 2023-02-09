package com.humara.nagar.ui.report

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentReportBinding
import com.humara.nagar.utils.PermissionsUtils
import com.humara.nagar.utils.Utils

class ReportFragment : BaseFragment() {

    private var _binding: FragmentReportBinding? = null
    private val reportViewModel by activityViewModels<ReportViewModel> {
        ViewModelFactory()
    }
    private lateinit var toolBarTitle: TextView
    private lateinit var toolBarBackButton: ImageView
    private lateinit var toolBarHistoryButton: ImageView
    private var requestedPermissions = mutableListOf<String>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)

        initViewModelObservers()
        initView()

        binding.inputLocation.setLayoutListener {
            checkForLocationPermission()
        }

        binding.addBtn.setOnClickListener {
            showPictureDialog()
        }

        return binding.root
    }

    private fun initViewModelObservers() {
        reportViewModel.run {
            enableSubmitButtonLiveData.observe(viewLifecycleOwner) {
                binding.btnSubmit.isEnabled = it
            }
        }
    }

    private fun initView() {

        binding.run {

            //Setting up the top app bar title
            toolBarTitle = root.findViewById(R.id.toolbar_title)
            toolBarTitle.text = resources.getString(R.string.reportIssueTitle)
            toolBarBackButton = root.findViewById(R.id.leftIcon)
            toolBarHistoryButton = root.findViewById(R.id.rightIcon)

            //Settings up list for spinners
            inputCategory.setOptions(R.array.category_list)
            inputLocality.setOptions(R.array.locality_list)
            inputLocality.setRequiredInput(true)
            inputComment.apply {
                switchToMultiLined()
            }

            inputCategory.setUserInputListener {
                reportViewModel.setCategory(it)
                Logger.debugLog(it)
            }
            inputLocality.setUserInputListener {
                reportViewModel.setLocality(it)
                Logger.debugLog(it)
            }
            inputLocation.setUserInputListener {
                reportViewModel.setLocation(it)
                Logger.debugLog(it)
            }
            inputComment.setUserInputListener {
                reportViewModel.setComment(it)
                Logger.debugLog(it)
            }
        }
    }

    private fun getAddress() {
        val addresses = Utils.getAddressFromLongAndLat(requireContext())
        val address = addresses[0]
        val addressLine = address.getAddressLine(0)
        val city = address.locality
        val state = address.adminArea
        val country = address.countryName
        val postalCode = address.postalCode
        Logger.debugLog("Address:\nAddressLine-> $addressLine\nCity-> $city\nState-> $state\nCountry-> $country\nPostalCode-> $postalCode")
        binding.inputLocation.setInput(addressLine)
        reportViewModel.setLocation(addressLine)
    }

    private fun checkForLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (!PermissionsUtils.checkPermissions(requireContext(), permissions)) {
            PermissionsUtils.requestPermissions(this@ReportFragment, permissions)
            requestedPermissions.clear()
            requestedPermissions = permissions.toMutableList()
        } else getAddress()
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(requireContext())
        pictureDialog.setTitle(resources.getString(R.string.selectAction))
        val pictureDialogItems = arrayOf(
            resources.getString(R.string.selectFromGallery),
            resources.getString(R.string.captureFromCamera)
        )
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        val dialog = pictureDialog.create()
        dialog.show()
    }

    private fun choosePhotoFromGallery() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (!PermissionsUtils.checkPermissions(requireContext(), permissions)) {
            PermissionsUtils.requestPermissions(this@ReportFragment, permissions)
            requestedPermissions.clear()
            requestedPermissions = permissions.toMutableList()
        } else {
            //Pick multiple pictures (limit max 5)
        }
    }

    private fun takePhotoFromCamera() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA
        )
        if (!PermissionsUtils.checkPermissions(requireContext(), permissions)) {
            PermissionsUtils.requestPermissions(this@ReportFragment, permissions)
            requestedPermissions.clear()
            requestedPermissions = permissions.toMutableList()
        } else {
            //Take picture
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var allPermissionsGranted = true
        val deniedPermissions = mutableListOf<String>()
        for (i in requestedPermissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Logger.debugLog("NOT GRANTED ${requestedPermissions[i]}")
                allPermissionsGranted = false
                deniedPermissions.add(requestedPermissions[i])
                break
            } else {
                Logger.debugLog("GRANTED ${requestedPermissions[i]}")
                break
            }
        }
        if (allPermissionsGranted) {
            // All permissions were granted. You can proceed with the action that requires these permissions.
            if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                getAddress()
            }
        } else {
            // One or more permissions were denied. You should show a message to the user explaining why you need these permissions.
            Logger.debugLog("Denied permission: $deniedPermissions")
            handlePermissionDenied(deniedPermissions.toTypedArray())
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun handlePermissionDenied(permissions: Array<String>) {
        val showPermissionRationalePopup: Boolean = permissions.isNotEmpty()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && showPermissionRationalePopup) {
            for (permission in permissions) {
                showRationaleDialog(
                    resources.getString(R.string.permission_required_title),
                resources.getString(R.string.premission_required_description),
                permission, PermissionsUtils.REQUEST_CODE
                )
                break
            }
        } else {
            gotoAppSettingsPermission()
        }
    }

    fun showRationaleDialog(title: String, message: String, permission: String, requestCode: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                requestPermissions(arrayOf(permission), requestCode)
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
                builder.create().dismiss()
            }
            .setNeutralButton(resources.getString(R.string.gotoSettings)) { _, _ ->
                gotoAppSettingsPermission()
            }
            .setCancelable(true)
        builder.create().show()
    }

    private fun gotoAppSettingsPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivityForResult(intent, PermissionsUtils.REQUEST_CODE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.REPORT_FRAGMENT
}