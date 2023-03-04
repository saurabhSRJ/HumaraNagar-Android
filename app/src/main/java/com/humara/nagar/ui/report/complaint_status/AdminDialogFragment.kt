package com.humara.nagar.ui.report.complaint_status

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.humara.nagar.databinding.FragmentAdminDialogBinding

class AdminDialogFragment : DialogFragment() {

    interface DialogListener {
        fun onDataEntered(data: String)
    }

    private var _binding: FragmentAdminDialogBinding? = null
    private val binding get() = _binding!!
    private var listener: DialogListener? = null

    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button
    private lateinit var inputField: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdminDialogBinding.inflate(layoutInflater, container, false)

        initView()

        return binding.root
    }

    private fun initView() {
        confirmButton = binding.confirmButton
        cancelButton = binding.cancelButton
        inputField = binding.editText
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setGravity(Gravity.CENTER)
            it.attributes?.width = ViewGroup.LayoutParams.MATCH_PARENT
            it.attributes?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        binding.confirmButton.setOnClickListener {
            listener?.onDataEntered(binding.editText.text.toString())
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    fun setCustomDialogListener(listener: DialogListener) {
        this.listener = listener
    }
}