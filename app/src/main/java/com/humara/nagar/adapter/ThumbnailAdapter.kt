package com.humara.nagar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.humara.nagar.R
import com.humara.nagar.databinding.ThumbnailLayoutItemBinding
import com.humara.nagar.utils.GlideUtil

class ThumbnailAdapter(val context: Context, val onThumbnailClick: (String) -> Unit) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
    private val imageList = mutableListOf<String>()
    private var selectedPosition: Int = 0

    fun updateSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    fun addImages(images: List<String>) {
        imageList.clear()
        imageList.addAll(images)
        notifyItemRangeInserted(0, images.size)
    }

    inner class ViewHolder(val binding: ThumbnailLayoutItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ThumbnailLayoutItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentImageUrl = imageList[position]
        holder.binding.apply {
            Glide.with(context)
                .load(GlideUtil.getUrlWithHeaders(currentImageUrl, context))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .transform(CenterCrop(), RoundedCorners(12))
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(thumbnailIV)
            thumbnailRL.setOnClickListener {
                onThumbnailClick(currentImageUrl)
            }
            if (selectedPosition == position) {
                thumbnailRL.setBackgroundResource(R.drawable.selected_drawable)
            } else {
                thumbnailRL.setBackgroundResource(R.drawable.default_drawable)
            }
        }
    }

    override fun getItemCount() = imageList.size
}