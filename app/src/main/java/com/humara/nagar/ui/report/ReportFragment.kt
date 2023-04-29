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
import com.bumptech.glide.util.Util
import com.google.android.gms.location.LocationServices
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.adapter.ImagePreviewAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentReportBinding
import com.humara.nagar.permissions.PermissionFragment
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.utils.IntentUtils
import com.humara.nagar.utils.PermissionUtils
import com.humara.nagar.utils.Utils
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import java.io.File
import java.io.IOException
import java.util.*

class ReportFragment : PermissionFragment() {
    companion object {
        const val TAG = "ReportFragment"
        private const val CURRENT_PATH = "CURRENT_PATH"
        private const val maxCommentLength: Int = 180
        private const val maxImageAttachments = 2
        private const val maxLocationLength = 70
    }

    private var _binding: FragmentReportBinding? = null
    private lateinit var currentPhotoPath: String
    private val reportViewModel by viewModels<ReportViewModel> {
        ViewModelFactory()
    }
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val getContentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (isFragmentAlive()) {
            onImageSelection(result?.data)
        }
    }

    private val takeCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK || context == null) {
            Toast.makeText(context, "No image clicked", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        if (isFragmentAlive()) {
            onImageCapture()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        savedInstanceState?.run {
            currentPhotoPath = getString(CURRENT_PATH, "")
        }
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
            getSubmitButtonState().observe(viewLifecycleOwner) { isEnabled ->
                binding.btnSubmit.isEnabled = isEnabled
            }
            getImages().observe(viewLifecycleOwner) {
                imagePreviewAdapter.setData(it)
                binding.uploadImageRequired.isVisible = it.isEmpty()
            }
        }
    }

    private fun initView() {
        binding.run {
            clForm.setOnClickListener { hideKeyboard() }
            addImageLayout.setOnClickListener {
                showPictureDialog()
            }
            //Setting up the top app bar title
            includedToolbar.apply {
                toolbarTitle.text = resources.getString(R.string.reportIssueTitle)
                rightIconTv.text = resources.getString(R.string.history)
                rightIcon.visibility = View.VISIBLE
                rightIconIv.setImageResource(R.drawable.ic_history)
            }
            //Settings up list for spinners
            inputCategory.apply {
                setOptions(R.array.category_list)
                setUserInputListener {
                    reportViewModel.setCategory(it)
                }
            }
            inputLocality.apply {
                setOptions(R.array.locality_list)
                setUserInputListener {
                    reportViewModel.setLocality(it)
                }
            }
            inputComment.apply {
                switchToMultiLined(4, svForm)
                setMaxLength(maxCommentLength)
                setUserInputListener {
                    reportViewModel.setComment(it)
                }
            }
            inputLocation.apply {
                switchToMultiLined(2, svForm)
                setMaxLength(maxLocationLength)
                setLayoutListener(true) {
                    if (isInputEmpty()) {
                        checkForLocationPermission()
                    }
                }
                setUserInputListener {
                    reportViewModel.setLocation(it)
                }
            }
            imagePreviewAdapter = ImagePreviewAdapter { idx ->
                reportViewModel.deleteImage(idx)
            }
            imagePreviewItemRCV.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(false)
                adapter = imagePreviewAdapter
            }
        }
    }

    private fun showPictureDialog() {
        if (reportViewModel.imageUris.size >= maxImageAttachments) {
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
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${reportViewModel.imageUris.size + 1}", ".jpg", storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    @Suppress("DEPRECATION")
    private fun getAddress() {
        showProgress(true)
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
                        val addressLine = Utils.findLargestPrefixSubstring(address.getAddressLine(0), maxLocationLength, ",")
                        Logger.debugLog(TAG, "Address: $addressLine")
                        binding.inputLocation.setInput(addressLine)
                        reportViewModel.setLocation(addressLine)
                    }
                }
            } ?: kotlin.run {
                Toast.makeText(requireContext(), getString(R.string.location_detection_error_message), Toast.LENGTH_LONG).show()
                hideProgress()
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
            if (count > maxImageAttachments - reportViewModel.imageUris.size) {
                Toast.makeText(requireContext(), getString(R.string.imagePickingLimit), Toast.LENGTH_SHORT).show()
                return
            }
            for (i in 0 until count) {
                val imageUri = selectedImages.getItemAt(i).uri
                imageUri?.let { uri ->
                    reportViewModel.addImages(listOf(uri))
                }
            }
        } ?: data?.data?.let {
            reportViewModel.addImages(listOf(it))
        } ?: kotlin.run {
            Toast.makeText(requireContext(), getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onImageCapture() {
        val imageUri = Uri.fromFile(File(currentPhotoPath))
        Logger.debugLog(TAG, imageUri.toString())
        reportViewModel.addImages(listOf(imageUri))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::currentPhotoPath.isInitialized)
            outState.putString(CURRENT_PATH, currentPhotoPath)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.REPORT_FRAGMENT
}