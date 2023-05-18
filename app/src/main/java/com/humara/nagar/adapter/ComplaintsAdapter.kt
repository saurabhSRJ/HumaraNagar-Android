package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.button.MaterialButton
import com.humara.nagar.R
import com.humara.nagar.databinding.ComplaintsItemsLayoutBinding
import com.humara.nagar.ui.report.model.ComplaintDetails
import com.humara.nagar.utils.ComplaintsUtils.ComplaintState
import com.humara.nagar.utils.GlideUtil
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
                        Glide.with(root.context)
                            .load(GlideUtil.getUrlWithHeaders(list[0], root.context))
                            .placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                            .transform(CenterCrop(), RoundedCorners(12))
                            .transition(DrawableTransitionOptions.withCrossFade(1000))
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.ic_image_placeholder)
                    }
                }
                categoryTV.setVisibilityAndText(complaint.category)
                localityTV.setVisibilityAndText(complaint.locality)
                resolvedTV.apply {
                    val resolvedText = StringBuilder()
                    text = if (!complaint.resolvedOn.isNullOrEmpty()) {
                        resolvedText.append(resources.getString(R.string.resolvedOn)).append(" ${complaint.resolvedOn}").toString()
                    } else {
                        resolvedText.append(resources.getString(R.string.resolutionExpectedBy)).append(" ${complaint.resolutionExpectedOn}").toString()
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