package com.humara.nagar.ui.residents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humara.nagar.R
import com.humara.nagar.adapter.ResidentSearchAdapter
import com.humara.nagar.adapter.ResidentsAdapter
import com.humara.nagar.analytics.AnalyticsData
import com.humara.nagar.base.BaseFragment
import com.humara.nagar.base.ViewModelFactory
import com.humara.nagar.databinding.FragmentResidentsBinding
import com.humara.nagar.ui.common.EndlessRecyclerViewScrollListener
import com.humara.nagar.utils.setNonDuplicateClickListener
import com.humara.nagar.utils.textInputAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ResidentsFragment : BaseFragment() {
    private var _binding: FragmentResidentsBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val residentsAdapter: ResidentsAdapter by lazy {
        ResidentsAdapter { }
    }
    private val residentsViewModel: ResidentsViewModel by viewModels {
        ViewModelFactory()
    }
    private val residentsManagementViewModel by navGraphViewModels<ResidentsManagementViewModel>(R.id.residents_navigation) {
        ViewModelFactory()
    }
    private val searchAdapter: ResidentSearchAdapter by lazy {
        ResidentSearchAdapter { }
    }
    private lateinit var endlessRecyclerViewScrollListener: EndlessRecyclerViewScrollListener
    private val navController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResidentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModelObservers()
        initView()
    }

    private fun initViewModelObservers() {
        residentsViewModel.run {
            observeErrorAndException(this, errorAction = {}, dismissAction = {})
            initialDataLiveData.observe(viewLifecycleOwner) {
                residentsAdapter.setData(it)
            }
            loadMoreDataLiveData.observe(viewLifecycleOwner) {
                residentsAdapter.addMoreData(it)
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
            initialDataErrorLiveData.observe(viewLifecycleOwner) {
                showErrorDialog(subtitle = it.message, ctaText = getString(R.string.retry), errorAction = { reloadList() }, dismissAction = { })
            }
            loadMoreDataErrorLiveData.observe(viewLifecycleOwner) {
                showPaginationLoadError()
            }
            searchResultLiveData.observe(viewLifecycleOwner) {
                val list = it.first
                val searchText = it.second
                if (list.isEmpty()) {
                    searchAdapter.setData(listOf("No Result"), searchText)
                } else {
                    searchAdapter.setData(list, searchText)
                }
            }
            isSearchingLiveData.observe(viewLifecycleOwner) {
                binding.pbSearch.isVisible = it
            }
        }
        residentsManagementViewModel.userAdditionSuccessLiveData.observe(viewLifecycleOwner) {
            reloadList()
        }
    }

    private fun initView() {
        binding.run {
            rvResidents.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = residentsAdapter
                endlessRecyclerViewScrollListener = object : EndlessRecyclerViewScrollListener(layoutManager!!, 2) {
                    override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                        if (residentsViewModel.canLoadMoreData) {
                            residentsViewModel.getAllResidents(true)
                        }
                    }
                }
                addOnScrollListener(endlessRecyclerViewScrollListener)
            }
            etSearch.textInputAsFlow()
                .map {
                    val searchBarIsEmpty = it.isNullOrEmpty()
                    ivCancelSearch.isVisible = searchBarIsEmpty.not()
                    rvSearchResults.isVisible = searchBarIsEmpty.not()
                    rvResidents.isVisible = searchBarIsEmpty
                    return@map it
                }
                .debounce(800)
                .onEach {
                    val searchText = it.toString().trim()
                    if (searchText.isEmpty()) {
                        searchAdapter.clearData()
                    } else {
                        residentsViewModel.searchResidentList(searchText)
                    }
                }
                .launchIn(lifecycleScope)
            ivCancelSearch.setOnClickListener {
                hideKeyboard()
                etSearch.setText("")
                etSearch.clearFocus()
                pbSearch.visibility = View.GONE
                ivCancelSearch.visibility = View.GONE
                rvResidents.visibility = View.VISIBLE
                rvSearchResults.visibility = View.GONE
            }
            rvSearchResults.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = searchAdapter
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            hideKeyboard()
                        }
                    }
                })
            }
            etSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                }
                false
            }
            btnBack.setOnClickListener { navController.navigateUp() }
            btnAddUser.setNonDuplicateClickListener {
                navController.navigate(ResidentsFragmentDirections.actionResidentsFragmentToAddUserNavigation())
            }
            swipeRefresh.setOnRefreshListener {
                lifecycleScope.launch {
                    reloadList()
                    swipeRefresh.isRefreshing = false
                }
            }
            swipeRefresh.setColorSchemeResources(R.color.primary_color, R.color.stroke_green, R.color.stroke_yellow, R.color.stroke_red)
            paginationLoader.retry.setNonDuplicateClickListener {
                residentsViewModel.getAllResidents(true)
            }
        }
    }

    private fun reloadList() {
        endlessRecyclerViewScrollListener.resetState()
        residentsViewModel.getAllResidents()
        binding.paginationLoader.retry.visibility = View.GONE
    }

    private fun handleInitialProgress(showProgress: Boolean) {
        if (showProgress) {
            binding.run {
                progressBar.visibility = View.VISIBLE
                rvResidents.visibility = View.GONE
            }
        } else {
            binding.run {
                progressBar.visibility = View.GONE
                rvResidents.visibility = View.VISIBLE
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getScreenName() = AnalyticsData.ScreenName.RESIDENTS_FRAGMENT
}