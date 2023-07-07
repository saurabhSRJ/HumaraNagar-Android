package com.humara.nagar.ui.report.complaints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.Role
import com.humara.nagar.adapter.ComplaintsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentComplaintsBinding
import com.humara.nagar.databinding.LayoutFilterItemBinding
import com.humara.nagar.ui.common.EndlessRecyclerViewScrollListener
import com.humara.nagar.ui.report.model.ComplaintDetails
import com.humara.nagar.utils.setNonDuplicateClickListener
import kotlinx.coroutines.launch

class ComplaintsFragment : BaseFragment() {
    companion object {
        const val IS_ADMIN = "is_admin"
    }

    private var _binding: FragmentComplaintsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val complaintsViewModel by viewModels<ComplaintsViewModel> {
        ViewModelFactory()
    }
    private val complaintManagementViewModel by navGraphViewModels<ComplaintManagementViewModel>(R.id.navigation_report) {
        ViewModelFactory()
    }
    private val navController: NavController by lazy { findNavController() }
    private val complaintsAdapter: ComplaintsAdapter by lazy {
        ComplaintsAdapter(Role.isLocalAdmin(getUserPreference().role?.id ?: 0)) { complaintId ->
            val action = ComplaintsFragmentDirections.actionComplaintsToComplaintStatus(complaintId, getScreenName())
            navController.navigate(action)
        }
    }
    private lateinit var endlessRecyclerViewScrollListener: EndlessRecyclerViewScrollListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (_binding == null) {
            _binding = FragmentComplaintsBinding.inflate(inflater, container, false)
        }
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
        navController.previousBackStackEntry?.savedStateHandle?.set(IS_ADMIN, Role.isLocalAdmin(getUserPreference().role?.id ?: 0))
    }

    private fun initViewModelObservers() {
        complaintsViewModel.run {
            observeProgress(this, false)
            observeException(this)
            selectedFilterLiveData.observe(viewLifecycleOwner) { id ->
                setFilterUI(id)
            }
            initialDataProgressLiveData.observe(viewLifecycleOwner) {
                handleInitialProgress(it)
            }
            loadMoreDataProgressLiveData.observe(viewLifecycleOwner) { progress ->
                if (progress) {
                    showPaginationLoader()
                } else {
                    hidePaginationLoader()
                }
            }
            initialDataLiveData.observe(viewLifecycleOwner) {
                val list = it.complaints
                addFilterData(it.totalResolved, it.totalPending)
                if (list.isNullOrEmpty()) {
                    showNoComplaintsView()
                } else {
                    showAllComplaints(list)
                }
            }
            loadMoreDataLiveData.observe(viewLifecycleOwner) {
                it.complaints?.let { complaints ->
                    complaintsAdapter.addMoreData(complaints)
                }
            }
            initialDataErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, ctaText = getString(R.string.retry), errorAction = { reloadList() }, dismissAction = { })
            }
            loadMoreDataErrorLiveData.observe(viewLifecycleOwner) {
                showPaginationLoadError()
            }
            errorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog()
            }
        }
        complaintManagementViewModel.complaintsListReloadLiveData.observe(viewLifecycleOwner) {
            reloadList()
        }
    }

    private fun showAllComplaints(complaints: List<ComplaintDetails>) {
        binding.run {
            clNoComplaints.visibility = View.GONE
            complaintsRCV.visibility = View.VISIBLE
        }
        complaintsAdapter.setData(complaints)
    }

    private fun showNoComplaintsView() {
        binding.run {
            clNoComplaints.visibility = View.VISIBLE
            complaintsRCV.visibility = View.GONE
        }
    }

    private fun initView() {
        binding.run {
            toolbar.apply {
                toolbarTitle.text = if (Role.isLocalAdmin(getUserPreference().role?.id ?: 0)) getString(R.string.all_complaints) else getString(R.string.past_complaints)
                leftIcon.setOnClickListener {
                    navController.navigateUp()
                }
            }
            pendingFilter.apply {
                tvTitle.text = getString(R.string.pending)
                clContainer.setNonDuplicateClickListener {
                    if (complaintsViewModel.selectedFilterLiveData.value != ComplaintStateFilter.PENDING.id) {
                        complaintsViewModel.setSelectedFilterId(ComplaintStateFilter.PENDING.id)
                        reloadList()
                    } else {
                        complaintsRCV.smoothScrollToPosition(0)
                    }
                }
            }
            resolvedFilter.apply {
                tvTitle.text = getString(R.string.finished)
                clContainer.setNonDuplicateClickListener {
                    if (complaintsViewModel.selectedFilterLiveData.value != ComplaintStateFilter.FINISHED.id) {
                        complaintsViewModel.setSelectedFilterId(ComplaintStateFilter.FINISHED.id)
                        reloadList()
                    } else {
                        complaintsRCV.smoothScrollToPosition(0)
                    }
                }
            }
            complaintsRCV.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = complaintsAdapter
                endlessRecyclerViewScrollListener = object : EndlessRecyclerViewScrollListener(layoutManager!!, 2) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        if (complaintsViewModel.canLoadMoreData) {
                            complaintsViewModel.getComplaints(true)
                        }
                    }
                }
                addOnScrollListener(endlessRecyclerViewScrollListener)
            }
            swipeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    reloadList()
                    swipeRefresh.isRefreshing = false
                }
            }
            swipeRefresh.setColorSchemeResources(R.color.primary_color, R.color.stroke_green, R.color.stroke_yellow, R.color.stroke_red)
            paginationLoader.retry.setNonDuplicateClickListener {
                complaintsViewModel.getComplaints(true)
            }
        }
    }

    private fun setFilterUI(filterId: Int) {
        val selectedFilterView: LayoutFilterItemBinding = if (filterId == ComplaintStateFilter.PENDING.id) binding.pendingFilter else binding.resolvedFilter
        selectedFilterView.run {
            clContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.feed_filter_active_bg)
            tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
            tvCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_color))
        }
        val unselectedFilterView: LayoutFilterItemBinding = if (filterId == ComplaintStateFilter.PENDING.id) binding.resolvedFilter else binding.pendingFilter
        unselectedFilterView.run {
            clContainer.background = ContextCompat.getDrawable(requireContext(), R.drawable.feed_filter_inactive_bg)
            tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_696969))
            tvCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_696969))
        }
    }

    private fun addFilterData(resolvedCount: Int, pendingCount: Int) {
        binding.run {
            resolvedFilter.tvCount.apply {
                visibility = View.VISIBLE
                text = getString(R.string.count_with_bracket, resolvedCount)
            }
            pendingFilter.tvCount.apply {
                visibility = View.VISIBLE
                text = getString(R.string.count_with_bracket, pendingCount)
            }
        }
    }

    private fun handleInitialProgress(showProgress: Boolean) {
        binding.run {
            shimmerLayout.isVisible = showProgress
            complaintsRCV.isVisible = showProgress.not()
            clNoComplaints.isVisible = showProgress.not()
            if (showProgress) shimmerLayout.startShimmer() else shimmerLayout.stopShimmer()
        }
    }

    private fun showPaginationLoader() {
        binding.paginationLoader.apply {
            progress.visibility = View.VISIBLE
            retry.visibility = View.GONE
        }
    }

    private fun hidePaginationLoader() {
        binding.paginationLoader.apply {
            progress.visibility = View.GONE
            retry.visibility = View.GONE
        }
    }

    private fun showPaginationLoadError() {
        binding.paginationLoader.retry.visibility = View.VISIBLE
    }

    private fun reloadList() {
        endlessRecyclerViewScrollListener.resetState()
        complaintsViewModel.getComplaints()
        binding.paginationLoader.retry.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.COMPLAINTS_FRAGMENT
}