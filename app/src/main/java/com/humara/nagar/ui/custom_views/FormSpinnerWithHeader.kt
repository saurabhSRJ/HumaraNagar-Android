package com.humara.nagar.ui.custom_views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.humara.nagar.R
import com.humara.nagar.databinding.ItemFormSpinnerWithHeaderBinding
import com.humara.nagar.utils.restoreChildViewStates
import com.humara.nagar.utils.saveChildViewStates

class FormSpinnerWithHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var binding: ItemFormSpinnerWithHeaderBinding
    private var isRestoredView = false

    init {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        binding = ItemFormSpinnerWithHeaderBinding.inflate(LayoutInflater.from(context), this, true)
        context.obtainStyledAttributes(attrs, R.styleable.FormSpinnerWithHeader).apply {
            try {
                val header = getString(R.styleable.FormSpinnerWithHeader_header)
                val hint = getString(R.styleable.FormSpinnerWithHeader_hint)
                val input = binding.spinnerTV.text.toString()
                setHeader(header)
                setHint(hint)
                setInput(input)
            } finally {
                recycle()
            }
        }
    }

    private fun setHeader(header: String?) {
        binding.tvHeader.text = header
    }

    private fun setHint(hint: String?) {
        binding.spinnerTV.hint = hint
    }

    fun setInput(text: String?) {
        binding.spinnerTV.setText(text)
        binding.tvRequiredAsterisk.isVisible = text.isNullOrEmpty()
    }

    fun clearInput() {
        binding.run {
            spinnerTV.text.clear()
            tvRequiredAsterisk.isVisible = true
            tvError.isVisible = false
        }
    }

    fun setOptions(options: Array<Any>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, options)
        binding.spinnerTV.run {
            setAdapter(adapter)
        }
    }

    fun setDefaultSelection(itemIndex: Int, listener: ((input: Any) -> Unit)) {
        if (itemIndex == -1) return
        binding.run {
            val item = spinnerTV.adapter.getItem(itemIndex)
            spinnerTV.setText(item.toString(), false)
            tvError.visibility = View.GONE
            tvRequiredAsterisk.visibility = View.GONE
            listener(item)
        }
    }

    fun setUserInputListener(listener: ((input: Any) -> Unit)? = null) {
        binding.run {
            spinnerTV.setOnItemClickListener { parent, _, position, _ ->
                val input = parent.getItemAtPosition(position)
                tvRequiredAsterisk.visibility = View.GONE
                tvError.visibility = View.GONE
                listener?.invoke(input)
            }
            binding.spinnerTV.doAfterTextChanged {
                if (isRestoredView) {
                    isRestoredView = false
                    tvRequiredAsterisk.isVisible = it.toString().trim().isEmpty()
                    tvError.visibility = View.GONE
                } else {
                    tvRequiredAsterisk.visibility = View.VISIBLE
                    tvError.visibility = View.VISIBLE
                    listener?.invoke("")
                }
            }
        }
    }

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
        return FormState(superSavedState = super.onSaveInstanceState(), childrenStates = saveChildViewStates())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        isRestoredView = true
        when (state) {
            is FormState -> {
                super.onRestoreInstanceState(state.superSavedState)
                state.childrenStates?.let { restoreChildViewStates(it) }
            }
            else -> super.onRestoreInstanceState(state)
        }
    }
}