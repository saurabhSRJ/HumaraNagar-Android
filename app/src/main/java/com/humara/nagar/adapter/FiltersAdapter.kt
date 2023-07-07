package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.LayoutFilterItemBinding
import com.humara.nagar.ui.signup.model.FeedFilter
import com.humara.nagar.utils.setNonDuplicateClickListener

class FeedFiltersAdapter(val onFilterSelection: (FeedFilter, Int) -> Unit) : RecyclerView.Adapter<FeedFiltersAdapter.FeedFiltersViewHolder>() {
    private val filters = arrayListOf<FeedFilter>()
    private var selectedFilterPosition = 0

    fun setData(data: List<FeedFilter>) {
        filters.clear()
        filters.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedFiltersViewHolder {
        return FeedFiltersViewHolder(LayoutFilterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = filters.size

    override fun onBindViewHolder(holder: FeedFiltersViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    inner class FeedFiltersViewHolder(val binding: LayoutFilterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(filter: FeedFilter) {
            binding.run {
                if (bindingAdapterPosition == selectedFilterPosition) {
                    clContainer.background = ContextCompat.getDrawable(root.context, R.drawable.feed_filter_active_bg)
                    tvTitle.setTextColor(ContextCompat.getColor(root.context, R.color.primary_color))
                } else {
                    clContainer.background = ContextCompat.getDrawable(root.context, R.drawable.feed_filter_inactive_bg)
                    tvTitle.setTextColor(ContextCompat.getColor(root.context, R.color.grey_696969))
                }
                tvTitle.text = filter.name
                clContainer.setNonDuplicateClickListener {
                    if (selectedFilterPosition == bindingAdapterPosition) {
                        onFilterSelection(filter, bindingAdapterPosition)
                    } else {
                        val previousFilterPosition = selectedFilterPosition
                        selectedFilterPosition = bindingAdapterPosition
                        notifyItemChanged(previousFilterPosition)
                        notifyItemChanged(selectedFilterPosition)
                        onFilterSelection(filter, bindingAdapterPosition)
                    }
                }
            }
        }
    }
}