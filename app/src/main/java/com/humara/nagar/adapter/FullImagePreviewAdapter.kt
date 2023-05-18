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
import com.humara.nagar.databinding.FullImagePreviewLayoutItemBinding
import com.humara.nagar.utils.GlideUtil

class FullImagePreviewAdapter(val context: Context) : RecyclerView.Adapter<FullImagePreviewAdapter.ViewHolder>() {
    private val imagesList = mutableListOf<String>()

    class ViewHolder(val binding: FullImagePreviewLayoutItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FullImagePreviewLayoutItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = imagesList[position]
        Glide.with(context)
            .load(GlideUtil.getUrlWithHeaders(image, context))
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade(1000))
            .centerCrop()
            .into(holder.binding.previewIV)
    }

    override fun getItemCount() = imagesList.size

    fun addImages(images: List<String>) {
        imagesList.clear()
        imagesList.addAll(images)
        notifyItemRangeInserted(0, images.size)
    }
}