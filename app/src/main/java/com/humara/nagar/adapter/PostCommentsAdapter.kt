package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.PostCommentItemBinding
import com.humara.nagar.ui.home.model.CommentDetails
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.loadUrl

class PostCommentsAdapter : RecyclerView.Adapter<PostCommentsAdapter.CommentsViewHolder>() {
    private val comments: ArrayList<CommentDetails> = arrayListOf()

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
                    ivUserPhoto.loadUrl(url, R.drawable.ic_user_image_placeholder)
                }
                tvUserName.text = item.name
                tvComment.text = item.comment
                tvCommentTime.text = DateTimeUtils.getRelativeDurationFromCurrentTime(root.context, item.createdAt)
            }
        }
    }
}