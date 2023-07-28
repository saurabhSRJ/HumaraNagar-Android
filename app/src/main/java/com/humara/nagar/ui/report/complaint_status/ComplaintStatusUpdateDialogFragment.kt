package com.humara.nagar.ui.report.complaint_status

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.humara.nagar.R
import com.humara.nagar.Role
import com.humara.nagar.databinding.FragmentComplaintStatusUpdateBinding
import com.humara.nagar.ui.report.model.UpdateComplaintRequest
import com.humara.nagar.utils.ComplaintsUtils
import com.humara.nagar.utils.DateTimeUtils
import com.humara.nagar.utils.StringUtils
import com.humara.nagar.utils.getUserSharedPreferences

class ComplaintStatusUpdateDialogFragment : DialogFragment() {
    private var _binding: FragmentComplaintStatusUpdateBinding? = null
    private val binding get() = _binding!!
    private val args: ComplaintStatusUpdateDialogFragmentArgs by navArgs()
    private val complaintResolutionDays = arrayListOf(10, 7, 3)
    private var resolutionDay: Int = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComplaintStatusUpdateBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
        }
        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setGravity(Gravity.CENTER)
            it.attributes?.width = ViewGroup.LayoutParams.MATCH_PARENT
            it.attributes?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val complaintResolutionOptions = arrayListOf<String>()
        for (item in complaintResolutionDays) {
            complaintResolutionOptions.add(resources.getQuantityString(R.plurals.n_days, item, item))
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, complaintResolutionOptions)
        binding.run {
            groupHeader.isVisible = Role.isLocalAdmin(requireContext().getUserSharedPreferences().role?.id ?: 0) &&
                    args.state == ComplaintsUtils.ComplaintState.SENT.currentState
            textView.text = if (Role.isResident(requireContext().getUserSharedPreferences().role?.id ?: 0) && args.state == ComplaintsUtils.ComplaintState.RESOLVED.currentState) {
                getString(R.string.is_your_complaint_resolved)
            } else {
                getString(R.string.provide_more_details)
            }
            resolutionDaysSpinner.apply {
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    resolutionDay = complaintResolutionDays[position]
                }
                setText(adapter.getItem(2).toString(), false)
            }
            confirmButton.setOnClickListener {
                val result = UpdateComplaintRequest(
                    StringUtils.replaceWhitespaces(binding.editText.text.toString().trim()),
                    DateTimeUtils.getFutureDateTimeInIsoFormat(resolutionDay)
                )
                setFragmentResult(ComplaintStatusFragment.COMMENT_RESULT_REQUEST, bundleOf(ComplaintStatusFragment.COMMENT_KEY to result))
                dismiss()
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
    }
}