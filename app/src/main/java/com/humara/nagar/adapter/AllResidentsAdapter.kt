package com.humara.nagar.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.databinding.ResidentLayoutItemBinding
import com.humara.nagar.ui.residents.model.Residents
import com.humara.nagar.ui.signup.model.Gender
import com.humara.nagar.utils.IntentUtils
import com.humara.nagar.utils.StringUtils
import java.util.*

class AllResidentsAdapter(
    private val context: Context,
    private val listener: (Residents) -> Unit
) : RecyclerView.Adapter<AllResidentsAdapter.ViewHolder>() {

    private val residentsList = mutableListOf<Residents>()
    private val maxStringLength = 16
    private val modifiedStringLength = 13

    class ViewHolder(val binding: ResidentLayoutItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ResidentLayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentResident = residentsList[position]
        holder.binding.apply {
            nameDataTV.text = currentResident.name.toString()

            if (currentResident.spouseName?.isNotEmpty() == true) {
                var spouseName = currentResident.spouseName.toString()
                if (spouseName.length > maxStringLength) {
                    spouseName = StringUtils.showDotStringAfterLimitReached(modifiedStringLength, spouseName)
                }
                fatherNameDataTV.text = spouseName
                fathernameTV.text = context.getString(R.string.spouse_name)
            } else {

                var fatherName = currentResident.fathersName.toString()

                //Make the string end with ... if it exceeds a length of 16
                if (fatherName.length > maxStringLength) {
                    fatherName =
                        StringUtils.showDotStringAfterLimitReached(modifiedStringLength, fatherName)
                }
                fatherNameDataTV.text = fatherName
            }
            ageDataTV.text = currentResident.age.toString()
            houseNoDataTV.text = currentResident.houseNumber

            var locality = currentResident.locality.toString()
            if (locality.length > maxStringLength) {
                locality = StringUtils.showDotStringAfterLimitReached(modifiedStringLength, locality)
            }
            localityDataTV.text = locality
            votedIdDataTV.text = currentResident.voterId

            //Check if current resident's gender is Female or not
            if (currentResident.gender == Gender.FEMALE.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }) {
                genderIconIV.setImageResource(R.drawable.woman_user_icon)
            }
            rootLayout.setOnClickListener {
                listener(currentResident)
            }
            callComplaintInitiatorCard.setOnClickListener {
                currentResident.phoneNumber?.let { it1 -> context.startActivity(IntentUtils.getCallIntent(it1)) }
            }
        }
    }

    override fun getItemCount() = residentsList.size

    fun setData(list: List<Residents>) {
        residentsList.clear()
        residentsList.addAll(list)
        notifyDataSetChanged()
    }
}