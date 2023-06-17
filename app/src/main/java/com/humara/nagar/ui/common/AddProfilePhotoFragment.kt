package com.humara.nagar.ui.common

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.IntentKeyConstants
import com.humara.nagar.databinding.FragmentAddProfilePhotoBinding
import com.humara.nagar.permissions.PermissionFragment
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.ui.report.ReportFragment
import com.humara.nagar.ui.signup.OnBoardingViewModel
import com.humara.nagar.utils.PermissionUtils
import com.humara.nagar.utils.StorageUtils
import com.humara.nagar.utils.loadUrl
import kotlinx.coroutines.launch

class AddProfilePhotoFragment : BaseFragment(), MediaSelectionListener {
    companion object {
        const val TAG = "AddProfilePhotoFragment"
        private const val IS_EDIT = "is_edit"
        fun getInstance(source: String, isEdit: Boolean = true) = AddProfilePhotoFragment().apply {
            arguments = Bundle().apply {
                putString(IntentKeyConstants.SOURCE, source)
                putBoolean(IS_EDIT, isEdit)
            }
        }
    }

    private lateinit var binding: FragmentAddProfilePhotoBinding
    private val onBoardingViewModel: OnBoardingViewModel by viewModels {
        ViewModelFactory()
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
//            compressImageAndShowPreview(cameraImageUri)
        }
    }

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            compressImageAndShowPreview(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddProfilePhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initViewModelObservers() {
        onBoardingViewModel.run {
            observeProgress(this)
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
        }
    }

    private fun initView() {
        binding.run {
            btnBack.setOnClickListener { requireActivity().onBackPressed() }
            getUserPreference().profileImage?.let { url ->
                ivProfilePhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
            }
        }
    }

    private fun compressImageAndShowPreview(uri: Uri?) {
        uri?.let {
            lifecycleScope.launch {
                onBoardingViewModel.progressLiveData.postValue(true)
                val compressedUri = StorageUtils.compressImageFile(requireContext(), uri)
//                createPostViewModel.setImageUri(compressedUri)
                onBoardingViewModel.progressLiveData.postValue(false)
            }
        }
    }

    override fun onCameraSelection() {
        TODO("Not yet implemented")
    }

    override fun onGallerySelection() {
        (requireActivity() as PermissionFragment).requestPermissions(PermissionUtils.storagePermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                if (context == null) {
                    Logger.debugLog(ReportFragment.TAG, "fragment detached from the activity")
                    return
                }
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    override fun getScreenName() = AnalyticsData.ScreenName.ADD_PROFILE_PHOTO_FRAGMENT
}