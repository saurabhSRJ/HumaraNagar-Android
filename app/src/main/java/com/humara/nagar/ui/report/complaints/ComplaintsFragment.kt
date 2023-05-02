package com.humara.nagar.ui.report.complaints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.adapter.ComplaintsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentComplaintsBinding
import com.humara.nagar.databinding.ToolbarLayoutBinding

class ComplaintsFragment : BaseFragment() {

    private var _binding: FragmentComplaintsBinding? = null
    private lateinit var toolbar: ToolbarLayoutBinding
    private val complaintsViewModel by viewModels<ComplaintsViewModel> {
        ViewModelFactory()
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var complaintsAdapter: ComplaintsAdapter
    private var isCurrentUserAdmin = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentComplaintsBinding.inflate(layoutInflater, container, false)

        initViewModelObservers()
        initView()

        //Back button
        binding.includedToolbar.leftIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun initView() {

        isCurrentUserAdmin = getUserPreference().isAdminUser

        binding.apply {

            //Setting up the top app bar title
            toolbar = includedToolbar
            if (isCurrentUserAdmin) {
                toolbar.toolbarTitle.text = resources.getString(R.string.all_complaints)
            } else {
                toolbar.toolbarTitle.text = resources.getString(R.string.past_complaints)
            }
            toolbar.rightIconTv.apply {
                text = resources.getString(R.string.history)
                visibility = View.VISIBLE
            }

            //Set-up recyclerview and adapter
            recyclerView = complaintsRCV
            complaintsAdapter = ComplaintsAdapter(getUserPreference().isAdminUser) {
                // Handle on click (Pass complain_id: String, when the API is completed)
                val action =
                    ComplaintsFragmentDirections.actionComplaintsFragmentToComplaintStatusFragment(
                        it
                    )
                findNavController().navigate(action)
            }
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = complaintsAdapter
            }
        }
    }

    private fun initViewModelObservers() {
        //Initialize ViewModel Observers here
        complaintsViewModel.run {
            observeProgress(this, false)
            observerException(this)
            getAllComplaints()

            allComplaintLiveData.observe(viewLifecycleOwner) {
                complaintsAdapter.clearAllData()
                complaintsAdapter.addData(it.complaints)
            }
            errorLiveData.observe(viewLifecycleOwner) {
                val errorString = StringBuilder()
                errorString.append(resources.getString(R.string.anErrorOccurred))
                    .append(" ${it.message}")
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.COMPLAINTS_FRAGMENT
}