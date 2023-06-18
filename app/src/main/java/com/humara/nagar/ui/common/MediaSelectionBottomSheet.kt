package com.humara.nagar.ui.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.humara.nagar.BuildConfig
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.BaseBottomSheetDialogFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.BottomSheetImagePickerBinding
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.ui.report.ReportFragment
import com.humara.nagar.utils.*
import kotlinx.coroutines.launch

class MediaSelectionBottomSheet : BaseBottomSheetDialogFragment() {
    companion object {
        const val TAG = "MediaSelectionBottomSheet"
        private const val CAMERA_IMAGE_URI = "camera_image_uri"
        private const val MAX_SELECTION = "max_selection"
        private var listener: MediaSelectionListener? = null
        fun show(fragmentManager: FragmentManager, listener: MediaSelectionListener, maxItems: Int = 1) {
            this.listener = listener
            MediaSelectionBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(MAX_SELECTION, maxItems)
                }
            }.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: BottomSheetImagePickerBinding
    private var cameraImageUri: Uri? = null
    private val mediaSelectionViewModel: MediaSelectionViewModel by viewModels {
        ViewModelFactory()
    }
    private var maxSelectionItems: Int = 1

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            cameraImageUri?.let {
                compressImage(listOf(it))
            }
        } else {
            context?.showToast(getString(R.string.no_image_clicked), true)
            dismiss()
        }
    }

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the photo picker.
        uri?.let {
            compressImage(listOf(it))
        } ?: kotlin.run {
            context?.showToast(getString(R.string.no_image_selected), true)
            dismiss()
            return@registerForActivityResult
        }
    }

    private val getContentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onImageSelection(result?.data)
    }

    private fun onImageSelection(data: Intent?) {
        data?.clipData?.let { selectedImages ->
            val count = selectedImages.itemCount
            if (count > maxSelectionItems) {
                context?.showToast(getString(R.string.imagePickingLimit), true)
                return
            }
            val uris = mutableListOf<Uri>()
            for (i in 0 until count) {
                uris.add(selectedImages.getItemAt(i).uri)
            }
            compressImage(uris)
        } ?: data?.data?.let {
            compressImage(listOf(it))
        } ?: kotlin.run {
            context?.showToast(getString(R.string.no_image_selected), true)
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetImagePickerBinding.inflate(inflater, container, false)
        isCancelable = true
        savedInstanceState?.run {
            cameraImageUri = this.parcelable(CAMERA_IMAGE_URI)
        }
        arguments?.run {
            maxSelectionItems = getInt(MAX_SELECTION, 1)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaSelectionViewModel.run {
            progressLiveData.observe(viewLifecycleOwner) { progress ->
                if (progress) {
                    showProgress(true)
                } else {
                    hideProgress()
                }
            }
            imagesLiveData.observe(viewLifecycleOwner) {
                listener?.onMediaSelection(it)
                dismiss()
            }
        }
        initView()
    }

    private fun initView() {
        binding.run {
            llGallery.setNonDuplicateClickListener {
                onGallerySelection()
            }
            llCamera.setNonDuplicateClickListener {
                onCameraSelection()
            }
        }
    }

    private fun onGallerySelection() {
        (activity as BaseActivity).requestPermissions(PermissionUtils.storagePermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                if (context == null) {
                    Logger.debugLog(ReportFragment.TAG, "fragment detached from the activity")
                    return
                }
                if (maxSelectionItems == 1) {
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    getContentLauncher.launch(IntentUtils.getImageGalleryIntent())
                }
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun onCameraSelection() {
        (activity as BaseActivity).requestPermissions(PermissionUtils.cameraPermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                if (context == null) {
                    Logger.debugLog(ReportFragment.TAG, "Fragment detached from the activity")
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
        cameraImageUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID.plus(".provider"), imageFile)
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun compressImage(uris: List<Uri>) {
        val compressedUris = mutableListOf<Uri>()
        lifecycleScope.launch {
            for (uri in uris) {
                compressedUris.add(StorageUtils.compressImageFile(requireContext(), uri))
            }
            mediaSelectionViewModel.addImages(compressedUris)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (cameraImageUri != null) outState.putParcelable(CAMERA_IMAGE_URI, cameraImageUri)
    }

    override fun shouldLogScreenView() = false
    override fun getScreenName() = AnalyticsData.ScreenName.MEDIA_SELECTION_BOTTOM_SHEET
}

interface MediaSelectionListener {
    fun onMediaSelection(uris: List<Uri>)
}