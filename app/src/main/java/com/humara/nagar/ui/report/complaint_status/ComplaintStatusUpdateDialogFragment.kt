package com.humara.nagar.ui.report.complaint_status

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.humara.nagar.databinding.FragmentComplaintStatusUpdateBinding

class ComplaintStatusUpdateDialogFragment : DialogFragment() {
    private var _binding: FragmentComplaintStatusUpdateBinding? = null
    private val binding get() = _binding!!

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
        binding.confirmButton.setOnClickListener {
            setFragmentResult(ComplaintStatusFragment.COMMENT_RESULT_REQUEST, bundleOf(ComplaintStatusFragment.COMMENT_KEY to binding.editText.text.toString().trim()))
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }
}