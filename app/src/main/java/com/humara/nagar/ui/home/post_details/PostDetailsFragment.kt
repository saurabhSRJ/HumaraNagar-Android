package com.humara.nagar.ui.home.post_details

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.Player
import com.humara.nagar.KohiiProvider
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.Role
import com.humara.nagar.adapter.PollOptionsAdapter
import com.humara.nagar.adapter.PollOptionsPreviewAdapter
import com.humara.nagar.adapter.PostCommentsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.*
import com.humara.nagar.permissions.PermissionHandler
import com.humara.nagar.ui.home.HomeFragment
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostComments
import com.humara.nagar.ui.home.model.PostType
import com.humara.nagar.ui.report.ReportFragment
import com.humara.nagar.utils.*
import kohii.v1.core.MemoryMode
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii

class PostDetailsFragment : BaseFragment(), Playback.ArtworkHintListener {
    private var _binding: FragmentPostDetailsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val navController: NavController by lazy {
        findNavController()
    }
    private val postDetailsViewModel by viewModels<PostDetailsViewModel> {
        ViewModelFactory()
    }
    private val pollOptionsAdapter: PollOptionsAdapter by lazy {
        PollOptionsAdapter {
            postDetailsViewModel.submitVote(it)
        }
    }
    private val args: PostDetailsFragmentArgs by navArgs()
    private val postCommentsAdapter: PostCommentsAdapter by lazy {
        PostCommentsAdapter(args.authorId) {
            postDetailsViewModel.deleteComment(it)
        }
    }
    private lateinit var kohii: Kohii

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController.currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<Long>(HomeFragment.UPDATE_POST).observe(viewLifecycleOwner) {
                postDetailsViewModel.getPostDetails()
                updatePostOnHomeScreen()
                remove<Long>(HomeFragment.UPDATE_POST)
            }
        }
        setUpVideoPlayer()
        initViewModelAndObservers()
        initView()
    }

    private fun setUpVideoPlayer() {
        kohii = KohiiProvider.get(requireContext())
        kohii.register(this, memoryMode = MemoryMode.HIGH)
            .addBucket(binding.nsvPost)
    }

    private fun initViewModelAndObservers() {
        postDetailsViewModel.run {
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            observeProgress(this)
            postDetailsLiveData.observe(viewLifecycleOwner) {
                inflatePostDetails(it)
            }
            postDetailsErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message)
            }
            initialCommentsLiveData.observe(viewLifecycleOwner) {
                showPostComments(it)
            }
            loadMoreCommentsLiveData.observe(viewLifecycleOwner) {
                binding.tvNoComments.visibility = View.GONE
                binding.rvComments.visibility = View.VISIBLE
                postCommentsAdapter.setData(it)
            }
            postCommentsErrorLiveData.observe(viewLifecycleOwner) {
                showPaginationLoadError()
            }
            commentLoaderLiveData.observe(viewLifecycleOwner) { progress ->
                if (progress) {
                    showPaginationLoader()
                } else {
                    hidePaginationLoader()
                }
            }
            addCommentSuccessLiveData.observe(viewLifecycleOwner) {
                binding.etAddComment.run {
                    clearFocus()
                    setText("")
                }
                updatePostOnHomeScreen()
            }
            addCommentErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            deleteCommentSuccessLiveData.observe(viewLifecycleOwner) {
                updatePostOnHomeScreen()
            }
            deleteCommentErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            voteSuccessLiveData.observe(viewLifecycleOwner) { post ->
                handlePollUI(binding.postLayout.pollLayout, post)
                updatePostOnHomeScreen()
            }
            voteErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            likePostSuccessLiveData.observe(viewLifecycleOwner) {
                updatePostOnHomeScreen()
            }
            isLikedLiveData.observe(viewLifecycleOwner) {
                updateLikeButton(it)
            }
            totalLikesLiveData.observe(viewLifecycleOwner) { count ->
                binding.postLayout.postFooter.tvLikeCount.apply {
                    visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
                    text = count.toString()
                }
            }
            likePostErrorLiveData.observe(viewLifecycleOwner) {
                requireContext().showToast(getString(R.string.like_button_error_message), true)
            }
            deletePostLiveData.observe(viewLifecycleOwner) { id ->
                deletePostOnHomeScreen(id)
                requireContext().showToast(getString(R.string.post_deleted))
                navController.navigateUp()
            }
        }
    }

    private fun deletePostOnHomeScreen(postId: Long) {
        navController.previousBackStackEntry?.savedStateHandle?.set(HomeFragment.DELETE_POST, postId)
    }

    private fun updatePostOnHomeScreen() {
        navController.previousBackStackEntry?.savedStateHandle?.set(HomeFragment.UPDATE_POST, args.postId)
    }

    private fun initView() {
        binding.run {
            toolbar.apply {
                leftIcon.setOnClickListener {
                    navController.navigateUp()
                }
                toolbarTitle.text = getString(R.string.details)
            }
            clContainer.setOnClickListener { hideKeyboard() }
            postLayout.run {
                rvComments.apply {
                    setHasFixedSize(true)
                    adapter = postCommentsAdapter
                }
                paginationLoader.retry.setNonDuplicateClickListener {
                    postDetailsViewModel.getPostComments()
                }
                postFooter.ivComment.setOnClickListener {
                    etAddComment.requestFocus()
                    showKeyboard(etAddComment)
                }
            }
            nsvPost.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                if (v.getChildAt(v.childCount - 1) != null) {
                    if (scrollY >= v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight && scrollY > oldScrollY
                        && postDetailsViewModel.canLoadMoreData && postDetailsViewModel.commentLoaderLiveData.value == false
                    ) {
                        postDetailsViewModel.getPostComments()
                    }
                }
            }
            etAddComment.doAfterTextChanged {
                val input = it.toString().trim()
                if (input.isNotEmpty()) {
                    tilAddComment.setEndIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary_color))
                    tilAddComment.setEndIconOnClickListener {
                        postDetailsViewModel.addComment(input)
                    }
                } else {
                    tilAddComment.setEndIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.grey_AEAEAE))
                }
            }
            etAddComment.setMaxLength(100)
        }
    }

    private fun inflatePostDetails(post: Post) {
        binding.run {
            nsvPost.visibility = View.VISIBLE
            clAddComment.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
        when (post.type) {
            PostType.TEXT.type -> inflateTextPostDetails(post)
            PostType.IMAGE.type -> inflateImagePostDetails(post)
            PostType.POLL.type -> inflatePollPostDetails(post)
            PostType.DOCUMENT.type -> inflateDocumentPostDetails(post)
            PostType.VIDEO.type -> inflateVideoPostDetails(post)
            else -> {} //NA
        }
    }

    private fun showPostComments(response: PostComments) {
        binding.run {
            if (response.comments.isEmpty()) {
                tvNoComments.visibility = View.VISIBLE
                tvNoComments.text = getString(R.string.no_comments_yet)
                rvComments.visibility = View.GONE
            } else {
                postLayout.postFooter.tvCommentCount.text = response.totalCount.toString()
                tvNoComments.visibility = View.GONE
                rvComments.visibility = View.VISIBLE
                postCommentsAdapter.setData(response.comments)
            }
        }
    }

    private fun inflateTextPostDetails(post: Post) {
        binding.postLayout.run {
            handlePostHeaderUI(postHeader, post)
            handleCommonPostContent(postContent, post.caption)
            handlePostFooterUI(postFooter, post)
        }
    }

    private fun inflateImagePostDetails(post: Post) {
        binding.postLayout.run {
            handlePostHeaderUI(postHeader, post)
            handleCommonPostContent(postContent, post.caption)
            handlePostFooterUI(postFooter, post)
            post.info?.mediaDetails?.getOrNull(0)?.media?.let { url ->
                ivPostImage.visibility = View.VISIBLE
                ivPostImage.loadUrl(url, R.drawable.ic_image_placeholder)
                ivPostImage.setNonDuplicateClickListener {
                    navController.navigate(PostDetailsFragmentDirections.actionPostDetailsFragmentToFullImagePreviewFragment(arrayOf(url), getScreenName()))
                }
            }
        }
    }

    private fun inflatePollPostDetails(post: Post) {
        binding.postLayout.run {
            handlePostHeaderUI(postHeader, post)
            handleCommonPostContent(postContent, post.caption)
            pollLayout.root.visibility = View.VISIBLE
            handlePollUI(pollLayout, post)
            handlePostFooterUI(postFooter, post)
        }
    }

    private fun inflateDocumentPostDetails(post: Post) {
        binding.postLayout.run {
            handlePostHeaderUI(postHeader, post)
            handleCommonPostContent(postContent, post.caption)
            handlePostFooterUI(postFooter, post)
            post.info?.mediaDetails?.getOrNull(0)?.media?.let { url ->
                tvDocumentPreview.visibility = View.VISIBLE
                tvDocumentPreview.text = FileUtils.getFileName(url)
                tvDocumentPreview.setNonDuplicateClickListener {
                    FileUtils.openPdfUrl(requireContext(), FeedUtils.getDocumentUrl(url))
                }
            }
        }
    }

    private fun inflateVideoPostDetails(post: Post) {
        binding.postLayout.run {
            handlePostHeaderUI(postHeader, post)
            handleCommonPostContent(postContent, post.caption)
            handlePostFooterUI(postFooter, post)
            post.info?.mediaDetails?.getOrNull(0)?.media?.let { url ->
                val videoUrl = VideoUtils.getVideoUrl(url)
                val videoUri = Uri.parse(videoUrl)
                playerViewContainer.visibility = View.VISIBLE
                videoPreview.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder)
                kohii.setUp(videoUri) {
                    tag = videoUri
                    preload = true
                    repeatMode = Player.REPEAT_MODE_ONE
                    artworkHintListener = this@PostDetailsFragment
                }.bind(playerViewContainer)
                playerViewContainer.setNonDuplicateClickListener {
                    it.findNavController().navigate(PostDetailsFragmentDirections.actionPostDetailsFragmentToVideoPlayerFragment(videoUri, getScreenName()))
                }
            }
        }
    }

    override fun onArtworkHint(playback: Playback, shouldShow: Boolean, position: Long, state: Int) {
        binding.postLayout.videoPreview.root.isVisible = shouldShow
    }

    private fun showPaginationLoader() {
        binding.paginationLoader.apply {
            progress.visibility = View.VISIBLE
            retry.visibility = View.GONE
        }
    }

    private fun hidePaginationLoader() {
        binding.paginationLoader.progress.visibility = View.GONE
    }

    private fun showPaginationLoadError() {
        binding.run {
            tvNoComments.visibility = View.VISIBLE
            tvNoComments.text = getString(R.string.error_loading_comments)
            paginationLoader.retry.visibility = View.VISIBLE
        }
    }

    private fun handlePostHeaderUI(postHeader: LayoutPostHeaderBinding, post: Post) {
        postHeader.apply {
            tvName.text = post.name
            tvRoleAndWard.text = FeedUtils.getRoleAndWardText(requireContext(), post.role, post.ward)
            tvPostTime.text = DateTimeUtils.getRelativeDurationFromCurrentTime(requireContext(), post.createdAt)
            ivStar.isVisible = Role.isAdmin(post.roleId)
            ivOptions.isVisible = post.isEditableByUser(requireContext())
            ivOptions.setNonDuplicateClickListener {
                showPostOptionMenu(post, it)
            }
            post.profileImage?.let { url ->
                ivProfilePhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
            }
        }
    }

    private fun handleCommonPostContent(postContent: ExpandableTextView, content: String?) {
        postContent.apply {
            setVisibilityAndText(content)
            maxLines = Int.MAX_VALUE
        }
    }

    private fun handlePostFooterUI(postFooter: LayoutPostFooterBinding, post: Post) {
        postFooter.apply {
            if (post.totalComments > 0) tvCommentCount.text = post.totalComments.toString()
            ivLike.setNonDuplicateClickListener {
                postDetailsViewModel.flipUserLike()
            }
            ivShare.setNonDuplicateClickListener {
                onSharePostClick()
            }
        }
    }

    private fun handlePollUI(pollPostBinding: LayoutPollPostBinding, post: Post) {
        pollPostBinding.run {
            post.info?.let {
                tvQuestion.text = it.question
                tvSubTitle.text = if (it.isAllowedToVote()) getString(R.string.you_can_see_how_people_vote) else resources.getQuantityString(R.plurals.n_votes, it.totalVotes, it.totalVotes)
                rvOptions.apply {
                    adapter = pollOptionsAdapter
                    setHasFixedSize(true)
                }
                pollOptionsAdapter.setData(it)
                if (it.isExpired()) {
                    tvExpiryTime.text = getString(R.string.completed)
                    tvExpiryTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.stroke_green))
                } else {
                    tvExpiryTime.text = DateTimeUtils.getRemainingDurationForPoll(requireContext(), it.expiryTime)
                    tvExpiryTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_585C60))
                }
            }
        }
    }

    private fun showPostOptionMenu(post: Post, anchorView: View) {
        val menuBinding = PostOptionsMenuBinding.inflate(LayoutInflater.from(context), null, false)
        val popupWindow = PopupWindow(menuBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            isOutsideTouchable = true
            isFocusable = true
            elevation = 5.0f
        }
        menuBinding.run {
            menuEdit.setNonDuplicateClickListener {
                navController.navigate(PostDetailsFragmentDirections.actionPostDetailsFragmentToCreatePostFragment(true, args.postId))
                popupWindow.dismiss()
            }
            menuDelete.setOnClickListener {
                FeedUtils.showDeletePostConfirmationDialog(parentFragmentManager, requireContext()) {
                    postDetailsViewModel.deletePost(post.postId)
                }
                popupWindow.dismiss()
            }
        }
        popupWindow.showAsDropDown(anchorView, (-1 * (resources.getDimensionPixelOffset(R.dimen.post_option_menu_offset))), 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateLikeButton(isLiked: Boolean) {
        binding.postLayout.postFooter.ivLike.setImageResource(if (isLiked) R.drawable.ic_like_selected else R.drawable.ic_like_unselected)
    }

    private fun onSharePostClick() {
        (activity as BaseActivity).requestPermissions(PermissionUtils.sharePostPermissions, object : PermissionHandler {
            override fun onPermissionGranted() {
                if (context == null) {
                    Logger.debugLog(ReportFragment.TAG, "fragment detached from the activity")
                    return
                }
                sharePost()
            }

            override fun onPermissionDenied(permissions: List<String>) {
                //NA
            }
        })
    }

    private fun sharePost() {
        showProgress(true)
        inflateSharePostLayout()
    }

    private fun inflateSharePostLayout() {
        val postShareBinding = PostShareLayoutBinding.inflate(layoutInflater, binding.root as ViewGroup, true)
        postDetailsViewModel.postDetailsLiveData.value?.let { post ->
            postShareBinding.run {
                tvName.text = post.name
                tvRoleAndWard.text = FeedUtils.getRoleAndWardText(requireContext(), post.role, post.ward)
                postContent.setVisibilityAndText(post.caption)
                post.profileImage?.let {
                    Glide.with(requireContext())
                        .load(GlideUtil.getUrlWithHeaders(post.profileImage, requireContext()))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .transform(CenterCrop(), RoundedCorners(12))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                addPostTypeDataBeforeSharing(post, postShareBinding)
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                addPostTypeDataBeforeSharing(post, postShareBinding)
                                return false
                            }
                        })
                        .into(ivProfilePhoto)
                } ?: run {
                    addPostTypeDataBeforeSharing(post, postShareBinding)
                }
            }
        }
    }

    private fun addPostTypeDataBeforeSharing(post: Post, binding: PostShareLayoutBinding) {
        when (post.type) {
            PostType.IMAGE.type -> addImagePostShareData(post, binding)
            PostType.POLL.type -> addPollPostShareData(post, binding)
            PostType.VIDEO.type -> addVideoPostShareData(post, binding)
            PostType.DOCUMENT.type, PostType.TEXT.type -> sharePostOnWhatsapp(binding.root, post.name)
            else -> {}
        }
    }

    private fun addImagePostShareData(post: Post, binding: PostShareLayoutBinding) {
        val url = post.info?.mediaDetails?.getOrNull(0)?.media
        url?.let {
            binding.ivPostImage.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                .placeholder(R.drawable.ic_image_placeholder)
                .transform(CenterCrop(), RoundedCorners(12))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        sharePostOnWhatsapp(binding.root, post.name)
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        sharePostOnWhatsapp(binding.root, post.name)
                        return false
                    }
                })
                .into(binding.ivPostImage)
        } ?: run {
            sharePostOnWhatsapp(binding.root, post.name)
        }
    }

    private fun addPollPostShareData(post: Post, binding: PostShareLayoutBinding) {
        binding.pollLayout.run {
            root.visibility = View.VISIBLE
            post.info?.let {
                tvQuestion.text = it.question
                tvSubTitle.text = resources.getQuantityString(R.plurals.n_votes, it.totalVotes, it.totalVotes)
                rvOptions.apply {
                    adapter = PollOptionsPreviewAdapter(it.getOptionsText())
                    setHasFixedSize(true)
                }
                pollOptionsAdapter.setData(it)
                if (it.isExpired()) {
                    tvExpiryTime.text = getString(R.string.completed)
                    tvExpiryTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.stroke_green))
                } else {
                    tvExpiryTime.text = DateTimeUtils.getRemainingDurationForPoll(requireContext(), it.expiryTime)
                    tvExpiryTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_585C60))
                }
            }
            sharePostOnWhatsapp(binding.root, post.name)
        }
    }

    private fun addVideoPostShareData(post: Post, binding: PostShareLayoutBinding) {
        binding.videoThumbnail.run {
            val url = post.info?.mediaDetails?.getOrNull(0)?.thumbnailUrl
            url?.let {
                root.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .transform(CenterCrop(), RoundedCorners(12))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            sharePostOnWhatsapp(binding.root, post.name)
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            sharePostOnWhatsapp(binding.root, post.name)
                            return false
                        }
                    })
                    .into(ivThumbnail)
            } ?: run {
                sharePostOnWhatsapp(binding.root, post.name)
            }
        }
    }

    private fun sharePostOnWhatsapp(postShareView: View, name: String) {
        postShareView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                postShareView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val shareProfileViaWhatsAppBitmap = Bitmap.createBitmap(
                    postShareView.measuredWidth,
                    postShareView.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
                val c = Canvas(shareProfileViaWhatsAppBitmap!!)
                postShareView.draw(c)
                IntentUtils.shareViaIntent(
                    requireActivity(),
                    shareProfileViaWhatsAppBitmap,
                    getString(R.string.share_post_caption, name, "https://humara.nagar/post/${args.postId}/${args.authorId}/send")
                )
                (binding.root as ViewGroup).removeView(postShareView)
                hideProgress()
            }
        })
    }

    override fun getScreenName() = AnalyticsData.ScreenName.POST_DETAILS_FRAGMENT
}