package com.humara.nagar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.ResidentLayoutItemBinding
import com.humara.nagar.ui.residents.model.ResidentDetails
import com.humara.nagar.utils.*

class ResidentsAdapter(private val listener: (ResidentDetails) -> Unit) : RecyclerView.Adapter<ResidentsViewHolder>() {

    private val residentDetailsList = mutableListOf<ResidentDetails>()

    fun setData(data: List<ResidentDetails>) {
        residentDetailsList.clear()
        residentDetailsList.addAll(data)
        notifyDataSetChanged()
    }

    fun addMoreData(moreData: List<ResidentDetails>) {
        val prevListEnd = residentDetailsList.size
        residentDetailsList.addAll(moreData)
        notifyItemRangeInserted(prevListEnd, moreData.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidentsViewHolder {
        return ResidentsViewHolder(ResidentLayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener)
    }

    override fun onBindViewHolder(holder: ResidentsViewHolder, position: Int) {
        holder.bind(residentDetailsList[position])
    }

    override fun getItemCount() = residentDetailsList.size
}

class ResidentsViewHolder(
    val binding: ResidentLayoutItemBinding,
    val listener: (ResidentDetails) -> Unit,
    private val isSearchResult: Boolean = false,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(resident: ResidentDetails, searchText: String = "") {
        binding.run {
            resident.name?.let { name ->
                if (isSearchResult) {
                    val matchIndex = name.indexOf(searchText, 0, true)
                    if (matchIndex != -1) {
                        tvName.text = StringUtils.getStringWithBackgroundColor(
                            name,
                            ContextCompat.getColor(root.context, R.color.light_blue_CFE6FF),
                            matchIndex,
                            name.length.coerceAtMost(matchIndex + searchText.length)
                        )
                    } else {
                        tvName.text = name
                    }
                } else {
                    tvName.text = name
                }
            }
            resident.fathersName?.let { name ->
                tvFatherNameData.text = name
            }
            resident.dateOfBirth?.let {
                val age = DateTimeUtils.getAgeInYearsFromIsoDate(it)
                tvAgeData.text = root.context.getString(R.string.n_years, age)
            }
            resident.image?.let { url ->
                ivUserPhoto.loadUrl(url, R.drawable.man_user_icon)
            }
            tvRoleAndWard.text = resident.role.plus(": ").plus(root.context.getString(R.string.ward_s, resident.ward))
            rootLayout.setNonDuplicateClickListener {
                listener(resident)
            }
            cvCall.setNonDuplicateClickListener {
                resident.phoneNumber?.let { number ->
                    root.context.startActivity(IntentUtils.getCallIntent(number))
                } ?: run {
                    root.context.showToast("Mobile number not available")
                }
            }
        }
    }
}