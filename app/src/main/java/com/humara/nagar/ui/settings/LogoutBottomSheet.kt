package com.humara.nagar.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseActivity
import com.humara.nagar.databinding.BottomSheetLogoutBinding

class LogoutBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetLogoutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetLogoutBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            ivClose.setOnClickListener { dismiss() }
            btnGoBack.setOnClickListener { dismiss() }
            btnLogout.setOnClickListener {
                (activity as? BaseActivity)?.logout(AnalyticsData.ScreenName.LOGOUT_BOTTOM_SHEET)
                dismiss()
            }
        }
    }
}