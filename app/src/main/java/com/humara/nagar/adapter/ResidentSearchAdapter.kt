package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.ItemEmptySearchResultBinding
import com.humara.nagar.databinding.ResidentLayoutItemBinding
import com.humara.nagar.ui.residents.model.ResidentDetails
import com.humara.nagar.utils.StringUtils.setStringWithTypeface

class ResidentSearchAdapter(private val listener: (ResidentDetails) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var residentList: MutableList<Any?> = mutableListOf()
    private var searchText: String = ""

    companion object {
        private const val HEADER = 1
        private const val RESIDENT = 2
    }

    fun setData(residentsToShow: List<Any?>, textToSearch: String) {
        residentList.clear()
        residentList.addAll(residentsToShow)
        searchText = textToSearch
        notifyDataSetChanged()
    }

    fun clearData() {
        residentList.clear()
        searchText = ""
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (residentList[position]) {
        is ResidentDetails -> RESIDENT
        else -> HEADER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            RESIDENT -> ResidentsViewHolder(ResidentLayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener, isSearchResult = true)
            else -> EmptySearchViewHolder(ItemEmptySearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount() = residentList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            RESIDENT -> (holder as ResidentsViewHolder).bind(residentList[position] as ResidentDetails, searchText)
            else -> (holder as EmptySearchViewHolder).bind(residentList[position] as String)
        }
    }

    inner class EmptySearchViewHolder(val binding: ItemEmptySearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.run {
                val desc = String.format(root.context.getString(R.string.no_results_found), searchText)
                val startIndex = desc.indexOf(searchText)
                binding.tvNoResultFound.setStringWithTypeface(startIndex, startIndex + searchText.length, desc, ResourcesCompat.getFont(root.context, R.font.open_sans_semibold))
            }
        }
    }
}