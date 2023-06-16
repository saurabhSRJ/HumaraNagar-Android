package com.humara.nagar.ui.home.post_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.humara.nagar.R
import com.humara.nagar.adapter.PollOptionsAdapter
import com.humara.nagar.adapter.PostCommentsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.*
import com.humara.nagar.ui.home.HomeFragment
import com.humara.nagar.ui.home.HomeViewModel
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostComments
import com.humara.nagar.ui.home.model.PostType
import com.humara.nagar.utils.*

class PostDetailsFragment : BaseFragment() {
    private var _binding: FragmentPostDetailsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val navController: NavController by lazy {
        findNavController()
    }
    private val postDetailsViewModel by viewModels<PostDetailsViewModel> {
        ViewModelFactory()
    }
    private val homeViewModel by navGraphViewModels<HomeViewModel>(R.id.home_navigation) {
        ViewModelFactory()
    }
    private val pollOptionsAdapter: PollOptionsAdapter by lazy {
        PollOptionsAdapter {
            postDetailsViewModel.submitVote(it)
        }
    }
    private val postCommentsAdapter: PostCommentsAdapter by lazy {
        PostCommentsAdapter()
    }
    private val args: PostDetailsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController.currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<Long>(HomeFragment.UPDATE_POST).observe(viewLifecycleOwner) { id ->
                postDetailsViewModel.getPostDetails()
                updatePostOnHomeScreen(id)
                remove<Long>(HomeFragment.UPDATE_POST)
            }
        }
        initViewModelAndObservers()
        initView()
    }

    private fun initViewModelAndObservers() {
        postDetailsViewModel.run {
            observeErrorAndException(this)
            observeProgress(this)
            postDetailsLiveData.observe(viewLifecycleOwner) {
                inflatePostDetails(it)
            }
            postDetailsErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog { }
            }
            initialCommentsLiveData.observe(viewLifecycleOwner) {
                showPostComments(it)
            }
            loadMoreCommentsLiveData.observe(viewLifecycleOwner) {
                binding.postLayout.tvNoComments.visibility = View.GONE
                binding.postLayout.rvComments.visibility = View.VISIBLE
                postCommentsAdapter.addMoreData(it)
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
                updatePostOnHomeScreen(it)
            }
            addCommentErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            voteSuccessLiveData.observe(viewLifecycleOwner) { post ->
                handlePollUI(binding.postLayout.pollLayout, post)
                updatePostOnHomeScreen(post.postId)
            }
            voteErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            likePostSuccessLiveData.observe(viewLifecycleOwner) {
                updatePostOnHomeScreen(it)
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
                homeViewModel.deletePostFromFeed(id)
                requireContext().showToast(getString(R.string.post_deleted))
                navController.navigateUp()
            }
        }
    }

    private fun updatePostOnHomeScreen(postId: Long) {
        homeViewModel.setPostUpdateRequired(postId)
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
                root.setOnClickListener { hideKeyboard() }
                commentDivider.visibility = View.VISIBLE
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
        binding.nsvPost.visibility = View.VISIBLE
        binding.tilAddComment.visibility = View.VISIBLE
        when (post.type) {
            PostType.TEXT.type -> inflateTextPostDetails(post)
            PostType.IMAGE.type -> inflateImagePostDetails(post)
            PostType.POLL.type -> inflatePollPostDetails(post)
            PostType.DOCUMENT.type -> inflateDocumentPostDetails(post)
            else -> {} //NA
        }
    }

    private fun showPostComments(response: PostComments) {
        binding.etAddComment.run {
            clearFocus()
            setText("")
        }
        binding.postLayout.run {
            if (response.comments.isNullOrEmpty()) {
                tvNoComments.visibility = View.VISIBLE
                tvNoComments.text = getString(R.string.no_comments_yet)
            } else {
                postFooter.tvCommentCount.text = response.totalCount.toString()
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
            post.info?.medias?.getOrNull(0)?.let { url ->
                ivPostImage.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                    .transform(CenterCrop(), RoundedCorners(12))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade(1000))
                    .into(ivPostImage)
                ivPostImage.setNonDuplicateClickListener {
                    navController.navigate(PostDetailsFragmentDirections.actionPostDetailsFragmentToFullImagePreviewFragment(post.info.medias.toTypedArray(), getScreenName()))
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
            post.info?.medias?.getOrNull(0)?.let { url ->
                tvDocumentPreview.visibility = View.VISIBLE
                tvDocumentPreview.text = FileUtil.getFileName(url)
                tvDocumentPreview.setNonDuplicateClickListener {
                    FileUtil.openPdfUrl(requireContext(), FeedUtils.getDocumentUrl(url))
                }
            }
        }
    }

    private fun showPaginationLoader() {
        binding.postLayout.paginationLoader.apply {
            progress.visibility = View.VISIBLE
            retry.visibility = View.GONE
        }
    }

    private fun hidePaginationLoader() {
        binding.postLayout.paginationLoader.progress.visibility = View.GONE
    }

    private fun showPaginationLoadError() {
        binding.postLayout.run {
            tvNoComments.visibility = View.VISIBLE
            tvNoComments.text = getString(R.string.error_loading_comments)
            paginationLoader.retry.visibility = View.VISIBLE
        }
    }

    private fun handlePostHeaderUI(postHeader: LayoutPostHeaderBinding, post: Post) {
        postHeader.apply {
            tvName.text = post.name
            tvLocality.setVisibilityAndText(post.locality)
            tvPostTime.text = DateTimeUtils.getRelativeDurationFromCurrentTime(requireContext(), post.createdAt)
            ivOptions.isVisible = post.isCreatedByUser(requireContext())
            ivOptions.setNonDuplicateClickListener {
                showPostOptionMenu(post, it)
            }
            post.profileImage?.let { url ->
                Glide.with(requireContext())
                    .load(GlideUtil.getUrlWithHeaders(url, requireContext()))
                    .transform(CenterCrop(), RoundedCorners(12))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade(1000))
                    .into(ivProfilePhoto)
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

            }
        }
    }

    private fun handlePollUI(pollPostBinding: LayoutPollPostBinding, post: Post) {
        pollPostBinding.apply {
            post.info?.let {
                tvQuestion.text = it.question
                tvSubTitle.text = if (it.isAllowedToVote()) getString(R.string.you_can_see_how_people_vote) else resources.getQuantityString(R.plurals.n_votes, it.totalVotes, it.totalVotes)
                rvOptions.apply {
                    adapter = pollOptionsAdapter
                    setHasFixedSize(true)
                }
                pollOptionsAdapter.setData(it)
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

    override fun getScreenName() = AnalyticsData.ScreenName.POST_DETAILS_FRAGMENT
}