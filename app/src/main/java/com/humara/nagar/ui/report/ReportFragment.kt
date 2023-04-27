package com.humara.nagar.ui.report

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.adapter.ImagePreviewAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentReportBinding
import com.humara.nagar.databinding.ToolbarLayoutBinding
import com.humara.nagar.permissions.PermissionFragment
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.utils.IntentUtils
import com.humara.nagar.utils.PermissionUtils
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val maxLength: Int = 300

class ReportFragment : PermissionFragment() {
    companion object {
        const val TAG = "ReportFragment"
    }

    private var _binding: FragmentReportBinding? = null
    private var currentPhotoPath: String? = null
    private val reportViewModel by viewModels<ReportViewModel> {
        ViewModelFactory()
    }
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter
    private val maxSelection = 2
    private val imageList = mutableListOf<Uri>()
    private lateinit var toolbar: ToolbarLayoutBinding
    private var isLocationPicked = false

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val getContentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent: Intent? = result?.data
        val resultCode: Int = result.resultCode
        if (resultCode != RESULT_OK || intent == null) return@registerForActivityResult
        if (isFragmentAlive()) {
            onImageSelection(intent)
        }
    }

    private val takeCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode: Int = result.resultCode
        if (resultCode != RESULT_OK || context == null) return@registerForActivityResult
        if (isFragmentAlive()) {
            onImageCapture()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        initViewModelObservers()
        initView()
        return binding.root
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
                .setBackgroundColor(ResourcesCompat.getColor(resources, R.color.grey_4F4F4F, null))
                .setText(resources.getString(R.string.trackYourPastComplaints))
                .setBalloonAnimation(BalloonAnimation.FADE)
                .build()

            getUserPreference().historyToolTipCounter = historyToolTipCounter + 1
            balloon.showAlignBottom(binding.includedToolbar.rightIcon)
        }
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
            addImageLayout.setOnClickListener {
                showPictureDialog()
            }
            //Setting up the top app bar title
            toolbar = includedToolbar
            toolbar.toolbarTitle.text = resources.getString(R.string.reportIssueTitle)
            toolbar.rightIconTV.apply {
                text = resources.getString(R.string.history)
                visibility = View.VISIBLE
            }
            //Settings up list for spinners
            inputCategory.setOptions(R.array.category_list)
            inputLocality.setOptions(R.array.locality_list)
            inputLocality.setRequiredInput(true)
            inputComment.apply {
                switchToMultiLined()
                setMaxLength(maxLength)
                setMultiLined(maxLength)
            }
            inputCategory.setUserInputListener {
                reportViewModel.setCategory(it)
            }
            inputLocality.setUserInputListener {
                reportViewModel.setLocality(it)
            }
            inputLocation.setLayoutListener(true) {
                if (!isLocationPicked) {
                    checkForLocationPermission()
                }
            }
            inputLocation.setUserInputListener {
                reportViewModel.setLocation(it)
            }
            inputComment.setUserInputListener {
                reportViewModel.setComment(it)
            }
            reportViewModel.inputImages.observe(viewLifecycleOwner) {
                binding.uploadImageRequired.isVisible = it.isEmpty()
            }
            imagePreviewAdapter = ImagePreviewAdapter {
                imageList.removeAt(it)
                imagePreviewAdapter.setData(imageList)
                reportViewModel.setImageList(imageList)
            }
            imagePreviewItemRCV.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(false)
                adapter = imagePreviewAdapter
            }
        }
    }

    private fun showPictureDialog() {
        if (imageList.size >= maxSelection) {
            Toast.makeText(requireContext(), resources.getString(R.string.imagePickingLimit), Toast.LENGTH_SHORT).show()
            return
        }
        val pictureDialog = AlertDialog.Builder(requireContext())
        pictureDialog.setTitle(resources.getString(R.string.selectAction))
        val pictureDialogItems = arrayOf(
            resources.getString(R.string.selectFromGallery),
            resources.getString(R.string.captureFromCamera)
        )
        pictureDialog.setItems(pictureDialogItems) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        val dialog = pictureDialog.create()
        dialog.show()
    }

    private fun choosePhotoFromGallery() {
        requestPermissions(PermissionUtils.storagePermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                val intent = IntentUtils.getImageGalleryIntent()
                getContentLauncher.launch(intent)
            }
            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun takePhotoFromCamera() {
        requestPermissions(PermissionUtils.cameraPermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                clickPicture()
            }
            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun clickPicture() {
        val imageFile = createImageFile()
        val imageUri = FileProvider.getUriForFile(
                requireContext(),
                resources.getString(R.string.provider_name),
                imageFile
            )
        val intent: Intent = IntentUtils.getCameraIntent(requireContext(), imageUri)
        if (IntentUtils.hasIntent(requireContext(), intent)) {
            takeCameraLauncher.launch(intent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    @Suppress("DEPRECATION")
    private fun getAddress() {
        showProgress(false)
        val client = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Get the last known location. In some rare situations, this can be null.
        client.lastLocation.addOnSuccessListener { lastLocation ->
            lastLocation?.let { location ->
                // Logic to handle location object.
                val errorMessage: String
                var addresses: List<Address>? = null
                try {
                    val geocoder = Geocoder(requireContext(), Locale("en", "IN"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) {
                            addresses = it
                        }
                    } else {
                        addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    }
                } catch (ioException: IOException) {
                    errorMessage = "Service Not Available"
                    Logger.debugLog(TAG, errorMessage)
                } catch (illegalArgumentException: IllegalArgumentException) {
                    errorMessage = "Invalid Latitude or Longitude Used"
                    Logger.debugLog(TAG, "$errorMessage. Latitude = ${location.latitude}, Longitude = ${location.longitude}")
                } finally {
                    hideProgress()
                    addresses?.get(0)?.let { address ->
                        val addressLine = address.getAddressLine(0)
                        val city = address.locality
                        val state = address.adminArea
                        val country = address.countryName
                        val postalCode = address.postalCode
                        Logger.debugLog(TAG, "Address:\nAddressLine-> $addressLine\nCity-> $city\nState-> $state\nCountry-> $country\nPostalCode-> $postalCode")
                        binding.inputLocation.setInput(addressLine)
                        isLocationPicked = true
                        reportViewModel.setLocation(addressLine)
                    }
                }
            }
        }
    }

    private fun checkForLocationPermission() {
        requestPermissions(PermissionUtils.locationPermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                getAddress()
            }
            override fun onPermissionDenied(permissions: List<String>) {
                //TODO: Add a common helper dialog with message about permanently denied permission
                Toast.makeText(requireContext(), getString(R.string.you_can_still_enter_location_manually), Toast.LENGTH_LONG).show()
            }
        }, isPermissionNecessary = false)
    }

    private fun onImageSelection(data: Intent?) {
        data?.clipData?.let { selectedImages ->
            val count = selectedImages.itemCount
            if (count > maxSelection - imageList.size) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.imagePickingLimit),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            for (i in 0 until count) {
                val imageUri = selectedImages.getItemAt(i).uri
                imageUri?.let { uri ->
                    imageList.add(uri)
                    uri.path?.let { it ->
                        Logger.debugLog("File name: ${File(it).name}")
                    }
                }
            }
            imagePreviewAdapter.setData(imageList)
            reportViewModel.setImageList(imageList)
        } ?: data?.data?.let {
            imageList.add(it)
            imagePreviewAdapter.addData(listOf(it))
            reportViewModel.setImageList(imageList)
        }
    }

    private fun onImageCapture() {
        currentPhotoPath?.let { path ->
            val imageUri = Uri.fromFile(File(path))
            imageList.add(imageUri)
            imagePreviewAdapter.addData(listOf(imageUri))
            reportViewModel.setImageList(imageList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.REPORT_FRAGMENT
}