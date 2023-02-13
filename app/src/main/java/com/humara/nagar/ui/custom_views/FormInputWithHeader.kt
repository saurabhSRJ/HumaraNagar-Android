package com.humara.nagar.ui.custom_views

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.humara.nagar.R
import com.humara.nagar.databinding.ItemFormInputWithHeaderBinding
import com.humara.nagar.utils.setNonDuplicateClickListener

class FormInputWithHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var binding: ItemFormInputWithHeaderBinding
    private var isRequired: Boolean = true

    init {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        binding = ItemFormInputWithHeaderBinding.inflate(LayoutInflater.from(context), this, true)
        context.obtainStyledAttributes(attrs, R.styleable.FormInputWithHeader).apply {
            try {
                val header = getString(R.styleable.FormInputWithHeader_header)
                val endDrawableIcon = getDrawable(R.styleable.FormInputWithHeader_endIcon)
                val inputEnabled = getBoolean(R.styleable.FormInputWithHeader_inputEnabled, true)
                val hint = getString(R.styleable.FormInputWithHeader_hint)
                val input = getString(R.styleable.FormInputWithHeader_input)
                val isRequired = getBoolean(R.styleable.FormInputWithHeader_required_input, true)
                val minHeight = getInt(R.styleable.FormInputWithHeader_minimumHeight, 0)
                val isMultipleLine = getBoolean(R.styleable.FormInputWithHeader_multiLine, false)
                setHeader(header)
                setEndDrawableIcon(endDrawableIcon)
                setInputEnabled(inputEnabled)
                setHint(hint)
                setInput(input)
                if (isMultipleLine) {
                    switchToMultiLined()
                }
                if (minHeight != 0) {
                    setMinimHeight(minHeight)
                }
                setRequiredInput(isRequired)
                if (isRequired) {
                    binding.etInput.doAfterTextChanged {
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

    fun setEndDrawableIcon(drawable: Drawable?) {
        drawable?.let {
            binding.ivEndIcon.setImageDrawable(it)
        }
    }

    fun switchToMultiLined() {
        binding.run {
            etInput.isSingleLine = false
            etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            etInput.maxLines = 3
            etInput.gravity = Gravity.START or Gravity.TOP
        }
    }

    fun setMinimHeight(height: Int) {
        binding.etInput.minimumHeight = height
    }

    fun setInputEnabled(isEnabled: Boolean) {
        binding.run {
            etInput.isEnabled = isEnabled
            etInput.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isEnabled) R.color.dark_grey_333333 else R.color.grey_828282
                )
            )
            clInput.setBackgroundResource(if (isEnabled) R.drawable.rect_white_fill_grey_outline_5dp else R.drawable.rect_grey_fill_grey_outline_5dp)
        }
    }

    fun setHint(hint: String?) {
        binding.etInput.hint = hint
    }

    fun setInput(text: String?) {
        binding.etInput.setText(text)
    }

    fun setRequiredInput(isRequired: Boolean) {
        this.isRequired = isRequired
        binding.tvRequiredAsterisk.isVisible = isRequired
    }

    fun setImeOptionType(imeOption: Int) {
        binding.etInput.imeOptions = imeOption
    }

    fun setInputType(inputType: Int) {
        binding.etInput.inputType = inputType
    }

    fun setMultiLined(maxLength: Int) {
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && maxLength > 0 && s.length >= maxLength) {
                    val cursorPos = binding.etInput.selectionEnd
                    val nextLineIndex = s.toString().indexOf('\n', cursorPos)
                    if (nextLineIndex < 0 || nextLineIndex > cursorPos + 1) {
                        s.append('\n')
                    }
                }
            }
        })
    }

    fun setMaxLines(maxLine: Int) {
        binding.etInput.maxLines = maxLine
    }

    fun setMaxLength(maxLength: Int) {
        binding.etInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
    }

    fun setLayoutListener(isFocusable: Boolean, listener: () -> Unit) {
        binding.run {
            etInput.isFocusable = isFocusable
            etInput.setNonDuplicateClickListener {
                listener.invoke()
            }
            root.setNonDuplicateClickListener {
                listener.invoke()
            }
            etInput.doAfterTextChanged {
                val input = it.toString()
                setRequiredInput(input.isEmpty())
            }
        }
    }

    fun setUserInputListener(listener: ((input: String) -> Unit)? = null) {
        binding.etInput.doAfterTextChanged {
            val input = it.toString().trim()
            listener?.invoke(input)
        }
    }
}