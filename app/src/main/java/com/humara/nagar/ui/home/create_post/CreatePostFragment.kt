package com.humara.nagar.ui.home.create_post

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
import androidx.core.net.toFile
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.humara.nagar.BuildConfig
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentCreatePostBinding
import com.humara.nagar.permissions.PermissionFragment
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.ui.common.MediaSelectionBottomSheet
import com.humara.nagar.ui.common.MediaSelectionListener
import com.humara.nagar.ui.home.HomeFragment
import com.humara.nagar.ui.report.ReportFragment
import com.humara.nagar.utils.*
import kotlinx.coroutines.launch

class CreatePostFragment : PermissionFragment(), MediaSelectionListener {
    private lateinit var binding: FragmentCreatePostBinding
    private val navController: NavController by lazy {
        findNavController()
    }
    private val createPostViewModel: CreatePostViewModel by viewModels {
        ViewModelFactory()
    }
    private lateinit var cameraImageUri: Uri

    private val getContentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (isFragmentAlive()) {
            result.data?.data?.let { documentUri ->
                onDocumentSelection(documentUri)
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            compressImageAndShowPreview(cameraImageUri)
        }
    }

    // Registers a photo picker activity launcher in single-select mode.
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the photo picker.
        uri?.let {
            compressImageAndShowPreview(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        createPostViewModel.run {
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            observeProgress(this)
            imageUriLiveData.observe(viewLifecycleOwner) { uri ->
                if (uri != null) {
                    showImagePreview(uri)
                }
            }
            documentUriLiveData.observe(viewLifecycleOwner) { uri ->
                if (uri != null) {
                    showPdfPreview(uri)
                }
            }
            postCreationSuccessLiveData.observe(viewLifecycleOwner) {
                setHomeScreenFeedReload()
                navController.navigateUp()
            }
            attachmentAvailableLivedata.observe(viewLifecycleOwner) { present ->
                binding.run {
                    if (present) {
                        groupAttachmentPreview.visibility = View.VISIBLE
                        groupAddAttachment.visibility = View.GONE
                    } else {
                        groupAttachmentPreview.visibility = View.GONE
                        groupAddAttachment.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setHomeScreenFeedReload() {
        navController.previousBackStackEntry?.savedStateHandle?.set(HomeFragment.RELOAD_FEED, true)
    }

    private fun initView() {
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.GONE
        binding.run {
            toolbar.apply {
                btnCross.setOnClickListener {
                    navController.navigateUp()
                }
            }
            clContainer.setOnClickListener { hideKeyboard() }
            getUserPreference().profileImage?.let { url ->
                Glide.with(this@CreatePostFragment)
                    .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                    .transform(CenterCrop(), RoundedCorners(12))
                    .placeholder(R.drawable.ic_user_image_placeholder)
                    .error(R.drawable.ic_user_image_placeholder)
                    .into(ivProfilePhoto)
            }
            tvName.text = getUserPreference().userProfile?.name
            etCaption.doAfterTextChanged {
                val caption = it.toString().trim()
                btnPost.isEnabled = caption.isNotEmpty()
                createPostViewModel.setCaption(caption)
            }
            btnDeleteAttachment.setOnClickListener {
                createPostViewModel.clearAttachmentData()
            }
            btnAddImage.setOnClickListener {
                MediaSelectionBottomSheet.show(parentFragmentManager, this@CreatePostFragment)
            }
            btnAddDocument.setOnClickListener {
                getContentLauncher.launch(IntentUtils.getOpenDocumentIntent())
            }
            btnAddVideo.setOnClickListener {
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
            }
            btnPost.setOnClickListener {
                createPostViewModel.createPost()
            }
        }
    }

    private fun compressImageAndShowPreview(uri: Uri) {
        lifecycleScope.launch {
            createPostViewModel.progressLiveData.postValue(true)
            val compressedUri = StorageUtils.compressImageFile(requireContext(), uri)
            createPostViewModel.setImageUri(compressedUri)
            createPostViewModel.progressLiveData.postValue(false)
        }
    }

    private fun showImagePreview(uri: Uri) {
        binding.run {
            ivImagePreview.visibility = View.VISIBLE
            ivImagePreview.setImageURI(uri)
        }
    }

    private fun onDocumentSelection(uri: Uri) {
        if (FileUtil.isValidDocumentSize(requireContext(), uri)) {
            lifecycleScope.launch {
                createPostViewModel.progressLiveData.postValue(true)
                val tempFile = FileUtil.createFileFromContentUri(requireContext(), uri)
                createPostViewModel.setDocumentUri(Uri.fromFile(tempFile))
                createPostViewModel.progressLiveData.postValue(false)
            }
        }
    }

    private fun showPdfPreview(uri: Uri) {
        binding.run {
            tvDocumentPreview.visibility = View.VISIBLE
            tvDocumentPreview.text = FileUtil.getFileName(uri.path)
            tvDocumentPreview.setOnClickListener {
                FileUtil.openPdfFile(requireContext(), uri.toFile())
            }
        }
    }

    override fun onCameraSelection() {
        requestPermissions(PermissionUtils.cameraPermissions, object : PermissionHandler {
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

    override fun onGallerySelection() {
        requestPermissions(PermissionUtils.storagePermissions, object : PermissionHandler {
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

    override fun getScreenName(): String = AnalyticsData.ScreenName.CREATE_POST
}