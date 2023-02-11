package com.humara.nagar.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.utils.Utils

/**
 * Created by binay on 09,February,2023
 */
class ImagePreviewAdapter(
    private val images: MutableList<Uri>
) : RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_preview)
        val textView: TextView = itemView.findViewById(R.id.image_title)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_preview_recyclerview_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUri = images[position]
        holder.imageView.setImageURI(imageUri)
        holder.textView.text = Utils.formatImageString(imageUri.lastPathSegment.toString())
        holder.deleteButton.setOnClickListener {
            Logger.debugLog("Position: $position")
            if (images.size > position) {
                images.removeAt(position)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

}