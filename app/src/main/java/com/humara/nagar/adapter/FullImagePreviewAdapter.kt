package com.humara.nagar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.FullImagePreviewLayoutItemBinding
import com.humara.nagar.utils.loadUrl

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
        holder.binding.previewIV.loadUrl(image, R.drawable.ic_image_placeholder)
    }

    override fun getItemCount() = imagesList.size

    fun addImages(images: List<String>) {
        imagesList.clear()
        imagesList.addAll(images)
        notifyItemRangeInserted(0, images.size)
    }
}