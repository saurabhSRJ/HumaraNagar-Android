package com.humara.nagar.ui.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.humara.nagar.R
import com.humara.nagar.databinding.DialogGenericStatusBinding
import com.humara.nagar.utils.Utils
import com.humara.nagar.utils.parcelable
import com.humara.nagar.utils.setNonDuplicateClickListener
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

class GenericStatusDialog private constructor() : DialogFragment() {
    private lateinit var binding: DialogGenericStatusBinding
    private lateinit var uiData: StatusData

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogGenericStatusBinding.inflate(inflater, container, false)
        arguments?.let {
            uiData = it.parcelable(DATA_KEY)!!
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.let {
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
        }
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.CENTER)
            setLayout((Utils.getScreenWidth() * 0.9).roundToInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        binding.run {
            tvTitle.text = uiData.title
            tvSubtitle.text = uiData.subtitle
            btnCta.text = uiData.ctaText ?: getString(R.string.ok)
            btnCta.setNonDuplicateClickListener {
                clickListener?.ctaClickListener()
                dismiss()
            }
            ivClose.setNonDuplicateClickListener {
                clickListener?.dismissClickListener()
                dismiss()
            }
            when (uiData.state) {
                State.SUCCESS -> {
                    ivStatus.setImageResource(uiData.icon ?: R.drawable.ic_success_static)
                    btnCta.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.stroke_green)
                }
                State.ERROR -> {
                    ivStatus.setImageResource(uiData.icon ?: R.drawable.ic_error_static)
                    btnCta.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.stroke_yellow)
                }
            }
        }
    }

    companion object {
        const val TAG = "GenericStatusDialog"
        private const val DATA_KEY = "DATA"
        private var clickListener: StatusDialogClickListener? = null

        fun show(fragmentManager: FragmentManager, data: StatusData, clickListener: StatusDialogClickListener?) {
            this.clickListener = clickListener
            GenericStatusDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA_KEY, data)
                }
            }.show(fragmentManager, TAG)
        }
    }

    enum class State {
        SUCCESS,
        ERROR
    }

    interface StatusDialogClickListener {
        fun ctaClickListener()

        fun dismissClickListener() {}
    }
}

@Parcelize
data class StatusData(
    val state: GenericStatusDialog.State,
    val title: String,
    val subtitle: String? = null,
    val ctaText: String? = null,
    @DrawableRes val icon: Int? = null
) : Parcelable