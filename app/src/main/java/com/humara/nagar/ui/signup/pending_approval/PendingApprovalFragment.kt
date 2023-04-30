package com.humara.nagar.ui.signup.pending_approval

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.databinding.FragmentPendingApprovalBinding

class PendingApprovalFragment : BaseFragment() {
    private lateinit var binding: FragmentPendingApprovalBinding

    companion object {
        const val TAG = "PendingApprovalFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPendingApprovalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getScreenName() = AnalyticsData.ScreenName.PENDING_APPROVAL_FRAGMENT
}