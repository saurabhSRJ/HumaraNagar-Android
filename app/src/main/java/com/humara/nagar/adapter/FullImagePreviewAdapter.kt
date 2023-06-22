package com.humara.nagar.adapter

import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.utils.loadUrlWithoutCrop
import com.ortiz.touchview.TouchImageView

class FullImagePreviewAdapter(val context: Context) : RecyclerView.Adapter<FullImagePreviewAdapter.ViewHolder>() {
    private val imagesList = mutableListOf<String>()

    class ViewHolder(val binding: TouchImageView) : RecyclerView.ViewHolder(binding) {
        val imagePlace = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TouchImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            setOnTouchListener { view, event ->
                var result = true
                //can scroll horizontally checks if there's still a part of the image
                //that can be scrolled until you reach the edge
                if (event.pointerCount >= 2 || view.canScrollHorizontally(1) && canScrollHorizontally(-1)) {
                    //multi-touch event
                    result = when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            // Disallow RecyclerView to intercept touch events.
                            parent.requestDisallowInterceptTouchEvent(true)
                            // Disable touch on view
                            false
                        }
                        MotionEvent.ACTION_UP -> {
                            // Allow RecyclerView to intercept touch events.
                            parent.requestDisallowInterceptTouchEvent(false)
                            true
                        }
                        else -> true
                    }
                }
                result
            }
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = imagesList[position]
        holder.imagePlace.loadUrlWithoutCrop(image, R.drawable.ic_image_placeholder)
    }

    override fun getItemCount() = imagesList.size

    fun addImages(images: List<String>) {
        imagesList.clear()
        imagesList.addAll(images)
        notifyDataSetChanged()
    }
}