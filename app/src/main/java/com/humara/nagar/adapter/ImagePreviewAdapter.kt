package com.humara.nagar.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.databinding.ImagePreviewRecyclerviewItemBinding

/**
 * Created by binay on 09,February,2023
 */
class ImagePreviewAdapter(
    private val listener: (Int) -> Unit
) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

    private val images = mutableListOf<Uri>()

    fun setData(imageList: List<Uri>) {
        images.clear()
        images.addAll(imageList)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ImagePreviewRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(binding.root)

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
        holder.binding.delete.setOnClickListener {
            if (images.size > position) {
                listener.invoke(position)
            }
        }
    }

    override fun getItemCount() = images.size
}