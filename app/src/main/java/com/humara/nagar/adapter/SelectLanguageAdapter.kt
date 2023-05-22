package com.humara.nagar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.SelectLanguageItemBinding
import com.humara.nagar.ui.settings.language.AppLanguage
import com.humara.nagar.utils.getAppSharedPreferences

class SelectLanguageAdapter(context: Context, private val list: Array<AppLanguage>, private val clickListener: (AppLanguage) -> Unit) : RecyclerView.Adapter<SelectLanguageAdapter
.SelectLanguageViewHolder>() {
    private var selectedPosition = -1

    init {
        selectedPosition = list.indexOfFirst { AppLanguage.getLanguageCode(it) == context.getAppSharedPreferences().appLanguage }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectLanguageViewHolder {
        return SelectLanguageViewHolder(SelectLanguageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SelectLanguageViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class SelectLanguageViewHolder(val binding: SelectLanguageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppLanguage) {
            binding.run {
                tvLanguage.text = item.lang
                if (selectedPosition == adapterPosition) {
                    ivCheck.setImageResource(R.drawable.ic_radio_checked)
                    clRoot.setBackgroundResource(R.drawable.rect_blue_fill_blue_outline_5dp)
                } else {
                    ivCheck.setImageResource(R.drawable.ic_radio_unchecked)
                    clRoot.setBackgroundResource(R.drawable.rect_white_fill_grey_outline_5dp)
                }
                root.setOnClickListener {
                    val prevPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(selectedPosition)
                    notifyItemChanged(prevPosition)
                    clickListener.invoke(item)
                }
            }
        }
    }
}