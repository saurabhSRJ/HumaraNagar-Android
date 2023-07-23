package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.LikedUserItemBinding
import com.humara.nagar.ui.home.model.LikeDetails
import com.humara.nagar.utils.FeedUtils
import com.humara.nagar.utils.loadUrl

class PostLikesAdapter : RecyclerView.Adapter<PostLikesViewHolder>() {
    private val likesList = mutableListOf<LikeDetails>()

    fun setData(data: List<LikeDetails>) {
        likesList.clear()
        likesList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostLikesViewHolder {
        return PostLikesViewHolder(LikedUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = likesList.size

    override fun onBindViewHolder(holder: PostLikesViewHolder, position: Int) {
        holder.bind(likesList[position])
    }
}

class PostLikesViewHolder(val binding: LikedUserItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: LikeDetails) {
        binding.run {
            item.image?.let {
                ivUserPhoto.loadUrl(it, R.drawable.man_user_icon)
            } ?: kotlin.run {
                ivUserPhoto.setImageResource(R.drawable.man_user_icon)
            }
            tvName.text = item.name
        }
    }
}