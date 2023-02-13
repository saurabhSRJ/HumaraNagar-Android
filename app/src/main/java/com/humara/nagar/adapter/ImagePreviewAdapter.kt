package com.humara.nagar.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.Logger
import com.humara.nagar.databinding.ImagePreviewRecyclerviewItemBinding
import com.humara.nagar.utils.Utils

/**
 * Created by binay on 09,February,2023
 */
class ImagePreviewAdapter(
    private val listener: (Int) -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

    private val images = mutableListOf<Uri>()

    fun addData(imageList: List<Uri>) {
        val currentSize = images.size
        images.addAll(imageList)
        notifyItemRangeInserted(currentSize, imageList.size)
    }

    fun setData(imageList: MutableList<Uri>) {
        images.clear()
        images.addAll(imageList)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ImagePreviewRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ImagePreviewRecyclerviewItemBinding
                .inflate(
                    LayoutInflater
                        .from(parent.context), parent, false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = images[position]
        holder.binding.imagePreview.setImageURI(imageUri)
        holder.binding.imageTitle.text =
            Utils.formatImageString(imageUri.lastPathSegment.toString())
        holder.binding.delete.setOnClickListener {
            Logger.debugLog("Position: $position")
            if (images.size > position) {
                listener(position)
            }
        }
    }

    override fun getItemCount() = images.size
}