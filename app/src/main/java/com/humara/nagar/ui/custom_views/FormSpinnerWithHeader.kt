package com.humara.nagar.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.humara.nagar.R
import com.humara.nagar.databinding.ItemFormSpinnerWithHeaderBinding


class FormSpinnerWithHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var binding: ItemFormSpinnerWithHeaderBinding
    private var isRequired: Boolean = true

    init {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        binding = ItemFormSpinnerWithHeaderBinding.inflate(LayoutInflater.from(context), this, true)
        context.obtainStyledAttributes(attrs, R.styleable.FormSpinnerWithHeader).apply {
            try {
                val header = getString(R.styleable.FormSpinnerWithHeader_header)
                val inputEnabled = getBoolean(R.styleable.FormSpinnerWithHeader_inputEnabled, true)
                val hint = getString(R.styleable.FormSpinnerWithHeader_hint)
                val input = getString(R.styleable.FormSpinnerWithHeader_input)
                val isRequired = getBoolean(R.styleable.FormSpinnerWithHeader_required_input, true)
                setHeader(header)
                setInputEnabled(inputEnabled)
                setHint(hint)
                setInput(input)
                setRequiredInput(isRequired)
                if (isRequired) {
                    binding.spinnerTV.doAfterTextChanged {
                        val text = it.toString().trim()
                        setRequiredInput(text.isEmpty())
                    }
                }
            } finally {
                recycle()
            }
        }
    }

    fun setHeader(header: String?) {
        binding.tvHeader.text = header
    }

    fun setInputEnabled(isEnabled: Boolean) {
        binding.run {
            spinnerTV.isEnabled = isEnabled
            spinnerTV.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isEnabled) R.color.dark_grey_333333 else R.color.grey_828282
                )
            )
        }
    }

    fun setHint(hint: String?) {
        binding.spinnerTV.hint = hint
    }

    fun setInput(text: String?) {
        binding.spinnerTV.setText(text)
    }

    fun setOptions(optionsArrayId: Int) {
        val options = context.resources.getStringArray(optionsArrayId)
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, options)
        binding.spinnerTV.setAdapter(adapter)
    }

    fun setRequiredInput(isRequired: Boolean) {
        this.isRequired = isRequired
        binding.tvRequiredAsterisk.isVisible = isRequired
    }

    fun setUserInputListener(listener: ((input: String) -> Unit)? = null) {
        binding.spinnerTV.doAfterTextChanged {
            val input = it.toString().trim()
            listener?.invoke(input)
        }
    }
}