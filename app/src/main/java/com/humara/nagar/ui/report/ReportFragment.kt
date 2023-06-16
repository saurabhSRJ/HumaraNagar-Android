package com.humara.nagar.ui.report

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.humara.nagar.BuildConfig
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.adapter.ImagePreviewAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentReportBinding
import com.humara.nagar.permissions.PermissionFragment
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.ui.AppConfigViewModel
import com.humara.nagar.ui.common.GenericStatusDialog
import com.humara.nagar.ui.common.MediaSelectionBottomSheet
import com.humara.nagar.ui.common.MediaSelectionListener
import com.humara.nagar.ui.common.StatusData
import com.humara.nagar.ui.report.complaints.ComplaintsFragment
import com.humara.nagar.utils.*
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

class ReportFragment : PermissionFragment(), MediaSelectionListener {
    private var _binding: FragmentReportBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val reportViewModel by viewModels<ReportViewModel> {
        ViewModelFactory()
    }
    private val appConfigViewModel by viewModels<AppConfigViewModel> {
        ViewModelFactory()
    }
    private lateinit var currentPhotoPath: String
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter
    private val navController: NavController by lazy { findNavController() }

    companion object {
        const val TAG = "ReportFragment"
        private const val CURRENT_PATH = "CURRENT_PATH"
        private const val maxImageAttachments = 2
        private const val maxLocationLength = 70
    }

