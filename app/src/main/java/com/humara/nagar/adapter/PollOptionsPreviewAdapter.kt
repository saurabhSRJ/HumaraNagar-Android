package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.ItemPollOptionBinding

class PollOptionsPreviewAdapter(val options: List<String>) : RecyclerView.Adapter<PollOptionsPreviewAdapter.OptionsViewHolder>() {
    inner class OptionsViewHolder(val binding: ItemPollOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.run {
                tvOption.text = item
                clContainer.foreground = null
                clContainer.background = ContextCompat.getDrawable(root.context, R.drawable.rect_white_fill_grey_outline_5dp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionsViewHolder {
        return OptionsViewHolder(ItemPollOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: OptionsViewHolder, position: Int) {
        holder.bind(options[position])
    }
}