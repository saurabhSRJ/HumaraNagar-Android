package com.humara.nagar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.databinding.*
import com.humara.nagar.ui.home.HomeFragmentDirections
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostType
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.GlideUtil
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.setVisibilityAndText

class PostAdapter(val context: Context, val listener: FeedItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TEXT_POST = 0
        const val IMAGE_POST = 2
        const val DOCUMENT_POST = 3
        const val POLLING_POST = 4
        const val VIDEO_POST = 5
    }

    private val posts: ArrayList<Post> = arrayListOf()
    private val source = AnalyticsData.ScreenName.HOME_FRAGMENT

    fun setData(data: List<Post>) {
        posts.clear()
        posts.addAll(data)
        notifyDataSetChanged()
    }

    fun addMoreData(moreData: List<Post>) {
        val prevListEnd = posts.size
        posts.addAll(moreData)
        notifyItemRangeInserted(prevListEnd, moreData.size)
    }

    fun updatePost(post: Post) {
        val position = posts.indexOfFirst { it.postId == post.postId }
        posts[position] = post
        notifyItemChanged(position)
    }

    fun deletePost(postId: Long) {
        val position = posts.indexOfFirst { it.postId == postId }
        posts.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            TEXT_POST -> TextPostViewHolder(binding)
            IMAGE_POST -> ImagePostViewHolder(binding)
            POLLING_POST -> PollingPostViewHolder(binding)
            else -> TextPostViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = posts[position]
        when (holder.itemViewType) {
            TEXT_POST -> (holder as TextPostViewHolder).bind(item)
            IMAGE_POST -> (holder as ImagePostViewHolder).bind(item)
            POLLING_POST -> (holder as PollingPostViewHolder).bind(item)
            else -> (holder as TextPostViewHolder).bind(item)
        }
    }

    override fun getItemCount() = posts.size

    override fun getItemViewType(position: Int): Int {
        return when (posts[position].type) {
            PostType.TEXT.type -> TEXT_POST
            PostType.IMAGE.type -> IMAGE_POST
            PostType.POLL.type -> POLLING_POST
            else -> TEXT_POST
        }
    }

    inner class TextPostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post) {
            binding.run {
                handlePostHeaderUI(postHeader, item)
                handleCommonPostContent(postContent, item)
                handlePostFooterUI(postFooter, item, adapterPosition)
            }
        }
    }

    inner class ImagePostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post) {
            binding.run {
                handlePostHeaderUI(postHeader, item)
                handleCommonPostContent(postContent, item)
                handlePostFooterUI(postFooter, item, adapterPosition)
                item.info?.medias?.getOrNull(0)?.let { url ->
                    ivPostImage.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(GlideUtil.getUrlWithHeaders(url, context))
                        .transform(CenterCrop(), RoundedCorners(12))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade(1000))
                        .into(ivPostImage)
                    ivPostImage.setNonDuplicateClickListener {
                        it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFullImagePreviewFragment(item.info.medias.toTypedArray(), source))
                    }
                }
            }
        }
    }

    inner class PollingPostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post) {
            binding.run {
                handlePostHeaderUI(postHeader, item)
                postContent.visibility = View.GONE
                pollLayout.root.visibility = View.VISIBLE
                handlePollUI(pollLayout, item)
                handlePostFooterUI(postFooter, item, adapterPosition)
            }
        }
    }

    private fun handlePostHeaderUI(postHeader: LayoutPostHeaderBinding, post: Post) {
        postHeader.apply {
            tvName.text = post.name
            tvLocality.setVisibilityAndText(post.locality)
            tvPostTime.text = DateTimeUtils.getRelativeDurationFromCurrentTime(context, post.createdAt)
            ivOptions.isVisible = post.isCreatedByUser(context)
            ivOptions.setOnClickListener {
                showPostOptionMenu(post, it)
            }
            post.profileImage?.let { url ->
                Glide.with(context)
                    .load(GlideUtil.getUrlWithHeaders(url, context))
                    .transform(CenterCrop(), RoundedCorners(12))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade(1000))
                    .into(ivProfilePhoto)
            }
        }
    }

    private fun handleCommonPostContent(postContent: ExpandableTextView, post: Post) {
        postContent.apply {
            visibility = View.VISIBLE
            text = post.caption
            setOnClickListener {
                if (isExpanded) {
                    Logger.debugLog("open post detail")
                    it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(post.postId, source))
                } else {
                    Logger.debugLog("expand content")
                    expand()
                }
            }
        }
    }

    private fun handlePostFooterUI(postFooter: LayoutPostFooterBinding, post: Post, position: Int) {
        postFooter.apply {
            postFooter.apply {
                if (post.totalComments > 0) tvCommentCount.text = post.totalComments.toString()
                tvLikeCount.apply {
                    visibility = if (post.totalLikes > 0) View.VISIBLE else View.INVISIBLE
                    text = post.totalLikes.toString()
                }
                ivLike.setNonDuplicateClickListener {
                    listener.onLikeButtonClick(post)
                    post.totalLikes = if (post.hasUserLike()) post.totalLikes.minus(1) else post.totalLikes.plus(1)
                    post.isLikedByUser = (post.isLikedByUser + 1) % 2
                    notifyItemChanged(position)
                }
                ivLike.setImageResource(if (post.hasUserLike()) R.drawable.ic_like_selected else R.drawable.ic_like_unselected)
                ivShare.setOnClickListener {

                }
                ivComment.setNonDuplicateClickListener {
                    it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(post.postId, source))
                }
            }
        }
    }

    private fun handlePollUI(pollPostBinding: LayoutPollPostBinding, post: Post) {
        pollPostBinding.apply {
            post.info?.let {
                tvQuestion.text = it.question
                tvSubTitle.text = if (it.isAllowedToVote()) context.getString(R.string.you_can_see_how_people_vote) else context.resources.getQuantityString(R.plurals.n_votes, it
                    .totalVotes, it
                    .totalVotes)
                rvOptions.apply {
                    val pollOptionsAdapter = PollOptionsAdapter { optionId ->
                        listener.submitVote(post, optionId)
                    }
                    adapter = pollOptionsAdapter
                    setHasFixedSize(true)
                    pollOptionsAdapter.setData(it)
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
                listener.onEditPostClick(post)
                popupWindow.dismiss()
            }
            menuDelete.setOnClickListener {
                listener.onDeletePostClick(post)
                popupWindow.dismiss()
            }
        }
        popupWindow.showAsDropDown(anchorView, (-1 * (context.resources.getDimensionPixelOffset(R.dimen.post_option_menu_offset))), 0)
    }
}

interface FeedItemClickListener {
    fun onLikeButtonClick(post: Post)
    fun submitVote(post: Post, optionId: Int)

    fun onEditPostClick(post: Post)

    fun onDeletePostClick(post: Post)
}