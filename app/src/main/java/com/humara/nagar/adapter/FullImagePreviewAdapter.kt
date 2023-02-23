package com.humara.nagar.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humara.nagar.R
import com.humara.nagar.databinding.FullImagePreviewLayoutItemBinding
import com.humara.nagar.utils.Utils


class FullImagePreviewAdapter(
    val context: Context,
) : RecyclerView.Adapter<FullImagePreviewAdapter.ViewHolder>() {

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
            .load(Uri.parse(image))
            .into(holder.binding.previewIV)
        holder.binding.saveImageTV.setOnClickListener {
            Utils.saveMediaToStorage(
                (holder.binding.previewIV.drawable as BitmapDrawable).bitmap,
                context
            ).also { status ->
                if (status) {
                    Toast.makeText(context, context.resources.getString(R.string.saved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, context.resources.getString(R.string.failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount() = imagesList.size

    @SuppressLint("NotifyDataSetChanged")
    fun addImages(images: List<String>) {
        imagesList.addAll(images)
        notifyDataSetChanged()
    }
}