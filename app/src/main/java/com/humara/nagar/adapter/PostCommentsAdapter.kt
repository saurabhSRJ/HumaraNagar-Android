package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.humara.nagar.R
import com.humara.nagar.databinding.PostCommentItemBinding
import com.humara.nagar.ui.home.model.CommentDetails
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.GlideUtil

class PostCommentsAdapter : RecyclerView.Adapter<PostCommentsAdapter.CommentsViewHolder>() {
    private val comments: ArrayList<CommentDetails> = arrayListOf()

    object CommentsDiffUtil : DiffUtil.ItemCallback<CommentDetails>() {
        override fun areItemsTheSame(oldItem: CommentDetails, newItem: CommentDetails): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CommentDetails, newItem: CommentDetails): Boolean {
            return oldItem.commentId == newItem.commentId
        }
    }

//    fun addMoreData(newData: List<CommentDetails>) {
//        val newList = mutableListOf<CommentDetails>().apply { addAll(currentList) }
//        newList.addAll(newData)
//        Logger.debugLog("newList: $newList")
//        submitList(newList)
//    }

    fun setData(data: List<CommentDetails>) {
        comments.clear()
        comments.addAll(data)
        notifyDataSetChanged()
    }

    fun addMoreData(moreData: List<CommentDetails>) {
        comments.addAll(moreData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        return CommentsViewHolder(PostCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = comments.size

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    inner class CommentsViewHolder(val binding: PostCommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentDetails) {
            binding.run {
                item.image?.let { url ->
                    Glide.with(root.context)
                        .load(GlideUtil.getUrlWithHeaders(url, root.context))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .transform(CenterCrop(), RoundedCorners(12))
                        .transition(DrawableTransitionOptions.withCrossFade(1000))
                        .into(ivUserPhoto)
                }
                tvUserName.text = item.name
                tvComment.text = item.comment
                tvCommentTime.text = DateTimeUtils.getRelativeDurationFromCurrentTime(root.context, item.createdAt)
            }
        }
    }
}