package com.humara.nagar.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.databinding.ItemPollOptionBinding

class PollOptionsPreviewAdapter(val options: List<String>) : RecyclerView.Adapter<PollOptionsPreviewAdapter.OptionsViewHolder>() {
    inner class OptionsViewHolder(val binding: ItemPollOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.run {
                tvOption.text = item
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    clContainer.foreground = null
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