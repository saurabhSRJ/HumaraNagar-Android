package com.humara.nagar.ui.common

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.humara.nagar.utils.*
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import kotlinx.coroutines.launch

class MediaSelectionBottomSheet : BaseBottomSheetDialogFragment() {
    companion object {
        const val TAG = "MediaSelectionBottomSheet"
        private const val CAMERA_IMAGE_URI = "camera_image_uri"
        private const val MAX_SELECTION = "max_selection"
        private const val REQUEST_CODE_CHOOSE = 19291
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

    /* Registers a photo picker activity launcher in single-select mode. Photo picker is available on Android 11 and later.
       If the photo picker isn't available on a device, the library automatically invokes the ACTION_OPEN_DOCUMENT intent action instead.
    */
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

    private var pickMultipleMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            maxSelectionItems = getInt(MAX_SELECTION, 1)
        }
        savedInstanceState?.run {
            cameraImageUri = this.parcelable(CAMERA_IMAGE_URI)
        }
        if (maxSelectionItems > 1) {
            pickMultipleMediaLauncher = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxSelectionItems)) { uris ->
                if (uris.isNotEmpty()) {
                    compressImage(uris)
                } else {
                    context?.showToast(getString(R.string.no_image_selected), true)
                    dismiss()
                    return@registerForActivityResult
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetImagePickerBinding.inflate(inflater, container, false)
        isCancelable = true
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
                    Logger.debugLog(TAG, "fragment detached from the activity")
                    return
                }
                if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()) {
                    if (maxSelectionItems == 1) {
                        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } else {
                        pickMultipleMediaLauncher?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                } else {
                    useCustomPhotoPicker()
                }
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun useCustomPhotoPicker() {
        Matisse.from(this@MediaSelectionBottomSheet)
            .choose(MimeType.ofImage())
            .countable(true)
            .maxSelectable(maxSelectionItems)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .thumbnailScale(0.85f)
            .imageEngine(GlideEngine())
            .showSingleMediaType(true)
            .showPreview(false) // Default is `true`
            .theme(R.style.photoPickerStyle)
            .forResult(REQUEST_CODE_CHOOSE)
    }

    private fun onCameraSelection() {
        (activity as BaseActivity).requestPermissions(PermissionUtils.cameraPermissions, object : PermissionHandler {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE) {
            if (resultCode == RESULT_OK) {
                val uris = Matisse.obtainResult(data)
                compressImage(uris)
            } else {
                context?.showToast(getString(R.string.no_image_selected), true)
                dismiss()
            }
        }
    }

    private fun clickPicture() {
        val imageFile = StorageUtils.createImageFile(requireContext())
        cameraImageUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID, imageFile)
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