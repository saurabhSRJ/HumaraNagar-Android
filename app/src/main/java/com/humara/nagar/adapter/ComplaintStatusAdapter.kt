package com.humara.nagar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.StepperItemBinding
import com.humara.nagar.ui.report.model.States


class ComplaintStatusAdapter(
    private val context: Context
) : RecyclerView.Adapter<ComplaintStatusAdapter.ViewHolder>() {

    private val stepperList = mutableListOf<States>()

    fun addData(steps: List<States>) {
        val currentSize = stepperList.size
        stepperList.addAll(steps)
        notifyItemRangeInserted(currentSize, stepperList.size)
    }

    class ViewHolder(val binding: StepperItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            StepperItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentStep = stepperList[position]
        val stateText = currentStep.state_text
        val stateSubText = currentStep.state_subtext
        val stateIsFinished = currentStep.Is_finished

        holder.binding.apply {
            textTV.text = stateText
            subTextTV.text = stateSubText

            if (position == stepperList.size - 1)
                barView.visibility = View.GONE

            if (stateIsFinished == true) {
                val color = ResourcesCompat.getColor(context.resources, R.color.stroke_green, null)
                checkIV.setImageResource(R.drawable.stepper_complete)
                textTV.setTextColor(color)
                if (position < stepperList.size - 1 && stepperList[position + 1].Is_finished == true)
                    barView.setBackgroundResource(R.drawable.solid_complete_bar)
            }
        }
    }

    override fun getItemCount() = stepperList.size
}