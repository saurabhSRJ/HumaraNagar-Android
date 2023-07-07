package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
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

class ComplaintsAdapter(private val isUserAdmin: Boolean, val listener: (String) -> Unit) : RecyclerView.Adapter<ComplaintViewHolder>() {
    private val complaints = mutableListOf<ComplaintDetails>()

    fun setData(data: List<ComplaintDetails>) {
        complaints.clear()
        complaints.addAll(data)
        notifyDataSetChanged()
    }

    fun addMoreData(moreData: List<ComplaintDetails>) {
        val prevListEnd = complaints.size
        complaints.addAll(moreData)
        notifyItemRangeInserted(prevListEnd, moreData.size)
    }

    fun updateComplaint(complaint: ComplaintDetails) {
        val position = complaints.indexOfFirst { it.complaintId == complaint.complaintId }
        complaints[position] = complaint
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        return ComplaintViewHolder(ComplaintsItemsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener, isUserAdmin)
    }

    override fun getItemCount() = complaints.size

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        holder.bind(complaints[position])
    }
}

class ComplaintViewHolder(private val binding: ComplaintsItemsLayoutBinding, val listener: (String) -> Unit, private val isUserAdmin: Boolean) : RecyclerView.ViewHolder(binding.root) {
    fun bind(complaint: ComplaintDetails) {
        binding.run {
            ctaButton.text = ComplaintState.getCtaText(complaint.state, root.context, isUserAdmin, complaint.isRatingPresent())
            setComplaintStateUI(stateBtn, complaint.state)
            complaint.getImageList().let { list ->
                if (list.isNotEmpty()) {
                    imageView.loadUrl(list[0], R.drawable.ic_image_placeholder)
                } else {
                    imageView.setImageResource(R.drawable.ic_image_placeholder)
                }
            }
            categoryTV.setVisibilityAndText(complaint.category)
            tvWard.text = root.resources.getString(R.string.ward_s, complaint.ward)
            tvResidentName.setVisibilityAndText(complaint.residentName)
            resolvedTV.apply {
                if (!complaint.resolvedOn.isNullOrEmpty()) {
                    val resolvedOnDate = DateTimeUtils.convertIsoDateTimeFormat(complaint.resolvedOn, "dd MMMM, yyyy")
                    text = resources.getString(R.string.resolvedOn, resolvedOnDate)
                } else if (!complaint.resolutionExpectedOn.isNullOrEmpty()) {
                    val resolutionExpectedData = DateTimeUtils.convertIsoDateTimeFormat(complaint.resolutionExpectedOn, "dd MMMM, yyyy")
                    text = resources.getString(R.string.resolutionExpectedBy, resolutionExpectedData)
                }
            }
            if (complaint.isRatingPresent()) {
                ratingBar.visibility = View.VISIBLE
                ratingBar.rating = complaint.rating!!.toFloat()
            } else {
                ratingBar.visibility = View.GONE
            }
            ctaButton.setNonDuplicateClickListener {
                listener(complaint.complaintId)
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