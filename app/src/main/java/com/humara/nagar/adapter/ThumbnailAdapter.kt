package com.humara.nagar.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humara.nagar.R
import com.humara.nagar.databinding.ThumbnailLayoutItemBinding


class ThumbnailAdapter(
    val context: Context,
    val onThumbnailClick: (String) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {

    private val imageList = mutableListOf<String>()

    private var selectedPosition: Int = 0

    fun updateSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    fun addImages(images: List<String>) {
        imageList.addAll(images)
        notifyDataSetChanged()
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
                .load(Uri.parse(currentImageUrl))
                .placeholder(R.drawable.default_drawable)
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