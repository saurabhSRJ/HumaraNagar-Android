package com.humara.nagar.ui.common

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.esafirm.imagepicker.features.*
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.BaseBottomSheetDialogFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.BottomSheetImagePickerBinding
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.utils.ImageUtils
import com.humara.nagar.utils.PermissionUtils
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.showToast
import kotlinx.coroutines.launch

class MediaSelectionBottomSheet : BaseBottomSheetDialogFragment() {
    companion object {
        const val TAG = "MediaSelectionBottomSheet"
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
    private val mediaSelectionViewModel: MediaSelectionViewModel by viewModels {
        ViewModelFactory()
    }
    private var maxSelectionItems: Int = 1

    private val captureImageLauncher = registerImagePicker { images ->
        val uris = images.map { it.uri }
        if (uris.isEmpty()) {
            context?.showToast(getString(R.string.no_image_clicked))
            dismiss()
        } else {
            compressImage(uris)
        }
    }

    private val pickImageLauncher = registerImagePicker { images ->
        val uris = images.map { it.uri }
        if (uris.isEmpty()) {
            context?.showToast(getString(R.string.no_image_selected))
            dismiss()
        } else {
            compressImage(uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            maxSelectionItems = getInt(MAX_SELECTION, 1)
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
                useImagePickerLauncher()
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
                    Logger.debugLog(TAG, "Fragment detached from the activity")
                    return
                }
                useCameraLauncher()
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun compressImage(uris: List<Uri>) {
        val compressedUris = mutableListOf<Uri>()
        lifecycleScope.launch {
            for (uri in uris) {
                compressedUris.add(ImageUtils.compressImageFile(requireContext(), uri))
            }
            mediaSelectionViewModel.addImages(compressedUris)
        }
    }

    private fun useImagePickerLauncher() {
        val config = ImagePickerConfig {
            mode = ImagePickerMode.MULTIPLE // default is multi image mode
            language = getAppPreference().appLanguage // Set image picker language
            // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
            returnMode = ReturnMode.NONE
            isFolderMode = false // set folder mode (false by default)
            isIncludeVideo = false // include video (false by default)
            isOnlyVideo = false // include video (false by default)
            arrowColor = Color.WHITE // set toolbar arrow up color
            imageTitle = getString(R.string.tap_to_select) // image selection title
            doneButtonText = getString(R.string.done) // done button text
            limit = maxSelectionItems // max images can be selected (99 by default)
            isShowCamera = false // show camera or not (true by default)
            theme = R.style.ImagePickerTheme
        }
        pickImageLauncher.launch(config)
    }

    private fun useCameraLauncher() {
        captureImageLauncher.launch(CameraOnlyConfig(savePath = ImagePickerSavePath(getString(R.string.app_name))))
    }

    override fun shouldLogScreenView() = false
    override fun getScreenName() = AnalyticsData.ScreenName.MEDIA_SELECTION_BOTTOM_SHEET
}

interface MediaSelectionListener {
    fun onMediaSelection(uris: List<Uri>)
}