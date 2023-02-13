package com.humara.nagar.ui.report

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.adapter.ImagePreviewAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentReportBinding
import com.humara.nagar.databinding.ToolbarLayoutBinding
import com.humara.nagar.utils.PermissionsUtils
import com.humara.nagar.utils.Utils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation


class ReportFragment : BaseFragment() {

    private var _binding: FragmentReportBinding? = null
    private var currentPhotoPath: String? = null
    private val reportViewModel by viewModels<ReportViewModel> {
        ViewModelFactory()
    }
    private var requestedPermissions = mutableListOf<String>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter

    private val PICK_IMAGE_MULTIPLE = 1
    private val maxSelection = 2
    private val REQUEST_IMAGE_CAPTURE = 2
    private val imageList = mutableListOf<Uri>()
    private lateinit var toolbar: ToolbarLayoutBinding
    private var isLocationPicked = false

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

        binding.inputLocation.setLayoutListener(true) {
            if (!isLocationPicked) {
                checkForLocationPermission()
            }
        }

        binding.addImageLayout.setOnClickListener {
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
            toolbar = includedToolbar
            toolbar.toolbarTitle.text = resources.getString(R.string.reportIssueTitle)

            //Settings up list for spinners
            inputCategory.setOptions(R.array.category_list)
            inputLocality.setOptions(R.array.locality_list)
            inputLocality.setRequiredInput(true)
            inputComment.apply {
                switchToMultiLined()
                setMaxLength(300)
                setMultiLined()
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

            reportViewModel.inputImages.observe(viewLifecycleOwner) {
                if (it.isNotEmpty())
                    binding.uploadImageRequired.visibility = View.GONE
                else
                    binding.uploadImageRequired.visibility = View.VISIBLE
            }

            recyclerView = imagePreviewItemRCV
            imagePreviewAdapter = ImagePreviewAdapter {
                imageList.removeAt(it)
                imagePreviewAdapter.setData(imageList)
                reportViewModel.setImageList(imageList)
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
                adapter = imagePreviewAdapter
            }
        }
    }

    private fun showPictureDialog() {
        if (imageList.size >= maxSelection) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.imagePickingLimit),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
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
        val storagePermissions = PermissionsUtils.storagePermissions
        if (!PermissionsUtils.checkPermissions(requireContext(), storagePermissions)) {
            PermissionsUtils.requestPermissions(this@ReportFragment, storagePermissions)
            requestedPermissions.clear()
            requestedPermissions = storagePermissions.toMutableList()
        } else {
            val intent = Intent()
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, resources.getString(R.string.select_images)),
                PICK_IMAGE_MULTIPLE
            )
        }
    }

    private fun takePhotoFromCamera() {
        val permissions = PermissionsUtils.cameraPermissions
        if (!PermissionsUtils.checkPermissions(requireContext(), permissions)) {
            PermissionsUtils.requestPermissions(this@ReportFragment, permissions)
            requestedPermissions.clear()
            requestedPermissions = permissions.toMutableList()
        } else {
            clickPicture()
        }
    }

    private fun clickPicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = createImageFile()
        val imageUri =
            FileProvider.getUriForFile(
                requireContext(),
                resources.getString(R.string.provider_name),
                imageFile
            )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
            Logger.debugLog("Exception caught: $e")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun getAddress() {
        try {
            val addresses = Utils.getAddressFromLongAndLat(requireContext())
            val address = addresses[0]
            val addressLine = address.getAddressLine(0)
            val city = address.locality
            val state = address.adminArea
            val country = address.countryName
            val postalCode = address.postalCode
            Logger.debugLog("Address:\nAddressLine-> $addressLine\nCity-> $city\nState-> $state\nCountry-> $country\nPostalCode-> $postalCode")
            binding.inputLocation.setInput(addressLine)
            isLocationPicked = true
            reportViewModel.setLocation(addressLine)
        } catch (e: java.lang.Exception) {
            Logger.debugLog("Exception caught at getAddress: $e")
        }
    }

    private fun checkForLocationPermission() {
        val permissions = PermissionsUtils.locationPermissions
        if (!PermissionsUtils.checkPermissions(requireContext(), permissions)) {
            PermissionsUtils.requestPermissions(this@ReportFragment, permissions)
            requestedPermissions.clear()
            requestedPermissions = permissions.toMutableList()
        } else getAddress()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK) {
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                if (count > maxSelection - imageList.size) {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.imagePickingLimit),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    imageUri?.let { uri ->
                        imageList.add(uri)
                        uri.path?.let { it ->
                            Logger.debugLog("File name: ${File(it).name}")
                        }
                    }
                    imagePreviewAdapter.setData(imageList)
                    reportViewModel.setImageList(imageList)
                }
            } else if (data?.data != null) {
                data.data?.let {
                    imageList.add(it)
                    imagePreviewAdapter.addData(listOf(it))
                    reportViewModel.setImageList(imageList)
                }
            } else {
                //Image clicked is null
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            currentPhotoPath?.let { path ->
                val imageUri = Uri.fromFile(File(path))
                imageList.add(imageUri)
                imagePreviewAdapter.addData(listOf(imageUri))
                reportViewModel.setImageList(imageList)
            }
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
            }
        }
        if (allPermissionsGranted) {
            // All permissions were granted. You can proceed with the action that requires these permissions.
            if (permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
                permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
            ) {
                getAddress()
            } else if (permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                choosePhotoFromGallery()
            } else if (permissions.contains(Manifest.permission.CAMERA)) {
                clickPicture()
            }
        } else {
            // One or more permissions were denied. You should show a message to the user explaining why you need these permissions.
            Logger.debugLog("Denied permission: $deniedPermissions")
            PermissionsUtils.handlePermissionDenied(
                deniedPermissions.toTypedArray(),
                requireContext()
            )
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyToolTipCounter = getUserPreference().historyToolTipCounter

        if (historyToolTipCounter < 3) {
            val balloon = Balloon.Builder(requireContext())
                .setArrowSize(10)
                .setIsVisibleArrow(true)
                .setArrowPosition(0.85f)
                .setWidthRatio(0.65f)
                .setHeight(56)
                .setCornerRadius(4f)
                .setAlpha(0.9f)
                .setTextSize(14f)
                .setAutoDismissDuration(5000L)
                .setTextColor(Color.WHITE)
                .setBackgroundColor(resources.getColor(R.color.grey_4F4F4F))
                .setText(resources.getString(R.string.trackYourPastComplaints))
                .setBalloonAnimation(BalloonAnimation.FADE)
                .build()

            getUserPreference().historyToolTipCounter = historyToolTipCounter + 1
            balloon.showAlignBottom(binding.includedToolbar.rightIcon)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.REPORT_FRAGMENT
}