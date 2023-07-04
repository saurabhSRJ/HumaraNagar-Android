package com.humara.nagar.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.humara.nagar.databinding.LayoutGenericAlertDialogBinding
import com.humara.nagar.utils.Utils
import kotlin.math.roundToInt

class GenericAlertDialog : DialogFragment() {
    private lateinit var binding: LayoutGenericAlertDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutGenericAlertDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isCancelable = arguments?.getBoolean(IS_CANCELABLE, false) ?: false
        dialog?.let {
            it.setCancelable(isCancelable)
            it.setCanceledOnTouchOutside(isCancelable)
        }
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
            setLayout((Utils.getScreenWidth() * 0.9).roundToInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        initView()
    }

    private fun initView() {
        binding.run {
            tvTitle.text = arguments?.getString(TITLE)
            tvMessage.text = arguments?.getString(MESSAGE)
            positiveButton.text = arguments?.getString(POSITIVE_CTA_TEXT)
            arguments?.getString(NEGATIVE_CTA_TEXT)?.let {
                negativeButton.text = it
            } ?: kotlin.run {
                negativeButton.visibility = View.GONE
            }
            negativeButton.setOnClickListener { dismiss() }
            positiveButton.setOnClickListener {
                clickListener.invoke()
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "GenericAlertDialog"
        private const val TITLE = "TITLE"
        private const val MESSAGE = "MESSAGE"
        private const val IS_CANCELABLE = "IS_CANCELABLE"
        private const val POSITIVE_CTA_TEXT = "POSITIVE_CTA_TEXT"
        private const val NEGATIVE_CTA_TEXT = "NEGATIVE_CTA_TEXT"
        private var clickListener: () -> Unit = {}

        fun show(fragmentManager: FragmentManager, title: String, message: String, isCancelable: Boolean, positiveButtonText: String, negativeButtonText: String? = null,
            positiveButtonListener: () -> Unit = {}) {
            this.clickListener = positiveButtonListener
            GenericAlertDialog().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(MESSAGE, message)
                    putBoolean(IS_CANCELABLE, isCancelable)
                    putString(POSITIVE_CTA_TEXT, positiveButtonText)
                    putString(NEGATIVE_CTA_TEXT, negativeButtonText)
                }
            }.show(fragmentManager, TAG)
        }
    }
}