package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.StepperItemBinding
import com.humara.nagar.ui.report.model.TrackingInfo
import com.humara.nagar.utils.StringUtils.setStringWithTypeface

class ComplaintStatusAdapter : RecyclerView.Adapter<ComplaintStatusAdapter.ViewHolder>() {
    private val stepperList = mutableListOf<TrackingInfo>()

    fun setData(steps: List<TrackingInfo>) {
        stepperList.clear()
        stepperList.addAll(steps)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(StepperItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = stepperList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stepperList[position])
    }

    inner class ViewHolder(val binding: StepperItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrackingInfo) {
            binding.run {
                textTV.text = item.stateText
                subTextTV.text = item.initialDate
                setUIBasedOnState(binding, item.isFinished)
                item.stateComment?.let {
                    commentTv.setStringWithTypeface(0, it.indexOf(":"), it, ResourcesCompat.getFont(root.context, R.font.open_sans_semibold))
                }
                updateDateTv.text = item.updateDate
                if (adapterPosition < stepperList.size - 1 && stepperList[adapterPosition + 1].isFinished)
                    barView.setImageResource(R.drawable.solid_complete_bar)
                else
                    barView.setImageResource(R.drawable.stepper_dotted_bar)
                //handle ui  for last item
                if (adapterPosition == stepperList.size -1) {
                    barView.visibility = View.GONE
                    updateDateTv.visibility = View.GONE
                } else {
                    barView.visibility = View.VISIBLE
                    updateDateTv.visibility = View.VISIBLE
                }
            }
        }

        private fun setUIBasedOnState(binding: StepperItemBinding, isFinished: Boolean) {
            binding.run {
                val color = ContextCompat.getColor(root.context, if (isFinished) R.color.stroke_green else R.color.grey_4F4F4F)
                val typeFace = ResourcesCompat.getFont(root.context, if (isFinished) R.font.open_sans_semibold else R.font.open_sans_regular)
                textTV.setTextColor(color)
                textTV.typeface = typeFace
                checkIV.setImageResource(if (isFinished) R.drawable.stepper_complete else R.drawable.stepper_incomplete)
            }
        }
    }
}