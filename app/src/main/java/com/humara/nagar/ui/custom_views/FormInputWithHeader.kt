package com.humara.nagar.ui.custom_views

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.InputType
import android.util.AttributeSet
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.humara.nagar.R
import com.humara.nagar.databinding.ItemFormInputWithHeaderBinding
import com.humara.nagar.utils.restoreChildViewStates
import com.humara.nagar.utils.saveChildViewStates
import com.humara.nagar.utils.setMaxLength
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
                val maxLength = getInt(R.styleable.FormInputWithHeader_maximumLength, 0)
                setHeader(header)
                setEndDrawableIcon(endDrawableIcon)
                setInputEnabled(inputEnabled)
                setHint(hint)
                setInput(input)
                if (minHeight != 0) {
                    setMinimHeight(minHeight)
                }
                if (maxLength != 0) {
                    setMaxLength(maxLength)
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
        binding.run {
            tvHeader.visibility = View.VISIBLE
            tvHeader.text = header
        }
    }

    fun setEndDrawableIcon(endDrawable: Drawable?) {
        // Create a custom drawable with fixed size
        endDrawable?.let { drawable ->
            val width = resources.getDimensionPixelSize(R.dimen.end_drawable_width)
            val height = resources.getDimensionPixelSize(R.dimen.end_drawable_height)
            drawable.setBounds(0, 0, width, height)
            binding.etInput.setCompoundDrawablesRelative(null, null, drawable, null)
        }
    }

    fun switchToMultiLined(maxLine: Int) {
        binding.run {
            etInput.isSingleLine = false
            etInput.inputType =  InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            etInput.maxLines = maxLine
            etInput.gravity = Gravity.START or Gravity.TOP
        }
    }

    fun scrollToTopOnFocusChange() {
        binding.run {
            etInput.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus.not()) {
                    etInput.scrollTo(0, 0)
                }
            }
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
            tilInput.boxBackgroundColor = ContextCompat.getColor(context, if (isEnabled) R.color.primary_background else R.color.grey_F5F5F5)
//            clInput.setBackgroundResource(if (isEnabled) R.drawable.rect_white_fill_grey_outline_5dp else R.drawable.rect_grey_fill_grey_outline_5dp)
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

    private fun setMaxLength(maxLength: Int) {
        binding.run {
            etInput.setMaxLength(maxLength)
            tilInput.isCounterEnabled = true
            tilInput.counterMaxLength = maxLength
        }
    }

    fun setLayoutListener(isFocusable: Boolean, listener: () -> Unit) {
        binding.run {
            etInput.setOnFocusChangeListener { view: View, hasFocus: Boolean ->
                if (view.isInTouchMode && hasFocus) {
                    //Once the view gains focus, clear the focus if required and perform click so that click listener can be invoked. This way we avoid janky behaviour of keyboard
                    if (isFocusable.not()) {
                        view.clearFocus()
                    }
                    view.performClick()
                }
            }
            etInput.setNonDuplicateClickListener {
                listener.invoke()
            }
            root.setNonDuplicateClickListener {
                listener.invoke()
            }
        }
    }

    fun setUserInputListener(listener: ((input: String) -> Unit)) {
        binding.etInput.doAfterTextChanged {
            val input = it.toString().trim()
            listener.invoke(input)
        }
    }

    fun isInputEmpty(): Boolean = binding.etInput.text?.isEmpty() == true

    /**
     * In order to save the state of a custom view in case of configuration changes or process death below steps are needed.
     * Refer: https://www.netguru.com/blog/how-to-correctly-save-the-state-of-a-custom-view-in-android
     */
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable {
        return FormState(
            superSavedState = super.onSaveInstanceState(),
            childrenStates = saveChildViewStates()
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is FormState -> {
                super.onRestoreInstanceState(state.superSavedState)
                state.childrenStates?.let { restoreChildViewStates(it) }
            }
            else -> super.onRestoreInstanceState(state)
        }
    }
}
