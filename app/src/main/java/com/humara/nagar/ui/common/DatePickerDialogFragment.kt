package com.humara.nagar.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.constants.Constants
import com.humara.nagar.databinding.DialogDatePickerBinding
import com.humara.nagar.ui.signup.profile_creation.ProfileCreationViewModel
import com.humara.nagar.utils.DateTimeUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatePickerDialogFragment : DialogFragment() {
    private lateinit var binding: DialogDatePickerBinding
    private val profileCreationViewModel by activityViewModels<ProfileCreationViewModel> {
        ViewModelFactory()
    }

    companion object {
        const val TAG = "DatePickerFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDatePickerBinding.inflate(layoutInflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setGravity(Gravity.CENTER)
            it.attributes?.width = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    private fun initView() {
        binding.run {
            val startingDate = DateTimeUtils.getEarlierDate(Constants.MIN_AGE_REQUIREMENT)
            datePickerAction.init(startingDate.first, startingDate.second - 1, startingDate.third, null)
            tvOk.setOnClickListener {
                val day = datePickerAction.dayOfMonth
                val month = datePickerAction.month + 1
                val year = datePickerAction.year
                val dob = LocalDate.of(year, month, day).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                profileCreationViewModel.setDateOfBirth(dob)
                dismiss()
            }
        }
    }
}