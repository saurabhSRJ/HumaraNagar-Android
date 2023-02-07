package com.humara.nagar.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.humara.nagar.Logger
import com.humara.nagar.R
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentReportBinding

class ReportFragment : BaseFragment() {

    private var _binding: FragmentReportBinding? = null
    private val reportViewModel by activityViewModels<ReportViewModel> {
        ViewModelFactory()
    }
    private lateinit var toolBarTitle: TextView
    private lateinit var toolBarBackButton: ImageView
    private lateinit var toolBarHistoryButton: ImageView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        initView()
        initViewModelObservers()



        return binding.root
    }

    private fun initViewModelObservers() {
        reportViewModel.run {
            enableSubmitButtonLiveData.observe(viewLifecycleOwner) {
                binding.btnSubmit.isEnabled = it
            }
        }
    }

    private fun initView() {

        binding.run {

            //Setting up the top app bar title
            toolBarTitle = root.findViewById(R.id.toolbar_title)
            toolBarTitle.text = resources.getString(R.string.reportIssueTitle)
            toolBarBackButton = root.findViewById(R.id.leftIcon)
            toolBarHistoryButton = root.findViewById(R.id.rightIcon)

            //Settings up list for spinners
            inputCategory.setOptions(R.array.category_list)
            inputLocality.setOptions(R.array.locality_list)
            inputLocality.setRequiredInput(true)
            inputComment.apply {
                switchToMultiLined()
            }

            inputCategory.setUserInputListener {
                reportViewModel.setCategory(it)
                Logger.debugLog(it)
            }
            inputLocality.setUserInputListener {
                reportViewModel.setLocality(it)
                Logger.debugLog(it)
            }
            inputLocation.setUserInputListener {
                reportViewModel.setLocation(it)
                Logger.debugLog(it)
            }
            inputCategory.setUserInputListener {
                reportViewModel.setComment(it)
                Logger.debugLog(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName()= AnalyticsData.ScreenName.REPORT_FRAGMENT
}