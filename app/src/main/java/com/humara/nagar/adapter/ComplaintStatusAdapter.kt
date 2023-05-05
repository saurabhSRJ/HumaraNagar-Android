package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.StepperItemBinding
import com.humara.nagar.ui.report.model.States

class ComplaintStatusAdapter : RecyclerView.Adapter<ComplaintStatusAdapter.ViewHolder>() {
    private val stepperList = mutableListOf<States>()

    fun setData(steps: List<States>) {
        stepperList.clear()
        stepperList.addAll(steps)
        notifyItemRangeInserted(0, stepperList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(StepperItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = stepperList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stepperList[position])
    }

    inner class ViewHolder(val binding: StepperItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: States) {
            binding.run {
                textTV.text = item.stateText
                subTextTV.text = item.stateSubtext
                setUIBasedOnState(binding, item.isFinished)
                //hide vertical line for last item
                barView.isVisible = adapterPosition < (stepperList.size - 1)
            }
        }

        private fun setUIBasedOnState(binding: StepperItemBinding, isFinished: Boolean) {
            binding.run {
                val color = ContextCompat.getColor(root.context, if (isFinished) R.color.stroke_green else R.color.grey_4F4F4F)
                val typeFace = ResourcesCompat.getFont(root.context, if (isFinished) R.font.open_sans_semibold else R.font.open_sans_regular)
                textTV.setTextColor(color)
                textTV.typeface = typeFace
                checkIV.setImageResource(if (isFinished) R.drawable.stepper_complete else R.drawable.stepper_incomplete)
                if (adapterPosition < stepperList.size - 1 && stepperList[adapterPosition + 1].isFinished)
                    barView.setBackgroundResource(R.drawable.solid_complete_bar)
                else
                    barView.setBackgroundResource(R.drawable.stepper_dotted_bar)
            }
        }
    }
}