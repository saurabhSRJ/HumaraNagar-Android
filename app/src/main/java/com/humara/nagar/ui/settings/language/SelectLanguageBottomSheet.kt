package com.humara.nagar.ui.settings.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humara.nagar.R
import com.humara.nagar.SplashActivity
import com.humara.nagar.adapter.SelectLanguageAdapter
import com.humara.nagar.databinding.BottomSheetSelectLanguageBinding
import com.humara.nagar.utils.getAppSharedPreferences
import com.humara.nagar.utils.getStringByLocale
import com.humara.nagar.utils.setNonDuplicateClickListener
import java.util.*

class SelectLanguageBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetSelectLanguageBinding
    private val languageAdapter: SelectLanguageAdapter by lazy {
        SelectLanguageAdapter(requireContext(), AppLanguage.values()) {
            selectedLanguage = AppLanguage.getLanguageCode(it)
            onLanguageSelected()
        }
    }
    private lateinit var selectedLanguage: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetSelectLanguageBinding.inflate(inflater, container, false)
        isCancelable = true
        selectedLanguage = requireContext().getAppSharedPreferences().appLanguage
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.run {
            setTextByLocale()
            ivClose.setOnClickListener { dismiss() }
            rvSelectLanguage.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = languageAdapter
            }
            btnOk.setNonDuplicateClickListener {
                updateLanguage()
            }
        }
    }

    private fun updateLanguage() {
        requireContext().getAppSharedPreferences().appLanguage = selectedLanguage
        SplashActivity.start(requireContext())
        dismiss()
    }

    private fun onLanguageSelected() {
        binding.btnOk.isEnabled = true
        setTextByLocale()
    }

    private fun setTextByLocale() {
        val locale = Locale(selectedLanguage)
        binding.run {
            btnOk.text = context?.getStringByLocale(R.string.ok, locale)
            tvSelectLanguageTitle.text = context?.getStringByLocale(R.string.choose_language, locale)
        }
    }
}