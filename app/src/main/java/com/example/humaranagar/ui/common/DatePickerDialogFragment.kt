package com.example.humaranagar.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.humaranagar.base.ViewModelFactory
import com.example.humaranagar.databinding.DialogDatePickerBinding
import com.example.humaranagar.network.BaseRepository
import com.example.humaranagar.ui.signup.profile_creation.ProfileCreationViewModel

class DatePickerDialogFragment : DialogFragment() {
    private lateinit var binding: DialogDatePickerBinding
    private val profileCreationViewModel by activityViewModels<ProfileCreationViewModel> {
        ViewModelFactory(BaseRepository())
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
            tvOk.setOnClickListener {
                val day = datePickerAction.dayOfMonth
                val month = datePickerAction.month + 1
                val year = datePickerAction.year
                profileCreationViewModel.setDateOfBirth("$day-$month-$year")
                dismiss()
            }
        }
    }
}