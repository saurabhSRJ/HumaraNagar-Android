package com.humara.nagar.ui.report.complaints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humara.nagar.R
import com.humara.nagar.Role
import com.humara.nagar.adapter.ComplaintsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentComplaintsBinding
import com.humara.nagar.ui.report.model.ComplaintDetails

class ComplaintsFragment : BaseFragment() {
    companion object {
        const val RELOAD_COMPLAINTS_LIST = "reload_list"
        const val IS_ADMIN = "is_admin"
    }

    private var _binding: FragmentComplaintsBinding? = null
    private val complaintsViewModel by viewModels<ComplaintsViewModel> {
        ViewModelFactory()
    }
    private val navController: NavController by lazy { findNavController() }
    private val complaintsAdapter: ComplaintsAdapter by lazy {
        ComplaintsAdapter(Role.isLocalAdmin(getUserPreference().role?.id ?: 0)) { complaintId ->
            //Handle on click (Pass complain_id: String)
            val action = ComplaintsFragmentDirections.actionComplaintsToComplaintStatus(complaintId, getScreenName())
            navController.navigate(action)
        }
    }
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComplaintsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController.previousBackStackEntry?.savedStateHandle?.set(IS_ADMIN, Role.isLocalAdmin(getUserPreference().role?.id ?: 0))
        navController.currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<Boolean>(RELOAD_COMPLAINTS_LIST).observe(viewLifecycleOwner) { shouldReload ->
                if (shouldReload) {
                    complaintsViewModel.getAllComplaints()
                    // To handle a result only once, you must call remove() on the SavedStateHandle to clear the result.
                    // If you do not remove the result, the LiveData will be triggered each time we come back to this fragment
                    remove<Boolean>(RELOAD_COMPLAINTS_LIST)
                }
            }
        }
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        complaintsViewModel.run {
            observeProgress(this, false)
            observeException(this)
            allComplaintLiveData.observe(viewLifecycleOwner) {
                val list = it.complaints
                if (list.isEmpty()) {
                    showNoComplaintsView()
                } else {
                    showAllComplaints(list)
                }
            }
            errorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog()
            }
        }
    }

    private fun showAllComplaints(complaints: ArrayList<ComplaintDetails>) {
        binding.complaintsRCV.visibility = View.VISIBLE
        complaintsAdapter.submitList(complaints)
    }

    private fun showNoComplaintsView() {
        binding.clNoComplaints.visibility = View.VISIBLE
    }

    private fun initView() {
        binding.run {
            //Setting up the toolbar
            includedToolbar.toolbarTitle.text = if (Role.isLocalAdmin(getUserPreference().role?.id ?: 0)) getString(R.string.all_complaints) else getString(R.string.past_complaints)
            includedToolbar.rightIconTv.apply {
                text = resources.getString(R.string.history)
                visibility = View.VISIBLE
            }
            includedToolbar.leftIcon.setOnClickListener {
                navController.navigateUp()
            }
            complaintsRCV.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = complaintsAdapter
                setHasFixedSize(true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.COMPLAINTS_FRAGMENT
}