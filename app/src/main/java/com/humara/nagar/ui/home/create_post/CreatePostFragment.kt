package com.humara.nagar.ui.home.create_post

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.humara.nagar.R
import com.humara.nagar.adapter.PollOptionsPreviewAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentCreatePostBinding
import com.humara.nagar.ui.common.MediaSelectionBottomSheet
import com.humara.nagar.ui.common.MediaSelectionListener
import com.humara.nagar.ui.home.HomeFragment
import com.humara.nagar.ui.home.HomeViewModel
import com.humara.nagar.ui.home.create_post.model.PollRequest
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostType
import com.humara.nagar.utils.*
import kotlinx.coroutines.launch

class CreatePostFragment : BaseFragment(), MediaSelectionListener {
    companion object {
        const val POLL_RESULT_REQUEST = "poll_request"
        const val POLL_DATA = "poll_data"
    }

    private lateinit var binding: FragmentCreatePostBinding
    private val navController: NavController by lazy {
        findNavController()
    }
    private val createPostViewModel: CreatePostViewModel by viewModels {
        ViewModelFactory()
    }
    private val homeViewModel: HomeViewModel by viewModels {
        ViewModelFactory()
    }
    private val args: CreatePostFragmentArgs by navArgs()

    private val getContentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (isFragmentAlive()) {
            result.data?.data?.let { documentUri ->
                onDocumentSelection(documentUri)
            } ?: kotlin.run {
                context?.showToast(getString(R.string.no_document_selected))
                return@registerForActivityResult
            }
        }
    }

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            onVideoSelection(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(POLL_RESULT_REQUEST) { _, bundle ->
            bundle.parcelable<PollRequest>(POLL_DATA)?.let {
                createPostViewModel.setPollData(it)
            }
        }
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        createPostViewModel.run {
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            observeProgress(this)
            imageUriLiveData.observe(viewLifecycleOwner) { uri ->
                showImagePreview(uri)
            }
            thumbnailUriLiveData.observe(viewLifecycleOwner) {
                if (documentUriLiveData.value != null) {
                    showPdfPreview(it)
                } else if (videoUriLiveData.value != null) {
                    showVideoPreview(it)
                } else {
                    hideVideoAndDocumentPreview()
                }
            }
            pollRequestLiveData.observe(viewLifecycleOwner) { poll ->
                showPollPreview(poll)
            }
            postCreationSuccessLiveData.observe(viewLifecycleOwner) {
                setHomeScreenFeedReload()
                navController.navigateUp()
            }
            attachmentAvailableLivedata.observe(viewLifecycleOwner) { present ->
                handleAttachmentPreview(present)
            }
            attachmentParsingErrorLiveData.observe(viewLifecycleOwner) {
                context?.showToast(getString(R.string.attachment_error_message), false)
            }
            postButtonStateLiveData.observe(viewLifecycleOwner) {
                binding.btnPost.isEnabled = it
            }
        }
        if (args.isEdit) {
            homeViewModel.run {
                observeErrorAndException(this, errorAction = {}, dismissAction = {})
                observeProgress(this)
                postDetailsLiveData.observe(viewLifecycleOwner) {
                    initEditPostView(it)
                }
                editPostSuccessLiveData.observe(viewLifecycleOwner) {
                    setPreviousScreenPostUpdate()
                    navController.navigateUp()
                }
            }
        }
    }

    private fun setHomeScreenFeedReload() {
        navController.previousBackStackEntry?.savedStateHandle?.set(HomeFragment.RELOAD_FEED, true)
    }

    private fun setPreviousScreenPostUpdate() {
        navController.previousBackStackEntry?.savedStateHandle?.set(HomeFragment.UPDATE_POST, args.postId)
    }

    private fun initView() {
        binding.run {
            toolbar.apply {
                btnCross.setOnClickListener {
                    navController.navigateUp()
                }
            }
            clContainer.setOnClickListener { hideKeyboard() }
            getUserPreference().profileImage?.let { url ->
                ivProfilePhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
            }
            tvName.text = getUserPreference().userProfile?.name
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
            btnAddPoll.setOnClickListener {
                navController.navigate(CreatePostFragmentDirections.actionCreatePostFragmentToCreatePollFragment())
            }
            btnPost.setOnClickListener {
                if (args.isEdit) {
                    homeViewModel.editPost(args.postId, etCaption.text.toString())
                } else {
                    createPostViewModel.createPost()
                }
            }
            if (args.isEdit) {
                homeViewModel.getPostDetails(args.postId)
            } else {
                binding.etCaption.doAfterTextChanged {
                    val caption = it.toString().trim()
                    createPostViewModel.setCaption(caption)
                }
            }
        }
    }

    private fun showImagePreview(uri: Uri?) {
        binding.run {
            uri?.let {
                ivImagePreview.visibility = View.VISIBLE
                ivImagePreview.setImageURI(uri)
            } ?: run {
                ivImagePreview.visibility = View.GONE
            }
        }
    }

    private fun onDocumentSelection(uri: Uri) {
        if (FileUtils.isValidDocumentSize(requireContext(), uri)) {
            lifecycleScope.launch {
                createPostViewModel.progressLiveData.postValue(true)
                val tempUri: Uri? = FileUtils.createTempUriFromContentUri(requireContext(), uri)
                tempUri?.let {
                    createPostViewModel.setDocumentUri(it)
                    createPostViewModel.setThumbnailUri(uri)
                } ?: kotlin.run {
                    createPostViewModel.setAttachmentError()
                }
                createPostViewModel.progressLiveData.postValue(false)
            }
        }
    }

    private fun onVideoSelection(uri: Uri) {
        if (VideoUtils.isValidVideoSize(requireContext(), uri)) {
            lifecycleScope.launch {
                createPostViewModel.progressLiveData.postValue(true)
                val tempUri: Uri? = VideoUtils.createTempUriFromContentUri(requireContext(), uri)
                tempUri?.let {
                    createPostViewModel.setVideoUri(it)
                    createPostViewModel.setThumbnailUri(uri)
                } ?: kotlin.run {
                    createPostViewModel.setAttachmentError()
                }
                createPostViewModel.progressLiveData.postValue(false)
            }
        }
    }

    private fun showPdfPreview(uri: Uri?) {
        binding.run {
            uri?.let {
                tvDocumentPreview.visibility = View.VISIBLE
                tvDocumentPreview.text = FileUtils.getFileName(requireContext(), it)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        ivImagePreview.visibility = View.VISIBLE
                        val thumbnail: Bitmap = requireContext().contentResolver.loadThumbnail(it, Size(640, 480), null)
                        ivImagePreview.setImageBitmap(thumbnail)
                    } catch (e: Exception) {
                        // ignore. Do not load pdf preview in this case
                    }
                }
                clAttachmentPreview.setOnClickListener {
                    FileUtils.openPdfFile(requireContext(), uri.toFile())
                }
            } ?: run {
                tvDocumentPreview.visibility = View.GONE
            }
        }
    }

    private fun showVideoPreview(uri: Uri?) {
        binding.videoPreview.run {
            uri?.let {
                root.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val thumbnail: Bitmap = requireContext().contentResolver.loadThumbnail(it, Size(1000, 1080), null)
                    ivThumbnail.setImageBitmap(thumbnail)
                } else {
                    Glide.with(requireContext())
                        .load(uri)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(ivThumbnail)
                }
                ivPlay.setNonDuplicateClickListener {
                    val action = CreatePostFragmentDirections.actionCreatePostFragmentToVideoPlayerFragment(uri, getScreenName())
                    navController.navigate(action)
                }
            }
        }
    }

    private fun hideVideoAndDocumentPreview() {
        binding.videoPreview.root.visibility = View.GONE
        binding.tvDocumentPreview.visibility = View.GONE
    }

    private fun showPollPreview(poll: PollRequest?) {
        poll?.let {
            binding.layoutPollPreview.run {
                root.visibility = View.VISIBLE
                tvQuestion.text = poll.question
                tvSubTitle.text = resources.getQuantityString(R.plurals.n_votes, 0, 0)
                rvOptions.apply {
                    val pollOptionsAdapter = PollOptionsPreviewAdapter(poll.options)
                    adapter = pollOptionsAdapter
                }
                tvExpiryTime.text = DateTimeUtils.getRemainingDurationForPoll(requireContext(), it.expiryTime)
            }
        } ?: run {
            binding.layoutPollPreview.root.visibility = View.GONE
        }
    }

    private fun handleAttachmentPreview(show: Boolean) {
        binding.run {
            if (show) {
                groupAttachmentPreview.visibility = View.VISIBLE
                groupAddAttachment.visibility = View.GONE
            } else {
                groupAttachmentPreview.visibility = View.GONE
                groupAddAttachment.visibility = View.VISIBLE
            }
        }
    }

    private fun initEditPostView(post: Post) {
        binding.run {
            etCaption.setText(post.caption)
            tvTitle.text = getString(R.string.edit_post)
            btnPost.text = getString(R.string.save)
            btnPost.isEnabled = true
            clAttachmentPreview.isClickable = false
            clAttachmentPreview.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.translucent_background))
            handleAttachmentPreview(show = true)
            btnDeleteAttachment.visibility = View.GONE
            when (post.type) {
                PostType.IMAGE.type -> handleEditImagePost(post)
                PostType.DOCUMENT.type -> handleEditDocumentPost(post)
                PostType.POLL.type -> handleEditPollPost(post)
                PostType.VIDEO.type -> handleEditVideoPost(post)
                else -> handleEditTextPost()
            }
        }
    }

    private fun handleEditTextPost() {
        binding.run {
            clAttachmentPreview.visibility = View.GONE
            etCaption.doAfterTextChanged {
                btnPost.isEnabled = it.toString().trim().isNotEmpty()
            }
        }
    }

    private fun handleEditImagePost(post: Post) {
        binding.run {
            post.info?.medias?.getOrNull(0)?.let { url ->
                ivImagePreview.visibility = View.VISIBLE
                ivImagePreview.loadUrl(url, R.drawable.ic_image_placeholder)
            } ?: run {
                ivImagePreview.visibility = View.GONE
            }
        }
    }

    private fun handleEditDocumentPost(post: Post) {
        binding.run {
            post.info?.medias?.getOrNull(0)?.let { url ->
                tvDocumentPreview.visibility = View.VISIBLE
                tvDocumentPreview.text = FileUtils.getFileName(url)
                tvDocumentPreview.foreground = null
            }
        }
    }

    private fun handleEditPollPost(post: Post) {
        post.info?.let { poll ->
            binding.layoutPollPreview.run {
                root.visibility = View.VISIBLE
                tvQuestion.text = poll.question
                tvSubTitle.text = resources.getQuantityString(R.plurals.n_votes, 0, 0)
                rvOptions.apply {
                    val pollOptionsAdapter = PollOptionsPreviewAdapter(poll.getOptionsText())
                    adapter = pollOptionsAdapter
                }
                tvExpiryTime.text = DateTimeUtils.getRemainingDurationForPoll(requireContext(), poll.expiryTime)
            }
        }
    }

    private fun handleEditVideoPost(post: Post) {
        binding.videoPreview.run {
            post.info?.medias?.getOrNull(0)?.let {
                root.visibility = View.VISIBLE
                ivThumbnail.setImageResource(R.drawable.ic_image_placeholder)
            }
        }
    }

    override fun onMediaSelection(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            createPostViewModel.setImageUri(uris[0])
        }
    }

    override fun getScreenName(): String = AnalyticsData.ScreenName.CREATE_POST_FRAGMENT
}