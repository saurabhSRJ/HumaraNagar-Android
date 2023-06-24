package com.humara.nagar.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.blogc.android.views.ExpandableTextView
import com.google.android.exoplayer2.Player
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.databinding.*
import com.humara.nagar.ui.home.HomeFragmentDirections
import com.humara.nagar.ui.home.model.Post
import com.humara.nagar.ui.home.model.PostType
import com.humara.nagar.utils.*
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii

class PostAdapter(private val kohii: Kohii, val context: Context, val listener: FeedItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
        //NOTE: do not change this to notifyItemRangeInserted(0, data.size). Getting crashes for some reason
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
            DOCUMENT_POST -> DocumentPostViewHolder(binding)
            VIDEO_POST -> VideoViewHolder(binding)
            else -> TextPostViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = posts[position]
        when (holder.itemViewType) {
            TEXT_POST -> (holder as TextPostViewHolder).bind(item)
            IMAGE_POST -> (holder as ImagePostViewHolder).bind(item)
            POLLING_POST -> (holder as PollingPostViewHolder).bind(item)
            DOCUMENT_POST -> (holder as DocumentPostViewHolder).bind(item)
            VIDEO_POST -> (holder as VideoViewHolder).bind(item)
            else -> (holder as TextPostViewHolder).bind(item)
        }
    }

    override fun getItemCount() = posts.size

    override fun getItemViewType(position: Int): Int {
        return when (posts[position].type) {
            PostType.TEXT.type -> TEXT_POST
            PostType.IMAGE.type -> IMAGE_POST
            PostType.POLL.type -> POLLING_POST
            PostType.DOCUMENT.type -> DOCUMENT_POST
            PostType.VIDEO.type -> VIDEO_POST
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
                    ivPostImage.loadUrl(url, R.drawable.ic_image_placeholder)
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
                handleCommonPostContent(postContent, item)
                pollLayout.root.visibility = View.VISIBLE
                handlePollUI(pollLayout, item)
                handlePostFooterUI(postFooter, item, adapterPosition)
            }
        }
    }

    inner class DocumentPostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post) {
            binding.run {
                handlePostHeaderUI(postHeader, item)
                handleCommonPostContent(postContent, item)
                item.info?.medias?.getOrNull(0)?.let { url ->
                    tvDocumentPreview.visibility = View.VISIBLE
                    tvDocumentPreview.text = FileUtils.getFileName(url)
                    tvDocumentPreview.setNonDuplicateClickListener {
                        FileUtils.openPdfUrl(context, FeedUtils.getDocumentUrl(url))
                    }
                }
                handlePostFooterUI(postFooter, item, adapterPosition)
            }
        }
    }

    inner class VideoViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root), Playback.ArtworkHintListener {
        fun bind(item: Post) {
            binding.run {
                handlePostHeaderUI(postHeader, item)
                handleCommonPostContent(postContent, item)
                item.info?.medias?.getOrNull(0)?.let { url ->
                    val videoUrl = VideoUtils.getVideoUrl(url)
                    val videoUri = Uri.parse(videoUrl)
                    playerViewContainer.visibility = View.VISIBLE
                    videoPreview.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder)
                    kohii.setUp(videoUri) {
                        tag = videoUri
                        preload = true
                        repeatMode = Player.REPEAT_MODE_ONE
                        artworkHintListener = this@VideoViewHolder
                    }.bind(playerViewContainer)
                    playerViewContainer.setNonDuplicateClickListener {
                        it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToVideoPlayerFragment(videoUri, source))
                    }
                }
                handlePostFooterUI(postFooter, item, adapterPosition)
            }
        }

        override fun onArtworkHint(playback: Playback, shouldShow: Boolean, position: Long, state: Int) {
            binding.videoPreview.root.isVisible = shouldShow
        }
    }

    private fun handlePostHeaderUI(postHeader: LayoutPostHeaderBinding, post: Post) {
        postHeader.apply {
            tvName.text = post.name
            tvRoleAndWard.text = FeedUtils.getRoleAndWardText(context)
            tvPostTime.text = DateTimeUtils.getRelativeDurationFromCurrentTime(context, post.createdAt)
            ivOptions.isVisible = post.isEditableByUser(context)
            ivOptions.setOnClickListener {
                showPostOptionMenu(post, it)
            }
            post.profileImage?.let { url ->
                ivProfilePhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
            }
        }
    }

    private fun handleCommonPostContent(postContent: ExpandableTextView, post: Post) {
        postContent.apply {
            setVisibilityAndText(post.caption)
            setOnClickListener {
                if (isExpanded) {
                    it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(post.postId, source))
                } else {
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
                    listener.onSharePostClick(post)
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
                tvSubTitle.text = if (it.isAllowedToVote()) context.getString(R.string.you_can_see_how_people_vote) else context.resources.getQuantityString(R.plurals.n_votes, it.totalVotes,
                    it.totalVotes)
                rvOptions.apply {
                    val pollOptionsAdapter = PollOptionsAdapter { optionId ->
                        listener.submitVote(post, optionId)
                    }
                    adapter = pollOptionsAdapter
                    setHasFixedSize(true)
                    pollOptionsAdapter.setData(it)
                }
                if (it.isExpired()) {
                    tvExpiryTime.text = context.getString(R.string.completed)
                    tvExpiryTime.setTextColor(context.getColor(R.color.stroke_green))
                } else {
                    tvExpiryTime.text = DateTimeUtils.getRemainingDurationForPoll(context, it.expiryTime)
                    tvExpiryTime.setTextColor(context.getColor(R.color.grey_585C60))
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

    fun onSharePostClick(post: Post)
}