    private val getContentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (isFragmentAlive()) {
            onImageSelection(result?.data)
        }
    }

    private val takeCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK || context == null) {
            context?.showToast(getString(R.string.no_image_clicked), true)
            return@registerForActivityResult
        }
        if (isFragmentAlive()) {
            onImageCapture()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentBackStackEntry = navController.getBackStackEntry(R.id.reportFragment)
        //Handle back button navigation for admin users
        currentBackStackEntry.savedStateHandle.getLiveData<Boolean>(ComplaintsFragment.IS_ADMIN).observe(currentBackStackEntry) { admin ->
            if (admin) {
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.home_navigation, inclusive = false).build()
                navController.navigate(R.id.home_navigation, null, navOptions = navOptions)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        savedInstanceState?.run {
            currentPhotoPath = getString(CURRENT_PATH, "")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //if user is admin navigate to all complaints screen
        if (getUserPreference().isAdminUser) {
            openComplaintsScreen()
        }
        initViewModelObservers()
        initView()
        initHistoryTooltip()
    }

    private fun initViewModelObservers() {
        appConfigViewModel.run {
            userLocalitiesLiveData.observe(viewLifecycleOwner) {
                binding.inputLocality.setOptions(it.toTypedArray())
            }
            complaintCategoriesLiveData.observe(viewLifecycleOwner) {
                binding.inputCategory.setOptions(it.toTypedArray())
            }
            getComplaintCategories()
            getUserLocalities()
        }
        reportViewModel.run {
            observeProgress(this, false)
            observeException(this)
            submitButtonStateData.observe(viewLifecycleOwner) { isEnabled ->
                binding.btnSubmit.isEnabled = isEnabled
            }
            imagesData.observe(viewLifecycleOwner) {
                imagePreviewAdapter.setData(it)
                binding.uploadImageRequired.isVisible = it.isEmpty()
            }
            postComplaintStatusLiveData.observe(viewLifecycleOwner) { success ->
                if (success) {
                    showComplaintSuccessDialog()
                } else {
                    showErrorDialog(errorAction = {}, dismissAction = {})
                }
            }
        }
    }

    private fun openComplaintsScreen() {
        val action = ReportFragmentDirections.actionReportToComplaints(getScreenName())
        navController.navigate(action)
    }

    private fun showComplaintSuccessDialog() {
        GenericStatusDialog.show(parentFragmentManager,
            StatusData(GenericStatusDialog.State.SUCCESS, getString(R.string.complaint_raised), getString(R.string.complaint_raised_subtitle), getString(R.string.track)),
            object : GenericStatusDialog.StatusDialogClickListener {
                override fun ctaClickListener() {
                    resetComplaintForm()
                    openComplaintsScreen()
                }

                override fun dismissClickListener() {
                    super.dismissClickListener()
                    resetComplaintForm()
                }
            })
    }

    private fun resetComplaintForm() {
        binding.run {
            inputCategory.setInput("")
            inputLocality.setInput("")
            inputComment.setInput("")
            inputLocation.setInput("")
            reportViewModel.deleteAllImages()
            currentPhotoPath = ""
        }
    }

    private fun initView() {
        binding.run {
            clForm.setOnClickListener { hideKeyboard() }
            //Setting up the top app bar title
            includedToolbar.apply {
                leftIcon.setOnClickListener {
                    navController.navigateUp()
                }
                toolbarTitle.text = resources.getString(R.string.reportIssueTitle)
                rightIconTv.text = resources.getString(R.string.history)
                rightIcon.visibility = View.VISIBLE
                rightIconTv.visibility = View.VISIBLE
                rightIconIv.setImageResource(R.drawable.ic_history)
                rightIcon.setNonDuplicateClickListener {
                    getUserPreference().historyToolTipCounter += 1
                    openComplaintsScreen()
                }
            }
            inputCategory.setUserInputListener {
                reportViewModel.setCategory(it)
            }
            inputLocality.setUserInputListener {
                reportViewModel.setLocality(it)
            }
            inputLocation.apply {
                switchToMultiLined(2)
                setLayoutListener(true) {
                    if (isInputEmpty()) {
                        checkForLocationPermission()
                    }
                }
                setUserInputListener {
                    reportViewModel.setLocation(it)
                }
            }
            inputComment.apply {
                switchToMultiLined(5)
                setUserInputListener {
                    reportViewModel.setComment(it)
                }
                setHint(getString(R.string.comments_short_hint))
            }
            addImageLayout.setOnClickListener {
                showMediaSelectionBottomSheet()
            }
            imagePreviewAdapter = ImagePreviewAdapter { idx ->
                reportViewModel.deleteImage(idx)
            }
            imagePreviewItemRCV.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(false)
                adapter = imagePreviewAdapter
            }
            btnSubmit.setNonDuplicateClickListener {
                reportViewModel.postComplaint()
            }
        }
    }

    private fun initHistoryTooltip() {
        val historyToolTipCounter = getUserPreference().historyToolTipCounter
        if (getUserPreference().isAdminUser.not() && historyToolTipCounter < 3) {
            val balloon = Balloon.Builder(requireContext())
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setPaddingHorizontal(8)
                .setPaddingVertical(8)
                .setMarginHorizontal(8)
                .setTextSize(12f)
                .setTextTypeface(Typeface.DEFAULT_BOLD)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_F1F1F1))
                .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
                .setText(resources.getString(R.string.trackYourPastComplaints))
                .setArrowSize(10)
                .setIsVisibleArrow(true)
                .setArrowPosition(0.50f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setCornerRadius(4f)
                .setBalloonAnimation(BalloonAnimation.ELASTIC)
                .setAutoDismissDuration(4000L)
                .setLifecycleOwner(this)
                .build()
            balloon.showAlignBottom(binding.includedToolbar.rightIcon)
        }
    }

    private fun showMediaSelectionBottomSheet() {
        if (reportViewModel.imageUris.size >= maxImageAttachments) {
            context?.showToast(getString(R.string.imagePickingLimit), true)
            return
        }
        MediaSelectionBottomSheet.show(parentFragmentManager, this)
    }

    private fun checkForLocationPermission() {
        requestPermissions(PermissionUtils.locationPermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                getAddress()
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //TODO: Add a common helper dialog with message about permanently denied permission
                context?.showToast(getString(R.string.you_can_still_enter_location_manually))
            }
        }, isPermissionNecessary = false)
    }

    @Suppress("DEPRECATION")
    private fun getAddress() {
        showProgress(true)
        val client = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Get the last known location. In some rare situations, this can be null.
        client.lastLocation.addOnSuccessListener { lastLocation ->
            lastLocation?.let { location ->
                // Logic to handle location object.
                var addresses: List<Address>? = null
                try {
                    val geocoder = Geocoder(requireContext(), Locale(getAppPreference().appLanguage, "IN"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) {
                            addresses = it
                        }
                    } else {
                        addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    }
                    reportViewModel.setLocationCoordinates(location.latitude, location.longitude)
                } catch (ioException: IOException) {
                    Logger.logException(TAG, ioException, Logger.LogLevel.ERROR, true)
                } catch (illegalArgumentException: IllegalArgumentException) {
                    Logger.logException(TAG, illegalArgumentException, Logger.LogLevel.ERROR, true)
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
                context?.showToast(getString(R.string.location_detection_error_message))
                hideProgress()
            }
        }
    }

    private fun onImageSelection(data: Intent?) {
        data?.clipData?.let { selectedImages ->
            val count = selectedImages.itemCount
            if (count > maxImageAttachments - reportViewModel.imageUris.size) {
                context?.showToast(getString(R.string.imagePickingLimit), true)
                return
            }
            for (i in 0 until count) {
                val imageUri = selectedImages.getItemAt(i).uri
                imageUri?.let { uri ->
                    compressImage(uri)
                }
            }
        } ?: data?.data?.let {
            compressImage(it)
        } ?: kotlin.run {
            context?.showToast(getString(R.string.no_image_selected), true)
        }
    }

    private fun onImageCapture() {
        if (this::currentPhotoPath.isInitialized && currentPhotoPath.isNotEmpty()) {
            val imageUri = Uri.fromFile(File(currentPhotoPath))
            compressImage(imageUri)
        }
    }

    private fun compressImage(uri: Uri) {
        lifecycleScope.launch {
            reportViewModel.progressLiveData.postValue(true)
            val compressedUri = StorageUtils.compressImageFile(requireContext(), uri)
            reportViewModel.addImages(listOf(compressedUri))
            reportViewModel.progressLiveData.postValue(false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::currentPhotoPath.isInitialized) outState.putString(CURRENT_PATH, currentPhotoPath)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.REPORT_FRAGMENT
    override fun onCameraSelection() {
        requestPermissions(PermissionUtils.cameraPermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                if (context == null) {
                    Logger.debugLog(TAG, "Fragment detached from the activity")
                    return
                }
                clickPicture()
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun clickPicture() {
        val imageFile = StorageUtils.createImageFile(requireContext())
        currentPhotoPath = imageFile.absolutePath
        val imageUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID.plus(".provider"), imageFile)
        val intent: Intent = IntentUtils.getCameraIntent(requireContext(), imageUri)
        if (IntentUtils.hasIntent(requireContext(), intent)) {
            takeCameraLauncher.launch(intent)
        }
    }

    override fun onGallerySelection() {
        requestPermissions(PermissionUtils.storagePermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                if (context == null) {
                    Logger.debugLog(TAG, "fragment detached from the activity")
                    return
                }
                val intent = IntentUtils.getImageGalleryIntent()
                getContentLauncher.launch(intent)
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }
}