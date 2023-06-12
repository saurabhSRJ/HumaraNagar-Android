package com.humara.nagar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.databinding.LayoutPostFooterBinding
import com.humara.nagar.databinding.LayoutPostHeaderBinding
import com.humara.nagar.databinding.PostItemBinding
import com.humara.nagar.ui.home.HomeFragmentDirections
import com.humara.nagar.utils.GlideUtil
import com.humara.nagar.utils.setNonDuplicateClickListener

class PostAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TEXT_POST = 0
        const val IMAGE_POST = 2
        const val DOCUMENT_POST = 3
        const val POLLING_POST = 4
        const val VIDEO_POST = 5
    }

    private var list: List<String> = listOf("The term \"Mossad\" refers to the national intelligence agency of Israel. It is an acronym in Hebrew that stands for \"HaMossad leModi'in uleTafkidim " +
            "Meyuhadim,\" which translates to \"The Institute for Intelligence and Special Operations\" in English. Mossad is responsible for intelligence gathering, covert operations, and counter terrorism activities on behalf of the State of Israel." +
            " Its primary focus is to protect Israeli national security interests and ensure the safety of its citizens both domestically and abroad.",
        "The state of utah in the united state is home to lots of beautiful parks and beaches.",
        "Hello, I am looking for a new career opportunity and would appreciate your support. Thanks in advance for any contact recommendation, advice.",
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley " +
                "of  type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged.",
        "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages")

    private val postId = 32L
    private val source = "FeedPostItem"
    fun shuffleList() {
        list = list.shuffled()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TEXT_POST -> TextPostViewHolder(PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            IMAGE_POST -> ImagePostViewHolder(PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> TextPostViewHolder(PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        when (holder.itemViewType) {
            TEXT_POST -> (holder as TextPostViewHolder).bind(item)
            IMAGE_POST -> (holder as ImagePostViewHolder).bind(item)
            else -> (holder as TextPostViewHolder).bind(item)
        }
    }

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TEXT_POST
            1, 3 -> IMAGE_POST
            else -> TEXT_POST
        }
    }

    inner class TextPostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.run {
                handlePostHeaderUI(postHeader)
                handleCommonPostContent(postContent, item)
                handlePostFooterUI(postFooter)
            }
        }
    }

    inner class ImagePostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.run {
                val url = "complaints/2/16/1684347008700.jpeg"
                handlePostHeaderUI(postHeader)
                handleCommonPostContent(postContent, item)
                Glide.with(context)
                    .load(GlideUtil.getUrlWithHeaders(url, context))
                    .transform(CenterCrop(), RoundedCorners(12))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade(1000))
                    .into(ivPostImage)
                ivPostImage.setNonDuplicateClickListener {
                    it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFullImagePreviewFragment(arrayOf(url), source))
                }
                handlePostFooterUI(postFooter)
            }
        }
    }

    private fun handlePostHeaderUI(postHeader: LayoutPostHeaderBinding) {
        postHeader.apply {
            tvName.text = "Saurabh Jaiswal"
            tvLocality.text = "sector 40, Gurugram"
            tvPostTime.text = "12 mins ago"
            ivOptions.setNonDuplicateClickListener {

            }
        }
    }

    private fun handleCommonPostContent(postContent: ExpandableTextView, content: String) {
        postContent.apply {
            text = content
            setOnClickListener {
                if (isExpanded) {
                    Logger.debugLog("open post detail")
                    it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(postId, source))
                } else {
                    Logger.debugLog("expand content")
                    expand()
                }
            }
        }
    }

    private fun handlePostFooterUI(postFooter: LayoutPostFooterBinding) {
        postFooter.apply {
            ivLike.setNonDuplicateClickListener {
                ivLike.setImageResource(R.drawable.ic_like_selected)
            }
//            tvLikeCount.text = "2"
//            tvCommentCount.text = "12"
            ivComment.setNonDuplicateClickListener {
                it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPostDetailsFragment(postId, source))
            }
            ivShare.setNonDuplicateClickListener {

            }
        }
    }
}