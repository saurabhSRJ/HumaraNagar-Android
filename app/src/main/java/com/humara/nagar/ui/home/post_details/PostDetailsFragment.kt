package com.humara.nagar.ui.home.post_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.adapter.PollOptionsAdapter
import com.humara.nagar.adapter.PostCommentsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentPostDetailsBinding
import com.humara.nagar.databinding.LayoutPollPostBinding
import com.humara.nagar.databinding.LayoutPostFooterBinding
import com.humara.nagar.databinding.LayoutPostHeaderBinding
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostComments
import com.humara.nagar.ui.home.model.PostType
import com.humara.nagar.utils.*

class PostDetailsFragment : BaseFragment() {
    private lateinit var binding: FragmentPostDetailsBinding
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
    private val postCommentsAdapter: PostCommentsAdapter by lazy {
        PostCommentsAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                postCommentsAdapter.addMoreData(it)
            }
            postCommentsErrorLiveData.observe(viewLifecycleOwner) {
                showCommentsLoadError()
            }
            commentLoaderLiveData.observe(viewLifecycleOwner) { progress ->
                if (progress) {
                    showCommentsLoader()
                } else {
                    hideCommentsLoader()
                }
            }
            addCommentErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
            }
            voteSuccessLiveData.observe(viewLifecycleOwner) { post ->
                handlePollUI(binding.postLayout.pollLayout, post)
            }
            voteErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, errorAction = {}, dismissAction = {})
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
        }
    }

    private fun initView() {
        binding.run {
            toolbar.apply {
                leftIcon.setOnClickListener {
                    navController.navigateUp()
                }
                toolbarTitle.text = "Details"
            }
            clContainer.setOnClickListener { hideKeyboard() }
            postLayout.root.setOnClickListener { hideKeyboard() }
            postLayout.commentDivider.visibility = View.VISIBLE
            postLayout.rvComments.apply {
                setHasFixedSize(true)
                adapter = postCommentsAdapter
            }
            nsvPost.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                Logger.debugLog("childCount: ${v.childCount}, lastItem: ${v.getChildAt(v.childCount - 1)}")
                if (v.getChildAt(v.childCount - 1) != null) {
                    if (scrollY >= v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight && scrollY > oldScrollY
                        && postDetailsViewModel.canLoadMoreData && postDetailsViewModel.commentLoaderLiveData.value == false
                    ) {
                        postDetailsViewModel.getPostComments(true)
                    }
                }
            }
            etAddComment.doAfterTextChanged {
                val input = it.toString().trim()
                if (input.isNotEmpty()) {
                    tilAddComment.setEndIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_4285F4))
                    tilAddComment.setEndIconOnClickListener {
                        postDetailsViewModel.addComment(input)
                    }
                } else {
                    tilAddComment.setEndIconTintList(ContextCompat.getColorStateList(requireContext(), R.color.grey_AEAEAE))
                }
            }
        }
    }

    private fun inflatePostDetails(post: Post) {
        binding.nsvPost.visibility = View.VISIBLE
        binding.tilAddComment.visibility = View.VISIBLE
        when (post.type) {
            PostType.TEXT.type -> inflateTextPostDetails(post)
            PostType.IMAGE.type -> inflateImagePostDetails(post)
            PostType.POLL.type -> inflatePollPostDetails(post)
            else -> ""
        }
    }

    private fun showPostComments(response: PostComments) {
        binding.etAddComment.run {
            clearFocus()
            setText("")
        }
        binding.postLayout.run {
            postFooter.tvCommentCount.text = response.totalCount.toString()
            if (response.comments.isNullOrEmpty()) {
                tvNoComments.visibility = View.VISIBLE
                tvNoComments.text = getString(R.string.no_comments_yet)
            } else {
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
            postContent.visibility = View.GONE
            pollLayout.root.visibility = View.VISIBLE
            handlePollUI(pollLayout, post)
            handlePostFooterUI(postFooter, post)
        }
    }

    private fun showCommentsLoader() {
        binding.postLayout.paginationLoader.apply {
            progress.visibility = View.VISIBLE
            retry.visibility = View.GONE
        }
    }

    private fun hideCommentsLoader() {
        binding.postLayout.paginationLoader.progress.visibility = View.GONE
    }

    private fun showCommentsLoadError() {
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
            ivOptions.setNonDuplicateClickListener {

            }
        }
    }

    private fun handleCommonPostContent(postContent: ExpandableTextView, content: String?) {
        postContent.apply {
            text = content
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

    private fun updateLikeButton(isLiked: Boolean) {
        binding.postLayout.postFooter.ivLike.setImageResource(if (isLiked) R.drawable.ic_like_selected else R.drawable.ic_like_unselected)
    }

    override fun getScreenName() = AnalyticsData.ScreenName.POST_DETAILS_FRAGMENT
}