package com.humara.nagar.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.ComplaintsItemsLayoutBinding
import com.humara.nagar.ui.report.model.ComplaintsResponse
import com.humara.nagar.utils.ComplaintsUtils.StateColor
import com.humara.nagar.utils.ComplaintsUtils.StateName
import com.humara.nagar.utils.ComplaintsUtils.StateDrawable


class ComplaintsAdapter(
    private val isUserAdmin: Boolean,
    private val listener: (String) -> Unit
) : RecyclerView.Adapter<ComplaintsAdapter.ViewHolder>() {

    private val complaintsResponseList = mutableListOf<ComplaintsResponse>()

    fun addData(complaints: List<ComplaintsResponse>) {
        val currentSize = complaintsResponseList.size
        complaintsResponseList.addAll(complaints)
        notifyItemRangeInserted(currentSize, complaintsResponseList.size)
    }

    class ViewHolder(val binding: ComplaintsItemsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ComplaintsItemsLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentComplaint = complaintsResponseList[position]
        val state = currentComplaint.state
        val category = currentComplaint.category
        val resolvedOn = currentComplaint.resolvedOn
        val resolutionExpectedOn = currentComplaint.resolutionExpectedOn
        val locality = currentComplaint.locality
        val rating = currentComplaint.rating
        val complaintID = currentComplaint.complaintId

        holder.binding.apply {
            ratingBar.visibility = View.GONE
            ctaButton.visibility = View.VISIBLE

            stateBtn.apply {

                when (state) {
                    StateName.SENT.currentState -> {
                        text = StateName.getName(StateName.SENT, context)
                        val color = ResourcesCompat.getColor(resources, StateColor.SENT.color, null)
                        setTextColor(color)
                        backgroundTintList = ColorStateList.valueOf(color)
                    }
                    StateName.IN_PROGRESS.currentState -> {
                        text = StateName.getName(StateName.IN_PROGRESS, context)
                        val color =
                            ResourcesCompat.getColor(resources, StateColor.IN_PROGRESS.color, null)
                        setTextColor(color)
                        backgroundTintList = ColorStateList.valueOf(color)
                    }
                    StateName.RESOLVED.currentState -> {
                        text = StateName.getName(StateName.RESOLVED, context)
                        val color =
                            ResourcesCompat.getColor(resources, StateColor.RESOLVED.color, null)
                        setTextColor(color)
                        backgroundTintList = ColorStateList.valueOf(color)
                        ratingBar.apply {
                            visibility = View.VISIBLE
                            this.rating = rating!!.toInt().toFloat()
                        }
                        ctaButton.visibility = View.GONE
                    }
                    StateName.WITHDRAW.currentState -> {
                        text = StateName.getName(StateName.WITHDRAW, context)
                        val color =
                            ResourcesCompat.getColor(resources, StateColor.WITHDRAW.color, null)
                        setTextColor(color)
                        backgroundTintList = ColorStateList.valueOf(color)
                    }
                }
            }

            imageView.apply {
                when (category) {
                    StateDrawable.DRAINAGE_SYSTEM.categoryName -> {
                        setImageResource(StateDrawable.getDrawable(StateDrawable.DRAINAGE_SYSTEM))
                    }
                    StateDrawable.ROAD_MAINTENANCE.categoryName -> {
                        setImageResource(StateDrawable.getDrawable(StateDrawable.ROAD_MAINTENANCE))
                    }
                    StateDrawable.WATER_SUPPLY.categoryName -> {
                        setImageResource(StateDrawable.getDrawable(StateDrawable.WATER_SUPPLY))
                    }
                    StateDrawable.GARBAGE_COLLECTION.categoryName -> {
                        setImageResource(StateDrawable.getDrawable(StateDrawable.GARBAGE_COLLECTION))
                    }
                }
            }

            complaintIdTV.text = complaintID.toString()
            categoryTV.text = category.toString()
            localityTV.text = locality.toString()

            resolvedTV.apply {
                val resolvedText = StringBuilder()
                text = if (!resolvedOn.isNullOrEmpty()) {
                    resolvedText.append(resources.getString(R.string.resolvedOn))
                        .append(" $resolvedOn")
                    resolvedText.toString()
                } else {
                    resolvedText.append(resources.getString(R.string.resolutionExpectedBy))
                        .append(" $resolutionExpectedOn")
                    resolvedText.toString()
                }
            }

            ctaButton.apply {
                text = if (isUserAdmin) {
                    resources.getString(R.string.update)
                } else {
                    resources.getString(R.string.track)
                }
                setOnClickListener {
                    listener(complaintID.toString())
                }
            }
        }
    }

    override fun getItemCount() = complaintsResponseList.size

    fun clearAllData() {
        complaintsResponseList.clear()
        notifyDataSetChanged()
    }
}