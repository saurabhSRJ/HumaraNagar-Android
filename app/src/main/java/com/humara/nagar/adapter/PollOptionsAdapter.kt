package com.humara.nagar.adapter

import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.ItemPollOptionBinding
import com.humara.nagar.ui.home.model.PollOption
import com.humara.nagar.ui.home.model.PostInfo
import com.humara.nagar.utils.StringUtils.setEndDrawable
import com.humara.nagar.utils.setNonDuplicateClickListener

class PollOptionsAdapter(val listener: (Int) -> Unit) : RecyclerView.Adapter<PollOptionsAdapter.PollOptionsViewHolder>() {
    private val optionList = mutableListOf<PollOption>()
    private lateinit var postInfo: PostInfo

    fun setData(info: PostInfo) {
        postInfo = info
        optionList.clear()
        optionList.addAll(info.options)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollOptionsViewHolder {
        return PollOptionsViewHolder(ItemPollOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = optionList.size

    override fun onBindViewHolder(holder: PollOptionsViewHolder, position: Int) {
        holder.bind(optionList[position])
    }

    inner class PollOptionsViewHolder(val binding: ItemPollOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PollOption) {
            binding.run {
                val layerDrawable = clContainer.background as LayerDrawable
                val drawable = layerDrawable.findDrawableByLayerId(R.id.indicator) as ClipDrawable
                if (postInfo.isAllowedToVote()) {
                    ivOption.visibility = View.VISIBLE
                    clContainer.isEnabled = true
                    clContainer.setNonDuplicateClickListener {
                        clContainer.isEnabled = false
                        listener(item.optionId)
                    }
                    drawable.level = 0
                } else {
                    clContainer.isEnabled = false
                    ivOption.visibility = View.GONE
                    val result: Int = if (postInfo.totalVotes == 0) {
                        0
                    } else {
                        (item.votes * 100 / postInfo.totalVotes)
                    }
                    tvPercentage.text = result.toString().plus("%")
                    drawable.level = result * 100
                }
                if (item.optionId == postInfo.userVote) {
                    tvOption.setEndDrawable(item.option, R.drawable.ic_poll_result_check)
                } else {
                    tvOption.text = item.option
                }
            }
        }
    }
}