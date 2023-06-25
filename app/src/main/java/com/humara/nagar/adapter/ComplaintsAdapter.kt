package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.humara.nagar.R
import com.humara.nagar.databinding.ComplaintsItemsLayoutBinding
import com.humara.nagar.ui.report.model.ComplaintDetails
import com.humara.nagar.utils.ComplaintsUtils.ComplaintState
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.loadUrl
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.setVisibilityAndText

class ComplaintsAdapter(val isUserAdmin: Boolean, val listener: (String) -> Unit) : ListAdapter<ComplaintDetails, ComplaintsAdapter.ComplaintViewHolder>(ComplaintsDiffUtil) {
    object ComplaintsDiffUtil : DiffUtil.ItemCallback<ComplaintDetails>() {
        override fun areItemsTheSame(oldItem: ComplaintDetails, newItem: ComplaintDetails): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ComplaintDetails, newItem: ComplaintDetails): Boolean {
            return oldItem.complaintId == newItem.complaintId
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        return ComplaintViewHolder(ComplaintsItemsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = getItem(position)
        holder.bind(complaint)
    }

    inner class ComplaintViewHolder(private val binding: ComplaintsItemsLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var complaint: ComplaintDetails

        init {
            binding.ctaButton.setNonDuplicateClickListener {
                listener(complaint.complaintId)
            }
        }

        fun bind(item: ComplaintDetails) {
            complaint = item
            binding.run {
                ctaButton.text = ComplaintState.getCtaText(item.state, root.context, isUserAdmin, item.isRatingPresent())
                setComplaintStateUI(stateBtn, item.state)
                item.getImageList().let { list ->
                    if (list.isNotEmpty()) {
                        imageView.loadUrl(list[0], R.drawable.ic_image_placeholder)
                    } else {
                        imageView.setImageResource(R.drawable.ic_image_placeholder)
                    }
                }
                categoryTV.setVisibilityAndText(complaint.category)
                localityTV.setVisibilityAndText(complaint.locality)
                tvResidentName.setVisibilityAndText(complaint.residentName)
                resolvedTV.apply {
                    text = if (!complaint.resolvedOn.isNullOrEmpty()) {
                        val resolvedOnDate = DateTimeUtils.convertIsoDateTimeFormat(complaint.resolvedOn!!, "dd MMMM, yyyy")
                        resources.getString(R.string.resolvedOn, resolvedOnDate)
                    } else {
                        resources.getString(R.string.resolutionExpectedBy, complaint.resolutionExpectedOn)
                    }
                }
            }
        }

        private fun setComplaintStateUI(btn: Button, state: String) {
            btn.apply {
                text = ComplaintState.getName(state, context)
                val stateColor = ComplaintState.getStateColor(state)
                setTextColor(ContextCompat.getColor(context, stateColor))
                (btn as? MaterialButton)?.setStrokeColorResource(stateColor)
            }
        }
    }
}