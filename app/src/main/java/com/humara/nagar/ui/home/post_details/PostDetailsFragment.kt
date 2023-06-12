package com.humara.nagar.ui.home.post_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentPostDetailsBinding
import com.humara.nagar.databinding.LayoutPostFooterBinding
import com.humara.nagar.databinding.LayoutPostHeaderBinding
import com.humara.nagar.ui.home.model.Post
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
            postDetailsLiveData.observe(viewLifecycleOwner) {
                inflatePostDetails(it)
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

    private fun inflatePostDetails(post: Post) {
        when (post.type) {
            PostType.TEXT.type -> inflateTextPostDetails(post)
            PostType.IMAGE.type -> inflateImagePostDetails(post)
            else -> ""
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
            post.info.medias?.getOrNull(0)?.let { url ->
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

    private fun initView() {
        binding.run {
            toolbar.apply {
                leftIcon.setOnClickListener {
                    navController.navigateUp()
                }
                toolbarTitle.text = "Details"
            }
            clContainer.setOnClickListener { hideKeyboard() }
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
            ivComment.setNonDuplicateClickListener {

            }
            ivShare.setNonDuplicateClickListener {

            }
        }
    }

    private fun updateLikeButton(isLiked: Boolean) {
        binding.postLayout.postFooter.ivLike.setImageResource(if (isLiked) R.drawable.ic_like_selected else R.drawable.ic_like_unselected)
    }

    override fun getScreenName() = AnalyticsData.ScreenName.POST_DETAILS_FRAGMENT
}